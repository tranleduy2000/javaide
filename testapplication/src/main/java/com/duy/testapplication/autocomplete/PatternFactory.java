package com.duy.testapplication.autocomplete;

import java.util.regex.Pattern;

/**
 * Created by Duy on 20-Jul-17.
 */

public class PatternFactory {
    public static final Pattern PACKAGE = Pattern.compile("package\\s+[^;]*;");
    public static final Pattern IMPORT = Pattern.compile("import\\s+[^;]*\\s?;");
    public static final Pattern WORD = Pattern.compile("[^\\s-]+$");

    public static Pattern makeImport(String className) {
        return Pattern.compile("import\\s+(.*" + className + ")\\s?;");
    }
}
