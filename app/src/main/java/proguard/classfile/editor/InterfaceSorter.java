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
package proguard.classfile.editor;

import java.util.Arrays;

import proguard.classfile.ProgramClass;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;

/**
 * This ClassVisitor sorts the interfaces of the program classes that it visits.
 *
 * @author Eric Lafortune
 */
public class InterfaceSorter
extends SimplifiedVisitor
implements   ClassVisitor
{
    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        int[] interfaces      = programClass.u2interfaces;
        int   interfacesCount = programClass.u2interfacesCount;

        // Sort the interfaces.
        Arrays.sort(interfaces, 0, interfacesCount);

        // Remove any duplicate entries.
        int newInterfacesCount     = 0;
        int previousInterfaceIndex = 0;
        for (int index = 0; index < interfacesCount; index++)
        {
            int interfaceIndex = interfaces[index];

            // Isn't this a duplicate of the previous interface?
            if (interfaceIndex != previousInterfaceIndex)
            {
                interfaces[newInterfacesCount++] = interfaceIndex;

                // Remember the interface.
                previousInterfaceIndex = interfaceIndex;
            }
        }

        programClass.u2interfacesCount = newInterfacesCount;
    }
}
