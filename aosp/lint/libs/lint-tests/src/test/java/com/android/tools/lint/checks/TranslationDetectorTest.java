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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.Variant;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Project;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public class TranslationDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new TranslationDetector();
    }

    @Override
    protected boolean includeParentPath() {
        return true;
    }

    public void testTranslation() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(
            // Sample files from the Home app
            "res/values/strings.xml:20: Error: \"show_all_apps\" is not translated in \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"show_all_apps\">All</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:23: Error: \"menu_wallpaper\" is not translated in \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"menu_wallpaper\">Wallpaper</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:25: Error: \"menu_settings\" is not translated in \"cs\" (Czech), \"de-DE\" (German: Germany), \"es\" (Spanish), \"es-US\" (Spanish: United States), \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"menu_settings\">Settings</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values-cs/arrays.xml:3: Error: \"security_questions\" is translated here but not found in default locale [ExtraTranslation]\n" +
            "  <string-array name=\"security_questions\">\n" +
            "                ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/values-es/strings.xml:12: Also translated here\n" +
            "res/values-de-rDE/strings.xml:11: Error: \"continue_skip_label\" is translated here but not found in default locale [ExtraTranslation]\n" +
            "    <string name=\"continue_skip_label\">\"Weiter\"</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "5 errors, 0 warnings\n",

            lintProject(
                 "res/values/strings.xml",
                 "res/values-cs/strings.xml",
                 "res/values-de-rDE/strings.xml",
                 "res/values-es/strings.xml",
                 "res/values-es-rUS/strings.xml",
                 "res/values-land/strings.xml",
                 "res/values-cs/arrays.xml",
                 "res/values-es/donottranslate.xml",
                 "res/values-nl-rNL/strings.xml"));
    }

    public void testTranslationWithCompleteRegions() throws Exception {
        TranslationDetector.sCompleteRegions = true;
        assertEquals(
            // Sample files from the Home app
            "res/values/strings.xml:19: Error: \"home_title\" is not translated in \"es-US\" (Spanish: United States) [MissingTranslation]\n" +
            "    <string name=\"home_title\">Home Sample</string>\n" +
            "            ~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:20: Error: \"show_all_apps\" is not translated in \"es-US\" (Spanish: United States), \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"show_all_apps\">All</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:23: Error: \"menu_wallpaper\" is not translated in \"es-US\" (Spanish: United States), \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"menu_wallpaper\">Wallpaper</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:25: Error: \"menu_settings\" is not translated in \"cs\" (Czech), \"de-DE\" (German: Germany), \"es-US\" (Spanish: United States), \"nl-NL\" (Dutch: Netherlands) [MissingTranslation]\n" +
            "    <string name=\"menu_settings\">Settings</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~\n" +
            "res/values/strings.xml:29: Error: \"wallpaper_instructions\" is not translated in \"es-US\" (Spanish: United States) [MissingTranslation]\n" +
            "    <string name=\"wallpaper_instructions\">Tap picture to set portrait wallpaper</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    res/values-land/strings.xml:19: <No location-specific message\n" +
            "res/values-de-rDE/strings.xml:11: Error: \"continue_skip_label\" is translated here but not found in default locale [ExtraTranslation]\n" +
            "    <string name=\"continue_skip_label\">\"Weiter\"</string>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "6 errors, 0 warnings\n",

            lintProject(
                 "res/values/strings.xml",
                 "res/values-cs/strings.xml",
                 "res/values-de-rDE/strings.xml",
                 "res/values-es-rUS/strings.xml",
                 "res/values-land/strings.xml",
                 "res/values-nl-rNL/strings.xml"));
    }

    public void testBcp47() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(""
                + "res/values/strings.xml:25: Error: \"menu_settings\" is not translated in \"tlh\" (Klingon; tlhIngan-Hol) [MissingTranslation]\n"
                + "    <string name=\"menu_settings\">Settings</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values-cs/strings.xml=>res/values-b+tlh/strings.xml"));
    }

    public void testHandleBom() throws Exception {
        // This isn't really testing translation detection; it's just making sure that the
        // XML parser doesn't bomb on BOM bytes (byte order marker) at the beginning of
        // the XML document
        assertEquals(
            "No warnings.",
            lintProject(
                 "res/values-de/strings.xml=>res/values/strings.xml"
            ));
    }

    public void testTranslatedArrays() throws Exception {
        TranslationDetector.sCompleteRegions = true;
        assertEquals(
            "No warnings.",

            lintProject(
                 "res/values/translatedarrays.xml",
                 "res/values-cs/translatedarrays.xml"));
    }

    public void testTranslationSuppresss() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/values/strings_ignore.xml=>res/values/strings.xml",
                    "res/values-es/strings_ignore.xml=>res/values-es/strings.xml",
                    "res/values-nl-rNL/strings.xml=>res/values-nl-rNL/strings.xml"));
    }

    public void testMixedTranslationArrays() throws Exception {
        // See issue http://code.google.com/p/android/issues/detail?id=29263
        assertEquals(
                "No warnings.",

                lintProject(
                        "res/values/strings3.xml=>res/values/strings.xml",
                        "res/values-fr/strings.xml=>res/values-fr/strings.xml"));
    }

    public void testLibraryProjects() throws Exception {
        // If a library project provides additional locales, that should not force
        // the main project to include all those translations
        assertEquals(
            "No warnings.",

             lintProject(
                 // Master project
                 "multiproject/main-manifest.xml=>AndroidManifest.xml",
                 "multiproject/main.properties=>project.properties",
                 "res/values/strings2.xml",

                 // Library project
                 "multiproject/library-manifest.xml=>../LibraryProject/AndroidManifest.xml",
                 "multiproject/library.properties=>../LibraryProject/project.properties",

                 "res/values/strings.xml=>../LibraryProject/res/values/strings.xml",
                 "res/values-cs/strings.xml=>../LibraryProject/res/values-cs/strings.xml",
                 "res/values-cs/strings.xml=>../LibraryProject/res/values-de/strings.xml",
                 "res/values-cs/strings.xml=>../LibraryProject/res/values-nl/strings.xml"
             ));
    }

    public void testNonTranslatable1() throws Exception {
        TranslationDetector.sCompleteRegions = true;
        assertEquals(
            "res/values-nb/nontranslatable.xml:3: Error: The resource string \"dummy\" has been marked as translatable=\"false\" [ExtraTranslation]\n" +
            "    <string name=\"dummy\">Ignore Me</string>\n" +
            "            ~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n" +
            "",

            lintProject("res/values/nontranslatable.xml",
                    "res/values/nontranslatable2.xml=>res/values-nb/nontranslatable.xml"));
    }

    public void testNonTranslatable2() throws Exception {
        TranslationDetector.sCompleteRegions = true;
        assertEquals(
            "res/values-nb/nontranslatable.xml:3: Error: Non-translatable resources should only be defined in the base values/ folder [ExtraTranslation]\n" +
            "    <string name=\"dummy\" translatable=\"false\">Ignore Me</string>\n" +
            "                         ~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n" +
            "",

            lintProject("res/values/nontranslatable.xml=>res/values-nb/nontranslatable.xml"));
    }

    public void testNonTranslatable3() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=92861
        // Don't treat "google_maps_key" or "google_maps_key_instructions" as translatable
        TranslationDetector.sCompleteRegions = true;
        assertEquals(
                "No warnings.",

                lintProject("res/values/google_maps_api.xml",
                        "res/values/strings2.xml",
                        "res/values/strings2.xml=>res/values-nb/strings2.xml"));
    }

    public void testSpecifiedLanguageOk() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(
            "No warnings.",

            lintProject(
                 "res/values-es/strings.xml=>res/values/strings.xml",
                 "res/values-es/strings.xml=>res/values-es/strings.xml",
                 "res/values-es-rUS/strings.xml"));
    }

    public void testSpecifiedLanguage() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(
            "No warnings.",

            lintProject(
                 "res/values-es/strings_locale.xml=>res/values/strings.xml",
                 "res/values-es-rUS/strings.xml"));
    }

    public void testAnalytics() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=43070
        assertEquals(
                "No warnings.",

                lintProject(
                        "res/values/analytics.xml",
                        "res/values-es/donottranslate.xml" // to make app multilingual
                ));
    }

    public void testIssue33845() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=33845
        assertEquals(""
                + "res/values/strings.xml:5: Error: \"dateTimeFormat\" is not translated in \"de\" (German) [MissingTranslation]\n"
                + "    <string name=\"dateTimeFormat\">MM/dd/yyyy - HH:mm</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "locale33845/.classpath=>.classpath",
                        "locale33845/AndroidManifest.xml=>AndroidManifest.xml",
                        "locale33845/project.properties=>project.properties",
                        "locale33845/res/values/strings.xml=>res/values/strings.xml",
                        "locale33845/res/values-de/strings.xml=>res/values-de/strings.xml",
                        "locale33845/res/values-en-rGB/strings.xml=>res/values-en-rGB/strings.xml"
                ));
    }

    public void testIssue33845b() throws Exception {
        // Similar to issue 33845, but with some variations to the test data
        // See http://code.google.com/p/android/issues/detail?id=33845
        assertEquals("No warnings.",

                lintProject(
                        "locale33845/.classpath=>.classpath",
                        "locale33845/AndroidManifest.xml=>AndroidManifest.xml",
                        "locale33845/project.properties=>project.properties",
                        "locale33845/res/values/styles.xml=>res/values/styles.xml",
                        "locale33845/res/values/strings2.xml=>res/values/strings.xml",
                        "locale33845/res/values-en-rGB/strings2.xml=>res/values-en-rGB/strings.xml"
                ));
    }

    public void testEnglishRegionAndValuesAsEnglish1() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        // tools:locale=en in base folder
        // Regression test for https://code.google.com/p/android/issues/detail?id=75879
        assertEquals("No warnings.",

                lintProject(
                        "locale33845/res/values/strings3.xml=>res/values/strings.xml",
                        "locale33845/res/values-en-rGB/strings3.xml=>res/values-en-rGB/strings.xml"
                ));
    }

    public void testEnglishRegionAndValuesAsEnglish2() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        // No tools:locale specified in the base folder: *assume* English
        // Regression test for https://code.google.com/p/android/issues/detail?id=75879
        assertEquals(""
                + "res/values/strings.xml:5: Error: \"other\" is not translated in \"de-DE\" (German: Germany) [MissingTranslation]\n"
                + "    <string name=\"other\">other</string>\n"
                + "            ~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "locale33845/res/values/strings4.xml=>res/values/strings.xml",
                        // Flagged because it's not the default locale:
                        "locale33845/res/values-en-rGB/strings3.xml=>res/values-de-rDE/strings.xml",
                        // Not flagged because it's the implicit default locale
                        "locale33845/res/values-en-rGB/strings3.xml=>res/values-en-rGB/strings.xml"
                ));
    }

    public void testEnglishRegionAndValuesAsEnglish3() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        // tools:locale=de in base folder
        // Regression test for https://code.google.com/p/android/issues/detail?id=75879
        assertEquals("No warnings.",

                lintProject(
                        "locale33845/res/values/strings5.xml=>res/values/strings.xml",
                        "locale33845/res/values-en-rGB/strings3.xml=>res/values-de-rDE/strings.xml"
                ));
    }

    public void testResConfigs() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(""
                + "res/values/strings.xml:25: Error: \"menu_settings\" is not translated in \"cs\" (Czech), \"de-DE\" (German: Germany) [MissingTranslation]\n"
                + "    <string name=\"menu_settings\">Settings</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-cs/arrays.xml:3: Error: \"security_questions\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "  <string-array name=\"security_questions\">\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/values-es/strings.xml:12: Also translated here\n"
                + "res/values-de-rDE/strings.xml:11: Error: \"continue_skip_label\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"continue_skip_label\">\"Weiter\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "3 errors, 0 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values-cs/strings.xml",
                        "res/values-de-rDE/strings.xml",
                        "res/values-es/strings.xml",
                        "res/values-es-rUS/strings.xml",
                        "res/values-land/strings.xml",
                        "res/values-cs/arrays.xml",
                        "res/values-es/donottranslate.xml",
                        "res/values-nl-rNL/strings.xml"));
    }

    public void testMissingBaseCompletely() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(""
                + "res/values-cs/strings.xml:4: Error: \"home_title\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"home_title\">\"Domů\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~\n"
                + "res/values-cs/strings.xml:5: Error: \"show_all_apps\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"show_all_apps\">\"Vše\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-cs/strings.xml:6: Error: \"menu_wallpaper\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"menu_wallpaper\">\"Tapeta\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-cs/strings.xml:7: Error: \"menu_search\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"menu_search\">\"Hledat\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~\n"
                + "res/values-cs/strings.xml:10: Error: \"wallpaper_instructions\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"wallpaper_instructions\">\"Klepnutím na obrázek nastavíte tapetu portrétu\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "5 errors, 0 warnings\n",

                lintProject("res/values-cs/strings.xml"));
    }

    public void testMissingSomeBaseStrings() throws Exception {
        TranslationDetector.sCompleteRegions = false;
        assertEquals(""
                + "res/values-es/strings.xml:4: Error: \"home_title\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"home_title\">\"Casa\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~\n"
                + "res/values-es/strings.xml:5: Error: \"show_all_apps\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"show_all_apps\">\"Todo\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-es/strings.xml:6: Error: \"menu_wallpaper\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"menu_wallpaper\">\"Papel tapiz\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-es/strings.xml:10: Error: \"wallpaper_instructions\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "    <string name=\"wallpaper_instructions\">\"Puntee en la imagen para establecer papel tapiz vertical\"</string>\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values-es/strings.xml:12: Error: \"security_questions\" is translated here but not found in default locale [ExtraTranslation]\n"
                + "  <string-array name=\"security_questions\">\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "5 errors, 0 warnings\n",

                lintProject(
                        "res/values-es-rUS/strings.xml=>res/values/strings.xml",
                        "res/values-es/strings.xml=>res/values-es/strings.xml"

                ));
    }

    @Override
    protected TestLintClient createClient() {
        if (!getName().startsWith("testResConfigs")) {
            return super.createClient();
        }

        // Set up a mock project model for the resource configuration test(s)
        // where we provide a subset of densities to be included

        return new TestLintClient() {
            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                return new Project(this, dir, referenceDir) {
                    @Override
                    public boolean isGradleProject() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public AndroidProject getGradleProjectModel() {
                        /*
                        Simulate variant freeBetaDebug in this setup:
                            defaultConfig {
                                ...
                                resConfigs "cs"
                            }
                            flavorDimensions  "pricing", "releaseType"
                            productFlavors {
                                beta {
                                    flavorDimension "releaseType"
                                    resConfig "en", "de"
                                    resConfigs "nodpi", "hdpi"
                                }
                                normal { flavorDimension "releaseType" }
                                free { flavorDimension "pricing" }
                                paid { flavorDimension "pricing" }
                            }
                         */
                        ProductFlavor flavorFree = mock(ProductFlavor.class);
                        when(flavorFree.getName()).thenReturn("free");
                        when(flavorFree.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorNormal = mock(ProductFlavor.class);
                        when(flavorNormal.getName()).thenReturn("normal");
                        when(flavorNormal.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorPaid = mock(ProductFlavor.class);
                        when(flavorPaid.getName()).thenReturn("paid");
                        when(flavorPaid.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorBeta = mock(ProductFlavor.class);
                        when(flavorBeta.getName()).thenReturn("beta");
                        List<String> resConfigs = Arrays.asList("hdpi", "en", "de", "nodpi");
                        when(flavorBeta.getResourceConfigurations()).thenReturn(resConfigs);

                        ProductFlavor defaultFlavor = mock(ProductFlavor.class);
                        when(defaultFlavor.getName()).thenReturn("main");
                        when(defaultFlavor.getResourceConfigurations()).thenReturn(
                                Collections.singleton("cs"));

                        ProductFlavorContainer containerBeta =
                                mock(ProductFlavorContainer.class);
                        when(containerBeta.getProductFlavor()).thenReturn(flavorBeta);

                        ProductFlavorContainer containerFree =
                                mock(ProductFlavorContainer.class);
                        when(containerFree.getProductFlavor()).thenReturn(flavorFree);

                        ProductFlavorContainer containerPaid =
                                mock(ProductFlavorContainer.class);
                        when(containerPaid.getProductFlavor()).thenReturn(flavorPaid);

                        ProductFlavorContainer containerNormal =
                                mock(ProductFlavorContainer.class);
                        when(containerNormal.getProductFlavor()).thenReturn(flavorNormal);

                        ProductFlavorContainer defaultContainer =
                                mock(ProductFlavorContainer.class);
                        when(defaultContainer.getProductFlavor()).thenReturn(defaultFlavor);

                        List<ProductFlavorContainer> containers = Arrays.asList(
                                containerPaid, containerFree, containerNormal, containerBeta
                        );

                        AndroidProject project = mock(AndroidProject.class);
                        when(project.getProductFlavors()).thenReturn(containers);
                        when(project.getDefaultConfig()).thenReturn(defaultContainer);
                        return project;
                    }

                    @Nullable
                    @Override
                    public Variant getCurrentVariant() {
                        List<String> productFlavorNames = Arrays.asList("free", "beta");
                        Variant mock = mock(Variant.class);
                        when(mock.getProductFlavors()).thenReturn(productFlavorNames);
                        return mock;
                    }
                };
            }
        };
    }
}
