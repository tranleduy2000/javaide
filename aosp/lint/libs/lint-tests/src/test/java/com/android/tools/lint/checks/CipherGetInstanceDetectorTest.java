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

public class CipherGetInstanceDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new CipherGetInstanceDetector();
    }

    public void testCipherGetInstanceAES() throws Exception {
        assertEquals(
                "src/test/pkg/CipherGetInstanceAES.java:7: Warning: Cipher.getInstance should not be called without setting the encryption mode and padding [GetInstance]\n"
                        + "    Cipher.getInstance(\"AES\");\n"
                        + "                       ~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/CipherGetInstanceAES.java.txt=>src/test/pkg/CipherGetInstanceAES.java"
                )
        );
    }

    public void testCipherGetInstanceDES() throws Exception {
        assertEquals(
                "src/test/pkg/CipherGetInstanceDES.java:7: Warning: Cipher.getInstance should not be called without setting the encryption mode and padding [GetInstance]\n"
                        + "    Cipher.getInstance(\"DES\");\n"
                        + "                       ~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/CipherGetInstanceDES.java.txt=>src/test/pkg/CipherGetInstanceDES.java"
                )
        );
    }

    public void testCipherGetInstanceAESECB() throws Exception {
        assertEquals(
                "src/test/pkg/CipherGetInstanceAESECB.java:7: Warning: ECB encryption mode should not be used [GetInstance]\n"
                        + "    Cipher.getInstance(\"AES/ECB/NoPadding\");\n"
                        + "                       ~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/CipherGetInstanceAESECB.java.txt=>src/test/pkg/CipherGetInstanceAESECB.java"
                )
        );
    }

    public void testCipherGetInstanceAESCBC() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "src/test/pkg/CipherGetInstanceAESCBC.java.txt=>src/test/pkg/CipherGetInstanceAESCBC.java"
                )
        );
    }

    public void testResolveConstants() throws Exception {
        assertEquals(
                "src/test/pkg/CipherGetInstanceTest.java:10: Warning: ECB encryption mode should not be used (was \"DES/ECB/NoPadding\") [GetInstance]\n"
                        + "        Cipher des = Cipher.getInstance(Constants.DES);\n"
                        + "                                        ~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "src/test/pkg/CipherGetInstanceTest.java.txt=>src/test/pkg/CipherGetInstanceTest.java"
                )
        );
    }
}