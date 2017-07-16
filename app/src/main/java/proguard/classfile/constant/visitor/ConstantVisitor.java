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
package proguard.classfile.constant.visitor;

import proguard.classfile.Clazz;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.DoubleConstant;
import proguard.classfile.constant.FieldrefConstant;
import proguard.classfile.constant.FloatConstant;
import proguard.classfile.constant.IntegerConstant;
import proguard.classfile.constant.InterfaceMethodrefConstant;
import proguard.classfile.constant.LongConstant;
import proguard.classfile.constant.MethodrefConstant;
import proguard.classfile.constant.NameAndTypeConstant;
import proguard.classfile.constant.StringConstant;
import proguard.classfile.constant.Utf8Constant;


/**
 * This interface specifies the methods for a visitor of <code>Constant</code>
 * objects.
 *
 * @author Eric Lafortune
 */
public interface ConstantVisitor
{
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant);
    public void visitLongConstant(Clazz clazz, LongConstant longConstant);
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant);
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant);
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant);
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant);
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant);
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant);
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant);
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant);
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant);
}
