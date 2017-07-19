/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.external;


import com.sun.tools.javac.Main;

import java.io.PrintWriter;

public class javac {
    public static void main(String[] zArgs) {
        int compile = Main.compile(zArgs);
    }

    public static int compile(String[] args) {
        return Main.compile(args);
    }

    public static int compile(String[] args, PrintWriter printWriter) {
        return Main.compile(args, printWriter);
    }
}
