/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.utils;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.text.NumberFormat;
import java.text.ParseException;

/** Miscellaneous utilities used by the Android SDK tools */
public class SdkUtils {
    /**
     * Returns true if the given string ends with the given suffix, using a
     * case-insensitive comparison.
     *
     * @param string the full string to be checked
     * @param suffix the suffix to be checked for
     * @return true if the string case-insensitively ends with the given suffix
     */
    public static boolean endsWithIgnoreCase(String string, String suffix) {
        return string.regionMatches(true /* ignoreCase */, string.length() - suffix.length(),
                suffix, 0, suffix.length());
    }

    /**
     * Returns true if the given sequence ends with the given suffix (case
     * sensitive).
     *
     * @param sequence the character sequence to be checked
     * @param suffix the suffix to look for
     * @return true if the given sequence ends with the given suffix
     */
    public static boolean endsWith(CharSequence sequence, CharSequence suffix) {
        return endsWith(sequence, sequence.length(), suffix);
    }

    /**
     * Returns true if the given sequence ends at the given offset with the given suffix (case
     * sensitive)
     *
     * @param sequence the character sequence to be checked
     * @param endOffset the offset at which the sequence is considered to end
     * @param suffix the suffix to look for
     * @return true if the given sequence ends with the given suffix
     */
    public static boolean endsWith(CharSequence sequence, int endOffset, CharSequence suffix) {
        if (endOffset < suffix.length()) {
            return false;
        }

        for (int i = endOffset - 1, j = suffix.length() - 1; j >= 0; i--, j--) {
            if (sequence.charAt(i) != suffix.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the given string starts with the given prefix, using a
     * case-insensitive comparison.
     *
     * @param string the full string to be checked
     * @param prefix the prefix to be checked for
     * @return true if the string case-insensitively starts with the given prefix
     */
    public static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.regionMatches(true /* ignoreCase */, 0, prefix, 0, prefix.length());
    }

    /**
     * Returns true if the given string starts at the given offset with the
     * given prefix, case insensitively.
     *
     * @param string the full string to be checked
     * @param offset the offset in the string to start looking
     * @param prefix the prefix to be checked for
     * @return true if the string case-insensitively starts at the given offset
     *         with the given prefix
     */
    public static boolean startsWith(String string, int offset, String prefix) {
        return string.regionMatches(true /* ignoreCase */, offset, prefix, 0, prefix.length());
    }

    /**
     * Strips the whitespace from the given string
     *
     * @param string the string to be cleaned up
     * @return the string, without whitespace
     */
    public static String stripWhitespace(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Returns true if the given string has an upper case character.
     *
     * @param s the string to check
     * @return true if it contains uppercase characters
     */
    public static boolean hasUpperCaseCharacter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /** For use by {@link #getLineSeparator()} */
    private static String sLineSeparator;

    /**
     * Returns the default line separator to use.
     * <p>
     * NOTE: If you have an associated IDocument (Eclipse), it is better to call
     * TextUtilities#getDefaultLineDelimiter(IDocument) since that will
     * allow (for example) editing a \r\n-delimited document on a \n-delimited
     * platform and keep a consistent usage of delimiters in the file.
     *
     * @return the delimiter string to use
     */
    @NonNull
    public static String getLineSeparator() {
        if (sLineSeparator == null) {
            // This is guaranteed to exist:
            sLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
        }

        return sLineSeparator;
    }

    /**
     * Wraps the given text at the given line width, with an optional hanging
     * indent.
     *
     * @param text the text to be wrapped
     * @param lineWidth the number of characters to wrap the text to
     * @param hangingIndent the hanging indent (to be used for the second and
     *            subsequent lines in each paragraph, or null if not known
     * @return the string, wrapped
     */
    @NonNull
    public static String wrap(
            @NonNull String text,
            int lineWidth,
            @Nullable String hangingIndent) {
        if (hangingIndent == null) {
            hangingIndent = "";
        }
        int explanationLength = text.length();
        StringBuilder sb = new StringBuilder(explanationLength * 2);
        int index = 0;

        while (index < explanationLength) {
            int lineEnd = text.indexOf('\n', index);
            int next;

            if (lineEnd != -1 && (lineEnd - index) < lineWidth) {
                next = lineEnd + 1;
            } else {
                // Line is longer than available width; grab as much as we can
                lineEnd = Math.min(index + lineWidth, explanationLength);
                if (lineEnd - index < lineWidth) {
                    next = explanationLength;
                } else {
                    // then back up to the last space
                    int lastSpace = text.lastIndexOf(' ', lineEnd);
                    if (lastSpace > index) {
                        lineEnd = lastSpace;
                        next = lastSpace + 1;
                    } else {
                        // No space anywhere on the line: it contains something wider than
                        // can fit (like a long URL) so just hard break it
                        next = lineEnd + 1;
                    }
                }
            }

            if (sb.length() > 0) {
                sb.append(hangingIndent);
            } else {
                lineWidth -= hangingIndent.length();
            }

            sb.append(text.substring(index, lineEnd));
            sb.append('\n');
            index = next;
        }

        return sb.toString();
    }

    /**
     * Returns the given localized string as an int. For example, in the
     * US locale, "1,000", will return 1000. In the French locale, "1.000" will return
     * 1000. It will return 0 for empty strings.
     * <p>
     * To parse a string without catching parser exceptions, call
     * {@link #parseLocalizedInt(String, int)} instead, passing the
     * default value to be returned if the format is invalid.
     *
     * @param string the string to be parsed
     * @return the integer value
     * @throws ParseException if the format is not correct
     */
    public static int parseLocalizedInt(@NonNull String string) throws ParseException {
        if (string.isEmpty()) {
            return 0;
        }
        return NumberFormat.getIntegerInstance().parse(string).intValue();
    }

    /**
     * Returns the given localized string as an int. For example, in the
     * US locale, "1,000", will return 1000. In the French locale, "1.000" will return
     * 1000.  If the format is invalid, returns the supplied default value instead.
     *
     * @param string the string to be parsed
     * @param defaultValue the value to be returned if there is a parsing error
     * @return the integer value
     */
    public static int parseLocalizedInt(@NonNull String string, int defaultValue) {
        try {
            return parseLocalizedInt(string);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the given localized string as a double. For example, in the
     * US locale, "3.14", will return 3.14. In the French locale, "3,14" will return
     * 3.14. It will return 0 for empty strings.
     * <p>
     * To parse a string without catching parser exceptions, call
     * {@link #parseLocalizedDouble(String, double)} instead, passing the
     * default value to be returned if the format is invalid.
     *
     * @param string the string to be parsed
     * @return the double value
     * @throws ParseException if the format is not correct
     */
    public static double parseLocalizedDouble(@NonNull String string) throws ParseException {
        if (string.isEmpty()) {
            return 0.0;
        }
        return NumberFormat.getNumberInstance().parse(string).doubleValue();
    }

    /**
     * Returns the given localized string as a double. For example, in the
     * US locale, "3.14", will return 3.14. In the French locale, "3,14" will return
     * 3.14. If the format is invalid, returns the supplied default value instead.
     *
     * @param string the string to be parsed
     * @param defaultValue the value to be returned if there is a parsing error
     * @return the double value
     */
    public static double parseLocalizedDouble(@NonNull String string, double defaultValue) {
        try {
            return parseLocalizedDouble(string);
        } catch (ParseException e) {
            return defaultValue;
        }
    }
}
