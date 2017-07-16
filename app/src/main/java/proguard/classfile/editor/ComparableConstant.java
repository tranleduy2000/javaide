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
import proguard.classfile.util.SimplifiedVisitor;


/**
 * This class is a <code>Comparable</code> wrapper of <code>Constant</code>
 * objects. It can store an index, in order to identify the constant   pool
 * entry after it has been sorted. The comparison is primarily based   on the
 * types of the constant pool entries, and secondarily on the contents of
 * the constant pool entries.
 *
 * @author Eric Lafortune
 */
class      ComparableConstant
extends SimplifiedVisitor
implements Comparable, ConstantVisitor
{
    private static final int[] PRIORITIES = new int[13];
    static
    {
        PRIORITIES[ClassConstants.CONSTANT_Integer]            = 0; // Possibly byte index (ldc).
        PRIORITIES[ClassConstants.CONSTANT_Float]              = 1;
        PRIORITIES[ClassConstants.CONSTANT_String]             = 2;
        PRIORITIES[ClassConstants.CONSTANT_Class]              = 3;
        PRIORITIES[ClassConstants.CONSTANT_Long]               = 4; // Always wide index (ldc2_w).
        PRIORITIES[ClassConstants.CONSTANT_Double]             = 5;
        PRIORITIES[ClassConstants.CONSTANT_Fieldref]           = 6; // Always wide index.
        PRIORITIES[ClassConstants.CONSTANT_Methodref]          = 7;
        PRIORITIES[ClassConstants.CONSTANT_InterfaceMethodref] = 8;
        PRIORITIES[ClassConstants.CONSTANT_NameAndType]        = 9;
        PRIORITIES[ClassConstants.CONSTANT_Utf8]               = 10;
    }

    private final Clazz    clazz;
    private final int      thisIndex;
    private final Constant thisConstant;

    private Constant otherConstant;
    private int      result;


    public ComparableConstant(Clazz clazz, int index, Constant constant)
    {
        this.clazz        = clazz;
        this.thisIndex    = index;
        this.thisConstant = constant;
    }


    public int getIndex()
    {
        return thisIndex;
    }


    public Constant getConstant()
    {
        return thisConstant;
    }


    // Implementations for Comparable.

    public int compareTo(Object other)
    {
        ComparableConstant otherComparableConstant = (ComparableConstant)other;

        otherConstant = otherComparableConstant.thisConstant;

        // Compare based on the original indices, if the actual constant pool
        // entries are the same.
        if (thisConstant == otherConstant)
        {
            int otherIndex = otherComparableConstant.thisIndex;

            return thisIndex <  otherIndex ? -1 :
                   thisIndex == otherIndex ?  0 :
                                              1;
        }

        // Compare based on the tags, if they are different.
        int thisTag  = thisConstant.getTag();
        int otherTag = otherConstant.getTag();

        if (thisTag != otherTag)
        {
            return PRIORITIES[thisTag] < PRIORITIES[otherTag] ? -1 : 1;
        }

        // Otherwise compare based on the contents of the Constant objects.
        thisConstant.accept(clazz, this);

        return result;
    }


    // Implementations for ConstantVisitor.

    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        // In JDK 1.4, we can use Integer.compare(a,b).
        result = new Integer(integerConstant.getValue()).compareTo(new Integer(((IntegerConstant)otherConstant).getValue()));
    }

    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        // In JDK 1.4, we can use Long.compare(a,b).
        result = new Long(longConstant.getValue()).compareTo(new Long(((LongConstant)otherConstant).getValue()));
    }

    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        // In JDK 1.4, we can use Float.compare(a,b).
        result = new Float(floatConstant.getValue()).compareTo(new Float(((FloatConstant)otherConstant).getValue()));
    }

    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        // In JDK 1.4, we can use Double.compare(a,b).
        result = new Double(doubleConstant.getValue()).compareTo(new Double(((DoubleConstant)otherConstant).getValue()));
    }

    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        result = stringConstant.getString(clazz).compareTo(((StringConstant)otherConstant).getString(clazz));
    }

    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        result = utf8Constant.getString().compareTo(((Utf8Constant)otherConstant).getString());
    }

    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        RefConstant otherRefConstant = (RefConstant)otherConstant;
        result = (refConstant.getClassName(clazz) + ' ' +
                  refConstant.getName(clazz)      + ' ' +
                  refConstant.getType(clazz))
                 .compareTo
                 (otherRefConstant.getClassName(clazz) + ' ' +
                  otherRefConstant.getName(clazz)      + ' ' +
                  otherRefConstant.getType(clazz));
    }

    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        result = classConstant.getName(clazz).compareTo(((ClassConstant)otherConstant).getName(clazz));
    }

    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        NameAndTypeConstant otherNameAndTypeConstant = (NameAndTypeConstant)otherConstant;
        result = (nameAndTypeConstant.getName(clazz) + ' ' +
                  nameAndTypeConstant.getType(clazz))
                 .compareTo
                 (otherNameAndTypeConstant.getName(clazz) + ' ' +
                  otherNameAndTypeConstant.getType(clazz));
    }


    // Implementations for Object.

    public boolean equals(Object other)
    {
        return other != null &&
               this.getClass().equals(other.getClass()) &&
               this.getConstant().getClass().equals(((ComparableConstant)other).getConstant().getClass()) &&
               this.compareTo(other) == 0;
    }


    public int hashCode()
    {
        return this.getClass().hashCode();
    }
}
