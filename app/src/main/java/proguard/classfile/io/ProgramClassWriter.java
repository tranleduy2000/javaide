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
package proguard.classfile.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

import proguard.classfile.Clazz;
import proguard.classfile.Field;
import proguard.classfile.LibraryClass;
import proguard.classfile.LibraryMember;
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMethod;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ConstantValueAttribute;
import proguard.classfile.attribute.DeprecatedAttribute;
import proguard.classfile.attribute.EnclosingMethodAttribute;
import proguard.classfile.attribute.ExceptionInfo;
import proguard.classfile.attribute.ExceptionsAttribute;
import proguard.classfile.attribute.InnerClassesAttribute;
import proguard.classfile.attribute.InnerClassesInfo;
import proguard.classfile.attribute.LineNumberInfo;
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
import proguard.classfile.attribute.annotation.ElementValue;
import proguard.classfile.attribute.annotation.EnumConstantElementValue;
import proguard.classfile.attribute.annotation.ParameterAnnotationsAttribute;
import proguard.classfile.attribute.annotation.visitor.AnnotationVisitor;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.attribute.preverification.FullFrame;
import proguard.classfile.attribute.preverification.LessZeroFrame;
import proguard.classfile.attribute.preverification.MoreZeroFrame;
import proguard.classfile.attribute.preverification.ObjectType;
import proguard.classfile.attribute.preverification.SameOneFrame;
import proguard.classfile.attribute.preverification.SameZeroFrame;
import proguard.classfile.attribute.preverification.StackMapAttribute;
import proguard.classfile.attribute.preverification.StackMapFrame;
import proguard.classfile.attribute.preverification.StackMapTableAttribute;
import proguard.classfile.attribute.preverification.UninitializedType;
import proguard.classfile.attribute.preverification.VerificationType;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
import proguard.classfile.attribute.visitor.InnerClassesInfoVisitor;
import proguard.classfile.attribute.visitor.LineNumberInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableTypeInfoVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.DoubleConstant;
import proguard.classfile.constant.FloatConstant;
import proguard.classfile.constant.IntegerConstant;
import proguard.classfile.constant.LongConstant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.Utf8Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;

/**
 * This ClassVisitor writes out the ProgramClass objects that it visits to the
 * given DataOutput object.
 *
 * @author Eric Lafortune
 */
public class ProgramClassWriter
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor,
             AttributeVisitor
{
    private RuntimeDataOutput dataOutput;

    private final ConstantBodyWriter         constantBodyWriter         = new ConstantBodyWriter();
    private final AttributeBodyWriter        attributeBodyWriter        = new AttributeBodyWriter();
    private final StackMapFrameBodyWriter    stackMapFrameBodyWriter    = new StackMapFrameBodyWriter();
    private final VerificationTypeBodyWriter verificationTypeBodyWriter = new VerificationTypeBodyWriter();
    private final ElementValueBodyWriter     elementValueBodyWriter     = new ElementValueBodyWriter();


    /**
     * Creates a new ProgramClassWriter for reading from the given DataOutput.
     */
    public ProgramClassWriter(DataOutput dataOutput)
    {
        this.dataOutput = new RuntimeDataOutput(dataOutput);
    }


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Write the magic number.
        dataOutput.writeInt(programClass.u4magic);

        // Write the version numbers.
        dataOutput.writeShort(ClassUtil.internalMinorClassVersion(programClass.u4version));
        dataOutput.writeShort(ClassUtil.internalMajorClassVersion(programClass.u4version));

        // Write the constant pool.
        dataOutput.writeShort(programClass.u2constantPoolCount);

        programClass.constantPoolEntriesAccept(this);

        // Write the general class information.
        dataOutput.writeShort(programClass.u2accessFlags);
        dataOutput.writeShort(programClass.u2thisClass);
        dataOutput.writeShort(programClass.u2superClass);

        // Write the interfaces.
        dataOutput.writeShort(programClass.u2interfacesCount);

        for (int index = 0; index < programClass.u2interfacesCount; index++)
        {
            dataOutput.writeShort(programClass.u2interfaces[index]);
        }

        // Write the fields.
        dataOutput.writeShort(programClass.u2fieldsCount);

        programClass.fieldsAccept(this);

        // Write the methods.
        dataOutput.writeShort(programClass.u2methodsCount);

        programClass.methodsAccept(this);

        // Write the class attributes.
        dataOutput.writeShort(programClass.u2attributesCount);

        programClass.attributesAccept(this);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        // Write the general field information.
        dataOutput.writeShort(programField.u2accessFlags);
        dataOutput.writeShort(programField.u2nameIndex);
        dataOutput.writeShort(programField.u2descriptorIndex);

        // Write the field attributes.
        dataOutput.writeShort(programField.u2attributesCount);

        programField.attributesAccept(programClass, this);
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        // Write the general method information.
        dataOutput.writeShort(programMethod.u2accessFlags);
        dataOutput.writeShort(programMethod.u2nameIndex);
        dataOutput.writeShort(programMethod.u2descriptorIndex);

        // Write the method attributes.
        dataOutput.writeShort(programMethod.u2attributesCount);

        programMethod.attributesAccept(programClass, this);
    }


    public void visitLibraryMember(LibraryClass libraryClass, LibraryMember libraryMember)
    {
    }


    // Implementations for ConstantVisitor.

    public void visitAnyConstant(Clazz clazz, Constant constant)
    {
        // Write the tag.
        dataOutput.writeByte(constant.getTag());

        // Write the actual body.
        constant.accept(clazz, constantBodyWriter);
    }


    private class ConstantBodyWriter
    extends       SimplifiedVisitor
    implements    ConstantVisitor
    {
        // Implementations for ConstantVisitor.

        public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
        {
            dataOutput.writeInt(integerConstant.u4value);
        }


        public void visitLongConstant(Clazz clazz, LongConstant longConstant)
        {
            dataOutput.writeLong(longConstant.u8value);
        }


        public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
        {
            dataOutput.writeFloat(floatConstant.f4value);
        }


        public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
        {
            dataOutput.writeDouble(doubleConstant.f8value);
        }


        public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
        {
            dataOutput.writeShort(stringConstant.u2stringIndex);
        }


        public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
        {
            byte[] bytes = utf8Constant.getBytes();

            dataOutput.writeShort(bytes.length);
            dataOutput.write(bytes);
        }


        public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
        {
            dataOutput.writeShort(refConstant.u2classIndex);
            dataOutput.writeShort(refConstant.u2nameAndTypeIndex);
        }


        public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
        {
            dataOutput.writeShort(classConstant.u2nameIndex);
        }


        public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
        {
            dataOutput.writeShort(nameAndTypeConstant.u2nameIndex);
            dataOutput.writeShort(nameAndTypeConstant.u2descriptorIndex);
        }
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute)
    {
        // Write the attribute name index.
        dataOutput.writeShort(attribute.u2attributeNameIndex);

        // We'll write the attribute body into an array first, so we can
        // automatically figure out its length.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Temporarily replace the current data output.
        RuntimeDataOutput oldDataOutput = dataOutput;
        dataOutput = new RuntimeDataOutput(new DataOutputStream(byteArrayOutputStream));

        // Write the attribute body into the array. Note that the
        // accept method with two dummy null arguments never throws
        // an UnsupportedOperationException.
        attribute.accept(clazz, null, null, attributeBodyWriter);

        // Restore the original data output.
        dataOutput = oldDataOutput;

        // Write the attribute length and body.
        byte[] info = byteArrayOutputStream.toByteArray();

        dataOutput.writeInt(info.length);
        dataOutput.write(info);
    }


    private class AttributeBodyWriter
    extends       SimplifiedVisitor
    implements    AttributeVisitor,
                  InnerClassesInfoVisitor,
                  ExceptionInfoVisitor,
                  StackMapFrameVisitor,
                  VerificationTypeVisitor,
                  LineNumberInfoVisitor,
                  LocalVariableInfoVisitor,
                  LocalVariableTypeInfoVisitor,
                  AnnotationVisitor,
                  ElementValueVisitor
    {
        // Implementations for AttributeVisitor.

        public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
        {
            // Write the unknown information.
            dataOutput.write(unknownAttribute.info);
        }


        public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
        {
            dataOutput.writeShort(sourceFileAttribute.u2sourceFileIndex);
        }


        public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
        {
            dataOutput.writeShort(sourceDirAttribute.u2sourceDirIndex);
        }


        public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
        {
            // Write the inner classes.
            dataOutput.writeShort(innerClassesAttribute.u2classesCount);

            innerClassesAttribute.innerClassEntriesAccept(clazz, this);
        }


        public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
        {
            dataOutput.writeShort(enclosingMethodAttribute.u2classIndex);
            dataOutput.writeShort(enclosingMethodAttribute.u2nameAndTypeIndex);
        }


        public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
        {
            // This attribute does not contain any additional information.
        }


        public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
        {
            // This attribute does not contain any additional information.
        }


        public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
        {
            dataOutput.writeShort(signatureAttribute.u2signatureIndex);
        }


        public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
        {
            dataOutput.writeShort(constantValueAttribute.u2constantValueIndex);
        }


        public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
        {
            // Write the exceptions.
            dataOutput.writeShort(exceptionsAttribute.u2exceptionIndexTableLength);

            for (int index = 0; index < exceptionsAttribute.u2exceptionIndexTableLength; index++)
            {
                dataOutput.writeShort(exceptionsAttribute.u2exceptionIndexTable[index]);
            }
        }


        public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
        {
            // Write the stack size and local variable frame size.
            dataOutput.writeShort(codeAttribute.u2maxStack);
            dataOutput.writeShort(codeAttribute.u2maxLocals);

            // Write the byte code.
            dataOutput.writeInt(codeAttribute.u4codeLength);

            dataOutput.write(codeAttribute.code, 0, codeAttribute.u4codeLength);

            // Write the exceptions.
            dataOutput.writeShort(codeAttribute.u2exceptionTableLength);

            codeAttribute.exceptionsAccept(clazz, method, this);

            // Write the code attributes.
            dataOutput.writeShort(codeAttribute.u2attributesCount);

            codeAttribute.attributesAccept(clazz, method, ProgramClassWriter.this);
        }


        public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
        {
            // Write the stack map frames (only full frames, without tag).
            dataOutput.writeShort(stackMapAttribute.u2stackMapFramesCount);

            stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, stackMapFrameBodyWriter);
        }


        public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
        {
            // Write the stack map frames.
            dataOutput.writeShort(stackMapTableAttribute.u2stackMapFramesCount);

            stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
        }


        public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
        {
            // Write the line numbers.
            dataOutput.writeShort(lineNumberTableAttribute.u2lineNumberTableLength);

            lineNumberTableAttribute.lineNumbersAccept(clazz, method, codeAttribute, this);
        }


        public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
        {
            // Write the local variables.
            dataOutput.writeShort(localVariableTableAttribute.u2localVariableTableLength);

            localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        }


        public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
        {
            // Write the local variable types.
            dataOutput.writeShort(localVariableTypeTableAttribute.u2localVariableTypeTableLength);

            localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        }


        public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
        {
            // Write the annotations.
            dataOutput.writeShort(annotationsAttribute.u2annotationsCount);

            annotationsAttribute.annotationsAccept(clazz, this);
        }


        public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
        {
            // Write the parameter annotations.
            dataOutput.writeByte(parameterAnnotationsAttribute.u2parametersCount);

            for (int parameterIndex = 0; parameterIndex < parameterAnnotationsAttribute.u2parametersCount; parameterIndex++)
            {
                // Write the parameter annotations of the given parameter.
                int          u2annotationsCount = parameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex];
                Annotation[] annotations        = parameterAnnotationsAttribute.parameterAnnotations[parameterIndex];

                dataOutput.writeShort(u2annotationsCount);

                for (int index = 0; index < u2annotationsCount; index++)
                {
                    Annotation annotation = annotations[index];
                    this.visitAnnotation(clazz, annotation);
                }

            }
        }


        public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
        {
            // Write the default element value.
            annotationDefaultAttribute.defaultValue.accept(clazz, null, this);
        }


        // Implementations for InnerClassesInfoVisitor.

        public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
        {
            dataOutput.writeShort(innerClassesInfo.u2innerClassIndex);
            dataOutput.writeShort(innerClassesInfo.u2outerClassIndex);
            dataOutput.writeShort(innerClassesInfo.u2innerNameIndex);
            dataOutput.writeShort(innerClassesInfo.u2innerClassAccessFlags);
        }


        // Implementations for ExceptionInfoVisitor.

        public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
        {
            dataOutput.writeShort(exceptionInfo.u2startPC);
            dataOutput.writeShort(exceptionInfo.u2endPC);
            dataOutput.writeShort(exceptionInfo.u2handlerPC);
            dataOutput.writeShort(exceptionInfo.u2catchType);
        }


        // Implementations for StackMapFrameVisitor.

        public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame)
        {
            // Write the stack map frame tag.
            dataOutput.writeByte(stackMapFrame.getTag());

            // Write the actual body.
            stackMapFrame.accept(clazz, method, codeAttribute, offset, stackMapFrameBodyWriter);
        }


        // Implementations for LineNumberInfoVisitor.

        public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo)
        {
            dataOutput.writeShort(lineNumberInfo.u2startPC);
            dataOutput.writeShort(lineNumberInfo.u2lineNumber);
        }


        // Implementations for LocalVariableInfoVisitor.

        public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
        {
            dataOutput.writeShort(localVariableInfo.u2startPC);
            dataOutput.writeShort(localVariableInfo.u2length);
            dataOutput.writeShort(localVariableInfo.u2nameIndex);
            dataOutput.writeShort(localVariableInfo.u2descriptorIndex);
            dataOutput.writeShort(localVariableInfo.u2index);
        }


        // Implementations for LocalVariableTypeInfoVisitor.

        public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
        {
            dataOutput.writeShort(localVariableTypeInfo.u2startPC);
            dataOutput.writeShort(localVariableTypeInfo.u2length);
            dataOutput.writeShort(localVariableTypeInfo.u2nameIndex);
            dataOutput.writeShort(localVariableTypeInfo.u2signatureIndex);
            dataOutput.writeShort(localVariableTypeInfo.u2index);
        }


        // Implementations for AnnotationVisitor.

        public void visitAnnotation(Clazz clazz, Annotation annotation)
        {
            // Write the annotation type.
            dataOutput.writeShort(annotation.u2typeIndex);

            // Write the element value pairs.
            dataOutput.writeShort(annotation.u2elementValuesCount);

            annotation.elementValuesAccept(clazz, this);
        }


        // Implementations for ElementValueVisitor.

        public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
        {
            // Write the element name index, if applicable.
            int u2elementNameIndex = elementValue.u2elementNameIndex;
            if (u2elementNameIndex != 0)
            {
                dataOutput.writeShort(u2elementNameIndex);
            }

            // Write the tag.
            dataOutput.writeByte(elementValue.getTag());

            // Write the actual body.
            elementValue.accept(clazz, annotation, elementValueBodyWriter);
        }
    }


    private class StackMapFrameBodyWriter
    extends       SimplifiedVisitor
    implements    StackMapFrameVisitor,
                  VerificationTypeVisitor
    {
        public void visitSameZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameZeroFrame sameZeroFrame)
        {
            if (sameZeroFrame.getTag() == StackMapFrame.SAME_ZERO_FRAME_EXTENDED)
            {
                dataOutput.writeShort(sameZeroFrame.u2offsetDelta);
            }
        }


        public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
        {
            if (sameOneFrame.getTag() == StackMapFrame.SAME_ONE_FRAME_EXTENDED)
            {
                dataOutput.writeShort(sameOneFrame.u2offsetDelta);
            }

            // Write the verification type of the stack entry.
            sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
        }


        public void visitLessZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LessZeroFrame lessZeroFrame)
        {
            dataOutput.writeShort(lessZeroFrame.u2offsetDelta);
        }


        public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
        {
            dataOutput.writeShort(moreZeroFrame.u2offsetDelta);

            // Write the verification types of the additional local variables.
            moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
        }


        public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
        {
            dataOutput.writeShort(fullFrame.u2offsetDelta);

            // Write the verification types of the local variables.
            dataOutput.writeShort(fullFrame.variablesCount);
            fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);

            // Write the verification types of the stack entries.
            dataOutput.writeShort(fullFrame.stackCount);
            fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
        }


        // Implementations for VerificationTypeVisitor.

        public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType)
        {
            // Write the verification type tag.
            dataOutput.writeByte(verificationType.getTag());

            // Write the actual body.
            verificationType.accept(clazz, method, codeAttribute, offset, verificationTypeBodyWriter);
        }
    }


    private class VerificationTypeBodyWriter
    extends       SimplifiedVisitor
    implements    VerificationTypeVisitor
    {
        // Implementations for VerificationTypeVisitor.

        public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType)
        {
            // Most verification types don't contain any additional information.
        }


        public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
        {
            dataOutput.writeShort(objectType.u2classIndex);
        }


        public void visitUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType uninitializedType)
        {
            dataOutput.writeShort(uninitializedType.u2newInstructionOffset);
        }
    }


    private class ElementValueBodyWriter
    extends       SimplifiedVisitor
    implements    ElementValueVisitor
    {
        // Implementations for ElementValueVisitor.

        public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
        {
            dataOutput.writeShort(constantElementValue.u2constantValueIndex);
        }


        public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
        {
            dataOutput.writeShort(enumConstantElementValue.u2typeNameIndex);
            dataOutput.writeShort(enumConstantElementValue.u2constantNameIndex);
        }


        public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
        {
            dataOutput.writeShort(classElementValue.u2classInfoIndex);
        }


        public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
        {
            // Write the annotation.
            attributeBodyWriter.visitAnnotation(clazz, annotationElementValue.annotationValue);
        }


        public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
        {
            // Write the element values.
            dataOutput.writeShort(arrayElementValue.u2elementValuesCount);

            arrayElementValue.elementValuesAccept(clazz, annotation, attributeBodyWriter);
        }
    }
}
