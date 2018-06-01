/*
 * Copyright (c) 1994, 1995, Oracle and/or its affiliates. All rights reserved.
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
 * The CRC-16 class calculates a 16 bit cyclic redundancy check of a set
 * of bytes. This error detecting code is used to determine if bit rot
 * has occured in a byte stream.
 */

public class CRC16 {

    /** value contains the currently computed CRC, set it to 0 initally */
    public int value;

    public CRC16() {
        value = 0;
    }

    /** update CRC with byte b */
    public void update(byte aByte) {
        int a, b;

        a = (int) aByte;
        for (int count = 7; count >=0; count--) {
            a = a << 1;
            b = (a >>> 8) & 1;
            if ((value & 0x8000) != 0) {
                value = ((value << 1) + b) ^ 0x1021;
            } else {
                value = (value << 1) + b;
            }
        }
        value = value & 0xffff;
        return;
    }

    /** reset CRC value to 0 */
    public void reset() {
        value = 0;
    }
}
