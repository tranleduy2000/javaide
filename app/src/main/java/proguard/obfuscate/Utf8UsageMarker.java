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
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramMember;
import proguard.classfile.VisitorAccepter;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ConstantValueAttribute;
import proguard.classfile.attribute.DeprecatedAttribute;
import proguard.classfile.attribute.EnclosingMethodAttribute;
import proguard.classfile.attribute.ExceptionsAttribute;
import proguard.classfile.attribute.InnerClassesAttribute;
import proguard.classfile.attribute.InnerClassesInfo;
import proguard.classfile.attribute.LineNumberTableAttribute;
import proguard.classfile.attribute.LocalVariableInfo;
import proguard.classfile.attribute.LocalVariableTableAttribute;
import proguard.classfile.attribute.LocalVariableTypeInfo;
import proguard.classfile.attribute.LocalVariableTypeTableAttribute;
import proguard.classfile.attribute.SignatureAttribute;
import proguard.classfile.attribute.SourceDirAttribute;
import proguard.classfile.attribute.SourceFileAttribute;
import proguard.classfile.attribute.SyntheticAttribute;
import proguard.classfile.attribute.UnknownAttribute;
import proguard.classfile.attribute.annotation.Annotation;
import proguard.classfile.attribute.annotation.AnnotationDefaultAttribute;
import proguard.classfile.attribute.annotation.AnnotationElementValue;
import proguard.classfile.attribute.annotation.AnnotationsAttribute;
import proguard.classfile.attribute.annotation.ArrayElementValue;
import proguard.classfile.attribute.annotation.ClassElementValue;
import proguard.classfile.attribute.annotation.ConstantElementValue;
import proguard.classfile.attribute.annotation.EnumConstantElementValue;
import proguard.classfile.attribute.annotation.ParameterAnnotationsAttribute;
import proguard.classfile.attribute.annotation.visitor.AnnotationVisitor;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.attribute.preverification.StackMapAttribute;
import proguard.classfile.attribute.preverification.StackMapTableAttribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.InnerClassesInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableTypeInfoVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.Utf8Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;

/**
 * This ClassVisitor marks all UTF-8 constant pool entries that are
 * being used in the program classes it visits.
 *
 * @see Utf8Shrinker
 *
 * @author Eric Lafortune
 */
public class Utf8UsageMarker
extends SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
        ConstantVisitor,
        AttributeVisitor,
             InnerClassesInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    // A visitor info flag to indicate the UTF-8 constant pool entry is being used.
    private static final Object USED = new Object();


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Mark the UTF-8 entries referenced by the other constant pool entries.
        programClass.constantPoolEntriesAccept(this);

        // Mark the UTF-8 entries referenced by the fields and methods.
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);

        // Mark the UTF-8 entries referenced by the attributes.
        programClass.attributesAccept(this);
    }


    // Implementations for MemberVisitor.

    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        // Mark the name and descriptor UTF-8 entries.
        markCpUtf8Entry(programClass, programMember.u2nameIndex);
        markCpUtf8Entry(programClass, programMember.u2descriptorIndex);

        // Mark the UTF-8 entries referenced by the attributes.
        programMember.attributesAccept(programClass, this);
    }


    // Implementations for ConstantVisitor.

    public void visitAnyConstant(Clazz clazz, Constant constant) {}


    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        markCpUtf8Entry(clazz, stringConstant.u2stringIndex);
    }


    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        markCpUtf8Entry(clazz, classConstant.u2nameIndex);
    }


    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        markCpUtf8Entry(clazz, nameAndTypeConstant.u2nameIndex);
        markCpUtf8Entry(clazz, nameAndTypeConstant.u2descriptorIndex);
    }


    // Implementations for AttributeVisitor.

    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        // This is the best we can do for unknown attributes.
        markCpUtf8Entry(clazz, unknownAttribute.u2attributeNameIndex);
    }


    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        markCpUtf8Entry(clazz, sourceFileAttribute.u2attributeNameIndex);

        markCpUtf8Entry(clazz, sourceFileAttribute.u2sourceFileIndex);
    }


    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        markCpUtf8Entry(clazz, sourceDirAttribute.u2attributeNameIndex);

        markCpUtf8Entry(clazz, sourceDirAttribute.u2sourceDirIndex);
    }


    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        markCpUtf8Entry(clazz, innerClassesAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the inner classes.
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }


    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        markCpUtf8Entry(clazz, enclosingMethodAttribute.u2attributeNameIndex);

        // These entries have already been marked in the constant pool.
        //clazz.constantPoolEntryAccept(this, enclosingMethodAttribute.u2classIndex);
        //clazz.constantPoolEntryAccept(this, enclosingMethodAttribute.u2nameAndTypeIndex);
    }


    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        markCpUtf8Entry(clazz, deprecatedAttribute.u2attributeNameIndex);
    }


    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        markCpUtf8Entry(clazz, syntheticAttribute.u2attributeNameIndex);
    }


    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        markCpUtf8Entry(clazz, signatureAttribute.u2attributeNameIndex);

        markCpUtf8Entry(clazz, signatureAttribute.u2signatureIndex);
    }


    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        markCpUtf8Entry(clazz, constantValueAttribute.u2attributeNameIndex);
    }


    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        markCpUtf8Entry(clazz, exceptionsAttribute.u2attributeNameIndex);
    }


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        markCpUtf8Entry(clazz, codeAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the attributes.
        codeAttribute.attributesAccept(clazz, method, this);
    }


    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        markCpUtf8Entry(clazz, stackMapAttribute.u2attributeNameIndex);
    }


    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        markCpUtf8Entry(clazz, stackMapTableAttribute.u2attributeNameIndex);
    }


    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        markCpUtf8Entry(clazz, lineNumberTableAttribute.u2attributeNameIndex);
    }


    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        markCpUtf8Entry(clazz, localVariableTableAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the local variables.
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        markCpUtf8Entry(clazz, localVariableTypeTableAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the local variable types.
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        markCpUtf8Entry(clazz, annotationsAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        annotationsAttribute.annotationsAccept(clazz, this);
    }


    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        markCpUtf8Entry(clazz, parameterAnnotationsAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }


    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        markCpUtf8Entry(clazz, annotationDefaultAttribute.u2attributeNameIndex);

        // Mark the UTF-8 entries referenced by the element value.
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            markCpUtf8Entry(clazz, innerClassesInfo.u2innerNameIndex);
        }
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        markCpUtf8Entry(clazz, localVariableInfo.u2nameIndex);
        markCpUtf8Entry(clazz, localVariableInfo.u2descriptorIndex);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        markCpUtf8Entry(clazz, localVariableTypeInfo.u2nameIndex);
        markCpUtf8Entry(clazz, localVariableTypeInfo.u2signatureIndex);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        markCpUtf8Entry(clazz, annotation.u2typeIndex);

        // Mark the UTF-8 entries referenced by the element values.
        annotation.elementValuesAccept(clazz, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        if (constantElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, constantElementValue.u2elementNameIndex);
        }

        // Only the string constant element value refers to a UTF-8 entry.
        if (constantElementValue.u1tag == ClassConstants.ELEMENT_VALUE_STRING_CONSTANT)
        {
            markCpUtf8Entry(clazz, constantElementValue.u2constantValueIndex);
        }
    }


    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        if (enumConstantElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, enumConstantElementValue.u2elementNameIndex);
        }

        markCpUtf8Entry(clazz, enumConstantElementValue.u2typeNameIndex);
        markCpUtf8Entry(clazz, enumConstantElementValue.u2constantNameIndex);
    }


    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        if (classElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, classElementValue.u2elementNameIndex);
        }

        markCpUtf8Entry(clazz, classElementValue.u2classInfoIndex);
    }


    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        if (annotationElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, annotationElementValue.u2elementNameIndex);
        }

        // Mark the UTF-8 entries referenced by the annotation.
        annotationElementValue.annotationAccept(clazz, this);
    }


    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        if (arrayElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, arrayElementValue.u2elementNameIndex);
        }

        // Mark the UTF-8 entries referenced by the element values.
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }


    // Small utility methods.

    /**
     * Marks the given UTF-8 constant pool entry of the given class.
     */
    private void markCpUtf8Entry(Clazz clazz, int index)
    {
         markAsUsed((Utf8Constant)((ProgramClass)clazz).getConstant(index));
    }


    /**
     * Marks the given VisitorAccepter as being used.
     * In this context, the VisitorAccepter will be a Utf8Constant object.
     */
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }


    /**
     * Returns whether the given VisitorAccepter has been marked as being used.
     * In this context, the VisitorAccepter will be a Utf8Constant object.
     */
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
