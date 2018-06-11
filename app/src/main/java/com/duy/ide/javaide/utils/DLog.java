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

package com.duy.ide.javaide.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;


/**
 * Created by Duy on 27-Mar-17.
 */
public class DLog {
    public static String TAG = DLog.class.getSimpleName();
    public static boolean DEBUG = true;
    public static boolean ANDROID = true;

    public static void d(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    public static void d(String TAG, Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    public static void w(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    public static void w(String TAG, Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    public static void e(Exception exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", exception);
            } else {
                System.err.println(TAG + ": " + exception.toString());
            }
        }
    }

    public static void e(String TAG, Exception exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", exception);
            } else {
                System.err.println(TAG + ": " + exception.toString());
            }
        }
    }

    public static void e(String TAG, String exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, exception);
            } else {
                System.err.println(TAG + ": " + exception);
            }
        }
    }

    public static void e(String TAG, String msg, Exception e) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, msg, e);
            } else {
                System.err.println(TAG + ": " + msg);
                e.printStackTrace();
            }
        }
    }

    public static void i(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.i(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    public static void reportException(Throwable e) {
        Crashlytics.logException(e);
    }

}
