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

package sun.security.jca;

import java.security.SecureRandom;

/**
 * Collection of static utility methods used by the security framework.
 *
 * @author  Andreas Sterbenz
 * @since   1.5
 */
public final class JCAUtil {

    private JCAUtil() {
        // no instantiation
    }

    // lock to use for synchronization
    private static final Object LOCK = JCAUtil.class;

    // cached SecureRandom instance
    private static volatile SecureRandom secureRandom;

    // size of the temporary arrays we use. Should fit into the CPU's 1st
    // level cache and could be adjusted based on the platform
    private final static int ARRAY_SIZE = 4096;

    /**
     * Get the size of a temporary buffer array to use in order to be
     * cache efficient. totalSize indicates the total amount of data to
     * be buffered. Used by the engineUpdate(ByteBuffer) methods.
     */
    public static int getTempArraySize(int totalSize) {
        return Math.min(ARRAY_SIZE, totalSize);
    }

    /**
     * Get a SecureRandom instance. This method should me used by JDK
     * internal code in favor of calling "new SecureRandom()". That needs to
     * iterate through the provider table to find the default SecureRandom
     * implementation, which is fairly inefficient.
     */
    public static SecureRandom getSecureRandom() {
        // we use double checked locking to minimize synchronization
        // works because we use a volatile reference
        SecureRandom r = secureRandom;
        if (r == null) {
            synchronized (LOCK) {
                r = secureRandom;
                if (r == null) {
                    r = new SecureRandom();
                    secureRandom = r;
                }
            }
        }
        return r;
    }

}
