/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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

package sun.misc;

/**
 * This class contains additional constants documenting limits of the
 * <code>float</code> type.
 *
 * @author Joseph D. Darcy
 */

public class FloatConsts {
    /**
     * Don't let anyone instantiate this class.
     */
    private FloatConsts() {}

    public static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    public static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;
    public static final float NaN = Float.NaN;
    public static final float MAX_VALUE = Float.MAX_VALUE;
    public static final float MIN_VALUE = Float.MIN_VALUE;

    /**
     * A constant holding the smallest positive normal value of type
     * <code>float</code>, 2<sup>-126</sup>.  It is equal to the value
     * returned by <code>Float.intBitsToFloat(0x00800000)</code>.
     */
    public static final float   MIN_NORMAL      = 1.17549435E-38f;

    /**
     * The number of logical bits in the significand of a
     * <code>float</code> number, including the implicit bit.
     */
    public static final int SIGNIFICAND_WIDTH   = 24;

    /**
     * Maximum exponent a finite <code>float</code> number may have.
     * It is equal to the value returned by
     * <code>Math.ilogb(Float.MAX_VALUE)</code>.
     */
    public static final int     MAX_EXPONENT    = 127;

    /**
     * Minimum exponent a normalized <code>float</code> number may
     * have.  It is equal to the value returned by
     * <code>Math.ilogb(Float.MIN_NORMAL)</code>.
     */
    public static final int     MIN_EXPONENT    = -126;

    /**
     * The exponent the smallest positive <code>float</code> subnormal
     * value would have if it could be normalized.  It is equal to the
     * value returned by <code>FpUtils.ilogb(Float.MIN_VALUE)</code>.
     */
    public static final int     MIN_SUB_EXPONENT = MIN_EXPONENT -
                                                   (SIGNIFICAND_WIDTH - 1);

    /**
     * Bias used in representing a <code>float</code> exponent.
     */
    public static final int     EXP_BIAS        = 127;

    /**
     * Bit mask to isolate the sign bit of a <code>float</code>.
     */
    public static final int     SIGN_BIT_MASK   = 0x80000000;

    /**
     * Bit mask to isolate the exponent field of a
     * <code>float</code>.
     */
    public static final int     EXP_BIT_MASK    = 0x7F800000;

    /**
     * Bit mask to isolate the significand field of a
     * <code>float</code>.
     */
    public static final int     SIGNIF_BIT_MASK = 0x007FFFFF;

    static {
        // verify bit masks cover all bit positions and that the bit
        // masks are non-overlapping
        assert(((SIGN_BIT_MASK | EXP_BIT_MASK | SIGNIF_BIT_MASK) == ~0) &&
               (((SIGN_BIT_MASK & EXP_BIT_MASK) == 0) &&
                ((SIGN_BIT_MASK & SIGNIF_BIT_MASK) == 0) &&
                ((EXP_BIT_MASK & SIGNIF_BIT_MASK) == 0)));
    }
}
