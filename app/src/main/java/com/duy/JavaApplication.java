package com.duy;

import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.duy.ide.BuildConfig;
import com.google.firebase.crash.FirebaseCrash;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by Duy on 17-Jul-17.
 */

public class JavaApplication extends MultiDexApplication {
    private ArrayList<PrintStream> out = new ArrayList<>();
    private ArrayList<PrintStream> err = new ArrayList<>();

    private InterceptorOutputStream systemOut;
    private InterceptorOutputStream systemErr;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FirebaseCrash.setCrashCollectionEnabled(false);
        }
        systemOut = new InterceptorOutputStream(System.out, out);
        systemErr = new InterceptorOutputStream(System.err, err);

        //for log cat
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public void addStdOut(PrintStream out) {
        this.out.add(out);
    }

    public void addStdErr(PrintStream err) {
        this.err.add(err);
    }

    public void removeOut(PrintStream out) {
        this.out.remove(out);
    }

    public void removeErr(PrintStream err) {
        this.err.remove(err);
    }

    private static class InterceptorOutputStream extends PrintStream {

        private ArrayList<PrintStream> out;

        public InterceptorOutputStream(@NonNull OutputStream file, ArrayList<PrintStream> out) {
            super(file, true);
            this.out = out;
        }


        @Override
        public void write(@NonNull byte[] buf, int off, int len) {
            super.write(buf, off, len);
            if (out != null) {
                for (PrintStream printStream : out) {
                    printStream.write(buf, off, len);
                }
            }
        }
    }
}
