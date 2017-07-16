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
package proguard.optimize.info;

import java.util.HashSet;
import java.util.Set;

import proguard.classfile.ClassConstants;
import proguard.classfile.Clazz;
import proguard.classfile.LibraryClass;
import proguard.classfile.LibraryField;
import proguard.classfile.LibraryMethod;
import proguard.classfile.Method;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramField;
import proguard.classfile.ProgramMethod;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.FieldrefConstant;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.BranchInstruction;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.InstructionConstants;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassCollector;
import proguard.classfile.visitor.MemberVisitor;

/**
 * This class can tell whether an instruction has any side effects. Return
 * instructions can be included or not.
 *
 * @see ReadWriteFieldMarker
 * @see StaticInitializerContainingClassMarker
 * @see NoSideEffectMethodMarker
 * @see SideEffectMethodMarker
 * @author Eric Lafortune
 */
public class SideEffectInstructionChecker
extends SimplifiedVisitor
implements InstructionVisitor,
        ConstantVisitor,
             MemberVisitor
{
    private final boolean includeReturnInstructions;

    // A return value for the visitor methods.
    private Clazz   referencingClass;
    private boolean hasSideEffects;


    public SideEffectInstructionChecker(boolean includeReturnInstructions)
    {
        this.includeReturnInstructions = includeReturnInstructions;
    }


    public boolean hasSideEffects(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        hasSideEffects = false;

        instruction.accept(clazz, method, codeAttribute, offset, this);

        return hasSideEffects;
    }


    // Implementations for InstructionVisitor.

    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}


    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        byte opcode = simpleInstruction.opcode;

        // Check for instructions that might cause side effects.
        if (opcode == InstructionConstants.OP_IASTORE      ||
            opcode == InstructionConstants.OP_LASTORE      ||
            opcode == InstructionConstants.OP_FASTORE      ||
            opcode == InstructionConstants.OP_DASTORE      ||
            opcode == InstructionConstants.OP_AASTORE      ||
            opcode == InstructionConstants.OP_BASTORE      ||
            opcode == InstructionConstants.OP_CASTORE      ||
            opcode == InstructionConstants.OP_SASTORE      ||
            opcode == InstructionConstants.OP_ATHROW       ||
            opcode == InstructionConstants.OP_MONITORENTER ||
            opcode == InstructionConstants.OP_MONITOREXIT  ||
            (includeReturnInstructions &&
             (opcode == InstructionConstants.OP_IRETURN ||
              opcode == InstructionConstants.OP_LRETURN ||
              opcode == InstructionConstants.OP_FRETURN ||
              opcode == InstructionConstants.OP_DRETURN ||
              opcode == InstructionConstants.OP_ARETURN ||
              opcode == InstructionConstants.OP_RETURN)))
        {
            // These instructions always cause a side effect.
            hasSideEffects = true;
        }

    }


    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        byte opcode = variableInstruction.opcode;

        // Check for instructions that might cause side effects.
        if (includeReturnInstructions &&
            opcode == InstructionConstants.OP_RET)
        {
            hasSideEffects = true;
        }
    }


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        byte opcode = constantInstruction.opcode;
        // Check for instructions that might cause side effects.
        if (opcode == InstructionConstants.OP_GETSTATIC     ||
            opcode == InstructionConstants.OP_PUTSTATIC     ||
            opcode == InstructionConstants.OP_GETFIELD      ||
            opcode == InstructionConstants.OP_PUTFIELD      ||
            opcode == InstructionConstants.OP_INVOKEVIRTUAL ||
            opcode == InstructionConstants.OP_INVOKESPECIAL ||
            opcode == InstructionConstants.OP_INVOKESTATIC  ||
            opcode == InstructionConstants.OP_INVOKEINTERFACE)
        {
            // Check if the field is write-only or volatile, or if the invoked
            // method is causing any side effects.
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
        }
    }


    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;

        // Check for instructions that might cause side effects.
        if (includeReturnInstructions &&
            (opcode == InstructionConstants.OP_JSR ||
             opcode == InstructionConstants.OP_JSR_W))
        {
            hasSideEffects = true;
        }
    }


    // Implementations for ConstantVisitor.

    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        // Pass the referencing class.
        referencingClass = clazz;

        // We'll have to assume accessing an unknown field has side effects.
        hasSideEffects = true;

        // Check the referenced field, if known.
        fieldrefConstant.referencedMemberAccept(this);
    }


    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant refConstant)
    {
        // Pass the referencing class.
        referencingClass = clazz;

        // We'll have to assume invoking an unknown method has side effects.
        hasSideEffects = true;

        // Check the referenced method, if known.
        refConstant.referencedMemberAccept(this);
    }


    // Implementations for MemberVisitor.

    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        hasSideEffects =
            (ReadWriteFieldMarker.isRead(programField) &&
             ReadWriteFieldMarker.isWritten(programField))                                ||
            ((programField.getAccessFlags() & ClassConstants.INTERNAL_ACC_VOLATILE) != 0) ||
            (!programClass.equals(referencingClass) &&
             !initializedSuperClasses(referencingClass).containsAll(initializedSuperClasses(programClass)));
    }


    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        // Note that side effects already include synchronization of some
        // implementation of the method.
        hasSideEffects =
            !NoSideEffectMethodMarker.hasNoSideEffects(programMethod) &&
            (SideEffectMethodMarker.hasSideEffects(programMethod) ||
             (!programClass.equals(referencingClass) &&
              !initializedSuperClasses(referencingClass).containsAll(initializedSuperClasses(programClass))));
    }


    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        hasSideEffects = true;
    }


    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        hasSideEffects =
            !NoSideEffectMethodMarker.hasNoSideEffects(libraryMethod);
    }


    /**
     * Returns the set of superclasses and interfaces that are initialized.
     */
    private Set initializedSuperClasses(Clazz clazz)
    {
        Set set = new HashSet();

        // Visit all superclasses and interfaces, collecting the ones that have
        // static initializers.
        clazz.hierarchyAccept(true, true, true, false,
                              new StaticInitializerContainingClassFilter(
                              new ClassCollector(set)));

        return set;
    }
}
