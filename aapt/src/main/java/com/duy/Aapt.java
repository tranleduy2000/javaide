package com.duy;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

public class Aapt {

    public static final File dirLog = new File("/storage/emulated/0/.AaptJNI");

    public static final File txtOut = new File(dirLog, "native_stdout.txt");
    public static final File txtErr = new File(dirLog, "native_stderr.txt");

    private static boolean bInitialized = false;

    public Aapt() {
        if (!bInitialized) {
            fnInit();
        }
    }

    private static boolean fnInit() {
        try {
            dirLog.mkdirs();
            System.out.println("Loading native library aaptcomplete...");
            System.loadLibrary("aaptcomplete");
            bInitialized = true;
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private native int JNImain(String args);

    public boolean isInitialized() {
        return bInitialized;
    }

    public synchronized int fnExecute(String args) {
        int rc = 99;
        System.out.println("Calling JNImain...");
        rc = JNImain(args.replace(' ', '\t'));
        System.out.println("Result from native lib=" + rc);
        fnGetNativeOutput();
        return rc;
    }

    private void fnGetNativeOutput() {
        LineNumberReader lnr;
        String st = "";
        try {
            lnr = new LineNumberReader(new FileReader(txtOut));
            st = "";
            while (st != null) {
                st = lnr.readLine();
                if (st != null)
                    System.out.println(st);
            }
            lnr.close();
            lnr = new LineNumberReader(new FileReader(txtErr));
            st = "";
            while (st != null) {
                st = lnr.readLine();
                if (st != null)
                    System.err.println(st);
            }
            lnr.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}