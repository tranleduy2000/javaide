/*
 * Copyright (c) 1995, Oracle and/or its affiliates. All rights reserved.
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
 * A class to signal exception from the RegexpPool class.
 * @author  James Gosling
 */

public class REException extends Exception {
    REException (String s) {
        super(s);
    }
}
