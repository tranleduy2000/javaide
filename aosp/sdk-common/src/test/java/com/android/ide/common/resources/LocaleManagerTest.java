/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.ide.common.resources;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

@SuppressWarnings({"javadoc", "SizeReplaceableByIsEmpty"})
public class LocaleManagerTest extends TestCase {
    /** Enable to see the languages with multiple relevant regions */
    private static final boolean DUMP_INFERRED_REGIONS = false;

    public void testIntegrity() {
        // Make sure we get a name for every single region and language
        // and that (with the exception of the aliases) they are all unique

        // Make sure all the languages codes are lowercase, and that all the
        // region codes are upper case, and that nothing is empty
        // Make sure the region names have been capitalized
        List<String> languages = LocaleManager.getLanguageCodes(true);
        List<String> regions = LocaleManager.getRegionCodes(true);

        for (String language : languages) {
            assertFalse(language.isEmpty());
            assertTrue(language.length() == 2 || language.length() == 3);
            assertEquals(language.toLowerCase(Locale.US), language);
            String name = LocaleManager.getLanguageName(language);
            assertNotNull(name);
            assertFalse(name.isEmpty());
            assertTrue(name, name.length() >= 2 && Character.isUpperCase(name.charAt(0)));
        }

        for (String region : regions) {
            assertFalse(region.isEmpty());
            assertTrue(region.length() == 2 || region.length() == 3);
            assertEquals(region.toUpperCase(Locale.US), region);
            String name = LocaleManager.getRegionName(region);
            assertNotNull(name);
            assertFalse(name.isEmpty());
            assertTrue(name, name.length() > 2 && Character.isUpperCase(name.charAt(0)));
        }
    }

    public void testGetLanguageNames() throws Exception {
        assertEquals("English", LocaleManager.getLanguageName("en"));
        assertEquals("Norwegian Bokm\u00e5l", LocaleManager.getLanguageName("nb"));
        assertEquals("Norwegian", LocaleManager.getLanguageName("no"));
        assertEquals("French", LocaleManager.getLanguageName("fr"));
        assertEquals("German", LocaleManager.getLanguageName("de"));
        assertEquals("Hindi", LocaleManager.getLanguageName("hi"));

        // Check deprecated codes
        assertEquals("Indonesian", LocaleManager.getLanguageName("id"));
        assertEquals("Indonesian", LocaleManager.getLanguageName("in"));
        assertEquals("Indonesian", LocaleManager.getLanguageName("ind"));
        assertEquals("Hebrew", LocaleManager.getLanguageName("he"));
        assertEquals("Hebrew", LocaleManager.getLanguageName("iw"));
        assertEquals("Hebrew", LocaleManager.getLanguageName("heb"));
        assertEquals("Yiddish", LocaleManager.getLanguageName("yi"));
        assertEquals("Yiddish", LocaleManager.getLanguageName("ji"));
        assertEquals("Yiddish", LocaleManager.getLanguageName("yid"));

        // 3 letter language lookup
        assertEquals("English", LocaleManager.getLanguageName("eng"));
        assertEquals("Norwegian", LocaleManager.getLanguageName("nor"));
        assertEquals("French", LocaleManager.getLanguageName("fra"));
    }

    public void testGetRegionNames() {
        assertEquals("United States", LocaleManager.getRegionName("US"));
        assertEquals("Norway", LocaleManager.getRegionName("NO"));
        assertEquals("France", LocaleManager.getRegionName("FR"));
        assertEquals("India", LocaleManager.getRegionName("IN"));

        // 3 letter region lookup
        assertEquals("United States", LocaleManager.getRegionName("USA"));
        assertEquals("Norway", LocaleManager.getRegionName("NOR"));
        assertEquals("France", LocaleManager.getRegionName("FRA"));
    }

    public void testGetLanguageCodes() {
        for (String code : LocaleManager.getLanguageCodes(false)) {
            assertEquals(2, code.length());
        }
        for (String code : LocaleManager.getRegionCodes(false)) {
            assertEquals(2, code.length());
        }

        // Check languages including 3 letters: make sure we find both
        boolean found2 = false;
        boolean found3 = false;
        for (String code : LocaleManager.getLanguageCodes(true)) {
            int length = code.length();
            if (length == 3) {
                found3 = true;
            }
            if (length == 2) {
                found2 = true;
            }
            assertTrue(length == 2 || length == 3);
        }
        assertTrue(found2);
        assertTrue(found3);

        /* Turns out we don't have 3-letter regions imported yet
        found2 = false;
        found3 = false;
        for (String code : LocaleManager.getRegionCodes(true)) {
            int length = code.length();
            if (length == 3) {
                found3 = true;
            }
            if (length == 2) {
                found2 = true;
            }
            assertTrue(length == 2 || length == 3);
            if (found3) {
                System.out.println(code + " : " + LocaleManager.getRegionName(code));
            }
        }
        assertTrue(found2);
        assertTrue(found3);
        */
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void testGetRelevantRegions() {
        assertEquals(Arrays.asList("NO"), LocaleManager.getRelevantRegions("no"));
        assertEquals(Arrays.asList("NO", "SJ"), LocaleManager.getRelevantRegions("nb"));
        assertEquals(Arrays.asList("DK","GL"), LocaleManager.getRelevantRegions("da"));
        assertTrue(LocaleManager.getRelevantRegions("en").contains("US"));
        assertTrue(LocaleManager.getRelevantRegions("en").contains("GB"));

        // 3 letter lookup
        assertTrue(LocaleManager.getRelevantRegions("eng").contains("US"));
    }

    public void testGetTimeZoneRegion() {
        assertEquals("PT", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("Europe/Lisbon")));
        assertEquals("PT", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("Atlantic/Azores")));
        assertEquals("PT", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("Atlantic/Azores")));

        assertEquals("BR", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("America/Araguaina")));

        assertEquals("US", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("America/Adak")));
        assertEquals("US", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("America/Anchorage")));
        assertEquals("US", LocaleManager.getTimeZoneRegionAlpha2(TimeZone.getTimeZone("PST")));

        // Test JDK variations
        assertEquals("LY", LocaleManager.getTimeZoneRegionAlpha2(
                TimeZone.getTimeZone("Africa/Tripoli")));
        assertEquals("LY", LocaleManager.getTimeZoneRegionAlpha2(
                new SimpleTimeZone(3600000, "Africa/Tripoli")));
        assertEquals("LY", LocaleManager.getTimeZoneRegionAlpha2(
                new SimpleTimeZone(7200000, "Africa/Tripoli"))); // changed in jdk8
        assertNull(LocaleManager.getTimeZoneRegionAlpha2(new SimpleTimeZone(-42, "Africa/Tripoli"))); // wrong
    }

    public void testAlpha23Conversions() {
        assertEquals("nob", LocaleManager.getLanguageAlpha3("nb"));
        assertEquals("nb", LocaleManager.getLanguageAlpha2("nob"));
        assertEquals("NOR", LocaleManager.getRegionAlpha3("NO"));
        assertEquals("NO", LocaleManager.getRegionAlpha2("NOR"));

        // Check deprecated codes
        assertEquals("in", LocaleManager.getLanguageAlpha2("ind"));
        assertEquals("ind", LocaleManager.getLanguageAlpha3("in"));
        assertEquals("ind", LocaleManager.getLanguageAlpha3("id"));
        assertEquals("iw", LocaleManager.getLanguageAlpha2("heb"));
        assertEquals("heb", LocaleManager.getLanguageAlpha3("he"));
        assertEquals("heb", LocaleManager.getLanguageAlpha3("iw"));
        assertEquals("ji", LocaleManager.getLanguageAlpha2("yid"));
        assertEquals("yid", LocaleManager.getLanguageAlpha3("yi"));
        assertEquals("yid", LocaleManager.getLanguageAlpha3("ji"));
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetLanguageRegion() {
        Locale prevLocale = Locale.getDefault();
        TimeZone prevTimeZone = TimeZone.getDefault();
        try {
            assertFalse("The envvar $STUDIO_LOCALES should not be set during unit test runs",
                    System.getenv("STUDIO_LOCALES") != null);
            assertNull(System.getProperty("studio.locales"), System.getProperty("studio.locales"));
            Locale.setDefault(Locale.US);

            // Pick English=>GU based on options
            System.setProperty("studio.locales", "es_US, en_GU");
            assertEquals("GU", LocaleManager.getLanguageRegion("en"));

            System.setProperty("studio.locales", "es-rUS, en-rGU"); // alternate supported syntax
            assertEquals("GU", LocaleManager.getLanguageRegion("en"));
            System.setProperty("studio.locales", "");

            // Pick English=>GB based on timezone
            Locale.setDefault(Locale.ITALY);
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
            assertEquals("GB", LocaleManager.getLanguageRegion("en"));

            // Pick English=>CA based on system locale country
            Locale.setDefault(Locale.CANADA);
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
            assertEquals("CA", LocaleManager.getLanguageRegion("en"));

            // Swedish should map to Sweden even though FI is a relevant region for sv
            // Regression test for issue 136001
            Locale.setDefault(Locale.FRANCE); // unrelated locale to this test
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Helsinki"));
            assertEquals("SE", LocaleManager.getLanguageRegion("sv"));

            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
            assertEquals("GB", LocaleManager.getLanguageRegion("en"));
            TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
            assertEquals("US", LocaleManager.getLanguageRegion("en"));
            assertEquals("ES", LocaleManager.getLanguageRegion("es"));
            TimeZone.setDefault(TimeZone.getTimeZone("America/Manaus"));
            assertEquals("BR", LocaleManager.getLanguageRegion("pt"));
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Lisbon"));
            assertEquals("PT", LocaleManager.getLanguageRegion("pt"));
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
            assertEquals("PT", LocaleManager.getLanguageRegion("pt"));
        } finally {
            Locale.setDefault(prevLocale);
            TimeZone.setDefault(prevTimeZone);
        }
    }

    public void testGetTimeZoneRegionAll() {
        for (String id : TimeZone.getAvailableIDs()) {
            TimeZone zone = TimeZone.getTimeZone(id);
            assertNotNull(id, zone);
            String region = LocaleManager.getTimeZoneRegionAlpha2(zone);
            assertNotNull(region, zone.getID());
            region = LocaleManager.getTimeZoneRegionAlpha3(zone);
            assertNotNull(region, zone.getID());
        }
    }

    /* Utility useful for identifying strings which must be using \\u in the string names
     * to ensure that they are handled properly during the build (outside of Studio/Eclipse,
     * where this source file is marked as using UTF-8.
    public void testPrintable() {
        Set<String> languageCodes = LocaleManager.getLanguageCodes();
        for (String code : languageCodes) {
            String name = LocaleManager.getLanguageName(code);
            assertNotNull(name);
            checkEncoding(name);
        }
        Set<String> regionCodes = LocaleManager.getRegionCodes();
        for (String code : regionCodes) {
            String name = LocaleManager.getRegionName(code);
            assertNotNull(name);
            checkEncoding(name);
        }
    }

    private static void checkEncoding(String s) {
        String encoded = escape(s);
        if (!encoded.equals(s)) {
            System.out.println("Need unicode encoding for '" + s + "'");
            System.out.println(" Replacement=" + encoded);
        }
    }

    // Generates source code for region list sorted by region
    public void sortRegions() {
        final Map<String, String> map = LocaleManager.getRegionNamesMap();
        List<String> sorted = new ArrayList<String>(map.keySet());
        Collections.sort(sorted, new Comparator<String>() {
            @Override
            public int compare(String code1, String code2) {
                String region1 = map.get(code1);
                String region2 = map.get(code2);
                return region1.compareTo(region2);
            }
        });
        for (String code : sorted) {
            String region = map.get(code);
            String line = "         sRegionNames.put(\"" + code + "\", \"" + escape(region)
                    + "\");";
            System.out.print(line);
            for (int column = line.length(); column < 86; column++) {
                System.out.print(' ');
            }
            System.out.println("//$NON-NLS-1$");
        }
    }
    */
}
