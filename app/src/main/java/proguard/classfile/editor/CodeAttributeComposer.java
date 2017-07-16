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

import proguard.classfile.ClassConstants;
import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ExceptionInfo;
import proguard.classfile.attribute.LineNumberInfo;
import proguard.classfile.attribute.LineNumberTableAttribute;
import proguard.classfile.attribute.LocalVariableInfo;
import proguard.classfile.attribute.LocalVariableTableAttribute;
import proguard.classfile.attribute.LocalVariableTypeInfo;
import proguard.classfile.attribute.LocalVariableTypeTableAttribute;
import proguard.classfile.attribute.preverification.FullFrame;
import proguard.classfile.attribute.preverification.MoreZeroFrame;
import proguard.classfile.attribute.preverification.SameOneFrame;
import proguard.classfile.attribute.preverification.StackMapAttribute;
import proguard.classfile.attribute.preverification.StackMapFrame;
import proguard.classfile.attribute.preverification.StackMapTableAttribute;
import proguard.classfile.attribute.preverification.UninitializedType;
import proguard.classfile.attribute.preverification.VerificationType;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
import proguard.classfile.attribute.visitor.LineNumberInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableInfoVisitor;
import proguard.classfile.attribute.visitor.LocalVariableTypeInfoVisitor;
import proguard.classfile.instruction.BranchInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.InstructionConstants;
import proguard.classfile.instruction.InstructionFactory;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.SwitchInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;

/**
 * This AttributeVisitor accumulates instructions and exceptions, and then
 * copies them into code attributes that it visits.
 *
 * @author Eric Lafortune
 */
public class CodeAttributeComposer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor,
             StackMapFrameVisitor,
             VerificationTypeVisitor,
             LineNumberInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    //*
    private static final boolean DEBUG = false;
    /*/
    public  static       boolean DEBUG = true;
    //*/


    private static final int MAXIMUM_LEVELS = 32;
    private static final int INVALID        = -1;


    private boolean allowExternalExceptionHandlers;

    private int maximumCodeLength;
    private int codeLength;
    private int exceptionTableLength;
    private int level = -1;

    private byte[]  code                  = new byte[ClassConstants.TYPICAL_CODE_LENGTH];
    private int[]   oldInstructionOffsets = new int[ClassConstants.TYPICAL_CODE_LENGTH];

    private final int[]   codeFragmentOffsets  = new int[MAXIMUM_LEVELS];
    private final int[]   codeFragmentLengths  = new int[MAXIMUM_LEVELS];
    private final int[][] instructionOffsetMap = new int[MAXIMUM_LEVELS][ClassConstants.TYPICAL_CODE_LENGTH + 1];

    private ExceptionInfo[] exceptionTable = new ExceptionInfo[ClassConstants.TYPICAL_EXCEPTION_TABLE_LENGTH];

    private int expectedStackMapFrameOffset;

    private final StackSizeUpdater    stackSizeUpdater    = new StackSizeUpdater();
    private final VariableSizeUpdater variableSizeUpdater = new VariableSizeUpdater();
//    private final InstructionWriter   instructionWriter   = new InstructionWriter();


    /**
     * Creates a new CodeAttributeComposer that doesn't allow external exception
     * handlers.
     */
    public CodeAttributeComposer()
    {
        this(false);
    }


    /**
     * Creates a new CodeAttributeComposer that optionally allows external
     * exception handlers.
     */
    public CodeAttributeComposer(boolean allowExternalExceptionHandlers)
    {
        this.allowExternalExceptionHandlers = allowExternalExceptionHandlers;
    }


    /**
     * Starts a new code definition.
     */
    public void reset()
    {
        maximumCodeLength    = 0;
        codeLength           = 0;
        exceptionTableLength = 0;
        level                = -1;
    }


    /**
     * Starts a new code fragment. Branch instructions that are added are
     * assumed to be relative within such code fragments.
     * @param maximumCodeFragmentLength the maximum length of the code that will
     *                                  be added as part of this fragment.
     */
    public void beginCodeFragment(int maximumCodeFragmentLength)
    {
        level++;

        if (level >= MAXIMUM_LEVELS)
        {
            throw new IllegalArgumentException("Maximum number of code fragment levels exceeded ["+level+"]");
        }

//        // TODO: Figure out some length.
//        if (level == 0)
//        {
//            // Prepare for possible widening of instructions.
//            instructionWriter.reset(2 * maximumCodeFragmentLength);
//        }

        // Make sure there is sufficient space for adding the code fragment.
        maximumCodeLength += maximumCodeFragmentLength;

        ensureCodeLength(maximumCodeLength);

        // Try to reuse the previous array for this code fragment.
        if (instructionOffsetMap[level].length <= maximumCodeFragmentLength)
        {
            instructionOffsetMap[level] = new int[maximumCodeFragmentLength + 1];
        }

        // Initialize the offset map.
        for (int index = 0; index <= maximumCodeFragmentLength; index++)
        {
            instructionOffsetMap[level][index] = INVALID;
        }

        // Remember the location of the code fragment.
        codeFragmentOffsets[level] = codeLength;
        codeFragmentLengths[level] = maximumCodeFragmentLength;
    }


    /**
     * Appends the given instruction with the given old offset.
     * @param oldInstructionOffset the old offset of the instruction, to which
     *                             branches and other references in the current
     *                             code fragment are pointing.
     * @param instruction          the instruction to be appended.
     */
    public void appendInstruction(int         oldInstructionOffset,
                                  Instruction instruction)
    {
        if (DEBUG)
        {
            println("["+codeLength+"] <- ", instruction.toString(oldInstructionOffset));
        }

        // Make sure the code array is large enough.
        int newCodeLength = codeLength + instruction.length(codeLength);

        ensureCodeLength(newCodeLength);

        // Remember the old offset of the appended instruction.
        oldInstructionOffsets[codeLength] = oldInstructionOffset;

        // Write the instruction.
//        instruction.accept(null,
//                           null,
//                           new CodeAttribute(0, 0, 0, 0, code, 0, null, 0, null),
//                           codeLength,
//                           instructionWriter);
        instruction.write(code, codeLength);

        // Fill out the new offset of the appended instruction.
        instructionOffsetMap[level][oldInstructionOffset] = codeLength;

        // Continue appending at the next instruction offset.
        codeLength = newCodeLength;
    }


    /**
     * Appends the given label with the given old offset.
     * @param oldInstructionOffset the old offset of the label, to which
     *                             branches and other references in the current
     *                             code fragment are pointing.
     */
    public void appendLabel(int oldInstructionOffset)
    {
        if (DEBUG)
        {
            println("["+codeLength+"] <- ", "[" + oldInstructionOffset + "] (label)");
        }

        // Fill out the new offset of the appended instruction.
        instructionOffsetMap[level][oldInstructionOffset] = codeLength;
    }


    /**
     * Appends the given exception to the exception table.
     * @param exceptionInfo the exception to be appended.
     */
    public void appendException(ExceptionInfo exceptionInfo)
    {
        if (DEBUG)
        {
            print("         ", "Exception ["+exceptionInfo.u2startPC+" -> "+exceptionInfo.u2endPC+": "+exceptionInfo.u2handlerPC+"]");
        }

        // Remap the exception right away.
        visitExceptionInfo(null, null, null, exceptionInfo);

        if (DEBUG)
        {
            System.out.println(" -> ["+exceptionInfo.u2startPC+" -> "+exceptionInfo.u2endPC+": "+exceptionInfo.u2handlerPC+"]");
        }

        // Don't add the exception if its instruction range is empty.
        if (exceptionInfo.u2startPC == exceptionInfo.u2endPC)
        {
            if (DEBUG)
            {
                println("         ", "  (not added because of empty instruction range)");
            }

            return;
        }

        // Make sure there is sufficient space in the exception table.
        if (exceptionTable.length <= exceptionTableLength)
        {
            ExceptionInfo[] newExceptionTable = new ExceptionInfo[exceptionTableLength+1];
            System.arraycopy(exceptionTable, 0, newExceptionTable, 0, exceptionTableLength);
            exceptionTable = newExceptionTable;
        }

        // Add the exception.
        exceptionTable[exceptionTableLength++] = exceptionInfo;
    }


    /**
     * Wraps up the current code fragment, continuing with the previous one on
     * the stack.
     */
    public void endCodeFragment()
    {
        if (level < 0)
        {
            throw new IllegalArgumentException("Code fragment not begun ["+level+"]");
        }

        // Remap the instructions of the code fragment.
        int instructionOffset = codeFragmentOffsets[level];
        while (instructionOffset < codeLength)
        {
            // Get the next instruction.
            Instruction instruction = InstructionFactory.create(code, instructionOffset);

            // Does this instruction still have to be remapped?
            if (oldInstructionOffsets[instructionOffset] >= 0)
            {
                // Adapt the instruction for its new offset.
                instruction.accept(null, null, null, instructionOffset, this);

                // Write the instruction back.
//                instruction.accept(null,
//                                   null,
//                                   new CodeAttribute(0, 0, 0, 0, code, 0, null, 0, null),
//                                   instructionOffset,
//                                   instructionWriter);
                instruction.write(code, instructionOffset);

                // Don't remap this instruction again.
                oldInstructionOffsets[instructionOffset] = -1;
            }

            // Continue remapping at the next instruction offset.
            instructionOffset += instruction.length(instructionOffset);
        }

        // Correct the estimated maximum code length, now that we know the
        // actual length of this code fragment.
        maximumCodeLength += codeLength - codeFragmentOffsets[level] -
                             codeFragmentLengths[level];

        // Try to remap the exception handlers that couldn't be remapped before.
        if (allowExternalExceptionHandlers)
        {
            for (int index = 0; index < exceptionTableLength; index++)
            {
                ExceptionInfo exceptionInfo = exceptionTable[index];

                // Unmapped exception handlers are still negated.
                int handlerPC = -exceptionInfo.u2handlerPC;
                if (handlerPC > 0)
                {
                    if (remappableInstructionOffset(handlerPC))
                    {
                        exceptionInfo.u2handlerPC = remapInstructionOffset(handlerPC);
                    }
                    else if (level == 0)
                    {
                        throw new IllegalStateException("Couldn't remap exception handler offset ["+handlerPC+"]");
                    }
                }
            }
        }

        level--;
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println("CodeAttributeComposer: putting results in ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
        }

        if (level != -1)
        {
            throw new IllegalArgumentException("Code fragment not ended ["+level+"]");
        }

        level++;

        // Make sure the code attribute has sufficient space for the composed
        // code.
        if (codeAttribute.u4codeLength < codeLength)
        {
            codeAttribute.code = new byte[codeLength];
        }

        // Copy the composed code over into the code attribute.
        codeAttribute.u4codeLength = codeLength;
        System.arraycopy(code, 0, codeAttribute.code, 0, codeLength);

        // Remove exceptions with empty code blocks (done before).
        //exceptionTableLength =
        //    removeEmptyExceptions(exceptionTable, exceptionTableLength);

        // Make sure the exception table has sufficient space for the composed
        // exceptions.
        if (codeAttribute.exceptionTable.length < exceptionTableLength)
        {
            codeAttribute.exceptionTable = new ExceptionInfo[exceptionTableLength];
        }

        // Copy the exception table.
        codeAttribute.u2exceptionTableLength = exceptionTableLength;
        System.arraycopy(exceptionTable, 0, codeAttribute.exceptionTable, 0, exceptionTableLength);

        // Update the maximum stack size and local variable frame size.
        stackSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        variableSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);

        // Remap  the line number table and the local variable table.
        codeAttribute.attributesAccept(clazz, method, this);

        // Remap the exception table.
        //codeAttribute.exceptionsAccept(clazz, method, this);

        // Remove exceptions with empty code blocks (done before).
        //codeAttribute.u2exceptionTableLength =
        //    removeEmptyExceptions(codeAttribute.exceptionTable,
        //                          codeAttribute.u2exceptionTableLength);

//        // Make sure instructions are widened if necessary.
//        instructionWriter.visitCodeAttribute(clazz, method, codeAttribute);

        level--;
    }


    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        // Remap all stack map entries.
        expectedStackMapFrameOffset = -1;
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        // Remap all stack map table entries.
        expectedStackMapFrameOffset = 0;
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }


    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        // Remap all line number table entries.
        lineNumberTableAttribute.lineNumbersAccept(clazz, method, codeAttribute, this);

        // Remove line numbers with empty code blocks.
        lineNumberTableAttribute.u2lineNumberTableLength =
           removeEmptyLineNumbers(lineNumberTableAttribute.lineNumberTable,
                                  lineNumberTableAttribute.u2lineNumberTableLength,
                                  codeAttribute.u4codeLength);
    }


    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        // Remap all local variable table entries.
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);

        // Remove local variables with empty code blocks.
        localVariableTableAttribute.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttribute.localVariableTable,
                                      localVariableTableAttribute.u2localVariableTableLength,
                                      codeAttribute.u2maxLocals);
    }


    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        // Remap all local variable table entries.
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);

        // Remove local variables with empty code blocks.
        localVariableTypeTableAttribute.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttribute.localVariableTypeTable,
                                          localVariableTypeTableAttribute.u2localVariableTypeTableLength,
                                          codeAttribute.u2maxLocals);
    }


    // Implementations for InstructionVisitor.

    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}


    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        // Adjust the branch offset.
        branchInstruction.branchOffset = remapBranchOffset(offset,
                                                           branchInstruction.branchOffset);
    }


    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        // Adjust the default jump offset.
        switchInstruction.defaultOffset = remapBranchOffset(offset,
                                                            switchInstruction.defaultOffset);

        // Adjust the jump offsets.
        remapJumpOffsets(offset,
                         switchInstruction.jumpOffsets);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        // Remap the code offsets. Note that the instruction offset map also has
        // an entry for the first offset after the code, for u2endPC.
        exceptionInfo.u2startPC = remapInstructionOffset(exceptionInfo.u2startPC);
        exceptionInfo.u2endPC   = remapInstructionOffset(exceptionInfo.u2endPC);

        // See if we can remap the handler right away. Unmapped exception
        // handlers are negated, in order to mark them as external.
        int handlerPC = exceptionInfo.u2handlerPC;
        exceptionInfo.u2handlerPC =
            !allowExternalExceptionHandlers ||
            remappableInstructionOffset(handlerPC) ?
                remapInstructionOffset(handlerPC) :
                -handlerPC;
    }


    // Implementations for StackMapFrameVisitor.

    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame)
    {
        // Remap the stack map frame offset.
        int stackMapFrameOffset = remapInstructionOffset(offset);

        int offsetDelta = stackMapFrameOffset;

        // Compute the offset delta if the frame is part of a stack map frame
        // table (for JDK 6.0) instead of a stack map (for Java Micro Edition).
        if (expectedStackMapFrameOffset >= 0)
        {
            offsetDelta -= expectedStackMapFrameOffset;

            expectedStackMapFrameOffset = stackMapFrameOffset + 1;
        }

        stackMapFrame.u2offsetDelta = offsetDelta;
    }


    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        // Remap the stack map frame offset.
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, sameOneFrame);

        // Remap the verification type offset.
        sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        // Remap the stack map frame offset.
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, moreZeroFrame);

        // Remap the verification type offsets.
        moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
    }


    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        // Remap the stack map frame offset.
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, fullFrame);

        // Remap the verification type offsets.
        fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
        fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
    }


    // Implementations for VerificationTypeVisitor.

    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType) {}


    public void visitUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType uninitializedType)
    {
        // Remap the offset of the 'new' instruction.
        uninitializedType.u2newInstructionOffset = remapInstructionOffset(uninitializedType.u2newInstructionOffset);
    }


    // Implementations for LineNumberInfoVisitor.

    public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo)
    {
        // Remap the code offset.
        lineNumberInfo.u2startPC = remapInstructionOffset(lineNumberInfo.u2startPC);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        // Remap the code offset and length.
        // TODO: The local variable frame might not be strictly preserved.
        int startPC = remapInstructionOffset(localVariableInfo.u2startPC);
        int endPC   = remapInstructionOffset(localVariableInfo.u2startPC + localVariableInfo.u2length);

        localVariableInfo.u2startPC = startPC;
        localVariableInfo.u2length  = endPC - startPC;
    }

    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Remap the code offset and length.
        // TODO: The local variable frame might not be strictly preserved.
        int startPC = remapInstructionOffset(localVariableTypeInfo.u2startPC);
        int endPC   = remapInstructionOffset(localVariableTypeInfo.u2startPC + localVariableTypeInfo.u2length);

        localVariableTypeInfo.u2startPC = startPC;
        localVariableTypeInfo.u2length  = endPC - startPC;
    }


    // Small utility methods.

    /**
     * Make sure the code arrays have at least the given size.
     */
    private void ensureCodeLength(int newCodeLength)
    {
        if (code.length < newCodeLength)
        {
            // Add 20% to avoid extending the arrays too often.
            newCodeLength = newCodeLength * 6 / 5;

            byte[] newCode = new byte[newCodeLength];
            System.arraycopy(code, 0, newCode, 0, codeLength);
            code = newCode;

            int[] newOldInstructionOffsets = new int[newCodeLength];
            System.arraycopy(oldInstructionOffsets, 0, newOldInstructionOffsets, 0, codeLength);
            oldInstructionOffsets = newOldInstructionOffsets;
        }
    }


    /**
     * Adjusts the given jump offsets for the instruction at the given offset.
     */
    private void remapJumpOffsets(int offset, int[] jumpOffsets)
    {
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            jumpOffsets[index] = remapBranchOffset(offset, jumpOffsets[index]);
        }
    }


    /**
     * Computes the new branch offset for the instruction at the given new offset
     * with the given old branch offset.
     */
    private int remapBranchOffset(int newInstructionOffset, int branchOffset)
    {
        if (newInstructionOffset < 0 ||
            newInstructionOffset > codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+newInstructionOffset +"] in code with length ["+codeLength+"]");
        }

        int oldInstructionOffset = oldInstructionOffsets[newInstructionOffset];

        return remapInstructionOffset(oldInstructionOffset + branchOffset) -
               remapInstructionOffset(oldInstructionOffset);
    }


    /**
     * Computes the new instruction offset for the instruction at the given old
     * offset.
     */
    private int remapInstructionOffset(int oldInstructionOffset)
    {
        if (oldInstructionOffset < 0 ||
            oldInstructionOffset > codeFragmentLengths[level])
        {
            throw new IllegalArgumentException("Instruction offset ["+oldInstructionOffset +"] out of range in code fragment with length ["+codeFragmentLengths[level]+"] at level "+level);
        }

        int newInstructionOffset = instructionOffsetMap[level][oldInstructionOffset];
        if (newInstructionOffset == INVALID)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+oldInstructionOffset +"] in code fragment at level "+level);
        }

        return newInstructionOffset;
    }


    /**
     * Returns whether the given old instruction offset can be remapped at the
     */
    private boolean remappableInstructionOffset(int oldInstructionOffset)
    {
        return
            oldInstructionOffset <= codeFragmentLengths[level] &&
            instructionOffsetMap[level][oldInstructionOffset] > INVALID;
    }


    /**
     * Returns the given list of exceptions, without the ones that have empty
     * code blocks.
     */
    private int removeEmptyExceptions(ExceptionInfo[] exceptionInfos,
                                      int             exceptionInfoCount)
    {
        // Overwrite all empty exceptions.
        int newIndex = 0;
        for (int index = 0; index < exceptionInfoCount; index++)
        {
            ExceptionInfo exceptionInfo = exceptionInfos[index];
            if (exceptionInfo.u2startPC < exceptionInfo.u2endPC)
            {
                exceptionInfos[newIndex++] = exceptionInfo;
            }
        }

        // Clear the unused array entries.
        for (int index = newIndex; index < exceptionInfoCount; index++)
        {
            exceptionInfos[index] = null;
        }

        return newIndex;
    }


    /**
     * Returns the given list of line numbers, without the ones that have empty
     * code blocks or that exceed the code size.
     */
    private int removeEmptyLineNumbers(LineNumberInfo[] lineNumberInfos,
                                       int              lineNumberInfoCount,
                                       int              codeLength)
    {
        // Overwrite all empty line number entries.
        int newIndex = 0;
        for (int index = 0; index < lineNumberInfoCount; index++)
        {
            LineNumberInfo lineNumberInfo = lineNumberInfos[index];
            int startPC = lineNumberInfo.u2startPC;
            if (startPC < codeLength &&
                (index == 0 || startPC > lineNumberInfos[index-1].u2startPC))
            {
                lineNumberInfos[newIndex++] = lineNumberInfo;
            }
        }

        // Clear the unused array entries.
        for (int index = newIndex; index < lineNumberInfoCount; index++)
        {
            lineNumberInfos[index] = null;
        }

        return newIndex;
    }


    /**
     * Returns the given list of local variables, without the ones that have empty
     * code blocks or that exceed the actual number of local variables.
     */
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount,
                                          int                 maxLocals)
    {
        // Overwrite all empty local variable entries.
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount; index++)
        {
            LocalVariableInfo localVariableInfo = localVariableInfos[index];
            if (localVariableInfo.u2length > 0 &&
                localVariableInfo.u2index < maxLocals)
            {
                localVariableInfos[newIndex++] = localVariableInfo;
            }
        }

        // Clear the unused array entries.
        for (int index = newIndex; index < localVariableInfoCount; index++)
        {
            localVariableInfos[index] = null;
        }

        return newIndex;
    }


    /**
     * Returns the given list of local variable types, without the ones that
     * have empty code blocks or that exceed the actual number of local variables.
     */
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount,
                                              int                     maxLocals)
    {
        // Overwrite all empty local variable type entries.
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount; index++)
        {
            LocalVariableTypeInfo localVariableTypeInfo = localVariableTypeInfos[index];
            if (localVariableTypeInfo.u2length > 0 &&
                localVariableTypeInfo.u2index < maxLocals)
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfo;
            }
        }

        // Clear the unused array entries.
        for (int index = newIndex; index < localVariableTypeInfoCount; index++)
        {
            localVariableTypeInfos[index] = null;
        }

        return newIndex;
    }


    private void println(String string1, String string2)
    {
        print(string1, string2);

        System.out.println();
    }

    private void print(String string1, String string2)
    {
        System.out.print(string1);

        for (int index = 0; index < level; index++)
        {
            System.out.print("  ");
        }

        System.out.print(string2);
    }


    public static void main(String[] args)
    {
        CodeAttributeComposer composer = new CodeAttributeComposer();

        composer.beginCodeFragment(4);
        composer.appendInstruction(0, new SimpleInstruction(InstructionConstants.OP_ICONST_0));
        composer.appendInstruction(1, new VariableInstruction(InstructionConstants.OP_ISTORE, 0));
        composer.appendInstruction(2, new BranchInstruction(InstructionConstants.OP_GOTO, 1));

        composer.beginCodeFragment(4);
        composer.appendInstruction(0, new VariableInstruction(InstructionConstants.OP_IINC, 0, 1));
        composer.appendInstruction(1, new VariableInstruction(InstructionConstants.OP_ILOAD, 0));
        composer.appendInstruction(2, new SimpleInstruction(InstructionConstants.OP_ICONST_5));
        composer.appendInstruction(3, new BranchInstruction(InstructionConstants.OP_IFICMPLT, -3));
        composer.endCodeFragment();

        composer.appendInstruction(3, new SimpleInstruction(InstructionConstants.OP_RETURN));
        composer.endCodeFragment();
    }
}
