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

package com.android.ide.common.resources.configuration;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

import java.util.Iterator;
import java.util.Locale;

/**
 * A locale qualifier, which can be constructed from:
 * <ul>
 *     <li>A plain 2-letter language descriptor</li>
 *     <li>A 2-letter language descriptor followed by a -r 2 letter region qualifier</li>
 *     <li>A plain 3-letter language descriptor</li>
 *     <li>A 3-letter language descriptor followed by a -r 2 letter region qualifier</li>
 *     <li>A BCP 47 language tag. The BCP-47 tag uses + instead of - as separators,
 *         and has the prefix b+. Therefore, the BCP-47 tag "zh-Hans-CN" would be
 *         written as "b+zh+Hans+CN" instead.</li>
 * </ul>
 */
public final class LocaleQualifier extends ResourceQualifier {
    public static final String FAKE_VALUE = "__"; //$NON-NLS-1$
    public static final String NAME = "Locale";
    // TODO: Case insensitive check!
    public static final String BCP_47_PREFIX = "b+"; //$NON-NLS-1$

    @NonNull private String mFull;
    @NonNull private String mLanguage;
    @Nullable private String mRegion;
    @Nullable private String mScript;

    public LocaleQualifier() {
        mFull = "";
    }

    public LocaleQualifier(@NonNull String language) {
        assert language.length() == 2 || language.length() == 3;
        mLanguage = language;
        mFull = language;
    }

    public LocaleQualifier(@Nullable String full, @NonNull String language,
                           @Nullable String region, @Nullable String script) {
        if (full == null) {
            if (region != null && region.length() == 3 || script != null) {
                StringBuilder sb = new StringBuilder(BCP_47_PREFIX);
                sb.append(language);
                if (region != null) {
                    sb.append('+');
                    sb.append(region);
                }
                if (script != null) {
                    sb.append('+');
                    sb.append(script);
                }
                full = sb.toString();
            } else if (region != null) {
                full = language + "-r" + region;
            } else {
                full = language;
            }
        }
        mFull = full;
        mLanguage = language;
        mRegion = region;
        mScript = script;
    }

    public static boolean isRegionSegment(@NonNull String segment) {
        return (segment.startsWith("r") || segment.startsWith("R")) && segment.length() == 3
                && Character.isLetter(segment.charAt(0)) && Character.isLetter(segment.charAt(1));
    }

    /**
     * Creates and returns a qualifier from the given folder segment. If the segment is incorrect,
     * <code>null</code> is returned.
     * @param segment the folder segment from which to create a qualifier.
     * @return a new {@link LocaleQualifier} object or <code>null</code>
     */
    @Nullable
    public static LocaleQualifier getQualifier(@NonNull String segment) {
        int length = segment.length();
        if (length == 2
                && Character.isLetter(segment.charAt(0))
                && Character.isLetter(segment.charAt(1))) { // to make sure we don't match e.g. "v4"
            segment = segment.toLowerCase(Locale.US);
            return new LocaleQualifier(segment, segment, null, null);
        } else if (length == 3
                && Character.isLetter(segment.charAt(0))
                && Character.isLetter(segment.charAt(1))
                && Character.isLetter(segment.charAt(2))) {
            segment = segment.toLowerCase(Locale.US);
            if ("car".equals(segment)) {
                // Special case: "car" is a valid 3 letter language code, but
                // it conflicts with the (much older) UI mode constant for
                // car dock mode, so this specific language string should not be recognized
                // as a 3 letter language string; it should match car dock mode instead.
                return null;
            }
            return new LocaleQualifier(segment, segment, null, null);
        } else if (segment.startsWith(BCP_47_PREFIX)) {
            return parseBcp47(segment);
        } else if (length == 6 && segment.charAt(2) == '-'
                && Character.toLowerCase(segment.charAt(3)) == 'r'
                && Character.isLetter(segment.charAt(0))
                && Character.isLetter(segment.charAt(1))
                && Character.isLetter(segment.charAt(4))
                && Character.isLetter(segment.charAt(5))) {
            String language = new String(new char[] {
                    Character.toLowerCase(segment.charAt(0)),
                    Character.toLowerCase(segment.charAt(1))
            });
            String region = new String(new char[] {
                    Character.toUpperCase(segment.charAt(4)),
                    Character.toUpperCase(segment.charAt(5))
            });

            return new LocaleQualifier(language + "-r" + region, language, region, null);
        } else if (length == 7 && segment.charAt(3) == '-'
                && Character.toLowerCase(segment.charAt(4)) == 'r'
                && Character.isLetter(segment.charAt(0))
                && Character.isLetter(segment.charAt(1))
                && Character.isLetter(segment.charAt(2))
                && Character.isLetter(segment.charAt(5))
                && Character.isLetter(segment.charAt(6))) {
            String language = new String(new char[] {
                    Character.toLowerCase(segment.charAt(0)),
                    Character.toLowerCase(segment.charAt(1)),
                    Character.toLowerCase(segment.charAt(2))
            });
            String region = new String(new char[] {
                    Character.toUpperCase(segment.charAt(5)),
                    Character.toUpperCase(segment.charAt(6))
            });
            return new LocaleQualifier(language + "-r" + region, language, region, null);
        }
        return null;
    }

    /** Given a BCP-47 string, normalizes the case to the recommended casing */
    @NonNull
    public static String normalizeCase(@NonNull String segment) {
        /* According to the BCP-47 spec:
           o  [ISO639-1] recommends that language codes be written in lowercase
              ('mn' Mongolian).

           o  [ISO15924] recommends that script codes use lowercase with the
              initial letter capitalized ('Cyrl' Cyrillic).

           o  [ISO3166-1] recommends that country codes be capitalized ('MN'
              Mongolia).


           An implementation can reproduce this format without accessing the
           registry as follows.  All subtags, including extension and private
           use subtags, use lowercase letters with two exceptions: two-letter
           and four-letter subtags that neither appear at the start of the tag
           nor occur after singletons.  Such two-letter subtags are all
           uppercase (as in the tags "en-CA-x-ca" or "sgn-BE-FR") and four-
           letter subtags are titlecase (as in the tag "az-Latn-x-latn").
         */
        if (isNormalizedCase(segment)) {
            return segment;
        }

        StringBuilder sb = new StringBuilder(segment.length());
        if (segment.startsWith(BCP_47_PREFIX)) {
            sb.append(BCP_47_PREFIX);
            assert segment.startsWith(BCP_47_PREFIX);
            int segmentBegin = BCP_47_PREFIX.length();
            int segmentLength = segment.length();
            int start = segmentBegin;

            int lastLength = -1;
            while (start < segmentLength) {
                if (start != segmentBegin) {
                    sb.append('+');
                }
                int end = segment.indexOf('+', start);
                if (end == -1) {
                    end = segmentLength;
                }
                int length = end - start;
                if ((length != 2 && length != 4) || start == segmentBegin || lastLength == 1) {
                    for (int i = start; i < end; i++) {
                        sb.append(Character.toLowerCase(segment.charAt(i)));
                    }
                } else if (length == 2) {
                    for (int i = start; i < end; i++) {
                        sb.append(Character.toUpperCase(segment.charAt(i)));
                    }
                } else {
                    assert length == 4 : length;
                    sb.append(Character.toUpperCase(segment.charAt(start)));
                    for (int i = start + 1; i < end; i++) {
                        sb.append(Character.toLowerCase(segment.charAt(i)));
                    }
                }

                lastLength = length;
                start = end + 1;
            }
        } else if (segment.length() == 6) {
            // Language + region: ll-rRR
            sb.append(Character.toLowerCase(segment.charAt(0)));
            sb.append(Character.toLowerCase(segment.charAt(1)));
            sb.append(segment.charAt(2)); // -
            sb.append(Character.toLowerCase(segment.charAt(3))); // r
            sb.append(Character.toUpperCase(segment.charAt(4)));
            sb.append(Character.toUpperCase(segment.charAt(5)));
        } else if (segment.length() == 7) {
            // Language + region: lll-rRR
            sb.append(Character.toLowerCase(segment.charAt(0)));
            sb.append(Character.toLowerCase(segment.charAt(1)));
            sb.append(Character.toLowerCase(segment.charAt(2)));
            sb.append(segment.charAt(3)); // -
            sb.append(Character.toLowerCase(segment.charAt(4))); // r
            sb.append(Character.toUpperCase(segment.charAt(5)));
            sb.append(Character.toUpperCase(segment.charAt(6)));
        } else {
            sb.append(segment.toLowerCase(Locale.US));
        }

        return sb.toString();
    }

    /**
     * Given a BCP-47 string, determines whether the string is already
     * capitalized correctly (where "correct" means for readability; all strings
     * should be compared case insensitively)
     */
    @VisibleForTesting
    static boolean isNormalizedCase(@NonNull String segment) {
        if (segment.startsWith(BCP_47_PREFIX)) {
            assert segment.startsWith(BCP_47_PREFIX);
            int segmentBegin = BCP_47_PREFIX.length();
            int segmentLength = segment.length();
            int start = segmentBegin;

            int lastLength = -1;
            while (start < segmentLength) {
                int end = segment.indexOf('+', start);
                if (end == -1) {
                    end = segmentLength;
                }
                int length = end - start;
                if ((length != 2 && length != 4) || start == segmentBegin || lastLength == 1) {
                    if (isNotLowerCase(segment, start, end)) {
                        return false;
                    }
                } else if (length == 2) {
                    if (isNotUpperCase(segment, start, end)) {
                        return false;
                    }
                } else {
                    assert length == 4 : length;
                    if (isNotUpperCase(segment, start, start + 1)) {
                        return false;
                    }
                    if (isNotLowerCase(segment, start + 1, end)) {
                        return false;
                    }
                }

                lastLength = length;
                start = end + 1;
            }

            return true;
        } else if (segment.length() == 2) {
            // Just a language: ll
            return Character.isLowerCase(segment.charAt(0))
                    && Character.isLowerCase(segment.charAt(1));
        } else if (segment.length() == 3) {
            // Just a language: lll
            return Character.isLowerCase(segment.charAt(0))
                    && Character.isLowerCase(segment.charAt(1))
                    && Character.isLowerCase(segment.charAt(2));
        } else if (segment.length() == 6) {
            // Language + region: ll-rRR
            return Character.isLowerCase(segment.charAt(0))
                    && Character.isLowerCase(segment.charAt(1))
                    && Character.isLowerCase(segment.charAt(3))
                    && Character.isUpperCase(segment.charAt(4))
                    && Character.isUpperCase(segment.charAt(5));
        } else if (segment.length() == 7) {
            // Language + region: lll-rRR
            return Character.isLowerCase(segment.charAt(0))
                    && Character.isLowerCase(segment.charAt(1))
                    && Character.isLowerCase(segment.charAt(2))
                    && Character.isLowerCase(segment.charAt(4))
                    && Character.isUpperCase(segment.charAt(5))
                    && Character.isUpperCase(segment.charAt(6));
        }

        return true;
    }

    private static boolean isNotLowerCase(@NonNull String segment, int start, int end) {
        for (int i = start; i < end; i++) {
            if (Character.isUpperCase(segment.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isNotUpperCase(@NonNull String segment, int start, int end) {
        for (int i = start; i < end; i++) {
            if (Character.isLowerCase(segment.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    public String getValue() {
        return mFull;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return NAME;
    }

    @Override
    public int since() {
        // This was added in Lollipop, but you can for example write b+en+US and aapt handles it
        // compatibly so we don't want to normalize this in normalize() to append -v21 etc
        return 1;
    }

    @Override
    public boolean isValid() {
        //noinspection StringEquality
        return mFull != FAKE_VALUE;
    }

    @Override
    public boolean hasFakeValue() {
        //noinspection StringEquality
        return mFull == FAKE_VALUE;
    }

    public boolean hasLanguage() {
        return !FAKE_VALUE.equals(mLanguage);
    }

    public boolean hasRegion() {
        return mRegion != null && !FAKE_VALUE.equals(mRegion);
    }

    @Override
    public boolean checkAndSet(@NonNull String value, @NonNull FolderConfiguration config) {
        LocaleQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setLocaleQualifier(qualifier);
            return true;
        }

        return false;
    }

    /**
     * Used only when constructing the qualifier, don't use after it's been assigned to a
     * {@link FolderConfiguration}.
     */
    void setRegionSegment(@NonNull String segment) {
        assert segment.length() == 3 : segment;
        mRegion = new String(new char[] {
                Character.toUpperCase(segment.charAt(1)),
                Character.toUpperCase(segment.charAt(2))
        });
        mFull = mLanguage + "-r" + mRegion;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocaleQualifier qualifier = (LocaleQualifier) o;

        if (!mFull.equals(qualifier.mFull)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return mFull.hashCode();
    }

    /**
     * Returns the string used to represent this qualifier in the folder name.
     */
    @Override
    public String getFolderSegment() {
        return mFull;
    }

    /** BCP 47 tag or "language,region", or language */
    @Override
    public String getShortDisplayValue() {
        if (mFull.startsWith(BCP_47_PREFIX)) {
            return mFull;
        } else if (mRegion != null) {
            return mLanguage + ',' + mRegion;
        } else {
            return mLanguage;
        }
    }

    /** Tag: language, or language-region, or BCP-47 tag */
    public String getTag() {
        if (mFull.startsWith(BCP_47_PREFIX)) {
            return mFull.substring(BCP_47_PREFIX.length()).replace('+','-');
        } else if (mRegion != null) {
            return mLanguage + '-' + mRegion;
        } else {
            return mLanguage;
        }
    }

    @Override
    public String getLongDisplayValue() {
        if (mFull.startsWith(BCP_47_PREFIX)) {
            return String.format("Locale %1$s", mFull);
        } else if (mRegion != null) {
            return String.format("Locale %1$s_%2$s", mLanguage, mRegion);
        } else //noinspection StringEquality
            if (mFull != FAKE_VALUE) {
            return String.format("Locale %1$s", mLanguage);
        }

        return ""; //$NON-NLS-1$
    }

    /**
     * Parse an Android BCP-47 string (which differs from BCP-47 in that
     * it has the prefix "b+" and the separator character has been changed from
     * - to +.
     *
     * @param qualifier the folder name to parse
     * @return a {@linkplain LocaleQualifier} holding the language, region and script
     *     or null if not a valid Android BCP 47 tag
     */
    @Nullable
    public static LocaleQualifier parseBcp47(@NonNull String qualifier) {
        if (qualifier.startsWith(BCP_47_PREFIX)) {
            qualifier = normalizeCase(qualifier);
            Iterator<String> iterator = Splitter.on('+').split(qualifier).iterator();
            // Skip b+ prefix, already checked above
            iterator.next();

            if (iterator.hasNext()) {
                String language = iterator.next();
                String region = null;
                String script = null;
                if (language.length() >= 2 && language.length() <= 3) {
                    if (iterator.hasNext()) {
                        String next = iterator.next();
                        if (next.length() == 4) {
                            // Script specified; look for next
                            script = next;
                            if (iterator.hasNext()) {
                                next = iterator.next();
                            }
                        } else if (next.length() >= 5) {
                            // Past region: specifying a variant
                            return new LocaleQualifier(qualifier, language, null, null);
                        }
                        if (next.length() >= 2 && next.length() <= 3) {
                            region = next;
                        }
                    }
                    if (script == null && (region == null || region.length() == 2)
                            && !iterator.hasNext()) {
                        // Switch from BCP 47 syntax to plain
                        qualifier = language.toLowerCase(Locale.US);
                        if (region != null) {
                            qualifier = qualifier + "-r" + region.toUpperCase(Locale.US);
                        }
                    }
                    return new LocaleQualifier(qualifier, language, region, script);
                }
            }
        }

        return null;
    }

    @NonNull
    public String getLanguage() {
        return mLanguage;
    }

    @Nullable
    public String getRegion() {
        return mRegion;
    }

    @Nullable
    public String getScript() {
        return mScript;
    }

    @NonNull
    public String getFull() {
        return mFull;
    }

    @Override
    public boolean isMatchFor(ResourceQualifier qualifier) {
        if (qualifier instanceof LocaleQualifier) {
            LocaleQualifier other = (LocaleQualifier)qualifier;
            if (!mLanguage.equals(other.mLanguage)) {
                return false;
            }

            if (mRegion != null && other.mRegion != null && !mRegion.equals(other.mRegion)) {
                return false;
            }

            if (mScript != null && other.mScript != null && !mScript.equals(other.mScript)) {
                return false;
            }

            return true;
        }
        return false;
    }
}
