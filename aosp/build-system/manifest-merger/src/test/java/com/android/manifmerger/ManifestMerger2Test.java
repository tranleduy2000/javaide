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

package com.android.manifmerger;

import static com.android.manifmerger.ManifestMerger2.SystemProperty;
import static com.android.manifmerger.MergingReport.Record;

import com.android.annotations.Nullable;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests for the {@link com.android.manifmerger.ManifestMerger2} class
 */
public class ManifestMerger2Test extends ManifestMergerTest {

    // so far, I only support 3 original tests.
    private static String[] sDataFiles = new String[]{
            "00_noop",
            "03_inject_attributes.xml",
            "05_inject_package.xml",
            "05_inject_package_placeholder.xml",
            "05_inject_package_with_overlays.xml",
            "06_inject_attributes_with_specific_prefix.xml",
            "07_no_package_provided.xml",
            "08_no_library_package_provided.xml",
            "09_overlay_package_provided.xml",
            "08b_library_injection.xml",
            "09b_overlay_package_different.xml",
            "09c_overlay_package_not_provided.xml",
            "10_activity_merge",
            "11_activity_dup",
            "12_alias_dup",
            "13_service_dup",
            "14_receiver_dup",
            "15_provider_dup",
            "16_fqcn_merge",
            "17_fqcn_conflict",
            "18_fqcn_success",
            "20_uses_lib_merge",
            "21_uses_main_errors",
            "22_uses_lib_errors",
            "25_permission_merge",
            "26_permission_dup",
            "28_uses_perm_merge",
            "29_uses_perm_selector",
            "29b_uses_perm_invalidSelector",
            "30_uses_sdk_ok",
            "32_uses_sdk_minsdk_ok",
            "33_uses_sdk_minsdk_conflict",
            "33b_uses_sdk_minsdk_override.xml",
            "33c_uses_sdk_minsdk_override_and_conflict.xml",
            "34_inject_uses_sdk_no_dup.xml",
            "36_uses_sdk_targetsdk_warning",
            "40_uses_feat_merge",
            "41_uses_feat_errors",
            "45_uses_feat_gles_once",
            "47_uses_feat_gles_conflict",
            "50_uses_conf_warning",
            "52_support_screens_warning",
            "54_compat_screens_warning",
            "56_support_gltext_warning",
            "60_merge_order",
            "65_override_app",
            "66_remove_app",
            "67_override_activities",
            "68_override_uses",
            "69_remove_uses",
            "70_expand_fqcns",
            "71_extract_package_prefix",
            "75_app_metadata_merge",
            "76_app_metadata_ignore",
            "77_app_metadata_conflict",
            "78_removeAll",
            "79_custom_node.xml",
    };

    @Override
    protected String getTestDataDirectory() {
        return "data2";
    }

    /**
     * This overrides the default test suite created by junit. The test suite is a bland TestSuite
     * with a dedicated name. We inject as many instances of {@link ManifestMergerTest} in the suite
     * as we have declared data files above.
     *
     * @return A new {@link junit.framework.TestSuite}.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        // Give a non-generic name to our test suite, for better unit reports.
        suite.setName("ManifestMergerTestSuite");

        for (String fileName : sDataFiles) {
            suite.addTest(TestSuite.createTest(ManifestMerger2Test.class, fileName));
        }

        return suite;
    }

    public ManifestMerger2Test(String testName) {
        super(testName);
    }

    /**
     * Processes the data from the given
     * {@link com.android.manifmerger.ManifestMergerTest.TestFiles} by invoking {@link
     * ManifestMerger#process(java.io.File, java.io.File, java.io.File[], java.util.Map, String)}:
     * the given library files are applied consecutively to the main XML document and the output is
     * generated. <p/> Then the expected and actual outputs are loaded into a DOM, dumped again to a
     * String using an XML transform and compared. This makes sure only the structure is checked and
     * that any formatting is ignored in the comparison.
     *
     * @param testFiles The test files to process. Must not be null.
     * @throws Exception when this go wrong.
     */
    @Override
    void processTestFiles(TestFiles testFiles) throws Exception {

        StdLogger stdLogger = new StdLogger(StdLogger.Level.VERBOSE);
        ManifestMerger2.Invoker invoker = ManifestMerger2.newMerger(testFiles.getMain(),
                stdLogger, ManifestMerger2.MergeType.APPLICATION)
                .addLibraryManifests(testFiles.getLibs())
                .addFlavorAndBuildTypeManifests(testFiles.getOverlayFiles())
                .withFeatures(ManifestMerger2.Invoker.Feature.KEEP_INTERMEDIARY_STAGES,
                        ManifestMerger2.Invoker.Feature.REMOVE_TOOLS_DECLARATIONS);

        if (!Strings.isNullOrEmpty(testFiles.getPackageOverride())) {
            invoker.setOverride(SystemProperty.PACKAGE, testFiles.getPackageOverride());
        }

        for (Map.Entry<String, String> injectable : testFiles.getInjectAttributes().entrySet()) {
            SystemProperty systemProperty = getSystemProperty(injectable.getKey());
            if (systemProperty != null) {
                invoker.setOverride(systemProperty, injectable.getValue());
            } else {
                invoker.setPlaceHolderValue(injectable.getKey(), injectable.getValue());
            }
        }

        MergingReport mergeReport = invoker.merge();


        // this is obviously quite hacky, refine once merge output is better defined.
        boolean notExpectingError = !isExpectingError(testFiles.getExpectedErrors());
        mergeReport.log(stdLogger);
        if (mergeReport.getMergedDocument().isPresent()) {

            XmlDocument actualResult = mergeReport.getMergedDocument().get();
            String prettyResult = actualResult.prettyPrint();
            stdLogger.info(prettyResult);

            if (testFiles.getActualResult() != null) {
                FileWriter writer = new FileWriter(testFiles.getActualResult());
                try {
                    writer.append(prettyResult);
                } finally {
                    writer.close();
                }
            }

            if (!notExpectingError) {
                fail("Did not get expected error : " + testFiles.getExpectedErrors());
            }

            XmlDocument expectedResult = TestUtils.xmlDocumentFromString(
                    TestUtils.sourceFile(getClass(), testFiles.getMain().getName()),
                    testFiles.getExpectedResult());
            Optional<String> comparingMessage =
                    expectedResult.compareTo(actualResult);

            if (comparingMessage.isPresent()) {
                Logger.getAnonymousLogger().severe(comparingMessage.get());
                fail(comparingMessage.get());
            }
            // process any warnings.
            if (mergeReport.getResult() == MergingReport.Result.WARNING) {
                compareExpectedAndActualErrors(mergeReport, testFiles.getExpectedErrors());
            }
        } else {
            for (Record record : mergeReport.getLoggingRecords()) {
                Logger.getAnonymousLogger().info("Returned log: " + record);
            }
            compareExpectedAndActualErrors(mergeReport, testFiles.getExpectedErrors());
            assertFalse(notExpectingError);
        }
    }

    private boolean isExpectingError(String expectedOutput) throws IOException {
        StringReader stringReader = new StringReader(expectedOutput);
        BufferedReader reader = new BufferedReader(stringReader);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("ERROR")) return true;
        }
        return false;
    }

    private void compareExpectedAndActualErrors(
            MergingReport mergeReport,
            String expectedOutput) throws IOException {

        StringReader stringReader = new StringReader(expectedOutput);
        BufferedReader reader = new BufferedReader(stringReader);
        String line = reader.readLine();
        List<Record> records = new ArrayList<Record>(mergeReport.getLoggingRecords());
        while (line != null) {
            if (line.startsWith("WARNING") || line.startsWith("ERROR")) {
                String message = line;
                do {
                    line = reader.readLine();
                    if (line != null && line.startsWith("    ")) {
                        message = message + "\n" + line;
                    }
                } while (line != null && line.startsWith("    "));

                // next might generate an exception which will make the test fail when we
                // get unexpected error message.
                if (!findLineInRecords(message, records)) {

                    StringBuilder errorMessage = new StringBuilder();
                    dumpRecords(records, errorMessage);
                    errorMessage.append("Cannot find expected error : \n").append(message);
                    fail(errorMessage.toString());
                }
            }
        }
        // check that we do not have any unexpected error messages.
        if (!records.isEmpty()) {
            StringBuilder message = new StringBuilder();
            dumpRecords(records, message);
            message.append("Unexpected error message(s)");
            fail(message.toString());
        }
    }

    private boolean findLineInRecords(String errorLine, List<Record> records) {
        String severity = errorLine.substring(0, errorLine.indexOf(':'));
        String message = errorLine.substring(errorLine.indexOf(':') + 1);
        for (Record record : records) {
            int indexOfSuggestions = record.getMessage().indexOf("\n\tSuggestion:");
            String messageRecord = indexOfSuggestions != -1
                    ? record.getMessage().substring(0, indexOfSuggestions)
                    : record.getMessage();
            Pattern pattern = Pattern.compile(message);
            Matcher matcher = pattern.matcher(messageRecord.replaceAll("\t", "    "));
            if (matcher.matches() && record.getSeverity() == Record.Severity.valueOf(severity)) {
                records.remove(record);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private SystemProperty getSystemProperty(String name) {
        for (SystemProperty systemProperty : SystemProperty.values()) {
            if (systemProperty.toCamelCase().equals(name)) {
                return systemProperty;
            }
        }
        return null;
    }

    private void dumpRecords(List<Record> records, StringBuilder stringBuilder) {
        stringBuilder.append("\n------------ Records : \n");
        for (Record record : records) {
            stringBuilder.append(record.toString());
            stringBuilder.append("\n");
        }
        stringBuilder.append("------------ End of records.\n");
    }
}
