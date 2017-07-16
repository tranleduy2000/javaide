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
package proguard.evaluation;

import proguard.classfile.ClassConstants;
import proguard.classfile.Clazz;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.DoubleConstant;
import proguard.classfile.constant.FloatConstant;
import proguard.classfile.constant.IntegerConstant;
import proguard.classfile.constant.LongConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.evaluation.value.Value;
import proguard.evaluation.value.ValueFactory;

/**
 * This class creates Value instance that correspond to specified constant pool
 * entries.
 *
 * @author Eric Lafortune
 */
public class ConstantValueFactory
extends SimplifiedVisitor
implements ConstantVisitor
{
    protected final ValueFactory valueFactory;

    // Field acting as a parameter for the ConstantVisitor methods.
    protected Value value;


    public ConstantValueFactory(ValueFactory valueFactory)
    {
        this.valueFactory = valueFactory;
    }


    /**
     * Returns the Value of the constant pool element at the given index.
     */
    public Value constantValue(Clazz clazz,
                               int   constantIndex)
    {
        // Visit the constant pool entry to get its return value.
        clazz.constantPoolEntryAccept(constantIndex, this);

        return value;
    }


    // Implementations for ConstantVisitor.

    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        value = valueFactory.createIntegerValue(integerConstant.getValue());
    }

    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        value = valueFactory.createLongValue(longConstant.getValue());
    }

    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        value = valueFactory.createFloatValue(floatConstant.getValue());
    }

    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        value = valueFactory.createDoubleValue(doubleConstant.getValue());
    }

    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        value = valueFactory.createReferenceValue(ClassConstants.INTERNAL_NAME_JAVA_LANG_STRING,
                                                    stringConstant.javaLangStringClass,
                                                    false);
    }

    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        value = valueFactory.createReferenceValue(classConstant.getName(clazz),
                                                  classConstant.referencedClass,
                                                  false);
    }
}