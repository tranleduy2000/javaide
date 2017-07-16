/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2011 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.obfuscate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import proguard.ClassSpecificationVisitorFactory;
import proguard.Configuration;
import proguard.classfile.ClassConstants;
import proguard.classfile.ClassPool;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.attribute.visitor.AllInnerClassesInfoVisitor;
import proguard.classfile.attribute.visitor.AttributeNameFilter;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.NonEmptyAttributeFilter;
import proguard.classfile.attribute.visitor.RequiredAttributeFilter;
import proguard.classfile.constant.visitor.AllConstantVisitor;
import proguard.classfile.editor.AccessFixer;
import proguard.classfile.editor.BridgeMethodFixer;
import proguard.classfile.editor.ClassReferenceFixer;
import proguard.classfile.editor.InnerClassesAccessFixer;
import proguard.classfile.editor.MemberReferenceFixer;
import proguard.classfile.util.MethodLinker;
import proguard.classfile.util.WarningPrinter;
import proguard.classfile.visitor.AllMemberVisitor;
import proguard.classfile.visitor.AllMethodVisitor;
import proguard.classfile.visitor.BottomClassFilter;
import proguard.classfile.visitor.ClassCleaner;
import proguard.classfile.visitor.ClassHierarchyTraveler;
import proguard.classfile.visitor.ClassPoolVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberAccessFilter;
import proguard.classfile.visitor.MultiClassVisitor;
import proguard.util.ListParser;
import proguard.util.NameParser;

/**
 * This class can perform obfuscation of class pools according to a given
 * specification.
 *
 * @author Eric Lafortune
 */
public class Obfuscator
{
    private final Configuration configuration;


    /**
     * Creates a new Obfuscator.
     */
    public Obfuscator(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Performs obfuscation of the given program class pool.
     */
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        // Check if we have at least some keep commands.
        if (configuration.keep         == null &&
            configuration.applyMapping == null &&
            configuration.printMapping == null)
        {
            throw new IOException("You have to specify '-keep' options for the obfuscation step.");
        }

        // Clean up any old visitor info.
        programClassPool.classesAccept(new ClassCleaner());
        libraryClassPool.classesAccept(new ClassCleaner());

        // If the class member names have to correspond globally,
        // link all class members in all classes, otherwise
        // link all non-private methods in all class hierarchies.
        ClassVisitor memberInfoLinker =
            configuration.useUniqueClassMemberNames ?
                (ClassVisitor)new AllMemberVisitor(new MethodLinker()) :
                (ClassVisitor)new BottomClassFilter(new MethodLinker());

        programClassPool.classesAccept(memberInfoLinker);
        libraryClassPool.classesAccept(memberInfoLinker);

        // Create a visitor for marking the seeds.
        NameMarker nameMarker = new NameMarker();
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    nameMarker,
                                                                    nameMarker,
                                                                    false,
                                                                    false,
                                                                    true);
        // Mark the seeds.
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);

        // All library classes and library class members keep their names.
        libraryClassPool.classesAccept(nameMarker);
        libraryClassPool.classesAccept(new AllMemberVisitor(nameMarker));

        // Mark attributes that have to be kept.
        AttributeVisitor attributeUsageMarker =
            new NonEmptyAttributeFilter(
            new AttributeUsageMarker());

        AttributeVisitor optionalAttributeUsageMarker =
            configuration.keepAttributes == null ? null :
                new AttributeNameFilter(new ListParser(new NameParser()).parse(configuration.keepAttributes),
                                        attributeUsageMarker);

        programClassPool.classesAccept(
            new AllAttributeVisitor(true,
            new RequiredAttributeFilter(attributeUsageMarker,
                                        optionalAttributeUsageMarker)));

        // Keep parameter names and types if specified.
        if (configuration.keepParameterNames)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new MemberNameFilter(
                new AllAttributeVisitor(true,
                new ParameterNameMarker(attributeUsageMarker)))));
        }

        // Remove the attributes that can be discarded. Note that the attributes
        // may only be discarded after the seeds have been marked, since the
        // configuration may rely on annotations.
        programClassPool.classesAccept(new AttributeShrinker());

        // Apply the mapping, if one has been specified. The mapping can
        // override the names of library classes and of library class members.
        if (configuration.applyMapping != null)
        {
            WarningPrinter warningPrinter = new WarningPrinter(System.err, configuration.warn);

            MappingReader reader = new MappingReader(configuration.applyMapping);

            MappingProcessor keeper =
                new MultiMappingProcessor(new MappingProcessor[]
                {
                    new MappingKeeper(programClassPool, warningPrinter),
                    new MappingKeeper(libraryClassPool, null),
                });

            reader.pump(keeper);

            // Print out a summary of the warnings if necessary.
            int mappingWarningCount = warningPrinter.getWarningCount();
            if (mappingWarningCount > 0)
            {
                System.err.println("Warning: there were " + mappingWarningCount +
                                                            " kept classes and class members that were remapped anyway.");
                System.err.println("         You should adapt your configuration or edit the mapping file.");

                if (!configuration.ignoreWarnings)
                {
                    System.err.println("         If you are sure this remapping won't hurt,");
                    System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                    throw new IOException("Please correct the above warnings first.");
                }
            }
        }

        // Come up with new names for all classes.
        DictionaryNameFactory classNameFactory = configuration.classObfuscationDictionary != null ?
            new DictionaryNameFactory(configuration.classObfuscationDictionary, null) :
            null;

        DictionaryNameFactory packageNameFactory = configuration.packageObfuscationDictionary != null ?
            new DictionaryNameFactory(configuration.packageObfuscationDictionary, null) :
            null;

        programClassPool.classesAccept(
            new ClassObfuscator(programClassPool,
                                classNameFactory,
                                packageNameFactory,
                                configuration.useMixedCaseClassNames,
                                configuration.keepPackageNames,
                                configuration.flattenPackageHierarchy,
                                configuration.repackageClasses,
                                configuration.allowAccessModification));

        // Come up with new names for all class members.
        NameFactory nameFactory = new SimpleNameFactory();

        if (configuration.obfuscationDictionary != null)
        {
            nameFactory = new DictionaryNameFactory(configuration.obfuscationDictionary,
                                                    nameFactory);
        }

        WarningPrinter warningPrinter = new WarningPrinter(System.err, configuration.warn);

        // Maintain a map of names to avoid [descriptor - new name - old name].
        Map descriptorMap = new HashMap();

        // Do the class member names have to be globally unique?
        if (configuration.useUniqueClassMemberNames)
        {
            // Collect all member names in all classes.
            programClassPool.classesAccept(
                new AllMemberVisitor(
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)));

            // Assign new names to all members in all classes.
            programClassPool.classesAccept(
                new AllMemberVisitor(
                new MemberObfuscator(configuration.overloadAggressively,
                                     nameFactory,
                                     descriptorMap)));
        }
        else
        {
            // Come up with new names for all non-private class members.
            programClassPool.classesAccept(
                new MultiClassVisitor(new ClassVisitor[]
                {
                    // Collect all private member names in this class and down
                    // the hierarchy.
                    new ClassHierarchyTraveler(true, false, false, true,
                    new AllMemberVisitor(
                    new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                    // Collect all non-private member names anywhere in the hierarchy.
                    new ClassHierarchyTraveler(true, true, true, true,
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                    // Assign new names to all non-private members in this class.
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberObfuscator(configuration.overloadAggressively,
                                         nameFactory,
                                         descriptorMap))),

                    // Clear the collected names.
                    new MapCleaner(descriptorMap)
                }));

            // Come up with new names for all private class members.
            programClassPool.classesAccept(
                new MultiClassVisitor(new ClassVisitor[]
                {
                    // Collect all member names in this class.
                    new AllMemberVisitor(
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)),

                    // Collect all non-private member names higher up the hierarchy.
                    new ClassHierarchyTraveler(false, true, true, false,
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                    // Assign new names to all private members in this class.
                    new AllMemberVisitor(
                    new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberObfuscator(configuration.overloadAggressively,
                                         nameFactory,
                                         descriptorMap))),

                    // Clear the collected names.
                    new MapCleaner(descriptorMap)
                }));
        }

        // Some class members may have ended up with conflicting names.
        // Come up with new, globally unique names for them.
        NameFactory specialNameFactory =
            new SpecialNameFactory(new SimpleNameFactory());

        // Collect a map of special names to avoid
        // [descriptor - new name - old name].
        Map specialDescriptorMap = new HashMap();

        programClassPool.classesAccept(
            new AllMemberVisitor(
            new MemberSpecialNameFilter(
            new MemberNameCollector(configuration.overloadAggressively,
                                    specialDescriptorMap))));

        libraryClassPool.classesAccept(
            new AllMemberVisitor(
            new MemberSpecialNameFilter(
            new MemberNameCollector(configuration.overloadAggressively,
                                    specialDescriptorMap))));

        // Replace conflicting non-private member names with special names.
        programClassPool.classesAccept(
            new MultiClassVisitor(new ClassVisitor[]
            {
                // Collect all private member names in this class and down
                // the hierarchy.
                new ClassHierarchyTraveler(true, false, false, true,
                new AllMemberVisitor(
                new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),

                // Collect all non-private member names in this class and
                // higher up the hierarchy.
                new ClassHierarchyTraveler(true, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),

                // Assign new names to all conflicting non-private members
                // in this class and higher up the hierarchy.
                new ClassHierarchyTraveler(true, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameConflictFixer(configuration.overloadAggressively,
                                            descriptorMap,
                                            warningPrinter,
                new MemberObfuscator(configuration.overloadAggressively,
                                     specialNameFactory,
                                     specialDescriptorMap))))),

                // Clear the collected names.
                new MapCleaner(descriptorMap)
            }));

        // Replace conflicting private member names with special names.
        // This is only possible if those names were kept or mapped.
        programClassPool.classesAccept(
            new MultiClassVisitor(new ClassVisitor[]
            {
                // Collect all member names in this class.
                new AllMemberVisitor(
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)),

                // Collect all non-private member names higher up the hierarchy.
                new ClassHierarchyTraveler(false, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),

                // Assign new names to all conflicting private members in this
                // class.
                new AllMemberVisitor(
                new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberNameConflictFixer(configuration.overloadAggressively,
                                            descriptorMap,
                                            warningPrinter,
                new MemberObfuscator(configuration.overloadAggressively,
                                     specialNameFactory,
                                     specialDescriptorMap)))),

                // Clear the collected names.
                new MapCleaner(descriptorMap)
            }));

        // Print out any warnings about member name conflicts.
        int warningCount = warningPrinter.getWarningCount();
        if (warningCount > 0)
        {
            System.err.println("Warning: there were " + warningCount +
                               " conflicting class member name mappings.");
            System.err.println("         Your configuration may be inconsistent.");

            if (!configuration.ignoreWarnings)
            {
                System.err.println("         If you are sure the conflicts are harmless,");
                System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                throw new IOException("Please correct the above warnings first.");
            }
        }

        // Print out the mapping, if requested.
        if (configuration.printMapping != null)
        {
            PrintStream ps = isFile(configuration.printMapping) ?
                new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printMapping))) :
                System.out;

            // Print out items that will be removed.
            programClassPool.classesAcceptAlphabetically(new MappingPrinter(ps));

            if (ps != System.out)
            {
                ps.close();
            }
        }

        // Actually apply the new names.
        programClassPool.classesAccept(new ClassRenamer());
        libraryClassPool.classesAccept(new ClassRenamer());

        // Update all references to these new names.
        programClassPool.classesAccept(new ClassReferenceFixer(false));
        libraryClassPool.classesAccept(new ClassReferenceFixer(false));
        programClassPool.classesAccept(new MemberReferenceFixer());

        // Make package visible elements public or protected, if obfuscated
        // classes are being repackaged aggressively.
        if (configuration.repackageClasses != null &&
            configuration.allowAccessModification)
        {
            programClassPool.classesAccept(
                new AllConstantVisitor(
                new AccessFixer()));

            // Fix the access flags of the inner classes information.
            programClassPool.classesAccept(
                new AllAttributeVisitor(
                new AllInnerClassesInfoVisitor(
                new InnerClassesAccessFixer())));
        }

        // Fix the bridge method flags.
        programClassPool.classesAccept(
            new AllMethodVisitor(
            new BridgeMethodFixer()));

        // Rename the source file attributes, if requested.
        if (configuration.newSourceFileAttribute != null)
        {
            programClassPool.classesAccept(new SourceFileRenamer(configuration.newSourceFileAttribute));
        }

        // Mark NameAndType constant pool entries that have to be kept
        // and remove the other ones.
        programClassPool.classesAccept(new NameAndTypeUsageMarker());
        programClassPool.classesAccept(new NameAndTypeShrinker());

        // Mark Utf8 constant pool entries that have to be kept
        // and remove the other ones.
        programClassPool.classesAccept(new Utf8UsageMarker());
        programClassPool.classesAccept(new Utf8Shrinker());
    }


    /**
     * Returns whether the given file is actually a file, or just a placeholder
     * for the standard output.
     */
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }
}
