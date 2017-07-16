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

import proguard.classfile.ClassConstants;
import proguard.classfile.LibraryClass;
import proguard.classfile.ProgramClass;
import proguard.classfile.constant.Constant;
import proguard.classfile.editor.ConstantPoolRemapper;
import proguard.classfile.visitor.ClassVisitor;


/**
 * This ClassVisitor removes NameAndType constant pool entries
 * that are not marked as being used.
 *
 * @see NameAndTypeUsageMarker
 *
 * @author Eric Lafortune
 */
public class NameAndTypeShrinker implements ClassVisitor
{
    private int[]                constantIndexMap;
    private final ConstantPoolRemapper constantPoolRemapper = new ConstantPoolRemapper();


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Shift the used constant pool entries together, filling out the
        // index map.
        programClass.u2constantPoolCount =
            shrinkConstantPool(programClass.constantPool,
                               programClass.u2constantPoolCount);


        // Remap all constant pool references.
        constantPoolRemapper.setConstantIndexMap(constantIndexMap);
        constantPoolRemapper.visitProgramClass(programClass);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }


    // Small utility methods.

    /**
     * Removes all NameAndType entries that are not marked as being used
     * from the given constant pool.
     * @return the new number of entries.
     */
    private int shrinkConstantPool(Constant[] constantPool, int length)
    {
        // Create a new index map, if necessary.
        if (constantIndexMap == null ||
            constantIndexMap.length < length)
        {
            constantIndexMap = new int[length];
        }

        int     counter = 1;
        boolean isUsed  = false;

        // Shift the used constant pool entries together.
        for (int index = 1; index < length; index++)
        {
            constantIndexMap[index] = counter;

            Constant constant = constantPool[index];

            // Don't update the flag if this is the second half of a long entry.
            if (constant != null)
            {
                isUsed = constant.getTag() != ClassConstants.CONSTANT_NameAndType ||
                         NameAndTypeUsageMarker.isUsed(constant);
            }

            if (isUsed)
            {
                constantPool[counter++] = constant;
            }
        }

        // Clear the remaining constant pool elements.
        for (int index = counter; index < length; index++)
        {
            constantPool[index] = null;
        }

        return counter;
    }
}
