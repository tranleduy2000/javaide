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
package proguard.optimize.peephole;

import proguard.classfile.ClassConstants;
import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ExceptionInfo;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.MethodrefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.BranchInstruction;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.InstructionConstants;
import proguard.classfile.instruction.InstructionFactory;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.SwitchInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;

/**
 * This AttributeVisitor finds all instruction offsets, branch targets, and
 * exception targets in the CodeAttribute objects that it visits.
 *
 * @author Eric Lafortune
 */
public class BranchTargetFinder
extends SimplifiedVisitor
implements AttributeVisitor,
        InstructionVisitor,
        ExceptionInfoVisitor,
        ConstantVisitor
{
    //*
    private static final boolean DEBUG = false;
    /*/
    private static       boolean DEBUG = true;
    //*/

    public static final int NONE            = -2;
    public static final int AT_METHOD_ENTRY = -1;

    private static final short INSTRUCTION           = 1 << 0;
    private static final short BRANCH_ORIGIN         = 1 << 1;
    private static final short BRANCH_TARGET         = 1 << 2;
    private static final short AFTER_BRANCH          = 1 << 3;
    private static final short EXCEPTION_START       = 1 << 4;
    private static final short EXCEPTION_END         = 1 << 5;
    private static final short EXCEPTION_HANDLER     = 1 << 6;
    private static final short SUBROUTINE_INVOCATION = 1 << 7;
    private static final short SUBROUTINE_RETURNING  = 1 << 8;

    private static final int MAXIMUM_CREATION_OFFSETS = 32;


    private short[] instructionMarks      = new short[ClassConstants.TYPICAL_CODE_LENGTH + 1];
    private int[]   subroutineStarts      = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int[]   subroutineEnds        = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int[]   creationOffsets       = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int[]   initializationOffsets = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int     superInitializationOffset;

    private int     currentSubroutineStart;
    private int     currentSubroutineEnd;
    private int[]   recentCreationOffsets = new int[MAXIMUM_CREATION_OFFSETS];
    private int     recentCreationOffsetIndex;
    private boolean isInitializer;


    /**
     * Returns whether there is an instruction at the given offset in the
     * CodeAttribute that was visited most recently.
     */
    public boolean isInstruction(int offset)
    {
        return (instructionMarks[offset] & INSTRUCTION) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the target of
     * any kind in the CodeAttribute that was visited most recently.
     */
    public boolean isTarget(int offset)
    {
        return offset == 0 ||
               (instructionMarks[offset] & (BRANCH_TARGET   |
                                            EXCEPTION_START |
                                            EXCEPTION_END   |
                                            EXCEPTION_HANDLER)) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the origin of a
     * branch instruction in the CodeAttribute that was visited most recently.
     */
    public boolean isBranchOrigin(int offset)
    {
        return (instructionMarks[offset] & BRANCH_ORIGIN) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the target of a
     * branch instruction in the CodeAttribute that was visited most recently.
     */
    public boolean isBranchTarget(int offset)
    {
        return (instructionMarks[offset] & BRANCH_TARGET) != 0;
    }


    /**
     * Returns whether the instruction at the given offset comes right after a
     * definite branch instruction in the CodeAttribute that was visited most
     * recently.
     */
    public boolean isAfterBranch(int offset)
    {
        return (instructionMarks[offset] & AFTER_BRANCH) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the start of an
     * exception try block in the CodeAttribute that was visited most recently.
     */
    public boolean isExceptionStart(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_START) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the end of an
     * exception try block in the CodeAttribute that was visited most recently.
     */
    public boolean isExceptionEnd(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_END) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the start of an
     * exception catch block in the CodeAttribute that was visited most recently.
     */
    public boolean isExceptionHandler(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_HANDLER) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is a subroutine
     * invocation in the CodeAttribute that was visited most recently.
     */
    public boolean isSubroutineInvocation(int offset)
    {
        return (instructionMarks[offset] & SUBROUTINE_INVOCATION) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the start of a
     * subroutine in the CodeAttribute that was visited most recently.
     */
    public boolean isSubroutineStart(int offset)
    {
        return subroutineStarts[offset] == offset;
    }


    /**
     * Returns whether the instruction at the given offset is part of a
     * subroutine in the CodeAttribute that was visited most recently.
     */
    public boolean isSubroutine(int offset)
    {
        return subroutineStarts[offset] != NONE;
    }


    /**
     * Returns whether the subroutine at the given offset is ever returning
     * by means of a regular 'ret' instruction.
     */
    public boolean isSubroutineReturning(int offset)
    {
        return (instructionMarks[offset] & SUBROUTINE_RETURNING) != 0;
    }


    /**
     * Returns the start offset of the subroutine at the given offset, in the
     * CodeAttribute that was visited most recently.
     */
    public int subroutineStart(int offset)
    {
        return subroutineStarts[offset];
    }


    /**
     * Returns the offset after the subroutine at the given offset, in the
     * CodeAttribute that was visited most recently.
     */
    public int subroutineEnd(int offset)
    {
        return subroutineEnds[offset];
    }


    /**
     * Returns whether the instruction at the given offset is a 'new'
     * instruction, in the CodeAttribute that was visited most recently.
     */
    public boolean isNew(int offset)
    {
        return initializationOffsets[offset] != NONE;
    }


    /**
     * Returns the instruction offset at which the object instance that is
     * created at the given 'new' instruction offset is initialized, or
     * <code>NONE</code> if it is not being created.
     */
    public int initializationOffset(int offset)
    {
        return initializationOffsets[offset];
    }


    /**
     * Returns whether the method is an instance initializer, in the
     * CodeAttribute that was visited most recently.
     */
    public boolean isInitializer()
    {
        return superInitializationOffset != NONE;
    }


    /**
     * Returns the instruction offset at which this initializer is calling
     * the "super" or "this" initializer method, or <code>NONE</code> if it is
     * not an initializer.
     */
    public int superInitializationOffset()
    {
        return superInitializationOffset;
    }


    /**
     * Returns whether the instruction at the given offset is the special
     * invocation of an instance initializer, in the CodeAttribute that was
     * visited most recently.
     */
    public boolean isInitializer(int offset)
    {
        return creationOffsets[offset] != NONE;
    }


    /**
     * Returns the offset of the 'new' instruction that corresponds to the
     * invocation of the instance initializer at the given offset, or
     * <code>AT_METHOD_ENTRY</code> if the invocation is calling the "super" or
     * "this" initializer method, , or <code>NONE</code> if it is not a 'new'
     * instruction.
     */
    public int creationOffset(int offset)
    {
        return creationOffsets[offset];
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
//        DEBUG =
//            clazz.getName().equals("abc/Def") &&
//            method.getName(clazz).equals("abc");

        // Make sure there are sufficiently large arrays.
        int codeLength = codeAttribute.u4codeLength;
        if (subroutineStarts.length < codeLength)
        {
            // Create new arrays.
            instructionMarks      = new short[codeLength + 1];
            subroutineStarts      = new int[codeLength];
            subroutineEnds        = new int[codeLength];
            creationOffsets       = new int[codeLength];
            initializationOffsets = new int[codeLength];

            // Reset the arrays.
            for (int index = 0; index < codeLength; index++)
            {
                subroutineStarts[index]      = NONE;
                subroutineEnds[index]        = NONE;
                creationOffsets[index]       = NONE;
                initializationOffsets[index] = NONE;
            }
        }
        else
        {
            // Reset the arrays.
            for (int index = 0; index < codeLength; index++)
            {
                instructionMarks[index]      = 0;
                subroutineStarts[index]      = NONE;
                subroutineEnds[index]        = NONE;
                creationOffsets[index]       = NONE;
                initializationOffsets[index] = NONE;
            }

            instructionMarks[codeLength] = 0;
        }

        superInitializationOffset = NONE;

        // We're assuming all subroutines are contiguous blocks of code.
        // We're not starting in a subroutine.
        currentSubroutineStart = NONE;
        currentSubroutineEnd   = NONE;

        recentCreationOffsetIndex = 0;

        // Initialize the stack of 'new' instruction offsets if this method is
        // an instance initializer.
        if (method.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            recentCreationOffsets[recentCreationOffsetIndex++] = AT_METHOD_ENTRY;
        }

        // The end of the code is a branch target sentinel.
        instructionMarks[codeLength] = BRANCH_TARGET;

        // Mark branch targets by going over all instructions.
        codeAttribute.instructionsAccept(clazz, method, this);

        // Mark branch targets in the exception table.
        codeAttribute.exceptionsAccept(clazz, method, this);

        // Fill out any gaps in the subroutine starts and the subroutine ends
        // and subroutine returning flags, working backward.

        // We're not starting in a subroutine.
        int     subroutineStart     = NONE;
        int     subroutineEnd       = codeLength;
        boolean subroutineReturning = false;

        for (int index = codeLength - 1; index >= 0; index--)
        {
            if (isInstruction(index))
            {
                // Are we inside a previously marked subroutine?
                if (subroutineStarts[index] != NONE)
                {
                    // Update the current subroutine start.
                    subroutineStart = subroutineStarts[index];
                }
                else if (subroutineStart != NONE)
                {
                    // Mark the subroutine start.
                    subroutineStarts[index] = subroutineStart;
                }

                // Did we reach the start of the subroutine.
                if (isSubroutineStart(index))
                {
                    // Stop marking it.
                    subroutineStart = NONE;
                }

                // Are we inside a subroutine?
                if (isSubroutine(index))
                {
                    // Mark the subroutine end.
                    subroutineEnds[index] = subroutineEnd;

                    // Update or mark the subroutine returning flag.
                    if (isSubroutineReturning(index))
                    {
                        subroutineReturning = true;
                    }
                    else if (subroutineReturning)
                    {
                        instructionMarks[index] |= SUBROUTINE_RETURNING;
                    }
                }
                else
                {
                    // Update the subroutine end and returning flag.
                    subroutineEnd       = index;
                    subroutineReturning = false;
                }
            }
        }

        if (DEBUG)
        {
            System.out.println();
            System.out.println("Branch targets: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));

            for (int index = 0; index < codeLength; index++)
            {
                if (isInstruction(index))
                {
                    System.out.println("" +
                                       (isBranchOrigin(index)         ? 'B' : '-') +
                                       (isAfterBranch(index)          ? 'b' : '-') +
                                       (isBranchTarget(index)         ? 'T' : '-') +
                                       (isExceptionStart(index)       ? 'E' : '-') +
                                       (isExceptionEnd(index)         ? 'e' : '-') +
                                       (isExceptionHandler(index)     ? 'H' : '-') +
                                       (isSubroutineInvocation(index) ? 'J' : '-') +
                                       (isSubroutineStart(index)      ? 'S' : '-') +
                                       (isSubroutineReturning(index)  ? 'r' : '-') +
                                       (isSubroutine(index)           ? " ["+subroutineStart(index)+" -> "+subroutineEnd(index)+"]" : "") +
                                       (isNew(index)                  ? " ["+initializationOffset(index)+"] " : " ---- ") +
                                       InstructionFactory.create(codeAttribute.code, index).toString(index));
                }
            }
        }
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;

        // Check if this is the first instruction of a subroutine.
        checkSubroutine(offset);

        byte opcode = simpleInstruction.opcode;
        if (opcode == InstructionConstants.OP_IRETURN ||
            opcode == InstructionConstants.OP_LRETURN ||
            opcode == InstructionConstants.OP_FRETURN ||
            opcode == InstructionConstants.OP_DRETURN ||
            opcode == InstructionConstants.OP_ARETURN ||
            opcode == InstructionConstants.OP_ATHROW)
        {
            // Mark the branch origin.
            markBranchOrigin(offset);

            // Mark the next instruction.
            markAfterBranchOrigin(offset + simpleInstruction.length(offset));
        }
    }


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;

        // Check if this is the first instruction of a subroutine.
        checkSubroutine(offset);

        // Check if the instruction is a 'new' instruction.
        if (constantInstruction.opcode == InstructionConstants.OP_NEW)
        {
            // Push the 'new' instruction offset on the stack.
            recentCreationOffsets[recentCreationOffsetIndex++] = offset;
        }
        else
        {
            // Check if the instruction is an initializer invocation.
            isInitializer = false;
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
            if (isInitializer)
            {
                // Pop the 'new' instruction offset from the stack.
                int recentCreationOffset = recentCreationOffsets[--recentCreationOffsetIndex];

                // Fill it out in the creation offsets.
                creationOffsets[offset] = recentCreationOffset;

                // Fill out the initialization offsets.
                if (recentCreationOffset == AT_METHOD_ENTRY)
                {
                    superInitializationOffset = offset;
                }
                else
                {
                    initializationOffsets[recentCreationOffset] = offset;
                }
            }
        }
    }


    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;

        // Check if this is the first instruction of a subroutine.
        checkSubroutine(offset);

        if (variableInstruction.opcode == InstructionConstants.OP_RET)
        {
            // Mark the branch origin.
            markBranchOrigin(offset);

            // Mark the regular subroutine return.
            instructionMarks[offset] |= SUBROUTINE_RETURNING;

            // Mark the next instruction.
            markAfterBranchOrigin(offset + variableInstruction.length(offset));
        }
    }


    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        // Mark the branch origin.
        markBranchOrigin(offset);

        // Check if this is the first instruction of a subroutine.
        checkSubroutine(offset);

        // Mark the branch target.
        markBranchTarget(offset, branchInstruction.branchOffset);

        byte opcode = branchInstruction.opcode;
        if (opcode == InstructionConstants.OP_JSR ||
            opcode == InstructionConstants.OP_JSR_W)
        {
            // Mark the subroutine invocation.
            instructionMarks[offset] |= SUBROUTINE_INVOCATION;

            // Mark the subroutine start.
            int targetOffset = offset + branchInstruction.branchOffset;
            subroutineStarts[targetOffset] = targetOffset;
        }
        else if (opcode == InstructionConstants.OP_GOTO ||
                 opcode == InstructionConstants.OP_GOTO_W)
        {
            // Mark the next instruction.
            markAfterBranchOrigin(offset + branchInstruction.length(offset));
        }
    }


    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        // Mark the branch origin.
        markBranchOrigin(offset);

        // Check if this is the first instruction of a subroutine.
        checkSubroutine(offset);

        // Mark the branch targets of the default jump offset.
        markBranchTarget(offset, switchInstruction.defaultOffset);

        // Mark the branch targets of the jump offsets.
        markBranchTargets(offset,
                          switchInstruction.jumpOffsets);

        // Mark the next instruction.
        markAfterBranchOrigin(offset + switchInstruction.length(offset));
    }


    // Implementations for ConstantVisitor.

    public void visitAnyConstant(Clazz clazz, Constant constant) {}


    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        isInitializer = methodrefConstant.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        // Mark the exception offsets.
        instructionMarks[exceptionInfo.u2startPC]   |= EXCEPTION_START;
        instructionMarks[exceptionInfo.u2endPC]     |= EXCEPTION_END;
        instructionMarks[exceptionInfo.u2handlerPC] |= EXCEPTION_HANDLER;
    }


    // Small utility methods.

    /**
     * Marks the branch targets of the given jump offsets for the instruction
     * at the given offset.
     */
    private void markBranchTargets(int offset, int[] jumpOffsets)
    {
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            markBranchTarget(offset, jumpOffsets[index]);
        }
    }


    /**
     * Marks the branch origin at the given offset.
     */
    private void markBranchOrigin(int offset)
    {
        instructionMarks[offset] |= INSTRUCTION | BRANCH_ORIGIN;
    }


    /**
     * Marks the branch target at the given offset.
     */
    private void markBranchTarget(int offset, int jumpOffset)
    {
        int targetOffset = offset + jumpOffset;

        instructionMarks[targetOffset] |= BRANCH_TARGET;

        // Are we inside a previously marked subroutine?
        if (isSubroutine(offset))
        {
            // Mark the subroutine start of the target.
            subroutineStarts[targetOffset] = currentSubroutineStart;

            // Update the current subroutine end.
            if (currentSubroutineEnd < targetOffset)
            {
                currentSubroutineEnd = targetOffset;
            }
        }
    }


    /**
     * Marks the instruction at the given offset, after a branch.
     */
    private void markAfterBranchOrigin(int nextOffset)
    {
        instructionMarks[nextOffset] |= AFTER_BRANCH;

        // Are we at the end of the current subroutine?
        if (currentSubroutineEnd <= nextOffset)
        {
            // Reset the subroutine start.
            currentSubroutineStart = NONE;
        }
    }


    /**
     * Checks if the specified instruction is inside a subroutine.
     */
    private void checkSubroutine(int offset)
    {
        // Are we inside a previously marked subroutine?
        if (isSubroutine(offset))
        {
            // Update the current subroutine start.
            currentSubroutineStart = subroutineStarts[offset];
        }
        else
        {
            // Mark the subroutine start (or NONE).
            subroutineStarts[offset] = currentSubroutineStart;
        }
    }
}
