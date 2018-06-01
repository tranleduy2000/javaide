/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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

/*
 * Exception when installation of an extension has failed for
 * any reason
 *
 * @author  Jerome Dochez
 */

public class ExtensionInstallationException extends Exception {

    /*
     * <p>
     * Construct a new exception with an exception reason
     * </p>
     */
    public ExtensionInstallationException(String s) {
        super(s);
    }
}
