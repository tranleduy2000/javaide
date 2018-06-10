package com.duy.ide.javaide;

import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

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
        systemOut = new InterceptorOutputStream(System.out, out);
        systemErr = new InterceptorOutputStream(System.err, err);
        System.setOut(systemOut);
        System.setErr(systemErr);

        //for log cat
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public void addStdOut(PrintStream out) {
        systemOut.add(out);
    }

    public void addStdErr(PrintStream err) {
        systemErr.add(err);
    }

    public void removeOutStream(PrintStream out) {
        systemOut.remove(out);
    }

    public void removeErrStream(PrintStream err) {
        systemErr.remove(err);
    }

    private static class InterceptorOutputStream extends PrintStream {

        private static final String TAG = "InterceptorOutputStream";
        private ArrayList<PrintStream> streams;

        public InterceptorOutputStream(@NonNull OutputStream file, ArrayList<PrintStream> streams) {
            super(file, true);
            this.streams = streams;
        }

        public ArrayList<PrintStream> getStreams() {
            return streams;
        }

        public void setStreams(ArrayList<PrintStream> streams) {
            this.streams = streams;
        }

        public void add(PrintStream out) {
            Log.d(TAG, "add() called with: out = [" + out + "]");

            this.streams.add(out);
        }

        public void remove(PrintStream out) {
            Log.d(TAG, "remove() called with: out = [" + out + "]");

            this.streams.remove(out);
        }

        @Override
        public void write(@NonNull byte[] buf, int off, int len) {
            super.write(buf, off, len);
            if (streams != null) {
                for (PrintStream printStream : streams) {
                    printStream.write(buf, off, len);
                }
            }
        }
    }

}
