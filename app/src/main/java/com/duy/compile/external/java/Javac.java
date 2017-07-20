package com.duy.compile.external.java;

import com.sun.tools.javac.Main;

import java.io.PrintWriter;

import javax.tools.DiagnosticListener;

/**
 * Created by duy on 19/07/2017.
 */

public class Javac {

    public static int compile(String[] args) {
        return Main.compile(args);
    }

    public static int compile(String[] args, PrintWriter printWriter) {
        return Main.compile(args, printWriter);
    }

    public static int compile(String[] args, PrintWriter printWriter, DiagnosticListener listener) {
        return Main.compile(args, printWriter, listener);
    }

}
