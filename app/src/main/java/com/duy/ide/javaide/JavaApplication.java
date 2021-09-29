/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide;

import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.duy.ide.javaide.setting.IdePreferenceManager;

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

        IdePreferenceManager.setDefaultValues(this);
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
