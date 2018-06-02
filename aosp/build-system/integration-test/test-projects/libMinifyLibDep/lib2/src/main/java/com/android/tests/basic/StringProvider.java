package com.android.tests.basic;

public class StringProvider {

    public static String getString(int foo) {
        return getStringInternal(foo);
    }

    /**
     * method that will get obfuscated by the library.
     */
    public static String getStringInternal(int foo) {
        return Integer.toString(foo);
    }

}
