/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class TextFieldDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new TextFieldDetector();
    }

    public void testField() throws Exception {
        assertEquals(
            "res/layout/note_edit.xml:50: Warning: This text field does not specify an inputType or a hint [TextFields]\n" +
            "        <EditText\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/note_edit.xml"));
    }

    public void testTypeFromName() throws Exception {
        assertEquals(
            "res/layout/edit_type.xml:14: Warning: The view name (@+id/mypassword) suggests this is a password, but it does not include 'textPassword' in the inputType [TextFields]\n" +
            "        android:inputType=\"text\" >\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:10: id defined here\n" +
            "res/layout/edit_type.xml:45: Warning: The view name (@+id/password_length) suggests this is a number, but it does not include a numeric inputType (such as 'numberSigned') [TextFields]\n" +
            "        android:inputType=\"text\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:41: id defined here\n" +
            "res/layout/edit_type.xml:54: Warning: The view name (@+id/welcome_url) suggests this is a URI, but it does not include 'textUri' in the inputType [TextFields]\n" +
            "        android:inputType=\"text\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:50: id defined here\n" +
            "res/layout/edit_type.xml:63: Warning: The view name (@+id/start_date) suggests this is a date, but it does not include 'date' or 'datetime' in the inputType [TextFields]\n" +
            "        android:inputType=\"text\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:59: id defined here\n" +
            "res/layout/edit_type.xml:72: Warning: The view name (@+id/email_address) suggests this is an e-mail address, but it does not include 'textEmail' in the inputType [TextFields]\n" +
            "        android:inputType=\"text\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:68: id defined here\n" +
            "res/layout/edit_type.xml:81: Warning: The view name (@+id/login_pin) suggests this is a password, but it does not include 'numberPassword' in the inputType [TextFields]\n" +
            "        android:inputType=\"textPassword\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/layout/edit_type.xml:77: id defined here\n" +
            "res/layout/edit_type.xml:83: Warning: This text field does not specify an inputType or a hint [TextFields]\n" +
            "    <EditText\n" +
            "    ^\n" +
            "res/layout/edit_type.xml:84: Warning: The view name (@+id/number_of_items) suggests this is a number, but it does not include a numeric inputType (such as 'numberSigned') [TextFields]\n" +
            "        android:id=\"@+id/number_of_items\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 8 warnings\n",

            lintFiles("res/layout/edit_type.xml"));
    }

    public void testContainsWord() {
        assertFalse(containsWord("", "foob"));
        assertFalse(containsWord("foo", "foob"));

        assertTrue(containsWord("foo", "foo"));
        assertTrue(containsWord("Foo", "foo"));
        assertTrue(containsWord("foo_bar", "foo"));
        assertTrue(containsWord("bar_foo", "foo"));
        assertTrue(containsWord("bar_Foo", "foo"));
        assertTrue(containsWord("bar_foo_baz", "foo"));
        assertTrue(containsWord("bar_Foo_baz", "foo"));
        assertTrue(containsWord("barFooBaz", "foo"));
        assertTrue(containsWord("barFOO_", "foo"));
        assertTrue(containsWord("FooBaz", "foo"));
        assertTrue(containsWord("BarFoo", "foo"));
        assertFalse(containsWord("barfoo", "foo"));
        assertTrue(containsWord("barfoo", "foo", false, true));
        assertTrue(containsWord("foobar", "foo", true, false));
        assertFalse(containsWord("foobar", "foo"));
        assertFalse(containsWord("barfoobar", "foo"));

        assertTrue(containsWord("phoneNumber", "phone"));
        assertTrue(containsWord("phoneNumber", "number"));
        assertTrue(containsWord("uri_prefix", "uri"));
        assertTrue(containsWord("fooURI", "uri"));
        assertTrue(containsWord("my_url", "url"));
        assertTrue(containsWord("network_prefix_length", "length"));

        assertFalse(containsWord("sizer", "size"));
        assertFalse(containsWord("synthesize_to_filename", "size"));
        assertFalse(containsWord("update_text", "date"));
        assertFalse(containsWord("daten", "date"));

        assertFalse(containsWord("phonenumber", "phone"));
        assertFalse(containsWord("myphone", "phone"));
        assertTrue(containsWord("phonenumber", "phone", true, true));
        assertTrue(containsWord("myphone", "phone", true, true));
        assertTrue(containsWord("phoneNumber", "phone"));

        assertTrue(containsWord("phoneNumber", "phone"));
        assertTrue(containsWord("@id/phoneNumber", "phone"));
        assertTrue(containsWord("@+id/phoneNumber", "phone"));
    }

    private static boolean containsWord(String name, String word, boolean allowPrefix,
            boolean allowSuffix) {
        return TextFieldDetector.containsWord(name, word, allowPrefix, allowSuffix);
    }

    private static boolean containsWord(String name, String word) {
        return TextFieldDetector.containsWord(name, word);
    }

    public void testIncremental1() throws Exception {
        assertEquals(""
            + "res/layout/note_edit2.xml:7: Warning: This text field does not specify an inputType or a hint [TextFields]\n"
            + "    <EditText\n"
            + "    ^\n"
            + "res/layout/note_edit2.xml:12: Warning: This text field does not specify an inputType or a hint [TextFields]\n"
            + "    <EditText\n"
            + "    ^\n"
            + "0 errors, 2 warnings\n",
            lintProjectIncrementally(
                    "res/layout/note_edit2.xml",
                    "res/layout/note_edit2.xml"));
    }

    public void testIncremental2() throws Exception {
        assertEquals("No warnings.",
            lintProjectIncrementally(
                    "res/layout/note_edit2.xml",
                    "res/layout/note_edit2.xml",
                    "res/values/styles-orientation.xml"));
    }
}
