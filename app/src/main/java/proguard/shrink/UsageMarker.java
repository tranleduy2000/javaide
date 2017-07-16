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
package proguard.shrink;

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
import proguard.classfile.VisitorAccepter;
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
import proguard.classfile.attribute.annotation.AnnotationDefaultAttribute;
import proguard.classfile.attribute.annotation.AnnotationsAttribute;
import proguard.classfile.attribute.annotation.ParameterAnnotationsAttribute;
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
import proguard.classfile.constant.FloatConstant;
import proguard.classfile.constant.IntegerConstant;
import proguard.classfile.constant.LongConstant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.Utf8Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassHierarchyTraveler;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.ConcreteClassDownTraveler;
import proguard.classfile.visitor.MemberAccessFilter;
import proguard.classfile.visitor.MemberVisitor;
import proguard.classfile.visitor.NamedMethodVisitor;


/**
 * This ClassVisitor and MemberVisitor recursively marks all classes and class
 * elements that are being used.
 *
 * @see ClassShrinker
 *
 * @author Eric Lafortune
 */
class      UsageMarker
extends SimplifiedVisitor
implements ClassVisitor,
           MemberVisitor,
        ConstantVisitor,
           AttributeVisitor,
           InnerClassesInfoVisitor,
           ExceptionInfoVisitor,
           StackMapFrameVisitor,
           VerificationTypeVisitor,
           LocalVariableInfoVisitor,
           LocalVariableTypeInfoVisitor,
//         AnnotationVisitor,
//         ElementValueVisitor,
        InstructionVisitor
{
    // A visitor info flag to indicate the ProgramMember object is being used,
    // if its Clazz can be determined as being used as well.
    private static final Object POSSIBLY_USED = new Object();
    // A visitor info flag to indicate the visitor accepter is being used.
    private static final Object USED          = new Object();


    private final MyInterfaceUsageMarker          interfaceUsageMarker          = new MyInterfaceUsageMarker();
    private final MyPossiblyUsedMemberUsageMarker possiblyUsedMemberUsageMarker = new MyPossiblyUsedMemberUsageMarker();
//    private ClassVisitor       dynamicClassMarker   =
//        new MultiClassVisitor(
//        new ClassVisitor[]
//        {
//            this,
//            new NamedMethodVisitor(ClassConstants.INTERNAL_METHOD_NAME_INIT,
//                                   ClassConstants.INTERNAL_METHOD_TYPE_INIT,
//                                   this)
//        });


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        if (shouldBeMarkedAsUsed(programClass))
        {
            // Mark this class.
            markAsUsed(programClass);

            markProgramClassBody(programClass);
        }
    }


    protected void markProgramClassBody(ProgramClass programClass)
    {
        // Mark this class's name.
        markConstant(programClass, programClass.u2thisClass);

        // Mark the superclass.
        if (programClass.u2superClass != 0)
        {
            markConstant(programClass, programClass.u2superClass);
        }

        // Give the interfaces preliminary marks.
        programClass.hierarchyAccept(false, false, true, false,
                                     interfaceUsageMarker);

        // Explicitly mark the <clinit> method.
        programClass.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                  ClassConstants.INTERNAL_METHOD_TYPE_CLINIT,
                                  this);

        // Explicitly mark the parameterless <init> method.
        programClass.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                  ClassConstants.INTERNAL_METHOD_TYPE_INIT,
                                  this);

        // Process all class members that have already been marked as possibly used.
        programClass.fieldsAccept(possiblyUsedMemberUsageMarker);
        programClass.methodsAccept(possiblyUsedMemberUsageMarker);

        // Mark the attributes.
        programClass.attributesAccept(this);
    }


    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (shouldBeMarkedAsUsed(libraryClass))
        {
            markAsUsed(libraryClass);

            // We're not going to analyze all library code. We're assuming that
            // if this class is being used, all of its methods will be used as
            // well. We'll mark them as such (here and in all subclasses).

            // Mark the superclass.
            Clazz superClass = libraryClass.superClass;
            if (superClass != null)
            {
                superClass.accept(this);
            }

            // Mark the interfaces.
            Clazz[] interfaceClasses = libraryClass.interfaceClasses;
            if (interfaceClasses != null)
            {
                for (int index = 0; index < interfaceClasses.length; index++)
                {
                    if (interfaceClasses[index] != null)
                    {
                        interfaceClasses[index].accept(this);
                    }
                }
            }

            // Mark all methods.
            libraryClass.methodsAccept(this);
        }
    }


    /**
     * This ClassVisitor marks ProgramClass objects as possibly used,
     * and it visits LibraryClass objects with its outer UsageMarker.
     */
    private class MyInterfaceUsageMarker
    implements    ClassVisitor
    {
        public void visitProgramClass(ProgramClass programClass)
        {
            if (shouldBeMarkedAsPossiblyUsed(programClass))
            {
                // We can't process the interface yet, because it might not
                // be required. Give it a preliminary mark.
                markAsPossiblyUsed(programClass);
            }
        }

        public void visitLibraryClass(LibraryClass libraryClass)
        {
            // Make sure all library interface methods are marked.
            UsageMarker.this.visitLibraryClass(libraryClass);
        }
    }


    private class MyPossiblyUsedMemberUsageMarker
    extends SimplifiedVisitor
    implements    MemberVisitor
    {
        // Implementations for MemberVisitor.

        public void visitProgramField(ProgramClass programClass, ProgramField programField)
        {
            // Has the method already been referenced?
            if (isPossiblyUsed(programField))
            {
                markAsUsed(programField);

                // Mark the name and descriptor.
                markConstant(programClass, programField.u2nameIndex);
                markConstant(programClass, programField.u2descriptorIndex);

                // Mark the attributes.
                programField.attributesAccept(programClass, UsageMarker.this);

                // Mark the classes referenced in the descriptor string.
                programField.referencedClassesAccept(UsageMarker.this);
            }
        }


        public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
        {
            // Has the method already been referenced?
            if (isPossiblyUsed(programMethod))
            {
                markAsUsed(programMethod);

                // Mark the method body.
                markProgramMethodBody(programClass, programMethod);

                // Note that, if the method has been marked as possibly used,
                // the method hierarchy has already been marked (cfr. below).
            }
        }
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (shouldBeMarkedAsUsed(programField))
        {
            // Is the field's class used?
            if (isUsed(programClass))
            {
                markAsUsed(programField);

                // Mark the field body.
                markProgramFieldBody(programClass, programField);
            }

            // Hasn't the field been marked as possibly being used yet?
            else if (shouldBeMarkedAsPossiblyUsed(programField))
            {
                // We can't process the field yet, because the class isn't
                // marked as being used (yet). Give it a preliminary mark.
                markAsPossiblyUsed(programField);
            }
        }
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (shouldBeMarkedAsUsed(programMethod))
        {
            // Is the method's class used?
            if (isUsed(programClass))
            {
                markAsUsed(programMethod);

                // Mark the method body.
                markProgramMethodBody(programClass, programMethod);

                // Mark the method hierarchy.
                markMethodHierarchy(programClass, programMethod);
            }

            // Hasn't the method been marked as possibly being used yet?
            else if (shouldBeMarkedAsPossiblyUsed(programMethod))
            {
                // We can't process the method yet, because the class isn't
                // marked as being used (yet). Give it a preliminary mark.
                markAsPossiblyUsed(programMethod);

                // Mark the method hierarchy.
                markMethodHierarchy(programClass, programMethod);
            }
        }
    }


    public void visitLibraryField(LibraryClass programClass, LibraryField programField) {}


    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (shouldBeMarkedAsUsed(libraryMethod))
        {
            markAsUsed(libraryMethod);

            // Mark the method hierarchy.
            markMethodHierarchy(libraryClass, libraryMethod);
        }
    }


    protected void markProgramFieldBody(ProgramClass programClass, ProgramField programField)
    {
        // Mark the name and descriptor.
        markConstant(programClass, programField.u2nameIndex);
        markConstant(programClass, programField.u2descriptorIndex);

        // Mark the attributes.
        programField.attributesAccept(programClass, this);

        // Mark the classes referenced in the descriptor string.
        programField.referencedClassesAccept(this);
    }


    protected void markProgramMethodBody(ProgramClass programClass, ProgramMethod programMethod)
    {
        // Mark the name and descriptor.
        markConstant(programClass, programMethod.u2nameIndex);
        markConstant(programClass, programMethod.u2descriptorIndex);

        // Mark the attributes.
        programMethod.attributesAccept(programClass, this);

        // Mark the classes referenced in the descriptor string.
        programMethod.referencedClassesAccept(this);
    }


    /**
     * Marks the hierarchy of implementing or overriding methods corresponding
     * to the given method, if any.
     */
    protected void markMethodHierarchy(Clazz clazz, Method method)
    {
        if ((method.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_PRIVATE |
              ClassConstants.INTERNAL_ACC_STATIC)) == 0)
        {
            clazz.accept(new ConcreteClassDownTraveler(
                         new ClassHierarchyTraveler(true, true, false, true,
                         new NamedMethodVisitor(method.getName(clazz),
                                                method.getDescriptor(clazz),
                         new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE | ClassConstants.INTERNAL_ACC_STATIC | ClassConstants.INTERNAL_ACC_ABSTRACT,
                         this)))));
        }
    }


    // Implementations for ConstantVisitor.

    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        if (shouldBeMarkedAsUsed(integerConstant))
        {
            markAsUsed(integerConstant);
        }
    }


    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        if (shouldBeMarkedAsUsed(longConstant))
        {
            markAsUsed(longConstant);
        }
    }


    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        if (shouldBeMarkedAsUsed(floatConstant))
        {
            markAsUsed(floatConstant);
        }
    }


    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        if (shouldBeMarkedAsUsed(doubleConstant))
        {
            markAsUsed(doubleConstant);
        }
    }


    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        if (shouldBeMarkedAsUsed(stringConstant))
        {
            markAsUsed(stringConstant);

            markConstant(clazz, stringConstant.u2stringIndex);

            // Mark the referenced class and its parameterless constructor,
            // if the string is being used in a Class.forName construct.
            //stringConstant.referencedClassAccept(dynamicClassMarker);

            // Mark the referenced class or class member, if any.
            stringConstant.referencedClassAccept(this);
            stringConstant.referencedMemberAccept(this);
        }
    }


    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        if (shouldBeMarkedAsUsed(utf8Constant))
        {
            markAsUsed(utf8Constant);
        }
    }


    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        if (shouldBeMarkedAsUsed(refConstant))
        {
            markAsUsed(refConstant);

            markConstant(clazz, refConstant.u2classIndex);
            markConstant(clazz, refConstant.u2nameAndTypeIndex);

            // When compiled with "-target 1.2" or higher, the class or
            // interface actually containing the referenced class member may
            // be higher up the hierarchy. Make sure it's marked, in case it
            // isn't used elsewhere.
            refConstant.referencedClassAccept(this);

            // Mark the referenced class member itself.
            refConstant.referencedMemberAccept(this);
        }
    }


    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        if (shouldBeMarkedAsUsed(classConstant))
        {
            markAsUsed(classConstant);

            markConstant(clazz, classConstant.u2nameIndex);

            // Mark the referenced class itself.
            classConstant.referencedClassAccept(this);
        }
    }


    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        if (shouldBeMarkedAsUsed(nameAndTypeConstant))
        {
            markAsUsed(nameAndTypeConstant);

            markConstant(clazz, nameAndTypeConstant.u2nameIndex);
            markConstant(clazz, nameAndTypeConstant.u2descriptorIndex);
        }
    }


    // Implementations for AttributeVisitor.
    // Note that attributes are typically only referenced once, so we don't
    // test if they have been marked already.

    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        // This is the best we can do for unknown attributes.
        markAsUsed(unknownAttribute);

        markConstant(clazz, unknownAttribute.u2attributeNameIndex);
    }


    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        markAsUsed(sourceFileAttribute);

        markConstant(clazz, sourceFileAttribute.u2attributeNameIndex);
        markConstant(clazz, sourceFileAttribute.u2sourceFileIndex);
    }


    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        markAsUsed(sourceDirAttribute);

        markConstant(clazz, sourceDirAttribute.u2attributeNameIndex);
        markConstant(clazz, sourceDirAttribute.u2sourceDirIndex);
    }


    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        // Don't mark the attribute and its name yet. We may mark it later, in
        // InnerUsageMarker.
        //markAsUsed(innerClassesAttribute);

        //markConstant(clazz, innerClassesAttribute.u2attrNameIndex);

        // Do mark the outer class entries.
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }


    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        markAsUsed(enclosingMethodAttribute);

        markConstant(clazz, enclosingMethodAttribute.u2attributeNameIndex);
        markConstant(clazz, enclosingMethodAttribute.u2classIndex);

        if (enclosingMethodAttribute.u2nameAndTypeIndex != 0)
        {
            markConstant(clazz, enclosingMethodAttribute.u2nameAndTypeIndex);
        }
    }


    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        markAsUsed(deprecatedAttribute);

        markConstant(clazz, deprecatedAttribute.u2attributeNameIndex);
    }


    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        markAsUsed(syntheticAttribute);

        markConstant(clazz, syntheticAttribute.u2attributeNameIndex);
    }


    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        markAsUsed(signatureAttribute);

        markConstant(clazz, signatureAttribute.u2attributeNameIndex);
        markConstant(clazz, signatureAttribute.u2signatureIndex);
    }


    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        markAsUsed(constantValueAttribute);

        markConstant(clazz, constantValueAttribute.u2attributeNameIndex);
        markConstant(clazz, constantValueAttribute.u2constantValueIndex);
    }


    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        markAsUsed(exceptionsAttribute);

        markConstant(clazz, exceptionsAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the exceptions.
        exceptionsAttribute.exceptionEntriesAccept((ProgramClass)clazz, this);
    }


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        markAsUsed(codeAttribute);

        markConstant(clazz, codeAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the instructions,
        // by the exceptions, and by the attributes.
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.exceptionsAccept(clazz, method, this);
        codeAttribute.attributesAccept(clazz, method, this);
    }


    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        markAsUsed(stackMapAttribute);

        markConstant(clazz, stackMapAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the stack map frames.
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        markAsUsed(stackMapTableAttribute);

        markConstant(clazz, stackMapTableAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the stack map frames.
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        markAsUsed(lineNumberTableAttribute);

        markConstant(clazz, lineNumberTableAttribute.u2attributeNameIndex);
    }


    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        markAsUsed(localVariableTableAttribute);

        markConstant(clazz, localVariableTableAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the local variables.
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        markAsUsed(localVariableTypeTableAttribute);

        markConstant(clazz, localVariableTypeTableAttribute.u2attributeNameIndex);

        // Mark the constant pool entries referenced by the local variable types.
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        // Don't mark the attribute and its contents yet. We may mark them later,
        // in AnnotationUsageMarker.
//        markAsUsed(annotationsAttribute);
//
//        markConstant(clazz, annotationsAttribute.u2attributeNameIndex);
//
//        // Mark the constant pool entries referenced by the annotations.
//        annotationsAttribute.annotationsAccept(clazz, this);
    }


    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        // Don't mark the attribute and its contents yet. We may mark them later,
        // in AnnotationUsageMarker.
//        markAsUsed(parameterAnnotationsAttribute);
//
//        markConstant(clazz, parameterAnnotationsAttribute.u2attributeNameIndex);
//
//        // Mark the constant pool entries referenced by the annotations.
//        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }


    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        // Don't mark the attribute and its contents yet. We may mark them later,
        // in AnnotationUsageMarker.
//        markAsUsed(annotationDefaultAttribute);
//
//        markConstant(clazz, annotationDefaultAttribute.u2attributeNameIndex);
//
//        // Mark the constant pool entries referenced by the element value.
//        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        markAsUsed(exceptionInfo);

        if (exceptionInfo.u2catchType != 0)
        {
            markConstant(clazz, exceptionInfo.u2catchType);
        }
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        // At this point, we only mark outer classes of this class.
        // Inner class can be marked later, by InnerUsageMarker.
        if (innerClassesInfo.u2innerClassIndex != 0 &&
            clazz.getName().equals(clazz.getClassName(innerClassesInfo.u2innerClassIndex)))
        {
            markAsUsed(innerClassesInfo);

            innerClassesInfo.innerClassConstantAccept(clazz, this);
            innerClassesInfo.outerClassConstantAccept(clazz, this);
            innerClassesInfo.innerNameConstantAccept(clazz, this);
        }
    }


    // Implementations for StackMapFrameVisitor.

    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame) {}


    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        // Mark the constant pool entries referenced by the verification types.
        sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        // Mark the constant pool entries referenced by the verification types.
        moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        // Mark the constant pool entries referenced by the verification types.
        fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
        fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
    }


    // Implementations for VerificationTypeVisitor.

    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType) {}


    public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
    {
        markConstant(clazz, objectType.u2classIndex);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        markConstant(clazz, localVariableInfo.u2nameIndex);
        markConstant(clazz, localVariableInfo.u2descriptorIndex);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        markConstant(clazz, localVariableTypeInfo.u2nameIndex);
        markConstant(clazz, localVariableTypeInfo.u2signatureIndex);
    }


//    // Implementations for AnnotationVisitor.
//
//    public void visitAnnotation(Clazz clazz, Annotation annotation)
//    {
//        markConstant(clazz, annotation.u2typeIndex);
//
//        // Mark the constant pool entries referenced by the element values.
//        annotation.elementValuesAccept(clazz, this);
//    }
//
//
//    // Implementations for ElementValueVisitor.
//
//    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
//    {
//        if (constantElementValue.u2elementNameIndex != 0)
//        {
//            markConstant(clazz, constantElementValue.u2elementNameIndex);
//        }
//
//        markConstant(clazz, constantElementValue.u2constantValueIndex);
//    }
//
//
//    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
//    {
//        if (enumConstantElementValue.u2elementNameIndex != 0)
//        {
//            markConstant(clazz, enumConstantElementValue.u2elementNameIndex);
//        }
//
//        markConstant(clazz, enumConstantElementValue.u2typeNameIndex);
//        markConstant(clazz, enumConstantElementValue.u2constantNameIndex);
//    }
//
//
//    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
//    {
//        if (classElementValue.u2elementNameIndex != 0)
//        {
//            markConstant(clazz, classElementValue.u2elementNameIndex);
//        }
//
//        // Mark the referenced class constant pool entry.
//        markConstant(clazz, classElementValue.u2classInfoIndex);
//    }
//
//
//    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
//    {
//        if (annotationElementValue.u2elementNameIndex != 0)
//        {
//            markConstant(clazz, annotationElementValue.u2elementNameIndex);
//        }
//
//        // Mark the constant pool entries referenced by the annotation.
//        annotationElementValue.annotationAccept(clazz, this);
//    }
//
//
//    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
//    {
//        if (arrayElementValue.u2elementNameIndex != 0)
//        {
//            markConstant(clazz, arrayElementValue.u2elementNameIndex);
//        }
//
//        // Mark the constant pool entries referenced by the element values.
//        arrayElementValue.elementValuesAccept(clazz, annotation, this);
//    }


    // Implementations for InstructionVisitor.

    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        markConstant(clazz, constantInstruction.constantIndex);
    }


    // Small utility methods.

    /**
     * Marks the given visitor accepter as being used.
     */
    protected void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }


    /**
     * Returns whether the given visitor accepter should still be marked as
     * being used.
     */
    protected boolean shouldBeMarkedAsUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() != USED;
    }


    /**
     * Returns whether the given visitor accepter has been marked as being used.
     */
    protected boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }


    /**
     * Marks the given visitor accepter as possibly being used.
     */
    protected void markAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(POSSIBLY_USED);
    }


    /**
     * Returns whether the given visitor accepter should still be marked as
     * possibly being used.
     */
    protected boolean shouldBeMarkedAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() != USED &&
               visitorAccepter.getVisitorInfo() != POSSIBLY_USED;
    }


    /**
     * Returns whether the given visitor accepter has been marked as possibly
     * being used.
     */
    protected boolean isPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == POSSIBLY_USED;
    }


    /**
     * Clears any usage marks from the given visitor accepter.
     */
    protected void markAsUnused(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(null);
    }


    /**
     * Marks the given constant pool entry of the given class. This includes
     * visiting any referenced objects.
     */
    private void markConstant(Clazz clazz, int index)
    {
         clazz.constantPoolEntryAccept(index, this);
    }
}
