/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Generic PKCS Encoding exception.
 *
 * @author Benjamin Renaud
 */

package sun.security.pkcs;

public class EncodingException extends Exception {

    private static final long serialVersionUID = 4060198374240668325L;

    public EncodingException() {
        super();
    }

    public EncodingException(String s) {
        super(s);
    }
}
