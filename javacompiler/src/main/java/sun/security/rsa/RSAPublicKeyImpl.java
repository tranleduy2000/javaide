/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.interfaces.RSAPublicKey;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;

/**
 * Key implementation for RSA public keys.
 * <p>
 * Note: RSA keys must be at least 512 bits long
 *
 * @author Andreas Sterbenz
 * @see RSAPrivateCrtKeyImpl
 * @see RSAKeyFactory
 * @since 1.5
 */
public final class RSAPublicKeyImpl extends X509Key implements RSAPublicKey {

    private static final long serialVersionUID = 2644735423591199609L;

    private BigInteger n;       // modulus
    private BigInteger e;       // public exponent

    /**
     * Construct a key from its components. Used by the
     * RSAKeyFactory and the RSAKeyPairGenerator.
     */
    public RSAPublicKeyImpl(BigInteger n, BigInteger e)
            throws InvalidKeyException {
        this.n = n;
        this.e = e;
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
        // generate the encoding
        algid = RSAPrivateCrtKeyImpl.rsaId;
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(n);
            out.putInteger(e);
            DerValue val =
                    new DerValue(DerValue.tag_Sequence, out.toByteArray());
            key = val.toByteArray();
        } catch (IOException exc) {
            // should never occur
            throw new InvalidKeyException(exc);
        }
    }

    /**
     * Construct a key from its encoding. Used by RSAKeyFactory.
     */
    public RSAPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
    }

    // see JCA doc
    public String getAlgorithm() {
        return "RSA";
    }

    // see JCA doc
    public BigInteger getModulus() {
        return n;
    }

    // see JCA doc
    public BigInteger getPublicExponent() {
        return e;
    }

    /**
     * Parse the key. Called by X509Key.
     */
    protected void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(key);
            DerValue derValue = in.getDerValue();
            if (derValue.tag != DerValue.tag_Sequence) {
                throw new IOException("Not a SEQUENCE");
            }
            DerInputStream data = derValue.data;
            n = RSAPrivateCrtKeyImpl.getBigInteger(data);
            e = RSAPrivateCrtKeyImpl.getBigInteger(data);
            if (derValue.data.available() != 0) {
                throw new IOException("Extra data available");
            }
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid RSA public key", e);
        }
    }

    // return a string representation of this key for debugging
    public String toString() {
        return "Sun RSA public key, " + n.bitLength() + " bits\n  modulus: "
                + n + "\n  public exponent: " + e;
    }

    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC,
                getAlgorithm(),
                getFormat(),
                getEncoded());
    }
}
