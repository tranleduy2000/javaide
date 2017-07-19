package com.duy.external.java;

import com.sun.tools.javac.Main;

import java.io.PrintWriter;

/**
 * Created by duy on 19/07/2017.
 */

public class Javac {
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
