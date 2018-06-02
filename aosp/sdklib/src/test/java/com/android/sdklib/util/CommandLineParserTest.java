/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.sdklib.util;

import com.android.utils.ILogger;
import com.android.utils.StdLogger;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;


public class CommandLineParserTest extends TestCase {

    private StdLogger mLog;

    /**
     * A mock version of the {@link CommandLineParser} class that does not
     * exits and captures its stdout/stderr output.
     */
    public static class MockCommandLineProcessor extends CommandLineParser {
        private boolean mExitCalled;
        private boolean mHelpCalled;
        private String mStdOut = "";
        private String mStdErr = "";

        public MockCommandLineProcessor(ILogger logger) {
            super(logger,
                  new String[][] {
                    { "verb1", "action1", "Some action" },
                    { "verb1", "action2", "Another action" },
                    { "verb2", NO_VERB_OBJECT, "Action with string array" },
            });
            define(Mode.STRING, false /*mandatory*/,
                    "verb1", "action1", "1", "first", "non-mandatory flag", null);
            define(Mode.STRING, true /*mandatory*/,
                    "verb1", "action1", "2", "second", "mandatory flag", null);

            define(Mode.STRING, true /*mandatory*/,
                    "verb2", NO_VERB_OBJECT, "1", "first", "1st mandatory flag", null);
            define(Mode.STRING_ARRAY, true /*mandatory*/,
                    "verb2", NO_VERB_OBJECT, "2", "second", "2nd mandatory flag", null);
            define(Mode.STRING, true /*mandatory*/,
                    "verb2", NO_VERB_OBJECT, "3", "third", "3rd mandatory flag", null);
        }

        @Override
        public void printHelpAndExitForAction(String verb, String directObject,
                String errorFormat, Object... args) {
            mHelpCalled = true;
            super.printHelpAndExitForAction(verb, directObject, errorFormat, args);
        }

        @Override
        protected void exit() {
            mExitCalled = true;
        }

        @Override
        protected void stdout(String format, Object... args) {
            String s = String.format(format, args);
            mStdOut += s + "\n";
            // don't call super to avoid printing stuff
        }

        @Override
        protected void stderr(String format, Object... args) {
            String s = String.format(format, args);
            mStdErr += s + "\n";
            // don't call super to avoid printing stuff
        }

        public boolean wasHelpCalled() {
            return mHelpCalled;
        }

        public boolean wasExitCalled() {
            return mExitCalled;
        }

        public String getStdOut() {
            return mStdOut;
        }

        public String getStdErr() {
            return mStdErr;
        }
    }

    @Override
    protected void setUp() throws Exception {
        mLog = new StdLogger(StdLogger.Level.VERBOSE);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPrintHelpAndExit() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertTrue(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().equals(""));
        c.printHelpAndExit(null);
        assertTrue(c.getStdOut().indexOf("-v") != -1);
        assertTrue(c.getStdOut().indexOf("--verbose") != -1);
        assertTrue(c.getStdErr().equals(""));
        assertTrue(c.wasExitCalled());

        c = new MockCommandLineProcessor(mLog);
        assertFalse(c.wasExitCalled());
        assertTrue(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().indexOf("Missing parameter") == -1);

        c.printHelpAndExit("Missing %s", "parameter");
        assertTrue(c.wasExitCalled());
        assertFalse(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().indexOf("Missing parameter") != -1);
    }

    public void testVerbose() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        assertFalse(c.isVerbose());
        c.parseArgs(new String[] { "-v" });
        assertTrue(c.isVerbose());
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing verb name.") != -1);

        c = new MockCommandLineProcessor(mLog);
        c.parseArgs(new String[] { "--verbose" });
        assertTrue(c.isVerbose());
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing verb name.") != -1);
    }

    public void testHelp() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        c.parseArgs(new String[] { "-h" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing verb name.") == -1);

        c = new MockCommandLineProcessor(mLog);
        c.parseArgs(new String[] { "--help" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing verb name.") == -1);
    }

    public void testMandatory() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        c.parseArgs(new String[] { "verb1", "action1", "-1", "value1", "-2", "value2" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals("value1", c.getValue("verb1", "action1", "first"));
        assertEquals("value2", c.getValue("verb1", "action1", "second"));

        c = new MockCommandLineProcessor(mLog);
        c.parseArgs(new String[] { "verb1", "action1", "-2", "value2" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals(null, c.getValue("verb1", "action1", "first"));
        assertEquals("value2", c.getValue("verb1", "action1", "second"));

        c = new MockCommandLineProcessor(mLog);
        c.parseArgs(new String[] { "verb1", "action1" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("must be defined") != -1);
        assertEquals(null, c.getValue("verb1", "action1", "first"));
        assertEquals(null, c.getValue("verb1", "action1", "second"));
    }

    public void testStringArray() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        c.parseArgs(new String[] { "verb2",
                                   "-1", "value1",
                                   "-2", "value2_a", "value2_b", "value2_c", "value2_d",
                                   "-3", "value3" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals("value1", c.getValue("verb2", null, "first"));
        assertTrue(c.getValue("verb2", null, "second") instanceof List<?>);
        assertEquals("[value2_a, value2_b, value2_c, value2_d]",
                     Arrays.toString(((List<?>) c.getValue("verb2", null, "second")).toArray()));
        assertEquals("value3", c.getValue("verb2", null, "third"));
    }

    public void testStringArray_DashDash() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        // Use -- to tell argument -2 it can absorb any argument, including dashed ones.
        // Logically -2 must be the last argument and -1/-3 must be placed before it.
        c.parseArgs(new String[] { "verb2",
                                   "-1", "value1",
                                   "-3", "value3",
                                   "-2", "value2_a", "--", "-value2_b", "--value2_c", "value2_d" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals("value1", c.getValue("verb2", null, "first"));
        assertTrue(c.getValue("verb2", null, "second") instanceof List<?>);
        assertEquals("[value2_a, --, -value2_b, --value2_c, value2_d]",
                     Arrays.toString(((List<?>) c.getValue("verb2", null, "second")).toArray()));
        assertEquals("value3", c.getValue("verb2", null, "third"));
    }

    public void testStringArray_EmptyStringArray() {
        MockCommandLineProcessor c = new MockCommandLineProcessor(mLog);

        c.parseArgs(new String[] { "verb2",
                                   "-1", "value1",
                                   "-2",
                                   "-3", "value3" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertEquals("Invalid usage for flag -2: No values provided.", c.getStdErr().trim());
    }
}
