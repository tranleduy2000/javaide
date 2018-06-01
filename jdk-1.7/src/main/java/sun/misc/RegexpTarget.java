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
 * A class to define actions to be performed when a regular expression match
 *  occurs.
 * @author  James Gosling
 */

public interface RegexpTarget {
    /** Gets called when a pattern in a RegexpPool matches.
      * This method is called by RegexpPool.match() who passes the return
      * value from found() back to its caller.
      * @param remainder the string that matched the * in the pattern.
      */
    Object found(String remainder);
}
