/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.provider;

import java.util.*;
import java.security.*;

import sun.security.action.PutAllAction;

/**
 * The SUN Security Provider.
 *
 */
public final class Sun extends Provider {

    private static final long serialVersionUID = 6440182097568097204L;

    private static final String INFO = "SUN " +
    "(DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; " +
    "SecureRandom; X.509 certificates; JKS keystore; PKIX CertPathValidator; " +
    "PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; " +
    "JavaLoginConfig Configuration)";

    public Sun() {
        /* We are the SUN provider */
        super("SUN", 1.7, INFO);

        // if there is no security manager installed, put directly into
        // the provider. Otherwise, create a temporary map and use a
        // doPrivileged() call at the end to transfer the contents
        if (System.getSecurityManager() == null) {
            SunEntries.putEntries(this);
        } else {
            // use LinkedHashMap to preserve the order of the PRNGs
            Map<Object, Object> map = new LinkedHashMap<>();
            SunEntries.putEntries(map);
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }

}
