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

import proguard.classfile.Clazz;
import proguard.classfile.ProgramClass;
import proguard.classfile.VisitorAccepter;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.EnclosingMethodAttribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;

/**
 * This ClassVisitor marks all NameAndType constant pool entries that are
 * being used in the program classes it visits.
 *
 * @see NameAndTypeShrinker
 *
 * @author Eric Lafortune
 */
public class NameAndTypeUsageMarker
extends SimplifiedVisitor
implements   ClassVisitor,
        ConstantVisitor,
             AttributeVisitor
{
    // A visitor info flag to indicate the NameAndType constant pool entry is being used.
    private static final Object USED = new Object();


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Mark the NameAndType entries referenced by all other constant pool
        // entries.
        programClass.constantPoolEntriesAccept(this);

        // Mark the NameAndType entries referenced by all EnclosingMethod
        // attributes.
        programClass.attributesAccept(this);
    }


    // Implementations for ConstantVisitor.

    public void visitAnyConstant(Clazz clazz, Constant constant) {}


    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        markNameAndTypeConstant(clazz, refConstant.u2nameAndTypeIndex);
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}


    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        if (enclosingMethodAttribute.u2nameAndTypeIndex != 0)
        {
            markNameAndTypeConstant(clazz, enclosingMethodAttribute.u2nameAndTypeIndex);
        }
    }


    // Small utility methods.

    /**
     * Marks the given UTF-8 constant pool entry of the given class.
     */
    private void markNameAndTypeConstant(Clazz clazz, int index)
    {
         markAsUsed((NameAndTypeConstant)((ProgramClass)clazz).getConstant(index));
    }


    /**
     * Marks the given VisitorAccepter as being used.
     * In this context, the VisitorAccepter will be a NameAndTypeConstant object.
     */
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }


    /**
     * Returns whether the given VisitorAccepter has been marked as being used.
     * In this context, the VisitorAccepter will be a NameAndTypeConstant object.
     */
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
