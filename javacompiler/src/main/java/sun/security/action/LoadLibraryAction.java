/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.action;

/**
 * A convenience class for loading a system library as a privileged action.
 * <p>
 * <p>An instance of this class can be used as the argument of
 * <code>AccessController.doPrivileged</code>.
 * <p>
 * <p>The following code attempts to load the system library named
 * <code>"lib"</code> as a privileged action: <p>
 * <p>
 * <pre>
 * java.security.AccessController.doPrivileged(new LoadLibraryAction("lib"));
 * </pre>
 *
 * @author Roland Schemers
 * @see java.security.PrivilegedAction
 * @see java.security.AccessController
 * @since 1.2
 */

public class LoadLibraryAction implements java.security.PrivilegedAction<Void> {
    private String theLib;

    /**
     * Constructor that takes the name of the system library that needs to be
     * loaded.
     * <p>
     * <p>The manner in which a library name is mapped to the
     * actual system library is system dependent.
     *
     * @param theLib the name of the library.
     */
    public LoadLibraryAction(String theLib) {
        this.theLib = theLib;
    }

    /**
     * Loads the system library whose name was specified in the constructor.
     */
    public Void run() {
        System.loadLibrary(theLib);
        return null;
    }
}
