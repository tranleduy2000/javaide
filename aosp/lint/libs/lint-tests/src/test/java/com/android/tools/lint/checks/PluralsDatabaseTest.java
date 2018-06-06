/*
 * Copyright (C) 2014 The Android Open Source Project
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

import static com.android.tools.lint.checks.PluralsDatabase.FLAG_FEW;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_MANY;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_MULTIPLE_ONE;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_MULTIPLE_TWO;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_MULTIPLE_ZERO;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_ONE;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_TWO;
import static com.android.tools.lint.checks.PluralsDatabase.FLAG_ZERO;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity.few;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity.many;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity.one;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity.two;
import static com.android.tools.lint.checks.PluralsDatabase.Quantity.zero;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.resources.LocaleManager;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PluralsDatabaseTest extends TestCase {
    public void testGetRelevant() {
        PluralsDatabase db = PluralsDatabase.get();
        assertNull(db.getRelevant("unknown"));
        EnumSet<Quantity> relevant = db.getRelevant("en");
        assertNotNull(relevant);
        assertEquals(1, relevant.size());
        assertSame(Quantity.one, relevant.iterator().next());

        relevant = db.getRelevant("cs");
        assertNotNull(relevant);
        assertEquals(EnumSet.of(Quantity.few, Quantity.one), relevant);
    }

    public void testFindExamples() {
        PluralsDatabase db = PluralsDatabase.get();

        //noinspection ConstantConditions
        assertEquals("1, 101, 201, 301, 401, 501, 601, 701, 1001, \u2026",
                db.findIntegerExamples("sl", Quantity.one));

        assertEquals("1, 21, 31, 41, 51, 61, 71, 81, 101, 1001, \u2026",
                db.findIntegerExamples("ru", Quantity.one));
    }

    public void testHasMultiValue() {
        PluralsDatabase db = PluralsDatabase.get();

        assertFalse(db.hasMultipleValuesForQuantity("en", Quantity.one));
        assertFalse(db.hasMultipleValuesForQuantity("en", Quantity.two));
        assertFalse(db.hasMultipleValuesForQuantity("en", Quantity.few));
        assertFalse(db.hasMultipleValuesForQuantity("en", Quantity.many));

        assertTrue(db.hasMultipleValuesForQuantity("br", Quantity.two));
        assertTrue(db.hasMultipleValuesForQuantity("mk", Quantity.one));
        assertTrue(db.hasMultipleValuesForQuantity("lv", Quantity.zero));
    }

    /**
     * If the lint unit test data/ folder contains a plurals.txt database file,
     * this test will parse that file and ensure that our current database produces
     * exactly the same results as those inferred from the file. If not, it will
     * dump out updated data structures for the database.
     */
    public void testDatabaseAccurate() {
        List<String> languages = new ArrayList<String>(LocaleManager.getLanguageCodes());
        Collections.sort(languages);
        PluralsTextDatabase db = PluralsTextDatabase.get();
        db.ensureInitialized();

        if (db.getSetName("en") == null) {
            // plurals.txt not found
            System.out.println("No plurals.txt database included; not checking consistency");
            return;
        }

        // Ensure that the two databases (the plurals.txt backed one and our actual
        // database) fully agree on everything
        PluralsDatabase pdb = PluralsDatabase.get();
        for (String language : languages) {
            if (!Objects.equal(pdb.getRelevant(language), db.getRelevant(language))) {
                dumpDatabaseTables();
                assertEquals(language, pdb.getRelevant(language), db.getRelevant(language));
            }
            if (db.getSetName(language) == null) {
                continue;
            }
            for (Quantity q : Quantity.values()) {
                boolean mv1 = pdb.hasMultipleValuesForQuantity(language, q);
                boolean mv2 = db.hasMultipleValuesForQuantity(language, q);
                if (mv1 != mv2) {
                    dumpDatabaseTables();
                    assertEquals(language, mv1, mv2);
                }
                if (mv2) {
                    String e1 = pdb.findIntegerExamples(language, q);
                    String e2 = db.findIntegerExamples(language, q);
                    if (!Objects.equal(e1, e2)) {
                        dumpDatabaseTables();
                        assertEquals(language, e1, e2);
                    }
                }
            }
        }
    }

    private static void dumpDatabaseTables() {
        List<String> languages = new ArrayList<String>(LocaleManager.getLanguageCodes());
        Collections.sort(languages);
        PluralsTextDatabase db = PluralsTextDatabase.get();
        db.ensureInitialized();

        db.getRelevant("en"); // ensure initialized
        Map<String,String> languageMap = Maps.newHashMap();
        Map<String,EnumSet<Quantity>> setMap = Maps.newHashMap();
        for (String language : languages) {
            String set = db.getSetName(language);
            if (set == null) {
                continue;
            }
            EnumSet<Quantity> quantitySet = db.getRelevant(language);
            if (quantitySet == null) {
                // No plurals data for this language. For example, in ICU 52, no
                // plurals data for the "nv" language (Navajo).
                continue;
            }
            assertNotNull(language, quantitySet);
            setMap.put(set, quantitySet);
            languageMap.put(set, language); // Could be multiple
        }

        List<String> setNames = Lists.newArrayList(setMap.keySet());
        Collections.sort(setNames);

        // Compute uniqueness
        Map<String,String> sameAs = Maps.newHashMap();
        for (int i = 0, n = setNames.size(); i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                String iSetName = setNames.get(i);
                String jSetName = setNames.get(j);
                assertNotNull(iSetName);
                assertNotNull(jSetName);
                EnumSet<Quantity> iSet = setMap.get(iSetName);
                EnumSet<Quantity> jSet = setMap.get(jSetName);
                assertNotNull(iSet);
                assertNotNull(jSet);
                if (iSet.equals(jSet)) {
                    String alias = sameAs.get(iSetName);
                    if (alias != null) {
                        iSetName = alias;
                    }
                    sameAs.put(jSetName, iSetName);
                    break;
                }
            }
        }

        final String indent = "    ";
        StringBuilder sb = new StringBuilder();

        // Multi Value Set names
        Set<String> sets = Sets.newHashSet();
        for (String language : languages) {
            String set = db.getSetName(language);
            sets.add(set);
            languageMap.put(set, language); // Could be multiple
        }

        Map<String,Integer> indices = Maps.newTreeMap();
        int index = 0;
        for (String set : setNames) {
            indices.put(set, index++);
        }

        // Language indices
        Map<String,Integer> languageIndices = Maps.newTreeMap();
        index = 0;
        for (String language : languages) {
            String set = db.getSetName(language);
            if (set == null) {
                continue;
            }

            languageIndices.put(language, index++);
        }

        Map<String, String> zero = computeExamples(db, Quantity.zero, sets, languageMap);
        Map<String, String> one = computeExamples(db, Quantity.one, sets, languageMap);
        Map<String, String> two = computeExamples(db, Quantity.two, sets, languageMap);

        // Language map
        sb.setLength(0);
        sb.append("/** Set of language codes relevant to plurals data */\n");
        sb.append("private static final String[] LANGUAGE_CODES = new String[] {\n");
        int column = 0;
        index = 0;
        sb.append(indent);
        for (String language : languages) {
            String set = db.getSetName(language);
            if (set == null) {
                continue;
            }
            sb.append('"').append(language).append("\", ");
            column++;
            if (column == 10) {
                sb.append("\n");
                sb.append(indent);
                column = 0;
            }
            assertEquals((int)languageIndices.get(language), index);
            index++;
        }
        sb.append("\n};\n");
        System.out.println(sb);

        // Quantity map
        sb.setLength(0);
        sb.append("/**\n"
                + " * Relevant flags for each language (corresponding to each language listed\n"
                + " * in the same position in {@link #LANGUAGE_CODES})\n"
                + " */\n");
        sb.append("private static final int[] FLAGS = new int[] {\n");
        column = 0;
        sb.append(indent);
        index = 0;
        for (String language : languages) {
            String setName = db.getSetName(language);
            if (setName == null) {
                continue;
            }
            assertEquals((int)languageIndices.get(language), index);

            // Compute flag
            int flag = 0;
            EnumSet<Quantity> relevant = db.getRelevant(language);
            assertNotNull(relevant);
            if (relevant.contains(Quantity.zero)) {
                flag |= FLAG_ZERO;
            }
            if (relevant.contains(Quantity.one)) {
                flag |= FLAG_ONE;
            }
            if (relevant.contains(Quantity.two)) {
                flag |= FLAG_TWO;
            }
            if (relevant.contains(Quantity.few)) {
                flag |= FLAG_FEW;
            }
            if (relevant.contains(Quantity.many)) {
                flag |= FLAG_MANY;
            }
            if (zero.containsKey(setName)) {
                flag |= FLAG_MULTIPLE_ZERO;
            }
            if (one.containsKey(setName)) {
                flag |= FLAG_MULTIPLE_ONE;
            }
            if (two.containsKey(setName)) {
                flag |= FLAG_MULTIPLE_TWO;
            }

            sb.append(String.format(Locale.US, "0x%04x, ", flag));
            column++;
            if (column == 8) {
                sb.append("\n");
                sb.append(indent);
                column = 0;
            }

            index++;
        }
        sb.append("\n};\n");
        System.out.println(sb);

        // Switch statement methods for examples
        printSwitch(db, Quantity.zero, languages, languageIndices, indices, zero);
        printSwitch(db, Quantity.one, languages, languageIndices, indices, one);
        printSwitch(db, Quantity.two, languages, languageIndices, indices, two);

    }

    private static Map<String, String> computeExamples(PluralsTextDatabase db, Quantity quantity,
            Set<String> sets, Map<String, String> languageMap) {

        Map<String, String> setsWithExamples = Maps.newHashMap();
        for (String set : sets) {
            String language = languageMap.get(set);
            String examples = db.findIntegerExamples(language, quantity);
            if (examples != null && examples.indexOf(',') != -1) {
                setsWithExamples.put(set, examples);
            }
        }

        return setsWithExamples;
    }


    private static void printSwitch(
            PluralsTextDatabase db,
            Quantity quantity,
            List<String> languages,
            Map<String,Integer> languageIndices,
            Map<String, Integer> indices,
            Map<String, String> setsWithExamples) {

        List<String> sorted = new ArrayList<String>(setsWithExamples.keySet());
        Collections.sort(sorted);

        StringBuilder sb = new StringBuilder();
        String quantityName = quantity.name();
        quantityName = Character.toUpperCase(quantityName.charAt(0)) + quantityName.substring(1);
        sb.append("    @Nullable\n"
                + "    private static String getExampleForQuantity").append(quantityName)
                               .append("(@NonNull String language) {\n"
                                       + "        int index = getLanguageIndex(language);\n"
                                       + "        switch (index) {\n");

        for (Map.Entry<String, Integer> entry : indices.entrySet()) {
            String set = entry.getKey();
            if (!setsWithExamples.containsKey(set)) {
                continue;
            }
            String example = setsWithExamples.get(set);
            example = example.replace("…", "\\u2026");
            sb.append("            // ").append(set).append("\n");
            for (String language : languages) {
                String setName = db.getSetName(language);
                if (set.equals(setName)) {
                    int languageIndex = languageIndices.get(language);
                    sb.append("            case ");
                    sb.append(languageIndex).append(": // ").append(language).append("\n");
                }
            }

            sb.append("                return ");
            sb.append("\"").append(example).append("\"");
            sb.append(";\n");
        }

        sb.append("            case -1:\n"
                + "            default:\n"
                + "                return null;\n"
                + "        }\n"
                + "    }\n");

        System.out.println(sb);
    }

    /**
     * Plurals database backed by a plurals.txt file from ICU
     */
    private static class PluralsTextDatabase {
        private static final boolean DEBUG = false;
        private static final EnumSet<Quantity> NONE = EnumSet.noneOf(Quantity.class);

        private static final PluralsTextDatabase sInstance = new PluralsTextDatabase();

        private Map<String, EnumSet<Quantity>> mPlurals;
        private Map<Quantity, Set<String>> mMultiValueSetNames = Maps.newEnumMap(Quantity.class);
        private String mDescriptions;
        private int mRuleSetOffset;
        private Map<String,String> mSetNamePerLanguage;

        @NonNull
        public static PluralsTextDatabase get() {
            return sInstance;
        }

        @Nullable
        public EnumSet<Quantity> getRelevant(@NonNull String language) {
            ensureInitialized();
            EnumSet<Quantity> set = mPlurals.get(language);
            if (set == null) {
                String s = getLocaleData(language);
                if (s == null) {
                    mPlurals.put(language, NONE);
                    return null;
                }
                // Process each item and look for relevance

                set = EnumSet.noneOf(Quantity.class);
                int length = s.length();
                for (int offset = 0, end; offset < length; offset = end + 1) {
                    for (; offset < length; offset++) {
                        if (!Character.isWhitespace(s.charAt(offset))) {
                            break;
                        }
                    }

                    int begin = s.indexOf('{', offset);
                    if (begin == -1) {
                        break;
                    }
                    end = findBalancedEnd(s, begin);
                    if (end == -1) {
                        end = length;
                    }

                    if (s.startsWith("other{", offset)) {
                        // Not included
                        continue;
                    }

                    // Make sure the rule references applies to integers:
                    // Rule definition mentions n or i or @integer
                    //
                    //    n  absolute value of the source number (integer and decimals).
                    //    i  integer digits of n.
                    //    v  number of visible fraction digits in n, with trailing zeros.
                    //    w  number of visible fraction digits in n, without trailing zeros.
                    //    f  visible fractional digits in n, with trailing zeros.
                    //    t  visible fractional digits in n, without trailing zeros.
                    boolean appliesToIntegers = false;
                    boolean inQuotes = false;
                    for (int i = begin + 1; i < end - 1; i++) {
                        char c = s.charAt(i);
                        if (c == '"') {
                            inQuotes = !inQuotes;
                        } else if (inQuotes) {
                            if (c == '@') {
                                if (s.startsWith("@integer", i)) {
                                    appliesToIntegers = true;
                                    break;
                                } else {
                                    // @decimal always comes after @integer
                                    break;
                                }
                            } else if ((c == 'i' || c == 'n') && Character
                                    .isWhitespace(s.charAt(i + 1))) {
                                appliesToIntegers = true;
                                break;
                            }
                        }
                    }

                    if (!appliesToIntegers) {
                        if (DEBUG) {
                            System.out.println("Skipping quantity " + s.substring(offset, begin)
                                    + " in set for locale " + language + " (" + getSetName(language)
                                    + ")");
                        }
                        continue;
                    }

                    if (s.startsWith("one{", offset)) {
                        set.add(one);
                    } else if (s.startsWith("few{", offset)) {
                        set.add(few);
                    } else if (s.startsWith("many{", offset)) {
                        set.add(many);
                    } else if (s.startsWith("two{", offset)) {
                        set.add(two);
                    } else if (s.startsWith("zero{", offset)) {
                        set.add(zero);
                    } else {
                        // Unexpected quantity: ignore
                        if (DEBUG) {
                            assert false : s.substring(offset, Math.min(offset + 10, length));
                        }
                    }
                }

                mPlurals.put(language, set);
            }
            return set == NONE ? null : set;
        }

        public boolean hasMultipleValuesForQuantity(
                @NonNull String language,
                @NonNull Quantity quantity) {
            if (quantity == Quantity.one || quantity == Quantity.two || quantity == Quantity.zero) {
                ensureInitialized();
                String setName = getSetName(language);
                if (setName != null) {
                    Set<String> names = mMultiValueSetNames.get(quantity);
                    assert names != null : quantity;
                    return names.contains(setName);
                }
            }

            return false;
        }

        private void ensureInitialized() {
            if (mPlurals == null) {
                initialize();
            }
        }

        @SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
        private void initialize() {
            // Sets where more than a single integer maps to the quantity. Take for example
            // set 10:
            //    set10{
            //        one{
            //            "n % 10 = 1 and n % 100 != 11 @integer 1, 21, 31, 41, 51, 61, 71, 81,"
            //            " 101, 1001, … @decimal 1.0, 21.0, 31.0, 41.0, 51.0, 61.0, 71.0, 81.0"
            //            ", 101.0, 1001.0, …"
            //        }
            //    }
            // Here we see that both "1" and "21" will match the "one" category.
            // Note that this only applies to integers (since getQuantityString only takes integer)
            // whereas the plurals data also covers fractions. I was not sure what to do about
            // set17:
            //    set17{
            //        one{"i = 0,1 and n != 0 @integer 1 @decimal 0.1~1.6"}
            //    }
            // since it looks to me like this only differs from 1 in the fractional part.
            //
            // This is encoded by looking at the rules; this is done by the unit test
            // testDeriveMultiValueSetNames() (which ensures that the set is correct and if
            // not computes the correct set of set names to use for the current plurals.txt
            // database.

            mMultiValueSetNames = Maps.newEnumMap(Quantity.class);
            mMultiValueSetNames.put(Quantity.two, Sets.newHashSet("set21", "set22", "set30", "set32"));
            mMultiValueSetNames.put(Quantity.one, Sets.newHashSet(
                    "set1", "set11", "set12", "set13", "set14", "set2", "set20",
                    "set21", "set22", "set26", "set27", "set29", "set30", "set32", "set5",
                    "set6"));
            mMultiValueSetNames.put(Quantity.zero, Sets.newHashSet("set14"));

            mSetNamePerLanguage = Maps.newHashMapWithExpectedSize(20);
            mPlurals = Maps.newHashMapWithExpectedSize(20);
        }

        @Nullable
        public String findIntegerExamples(@NonNull String language, @NonNull Quantity quantity) {
            String data = getQuantityData(language, quantity);
            if (data != null) {
                int index = data.indexOf("@integer");
                if (index == -1) {
                    return null;
                }
                int start = index + "@integer".length();
                int end = data.indexOf('@', start);
                if (end == -1) {
                    end = data.length();
                }
                return data.substring(start, end).trim();
            }

            return null;
        }


        @NonNull
        private String getPluralsDescriptions() {
            if (mDescriptions == null) {
                InputStream stream = PluralsDatabaseTest.class.getResourceAsStream("data/plurals.txt");
                if (stream != null) {
                    try {
                        byte[] bytes = ByteStreams.toByteArray(stream);
                        mDescriptions = new String(bytes, Charsets.UTF_8);
                        mRuleSetOffset = mDescriptions.indexOf("rules{");
                        if (mRuleSetOffset == -1) {
                            if (DEBUG) {
                                assert false;
                            }
                            mDescriptions = "";
                            mRuleSetOffset = 0;
                        }

                    } catch (IOException e) {
                        try {
                            stream.close();
                        } catch (IOException e1) {
                            // Stupid API.
                        }
                    }
                }
                if (mDescriptions == null) {
                    mDescriptions = "";
                }
            }
            return mDescriptions;
        }

        @Nullable
        public String getQuantityData(@NonNull String language, @NonNull Quantity quantity) {
            String data = getLocaleData(language);
            if (data == null) {
                return null;
            }
            String quantityDeclaration = quantity.name() + "{";
            int quantityStart = data.indexOf(quantityDeclaration);
            if (quantityStart == -1) {
                return null;
            }
            int quantityEnd = findBalancedEnd(data, quantityStart);
            if (quantityEnd == -1) {
                return null;
            }
            //String s = data.substring(quantityStart + quantityDeclaration.length(), quantityEnd);
            StringBuilder sb = new StringBuilder();
            boolean inString = false;
            for (int i = quantityStart + quantityDeclaration.length(); i < quantityEnd; i++) {
                char c = data.charAt(i);
                if (c == '"') {
                    inString = !inString;
                } else if (inString) {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        @Nullable
        public String getSetName(@NonNull String language) {
            String name = mSetNamePerLanguage.get(language);
            if (name == null) {
                name = findSetName(language);
                if (name == null) {
                    name = ""; // Store "" instead of null so we remember search result
                }
                mSetNamePerLanguage.put(language, name);
            }

            return name.isEmpty() ? null : name;
        }

        @Nullable
        private String findSetName(@NonNull String language) {
            String data = getPluralsDescriptions();
            int index = data.indexOf("locales{");
            if (index == -1) {
                return null;
            }
            int end = data.indexOf("locales_ordinals{", index + 1);
            if (end == -1) {
                return null;
            }
            String languageDeclaration = " " + language + "{\"";
            index = data.indexOf(languageDeclaration);
            if (index == -1 || index >= end) {
                return null;
            }
            int setEnd = data.indexOf('\"', index + languageDeclaration.length());
            if (setEnd == -1) {
                return null;
            }
            return data.substring(index + languageDeclaration.length(), setEnd).trim();
        }

        @Nullable
        public String getLocaleData(@NonNull String language) {
            String set = getSetName(language);
            if (set == null) {
                return null;
            }
            String data = getPluralsDescriptions();
            int setStart = data.indexOf(set + "{", mRuleSetOffset);
            if (setStart == -1) {
                return null;
            }
            int setEnd = findBalancedEnd(data, setStart);
            if (setEnd == -1) {
                return null;
            }
            return data.substring(setStart + set.length() + 1, setEnd);
        }

        private static int findBalancedEnd(String data, int offset) {
            int balance = 0;
            int length = data.length();
            for (; offset < length; offset++) {
                char c = data.charAt(offset);
                if (c == '{') {
                    balance++;
                } else if (c == '}') {
                    balance--;
                    if (balance == 0) {
                        return offset;
                    }
                }
            }

            return -1;
        }
    }
}