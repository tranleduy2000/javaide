package com.duy.ide.java.code_sample;

/**
 * Created by Duy on 28-Jul-17.
 */

public class StringUtil {
    public static String insertSpace(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(" ").append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
