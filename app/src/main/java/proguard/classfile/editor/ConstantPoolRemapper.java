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

import proguard.classfile.Clazz;
import proguard.classfile.Field;
import proguard.classfile.LibraryClass;
import proguard.classfile.LibraryField;
import proguard.classfile.LibraryMethod;
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMember;
import proguard.classfile.ProgramMethod;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ConstantValueAttribute;
import proguard.classfile.attribute.DeprecatedAttribute;
import proguard.classfile.attribute.EnclosingMethodAttribute;
import proguard.classfile.attribute.ExceptionInfo;
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
import proguard.classfile.attribute.preverification.FullFrame;
import proguard.classfile.attribute.preverification.MoreZeroFrame;
import proguard.classfile.attribute.preverification.ObjectType;
import proguard.classfile.attribute.preverification.SameOneFrame;
import proguard.classfile.attribute.preverification.StackMapAttribute;
import proguard.classfile.attribute.preverification.StackMapFrame;
import proguard.classfile.attribute.preverification.StackMapTableAttribute;
import proguard.classfile.attribute.preverification.VerificationType;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
import proguard.classfile.attribute.visitor.InnerClassesInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableTypeInfoVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.DoubleConstant;
import proguard.classfile.constant.FieldrefConstant;
import proguard.classfile.constant.FloatConstant;
import proguard.classfile.constant.IntegerConstant;
import proguard.classfile.constant.InterfaceMethodrefConstant;
import proguard.classfile.constant.LongConstant;
import proguard.classfile.constant.MethodrefConstant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.Utf8Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;

/**
 * This ClassVisitor remaps all possible references to constant pool entries
 * of the classes that it visits, based on a given index map. It is assumed that
 * the constant pool entries themselves have already been remapped.
 *
 * @author Eric Lafortune
 */
public class ConstantPoolRemapper
extends SimplifiedVisitor
implements   ClassVisitor,
        ConstantVisitor,
             MemberVisitor,
        AttributeVisitor,
        InstructionVisitor,
             InnerClassesInfoVisitor,
             ExceptionInfoVisitor,
             StackMapFrameVisitor,
             VerificationTypeVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor(false);

    private int[] constantIndexMap;


    /**
     * Sets the given mapping of old constant pool entry indexes to their new
     * indexes.
     */
    public void setConstantIndexMap(int[] constantIndexMap)
    {
        this.constantIndexMap = constantIndexMap;
    }


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Remap the local constant pool references.
        programClass.u2thisClass  = remapConstantIndex(programClass.u2thisClass);
        programClass.u2superClass = remapConstantIndex(programClass.u2superClass);

        remapConstantIndexArray(programClass.u2interfaces,
                                programClass.u2interfacesCount);

        // Remap the references of the contant pool entries themselves.
        programClass.constantPoolEntriesAccept(this);

        // Remap the references in all fields, methods, and attributes.
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }


    // Implementations for ConstantVisitor.

    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.u2nameIndex =
            remapConstantIndex(classConstant.u2nameIndex);
    }


    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        // Nothing to do.
    }


    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        fieldrefConstant.u2classIndex =
            remapConstantIndex(fieldrefConstant.u2classIndex);
        fieldrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(fieldrefConstant.u2nameAndTypeIndex);
    }


    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        // Nothing to do.
    }


    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        // Nothing to do.
    }


    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        interfaceMethodrefConstant.u2classIndex =
            remapConstantIndex(interfaceMethodrefConstant.u2classIndex);
        interfaceMethodrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(interfaceMethodrefConstant.u2nameAndTypeIndex);
    }


    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        // Nothing to do.
    }


    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        methodrefConstant.u2classIndex =
            remapConstantIndex(methodrefConstant.u2classIndex);
        methodrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(methodrefConstant.u2nameAndTypeIndex);
    }


    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        nameAndTypeConstant.u2nameIndex =
            remapConstantIndex(nameAndTypeConstant.u2nameIndex);
        nameAndTypeConstant.u2descriptorIndex =
            remapConstantIndex(nameAndTypeConstant.u2descriptorIndex);
    }


    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.u2stringIndex =
            remapConstantIndex(stringConstant.u2stringIndex);
    }


    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        // Nothing to do.
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        visitMember(programClass, programField);
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        visitMember(programClass, programMethod);
    }


    private void visitMember(ProgramClass programClass, ProgramMember programMember)
    {
        // Remap the local constant pool references.
        programMember.u2nameIndex =
            remapConstantIndex(programMember.u2nameIndex);
        programMember.u2descriptorIndex =
            remapConstantIndex(programMember.u2descriptorIndex);

        // Remap the constant pool references of the remaining attributes.
        programMember.attributesAccept(programClass, this);
    }


    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        // Library classes are left unchanged.
    }


    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        // Library classes are left unchanged.
    }


    // Implementations for AttributeVisitor.

    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        unknownAttribute.u2attributeNameIndex =
            remapConstantIndex(unknownAttribute.u2attributeNameIndex);

        // There's not much else we can do with unknown attributes.
    }


    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        sourceFileAttribute.u2attributeNameIndex =
            remapConstantIndex(sourceFileAttribute.u2attributeNameIndex);
        sourceFileAttribute.u2sourceFileIndex =
            remapConstantIndex(sourceFileAttribute.u2sourceFileIndex);
    }


    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        sourceDirAttribute.u2attributeNameIndex =
            remapConstantIndex(sourceDirAttribute.u2attributeNameIndex);
        sourceDirAttribute.u2sourceDirIndex       =
            remapConstantIndex(sourceDirAttribute.u2sourceDirIndex);
    }


    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.u2attributeNameIndex =
            remapConstantIndex(innerClassesAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the inner classes.
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }


    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        enclosingMethodAttribute.u2attributeNameIndex =
            remapConstantIndex(enclosingMethodAttribute.u2attributeNameIndex);
        enclosingMethodAttribute.u2classIndex =
            remapConstantIndex(enclosingMethodAttribute.u2classIndex);
        enclosingMethodAttribute.u2nameAndTypeIndex =
            remapConstantIndex(enclosingMethodAttribute.u2nameAndTypeIndex);
    }


    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        deprecatedAttribute.u2attributeNameIndex =
            remapConstantIndex(deprecatedAttribute.u2attributeNameIndex);
    }


    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        syntheticAttribute.u2attributeNameIndex =
            remapConstantIndex(syntheticAttribute.u2attributeNameIndex);
    }


    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        signatureAttribute.u2attributeNameIndex =
            remapConstantIndex(signatureAttribute.u2attributeNameIndex);
        signatureAttribute.u2signatureIndex       =
            remapConstantIndex(signatureAttribute.u2signatureIndex);
    }


    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        constantValueAttribute.u2attributeNameIndex =
            remapConstantIndex(constantValueAttribute.u2attributeNameIndex);
        constantValueAttribute.u2constantValueIndex =
            remapConstantIndex(constantValueAttribute.u2constantValueIndex);
    }


    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        exceptionsAttribute.u2attributeNameIndex =
            remapConstantIndex(exceptionsAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the exceptions.
        remapConstantIndexArray(exceptionsAttribute.u2exceptionIndexTable,
                                exceptionsAttribute.u2exceptionIndexTableLength);
    }


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.u2attributeNameIndex =
            remapConstantIndex(codeAttribute.u2attributeNameIndex);

        // Initially, the code attribute editor doesn't contain any changes.
        codeAttributeEditor.reset(codeAttribute.u4codeLength);

        // Remap the constant pool references of the instructions.
        codeAttribute.instructionsAccept(clazz, method, this);

        // Apply the code atribute editor. It will only contain any changes if
        // the code length is changing at any point.
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);

        // Remap the constant pool references of the exceptions and attributes.
        codeAttribute.exceptionsAccept(clazz, method, this);
        codeAttribute.attributesAccept(clazz, method, this);
    }


    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        stackMapAttribute.u2attributeNameIndex =
            remapConstantIndex(stackMapAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the stack map frames.
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        stackMapTableAttribute.u2attributeNameIndex =
            remapConstantIndex(stackMapTableAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the stack map frames.
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        lineNumberTableAttribute.u2attributeNameIndex =
            remapConstantIndex(lineNumberTableAttribute.u2attributeNameIndex);
    }


    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.u2attributeNameIndex =
            remapConstantIndex(localVariableTableAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the local variables.
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.u2attributeNameIndex =
            remapConstantIndex(localVariableTypeTableAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the local variables.
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute.u2attributeNameIndex =
            remapConstantIndex(annotationsAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the annotations.
        annotationsAttribute.annotationsAccept(clazz, this);
    }


    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        parameterAnnotationsAttribute.u2attributeNameIndex =
            remapConstantIndex(parameterAnnotationsAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the annotations.
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }


    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        annotationDefaultAttribute.u2attributeNameIndex =
            remapConstantIndex(annotationDefaultAttribute.u2attributeNameIndex);

        // Remap the constant pool references of the annotations.
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerClassIndex != 0)
        {
            innerClassesInfo.u2innerClassIndex =
                remapConstantIndex(innerClassesInfo.u2innerClassIndex);
        }

        if (innerClassesInfo.u2outerClassIndex != 0)
        {
            innerClassesInfo.u2outerClassIndex =
                remapConstantIndex(innerClassesInfo.u2outerClassIndex);
        }

        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            innerClassesInfo.u2innerNameIndex =
                remapConstantIndex(innerClassesInfo.u2innerNameIndex);
        }
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        if (exceptionInfo.u2catchType != 0)
        {
            exceptionInfo.u2catchType =
                remapConstantIndex(exceptionInfo.u2catchType);
        }
    }


    // Implementations for InstructionVisitor.

    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        // Is the new constant pool index different from the original one?
        int newConstantIndex = remapConstantIndex(constantInstruction.constantIndex);
        if (newConstantIndex != constantInstruction.constantIndex)
        {
            // Replace the instruction.
            Instruction replacementInstruction =
                new ConstantInstruction(constantInstruction.opcode,
                                        newConstantIndex,
                                        constantInstruction.constant).shrink();

            codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        }
    }


    // Implementations for StackMapFrameVisitor.

    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame) {}


    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        // Remap the constant pool references of the verification types.
        sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        // Remap the constant pool references of the verification types.
        moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        // Remap the constant pool references of the verification types.
        fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
        fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
    }


    // Implementations for VerificationTypeVisitor.

    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType) {}


    public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
    {
        objectType.u2classIndex =
            remapConstantIndex(objectType.u2classIndex);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.u2nameIndex =
            remapConstantIndex(localVariableInfo.u2nameIndex);
        localVariableInfo.u2descriptorIndex =
            remapConstantIndex(localVariableInfo.u2descriptorIndex);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2nameIndex =
            remapConstantIndex(localVariableTypeInfo.u2nameIndex);
        localVariableTypeInfo.u2signatureIndex       =
            remapConstantIndex(localVariableTypeInfo.u2signatureIndex);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.u2typeIndex =
            remapConstantIndex(annotation.u2typeIndex);

        // Remap the constant pool references of the element values.
        annotation.elementValuesAccept(clazz, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        constantElementValue.u2elementNameIndex =
            remapConstantIndex(constantElementValue.u2elementNameIndex);
        constantElementValue.u2constantValueIndex =
            remapConstantIndex(constantElementValue.u2constantValueIndex);
    }


    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        enumConstantElementValue.u2elementNameIndex =
            remapConstantIndex(enumConstantElementValue.u2elementNameIndex);
        enumConstantElementValue.u2typeNameIndex =
            remapConstantIndex(enumConstantElementValue.u2typeNameIndex);
        enumConstantElementValue.u2constantNameIndex =
            remapConstantIndex(enumConstantElementValue.u2constantNameIndex);
    }


    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        classElementValue.u2elementNameIndex =
            remapConstantIndex(classElementValue.u2elementNameIndex);
        classElementValue.u2classInfoIndex       =
            remapConstantIndex(classElementValue.u2classInfoIndex);
    }


    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        annotationElementValue.u2elementNameIndex =
            remapConstantIndex(annotationElementValue.u2elementNameIndex);

        // Remap the constant pool references of the annotation.
        annotationElementValue.annotationAccept(clazz, this);
    }


    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.u2elementNameIndex =
            remapConstantIndex(arrayElementValue.u2elementNameIndex);

        // Remap the constant pool references of the element values.
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }


    // Small utility methods.

    /**
     * Remaps all constant pool indices in the given array.
     */
    private void remapConstantIndexArray(int[] array, int length)
    {
        for (int index = 0; index < length; index++)
        {
            array[index] = remapConstantIndex(array[index]);
        }
    }


    /**
     * Returns the new constant pool index of the entry at the
     * given index.
     */
    private int remapConstantIndex(int constantIndex)
    {
        return constantIndexMap[constantIndex];
    }
}
