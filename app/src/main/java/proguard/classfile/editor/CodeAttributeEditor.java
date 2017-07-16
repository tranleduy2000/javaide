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
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.InstructionFactory;
import proguard.classfile.instruction.LookUpSwitchInstruction;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.TableSwitchInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;

/**
 * This AttributeVisitor accumulates specified changes to code, and then applies
 * these accumulated changes to the code attributes that it visits.
 *
 * @author Eric Lafortune
 */
public class CodeAttributeEditor
extends SimplifiedVisitor
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
    private static       boolean DEBUG = true;
    //*/

    private boolean updateFrameSizes;

    private int     codeLength;
    private boolean modified;
    private boolean simple;

    /*private*/public Instruction[]    preInsertions  = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    /*private*/public Instruction[]    replacements   = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    /*private*/public Instruction[]    postInsertions = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    /*private*/public boolean[]        deleted        = new boolean[ClassConstants.TYPICAL_CODE_LENGTH];

    private int[]   instructionOffsetMap = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int     newOffset;
    private boolean lengthIncreased;

    private int expectedStackMapFrameOffset;

    private final StackSizeUpdater    stackSizeUpdater    = new StackSizeUpdater();
    private final VariableSizeUpdater variableSizeUpdater = new VariableSizeUpdater();
    private final InstructionWriter   instructionWriter   = new InstructionWriter();


    public CodeAttributeEditor()
    {
        this(true);
    }


    public CodeAttributeEditor(boolean updateFrameSizes)
    {
        this.updateFrameSizes = updateFrameSizes;
    }


    /**
     * Resets the accumulated code changes.
     * @param codeLength the length of the code that will be edited next.
     */
    public void reset(int codeLength)
    {
        this.codeLength = codeLength;

        // Try to reuse the previous arrays.
        if (preInsertions.length < codeLength)
        {
            preInsertions  = new Instruction[codeLength];
            replacements   = new Instruction[codeLength];
            postInsertions = new Instruction[codeLength];
            deleted        = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                preInsertions[index]  = null;
                replacements[index]   = null;
                postInsertions[index] = null;
                deleted[index]        = false;
            }
        }

        modified = false;
        simple   = true;

    }


    /**
     * Remembers to place the given instruction right before the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instruction       the new instruction.
     */
    public void insertBeforeInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        preInsertions[instructionOffset] = instruction;

        modified = true;
        simple   = false;

    }


    /**
     * Remembers to place the given instructions right before the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instructions      the new instructions.
     */
    public void insertBeforeInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        preInsertions[instructionOffset] = new CompositeInstruction(instructions);

        modified = true;
        simple   = false;

    }


    /**
     * Remembers to replace the instruction at the given offset by the given
     * instruction.
     * @param instructionOffset the offset of the instruction to be replaced.
     * @param instruction       the new instruction.
     */
    public void replaceInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        replacements[instructionOffset] = instruction;

        modified = true;
    }


    /**
     * Remembers to replace the instruction at the given offset by the given
     * instructions.
     * @param instructionOffset the offset of the instruction to be replaced.
     * @param instructions      the new instructions.
     */
    public void replaceInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        replacements[instructionOffset] = new CompositeInstruction(instructions);

        modified = true;
    }


    /**
     * Remembers to place the given instruction right after the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instruction       the new instruction.
     */
    public void insertAfterInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        postInsertions[instructionOffset] = instruction;

        modified = true;
        simple   = false;
    }


    /**
     * Remembers to place the given instructions right after the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instructions      the new instructions.
     */
    public void insertAfterInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        postInsertions[instructionOffset] = new CompositeInstruction(instructions);

        modified = true;
        simple   = false;
    }


    /**
     * Remembers to delete the instruction at the given offset.
     * @param instructionOffset the offset of the instruction to be deleted.
     */
    public void deleteInstruction(int instructionOffset)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        deleted[instructionOffset] = true;

        modified = true;
        simple   = false;
    }


    /**
     * Remembers not to delete the instruction at the given offset.
     * @param instructionOffset the offset of the instruction not to be deleted.
     */
    public void undeleteInstruction(int instructionOffset)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        deleted[instructionOffset] = false;
    }


    /**
     * Returns whether the instruction at the given offset has been modified
     * in any way.
     */
    public boolean isModified(int instructionOffset)
    {
        return preInsertions[instructionOffset]  != null ||
               replacements[instructionOffset]   != null ||
               postInsertions[instructionOffset] != null ||
               deleted[instructionOffset];
    }


    // Implementations for AttributeVisitor.

    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}


    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
//        DEBUG =
//            clazz.getName().equals("abc/Def") &&
//            method.getName(clazz).equals("abc");

        // TODO: Remove this when the code has stabilized.
        // Catch any unexpected exceptions from the actual visiting method.
        try
        {
            // Process the code.
            visitCodeAttribute0(clazz, method, codeAttribute);
        }
        catch (RuntimeException ex)
        {
            System.err.println("Unexpected error while editing code:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");

            throw ex;
        }
    }


    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println("CodeAttributeEditor: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
        }

        // Do we have to update the code?
        if (modified)
        {
            // Can we perform a faster simple replacement of instructions?
            if (canPerformSimpleReplacements(codeAttribute))
            {
                if (DEBUG)
                {
                    System.out.println("  Simple editing");
                }

                // Simply overwrite the instructions.
                performSimpleReplacements(codeAttribute);
            }
            else
            {
                if (DEBUG)
                {
                    System.out.println("  Full editing");
                }

                // Move and remap the instructions.
                codeAttribute.u4codeLength =
                    updateInstructions(clazz, method, codeAttribute);

                // Remap the exception table.
                codeAttribute.exceptionsAccept(clazz, method, this);

                // Remove exceptions with empty code blocks.
                codeAttribute.u2exceptionTableLength =
                    removeEmptyExceptions(codeAttribute.exceptionTable,
                                          codeAttribute.u2exceptionTableLength);

                // Remap the line number table and the local variable tables.
                codeAttribute.attributesAccept(clazz, method, this);
            }

            // Make sure instructions are widened if necessary.
            instructionWriter.visitCodeAttribute(clazz, method, codeAttribute);
        }

        // Update the maximum stack size and local variable frame size.
        if (updateFrameSizes)
        {
            stackSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
            variableSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        }
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
    }


    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        // Remap all local variable table entries.
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }


    /**
     * Checks if it is possible to modifies the given code without having to
     * update any offsets.
     * @param codeAttribute the code to be changed.
     * @return the new code length.
     */
    private boolean canPerformSimpleReplacements(CodeAttribute codeAttribute)
    {
        if (!simple)
        {
            return false;
        }

        byte[] code       = codeAttribute.code;
        int    codeLength = codeAttribute.u4codeLength;

        // Go over all replacement instructions.
        for (int offset = 0; offset < codeLength; offset++)
        {
            // Check if the replacement instruction, if any, has a different
            // length than the original instruction.
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null &&
                replacementInstruction.length(offset) !=
                    InstructionFactory.create(code, offset).length(offset))
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Modifies the given code without updating any offsets.
     * @param codeAttribute the code to be changed.
     */
    private void performSimpleReplacements(CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;

        // Go over all replacement instructions.
        for (int offset = 0; offset < codeLength; offset++)
        {
            // Overwrite the original instruction with the replacement
            // instruction if any.
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null)
            {
                replacementInstruction.write(codeAttribute, offset);

                if (DEBUG)
                {
                    System.out.println("  Replaced "+replacementInstruction.toString(newOffset));
                }
            }
        }
    }


    /**
     * Modifies the given code based on the previously specified changes.
     * @param clazz         the class file of the code to be changed.
     * @param method        the method of the code to be changed.
     * @param codeAttribute the code to be changed.
     * @return the new code length.
     */
    private int updateInstructions(Clazz         clazz,
                                   Method        method,
                                   CodeAttribute codeAttribute)
    {
        byte[] oldCode   = codeAttribute.code;
        int    oldLength = codeAttribute.u4codeLength;

        // Make sure there is a sufficiently large instruction offset map.
        if (instructionOffsetMap.length < oldLength + 1)
        {
            instructionOffsetMap = new int[oldLength + 1];
        }

        // Fill out the instruction offset map.
        int newLength = mapInstructions(oldCode,
                                        oldLength);

        // Create a new code array if necessary.
        if (lengthIncreased)
        {
            codeAttribute.code = new byte[newLength];
        }

        // Prepare for possible widening of instructions.
        instructionWriter.reset(newLength);

        // Move the instructions into the new code array.
        moveInstructions(clazz,
                         method,
                         codeAttribute,
                         oldCode,
                         oldLength);

        // We can return the new length.
        return newLength;
    }


    /**
     * Fills out the instruction offset map for the given code block.
     * @param oldCode   the instructions to be moved.
     * @param oldLength the code length.
     * @return the new code length.
     */
    private int mapInstructions(byte[] oldCode, int oldLength)
    {
        // Start mapping instructions at the beginning.
        newOffset       = 0;
        lengthIncreased = false;

        int oldOffset = 0;
        do
        {
            // Get the next instruction.
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);

            // Compute the mapping of the instruction.
            mapInstruction(oldOffset, instruction);

            oldOffset += instruction.length(oldOffset);

            if (newOffset > oldOffset)
            {
                lengthIncreased = true;
            }
        }
        while (oldOffset < oldLength);

        // Also add an entry for the first offset after the code.
        instructionOffsetMap[oldOffset] = newOffset;

        return newOffset;
    }


    /**
     * Fills out the instruction offset map for the given instruction.
     * @param oldOffset   the instruction's old offset.
     * @param instruction the instruction to be moved.
     */
    private void mapInstruction(int         oldOffset,
                                Instruction instruction)
    {
        instructionOffsetMap[oldOffset] = newOffset;

        // Account for the pre-inserted instruction, if any.
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            newOffset += preInstruction.length(newOffset);
        }

        // Account for the replacement instruction, or for the current
        // instruction, if it shouldn't be  deleted.
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            newOffset += replacementInstruction.length(newOffset);
        }
        else if (!deleted[oldOffset])
        {
            // Note that the instruction's length may change at its new offset,
            // e.g. if it is a switch instruction.
            newOffset += instruction.length(newOffset);
        }

        // Account for the post-inserted instruction, if any.
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            newOffset += postInstruction.length(newOffset);
        }
    }


    /**
     * Moves the given code block to the new offsets.
     * @param clazz         the class file of the code to be changed.
     * @param method        the method of the code to be changed.
     * @param codeAttribute the code to be changed.
     * @param oldCode       the original code to be moved.
     * @param oldLength     the original code length.
     */
    private void moveInstructions(Clazz         clazz,
                                  Method        method,
                                  CodeAttribute codeAttribute,
                                  byte[]        oldCode,
                                  int           oldLength)
    {
        // Start writing instructions at the beginning.
        newOffset = 0;

        int oldOffset = 0;
        do
        {
            // Get the next instruction.
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);

            // Move the instruction to its new offset.
            moveInstruction(clazz,
                            method,
                            codeAttribute,
                            oldOffset,
                            instruction);

            oldOffset += instruction.length(oldOffset);
        }
        while (oldOffset < oldLength);
    }


    /**
     * Moves the given instruction to its new offset.
     * @param clazz         the class file of the code to be changed.
     * @param method        the method of the code to be changed.
     * @param codeAttribute the code to be changed.
     * @param oldOffset     the original instruction offset.
     * @param instruction   the original instruction.
     */
    private void moveInstruction(Clazz         clazz,
                                 Method        method,
                                 CodeAttribute codeAttribute,
                                 int           oldOffset,
                                 Instruction   instruction)
    {
        // Remap and insert the pre-inserted instruction, if any.
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Pre-inserted  "+preInstruction.toString(newOffset));
            }

            // Remap the instruction.
            preInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }

        // Remap and insert the replacement instruction, or the current
        // instruction, if it shouldn't be deleted.
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Replaced      "+replacementInstruction.toString(newOffset));
            }
            // Remap the instruction.
            replacementInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
        else if (!deleted[oldOffset])
        {
            if (DEBUG)
            {
                System.out.println("  Copied        "+instruction.toString(newOffset));
            }

            // Remap the instruction.
            instruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }

        // Remap and insert the post-inserted instruction, if any.
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Post-inserted "+postInstruction.toString(newOffset));
            }

            // Remap the instruction.
            postInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        // Write out the instruction.
        instructionWriter.visitSimpleInstruction(clazz,
                                                 method,
                                                 codeAttribute,
                                                 newOffset,
                                                 simpleInstruction);

        newOffset += simpleInstruction.length(newOffset);
    }


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        // Write out the instruction.
        instructionWriter.visitConstantInstruction(clazz,
                                                   method,
                                                   codeAttribute,
                                                   newOffset,
                                                   constantInstruction);

        newOffset += constantInstruction.length(newOffset);
    }


    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        // Write out the instruction.
        instructionWriter.visitVariableInstruction(clazz,
                                                   method,
                                                   codeAttribute,
                                                   newOffset,
                                                   variableInstruction);

        newOffset += variableInstruction.length(newOffset);
    }


    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        // Adjust the branch offset.
        branchInstruction.branchOffset = remapBranchOffset(offset,
                                                           branchInstruction.branchOffset);

        // Write out the instruction.
        instructionWriter.visitBranchInstruction(clazz,
                                                 method,
                                                 codeAttribute,
                                                 newOffset,
                                                 branchInstruction);

        newOffset += branchInstruction.length(newOffset);
    }


    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Adjust the default jump offset.
        tableSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                 tableSwitchInstruction.defaultOffset);

        // Adjust the jump offsets.
        remapJumpOffsets(offset,
                         tableSwitchInstruction.jumpOffsets);

        // Write out the instruction.
        instructionWriter.visitTableSwitchInstruction(clazz,
                                                      method,
                                                      codeAttribute,
                                                      newOffset,
                                                      tableSwitchInstruction);

        newOffset += tableSwitchInstruction.length(newOffset);
    }


    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Adjust the default jump offset.
        lookUpSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                  lookUpSwitchInstruction.defaultOffset);

        // Adjust the jump offsets.
        remapJumpOffsets(offset,
                         lookUpSwitchInstruction.jumpOffsets);

        // Write out the instruction.
        instructionWriter.visitLookUpSwitchInstruction(clazz,
                                                       method,
                                                       codeAttribute,
                                                       newOffset,
                                                       lookUpSwitchInstruction);

        newOffset += lookUpSwitchInstruction.length(newOffset);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        // Remap the code offsets. Note that the instruction offset map also has
        // an entry for the first offset after the code, for u2endPC.
        exceptionInfo.u2startPC   = remapInstructionOffset(exceptionInfo.u2startPC);
        exceptionInfo.u2endPC     = remapInstructionOffset(exceptionInfo.u2endPC);
        exceptionInfo.u2handlerPC = remapInstructionOffset(exceptionInfo.u2handlerPC);
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
        int newStartPC = remapInstructionOffset(localVariableInfo.u2startPC);
        int newEndPC   = remapInstructionOffset(localVariableInfo.u2startPC +
                                                localVariableInfo.u2length);

        localVariableInfo.u2length  = newEndPC - newStartPC;
        localVariableInfo.u2startPC = newStartPC;
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Remap the code offset and length.
        int newStartPC = remapInstructionOffset(localVariableTypeInfo.u2startPC);
        int newEndPC   = remapInstructionOffset(localVariableTypeInfo.u2startPC +
                                                localVariableTypeInfo.u2length);

        localVariableTypeInfo.u2length  = newEndPC - newStartPC;
        localVariableTypeInfo.u2startPC = newStartPC;
    }


    // Small utility methods.

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
     * Computes the new branch offset for the instruction at the given offset
     * with the given branch offset.
     */
    private int remapBranchOffset(int offset, int branchOffset)
    {
        return remapInstructionOffset(offset + branchOffset) - newOffset;
    }


    /**
     * Computes the new instruction offset for the instruction at the given offset.
     */
    private int remapInstructionOffset(int offset)
    {
        if (offset < 0 ||
            offset > codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+offset+"] in code with length ["+codeLength+"]");
        }

        return instructionOffsetMap[offset];
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

        return newIndex;
    }


    private class CompositeInstruction
    extends       Instruction
    {
        private Instruction[] instructions;


        private CompositeInstruction(Instruction[] instructions)
        {
            this.instructions = instructions;
        }


        // Implementations for Instruction.

        public Instruction shrink()
        {
            for (int index = 0; index < instructions.length; index++)
            {
                instructions[index] = instructions[index].shrink();
            }

            return this;
        }


        public void write(byte[] code, int offset)
        {
            for (int index = 0; index < instructions.length; index++)
            {
                Instruction instruction = instructions[index];

                instruction.write(code, offset);

                offset += instruction.length(offset);
            }
        }


        protected void readInfo(byte[] code, int offset)
        {
            throw new UnsupportedOperationException("Can't read composite instruction");
        }


        protected void writeInfo(byte[] code, int offset)
        {
            throw new UnsupportedOperationException("Can't write composite instruction");
        }


        public int length(int offset)
        {
            int newOffset = offset;

            for (int index = 0; index < instructions.length; index++)
            {
                newOffset += instructions[index].length(newOffset);
            }

            return newOffset - offset;
        }


        public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
        {
            if (instructionVisitor != CodeAttributeEditor.this)
            {
                throw new UnsupportedOperationException("Unexpected visitor ["+instructionVisitor+"]");
            }

            for (int index = 0; index < instructions.length; index++)
            {
                Instruction instruction = instructions[index];

                instruction.accept(clazz, method, codeAttribute, offset, CodeAttributeEditor.this);

                offset += instruction.length(offset);
            }
        }


        // Implementations for Object.

        public String toString()
        {
            StringBuffer stringBuffer = new StringBuffer();

            for (int index = 0; index < instructions.length; index++)
            {
                stringBuffer.append(instructions[index].toString()).append("; ");
            }

            return stringBuffer.toString();
        }
    }
}
