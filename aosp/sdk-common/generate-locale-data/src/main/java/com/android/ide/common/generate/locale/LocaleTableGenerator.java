/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.ide.common.generate.locale;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import com.android.ide.common.resources.LocaleManager;
import com.ibm.icu.util.ULocale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code which generates the lookup tables in {@link LocaleManager}.
 */
public class LocaleTableGenerator {
    private static final boolean DEBUG = true;
    private static final boolean INCLUDE_TIMEZONE_COMMENTS = false;
    private static final int PADDING = 16;

    private Map<String, String> mLanguage3to2;
    private Map<String, String> mRegion3to2;
    private Map<String, String> mLanguage2to3;
    private Map<String, String> mRegion2to3;
    private Map<String, String> mRegionName;
    private Map<String, String> mLanguageName;
    private Multimap<String, String> mAssociatedRegions;
    private final List<String> mLanguage2Codes;
    private final List<String> mLanguage3Codes;
    private final List<String> mRegion2Codes;
    private final List<String> mRegion3Codes;
    private final Map<String, Integer> mLanguage2Index;
    private final Map<String, Integer> mLanguage3Index;
    private final Map<String, Integer> mRegion2Index;
    private final Map<String, Integer> mRegion3Index;
    private Map<String, List<String>> mRegionLists;
    @SuppressWarnings("StringBufferField") private StringBuilder mOutput;
    private Map<TimeZone, String> mZoneToRegion;
    private List<TimeZone> mZoneCandidates;

    public static void main(String[] args) {
        new LocaleTableGenerator().generate();
    }

    private void importLanguageSpec() {
        // Read in contents of http://www.loc.gov/standards/iso639-2/ISO-639-2_utf-8.txt
        byte[] bytes = new byte[0];
        try {
            HttpURLConnection connection = null;
            InputStream stream = LocaleTableGenerator.class.getResourceAsStream("ISO-639-2_utf-8.txt");
            if (stream == null) {
                // Try to fetch it remotely
                URL url = new URL("http://www.loc.gov/standards/iso639-2/ISO-639-2_utf-8.txt");
                connection = (HttpURLConnection) url.openConnection();
                stream = connection.getInputStream();
                if (stream == null) {
                    System.err.println("Couldn't find 639-2 spec; download from "
                            + "http://www.loc.gov/standards/iso639-2/ISO-639-2_utf-8.txt and place "
                            + "in source folder");
                    System.exit(-1);
                }
            }
            bytes = ByteStreams.toByteArray(stream);
            stream.close();
            if (connection != null) {
                connection.disconnect();
            }
        } catch (IOException e) {
            assert false;
        }
        String spec = new String(bytes, Charsets.UTF_8);
        if (spec.charAt(0) == '\ufeff') { // Strip off UTF-8 BOM
            spec = spec.substring(1);
        }
        int count3 = 0;
        int count2 = 0;
        int matched = 0;
        for (String line : Splitter.on('\n').trimResults().omitEmptyStrings().split(spec)) {
            String[] components = line.split("\\|");
            assert components.length == 5 : line;
            String iso2 = components[2];
            String iso3 = components[0];
            String languageName = components[3];
            assert iso3 != null : line;
            assert iso2 != null : line;
            assert languageName != null : line;
            if (iso3.contains("-") || languageName.contains("Reserved")) {
                // e.g. "qaa-qtz|||Reserved for local use|"
                continue;
            }
            assert iso3.length() == 3 : iso3;

            // 3 languages in that spec have deprecated codes which will not work right;
            // see LocaleFolderDetector#DEPRECATED_CODE for details
            if (iso2.equals("he")) {
                iso2 = "iw";
            } else if (iso2.equals("id")) {
                iso2 = "in";
            } else if (iso2.equals("yi")) {
                iso2 = "ji";
            }

            if (!iso2.isEmpty() && mLanguage2to3.containsKey(iso2)) {
                // We already know about this one
                matched++;
                continue;
            } else if ("zxx".equals(iso3)) {
                // "No linguistic content
                continue;
            }
            if (!iso2.isEmpty()) {
                assert iso2.length() == 2 : iso2;
                mLanguage2to3.put(iso2, iso3);
                count2++;
            }
            count3++;
            mLanguageName.put(iso3, languageName);
        }

        if (DEBUG) {
            System.out.println("Added in " + count3 + " extra codes, " + count2
                    + " of them for 2-letter codes, and " + matched
                    + " were ignored because we had ICU data.");
        }
    }

    private void importRegionSpec() {
        // Read in text contents of the table from http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
        byte[] bytes = new byte[0];
        try {
            HttpURLConnection connection;
            InputStream stream = LocaleTableGenerator.class.getResourceAsStream("ISO_3166-1_alpha-3.txt");
            if (stream == null) {
                // Try to fetch it remotely
                URL url = new URL("http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3");
                connection = (HttpURLConnection) url.openConnection();
                stream = connection.getInputStream();
                if (stream == null) {
                    System.err.println("Couldn't find 639-2 spec; download from "
                            + "http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3 and place "
                            + "in source folder as ISO_3166-1_alpha-3.txt");
                    System.exit(-1);
                }

                bytes = ByteStreams.toByteArray(stream);
                String spec = new String(bytes, Charsets.UTF_8);
                StringBuilder recreated = new StringBuilder(1000);
                String prev = null;
                Pattern CODE_PATTERN = Pattern.compile("<td.*><tt>(...)</tt></td>");
                // or <tt>ABW</tt></td>
                Pattern NAME_PATTERN = Pattern.compile("<td><a href=\"/wiki/(.+)\" title=\"(.+)\">(.+)</a></td>");
                for (String line : spec.split("\n")) {
                    if (prev != null && line.startsWith("<td><a href=")) {
                        Matcher matcher = NAME_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            Matcher matcherPrev = CODE_PATTERN.matcher(prev);
                            if (matcherPrev.matches()) {
                                recreated.append(matcherPrev.group(1)).append("\t").append(matcher.group(3)).append("\n");
                            }
                        }
                    }
                    prev = line;
                }

                stream.close();
                connection.disconnect();
                bytes = recreated.toString().getBytes(Charsets.UTF_8);
            } else {
                bytes = ByteStreams.toByteArray(stream);
                stream.close();
            }
        } catch (IOException e) {
            assert false;
        }
        String spec = new String(bytes, Charsets.UTF_8);
        if (spec.charAt(0) == '\ufeff') { // Strip off UTF-8 BOM
            spec = spec.substring(1);
        }
        int count3 = 0;
        int matched = 0;

        Map<String,String> regionNameToCode = Maps.newHashMap();
        for (Map.Entry<String,String> entry : mRegionName.entrySet()) {
            regionNameToCode.put(entry.getValue(), entry.getKey());
        }

        // Look at existing entries
        Map<String,String> regionNameToCode2 = Maps.newHashMap();
        for (String code : LocaleManager.getRegionCodes(true)) {
            regionNameToCode2.put(LocaleManager.getRegionName(code), code);
        }

        for (String line : Splitter.on('\n').trimResults().omitEmptyStrings().split(spec)) {
            String[] components = line.split("\\t");
            assert components.length == 2 : line;
            String iso3 = components[0];
            if (mRegionName.containsKey(iso3)) {
                matched++;
                continue;
            }
            String name = components[1];
            if (regionNameToCode.get(name) != null) {
                if (DEBUG) {
                    System.out.println("Found region " + name + " as "
                            + regionNameToCode.get(name));
                }
            } else {
                String iso2 = regionNameToCode2.get(name);
                if (iso2 != null) {
                    mRegionName.put(iso3, name);
                    mRegion2to3.put(iso2, iso3);
                    count3++;
                } else {
                    if (DEBUG) {
                        System.out.println("Unexpected name=" + name + ", code=" + iso3
                                + ", line=" + line);
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.println("Added in " + count3 + " extra codes, and " + matched
                    + " were ignored because we had ICU data.");
        }
    }

    public LocaleTableGenerator() {
        mLanguage2to3 = Maps.newHashMap();
        mRegion2to3 = Maps.newHashMap();
        mLanguageName = Maps.newHashMap();
        mRegionName = Maps.newHashMap();
        mAssociatedRegions = ArrayListMultimap.create();

        importFromIcu4j();

        // We've pulled data out of ICU, but still missing languages and regions; merge
        // in from the specs
        importLanguageSpec();
        importRegionSpec();

        // There are some macro-languages missing here!


        // UMN.49: http://en.wikipedia.org/wiki/UN_M.49
        populateUNM49();

        mLanguage3to2 = Maps.newHashMap();
        for (Map.Entry<String,String> entry : mLanguage2to3.entrySet()) {
            mLanguage3to2.put(entry.getValue(), entry.getKey());
        }
        mRegion3to2 = Maps.newHashMap();
        for (Map.Entry<String,String> entry : mRegion2to3.entrySet()) {
            mRegion3to2.put(entry.getValue(), entry.getKey());
        }

        // Register deprecated codes manually. This is done *after* we produce the
        // reverse map above, such that we make sure we don't accidentally have
        // the 3-letter new code map back and override the proper 3-letter code
        // with the deprecated code
        if (!mLanguageName.containsKey("ind")) {
            mLanguageName.put("ind", "Indonesian");
        }
        if (!mLanguageName.containsKey("yid")) {
            mLanguageName.put("ji", "Yiddish");
        }
        if (!mLanguageName.containsKey("heb")) {
            mLanguageName.put("iw", "Hebrew");
        }
        mLanguage2to3.put("in", "ind"); // proper 3-letter code, but NOT mapping back: should be id
        mLanguage2to3.put("ji", "yid"); // proper 3-letter code, but NOT mapping back: should be yi
        mLanguage2to3.put("iw", "heb"); // proper 3-letter code, but NOT mapping back: should be he
        // Make sure the forward map (from 3 to 2) is also using the primary (new) code
        mLanguage3to2.put("ind", "in");
        mLanguage3to2.put("heb", "iw");
        mLanguage3to2.put("yid", "ji");


        mLanguage2Codes = sorted(mLanguage2to3.keySet());
        mLanguage3Codes = sorted(mLanguageName.keySet());
        mRegion2Codes = sorted(mRegion2to3.keySet());
        mRegion3Codes = sorted(mRegionName.keySet());

        mLanguage2Index = Maps.newHashMap();
        for (int i = 0, n = mLanguage2Codes.size(); i < n; i++) {
            mLanguage2Index.put(mLanguage2Codes.get(i), i);
        }
        mLanguage3Index = Maps.newHashMap();
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            mLanguage3Index.put(mLanguage3Codes.get(i), i);
        }
        mRegion2Index = Maps.newHashMap();
        for (int i = 0, n = mRegion2Codes.size(); i < n; i++) {
            mRegion2Index.put(mRegion2Codes.get(i), i);
        }
        mRegion3Index = Maps.newHashMap();
        for (int i = 0, n = mRegion3Codes.size(); i < n; i++) {
            mRegion3Index.put(mRegion3Codes.get(i), i);
        }

        Set<String> known = Sets.newHashSet(LocaleManager.getLanguageCodes());
        known.removeAll(mLanguage2to3.keySet());
        if (!known.isEmpty()) {
            List<String> sorted = Lists.newArrayList(known);
            Collections.sort(sorted);
            System.out.println("Missing language registrations for " + sorted);
            assert false;
        }

        known = Sets.newHashSet(LocaleManager.getRegionCodes());
        known.removeAll(mRegion2to3.keySet());
        if (!known.isEmpty()) {
            List<String> sorted = Lists.newArrayList(known);
            Collections.sort(sorted);
            System.out.println("Missing region registrations for " + sorted);
            assert false;
        }

        // Compute list of ordered regions
        mRegionLists = Maps.newHashMap();
        for (String code : mLanguage3Codes) {
            Collection<String> regions = mAssociatedRegions.get(code);
            final String defaultRegion = getDefaultRegionFor(code);
            if (regions == null || regions.size() < 2) {
                if (regions != null && regions.size() == 1 && defaultRegion != null) {
                    assert regions.iterator().next().equals(defaultRegion);
                }
                if (regions != null && regions.size() == 1) {
                    mRegionLists.put(code, Collections.singletonList(regions.iterator().next()));
                } else if (defaultRegion != null) {
                    mRegionLists.put(code, Collections.singletonList(defaultRegion));
                }
            } else {
                final List<String> sorted = Lists.newArrayList(regions);
                Collections.sort(sorted, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int rank1 = o1.equals(defaultRegion) ? 0 : 1;
                        int rank2 = o2.equals(defaultRegion) ? 0 : 1;
                        int delta = rank1 - rank2;
                        if (delta == 0) {
                            delta = o1.compareTo(o2);
                            assert delta != 0 :
                                    "Found more than one occurrence of " + o1 + " in " + sorted;
                        }
                        return delta;
                    }
                });
                mRegionLists.put(code, sorted);
            }
        }
    }

    private void importFromIcu4j() {
        for (ULocale locale : ULocale.getAvailableLocales()) {
            String language2 = locale.getLanguage();
            String language3 = locale.getISO3Language();
            if (language3.isEmpty()) {
                // suspicious; skip this one. Misconfigured ICU?
                continue;
            }
            if (!language2.equals(language3)) {
                mLanguage2to3.put(language2, language3);
            }
            mLanguageName.put(language3, locale.getDisplayLanguage());

            String region3 = locale.getISO3Country();
            if (region3 != null && !region3.isEmpty()) {
                mRegionName.put(region3, locale.getDisplayCountry());
                String region2 = locale.getCountry();
                if (!region3.equals(region2)) {
                    mRegion2to3.put(region2, region3);
                }

                if (!mAssociatedRegions.containsEntry(language3, region3)) {
                    mAssociatedRegions.put(language3, region3);
                }
            }

            if (locale.getFallback() != null && !locale.getFallback().toString().isEmpty() &&
                     !locale.toString().startsWith(locale.getFallback().toString())) {
                System.out.println("Fallback for " + locale + " is " + locale.getFallback());
            }

            // TODO: Include this
            //locale.isRightToLeft()



            //System.out.println("Locale " + locale.toString() + "; " + locale.getISO3Language() + " : " + locale.getISO3Country() + " : " + locale.getDisplayLanguage() + ", " + locale.getDisplayCountry());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void registerMacroLanguage(String iso2, String iso3) {
        if (!mLanguage2to3.containsKey(iso2)) {
            assert mLanguageName.containsKey(iso3) : iso2;
            mLanguage2to3.put(iso2, iso3);
        }
    }

    private void populateUNM49() {
        // TODO: Populate region names from http://en.wikipedia.org/wiki/UN_M.49, e.g.
        // via something like the following (but we're not doing this, since ICU4J
        // can't provide actual region names:
        //        for (Region region : Region.getAvailable(Region.RegionType.CONTINENT)) {
        //            mRegionName.put(String.valueOf(region.getNumericCode()), region.toString());
        //        }
        // What we want here are codes like
        //  001 World
        //  002 Africa
        //  015 Northern Africa
        //  014 Eastern Africa
        // etc
    }

    private void generate() {
        String code1 = generateTimeZoneLookup();
        String code2 = generateLocaleTables();

        String code =
                "    // The remainder of this class is generated by generate-locale-data\n" +
                "    // DO NOT EDIT MANUALLY\n\n" +
                code1 + "\n" + code2;
        if (DEBUG) {
            int lines = 0;
            for (int i = 0, n = code.length(); i < n; i++) {
                if (code.charAt(i) == '\n') {
                    lines++;
                }
            }
            System.out.println("Generated " + lines + " lines.");
        }
        try {
            File tempFile = File.createTempFile("LocaleData", ".java");
            Files.write(code, tempFile, Charsets.UTF_8);
            System.out.println("Wrote updated locale data code fragment to " + tempFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            assert false;
        }
    }

    private String generateLocaleTables() {
        StringBuilder sb = new StringBuilder(5000);

        int level = 1;
        generateLanguageTables(sb, level);
        generateRegionTables(sb, level);
        generateRegionMappingTables(sb, level);
        generateAssertions(sb, level);

        if (DEBUG) {
            System.out.println("Number of languages=" + mLanguageName.size());
            System.out.println("Number of regions=" + mRegionName.size());
        }

        return sb.toString();
    }

    private void generateRegionMappingTables(StringBuilder sb, int level) {
        for (String code : mLanguage3Codes) {
            List<String> sorted = mRegionLists.get(code);
            if (sorted != null && sorted.size() > 1) {
                indent(sb, level);
                sb.append("// Language ").append(code).append(": ")
                        .append(Joiner.on(",").join(sorted)).append("\n");

                indent(sb, level);
                sb.append("private static final int[] ").append(getLanguageRegionFieldName(code))
                        .append(" = new int[] { ");
                boolean first = true;
                for (String region : sorted) {
                    Integer integer = mRegion3Index.get(region);
                    assert integer != null : region;
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(integer);
                }
                sb.append(" };\n");
            }
        }

        // Format preferred regions (multiple)
        sb.append("\n");
        indent(sb, level);
        sb.append("private static final int[][] LANGUAGE_REGIONS = new int[][] {\n");
        int column = 0;
        indent(sb, level + 1);
        int lineBegin = sb.length();
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            String code = mLanguage3Codes.get(i);
            List<String> sorted = mRegionLists.get(code);
            if (sorted != null && sorted.size() > 1) {
                sb.append(getLanguageRegionFieldName(code));
            } else {
                sb.append("null");
            }
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (sb.length() - lineBegin > 60) {
                sb.append("\n");
                lineBegin = sb.length();
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");


        // Format preferred region (just one)
        indent(sb, level);
        sb.append("private static final int[] LANGUAGE_REGION = new int[] {\n");
        column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            String iso3 = mLanguage3Codes.get(i);
            List<String> sorted = mRegionLists.get(iso3);
            Integer index = -1;
            if (sorted != null && !sorted.isEmpty()) {
                index = mRegion3Index.get(sorted.get(0));
            }
            append(sb, String.valueOf(index), 3, true);
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 8) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");
        sb.append("\n");
    }

    private static String getLanguageRegionFieldName(String languageCode) {
        assert languageCode.length() == 3 : languageCode;
        return "REGIONS_" + languageCode.toUpperCase(Locale.US);
    }

    private String getDefaultRegionFor(String languageCode) {
        assert languageCode.length() == 3 : languageCode;

        String iso2Language = getIso2Language(languageCode);
        String hardcoded = iso2Language != null ? LocaleManager.getDefaultLanguageRegion(
                iso2Language) : null;
        if (hardcoded != null) {
            if (getIso3Region(hardcoded) == null) {
                assert false : "Couldn't find an ISO 3 region for " + hardcoded + " with language code " + languageCode + " (region-2 is " + LocaleManager.getRegionName(hardcoded);
            }
            hardcoded = getIso3Region(hardcoded);
            assert hardcoded != null : languageCode;
        }

        Collection<String> regions = mAssociatedRegions.get(languageCode);
        if (regions != null && !regions.isEmpty()) {
            // Violated for example for gsw (Swiss German, with regions CHE, FRA, LIE)
            //assert iso2Language != null : languageCode + " didn't have an iso 2 code but does have regions " + regions; // We only have
            if (regions.size() == 1) {
                // Make
                String region = regions.iterator().next();
                assert hardcoded == null || region.equals(hardcoded) : region + " vs " + hardcoded;
                return region;
            }

            if (hardcoded == null || regions.contains(hardcoded)) {
                return hardcoded;
            } else {
                assert false : "Didn't find region " + hardcoded + " in expected regions " + regions + " for " + languageCode;
            }
        }

        return hardcoded;
    }

    private void generateAssertions(StringBuilder sb, int level) {
        indent(sb, level);
        sb.append("static {\n");
        level++;


        indent(sb, level);
        sb.append(
                "// These maps should have been generated programmatically; look for accidental edits\n");
        indent(sb, level);
        sb.append("assert ISO_639_2_CODES.length == ").append(mLanguage3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_639_2_NAMES.length == ").append(mLanguage3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_639_2_TO_1.length == ").append(mLanguage3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_639_1_CODES.length == ").append(mLanguage2Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_639_1_TO_2.length == ").append(mLanguage2Codes.size()).append(";\n");
        sb.append("\n");
        indent(sb, level);
        sb.append("assert ISO_3166_2_CODES.length == ").append(mRegion3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_3166_2_NAMES.length == ").append(mRegion3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_3166_2_TO_1.length == ").append(mRegion3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_3166_1_CODES.length == ").append(mRegion2Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert ISO_3166_1_TO_2.length == ").append(mRegion2Codes.size()).append(";\n");
        sb.append("\n");
        indent(sb, level);
        sb.append("assert LANGUAGE_REGION.length == ").append(mLanguage3Codes.size()).append(";\n");
        indent(sb, level);
        sb.append("assert LANGUAGE_REGIONS.length == ").append(mLanguage3Codes.size()).append(";\n");

        level--;
        indent(sb, level);
        sb.append("}\n");
    }

    @SuppressWarnings("UnusedDeclaration")
    private String getIso3Language(String iso2Code) {
        assert iso2Code.length() == 3 : iso2Code;
        return mLanguage2to3.get(iso2Code);
    }

    private String getIso2Language(String iso3Code) {
        assert iso3Code.length() == 3 : iso3Code + " was " + iso3Code.length() + " chars";
        return mLanguage3to2.get(iso3Code);
    }

    private String getIso3Region(String iso2Code) {
        assert iso2Code.length() == 2 : iso2Code;
        return mRegion2to3.get(iso2Code);
    }

    private String getIso2Region(String iso3Code) {
        assert iso3Code.length() == 3 : iso3Code;
        return mRegion3to2.get(iso3Code);
    }

    private void generateLanguageTables(StringBuilder sb, int level) {

        // Format ISO 3 code table
        indent(sb, level);
        sb.append("private static final String[] ISO_639_2_CODES = new String[] {\n");
        int column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            String code = mLanguage3Codes.get(i);
            sb.append('"').append(code).append('"');
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 9) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // ISO 3 language names
        indent(sb, level);
        sb.append("private static final String[] ISO_639_2_NAMES = new String[] {\n");
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            String code = mLanguage3Codes.get(i);
            String name = mLanguageName.get(code);
            assert name != null : code;
            indent(sb, level + 1);
            String literal = '"' + escape(name) + '"' + (i < n -1 ? "," : "");
            append(sb, literal, 40, false);
            sb.append("// Code ").append(code);
            String iso2 = getIso2Language(code);
            if (iso2 != null) {
                sb.append("/").append(iso2);
            }
            sb.append("\n");
        }
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // Format ISO 2 code table
        indent(sb, level);
        sb.append("private static final String[] ISO_639_1_CODES = new String[] {\n");
        column = 0;
        indent(sb, level + 1);

        for (int i = 0, n = mLanguage2Codes.size(); i < n; i++) {
            String code = mLanguage2Codes.get(i);
            sb.append('"').append(code).append('"');
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }

        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");
        sb.append("\n");

        // Format iso2 to iso3 mapping table
        indent(sb, level);
        sb.append("// Each element corresponds to an ISO 639-1 code, and contains the index\n");
        indent(sb, level);
        sb.append("// for the corresponding ISO 639-2 code\n");
        indent(sb, level);
        sb.append("private static final int[] ISO_639_1_TO_2 = new int[] {\n");
        column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mLanguage2Codes.size(); i < n; i++) {
            String iso2 = mLanguage2Codes.get(i);
            String iso3 = mLanguage2to3.get(iso2);
            int index = mLanguage3Index.get(iso3);
            append(sb, String.valueOf(index), 3, true);
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // Format iso3 to iso2 mapping table
        indent(sb, level);
        sb.append("// Each element corresponds to an ISO 639-2 code, and contains the index\n");
        indent(sb, level);
        sb.append("// for the corresponding ISO 639-1 code, or -1 if not represented\n");
        indent(sb, level);
        sb.append("private static final int[] ISO_639_2_TO_1 = new int[] {\n");
        column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mLanguage3Codes.size(); i < n; i++) {
            String iso3 = mLanguage3Codes.get(i);
            int index = -1;
            String iso2 = mLanguage3to2.get(iso3);
            if (iso2 != null) {
                index = mLanguage2Index.get(iso2);
            }
            append(sb, String.valueOf(index), 3, true);
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");
    }

    private void generateRegionTables(StringBuilder sb, int level) {
        // Format ISO 3 code table
        indent(sb, level);
        sb.append("private static final String[] ISO_3166_2_CODES = new String[] {\n");
        int column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mRegion3Codes.size(); i < n; i++) {
            String code = mRegion3Codes.get(i);
            sb.append('"').append(code).append('"');
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 9) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // ISO 3 language names
        indent(sb, level);
        sb.append("private static final String[] ISO_3166_2_NAMES = new String[] {\n");
        for (int i = 0, n = mRegion3Codes.size(); i < n; i++) {
            String code = mRegion3Codes.get(i);
            String name = mRegionName.get(code);
            assert name != null : code;
            indent(sb, level + 1);
            String literal = '"' + escape(name) + '"' + (i < n -1 ? "," : "");
            append(sb, literal, 40, false);
            sb.append("// Code ").append(code);
            String iso2 = getIso2Region(code);
            if (iso2 != null) {
                sb.append("/").append(iso2);
            }


            sb.append("\n");
        }
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // Format ISO 2 code table
        indent(sb, level);
        sb.append("private static final String[] ISO_3166_1_CODES = new String[] {\n");
        column = 0;
        indent(sb, level + 1);

        for (int i = 0, n = mRegion2Codes.size(); i < n; i++) {
            String code = mRegion2Codes.get(i);
            sb.append('"').append(code).append('"');
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");
        sb.append("\n");

        // Format iso2 to iso3 mapping table
        indent(sb, level);
        sb.append("// Each element corresponds to an ISO2 code, and contains the index\n");
        indent(sb, level);
        sb.append("// for the corresponding ISO3 code\n");
        indent(sb, level);
        sb.append("private static final int[] ISO_3166_1_TO_2 = new int[] {\n");
        column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mRegion2Codes.size(); i < n; i++) {
            String iso2 = mRegion2Codes.get(i);
            String iso3 = mRegion2to3.get(iso2);
            assert iso3 != null;
            int index = mRegion3Index.get(iso3);
            append(sb, String.valueOf(index), 3, true);
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");

        sb.append("\n");

        // Format iso3 to iso2 mapping table
        indent(sb, level);
        sb.append("// Each element corresponds to an ISO3 code, and contains the index\n");
        indent(sb, level);
        sb.append("// for the corresponding ISO2 code, or -1 if not represented\n");
        indent(sb, level);
        sb.append("private static final int[] ISO_3166_2_TO_1 = new int[] {\n");
        column = 0;
        indent(sb, level + 1);
        for (int i = 0, n = mRegion3Codes.size(); i < n; i++) {
            String iso3 = mRegion3Codes.get(i);
            int index = -1;
            String iso2 = mRegion3to2.get(iso3);
            if (iso2 != null) {
                index = mRegion2Index.get(iso2);
            }
            append(sb, String.valueOf(index), 3, true);
            if (i < n - 1) {
                sb.append(", ");
            }
            column++;
            if (column == 11) {
                column = 0;
                sb.append("\n");
                if (i < n - 1) {
                    indent(sb, level + 1);
                }
            }
        }
        sb.append("\n");
        indent(sb, level);
        sb.append("};\n");
    }

    private static void append(StringBuilder sb, String string, int width, boolean rhs) {
        if (!rhs) {
            sb.append(string);
        }
        for (int i = width - string.length(); i > 0; i--) {
            sb.append(' ');
        }
        if (rhs) {
            sb.append(string);
        }
    }

    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
    }

    private static String escape(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c >= 128) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0, m = s.length(); j < m; j++) {
                    char d = s.charAt(j);
                    if (d < 128) {
                        sb.append(d);
                    }
                    else {
                        sb.append('\\');
                        sb.append('u');
                        sb.append(String.format("%04x", (int) d));
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }


    private static <T extends Comparable> List<T> sorted(Collection<T> list) {
        List<T> sorted = Lists.newArrayList(list);
        Collections.sort(sorted);
        return sorted;
    }

    /** Produce a map from a region code to relevant timezones */
    private static Map<String, Set<TimeZone>> getRegionToZones() {
        Map<String, Set<TimeZone>> zones = new HashMap<String, Set<TimeZone>>();
        for (Locale locale : Locale.getAvailableLocales()) {
            String countryCode = locale.getCountry();
            if (countryCode.length() != 2) {
                continue;
            }
            Set<TimeZone> timezones = zones.get(countryCode);
            if (timezones == null) {
                timezones = Sets.newHashSet();
                zones.put(countryCode, timezones);
            }
            String[] ids = com.ibm.icu.util.TimeZone.getAvailableIDs(countryCode);
            for (String id : ids) {
                timezones.add(TimeZone.getTimeZone(id));
            }
        }
        return zones;
    }

    /** Produces an inverse mapping for the result from {@link #getRegionToZones()} */
    private static Map<TimeZone, String> getZoneToRegion(
            Map<String, Set<TimeZone>> regionToZones) {
        final Map<TimeZone, String> zoneToRegion = Maps.newHashMap();
        for (Map.Entry<String, Set<TimeZone>> entry : regionToZones.entrySet()) {
            String region = entry.getKey();
            for (TimeZone zone : entry.getValue()) {
                if (zone.getID().equals("GMT")) {
                    continue;
                }
                if (zoneToRegion.containsKey(zone) && !zoneToRegion.get(zone).equals(region)) {
                    System.err.println(
                            "Didn't expect multiple regions to have the same time zone: " +
                                    zone.getID() + " in both " + zoneToRegion.get(zone) +
                                    " and now " + region);
                    System.exit(-1);
                }
                zoneToRegion.put(zone, region);
            }
        }

        return zoneToRegion;
    }

    /** Removes timezones that reference regions directly in the id */
    private static List<TimeZone> filterCountryZones(Collection<TimeZone> zones) {
        // Generate lookup tree for regions

        Map<String,String> countryToRegionCode = Maps.newHashMap();
        for (String region : LocaleManager.getRegionCodes()) {
            countryToRegionCode.put(LocaleManager.getRegionName(region), region);
        }

        // Remove continent codes that are actually country names; these are
        // obvious later
        List<TimeZone> filtered = Lists.newArrayListWithExpectedSize(zones.size());
        for (TimeZone zone : zones) {
            String id = zone.getID();

            if (id.equals("GMT")) {
                // In some ICU database this is mapped to Taiwan; that's not right.
                continue;
            }

            boolean containsCountry = false;
            // Look in all elements of the timezone, e.g. we can have
            // Portugal, or Asia/Singapore, or America/Argentina/Mendoza
            for (String element : Splitter.on('/').trimResults().split(id)) {
                // Change '_' to ' ' such that we look up e.g. El Salvador, not El_Salvador
                String name = element.replace('_', ' ');
                if (countryToRegionCode.get(name) != null) {
                    containsCountry = true;
                    break;
                }
                if (element.equals("US")) {
                    // Region name is United States, but this really does map to a country code
                    containsCountry = true;
                    break;
                }

            }
            if (!containsCountry) {
                filtered.add(zone);
            }
        }

        return filtered;
    }

    private static int hashedId(TimeZone zone) {
        // Instead of String#hashCode, use this to ensure stable across platforms
        int h = 0;
        String id = zone.getID();
        for (int i = 0, n = id.length(); i < n; i++) {
            h = 31 * h + id.charAt(i);
        }
        return h;
    }

    private List<TimeZone> sortByRegion(Collection<TimeZone> zones) {
        List<TimeZone> sorted = Lists.newArrayList(zones);

        final Map<String,Integer> regionFrequency = Maps.newHashMap();
        for (TimeZone zone : zones) {
            String region = mZoneToRegion.get(zone);
            Integer frequency = regionFrequency.get(region);
            if (frequency == null) {
                regionFrequency.put(region, 1);
            } else {
                regionFrequency.put(region, frequency + 1);
            }
        }

        Collections.sort(sorted, new Comparator<TimeZone>() {
            // Sort such that the timezones are all sorted (alphabetically) grouped
            // by the same target region, and the regions in turn are sorted
            // by frequency
            @Override
            public int compare(TimeZone o1, TimeZone o2) {
                String r1 = mZoneToRegion.get(o1);
                String r2 = mZoneToRegion.get(o2);
                assert r1 != null : o1.getID();
                assert r2 != null : o2.getID();
                int delta = r1.compareTo(r2);
                if (delta == 0) { // Same region: compare alphabetically by id
                    return o1.getID().compareTo(o2.getID());
                } else {
                    // Different regions: compare by frequency
                    int f1 = regionFrequency.get(r1);
                    int f2 = regionFrequency.get(r2);
                    delta = f1 - f2;
                    if (delta == 0) {
                        delta = r1.compareTo(r2); // If same region frequency, alphabetical
                        if (delta != 0) {
                            // But sort empty strings last
                            if (r1.isEmpty()) {
                                return 1;
                            } else if (r2.isEmpty()) {
                                return -1;
                            }
                        }
                    }
                }

                return delta;
            }
        });
        return sorted;
    }

    public String generateTimeZoneLookup() {
        Map<String, Set<TimeZone>> regionToZones = getRegionToZones(); // ICU data
        Map<TimeZone, String> zoneToRegion = getZoneToRegion(regionToZones);

        // Make sure this is generated on a recent JDK which has a lot of
        // the new time zones
        String property = System.getProperty("java.version");
        assert property.startsWith("1.") : property;
        assert property.charAt(2) >= '8' : "Use a more recent JRE when generating data";

        List<TimeZone> candidates = Lists.newArrayList(zoneToRegion.keySet());

        mZoneToRegion = zoneToRegion;
        mZoneCandidates = candidates;
        mOutput = new StringBuilder(1000);

        // Generate lookup tree for regions
        final Multimap<String, TimeZone> mContinentMap = ArrayListMultimap.create();
        for (TimeZone zone : mZoneCandidates) {
            String id = zone.getID();
            int index = id.indexOf('/');
            String continent = "";
            if (index != -1) {
                continent = id.substring(0, index);
            }
            mContinentMap.put(continent, zone);
        }
        List<String> sortedContinents = Lists.newArrayList(mContinentMap.keySet());
        Collections.sort(sortedContinents, new Comparator<String>() {
            // Sort by decreasing timezone-per continent count, but put the
            // no-continent codes at the end
            @Override
            public int compare(String o1, String o2) {
                boolean e1 = o1.isEmpty();
                boolean e2 = o2.isEmpty();
                if (e1 && e2) {
                    return 0;
                } else if (e1) {
                    return 1;
                } else if (e2) {
                    return -1;
                } else {
                    int delta = mContinentMap.get(o2).size() - mContinentMap.get(o1).size();
                    if (delta != 0) {
                        return delta;
                    }
                    return o1.compareTo(o2);
                }
            }
        });

        int level = 1;

        if (INCLUDE_TIMEZONE_COMMENTS) {
            indent(mOutput, level);
            mOutput.append("@SuppressWarnings(\"SpellCheckingInspection\")\n");
        }
        indent(mOutput, level);
        mOutput.append("private static int getTimeZoneRegionIndex(@NonNull TimeZone zone) {\n");
        level++;

        indent(mOutput, level);
        mOutput.append("// Instead of String#hashCode, use this to ensure stable across platforms\n");
        indent(mOutput, level);
        mOutput.append("String id = zone.getID();\n");
        indent(mOutput, level);
        mOutput.append("int hashedId = 0;\n");
        indent(mOutput, level);
        mOutput.append("for (int i = 0, n = id.length(); i < n; i++) {\n");
        indent(mOutput, level + 1);
        mOutput.append("hashedId = 31 * hashedId + id.charAt(i);\n");
        indent(mOutput, level);
        mOutput.append("}\n");

        indent(mOutput, level);
        mOutput.append("switch (zone.getRawOffset()) {\n");

        Multimap<Integer,TimeZone> offsetMap = ArrayListMultimap.create();
        for (TimeZone zone : mZoneCandidates) {
            int rawOffset = zone.getRawOffset();
            offsetMap.put(rawOffset, zone);
        }
        makeJdkTimezoneCorrections(offsetMap);

        level++;
        for (Integer offset : sorted(offsetMap.keySet())) {
            indent(mOutput, level);
            mOutput.append("case ").append(offset).append(":");

            if (INCLUDE_TIMEZONE_COMMENTS) {
                mOutput.append(" // ");
                mOutput.append(String.valueOf(offset / 3600000.0));
                mOutput.append(" hours");
            }
            mOutput.append("\n");

            generateZones(level + 1, offsetMap.get(offset));
        }
        level--;

        indent(mOutput, level);
        mOutput.append("}\n");

        indent(mOutput, level);
        mOutput.append("return -1;\n");
        level--;
        indent(mOutput, level);
        mOutput.append("}\n");

        // Check that our lookup method not only always produces a region
        // (which is checked by testFindRegionByTimeZone), but also check
        // that it produces the *same* results as the ICU mappings!
        for (TimeZone zone : mZoneCandidates) {
            String region = LocaleManager.getTimeZoneRegionAlpha2(zone);
            String expected = zoneToRegion.get(zone);
            if (expected.isEmpty()) {
                continue;
            }
            assert expected.equals(region) : expected  + " vs " + region;
        }

        return mOutput.toString();
    }

    private void makeJdkTimezoneCorrections(Multimap<Integer,TimeZone> offsetMap) {
        // There have been some timezone corrections; since our switch-based lookup is
        // based on offsets, we should list these corrected timezones in both switch
        // blocks.
        //
        // For example, in JDK 6, the timezone "America/Rio_Branco" had rawOffset -14400000,
        // and in JDK 8 it's -18000000. Therefore, we should have switch statements for
        // both.

        // JDK 1.6 to JDK 1.7:
        correctOffset(offsetMap, "Africa/Tripoli", 3600000, 7200000);
        correctOffset(offsetMap, "Libya", 3600000, 7200000);
        correctOffset(offsetMap, "America/Argentina/San_Luis", -14400000, -10800000);
        correctOffset(offsetMap, "America/Eirunepe", -14400000, -10800000);
        correctOffset(offsetMap, "America/Porto_Acre", -14400000, -10800000);
        correctOffset(offsetMap, "America/Rio_Branco", -14400000, -10800000);
        correctOffset(offsetMap, "Brazil/Acre", -14400000, -10800000);
        //correctOffset(offsetMap, "Pacific/Fakaofo", 50400000, 46800000);

        // JDK 1.7 to JDK 1.8:
        correctOffset(offsetMap, "Europe/Simferopol", 7200000, 14400000);
        //correctId(offsetMap, "Asia/Riyadh87", "Mideast/Riyadh87", 11224000);
        //correctId(offsetMap, "Asia/Riyadh88", "Mideast/Riyadh88", 11224000);
        //correctId(offsetMap, "Asia/Riyadh89", "Mideast/Riyadh89", 11224000);

        // JDK 1.8 to JDK 1. EA:
        //correctId(offsetMap, null, "Asia/Chita", 28800000);
        //correctId(offsetMap, null, "Asia/Srednekolymsk", 39600000);
        //correctId(offsetMap, null, "Pacific/Bougainville", 39600000);
        correctOffset(offsetMap, "Asia/Irkutsk", 32400000, 28800000);
        correctOffset(offsetMap, "Asia/Kashgar", 28800000, 21600000);
        correctOffset(offsetMap, "Asia/Khandyga", 36000000, 32400000);
        correctOffset(offsetMap, "Asia/Krasnoyarsk", 28800000, 25200000);
        correctOffset(offsetMap, "Asia/Magadan", 43200000, 36000000);
        correctOffset(offsetMap, "Asia/Novosibirsk", 25200000, 21600000);
        correctOffset(offsetMap, "Asia/Omsk", 25200000, 21600000);
        correctOffset(offsetMap, "Asia/Sakhalin", 39600000, 36000000);
        correctOffset(offsetMap, "Asia/Urumqi", 28800000, 21600000);
        correctOffset(offsetMap, "Asia/Ust-Nera", 39600000, 36000000);
        correctOffset(offsetMap, "Asia/Vladivostok", 39600000, 36000000);
        correctOffset(offsetMap, "Asia/Yakutsk", 36000000, 32400000);
        correctOffset(offsetMap, "Asia/Yekaterinburg", 21600000, 18000000);
        correctOffset(offsetMap, "Europe/Kaliningrad", 10800000, 7200000);
        correctOffset(offsetMap, "Europe/Moscow", 14400000, 10800000);
        correctOffset(offsetMap, "Europe/Simferopol", 14400000, 10800000);
        correctOffset(offsetMap, "Europe/Volgograd", 14400000, 10800000);
        correctOffset(offsetMap, "W-SU", 14400000, 10800000);

        correctRegion(offsetMap, "Europe/Simferopol", 7200000, 14400000, 10800000, "");
    }

    private void correctOffset(
            Multimap<Integer, TimeZone> offsetMap,
            String id,
            int rawOffset1,
            int rawOffset2) {
        TimeZone old = findZone(id);
        if (old == null) {
            // Not relevant for our usage
            return;
        }
        TimeZone zone1 = findZone(offsetMap, id, rawOffset1);
        if (zone1 == null) {
            addZone(offsetMap, id, rawOffset1, old);
        }
        TimeZone zone2 = findZone(offsetMap, id, rawOffset2);
        if (zone2 == null) {
            addZone(offsetMap, id, rawOffset2, old);
        }
    }

    private void correctRegion(
            Multimap<Integer, TimeZone> offsetMap,
            String id,
            int rawOffset1,
            int rawOffset2,
            int rawOffset3,
            String region) {
        TimeZone zone1 = findZone(offsetMap, id, rawOffset1);
        if (zone1 != null) {
            mZoneToRegion.put(zone1, region);
        }
        TimeZone zone2 = findZone(offsetMap, id, rawOffset2);
        if (zone2 != null) {
            mZoneToRegion.put(zone2, region);
        }
        TimeZone zone3 = findZone(offsetMap, id, rawOffset3);
        if (zone3 != null) {
            mZoneToRegion.put(zone3, region);
        }
    }

    private void addZone(Multimap<Integer, TimeZone> offsetMap, String id, int rawOffset,
            TimeZone old) {
        TimeZone zone = new SimpleTimeZone(rawOffset, id);
        offsetMap.put(rawOffset, zone);
        mZoneCandidates.add(zone);
        mZoneToRegion.put(zone, mZoneToRegion.get(old));
    }

    private void correctId(
            Multimap<Integer, TimeZone> offsetMap,
            String id1,
            String id2,
            int rawOffset) {
        if (id1 != null) {
            TimeZone zone1 = findZone(offsetMap, id1, rawOffset);
            if (zone1 == null && id2 != null) {
                TimeZone old = findZone(id2);
                addZone(offsetMap, id1, rawOffset, old);
            }
        }
        if (id2 != null) {
            TimeZone zone2 = findZone(offsetMap, id2, rawOffset);
            if (zone2 == null && id1 != null) {
                TimeZone old = findZone(id1);
                addZone(offsetMap, id2, rawOffset, old);
            }
        }
    }

    private TimeZone findZone(String id) {
        for (TimeZone zone : mZoneCandidates) {
            if (zone.getID().equals(id)) {
                return zone;
            }
        }

        return null;
    }

    private static TimeZone findZone(Multimap<Integer, TimeZone> offsetMap,
            String id, int rawOffset) {
        Collection<TimeZone> zones = offsetMap.get(rawOffset);
        if (zones == null) {
            return null;
        }

        for (TimeZone zone : zones) {
            if (id.equals(zone.getID())) {
                return zone;
            }
        }

        return null;
    }

    private void generateZones(int level, Collection<TimeZone> zones) {
        assert !zones.isEmpty();

        // See if they all map to the same region
        boolean regionsDiffer = false;
        String sameRegion = mZoneToRegion.get(zones.iterator().next());
        for (TimeZone zone : zones) {
            String region = mZoneToRegion.get(zone);
            if (!sameRegion.equals(region)) {
                regionsDiffer = true;
                break;
            }
        }
        if (!regionsDiffer) {
            returnRegion(zones, level);
            return;
        }

        indent(mOutput, level);
        mOutput.append("switch (hashedId) {\n");
        level++;

        Map<Integer,TimeZone> hashCodes = Maps.newHashMap();
        List<TimeZone> sorted = sortByRegion(zones);
        String lastRegion = mZoneToRegion.get(sorted.get(zones.size() - 1));
        for (int i = 0, n = sorted.size(); i < n; i++) {
            TimeZone zone = sorted.get(i);
            int hash = hashedId(zone);
            if (hashCodes.containsKey(hash)) {
                System.err.println("Timezones clash: same hash " + hash + " for "
                        + zone.getID() + " and "
                        + hashCodes.get(hash));
                System.exit(-1);
            }

            String region = mZoneToRegion.get(zone);

            if (i < n - 1 && region.equals(lastRegion)) {
                // TODO: Combine into a list instead
                if (INCLUDE_TIMEZONE_COMMENTS) {
                    indent(mOutput, level);
                    pad(7);
                    mOutput.append("// ").append(escape(zone.getID()));
                    mOutput.append("\n");
                }
                continue;
            } else if (i == n - 1) {
                indent(mOutput, level);
                mOutput.append("default:\n");
            } else {
                indent(mOutput, level);
                mOutput.append("case ").append(hash).append(":");
                String text = String.valueOf(hash);
                pad(text);

                if (INCLUDE_TIMEZONE_COMMENTS) {
                    mOutput.append(" // ").append(escape(zone.getID()));
                }
                mOutput.append("\n");
            }
            if (i < n - 1 && region.equals(mZoneToRegion.get(sorted.get(i + 1)))) {
                // Don't return each one; share a single body when the regions are the same
                continue;
            }
            returnRegion(zone, level + 1);
        }

        level--;
        indent(mOutput, level);
        mOutput.append("}\n");
    }

    private void pad(int space) {
        int padding = PADDING + space;
        for (int j = 0; j < padding; j++) {
            mOutput.append(' ');
        }
    }

    private void pad(String text) {
        int padding = PADDING - text.length();
        for (int j = 0; j < padding; j++) {
            mOutput.append(' ');
        }
    }

    private void returnRegion(Collection<TimeZone> zones, int level) {
        String region = mZoneToRegion.get(zones.iterator().next());
        List<String> ids = Lists.newArrayList();
        for (TimeZone zone : zones) {
            ids.add(zone.getID());
            assert region.equals(mZoneToRegion.get(zone)) : zone + " vs " + region;
        }
        indent(mOutput, level);
        if (region.isEmpty()) {
            mOutput.append("return -1;\n");
            return;
        }
        int index = mRegion3Index.get(getIso3Region(region));
        mOutput.append("return ").append(index).append(";");
        if (INCLUDE_TIMEZONE_COMMENTS) {
            pad(-9);
            mOutput.append("// ").append(escape(LocaleManager.getRegionName(region)));
        }
        mOutput.append("\n");
    }

    private void returnRegion(TimeZone zone, int level) {
        returnRegion(Collections.singletonList(zone), level);
    }
}
