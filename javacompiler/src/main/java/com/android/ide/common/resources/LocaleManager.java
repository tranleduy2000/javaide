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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The {@linkplain LocaleManager} provides access to locale information such as
 * language names and language to region name mappings for the various locales.
 */
public class LocaleManager {
    @SuppressWarnings("InstantiationOfUtilityClass")
    private static final LocaleManager sInstance = new LocaleManager();

    /**
     * Returns the {@linkplain LocaleManager} singleton
     *
     * @return the {@linkplain LocaleManager} singleton, never null
     */
    @NonNull
    public static LocaleManager get() {
        return sInstance;
    }

    /** Use the {@link #get()} factory method */
    private LocaleManager() {
    }

    /**
     * Map of default bindings from language to country (if a region is not
     * specified). Note that if a given language is the language of the default
     * locale on the user's machine, then the country corresponding to that
     * locale is used. Thus, even if for example the default binding of the "en"
     * language is "US", if the current locale has language="en" and the country
     * for that locale is "GB", then "GB" will be used.
     */
    private static Map<String, String> sLanguageToCountry = Maps.newHashMapWithExpectedSize(177);
    /** Names of the various languages according to ISO 639-1 */
    private static Map<String, String> sLanguageNames = Maps.newHashMapWithExpectedSize(187);
    /** Names of the various regions according to ISO 3166-1 */
    private static Map<String, String> sRegionNames = Maps.newHashMapWithExpectedSize(249);

    /**
     * Returns the name of the given region for a 2 letter region code, in English.
     *
     * @param regionCode the 2 letter region code (ISO 3166-1 alpha-2)
     * @return the name of the given region for a region code, in English, or
     *         null if not known
     */
    @Nullable
    public static String getRegionName(@NonNull String regionCode) {
        assert regionCode.length() == 2
                && Character.isUpperCase(regionCode.charAt(0))
                && Character.isUpperCase(regionCode.charAt(1)) : regionCode;

        return sRegionNames.get(regionCode);
    }

    /**
     * Returns the name of the given language for a language code, in English.
     *
     * @param languageCode the 2 letter language code (ISO 639-1)
     * @return the name of the given language for a language code, in English, or
     *         null if not known
     */
    @Nullable
    public static String getLanguageName(@NonNull String languageCode) {
        assert languageCode.length() == 2
                && Character.isLowerCase(languageCode.charAt(0))
                && Character.isLowerCase(languageCode.charAt(1)) : languageCode;

        return sLanguageNames.get(languageCode);
    }

    /**
     * Returns all the known language codes
     *
     * @return all the known language codes
     */
    @NonNull
    public static Set<String> getLanguageCodes() {
        return Collections.unmodifiableSet(sLanguageNames.keySet());
    }

    /**
     * Returns all the known region codes
     *
     * @return all the known region codes
     */
    @NonNull
    public static Set<String> getRegionCodes() {
        return Collections.unmodifiableSet(sRegionNames.keySet());
    }

    /**
     * Returns the region code for the given language. <b>Note that there can be
     * many regions that speak a given language; this just picks one</b>.
     * Note that if the current locale of the user happens to have the same
     * language as the given language code, in that case we pick the current
     * region.
     *
     * @param languageCode the language to look up
     * @return the corresponding region code, if any
     */
    @Nullable
    public static String getLanguageRegion(@NonNull String languageCode) {
        // Prefer the local registration of the current locale; even if
        // for example the default locale for English is the US, if the current
        // default locale is English, then use its associated country, which could
        // for example be Australia.
        @SuppressWarnings("UnnecessaryFullyQualifiedName")
        java.util.Locale locale = java.util.Locale.getDefault();
        if (languageCode.equalsIgnoreCase(locale.getLanguage())) {
            return locale.getCountry();
        }


        return sLanguageToCountry.get(languageCode);
    }

    /**
     * Populate the various maps.
     * <p>
     * The language to region mapping was constructed by using the ISO 639-1 table from
     * http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
     * and for each language, looking up the corresponding Wikipedia entry
     * and picking the first mentioned or in some cases largest country where
     * the language is spoken, then mapping that back to the corresponding ISO 3166-1
     * code.
     */
    static {
        // Afar -> Ethiopia
        sLanguageToCountry.put("aa", "ET"); //$NON-NLS-1$ //$NON-NLS-2$
        sLanguageNames.put("aa", "Afar"); //$NON-NLS-1$

         // "ab": Abkhaz -> Abkhazia, Georgia
         sLanguageToCountry.put("ab", "GE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ab", "Abkhaz"); //$NON-NLS-1$

         // "af": Afrikaans  -> South Africa, Namibia
         sLanguageToCountry.put("af", "ZA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("af", "Afrikaans"); //$NON-NLS-1$

         // "ak": Akan -> Ghana, Ivory Coast
         sLanguageToCountry.put("ak", "GH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ak", "Akan"); //$NON-NLS-1$

         // "am": Amharic -> Ethiopia
         sLanguageToCountry.put("am", "ET"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("am", "Amharic"); //$NON-NLS-1$

         // "an": Aragonese  -> Aragon in Spain
         sLanguageToCountry.put("an", "ES"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("an", "Aragonese"); //$NON-NLS-1$

         // "ar": Arabic -> United Arab Emirates, Kuwait, Oman, Saudi Arabia, Qatar, and Bahrain
         sLanguageToCountry.put("ar", "AE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ar", "Arabic"); //$NON-NLS-1$

         // "as": Assamese -> India
         sLanguageToCountry.put("as", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("as", "Assamese"); //$NON-NLS-1$

         // "av": Avaric -> Azerbaijan
         sLanguageToCountry.put("av", "AZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("av", "Avaric"); //$NON-NLS-1$

         // "ay": Aymara -> Bolivia
         sLanguageToCountry.put("ay", "BO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ay", "Aymara"); //$NON-NLS-1$

         // "az": Azerbaijani -> Azerbaijan
         sLanguageToCountry.put("az", "AZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("az", "Azerbaijani"); //$NON-NLS-1$

         // "ba": Bashkir -> Russia
         sLanguageToCountry.put("ba", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ba", "Bashkir"); //$NON-NLS-1$

         // "be": Belarusian -> Belarus
         sLanguageToCountry.put("be", "BY"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("be", "Belarusian"); //$NON-NLS-1$

         // "bg": Bulgarian -> Bulgaria
         sLanguageToCountry.put("bg", "BG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bg", "Bulgarian"); //$NON-NLS-1$

         // "bh": Bihari languages -> India, Nepal
         sLanguageToCountry.put("bh", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bh", "Bihari languages"); //$NON-NLS-1$

         // "bi": Bislama -> Vanatu
         sLanguageToCountry.put("bi", "VU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bi", "Bislama"); //$NON-NLS-1$

         // "bm": Bambara -> Mali
         sLanguageToCountry.put("bm", "ML"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bm", "Bambara"); //$NON-NLS-1$

         // "bn": Bengali -> Bangladesh, India
         sLanguageToCountry.put("bn", "BD"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bn", "Bengali"); //$NON-NLS-1$

         // "bo": Tibetan -> China
         sLanguageToCountry.put("bo", "CN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bo", "Tibetan"); //$NON-NLS-1$

         // "br": Breton -> France
         sLanguageToCountry.put("br", "FR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("br", "Breton"); //$NON-NLS-1$

         // "bs": Bosnian -> Bosnia and Herzegovina
         sLanguageToCountry.put("bs", "BA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("bs", "Bosnian"); //$NON-NLS-1$

         // "ca": Catalan -> Andorra, Catalonia
         sLanguageToCountry.put("ca", "AD"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ca", "Catalan"); //$NON-NLS-1$

         // "ce": Chechen -> Russia
         sLanguageToCountry.put("ce", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ce", "Chechen"); //$NON-NLS-1$

         // "ch": Chamorro -> Guam, Northern Mariana Islands
         sLanguageToCountry.put("ch", "GU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ch", "Chamorro"); //$NON-NLS-1$

         // "co": Corsican -> France
         sLanguageToCountry.put("co", "FR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("co", "Corsican"); //$NON-NLS-1$

         // "cr": Cree -> Canada and United States
         sLanguageToCountry.put("cr", "CA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("cr", "Cree"); //$NON-NLS-1$

         // "cs": Czech -> Czech Republic
         sLanguageToCountry.put("cs", "CZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("cs", "Czech"); //$NON-NLS-1$

         // "cv": Chuvash -> Russia, Kazakhstan, Ukraine, Uzbekistan...
         sLanguageToCountry.put("cv", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("cv", "Chuvash"); //$NON-NLS-1$

         // "cy": Welsh -> Wales (no 3166 code; using GB)
         sLanguageToCountry.put("cy", "GB"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("cy", "Welsh"); //$NON-NLS-1$

         // "da": Danish -> Denmark
         sLanguageToCountry.put("da", "DK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("da", "Danish"); //$NON-NLS-1$

         // "de": German -> Germany
         sLanguageToCountry.put("de", "DE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("de", "German"); //$NON-NLS-1$

         // "dv": Divehi -> Maldives
         sLanguageToCountry.put("dv", "MV"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("dv", "Divehi"); //$NON-NLS-1$

         // "dz": Dzongkha -> Bhutan
         sLanguageToCountry.put("dz", "BT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("dz", "Dzongkha"); //$NON-NLS-1$

         // "ee": Ewe -> Ghana, Togo
         sLanguageToCountry.put("ee", "GH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ee", "Ewe"); //$NON-NLS-1$

         // "el": Greek -> Greece
         sLanguageToCountry.put("el", "GR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("el", "Greek"); //$NON-NLS-1$

         // "en": English -> United States, United Kingdom, Australia, ...
         sLanguageToCountry.put("en", "US"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("en", "English"); //$NON-NLS-1$

         // "es": Spanish -> Spain, Mexico, ...
         sLanguageToCountry.put("es", "ES"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("es", "Spanish"); //$NON-NLS-1$

         // "et": Estonian ->
         sLanguageToCountry.put("et", "EE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("et", "Estonian"); //$NON-NLS-1$

         // "eu": Basque -> Spain, France
         sLanguageToCountry.put("eu", "ES"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("eu", "Basque"); //$NON-NLS-1$

         // "fa": Persian -> Iran, Afghanistan
         sLanguageToCountry.put("fa", "IR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fa", "Persian"); //$NON-NLS-1$

         // "ff": Fulah -> Mauritania, Senegal, Mali, Guinea, Burkina Faso, ...
         sLanguageToCountry.put("ff", "MR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ff", "Fulah"); //$NON-NLS-1$

         // "fi": Finnish -> Finland
         sLanguageToCountry.put("fi", "FI"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fi", "Finnish"); //$NON-NLS-1$

         // "fj": Fijian -> Fiji
         sLanguageToCountry.put("fj", "FJ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fj", "Fijian"); //$NON-NLS-1$

         // "fo": Faroese -> Denmark
         sLanguageToCountry.put("fo", "DK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fo", "Faroese"); //$NON-NLS-1$

         // "fr": French -> France
         sLanguageToCountry.put("fr", "FR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fr", "French"); //$NON-NLS-1$

         // "fy": Western Frisian -> Netherlands
         sLanguageToCountry.put("fy", "NL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("fy", "Western Frisian"); //$NON-NLS-1$

         // "ga": Irish -> Ireland
         sLanguageToCountry.put("ga", "IE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ga", "Irish"); //$NON-NLS-1$

         // "gd": Gaelic -> Scotland
         sLanguageToCountry.put("gd", "GB"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("gd", "Gaelic"); //$NON-NLS-1$

         // "gl": Galician -> Galicia/Spain
         sLanguageToCountry.put("gl", "ES"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("gl", "Galician"); //$NON-NLS-1$

         // "gn": Guaraní -> Paraguay
         sLanguageToCountry.put("gn", "PY"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("gn", "Guaran\u00ed" /*Guaraní*/); //$NON-NLS-1$

         // "gu": Gujarati -> India
         sLanguageToCountry.put("gu", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("gu", "Gujarati"); //$NON-NLS-1$

         // "gv": Manx -> Isle of Man
         // We don't have an icon for IM
         //sLanguageToCountry.put("gv", "IM"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("gv", "Manx"); //$NON-NLS-1$

         // "ha": Hausa -> Nigeria, Niger
         sLanguageToCountry.put("ha", "NG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ha", "Hausa"); //$NON-NLS-1$

         // "he": Hebrew -> Israel
         sLanguageToCountry.put("he", "IL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("he", "Hebrew"); //$NON-NLS-1$

         // "hi": Hindi -> India
         sLanguageToCountry.put("hi", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("hi", "Hindi"); //$NON-NLS-1$

         // "ho": Hiri Motu -> Papua New Guinea
         sLanguageToCountry.put("ho", "PG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ho", "Hiri Motu"); //$NON-NLS-1$

         // "hr": Croatian ->
         sLanguageToCountry.put("hr", "HR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("hr", "Croatian"); //$NON-NLS-1$

         // "ht": Haitian -> Haiti
         sLanguageToCountry.put("ht", "HT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ht", "Haitian"); //$NON-NLS-1$

         // "hu": Hungarian -> Hungary
         sLanguageToCountry.put("hu", "HU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("hu", "Hungarian"); //$NON-NLS-1$

         // "hy": Armenian -> Armenia
         sLanguageToCountry.put("hy", "AM"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("hy", "Armenian"); //$NON-NLS-1$

         // "hz": Herero -> Namibia, Botswana
         sLanguageToCountry.put("hz", "NA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("hz", "Herero"); //$NON-NLS-1$

         // "id": Indonesian -> Indonesia
         sLanguageToCountry.put("id", "ID"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("id", "Indonesian"); //$NON-NLS-1$

         // "ig": Igbo ->
         sLanguageToCountry.put("ig", "NG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ig", "Igbo"); //$NON-NLS-1$

         // "ii": Nuosu -> China
         sLanguageToCountry.put("ii", "CN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ii", "Nuosu"); //$NON-NLS-1$

         // "ik": Inupiaq -> USA
         sLanguageToCountry.put("ik", "US"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ik", "Inupiaq"); //$NON-NLS-1$

         // "is": Icelandic -> Iceland
         sLanguageToCountry.put("is", "IS"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("is", "Icelandic"); //$NON-NLS-1$

         // "it": Italian -> Italy
         sLanguageToCountry.put("it", "IT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("it", "Italian"); //$NON-NLS-1$

         // "iu": Inuktitut -> Canada
         sLanguageToCountry.put("iu", "CA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("iu", "Inuktitut"); //$NON-NLS-1$

         // "ja": Japanese -> Japan
         sLanguageToCountry.put("ja", "JP"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ja", "Japanese"); //$NON-NLS-1$

         // "jv": Javanese -> Indonesia
         sLanguageToCountry.put("jv", "ID"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("jv", "Javanese"); //$NON-NLS-1$

         // "ka": Georgian -> Georgia
         sLanguageToCountry.put("ka", "GE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ka", "Georgian"); //$NON-NLS-1$

         // "kg": Kongo -> Angola, Congo
         sLanguageToCountry.put("kg", "AO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kg", "Kongo"); //$NON-NLS-1$

         // "ki": Kikuyu -> Kenya
         sLanguageToCountry.put("ki", "KE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ki", "Kikuyu"); //$NON-NLS-1$

         // "kj": Kwanyama -> Angola, Namibia
         sLanguageToCountry.put("kj", "AO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kj", "Kwanyama"); //$NON-NLS-1$

         // "kk": Kazakh -> Kazakhstan
         sLanguageToCountry.put("kk", "KZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kk", "Kazakh"); //$NON-NLS-1$

         // "kl": Kalaallisut -> Denmark
         sLanguageToCountry.put("kl", "DK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kl", "Kalaallisut"); //$NON-NLS-1$

         // "km": Khmer -> Cambodia
         sLanguageToCountry.put("km", "KH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("km", "Khmer"); //$NON-NLS-1$

         // "kn": Kannada -> India
         sLanguageToCountry.put("kn", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kn", "Kannada"); //$NON-NLS-1$

         // "ko": Korean -> Korea
         sLanguageToCountry.put("ko", "KR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ko", "Korean"); //$NON-NLS-1$

         // "kr": Kanuri -> Nigeria
         sLanguageToCountry.put("kr", "NG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kr", "Kanuri"); //$NON-NLS-1$

         // "ks": Kashmiri -> India
         sLanguageToCountry.put("ks", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ks", "Kashmiri"); //$NON-NLS-1$

         // "ku": Kurdish -> Maps to multiple ISO 3166 codes
         sLanguageNames.put("ku", "Kurdish"); //$NON-NLS-1$

         // "kv": Komi -> Russia
         sLanguageToCountry.put("kv", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kv", "Komi"); //$NON-NLS-1$

         // "kw": Cornish -> UK
         sLanguageToCountry.put("kw", "GB"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("kw", "Cornish"); //$NON-NLS-1$

         // "ky": Kyrgyz -> Kyrgyzstan
         sLanguageToCountry.put("ky", "KG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ky", "Kyrgyz"); //$NON-NLS-1$

         // "lb": Luxembourgish -> Luxembourg
         sLanguageToCountry.put("lb", "LU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lb", "Luxembourgish"); //$NON-NLS-1$

         // "lg": Ganda -> Uganda
         sLanguageToCountry.put("lg", "UG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lg", "Ganda"); //$NON-NLS-1$

         // "li": Limburgish -> Netherlands
         sLanguageToCountry.put("li", "NL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("li", "Limburgish"); //$NON-NLS-1$

         // "ln": Lingala -> Congo
         sLanguageToCountry.put("ln", "CD"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ln", "Lingala"); //$NON-NLS-1$

         // "lo": Lao -> Laos
         sLanguageToCountry.put("lo", "LA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lo", "Lao"); //$NON-NLS-1$

         // "lt": Lithuanian -> Lithuania
         sLanguageToCountry.put("lt", "LT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lt", "Lithuanian"); //$NON-NLS-1$

         // "lu": Luba-Katanga -> Congo
         sLanguageToCountry.put("lu", "CD"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lu", "Luba-Katanga"); //$NON-NLS-1$

         // "lv": Latvian -> Latvia
         sLanguageToCountry.put("lv", "LV"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("lv", "Latvian"); //$NON-NLS-1$

         // "mg": Malagasy -> Madagascar
         sLanguageToCountry.put("mg", "MG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mg", "Malagasy"); //$NON-NLS-1$

         // "mh": Marshallese -> Marshall Islands
         sLanguageToCountry.put("mh", "MH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mh", "Marshallese"); //$NON-NLS-1$

         // "mi": Maori -> New Zealand
         sLanguageToCountry.put("mi", "NZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mi", "M\u0101ori"); //$NON-NLS-1$

         // "mk": Macedonian -> Macedonia
         sLanguageToCountry.put("mk", "MK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mk", "Macedonian"); //$NON-NLS-1$

         // "ml": Malayalam -> India
         sLanguageToCountry.put("ml", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ml", "Malayalam"); //$NON-NLS-1$

         // "mn": Mongolian -> Mongolia
         sLanguageToCountry.put("mn", "MN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mn", "Mongolian"); //$NON-NLS-1$

         // "mr": Marathi -> India
         sLanguageToCountry.put("mr", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mr", "Marathi"); //$NON-NLS-1$

         // "ms": Malay -> Malaysia, Indonesia ...
         sLanguageToCountry.put("ms", "MY"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ms", "Malay"); //$NON-NLS-1$

         // "mt": Maltese -> Malta
         sLanguageToCountry.put("mt", "MT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("mt", "Maltese"); //$NON-NLS-1$

         // "my": Burmese -> Myanmar
         sLanguageToCountry.put("my", "MM"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("my", "Burmese"); //$NON-NLS-1$

         // "na": Nauru -> Nauru
         sLanguageToCountry.put("na", "NR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("na", "Nauru"); //$NON-NLS-1$

         // "nb": Norwegian -> Norway
         sLanguageToCountry.put("nb", "NO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nb", "Norwegian Bokm\u00e5l" /*Norwegian Bokmål*/); //$NON-NLS-1$

         // "nd": North Ndebele -> Zimbabwe
         sLanguageToCountry.put("nd", "ZW"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nd", "North Ndebele"); //$NON-NLS-1$

         // "ne": Nepali -> Nepal
         sLanguageToCountry.put("ne", "NP"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ne", "Nepali"); //$NON-NLS-1$

         // "ng":Ndonga  -> Namibia
         sLanguageToCountry.put("ng", "NA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ng", "Ndonga"); //$NON-NLS-1$

         // "nl": Dutch -> Netherlands
         sLanguageToCountry.put("nl", "NL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nl", "Dutch"); //$NON-NLS-1$

         // "nn": Norwegian Nynorsk -> Norway
         sLanguageToCountry.put("nn", "NO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nn", "Norwegian Nynorsk"); //$NON-NLS-1$

         // "no": Norwegian -> Norway
         sLanguageToCountry.put("no", "NO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("no", "Norwegian"); //$NON-NLS-1$

         // "nr": South Ndebele -> South Africa
         sLanguageToCountry.put("nr", "ZA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nr", "South Ndebele"); //$NON-NLS-1$

         // "nv": Navajo -> USA
         sLanguageToCountry.put("nv", "US"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("nv", "Navajo"); //$NON-NLS-1$

         // "ny": Chichewa -> Malawi, Zambia
         sLanguageToCountry.put("ny", "MW"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ny", "Chichewa"); //$NON-NLS-1$

         // "oc": Occitan -> France, Italy, Spain, Monaco
         sLanguageToCountry.put("oc", "FR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("oc", "Occitan"); //$NON-NLS-1$

         // "oj": Ojibwe -> Canada, United States
         sLanguageToCountry.put("oj", "CA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("oj", "Ojibwe"); //$NON-NLS-1$

         // "om": Oromo -> Ethiopia
         sLanguageToCountry.put("om", "ET"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("om", "Oromo"); //$NON-NLS-1$

         // "or": Oriya -> India
         sLanguageToCountry.put("or", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("or", "Oriya"); //$NON-NLS-1$

         // "os": Ossetian -> Russia (North Ossetia), Georgia
         sLanguageToCountry.put("os", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("os", "Ossetian"); //$NON-NLS-1$

         // "pa": Panjabi, -> Pakistan, India
         sLanguageToCountry.put("pa", "PK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("pa", "Panjabi"); //$NON-NLS-1$

         // "pl": Polish -> Poland
         sLanguageToCountry.put("pl", "PL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("pl", "Polish"); //$NON-NLS-1$

         // "ps": Pashto -> Afghanistan, Pakistan
         sLanguageToCountry.put("ps", "AF"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ps", "Pashto"); //$NON-NLS-1$

         // "pt": Portuguese -> Brazil, Portugal, ...
         sLanguageToCountry.put("pt", "BR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("pt", "Portuguese"); //$NON-NLS-1$

         // "qu": Quechua -> Peru, Bolivia
         sLanguageToCountry.put("qu", "PE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("qu", "Quechua"); //$NON-NLS-1$

         // "rm": Romansh -> Switzerland
         sLanguageToCountry.put("rm", "CH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("rm", "Romansh"); //$NON-NLS-1$

         // "rn": Kirundi -> Burundi, Uganda
         sLanguageToCountry.put("rn", "BI"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("rn", "Kirundi"); //$NON-NLS-1$

         // "ro": Romanian -> Romania, Republic of Moldova
         sLanguageToCountry.put("ro", "RO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ro", "Romanian"); //$NON-NLS-1$

         // "ru": Russian -> Russia
         sLanguageToCountry.put("ru", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ru", "Russian"); //$NON-NLS-1$

         // "rw": Kinyarwanda -> Rwanda, Uganda, Democratic Republic of the Congo
         sLanguageToCountry.put("rw", "RW"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("rw", "Kinyarwanda"); //$NON-NLS-1$

         // "sa": Sanskrit -> India
         sLanguageToCountry.put("sa", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sa", "Sanskrit"); //$NON-NLS-1$

         // "sc": Sardinian -> Italy
         sLanguageToCountry.put("sc", "IT"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sc", "Sardinian"); //$NON-NLS-1$

         // "sd": Sindhi -> Pakistan, India
         sLanguageToCountry.put("sd", "PK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sd", "Sindhi"); //$NON-NLS-1$

         // "se": Northern Sami -> Norway, Sweden, Finland
         sLanguageToCountry.put("se", "NO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("se", "Northern Sami"); //$NON-NLS-1$

         // "sg": Sango -> Central African Republic
         sLanguageToCountry.put("sg", "CF"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sg", "Sango"); //$NON-NLS-1$

         // "si": Sinhala ->  Sri Lanka
         sLanguageToCountry.put("si", "LK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("si", "Sinhala"); //$NON-NLS-1$

         // "sk": Slovak -> Slovakia
         sLanguageToCountry.put("sk", "SK"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sk", "Slovak"); //$NON-NLS-1$

         // "sl": Slovene -> Slovenia
         sLanguageToCountry.put("sl", "SI"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sl", "Slovene"); //$NON-NLS-1$

         // "sm": Samoan -> Samoa
         sLanguageToCountry.put("sm", "WS"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sm", "Samoan"); //$NON-NLS-1$

         // "sn": Shona -> Zimbabwe
         sLanguageToCountry.put("sn", "ZW"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sn", "Shona"); //$NON-NLS-1$

         // "so": Somali -> Somalia
         sLanguageToCountry.put("so", "SO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("so", "Somali"); //$NON-NLS-1$

         // "sq": Albanian -> Albania
         sLanguageToCountry.put("sq", "AL"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sq", "Albanian"); //$NON-NLS-1$

         // "sr": Serbian -> Serbia, Bosnia and Herzegovina
         sLanguageToCountry.put("sr", "RS"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sr", "Serbian"); //$NON-NLS-1$

         // "ss": Swati -> Swaziland
         sLanguageToCountry.put("ss", "SZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ss", "Swati"); //$NON-NLS-1$

         // "st": Southern Sotho -> Lesotho, South Africa
         sLanguageToCountry.put("st", "LS"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("st", "Southern Sotho"); //$NON-NLS-1$

         // "su": Sundanese -> Indoniesia
         sLanguageToCountry.put("su", "ID"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("su", "Sundanese"); //$NON-NLS-1$

         // "sv": Swedish -> Sweden
         sLanguageToCountry.put("sv", "SE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sv", "Swedish"); //$NON-NLS-1$

         // "sw": Swahili -> Tanzania, Kenya, and Congo (DRC)
         sLanguageToCountry.put("sw", "TZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("sw", "Swahili"); //$NON-NLS-1$

         // "ta": Tamil -> India, Sri Lanka
         sLanguageToCountry.put("ta", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ta", "Tamil"); //$NON-NLS-1$

         // "te": Telugu -> India
         sLanguageToCountry.put("te", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("te", "Telugu"); //$NON-NLS-1$

         // "tg": Tajik -> Tajikistan, Uzbekistan, Russia, Afghanistan
         sLanguageToCountry.put("tg", "TJ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tg", "Tajik"); //$NON-NLS-1$

         // "th": Thai -> Thailand
         sLanguageToCountry.put("th", "TH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("th", "Thai"); //$NON-NLS-1$

         // "ti": Tigrinya -> Eritrea, Ethiopia
         sLanguageToCountry.put("ti", "ER"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ti", "Tigrinya"); //$NON-NLS-1$

         // "tk": Turkmen -> Turkmenistan
         sLanguageToCountry.put("tk", "TM"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tk", "Turkmen"); //$NON-NLS-1$

         // "tl": Tagalog -> Philippines
         sLanguageToCountry.put("tl", "PH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tl", "Tagalog"); //$NON-NLS-1$

         // "tn": Tswana -> Botswana, South Africa,
         sLanguageToCountry.put("tn", "BW"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tn", "Tswana"); //$NON-NLS-1$

         // "to": Tonga -> Tonga
         sLanguageToCountry.put("to", "TO"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("to", "Tonga"); //$NON-NLS-1$

         // "tr": Turkish -> Turkey
         sLanguageToCountry.put("tr", "TR"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tr", "Turkish"); //$NON-NLS-1$

         // "ts": Tsonga -> Mozambique, South Africa
         sLanguageToCountry.put("ts", "MZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ts", "Tsonga"); //$NON-NLS-1$

         // "tt": Tatar -> Russia
         sLanguageToCountry.put("tt", "RU"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tt", "Tatar"); //$NON-NLS-1$

         // "tw": Twi -> Ghana, Ivory Coast
         sLanguageToCountry.put("tw", "GH"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("tw", "Twi"); //$NON-NLS-1$

         // "ty": Tahitian -> French Polynesia
         sLanguageToCountry.put("ty", "PF"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ty", "Tahitian"); //$NON-NLS-1$

         // "ug": Uighur -> China, Kazakhstan
         sLanguageToCountry.put("ug", "CN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ug", "Uighur"); //$NON-NLS-1$

         // "uk": Ukrainian -> Ukraine
         sLanguageToCountry.put("uk", "UA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("uk", "Ukrainian"); //$NON-NLS-1$

         // "ur": Urdu -> India, Pakistan
         sLanguageToCountry.put("ur", "IN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ur", "Urdu"); //$NON-NLS-1$

         // "uz": Uzbek -> Uzbekistan
         sLanguageToCountry.put("uz", "UZ"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("uz", "Uzbek"); //$NON-NLS-1$

         // "ve": Venda -> South Africa, Zimbabwe
         sLanguageToCountry.put("ve", "ZA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ve", "Venda"); //$NON-NLS-1$

         // "vi": Vietnamese -> Vietnam
         sLanguageToCountry.put("vi", "VN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("vi", "Vietnamese"); //$NON-NLS-1$

         // "wa": Walloon -> Belgium, France
         sLanguageToCountry.put("wa", "BE"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("wa", "Walloon"); //$NON-NLS-1$

         // "wo": Wolof -> Senegal, Gambia, Mauritania
         sLanguageToCountry.put("wo", "SN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("wo", "Wolof"); //$NON-NLS-1$

         // "xh": Xhosa -> South Africa, Lesotho
         sLanguageToCountry.put("xh", "ZA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("xh", "Xhosa"); //$NON-NLS-1$

         // "yi": Yiddish -> United States, Israel, Argentina, Brazil, ...
         sLanguageToCountry.put("yi", "US"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("yi", "Yiddish"); //$NON-NLS-1$

         // "yo": Yorùbá -> Nigeria, Togo, Benin
         sLanguageToCountry.put("yo", "NG"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("yo", "Yor\u00f9b\u00e1" /*Yorùbá*/); //$NON-NLS-1$

         // "za": Zhuang -> China
         sLanguageToCountry.put("za", "CN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("za", "Zhuang"); //$NON-NLS-1$

         // "zh": Chinese -> China, Taiwan, Singapore
         sLanguageToCountry.put("zh", "CN"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("zh", "Chinese"); //$NON-NLS-1$

         // "zu": Zulu -> South Africa
         sLanguageToCountry.put("zu", "ZA"); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("zu", "Zulu"); //$NON-NLS-1$

         // Region Name Map, ISO_3166-1, alpha-2
         sRegionNames.put("AF", "Afghanistan");                                       //$NON-NLS-1$
         sRegionNames.put("AL", "Albania");                                           //$NON-NLS-1$
         sRegionNames.put("DZ", "Algeria");                                           //$NON-NLS-1$
         sRegionNames.put("AS", "American Samoa");                                    //$NON-NLS-1$
         sRegionNames.put("AD", "Andorra");                                           //$NON-NLS-1$
         sRegionNames.put("AO", "Angola");                                            //$NON-NLS-1$
         sRegionNames.put("AI", "Anguilla");                                          //$NON-NLS-1$
         sRegionNames.put("AQ", "Antarctica");                                        //$NON-NLS-1$
         sRegionNames.put("AG", "Antigua and Barbuda");                               //$NON-NLS-1$
         sRegionNames.put("AR", "Argentina");                                         //$NON-NLS-1$
         sRegionNames.put("AM", "Armenia");                                           //$NON-NLS-1$
         sRegionNames.put("AW", "Aruba");                                             //$NON-NLS-1$
         sRegionNames.put("AU", "Australia");                                         //$NON-NLS-1$
         sRegionNames.put("AT", "Austria");                                           //$NON-NLS-1$
         sRegionNames.put("AZ", "Azerbaijan");                                        //$NON-NLS-1$
         sRegionNames.put("BS", "Bahamas");                                           //$NON-NLS-1$
         sRegionNames.put("BH", "Bahrain");                                           //$NON-NLS-1$
         sRegionNames.put("BD", "Bangladesh");                                        //$NON-NLS-1$
         sRegionNames.put("BB", "Barbados");                                          //$NON-NLS-1$
         sRegionNames.put("BY", "Belarus");                                           //$NON-NLS-1$
         sRegionNames.put("BE", "Belgium");                                           //$NON-NLS-1$
         sRegionNames.put("BZ", "Belize");                                            //$NON-NLS-1$
         sRegionNames.put("BJ", "Benin");                                             //$NON-NLS-1$
         sRegionNames.put("BM", "Bermuda");                                           //$NON-NLS-1$
         sRegionNames.put("BT", "Bhutan");                                            //$NON-NLS-1$
         sRegionNames.put("BO", "Bolivia, Plurinational State of");                   //$NON-NLS-1$
         sRegionNames.put("BQ", "Bonaire, Sint Eustatius and Saba");                  //$NON-NLS-1$
         sRegionNames.put("BA", "Bosnia and Herzegovina");                            //$NON-NLS-1$
         sRegionNames.put("BW", "Botswana");                                          //$NON-NLS-1$
         sRegionNames.put("BV", "Bouvet Island");                                     //$NON-NLS-1$
         sRegionNames.put("BR", "Brazil");                                            //$NON-NLS-1$
         sRegionNames.put("IO", "British Indian Ocean Territory");                    //$NON-NLS-1$
         sRegionNames.put("BN", "Brunei Darussalam");                                 //$NON-NLS-1$
         sRegionNames.put("BG", "Bulgaria");                                          //$NON-NLS-1$
         sRegionNames.put("BF", "Burkina Faso");                                      //$NON-NLS-1$
         sRegionNames.put("BI", "Burundi");                                           //$NON-NLS-1$
         sRegionNames.put("KH", "Cambodia");                                          //$NON-NLS-1$
         sRegionNames.put("CM", "Cameroon");                                          //$NON-NLS-1$
         sRegionNames.put("CA", "Canada");                                            //$NON-NLS-1$
         sRegionNames.put("CV", "Cape Verde");                                        //$NON-NLS-1$
         sRegionNames.put("KY", "Cayman Islands");                                    //$NON-NLS-1$
         sRegionNames.put("CF", "Central African Republic");                          //$NON-NLS-1$
         sRegionNames.put("TD", "Chad");                                              //$NON-NLS-1$
         sRegionNames.put("CL", "Chile");                                             //$NON-NLS-1$
         sRegionNames.put("CN", "China");                                             //$NON-NLS-1$
         sRegionNames.put("CX", "Christmas Island");                                  //$NON-NLS-1$
         sRegionNames.put("CC", "Cocos (Keeling) Islands");                           //$NON-NLS-1$
         sRegionNames.put("CO", "Colombia");                                          //$NON-NLS-1$
         sRegionNames.put("KM", "Comoros");                                           //$NON-NLS-1$
         sRegionNames.put("CG", "Congo");                                             //$NON-NLS-1$
         sRegionNames.put("CD", "Congo, the Democratic Republic of the");             //$NON-NLS-1$
         sRegionNames.put("CK", "Cook Islands");                                      //$NON-NLS-1$
         sRegionNames.put("CR", "Costa Rica");                                        //$NON-NLS-1$
         sRegionNames.put("HR", "Croatia");                                           //$NON-NLS-1$
         sRegionNames.put("CU", "Cuba");                                              //$NON-NLS-1$
         sRegionNames.put("CW", "Cura\u00e7ao");                                      //$NON-NLS-1$
         sRegionNames.put("CY", "Cyprus");                                            //$NON-NLS-1$
         sRegionNames.put("CZ", "Czech Republic");                                    //$NON-NLS-1$
         sRegionNames.put("CI", "C\u00f4te d'Ivoire");                                //$NON-NLS-1$
         sRegionNames.put("DK", "Denmark");                                           //$NON-NLS-1$
         sRegionNames.put("DJ", "Djibouti");                                          //$NON-NLS-1$
         sRegionNames.put("DM", "Dominica");                                          //$NON-NLS-1$
         sRegionNames.put("DO", "Dominican Republic");                                //$NON-NLS-1$
         sRegionNames.put("EC", "Ecuador");                                           //$NON-NLS-1$
         sRegionNames.put("EG", "Egypt");                                             //$NON-NLS-1$
         sRegionNames.put("SV", "El Salvador");                                       //$NON-NLS-1$
         sRegionNames.put("GQ", "Equatorial Guinea");                                 //$NON-NLS-1$
         sRegionNames.put("ER", "Eritrea");                                           //$NON-NLS-1$
         sRegionNames.put("EE", "Estonia");                                           //$NON-NLS-1$
         sRegionNames.put("ET", "Ethiopia");                                          //$NON-NLS-1$
         sRegionNames.put("FK", "Falkland Islands (Malvinas)");                       //$NON-NLS-1$
         sRegionNames.put("FO", "Faroe Islands");                                     //$NON-NLS-1$
         sRegionNames.put("FJ", "Fiji");                                              //$NON-NLS-1$
         sRegionNames.put("FI", "Finland");                                           //$NON-NLS-1$
         sRegionNames.put("FR", "France");                                            //$NON-NLS-1$
         sRegionNames.put("GF", "French Guiana");                                     //$NON-NLS-1$
         sRegionNames.put("PF", "French Polynesia");                                  //$NON-NLS-1$
         sRegionNames.put("TF", "French Southern Territories");                       //$NON-NLS-1$
         sRegionNames.put("GA", "Gabon");                                             //$NON-NLS-1$
         sRegionNames.put("GM", "Gambia");                                            //$NON-NLS-1$
         sRegionNames.put("GE", "Georgia");                                           //$NON-NLS-1$
         sRegionNames.put("DE", "Germany");                                           //$NON-NLS-1$
         sRegionNames.put("GH", "Ghana");                                             //$NON-NLS-1$
         sRegionNames.put("GI", "Gibraltar");                                         //$NON-NLS-1$
         sRegionNames.put("GR", "Greece");                                            //$NON-NLS-1$
         sRegionNames.put("GL", "Greenland");                                         //$NON-NLS-1$
         sRegionNames.put("GD", "Grenada");                                           //$NON-NLS-1$
         sRegionNames.put("GP", "Guadeloupe");                                        //$NON-NLS-1$
         sRegionNames.put("GU", "Guam");                                              //$NON-NLS-1$
         sRegionNames.put("GT", "Guatemala");                                         //$NON-NLS-1$
         sRegionNames.put("GG", "Guernsey");                                          //$NON-NLS-1$
         sRegionNames.put("GN", "Guinea");                                            //$NON-NLS-1$
         sRegionNames.put("GW", "Guinea-Bissau");                                     //$NON-NLS-1$
         sRegionNames.put("GY", "Guyana");                                            //$NON-NLS-1$
         sRegionNames.put("HT", "Haiti");                                             //$NON-NLS-1$
         sRegionNames.put("HM", "Heard Island and McDonald Islands");                 //$NON-NLS-1$
         sRegionNames.put("VA", "Holy See (Vatican City State)");                     //$NON-NLS-1$
         sRegionNames.put("HN", "Honduras");                                          //$NON-NLS-1$
         sRegionNames.put("HK", "Hong Kong");                                         //$NON-NLS-1$
         sRegionNames.put("HU", "Hungary");                                           //$NON-NLS-1$
         sRegionNames.put("IS", "Iceland");                                           //$NON-NLS-1$
         sRegionNames.put("IN", "India");                                             //$NON-NLS-1$
         sRegionNames.put("ID", "Indonesia");                                         //$NON-NLS-1$
         sRegionNames.put("IR", "Iran, Islamic Republic of");                         //$NON-NLS-1$
         sRegionNames.put("IQ", "Iraq");                                              //$NON-NLS-1$
         sRegionNames.put("IE", "Ireland");                                           //$NON-NLS-1$
         sRegionNames.put("IM", "Isle of Man");                                       //$NON-NLS-1$
         sRegionNames.put("IL", "Israel");                                            //$NON-NLS-1$
         sRegionNames.put("IT", "Italy");                                             //$NON-NLS-1$
         sRegionNames.put("JM", "Jamaica");                                           //$NON-NLS-1$
         sRegionNames.put("JP", "Japan");                                             //$NON-NLS-1$
         sRegionNames.put("JE", "Jersey");                                            //$NON-NLS-1$
         sRegionNames.put("JO", "Jordan");                                            //$NON-NLS-1$
         sRegionNames.put("KZ", "Kazakhstan");                                        //$NON-NLS-1$
         sRegionNames.put("KE", "Kenya");                                             //$NON-NLS-1$
         sRegionNames.put("KI", "Kiribati");                                          //$NON-NLS-1$
         sRegionNames.put("KP", "Korea, Democratic People's Republic of");            //$NON-NLS-1$
         sRegionNames.put("KR", "Korea, Republic of");                                //$NON-NLS-1$
         sRegionNames.put("KW", "Kuwait");                                            //$NON-NLS-1$
         sRegionNames.put("KG", "Kyrgyzstan");                                        //$NON-NLS-1$
         sRegionNames.put("LA", "Lao People's Democratic Republic");                  //$NON-NLS-1$
         sRegionNames.put("LV", "Latvia");                                            //$NON-NLS-1$
         sRegionNames.put("LB", "Lebanon");                                           //$NON-NLS-1$
         sRegionNames.put("LS", "Lesotho");                                           //$NON-NLS-1$
         sRegionNames.put("LR", "Liberia");                                           //$NON-NLS-1$
         sRegionNames.put("LY", "Libya");                                             //$NON-NLS-1$
         sRegionNames.put("LI", "Liechtenstein");                                     //$NON-NLS-1$
         sRegionNames.put("LT", "Lithuania");                                         //$NON-NLS-1$
         sRegionNames.put("LU", "Luxembourg");                                        //$NON-NLS-1$
         sRegionNames.put("MO", "Macao");                                             //$NON-NLS-1$
         sRegionNames.put("MK", "Macedonia, the former Yugoslav Republic of");        //$NON-NLS-1$
         sRegionNames.put("MG", "Madagascar");                                        //$NON-NLS-1$
         sRegionNames.put("MW", "Malawi");                                            //$NON-NLS-1$
         sRegionNames.put("MY", "Malaysia");                                          //$NON-NLS-1$
         sRegionNames.put("MV", "Maldives");                                          //$NON-NLS-1$
         sRegionNames.put("ML", "Mali");                                              //$NON-NLS-1$
         sRegionNames.put("MT", "Malta");                                             //$NON-NLS-1$
         sRegionNames.put("MH", "Marshall Islands");                                  //$NON-NLS-1$
         sRegionNames.put("MQ", "Martinique");                                        //$NON-NLS-1$
         sRegionNames.put("MR", "Mauritania");                                        //$NON-NLS-1$
         sRegionNames.put("MU", "Mauritius");                                         //$NON-NLS-1$
         sRegionNames.put("YT", "Mayotte");                                           //$NON-NLS-1$
         sRegionNames.put("MX", "Mexico");                                            //$NON-NLS-1$
         sRegionNames.put("FM", "Micronesia, Federated States of");                   //$NON-NLS-1$
         sRegionNames.put("MD", "Moldova, Republic of");                              //$NON-NLS-1$
         sRegionNames.put("MC", "Monaco");                                            //$NON-NLS-1$
         sRegionNames.put("MN", "Mongolia");                                          //$NON-NLS-1$
         sRegionNames.put("ME", "Montenegro");                                        //$NON-NLS-1$
         sRegionNames.put("MS", "Montserrat");                                        //$NON-NLS-1$
         sRegionNames.put("MA", "Morocco");                                           //$NON-NLS-1$
         sRegionNames.put("MZ", "Mozambique");                                        //$NON-NLS-1$
         sRegionNames.put("MM", "Myanmar");                                           //$NON-NLS-1$
         sRegionNames.put("NA", "Namibia");                                           //$NON-NLS-1$
         sRegionNames.put("NR", "Nauru");                                             //$NON-NLS-1$
         sRegionNames.put("NP", "Nepal");                                             //$NON-NLS-1$
         sRegionNames.put("NL", "Netherlands");                                       //$NON-NLS-1$
         sRegionNames.put("NC", "New Caledonia");                                     //$NON-NLS-1$
         sRegionNames.put("NZ", "New Zealand");                                       //$NON-NLS-1$
         sRegionNames.put("NI", "Nicaragua");                                         //$NON-NLS-1$
         sRegionNames.put("NE", "Niger");                                             //$NON-NLS-1$
         sRegionNames.put("NG", "Nigeria");                                           //$NON-NLS-1$
         sRegionNames.put("NU", "Niue");                                              //$NON-NLS-1$
         sRegionNames.put("NF", "Norfolk Island");                                    //$NON-NLS-1$
         sRegionNames.put("MP", "Northern Mariana Islands");                          //$NON-NLS-1$
         sRegionNames.put("NO", "Norway");                                            //$NON-NLS-1$
         sRegionNames.put("OM", "Oman");                                              //$NON-NLS-1$
         sRegionNames.put("PK", "Pakistan");                                          //$NON-NLS-1$
         sRegionNames.put("PW", "Palau");                                             //$NON-NLS-1$
         sRegionNames.put("PS", "Palestine");                                         //$NON-NLS-1$
         sRegionNames.put("PA", "Panama");                                            //$NON-NLS-1$
         sRegionNames.put("PG", "Papua New Guinea");                                  //$NON-NLS-1$
         sRegionNames.put("PY", "Paraguay");                                          //$NON-NLS-1$
         sRegionNames.put("PE", "Peru");                                              //$NON-NLS-1$
         sRegionNames.put("PH", "Philippines");                                       //$NON-NLS-1$
         sRegionNames.put("PN", "Pitcairn");                                          //$NON-NLS-1$
         sRegionNames.put("PL", "Poland");                                            //$NON-NLS-1$
         sRegionNames.put("PT", "Portugal");                                          //$NON-NLS-1$
         sRegionNames.put("PR", "Puerto Rico");                                       //$NON-NLS-1$
         sRegionNames.put("QA", "Qatar");                                             //$NON-NLS-1$
         sRegionNames.put("RO", "Romania");                                           //$NON-NLS-1$
         sRegionNames.put("RU", "Russian Federation");                                //$NON-NLS-1$
         sRegionNames.put("RW", "Rwanda");                                            //$NON-NLS-1$
         sRegionNames.put("RE", "R\u00e9union");                                      //$NON-NLS-1$
         sRegionNames.put("BL", "Saint Barth\u00e9lemy");                             //$NON-NLS-1$
         sRegionNames.put("SH", "Saint Helena, Ascension and Tristan da Cunha");      //$NON-NLS-1$
         sRegionNames.put("KN", "Saint Kitts and Nevis");                             //$NON-NLS-1$
         sRegionNames.put("LC", "Saint Lucia");                                       //$NON-NLS-1$
         sRegionNames.put("MF", "Saint Martin (French part)");                        //$NON-NLS-1$
         sRegionNames.put("PM", "Saint Pierre and Miquelon");                         //$NON-NLS-1$
         sRegionNames.put("VC", "Saint Vincent and the Grenadines");                  //$NON-NLS-1$
         sRegionNames.put("WS", "Samoa");                                             //$NON-NLS-1$
         sRegionNames.put("SM", "San Marino");                                        //$NON-NLS-1$
         sRegionNames.put("ST", "Sao Tome and Principe");                             //$NON-NLS-1$
         sRegionNames.put("SA", "Saudi Arabia");                                      //$NON-NLS-1$
         sRegionNames.put("SN", "Senegal");                                           //$NON-NLS-1$
         sRegionNames.put("RS", "Serbia");                                            //$NON-NLS-1$
         sRegionNames.put("SC", "Seychelles");                                        //$NON-NLS-1$
         sRegionNames.put("SL", "Sierra Leone");                                      //$NON-NLS-1$
         sRegionNames.put("SG", "Singapore");                                         //$NON-NLS-1$
         sRegionNames.put("SX", "Sint Maarten (Dutch part)");                         //$NON-NLS-1$
         sRegionNames.put("SK", "Slovakia");                                          //$NON-NLS-1$
         sRegionNames.put("SI", "Slovenia");                                          //$NON-NLS-1$
         sRegionNames.put("SB", "Solomon Islands");                                   //$NON-NLS-1$
         sRegionNames.put("SO", "Somalia");                                           //$NON-NLS-1$
         sRegionNames.put("ZA", "South Africa");                                      //$NON-NLS-1$
         sRegionNames.put("GS", "South Georgia and the South Sandwich Islands");      //$NON-NLS-1$
         sRegionNames.put("SS", "South Sudan");                                       //$NON-NLS-1$
         sRegionNames.put("ES", "Spain");                                             //$NON-NLS-1$
         sRegionNames.put("LK", "Sri Lanka");                                         //$NON-NLS-1$
         sRegionNames.put("SD", "Sudan");                                             //$NON-NLS-1$
         sRegionNames.put("SR", "Suriname");                                          //$NON-NLS-1$
         sRegionNames.put("SJ", "Svalbard and Jan Mayen");                            //$NON-NLS-1$
         sRegionNames.put("SZ", "Swaziland");                                         //$NON-NLS-1$
         sRegionNames.put("SE", "Sweden");                                            //$NON-NLS-1$
         sRegionNames.put("CH", "Switzerland");                                       //$NON-NLS-1$
         sRegionNames.put("SY", "Syrian Arab Republic");                              //$NON-NLS-1$
         sRegionNames.put("TW", "Taiwan, Province of China");                         //$NON-NLS-1$
         sRegionNames.put("TJ", "Tajikistan");                                        //$NON-NLS-1$
         sRegionNames.put("TZ", "Tanzania, United Republic of");                      //$NON-NLS-1$
         sRegionNames.put("TH", "Thailand");                                          //$NON-NLS-1$
         sRegionNames.put("TL", "Timor-Leste");                                       //$NON-NLS-1$
         sRegionNames.put("TG", "Togo");                                              //$NON-NLS-1$
         sRegionNames.put("TK", "Tokelau");                                           //$NON-NLS-1$
         sRegionNames.put("TO", "Tonga");                                             //$NON-NLS-1$
         sRegionNames.put("TT", "Trinidad and Tobago");                               //$NON-NLS-1$
         sRegionNames.put("TN", "Tunisia");                                           //$NON-NLS-1$
         sRegionNames.put("TR", "Turkey");                                            //$NON-NLS-1$
         sRegionNames.put("TM", "Turkmenistan");                                      //$NON-NLS-1$
         sRegionNames.put("TC", "Turks and Caicos Islands");                          //$NON-NLS-1$
         sRegionNames.put("TV", "Tuvalu");                                            //$NON-NLS-1$
         sRegionNames.put("UG", "Uganda");                                            //$NON-NLS-1$
         sRegionNames.put("UA", "Ukraine");                                           //$NON-NLS-1$
         sRegionNames.put("AE", "United Arab Emirates");                              //$NON-NLS-1$
         sRegionNames.put("GB", "United Kingdom");                                    //$NON-NLS-1$
         sRegionNames.put("US", "United States");                                     //$NON-NLS-1$
         sRegionNames.put("UM", "United States Minor Outlying Islands");              //$NON-NLS-1$
         sRegionNames.put("UY", "Uruguay");                                           //$NON-NLS-1$
         sRegionNames.put("UZ", "Uzbekistan");                                        //$NON-NLS-1$
         sRegionNames.put("VU", "Vanuatu");                                           //$NON-NLS-1$
         sRegionNames.put("VE", "Venezuela, Bolivarian Republic of");                 //$NON-NLS-1$
         sRegionNames.put("VN", "Viet Nam");                                          //$NON-NLS-1$
         sRegionNames.put("VG", "Virgin Islands, British");                           //$NON-NLS-1$
         sRegionNames.put("VI", "Virgin Islands, U.S.");                              //$NON-NLS-1$
         sRegionNames.put("WF", "Wallis and Futuna");                                 //$NON-NLS-1$
         sRegionNames.put("EH", "Western Sahara");                                    //$NON-NLS-1$
         sRegionNames.put("YE", "Yemen");                                             //$NON-NLS-1$
         sRegionNames.put("ZM", "Zambia");                                            //$NON-NLS-1$
         sRegionNames.put("ZW", "Zimbabwe");                                          //$NON-NLS-1$
         sRegionNames.put("AX", "\u00c5land Islands");                                //$NON-NLS-1$

         // Aliases
         // http://developer.android.com/reference/java/util/Locale.html
         // Apparently we're using some old aliases for some languages
         //  The Hebrew ("he") language code is rewritten as "iw", Indonesian ("id") as "in",
         // and Yiddish ("yi") as "ji".
         sLanguageToCountry.put("iw", sLanguageToCountry.get("he")); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageToCountry.put("in", sLanguageToCountry.get("id")); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageToCountry.put("ji", sLanguageToCountry.get("yi")); //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("iw", sLanguageNames.get("he"));         //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("in", sLanguageNames.get("id"));         //$NON-NLS-1$ //$NON-NLS-2$
         sLanguageNames.put("ji", sLanguageNames.get("yi"));         //$NON-NLS-1$ //$NON-NLS-2$

        // The following miscellaneous languages have no binding to a region
        // in sLanguageToCountry, since they are either extinct or constructed or
        // only in literary use:
        sLanguageNames.put("pi", "Pali"); //$NON-NLS-1$
        sLanguageNames.put("vo", "Volap\u00fck" /*Volapük*/); //$NON-NLS-1$
        sLanguageNames.put("eo", "Esperanto"); //$NON-NLS-1$
        sLanguageNames.put("la", "Latin"); //$NON-NLS-1$
        sLanguageNames.put("ia", "Interlingua"); //$NON-NLS-1$
        sLanguageNames.put("ie", "Interlingue"); //$NON-NLS-1$
        sLanguageNames.put("io", "Ido"); //$NON-NLS-1$
        sLanguageNames.put("ae", "Avestan"); //$NON-NLS-1$
        sLanguageNames.put("cu", "Church Slavic"); //$NON-NLS-1$

        // To check initial capacities of the maps and avoid dynamic resizing:
        //System.out.println("Language count = " + sLanguageNames.size());
        //System.out.println("Language Binding count = " + sLanguageToCountry.size());
        //System.out.println("Region count = " + sRegionNames.size());
    }

    @VisibleForTesting
    public static Map<String, String> getLanguageToCountryMap() {
        return sLanguageToCountry;
    }

    @VisibleForTesting
    public static Map<String, String> getLanguageNamesMap() {
        return sLanguageNames;
    }

    @VisibleForTesting
    public static Map<String, String> getRegionNamesMap() {
        return sRegionNames;
    }
}
