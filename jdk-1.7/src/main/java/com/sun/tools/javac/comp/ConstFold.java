/*
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import static com.sun.tools.javac.code.TypeTags.BOOLEAN;
import static com.sun.tools.javac.code.TypeTags.BYTE;
import static com.sun.tools.javac.code.TypeTags.CHAR;
import static com.sun.tools.javac.code.TypeTags.DOUBLE;
import static com.sun.tools.javac.code.TypeTags.FLOAT;
import static com.sun.tools.javac.code.TypeTags.INT;
import static com.sun.tools.javac.code.TypeTags.LONG;
import static com.sun.tools.javac.code.TypeTags.SHORT;
import static com.sun.tools.javac.jvm.ByteCodes.bool_and;
import static com.sun.tools.javac.jvm.ByteCodes.bool_not;
import static com.sun.tools.javac.jvm.ByteCodes.bool_or;
import static com.sun.tools.javac.jvm.ByteCodes.dadd;
import static com.sun.tools.javac.jvm.ByteCodes.dcmpg;
import static com.sun.tools.javac.jvm.ByteCodes.dcmpl;
import static com.sun.tools.javac.jvm.ByteCodes.ddiv;
import static com.sun.tools.javac.jvm.ByteCodes.dmod;
import static com.sun.tools.javac.jvm.ByteCodes.dmul;
import static com.sun.tools.javac.jvm.ByteCodes.dneg;
import static com.sun.tools.javac.jvm.ByteCodes.dsub;
import static com.sun.tools.javac.jvm.ByteCodes.fadd;
import static com.sun.tools.javac.jvm.ByteCodes.fcmpg;
import static com.sun.tools.javac.jvm.ByteCodes.fcmpl;
import static com.sun.tools.javac.jvm.ByteCodes.fdiv;
import static com.sun.tools.javac.jvm.ByteCodes.fmod;
import static com.sun.tools.javac.jvm.ByteCodes.fmul;
import static com.sun.tools.javac.jvm.ByteCodes.fneg;
import static com.sun.tools.javac.jvm.ByteCodes.fsub;
import static com.sun.tools.javac.jvm.ByteCodes.iadd;
import static com.sun.tools.javac.jvm.ByteCodes.iand;
import static com.sun.tools.javac.jvm.ByteCodes.idiv;
import static com.sun.tools.javac.jvm.ByteCodes.if_acmpeq;
import static com.sun.tools.javac.jvm.ByteCodes.if_acmpne;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmpeq;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmpge;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmpgt;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmple;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmplt;
import static com.sun.tools.javac.jvm.ByteCodes.if_icmpne;
import static com.sun.tools.javac.jvm.ByteCodes.ifeq;
import static com.sun.tools.javac.jvm.ByteCodes.ifge;
import static com.sun.tools.javac.jvm.ByteCodes.ifgt;
import static com.sun.tools.javac.jvm.ByteCodes.ifle;
import static com.sun.tools.javac.jvm.ByteCodes.iflt;
import static com.sun.tools.javac.jvm.ByteCodes.ifne;
import static com.sun.tools.javac.jvm.ByteCodes.imod;
import static com.sun.tools.javac.jvm.ByteCodes.imul;
import static com.sun.tools.javac.jvm.ByteCodes.ineg;
import static com.sun.tools.javac.jvm.ByteCodes.ior;
import static com.sun.tools.javac.jvm.ByteCodes.ishl;
import static com.sun.tools.javac.jvm.ByteCodes.ishll;
import static com.sun.tools.javac.jvm.ByteCodes.ishr;
import static com.sun.tools.javac.jvm.ByteCodes.ishrl;
import static com.sun.tools.javac.jvm.ByteCodes.isub;
import static com.sun.tools.javac.jvm.ByteCodes.iushr;
import static com.sun.tools.javac.jvm.ByteCodes.iushrl;
import static com.sun.tools.javac.jvm.ByteCodes.ixor;
import static com.sun.tools.javac.jvm.ByteCodes.ladd;
import static com.sun.tools.javac.jvm.ByteCodes.land;
import static com.sun.tools.javac.jvm.ByteCodes.lcmp;
import static com.sun.tools.javac.jvm.ByteCodes.ldiv;
import static com.sun.tools.javac.jvm.ByteCodes.lmod;
import static com.sun.tools.javac.jvm.ByteCodes.lmul;
import static com.sun.tools.javac.jvm.ByteCodes.lneg;
import static com.sun.tools.javac.jvm.ByteCodes.lor;
import static com.sun.tools.javac.jvm.ByteCodes.lshl;
import static com.sun.tools.javac.jvm.ByteCodes.lshll;
import static com.sun.tools.javac.jvm.ByteCodes.lshr;
import static com.sun.tools.javac.jvm.ByteCodes.lshrl;
import static com.sun.tools.javac.jvm.ByteCodes.lsub;
import static com.sun.tools.javac.jvm.ByteCodes.lushr;
import static com.sun.tools.javac.jvm.ByteCodes.lxor;
import static com.sun.tools.javac.jvm.ByteCodes.nop;
import static com.sun.tools.javac.jvm.ByteCodes.string_add;

/** Helper class for constant folding, used by the attribution phase.
 *  This class is marked strictfp as mandated by JLS 15.4.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
strictfp class ConstFold {
    protected static final Context.Key<ConstFold> constFoldKey =
        new Context.Key<ConstFold>();

    private Symtab syms;

    public static ConstFold instance(Context context) {
        ConstFold instance = context.get(constFoldKey);
        if (instance == null)
            instance = new ConstFold(context);
        return instance;
    }

    private ConstFold(Context context) {
        context.put(constFoldKey, this);

        syms = Symtab.instance(context);
    }

    static Integer minusOne = -1;
    static Integer zero     = 0;
    static Integer one      = 1;

   /** Convert boolean to integer (true = 1, false = 0).
    */
    private static Integer b2i(boolean b) {
        return b ? one : zero;
    }
    private static int intValue(Object x) { return ((Number)x).intValue(); }
    private static long longValue(Object x) { return ((Number)x).longValue(); }
    private static float floatValue(Object x) { return ((Number)x).floatValue(); }
    private static double doubleValue(Object x) { return ((Number)x).doubleValue(); }

    /** Fold binary or unary operation, returning constant type reflecting the
     *  operations result. Return null if fold failed due to an
     *  arithmetic exception.
     *  @param opcode    The operation's opcode instruction (usually a byte code),
     *                   as entered by class Symtab.
     *  @param argtypes  The operation's argument types (a list of length 1 or 2).
     *                   Argument types are assumed to have non-null constValue's.
     */
    Type fold(int opcode, List<Type> argtypes) {
        int argCount = argtypes.length();
        if (argCount == 1)
            return fold1(opcode, argtypes.head);
        else if (argCount == 2)
            return fold2(opcode, argtypes.head, argtypes.tail.head);
        else
            throw new AssertionError();
    }

    /** Fold unary operation.
     *  @param opcode    The operation's opcode instruction (usually a byte code),
     *                   as entered by class Symtab.
     *                   opcode's ifeq to ifge are for postprocessing
     *                   xcmp; ifxx pairs of instructions.
     *  @param operand   The operation's operand type.
     *                   Argument types are assumed to have non-null constValue's.
     */
    Type fold1(int opcode, Type operand) {
        try {
            Object od = operand.constValue();
            switch (opcode) {
            case nop:
                return operand;
            case ineg: // unary -
                return syms.intType.constType(-intValue(od));
            case ixor: // ~
                return syms.intType.constType(~intValue(od));
            case bool_not: // !
                return syms.booleanType.constType(b2i(intValue(od) == 0));
            case ifeq:
                return syms.booleanType.constType(b2i(intValue(od) == 0));
            case ifne:
                return syms.booleanType.constType(b2i(intValue(od) != 0));
            case iflt:
                return syms.booleanType.constType(b2i(intValue(od) < 0));
            case ifgt:
                return syms.booleanType.constType(b2i(intValue(od) > 0));
            case ifle:
                return syms.booleanType.constType(b2i(intValue(od) <= 0));
            case ifge:
                return syms.booleanType.constType(b2i(intValue(od) >= 0));

            case lneg: // unary -
                return syms.longType.constType(new Long(-longValue(od)));
            case lxor: // ~
                return syms.longType.constType(new Long(~longValue(od)));

            case fneg: // unary -
                return syms.floatType.constType(new Float(-floatValue(od)));

            case dneg: // ~
                return syms.doubleType.constType(new Double(-doubleValue(od)));

            default:
                return null;
            }
        } catch (ArithmeticException e) {
            return null;
        }
    }

    /** Fold binary operation.
     *  @param opcode    The operation's opcode instruction (usually a byte code),
     *                   as entered by class Symtab.
     *                   opcode's ifeq to ifge are for postprocessing
     *                   xcmp; ifxx pairs of instructions.
     *  @param left      The type of the operation's left operand.
     *  @param right     The type of the operation's right operand.
     */
    Type fold2(int opcode, Type left, Type right) {
        try {
            if (opcode > ByteCodes.preMask) {
                // we are seeing a composite instruction of the form xcmp; ifxx.
                // In this case fold both instructions separately.
                Type t1 = fold2(opcode >> ByteCodes.preShift, left, right);
                return (t1.constValue() == null) ? t1
                    : fold1(opcode & ByteCodes.preMask, t1);
            } else {
                Object l = left.constValue();
                Object r = right.constValue();
                switch (opcode) {
                case iadd:
                    return syms.intType.constType(intValue(l) + intValue(r));
                case isub:
                    return syms.intType.constType(intValue(l) - intValue(r));
                case imul:
                    return syms.intType.constType(intValue(l) * intValue(r));
                case idiv:
                    return syms.intType.constType(intValue(l) / intValue(r));
                case imod:
                    return syms.intType.constType(intValue(l) % intValue(r));
                case iand:
                    return (left.tag == BOOLEAN
                      ? syms.booleanType : syms.intType)
                      .constType(intValue(l) & intValue(r));
                case bool_and:
                    return syms.booleanType.constType(b2i((intValue(l) & intValue(r)) != 0));
                case ior:
                    return (left.tag == BOOLEAN
                      ? syms.booleanType : syms.intType)
                      .constType(intValue(l) | intValue(r));
                case bool_or:
                    return syms.booleanType.constType(b2i((intValue(l) | intValue(r)) != 0));
                case ixor:
                    return (left.tag == BOOLEAN
                      ? syms.booleanType : syms.intType)
                      .constType(intValue(l) ^ intValue(r));
                case ishl: case ishll:
                    return syms.intType.constType(intValue(l) << intValue(r));
                case ishr: case ishrl:
                    return syms.intType.constType(intValue(l) >> intValue(r));
                case iushr: case iushrl:
                    return syms.intType.constType(intValue(l) >>> intValue(r));
                case if_icmpeq:
                    return syms.booleanType.constType(
                        b2i(intValue(l) == intValue(r)));
                case if_icmpne:
                    return syms.booleanType.constType(
                        b2i(intValue(l) != intValue(r)));
                case if_icmplt:
                    return syms.booleanType.constType(
                        b2i(intValue(l) < intValue(r)));
                case if_icmpgt:
                    return syms.booleanType.constType(
                        b2i(intValue(l) > intValue(r)));
                case if_icmple:
                    return syms.booleanType.constType(
                        b2i(intValue(l) <= intValue(r)));
                case if_icmpge:
                    return syms.booleanType.constType(
                        b2i(intValue(l) >= intValue(r)));

                case ladd:
                    return syms.longType.constType(
                        new Long(longValue(l) + longValue(r)));
                case lsub:
                    return syms.longType.constType(
                        new Long(longValue(l) - longValue(r)));
                case lmul:
                    return syms.longType.constType(
                        new Long(longValue(l) * longValue(r)));
                case ldiv:
                    return syms.longType.constType(
                        new Long(longValue(l) / longValue(r)));
                case lmod:
                    return syms.longType.constType(
                        new Long(longValue(l) % longValue(r)));
                case land:
                    return syms.longType.constType(
                        new Long(longValue(l) & longValue(r)));
                case lor:
                    return syms.longType.constType(
                        new Long(longValue(l) | longValue(r)));
                case lxor:
                    return syms.longType.constType(
                        new Long(longValue(l) ^ longValue(r)));
                case lshl: case lshll:
                    return syms.longType.constType(
                        new Long(longValue(l) << intValue(r)));
                case lshr: case lshrl:
                    return syms.longType.constType(
                        new Long(longValue(l) >> intValue(r)));
                case lushr:
                    return syms.longType.constType(
                        new Long(longValue(l) >>> intValue(r)));
                case lcmp:
                    if (longValue(l) < longValue(r))
                        return syms.intType.constType(minusOne);
                    else if (longValue(l) > longValue(r))
                        return syms.intType.constType(one);
                    else
                        return syms.intType.constType(zero);
                case fadd:
                    return syms.floatType.constType(
                        new Float(floatValue(l) + floatValue(r)));
                case fsub:
                    return syms.floatType.constType(
                        new Float(floatValue(l) - floatValue(r)));
                case fmul:
                    return syms.floatType.constType(
                        new Float(floatValue(l) * floatValue(r)));
                case fdiv:
                    return syms.floatType.constType(
                        new Float(floatValue(l) / floatValue(r)));
                case fmod:
                    return syms.floatType.constType(
                        new Float(floatValue(l) % floatValue(r)));
                case fcmpg: case fcmpl:
                    if (floatValue(l) < floatValue(r))
                        return syms.intType.constType(minusOne);
                    else if (floatValue(l) > floatValue(r))
                        return syms.intType.constType(one);
                    else if (floatValue(l) == floatValue(r))
                        return syms.intType.constType(zero);
                    else if (opcode == fcmpg)
                        return syms.intType.constType(one);
                    else
                        return syms.intType.constType(minusOne);
                case dadd:
                    return syms.doubleType.constType(
                        new Double(doubleValue(l) + doubleValue(r)));
                case dsub:
                    return syms.doubleType.constType(
                        new Double(doubleValue(l) - doubleValue(r)));
                case dmul:
                    return syms.doubleType.constType(
                        new Double(doubleValue(l) * doubleValue(r)));
                case ddiv:
                    return syms.doubleType.constType(
                        new Double(doubleValue(l) / doubleValue(r)));
                case dmod:
                    return syms.doubleType.constType(
                        new Double(doubleValue(l) % doubleValue(r)));
                case dcmpg: case dcmpl:
                    if (doubleValue(l) < doubleValue(r))
                        return syms.intType.constType(minusOne);
                    else if (doubleValue(l) > doubleValue(r))
                        return syms.intType.constType(one);
                    else if (doubleValue(l) == doubleValue(r))
                        return syms.intType.constType(zero);
                    else if (opcode == dcmpg)
                        return syms.intType.constType(one);
                    else
                        return syms.intType.constType(minusOne);
                case if_acmpeq:
                    return syms.booleanType.constType(b2i(l.equals(r)));
                case if_acmpne:
                    return syms.booleanType.constType(b2i(!l.equals(r)));
                case string_add:
                    return syms.stringType.constType(
                        left.stringValue() + right.stringValue());
                default:
                    return null;
                }
            }
        } catch (ArithmeticException e) {
            return null;
        }
    }

    /** Coerce constant type to target type.
     *  @param etype      The source type of the coercion,
     *                    which is assumed to be a constant type compatble with
     *                    ttype.
     *  @param ttype      The target type of the coercion.
     */
     Type coerce(Type etype, Type ttype) {
         // WAS if (etype.baseType() == ttype.baseType())
         if (etype.tsym.type == ttype.tsym.type)
             return etype;
         if (etype.tag <= DOUBLE) {
             Object n = etype.constValue();
             switch (ttype.tag) {
             case BYTE:
                 return syms.byteType.constType(0 + (byte)intValue(n));
             case CHAR:
                 return syms.charType.constType(0 + (char)intValue(n));
             case SHORT:
                 return syms.shortType.constType(0 + (short)intValue(n));
             case INT:
                 return syms.intType.constType(intValue(n));
             case LONG:
                 return syms.longType.constType(longValue(n));
             case FLOAT:
                 return syms.floatType.constType(floatValue(n));
             case DOUBLE:
                 return syms.doubleType.constType(doubleValue(n));
             }
         }
         return ttype;
     }
}
