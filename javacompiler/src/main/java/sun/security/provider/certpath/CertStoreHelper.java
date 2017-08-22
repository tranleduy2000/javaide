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

package sun.security.provider.certpath;

import java.net.URI;
import java.util.Collection;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertStore;
import java.security.cert.X509CertSelector;
import java.security.cert.X509CRLSelector;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;

/**
 * Helper used by URICertStore when delegating to another CertStore to
 * fetch certs and CRLs.
 */

public interface CertStoreHelper {

    /**
     * Returns a CertStore using the given URI as parameters.
     */
    CertStore getCertStore(URI uri)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    /**
     * Wraps an existing X509CertSelector when needing to avoid DN matching
     * issues.
     */
    X509CertSelector wrap(X509CertSelector selector,
                          X500Principal certSubject,
                          String dn)
        throws IOException;

    /**
     * Wraps an existing X509CRLSelector when needing to avoid DN matching
     * issues.
     */
    X509CRLSelector wrap(X509CRLSelector selector,
                         Collection<X500Principal> certIssuers,
                         String dn)
        throws IOException;
}
