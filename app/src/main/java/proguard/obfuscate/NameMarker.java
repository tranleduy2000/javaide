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
import proguard.classfile.Clazz;
import proguard.classfile.Field;
import proguard.classfile.LibraryClass;
import proguard.classfile.LibraryField;
import proguard.classfile.LibraryMethod;
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMethod;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.InnerClassesAttribute;
import proguard.classfile.attribute.InnerClassesInfo;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.InnerClassesInfoVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;


/**
 * This <code>ClassVisitor</code> and <code>MemberVisitor</code>
 * marks names of the classes and class members it visits. The marked names
 * will remain unchanged in the obfuscation step.
 *
 * @see ClassObfuscator
 * @see MemberObfuscator
 *
 * @author Eric Lafortune
 */
class      NameMarker
extends SimplifiedVisitor
implements ClassVisitor,
           MemberVisitor,
        AttributeVisitor,
        InnerClassesInfoVisitor,
        ConstantVisitor
{
    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        keepClassName(programClass);

        // Make sure any outer class names are kept as well.
        programClass.attributesAccept(this);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
        keepClassName(libraryClass);
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        keepFieldName(programClass, programField);
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        keepMethodName(programClass, programMethod);
    }


    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        keepFieldName(libraryClass, libraryField);
    }


    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        keepMethodName(libraryClass, libraryMethod);
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}


    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        // Make sure the outer class names are kept as well.
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        // Make sure the outer class name is kept as well.
        int innerClassIndex = innerClassesInfo.u2innerClassIndex;
        int outerClassIndex = innerClassesInfo.u2outerClassIndex;
        if (innerClassIndex != 0 &&
            outerClassIndex != 0 &&
            clazz.getClassName(innerClassIndex).equals(clazz.getName()))
        {
            clazz.constantPoolEntryAccept(outerClassIndex, this);
        }
    }


    // Implementations for ConstantVisitor.

    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        // Make sure the outer class name is kept as well.
        classConstant.referencedClassAccept(this);
    }


    // Small utility method.

    /**
     * Ensures the name of the given class name will be kept.
     */
    public void keepClassName(Clazz clazz)
    {
        ClassObfuscator.setNewClassName(clazz,
                                        clazz.getName());
    }


    /**
     * Ensures the name of the given field name will be kept.
     */
    private void keepFieldName(Clazz clazz, Field field)
    {
        MemberObfuscator.setFixedNewMemberName(field,
                                               field.getName(clazz));
    }


    /**
     * Ensures the name of the given method name will be kept.
     */
    private void keepMethodName(Clazz clazz, Method method)
    {
        String name = method.getName(clazz);

        if (!name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            MemberObfuscator.setFixedNewMemberName(method,
                                                   method.getName(clazz));
        }
    }
}
