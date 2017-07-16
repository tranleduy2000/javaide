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

import java.io.PrintStream;

import proguard.classfile.ClassConstants;
import proguard.classfile.LibraryClass;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMember;
import proguard.classfile.ProgramMethod;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;


/**
 * This ClassVisitor prints out the renamed classes and class members with
 * their old names and new names.
 *
 * @see ClassRenamer
 *
 * @author Eric Lafortune
 */
public class MappingPrinter
extends SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor
{
    private final PrintStream ps;


    /**
     * Creates a new MappingPrinter that prints to <code>System.out</code>.
     */
    public MappingPrinter()
    {
        this(System.out);
    }


    /**
     * Creates a new MappingPrinter that prints to the given stream.
     * @param printStream the stream to which to print
     */
    public MappingPrinter(PrintStream printStream)
    {
        this.ps = printStream;
    }


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        String name    = programClass.getName();
        String newName = ClassObfuscator.newClassName(programClass);

        ps.println(ClassUtil.externalClassName(name) +
                   " -> " +
                   ClassUtil.externalClassName(newName) +
                   ":");

        // Print out the class members.
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        String newName = MemberObfuscator.newMemberName(programField);
        if (newName != null)
        {
            ps.println("    " +
                       //lineNumberRange(programClass, programField) +
                       ClassUtil.externalFullFieldDescription(
                           0,
                           programField.getName(programClass),
                           programField.getDescriptor(programClass)) +
                       " -> " +
                       newName);
        }
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        String name = programMethod.getName(programClass);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        String newName = MemberObfuscator.newMemberName(programMethod);
        if (newName != null)
        {
            ps.println("    " +
                       lineNumberRange(programClass, programMethod) +
                       ClassUtil.externalFullMethodDescription(
                           programClass.getName(),
                           0,
                           programMethod.getName(programClass),
                           programMethod.getDescriptor(programClass)) +
                       " -> " +
                       newName);
        }
    }


    // Small utility methods.

    /**
     * Returns the line number range of the given class member, followed by a
     * colon, or just an empty String if no range is available.
     */
    private static String lineNumberRange(ProgramClass programClass, ProgramMember programMember)
    {
        String range = programMember.getLineNumberRange(programClass);
        return range != null ?
            (range + ":") :
            "";
    }
}
