/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.util;

import java.security.Key;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.DSAKey;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;

/**
 * A utility class to get key length
 */
public final class KeyLength {

    /**
     * Returns the key size of the given key object in bits.
     *
     * @param key the key object, cannot be null
     * @return the key size of the given key object in bits, or -1 if the
     *       key size is not accessible
     */
    final public static int getKeySize(Key key) {
        int size = -1;

        if (key instanceof Length) {
            try {
                Length ruler = (Length)key;
                size = ruler.length();
            } catch (UnsupportedOperationException usoe) {
                // ignore the exception
            }

            if (size >= 0) {
                return size;
            }
        }

        // try to parse the length from key specification
        if (key instanceof SecretKey) {
            SecretKey sk = (SecretKey)key;
            String format = sk.getFormat();
            if ("RAW".equals(format) && sk.getEncoded() != null) {
                size = (sk.getEncoded().length * 8);
            }   // Otherwise, it may be a unextractable key of PKCS#11, or
                // a key we are not able to handle.
        } else if (key instanceof RSAKey) {
            RSAKey pubk = (RSAKey)key;
            size = pubk.getModulus().bitLength();
        } else if (key instanceof ECKey) {
            ECKey pubk = (ECKey)key;
            size = pubk.getParams().getOrder().bitLength();
        } else if (key instanceof DSAKey) {
            DSAKey pubk = (DSAKey)key;
            size = pubk.getParams().getP().bitLength();
        } else if (key instanceof DHKey) {
            DHKey pubk = (DHKey)key;
            size = pubk.getParams().getP().bitLength();
        }   // Otherwise, it may be a unextractable key of PKCS#11, or
            // a key we are not able to handle.

        return size;
    }
}

