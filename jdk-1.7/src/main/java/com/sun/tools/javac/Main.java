/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.javac;

import java.io.PrintWriter;
import java.lang.reflect.*;


/**
 * The programmatic interface for the Java Programming Language
 * compiler, javac.
 *
 * <p>Except for the two methods
 * {@link #compile(String[])}
 * {@link #compile(String[],PrintWriter)},
 * nothing described in this source file is part of any supported
 * API.  If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.
 */
public class Main {

    /** Unsupported command line interface.
     * @param args   The command line parameters.
     */
    public static void main(String[] args) throws Exception {
      if (args.length > 0 && args[0].equals("-Xjdb")) {
        String[] newargs = new String[args.length + 2];
        Class<?> c = Class.forName("com.sun.tools.example.debug.tty.TTY");
        Method method = c.getDeclaredMethod ("main", new Class<?>[] {args.getClass()});
        method.setAccessible(true);
        System.arraycopy(args, 1, newargs, 3, args.length - 1);
        newargs[0] = "-connect";
        newargs[1] = "com.sun.jdi.CommandLineLaunch:options=-esa -ea:com.sun.tools...";
        newargs[2] = "com.sun.tools.javac.Main";
        method.invoke(null, new Object[] { newargs });
      } else {
        System.exit(compile(args));
      }
    }

    /** Programmatic interface to the Java Programming Language
     * compiler, javac.
     *
     * @param args The command line arguments that would normally be
     * passed to the javac program as described in the man page.
     * @return an integer equivalent to the exit value from invoking
     * javac, see the man page for details.
     */
    public static int compile(String[] args) {
        com.sun.tools.javac.main.Main compiler =
            new com.sun.tools.javac.main.Main("javac");
        return compiler.compile(args);
    }



    /** Programmatic interface to the Java Programming Language
     * compiler, javac.
     *
     * @param args The command line arguments that would normally be
     * passed to the javac program as described in the man page.
     * @param out PrintWriter to which the compiler's diagnostic
     * output is directed.
     * @return an integer equivalent to the exit value from invoking
     * javac, see the man page for details.
     */
    public static int compile(String[] args, PrintWriter out) {
        com.sun.tools.javac.main.Main compiler =
            new com.sun.tools.javac.main.Main("javac", out);
        return compiler.compile(args);
    }
}
