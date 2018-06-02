package com.android.tests.basic;

import java.io.InputStream;
import java.io.IOException;
import java.lang.RuntimeException;
import java.util.Properties;
import java.util.Enumeration;
import java.net.URL;

import com.android.tests.other.PropertiesProvider;

/**
 * String provider getting the string format from a co-bundled resources.properties file.
 */
public class StringProvider {

    public static String getString(int aNumber) {
        return String.format(
                PropertiesProvider.getProperty("the.format"),
                PropertiesProvider.getProperty("the.string"),
                aNumber);
    }
}
