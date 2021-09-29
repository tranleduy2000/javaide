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

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class PrivateKeyDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new PrivateKeyDetector();
    }

    @Override
    protected boolean includeParentPath() {
        return true;
    }

    public void testPrivateKey() throws Exception {
        assertEquals(
                "res/private_key.pem: Error: The res/private_key.pem file seems to be a private key file. Please make sure not to embed this in your APK file. [PackagedPrivateKey]\n" +
                "1 errors, 0 warnings\n",
                lintProject(
                    // Not a private key file
                    "res/values/strings.xml=>res/values/strings/xml",
                    // Private key file
                    "res/private_key.pem=>res/private_key.pem"));
    }
}
