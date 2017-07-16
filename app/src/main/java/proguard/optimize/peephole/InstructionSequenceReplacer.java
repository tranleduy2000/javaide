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

import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.BranchInstruction;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.InstructionFactory;
import proguard.classfile.instruction.LookUpSwitchInstruction;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.TableSwitchInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.util.SimplifiedVisitor;

/**
 * This InstructionVisitor replaces a given pattern instruction sequence by
 * another given replacement instruction sequence. The arguments of the
 * instruction sequences can be wildcards that are matched and replaced.
 *
 * @see InstructionSequenceMatcher
 * @author Eric Lafortune
 */
public class InstructionSequenceReplacer
extends SimplifiedVisitor
implements InstructionVisitor,
        ConstantVisitor
{
    private static final boolean DEBUG = false;


    private final InstructionSequenceMatcher instructionSequenceMatcher;
    private final Instruction[]              replacementInstructions;
    private final BranchTargetFinder         branchTargetFinder;
    private final CodeAttributeEditor codeAttributeEditor;
    private final InstructionVisitor extraInstructionVisitor;

    private final MyReplacementInstructionFactory replacementInstructionFactory = new MyReplacementInstructionFactory();


    /**
     * Creates a new InstructionSequenceReplacer.
     * @param patternConstants        any constants referenced by the pattern
     *                                instruction.
     * @param patternInstructions     the pattern instruction sequence.
     * @param replacementInstructions the replacement instruction sequence.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttributeEditor     a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public InstructionSequenceReplacer(Constant[]          patternConstants,
                                       Instruction[]       patternInstructions,
                                       Instruction[]       replacementInstructions,
                                       BranchTargetFinder  branchTargetFinder,
                                       CodeAttributeEditor codeAttributeEditor)
    {
        this(patternConstants,
             patternInstructions,
             replacementInstructions,
             branchTargetFinder,
             codeAttributeEditor,
             null);
    }


    /**
     * Creates a new InstructionSequenceReplacer.
     * @param patternConstants        any constants referenced by the pattern
     *                                instruction.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttributeEditor     a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all deleted
     *                                load instructions.
     */
    public InstructionSequenceReplacer(Constant[]          patternConstants,
                                       Instruction[]       patternInstructions,
                                       Instruction[]       replacementInstructions,
                                       BranchTargetFinder  branchTargetFinder,
                                       CodeAttributeEditor codeAttributeEditor,
                                       InstructionVisitor extraInstructionVisitor)
    {
        this.instructionSequenceMatcher = new InstructionSequenceMatcher(patternConstants, patternInstructions);
        this.replacementInstructions    = replacementInstructions;
        this.branchTargetFinder         = branchTargetFinder;
        this.codeAttributeEditor        = codeAttributeEditor;
        this.extraInstructionVisitor    = extraInstructionVisitor;
    }


    // Implementations for InstructionVisitor.

    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        // Reset the instruction sequence matcher if the instruction is a branch
        // target or if it has already been modified.
        if (branchTargetFinder.isTarget(offset) ||
            codeAttributeEditor.isModified(offset))
        {
            instructionSequenceMatcher.reset();
        }

        // Try to match the instruction.
        instruction.accept(clazz, method, codeAttribute, offset, instructionSequenceMatcher);

        // Did the instruction sequence match and is it still unmodified?
        if (instructionSequenceMatcher.isMatching() &&
            matchedInstructionsUnmodified())
        {
            if (DEBUG)
            {
                System.out.println("InstructionSequenceReplacer: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
                System.out.println("  Matched:");
                for (int index = 0; index < instructionSequenceMatcher.instructionCount(); index++)
                {
                    int matchedOffset = instructionSequenceMatcher.matchedInstructionOffset(index);
                    System.out.println("    "+ InstructionFactory.create(codeAttribute.code, matchedOffset).toString(matchedOffset));
                }
                System.out.println("  Replacement:");
                for (int index = 0; index < replacementInstructions.length; index++)
                {
                    int matchedOffset = instructionSequenceMatcher.matchedInstructionOffset(index);
                    System.out.println("    "+replacementInstructionFactory.create(index).shrink().toString(matchedOffset));
                }
            }

            // Replace the instruction sequence.
            for (int index = 0; index < replacementInstructions.length; index++)
            {
                codeAttributeEditor.replaceInstruction(instructionSequenceMatcher.matchedInstructionOffset(index),
                                                       replacementInstructionFactory.create(index).shrink());
            }

            // Delete any remaining instructions in the from sequence.
            for (int index = replacementInstructions.length; index < instructionSequenceMatcher.instructionCount(); index++)
            {
                codeAttributeEditor.deleteInstruction(instructionSequenceMatcher.matchedInstructionOffset(index));
            }

            // Visit the instruction, if required.
            if (extraInstructionVisitor != null)
            {
                instruction.accept(clazz,
                                   method,
                                   codeAttribute,
                                   offset,
                                   extraInstructionVisitor);
            }
        }
    }


    // Small utility methods.

    /**
     * Returns whether the matched pattern instructions haven't been modified
     * before.
     */
    private boolean matchedInstructionsUnmodified()
    {
        for (int index = 0; index < instructionSequenceMatcher.instructionCount(); index++)
        {
            if (codeAttributeEditor.isModified(instructionSequenceMatcher.matchedInstructionOffset(index)))
            {
                return false;
            }
        }

        return true;
    }


    /**
     * This class creates replacement instructions for matched sequences, with
     * any matched arguments filled out.
     */
    private class MyReplacementInstructionFactory
    implements InstructionVisitor
    {
        private Instruction replacementInstruction;


        /**
         * Creates the replacement instruction for the given index in the
         * instruction sequence.
         */
        public Instruction create(int index)
        {
            // Create the instruction.
            replacementInstructions[index].accept(null,
                                                  null,
                                                  null,
                                                  instructionSequenceMatcher.matchedInstructionOffset(index),
                                                  this);

            // Return it.
            return replacementInstruction.shrink();
        }


        // Implementations for InstructionVisitor.

        public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
        {
            replacementInstruction =
                new SimpleInstruction(simpleInstruction.opcode,
                                      instructionSequenceMatcher.matchedArgument(simpleInstruction.constant));
        }


        public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
        {
            replacementInstruction =
                new VariableInstruction(variableInstruction.opcode,
                                        instructionSequenceMatcher.matchedArgument(variableInstruction.variableIndex),
                                        instructionSequenceMatcher.matchedArgument(variableInstruction.constant));
        }


        public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
        {
            replacementInstruction =
                new ConstantInstruction(constantInstruction.opcode,
                                        instructionSequenceMatcher.matchedConstantIndex(constantInstruction.constantIndex),
                                        instructionSequenceMatcher.matchedArgument(constantInstruction.constant));
        }


        public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
        {
            replacementInstruction =
                new BranchInstruction(branchInstruction.opcode,
                                      instructionSequenceMatcher.matchedBranchOffset(offset, branchInstruction.branchOffset));
        }


        public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
        {
            replacementInstruction =
                new TableSwitchInstruction(tableSwitchInstruction.opcode,
                                           instructionSequenceMatcher.matchedBranchOffset(offset, tableSwitchInstruction.defaultOffset),
                                           instructionSequenceMatcher.matchedArgument(tableSwitchInstruction.lowCase),
                                           instructionSequenceMatcher.matchedArgument(tableSwitchInstruction.highCase),
                                           instructionSequenceMatcher.matchedJumpOffsets(offset, tableSwitchInstruction.jumpOffsets));

        }


        public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
        {
            replacementInstruction =
                new LookUpSwitchInstruction(lookUpSwitchInstruction.opcode,
                                            instructionSequenceMatcher.matchedBranchOffset(offset, lookUpSwitchInstruction.defaultOffset),
                                            instructionSequenceMatcher.matchedArguments(lookUpSwitchInstruction.cases),
                                            instructionSequenceMatcher.matchedJumpOffsets(offset, lookUpSwitchInstruction.jumpOffsets));
        }
    }
}
