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
package proguard.classfile.util;

import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.attribute.CodeAttribute;
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
import proguard.classfile.instruction.BranchInstruction;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.InstructionFactory;
import proguard.classfile.instruction.LookUpSwitchInstruction;
import proguard.classfile.instruction.SimpleInstruction;
import proguard.classfile.instruction.TableSwitchInstruction;
import proguard.classfile.instruction.VariableInstruction;
import proguard.classfile.instruction.visitor.InstructionVisitor;

/**
 * This InstructionVisitor checks whether a given pattern instruction sequence
 * occurs in the instructions that are visited. The arguments of the
 * instruction sequence can be wildcards that are matched.
 *
 * @author Eric Lafortune
 */
public class InstructionSequenceMatcher
extends SimplifiedVisitor
implements InstructionVisitor,
        ConstantVisitor
{
    /*
    public  static       boolean DEBUG      = false;
    public  static       boolean DEBUG_MORE = false;
    /*/
    private static final boolean DEBUG      = false;
    private static final boolean DEBUG_MORE = false;
    //*/

    public static final int X = 0x40000000;
    public static final int Y = 0x40000001;
    public static final int Z = 0x40000002;

    public static final int A = 0x40000003;
    public static final int B = 0x40000004;
    public static final int C = 0x40000005;
    public static final int D = 0x40000006;


    private final Constant[]    patternConstants;
    private final Instruction[] patternInstructions;

    private boolean     matching;
    private boolean     matchingAnyWildCards;
    private int         patternInstructionIndex;
    private final int[] matchedInstructionOffsets;
    private int         matchedArgumentFlags;
    private final int[] matchedArguments = new int[7];
    private long        matchedConstantFlags;
    private final int[] matchedConstantIndices;

    // Fields acting as a parameter and a return value for visitor methods.
    private Constant patternConstant;
    private boolean  matchingConstant;


    /**
     * Creates a new InstructionSequenceMatcher.
     * @param patternConstants        any constants referenced by the pattern
     *                                instruction.
     * @param patternInstructions     the pattern instruction sequence.
     */
    public InstructionSequenceMatcher(Constant[]    patternConstants,
                                      Instruction[] patternInstructions)
    {
        this.patternConstants    = patternConstants;
        this.patternInstructions = patternInstructions;

        matchedInstructionOffsets = new int[patternInstructions.length];
        matchedConstantIndices    = new int[patternConstants.length];
    }


    /**
     * Starts matching from the first instruction again next time.
     */
    public void reset()
    {
        patternInstructionIndex = 0;
        matchedArgumentFlags    = 0;
        matchedConstantFlags    = 0L;
    }


    public boolean isMatching()
    {
        return matching;
    }


    public boolean isMatchingAnyWildcards()
    {
        return matchingAnyWildCards;
    }


    public int instructionCount()
    {
        return patternInstructions.length;
    }


    public int matchedInstructionOffset(int index)
    {
        return matchedInstructionOffsets[index];
    }


    public int matchedArgument(int argument)
    {
        int argumentIndex = argument - X;
        return argumentIndex < 0 ?
            argument :
            matchedArguments[argumentIndex];
    }


    public int[] matchedArguments(int[] arguments)
    {
        int[] matchedArguments = new int[arguments.length];

        for (int index = 0; index < arguments.length; index++)
        {
            matchedArguments[index] = matchedArgument(arguments[index]);
        }

        return matchedArguments;
    }


    public int matchedConstantIndex(int constantIndex)
    {
        int argumentIndex = constantIndex - X;
        return argumentIndex < 0 ?
            matchedConstantIndices[constantIndex] :
            matchedArguments[argumentIndex];
    }


    public int matchedBranchOffset(int offset, int branchOffset)
    {
        int argumentIndex = branchOffset - X;
        return argumentIndex < 0 ?
            branchOffset :
            matchedArguments[argumentIndex] - offset;
    }


    public int[] matchedJumpOffsets(int offset, int[] jumpOffsets)
    {
        int[] matchedJumpOffsets = new int[jumpOffsets.length];

        for (int index = 0; index < jumpOffsets.length; index++)
        {
            matchedJumpOffsets[index] = matchedBranchOffset(offset,
                                                            jumpOffsets[index]);
        }

        return matchedJumpOffsets;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the sequence.
        boolean condition =
            matchingOpcodes(simpleInstruction, patternInstruction) &&
            matchingArguments(simpleInstruction.constant,
                              ((SimpleInstruction)patternInstruction).constant);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   simpleInstruction);
    }


    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the sequence.
        boolean condition =
            matchingOpcodes(variableInstruction, patternInstruction) &&
            matchingArguments(variableInstruction.variableIndex,
                              ((VariableInstruction)patternInstruction).variableIndex) &&
            matchingArguments(variableInstruction.constant,
                              ((VariableInstruction)patternInstruction).constant);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   variableInstruction);
    }


    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the sequence.
        boolean condition =
            matchingOpcodes(constantInstruction, patternInstruction) &&
            matchingConstantIndices(clazz,
                                    constantInstruction.constantIndex,
                                    ((ConstantInstruction)patternInstruction).constantIndex) &&
            matchingArguments(constantInstruction.constant,
                              ((ConstantInstruction)patternInstruction).constant);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   constantInstruction);
    }


    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the from
        // sequence.
        boolean condition =
            matchingOpcodes(branchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  branchInstruction.branchOffset,
                                  ((BranchInstruction)patternInstruction).branchOffset);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   branchInstruction);
    }


    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the sequence.
        boolean condition =
            matchingOpcodes(tableSwitchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  tableSwitchInstruction.defaultOffset,
                                  ((TableSwitchInstruction)patternInstruction).defaultOffset) &&
            matchingArguments(tableSwitchInstruction.lowCase,
                              ((TableSwitchInstruction)patternInstruction).lowCase)  &&
            matchingArguments(tableSwitchInstruction.highCase,
                              ((TableSwitchInstruction)patternInstruction).highCase) &&
            matchingJumpOffsets(offset,
                                tableSwitchInstruction.jumpOffsets,
                                ((TableSwitchInstruction)patternInstruction).jumpOffsets);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   tableSwitchInstruction);
    }


    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];

        // Check if the instruction matches the next instruction in the sequence.
        boolean condition =
            matchingOpcodes(lookUpSwitchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  lookUpSwitchInstruction.defaultOffset,
                                  ((LookUpSwitchInstruction)patternInstruction).defaultOffset) &&
            matchingArguments(lookUpSwitchInstruction.cases,
                              ((LookUpSwitchInstruction)patternInstruction).cases) &&
            matchingJumpOffsets(offset,
                                lookUpSwitchInstruction.jumpOffsets,
                                ((LookUpSwitchInstruction)patternInstruction).jumpOffsets);

        // Check if the instruction sequence is matching now.
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   lookUpSwitchInstruction);
    }


    // Implementations for ConstantVisitor.

    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        IntegerConstant integerPatternConstant = (IntegerConstant)patternConstant;

        // Compare the integer values.
        matchingConstant = integerConstant.getValue() ==
                           integerPatternConstant.getValue();
    }


    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        LongConstant longPatternConstant = (LongConstant)patternConstant;

        // Compare the long values.
        matchingConstant = longConstant.getValue() ==
                           longPatternConstant.getValue();
    }


    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        FloatConstant floatPatternConstant = (FloatConstant)patternConstant;

        // Compare the float values.
        matchingConstant = floatConstant.getValue() ==
                           floatPatternConstant.getValue();
    }


    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        DoubleConstant doublePatternConstant = (DoubleConstant)patternConstant;

        // Compare the double values.
        matchingConstant = doubleConstant.getValue() ==
                           doublePatternConstant.getValue();
    }


    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        StringConstant stringPatternConstant = (StringConstant)patternConstant;

        // Check the UTF-8 constant.
        matchingConstant =
            matchingConstantIndices(clazz,
                                    stringConstant.u2stringIndex,
                                    stringPatternConstant.u2stringIndex);
    }


    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        Utf8Constant utf8PatternConstant = (Utf8Constant)patternConstant;

        // Compare the actual strings.
        matchingConstant = utf8Constant.getString().equals(
                           utf8PatternConstant.getString());
    }


    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        RefConstant refPatternConstant = (RefConstant)patternConstant;

        // Check the class and the name and type.
        matchingConstant =
            matchingConstantIndices(clazz,
                                    refConstant.getClassIndex(),
                                    refPatternConstant.getClassIndex()) &&
            matchingConstantIndices(clazz,
                                    refConstant.getNameAndTypeIndex(),
                                    refPatternConstant.getNameAndTypeIndex());
    }


    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        ClassConstant classPatternConstant = (ClassConstant)patternConstant;

        // Check the class name.
        matchingConstant =
            matchingConstantIndices(clazz,
                                    classConstant.u2nameIndex,
                                    classPatternConstant.u2nameIndex);
    }


    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        NameAndTypeConstant typePatternConstant = (NameAndTypeConstant)patternConstant;

        // Check the name and the descriptor.
        matchingConstant =
            matchingConstantIndices(clazz,
                                    nameAndTypeConstant.u2nameIndex,
                                    typePatternConstant.u2nameIndex) &&
            matchingConstantIndices(clazz,
                                    nameAndTypeConstant.u2descriptorIndex,
                                    typePatternConstant.u2descriptorIndex);
    }


    // Small utility methods.

    private boolean matchingOpcodes(Instruction instruction1,
                                    Instruction instruction2)
    {
        // Check the opcode.
        return instruction1.opcode            == instruction2.opcode ||
               instruction1.canonicalOpcode() == instruction2.opcode;
    }


    private boolean matchingArguments(int argument1,
                                      int argument2)
    {
        int argumentIndex = argument2 - X;
        if (argumentIndex < 0)
        {
            // Check the literal argument.
            return argument1 == argument2;
        }
        else if ((matchedArgumentFlags & (1 << argumentIndex)) == 0)
        {
            // Store a wildcard argument.
            matchedArguments[argumentIndex] = argument1;
            matchedArgumentFlags |= 1 << argumentIndex;

            return true;
        }
        else
        {
            // Check the previously stored wildcard argument.
            return matchedArguments[argumentIndex] == argument1;
        }
    }


    private boolean matchingArguments(int[] arguments1,
                                      int[] arguments2)
    {
        if (arguments1.length != arguments2.length)
        {
            return false;
        }

        for (int index = 0; index < arguments1.length; index++)
        {
            if (!matchingArguments(arguments1[index], arguments2[index]))
            {
                return false;
            }
        }

        return true;
    }


    private boolean matchingConstantIndices(Clazz clazz,
                                            int   constantIndex1,
                                            int   constantIndex2)
    {
        if (constantIndex2 >= X)
        {
            // Check the constant index.
            return matchingArguments(constantIndex1, constantIndex2);
        }
        else if ((matchedConstantFlags & (1L << constantIndex2)) == 0)
        {
            // Check the actual constant.
            matchingConstant = false;
            patternConstant  = patternConstants[constantIndex2];

            if (clazz.getTag(constantIndex1) == patternConstant.getTag())
            {
                clazz.constantPoolEntryAccept(constantIndex1, this);

                if (matchingConstant)
                {
                    // Store the constant index.
                    matchedConstantIndices[constantIndex2] = constantIndex1;
                    matchedConstantFlags |= 1L << constantIndex2;
                }
            }

            return matchingConstant;
        }
        else
        {
            // Check a previously stored constant index.
            return matchedConstantIndices[constantIndex2] == constantIndex1;
        }
    }


    private boolean matchingBranchOffsets(int offset,
                                          int branchOffset1,
                                          int branchOffset2)
    {
        int argumentIndex = branchOffset2 - X;
        if (argumentIndex < 0)
        {
            // Check the literal argument.
            return branchOffset1 == branchOffset2;
        }
        else if ((matchedArgumentFlags & (1 << argumentIndex)) == 0)
        {
            // Store a wildcard argument.
            matchedArguments[argumentIndex] = offset + branchOffset1;
            matchedArgumentFlags |= 1 << argumentIndex;

            return true;
        }
        else
        {
            // Check the previously stored wildcard argument.
            return matchedArguments[argumentIndex] == offset + branchOffset1;
        }
    }


    private boolean matchingJumpOffsets(int   offset,
                                        int[] jumpOffsets1,
                                        int[] jumpOffsets2)
    {
        if (jumpOffsets1.length != jumpOffsets2.length)
        {
            return false;
        }

        for (int index = 0; index < jumpOffsets1.length; index++)
        {
            if (!matchingBranchOffsets(offset,
                                       jumpOffsets1[index],
                                       jumpOffsets2[index]))
            {
                return false;
            }
        }

        return true;
    }


    private void checkMatch(boolean       condition,
                            Clazz         clazz,
                            Method        method,
                            CodeAttribute codeAttribute,
                            int           offset,
                            Instruction   instruction)
    {
        if (DEBUG_MORE)
        {
            System.out.println("InstructionSequenceMatcher: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]: "+patternInstructions[patternInstructionIndex].toString(patternInstructionIndex)+(condition?"\t== ":"\t   ")+instruction.toString(offset));
        }

        // Did the instruction match?
        if (condition)
        {
            // Remember the offset of the matching instruction.
            matchedInstructionOffsets[patternInstructionIndex] = offset;

            // Try to match the next instruction next time.
            patternInstructionIndex++;

            // Did we match all instructions in the sequence?
            matching = patternInstructionIndex == patternInstructions.length;

            // Did we match any wildcards along the way?
            matchingAnyWildCards = matchedArgumentFlags != 0;

            if (matching)
            {
                if (DEBUG)
                {
                    System.out.println("InstructionSequenceMatcher: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
                    for (int index = 0; index < patternInstructionIndex; index++)
                    {
                        System.out.println("    "+ InstructionFactory.create(codeAttribute.code, matchedInstructionOffsets[index]).toString(matchedInstructionOffsets[index]));
                    }
                }

                // Start matching from the first instruction again next time.
                reset();
            }
        }
        else
        {
            // The instruction didn't match.
            matching = false;

            // Is this a failed second instruction?
            boolean retry = patternInstructionIndex == 1;

            // Start matching from the first instruction next time.
            reset();

            // Retry a failed second instruction as a first instruction.
            if (retry)
            {
                instruction.accept(clazz, method, codeAttribute, offset, this);
            }
        }
    }
}
