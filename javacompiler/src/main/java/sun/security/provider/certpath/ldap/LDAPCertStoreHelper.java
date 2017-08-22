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

package sun.security.provider.certpath.ldap;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertStore;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import sun.security.provider.certpath.CertStoreHelper;

/**
 * LDAP implementation of CertStoreHelper.
 */

public class LDAPCertStoreHelper
    implements CertStoreHelper
{
    public LDAPCertStoreHelper() { }

    @Override
    public CertStore getCertStore(URI uri)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
    {
        return LDAPCertStore.getInstance(LDAPCertStore.getParameters(uri));
    }

    @Override
    public X509CertSelector wrap(X509CertSelector selector,
                                 X500Principal certSubject,
                                 String ldapDN)
        throws IOException
    {
        return new LDAPCertStore.LDAPCertSelector(selector, certSubject, ldapDN);
    }

    @Override
    public X509CRLSelector wrap(X509CRLSelector selector,
                                Collection<X500Principal> certIssuers,
                                String ldapDN)
        throws IOException
    {
        return new LDAPCertStore.LDAPCRLSelector(selector, certIssuers, ldapDN);
    }
}
