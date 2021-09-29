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

package com.android.tools.lint.checks;

import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Detector;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("javadoc")
public class TypoLookupTest extends AbstractCheckTest {
    private static final String SEPARATOR = "->";

    public void testCapitalization() throws Exception {
        LintClient client = new TestLintClient();
        // Make sure it can be read in
        TypoLookup db = TypoLookup.get(client, "de", null);
        assertNotNull(db);
        assertNotNull(db.getTypos("Andriod".getBytes(Charsets.UTF_8), 0, "Andriod".length()));
    }

    public void testDictionary_English() throws Exception {
        validateDictionary("en");
    }

    public void testDictionary_German() throws Exception {
        validateDictionary("de");
    }

    public void testDictionary_Spanish() throws Exception {
        validateDictionary("es");
    }

    public void testDictionary_Hungarian() throws Exception {
        validateDictionary("hu");
    }

    public void testDictionary_Italian() throws Exception {
        validateDictionary("it");
    }

    public void testDictionary_Norwegian() throws Exception {
        validateDictionary("nb");
    }

    public void testDictionary_Portuguese() throws Exception {
        validateDictionary("pt");
    }

    public void testDictionary_Turkish() throws Exception {
        validateDictionary("tr");
    }

    public void test1() {
        TypoLookup db = TypoLookup.get(new TestLintClient(), "en", null);
        assertNull(db.getTypos("hello", 0, "hello".length()));
        assertNull(db.getTypos("this", 0, "this".length()));

        assertNotNull(db.getTypos("wiht", 0, "wiht".length()));
        assertNotNull(db.getTypos("woudl", 0, "woudl".length()));
        assertEquals("would", db.getTypos("woudl", 0, "woudl".length()).get(1));
        assertEquals("would", db.getTypos("  woudl  ", 2, 7).get(1));
        assertNotNull(db.getTypos("foo wiht bar", 4, 8));

        List<String> typos = db.getTypos("throught", 0, "throught".length());
        assertEquals("throught", typos.get(0)); // the typo
        assertEquals("thought", typos.get(1));
        assertEquals("through", typos.get(2));
        assertEquals("throughout", typos.get(3));

        // Capitalization handling
        assertNotNull(db.getTypos("Woudl", 0, "Woudl".length()));
        assertNotNull(db.getTypos("Enlish", 0, "Enlish".length()));
        assertNull(db.getTypos("enlish", 0, "enlish".length()));
        assertNull(db.getTypos("enlish".getBytes(Charsets.UTF_8), 0, "enlish".length()));
        assertNotNull(db.getTypos("ok", 0, "ok".length()));
        assertNotNull(db.getTypos("Ok", 0, "Ok".length()));
        assertNull(db.getTypos("OK", 0, "OK".length()));
    }

    public void testRegion() {
        TypoLookup db = TypoLookup.get(new TestLintClient(), "en", "US");
        assertNotNull(db.getTypos("wiht", 0, "wiht".length()));
        db = TypoLookup.get(new TestLintClient(), "en", "GB");
        assertNotNull(db.getTypos("wiht", 0, "wiht".length()));
    }

    public void test2() {
        TypoLookup db = TypoLookup.get(new TestLintClient(), "nb", null); //$NON-NLS-1$
        assertNull(db.getTypos("hello", 0, "hello".length()));
        assertNull(db.getTypos("this", 0, "this".length()));

        assertNotNull(db.getTypos("altid", 0, "altid".length()));
        assertEquals("alltid", db.getTypos("altid", 0, "altid".length()).get(1));
        assertEquals("alltid", db.getTypos("  altid  ", 2, 7).get(1));
        assertNotNull(db.getTypos("foo altid bar", 4, 9));

        // Test utf-8 string which isn't ASCII
        String s = "karriære";
        byte[] sb = s.getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(sb, 0, sb.length));

        assertEquals("karrière", db.getTypos(sb, 0, sb.length).get(1));
    }

    public void testMultiWords() {
        // Some language dictionaries contain multi-word sequences (e.g. where there's a
        // space on the left hand side). This needs some particular care in the lookup
        // which is usually word oriented.
        TypoLookup db = TypoLookup.get(new TestLintClient(), "de", "DE"); //$NON-NLS-1$

        // all zu->allzu

        // Text handling
        String t = "all zu";
        assertNotNull(db.getTypos(t, 0, t.length()));
        assertEquals("allzu", db.getTypos(t, 0, t.length()).get(1));

        // Byte handling
        byte[] text = "all zu".getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(text, 0, text.length));
        assertEquals("allzu", db.getTypos(text, 0, text.length).get(1));

        // Test automatically extending search beyond current word
        text = "all zu".getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(text, 0, 3));
        assertEquals("allzu", db.getTypos(text, 0, text.length).get(1));


        text = ") all zu (".getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(text, 2, 8));
        assertEquals("allzu", db.getTypos(text, 2, 8).get(1));

        text = "am einem".getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(text, 0, text.length));
        assertEquals("an einem", db.getTypos(text, 0, text.length).get(1));
    }

    public void testGlobbing() {
        TypoLookup db = TypoLookup.get(new TestLintClient(), "de", null);

        // Authorisierung*->Autorisierung*
        String text = "Authorisierungscode";
        byte[] bytes = text.getBytes(Charsets.UTF_8);

        assertNotNull(db.getTypos(text, 0, text.length()));
        assertEquals("Autorisierungscode", db.getTypos(text, 0, text.length()).get(1));
        assertEquals(text, db.getTypos(text, 0, text.length()).get(0));

        assertNotNull(db.getTypos(bytes, 0, bytes.length));
        assertEquals("Autorisierungscode", db.getTypos(bytes, 0, bytes.length).get(1));

        // befindet ein*->befindet sich ein*
        text = "wo befindet eine ip";
        assertEquals("befindet sich eine", db.getTypos(text, 3, 16).get(1));

        // zurück ge*->zurückge*
        text = "zurück gefoobaren";
        bytes = text.getBytes(Charsets.UTF_8);
        assertNotNull(db.getTypos(bytes, 0, bytes.length));
        assertEquals("zurückgefoobaren", db.getTypos(bytes, 0, bytes.length).get(1));
    }

    public void testComparisons() throws Exception {
        // Ensure that the two comparison methods agree

        LintClient client = new TestLintClient();
        for (String locale : new String[] { "de", "nb", "es", "en", "pt", "hu", "it", "tr" }) {
            File f = client.findResource(String.format("tools/support/typos-%1$s.txt", locale));
            assertTrue(locale, f != null && f.exists());

            Set<String> typos = new HashSet<String>(2000);
            List<String> lines = Files.readLines(f, Charsets.UTF_8);
            for (int i = 0, n = lines.size(); i < n; i++) {
                String line = lines.get(i);
                if (line.isEmpty() || line.trim().startsWith("#")) { //$NON-NLS-1$
                    continue;
                }

                int index = line.indexOf(SEPARATOR);
                if (index == -1) {
                    continue;
                }
                String typo = line.substring(0, index).trim();
                typos.add(typo);
           }

            List<String> words = new ArrayList<String>(typos);

            // Make sure that the two comparison methods agree on all the strings
            // (which should be in a semi-random order now that they're in a set ordered
            // by their hash codes)

            String prevText = words.get(0) + '\000';
            byte[] prevBytes = prevText.getBytes(Charsets.UTF_8);

            for (int i = 1; i < words.size(); i++) {
                String text = words.get(i) + '\000';
                byte[] bytes = text.getBytes(Charsets.UTF_8);

                int textCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, text, 0,
                        text.length());
                int byteCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, bytes, 0,
                        bytes.length);
                assertEquals("Word " + text + " versus prev " + prevText + " at " + i,
                        Math.signum(textCompare), Math.signum(byteCompare));
            }
        }
    }

    public void testComparison1() throws Exception {
        String prevText = "heraus gebracht\u0000";
        byte[] prevBytes = prevText.getBytes(Charsets.UTF_8);

        String text = "Päsident\u0000";
        byte[] bytes = text.getBytes(Charsets.UTF_8);


        int textCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, text, 0,
                text.length());
        int byteCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, bytes, 0,
                bytes.length);
        assertTrue(byteCompare < 0);
        assertTrue(textCompare < 0);
        assertEquals("Word " + text + " versus prev " + prevText,
                Math.signum(textCompare), Math.signum(byteCompare));
    }

    public void testComparison2() throws Exception {
        String prevText = "intepretation\u0000";
        byte[] prevBytes = prevText.getBytes(Charsets.UTF_8);

        String text = "Woudl\u0000";
        byte[] bytes = text.getBytes(Charsets.UTF_8);

        int textCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, text, 0, text.length());
        int byteCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, bytes, 0, bytes.length);
        assertTrue(byteCompare < 0);
        assertTrue(textCompare < 0);
        assertEquals("Word " + text + " versus prev " + prevText,
                Math.signum(textCompare), Math.signum(byteCompare));

        // Reverse capitalization and ensure that it's still the same
        prevText = "Intepretation\u0000";
        prevBytes = prevText.getBytes(Charsets.UTF_8);

        text = "woudl\u0000";
        bytes = text.getBytes(Charsets.UTF_8);

        textCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, text, 0, text.length());
        byteCompare = TypoLookup.compare(prevBytes, 0, (byte) 0, bytes, 0, bytes.length);
        assertTrue(byteCompare < 0);
        assertTrue(textCompare < 0);
        assertEquals("Word " + text + " versus prev " + prevText,
                Math.signum(textCompare), Math.signum(byteCompare));
    }

    // Some dictionaries contain actual sentences regarding usage; these must be stripped out.
    // They're just hardcoded here as we find them
    private static final String[] sRemove = new String[] {
        "- besser ganz darauf verzichten",
        "oft fälschlich für \"angekündigt\"",
        "hinausgehende* − insb. „darüber hinausgehende“",
        " - besser ganz darauf verzichten",
        "svw. bzw. so viel wie bzw. sprachverwandt"
    };

    private void validateDictionary(String locale) throws Exception {
        // Check that all the typo files are well formed
        LintClient client = new TestLintClient();
        File f = client.findResource(String.format("tools/support/typos-%1$s.txt", locale));
        assertTrue(locale, f != null && f.exists());

        Set<String> typos = new HashSet<String>(2000);
        List<Pattern> patterns = new ArrayList<Pattern>(100);

        List<String> lines = Files.readLines(f, Charsets.UTF_8);
        for (int i = 0, n = lines.size(); i < n; i++) {
            String line = lines.get(i);
            if (line.isEmpty() || line.trim().startsWith("#")) { //$NON-NLS-1$
                continue;
            }

            assertTrue(msg(f, i, "Line should contain '->': %1$s", line),
                    line.contains(SEPARATOR));
            int index = line.indexOf(SEPARATOR);
            String typo = line.substring(0, index).trim();
            String replacements = line.substring(index + SEPARATOR.length()).trim();

            if (typo.contains("*") && !typo.endsWith("*")) {
                fixDictionary(f);
                fail(msg(f, i, "Globbing (*) not supported anywhere but at the tail: %1$s", line));
            } else if (typo.contains("*") && !replacements.contains("*")) {
                fail(msg(f, i, "No glob found in the replacements for %1$s", line));
            }

            if (replacements.indexOf(',') != -1) {
                Set<String> seen = new HashSet<String>();
                for (String s : Splitter.on(',').omitEmptyStrings().split(replacements)) {
                    if (seen.contains(s)) {
                        fixDictionary(f);
                        fail(msg(f, i, "For typo " + typo
                                + " there are repeated replacements (" + s + "): " + line));
                    }
                }
            }

            assertTrue(msg(f, i, "Typo entry was empty: %1$s", line), !typo.isEmpty());
            assertTrue(msg(f, i, "Typo replacements was empty: %1$s", line),
                    !replacements.isEmpty());

            for (String blacklist : sRemove) {
                if (replacements.contains(blacklist)) {
                    fail(msg(f, i, "Replacements for typo %1$s contain description: %2$s",
                            typo, replacements));
                }
            }
            if (typo.equals("sólo") && locale.equals("es")) {
                // sólo->solo
                // This seems to trigger a lot of false positives
                fail(msg(f, i, "Typo %1$s triggers a lot of false positives, should be omitted",
                        typo));
            }
            if (locale.equals("tr") && (typo.equals("hiç bir")|| typo.equals("öğe"))) {
                // hiç bir->hiçbir
                // öğe->öge
                // According to a couple of native speakers these are not necessarily
                // typos
                fail(msg(f, i, "Typo %1$s triggers a lot of false positives, should be omitted",
                        typo));
            }

            if (typo.contains("*")) {
                patterns.add(Pattern.compile(typo.replace("*", ".*")));
            } else if (!patterns.isEmpty()) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(typo).matches()) {
                        fixDictionary(f);
                        fail(msg(f, i, "The typo " + typo + " matches an earlier glob: ignoring"));
                        continue;
                    }
                }
            }


            if (typos.contains(typo)) {
                fixDictionary(f);
                fail(msg(f, i, "Typo appeared more than once on lhs: %1$s", typo));
            }
            typos.add(typo);
        }

        // Make sure it can be read in
        TypoLookup db = TypoLookup.get(client, locale, null);
        assertNotNull(db);
        assertNull(db.getTypos("abcdefghijklmnopqrstuvxyz", 0, 25));
        assertNull(db.getTypos("abcdefghijklmnopqrstuvxyz".getBytes(Charsets.UTF_8), 0, 25));
        assertNotNull(db.getTypos("Andriod", 0, "Andriod".length()));
        assertNotNull(db.getTypos("Andriod".getBytes(Charsets.UTF_8), 0, "Andriod".length()));
    }

    private void fixDictionary(File original) throws Exception {
        File fixed = new File(original.getParentFile(), "fixed-" + original.getName());

        Map<String, Integer> typos = new HashMap<String, Integer>(2000);
        List<Pattern> patterns = new ArrayList<Pattern>(100);
        List<String> lines = Files.readLines(original, Charsets.UTF_8);
        List<String> output = new ArrayList<String>(lines.size());

        wordLoop:
        for (int i = 0, n = lines.size(); i < n; i++) {
            String line = lines.get(i);
            if (line.isEmpty() || line.trim().startsWith("#")) { //$NON-NLS-1$
                output.add(line);
                continue;
            }

            if (!line.contains(SEPARATOR)) {
                System.err.println("Commented out line missing ->: " + line);
                output.add("# " + line);
                continue;
            }
            int index = line.indexOf(SEPARATOR);
            String typo = line.substring(0, index).trim();
            String replacements = line.substring(index + SEPARATOR.length()).trim();

            if (typo.isEmpty()) {
                System.err.println("Commented out line missing a typo on the lhs: " + line);
                output.add("# " + line);
                continue;
            }
            if (replacements.isEmpty()) {
                System.err.println("Commented out line missing replacements on the rhs: " + line);
                output.add("# " + line);
                continue;
            }

            // Ensure that all the replacements are unique
            if (replacements.indexOf(',') != -1) {
                Set<String> seen = new HashSet<String>();
                List<String> out = new ArrayList<String>();
                boolean rewrite = false;
                for (String s : Splitter.on(',').omitEmptyStrings().split(replacements)) {
                    if (seen.contains(s)) {
                        System.err.println("For typo " + typo
                                + " there are repeated replacements (" + s + "): " + line);
                        rewrite = true;
                    }
                    seen.add(s);
                    out.add(s);
                }
                if (rewrite) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : out) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(s);
                    }
                    replacements = sb.toString();
                    line = typo + SEPARATOR + replacements;
                }
            }

            if (typo.contains("*")) {
                if (!typo.endsWith("*")) {
                    // Globbing not supported anywhere but the end
                    // Drop the whole word
                    System.err.println("Skipping typo " + typo
                            + " because globbing is only supported at the end of the word");
                    continue;
                }
                patterns.add(Pattern.compile(typo.replace("*", ".*")));
            } else if (replacements.contains("*")) {
                System.err.println("Skipping typo " + typo + " because unexpected " +
                        "globbing character found in replacements: "
                        + replacements);
                continue;
            } else if (!patterns.isEmpty()) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(typo).matches()) {
                        System.err.println("The typo " + typo
                                + " matches an earlier glob: ignoring");
                        continue wordLoop;
                    }
                }
            }

            // TODO: Strip whitespace around ->, prefix of # etc such that reading in
            // the databases needs to do less work at runtime

            if (typos.containsKey(typo)) {
                int l = typos.get(typo);
                String prev = output.get(l);
                assertTrue(prev.startsWith(typo));
                // Append new replacements and put back into the list
                // (unless they're already listed as replacements)
                Set<String> seen = new HashSet<String>();
                for (String s : Splitter.on(',').split(prev.substring(prev.indexOf(SEPARATOR)
                        + 2))) {
                    seen.add(s);
                }
                for (String s : Splitter.on(',').omitEmptyStrings().split(replacements)) {
                    if (!seen.contains(s)) {
                        prev = prev + "," + s;
                    }
                    seen.add(s);
                }
                output.set(l, prev);
            } else {
                typos.put(typo, output.size());
                output.add(line);
            }
        }

        Writer writer = new BufferedWriter(new FileWriter(fixed));
        for (String line : output) {
            writer.write(line);
            writer.write('\n');
        }
        writer.close();

        System.err.println("==> Wrote fixed typo file to " + fixed.getPath());
    }

    private static String msg(File file, int line, String message, Object... args) {
        return file.getName() + ':' + Integer.toString(line + 1) + ':' + ' ' +
                String.format(message, args);
    }

    @Override
    protected Detector getDetector() {
        fail("This is not used in the TypoLookupTest");
        return null;
    }
}
