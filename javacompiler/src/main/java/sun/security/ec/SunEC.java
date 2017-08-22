/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.ec;

import java.util.*;
import java.security.*;
import sun.security.action.PutAllAction;

/**
 * Provider class for the Elliptic Curve provider.
 * Supports EC keypair and parameter generation, ECDSA signing and
 * ECDH key agreement.
 *
 * IMPLEMENTATION NOTE:
 * The Java classes in this provider access a native ECC implementation
 * via JNI to a C++ wrapper class which in turn calls C functions.
 * The Java classes are packaged into the signed sunec.jar in the JRE
 * extensions directory and the C++ and C functions are packaged into
 * libsunec.so or sunec.dll in the JRE native libraries directory.
 * If the native library is not present then this provider is registered
 * with support for fewer ECC algorithms (KeyPairGenerator, Signature and
 * KeyAgreement are omitted).
 *
 * @since   1.7
 */
public final class SunEC extends Provider {

    private static final long serialVersionUID = -2279741672933606418L;

    // flag indicating whether the full EC implementation is present
    // (when native library is absent then fewer EC algorithms are available)
    private static boolean useFullImplementation = true;
    static {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("sunec"); // check for native library
                    return null;
                }
            });
        } catch (UnsatisfiedLinkError e) {
            useFullImplementation = false;
        }
    }

    public SunEC() {
        super("SunEC", 1.7d, "Sun Elliptic Curve provider (EC, ECDSA, ECDH)");

        // if there is no security manager installed, put directly into
        // the provider. Otherwise, create a temporary map and use a
        // doPrivileged() call at the end to transfer the contents
        if (System.getSecurityManager() == null) {
            SunECEntries.putEntries(this, useFullImplementation);
        } else {
            Map<Object, Object> map = new HashMap<Object, Object>();
            SunECEntries.putEntries(map, useFullImplementation);
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }

}
