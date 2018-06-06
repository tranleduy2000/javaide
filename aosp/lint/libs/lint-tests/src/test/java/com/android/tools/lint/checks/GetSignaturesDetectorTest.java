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

import com.android.tools.lint.detector.api.Detector;

public class GetSignaturesDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new GetSignaturesDetector();
    }

    public void testLintWarningOnSingleGetSignaturesFlag() throws Exception {
        assertEquals(
                "src/test/pkg/GetSignaturesSingleFlagTest.java:9: Information: Reading app signatures from getPackageInfo: The app signatures could be exploited if not validated properly; see issue explanation for details. [PackageManagerGetSignatures]\n"
                        + "            .getPackageInfo(\"some.pkg\", PackageManager.GET_SIGNATURES);\n"
                        + "                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/GetSignaturesSingleFlagTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesSingleFlagTest.java"
                ));
    }

    public void testLintWarningOnGetSignaturesFlagInBitwiseOrExpression() throws Exception {
        assertEquals(
            "src/test/pkg/GetSignaturesBitwiseOrTest.java:11: Information: Reading app signatures from getPackageInfo: The app signatures could be exploited if not validated properly; see issue explanation for details. [PackageManagerGetSignatures]\n"
                    + "            .getPackageInfo(\"some.pkg\", GET_GIDS | GET_SIGNATURES | GET_PROVIDERS);\n"
                    + "                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "0 errors, 1 warnings\n",
            lintProject(
                "src/test/pkg/GetSignaturesBitwiseOrTest.java.txt" +
                        "=>src/test/pkg/GetSignaturesBitwiseOrTest.java"
            ));
    }

    public void testLintWarningOnGetSignaturesFlagInBitwiseXorExpression() throws Exception {
        assertEquals(
                "src/test/pkg/GetSignaturesBitwiseXorTest.java:8: Information: Reading app signatures from getPackageInfo: The app signatures could be exploited if not validated properly; see issue explanation for details. [PackageManagerGetSignatures]\n"
                        + "        getPackageManager().getPackageInfo(\"some.pkg\", PackageManager.GET_SIGNATURES ^ 0x0);\n"
                        + "                                                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/GetSignaturesBitwiseXorTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesBitwiseXorTest.java"
                ));
    }

    public void testLintWarningOnGetSignaturesFlagInBitwiseAndExpression() throws Exception {
        assertEquals(
                "src/test/pkg/GetSignaturesBitwiseAndTest.java:9: Information: Reading app signatures from getPackageInfo: The app signatures could be exploited if not validated properly; see issue explanation for details. [PackageManagerGetSignatures]\n"
                        + "            Integer.MAX_VALUE & PackageManager.GET_SIGNATURES);\n"
                        + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/GetSignaturesBitwiseAndTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesBitwiseAndTest.java"
                ));
    }

    public void testLintWarningOnFlagsInStaticField() throws Exception {
        assertEquals(
                "src/test/pkg/GetSignaturesStaticFieldTest.java:9: Information: Reading app signatures from getPackageInfo: The app signatures could be exploited if not validated properly; see issue explanation for details. [PackageManagerGetSignatures]\n"
                        + "        getPackageManager().getPackageInfo(\"some.pkg\", FLAGS);\n"
                        + "                                                       ~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/GetSignaturesStaticFieldTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesStaticFieldTest.java"
                ));
    }

    public void testNoLintWarningOnFlagsInLocalVariable() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "src/test/pkg/GetSignaturesLocalVariableTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesLocalVariableTest.java"
                ));
    }

    public void testNoLintWarningOnGetSignaturesWithNoFlag() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "src/test/pkg/GetSignaturesNoFlagTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesNoFlagTest.java"
                ));
    }

    public void testNoLintWarningOnGetPackageInfoOnNonPackageManagerClass() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "src/test/pkg/GetSignaturesNotPackageManagerTest.java.txt" +
                                "=>src/test/pkg/GetSignaturesNotPackageManagerTest.java"
                ));
    }
}
