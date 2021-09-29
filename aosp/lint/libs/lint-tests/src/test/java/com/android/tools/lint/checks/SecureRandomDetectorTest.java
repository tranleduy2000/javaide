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
public class SecureRandomDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new SecureRandomDetector();
    }

    public void test1() throws Exception {
        assertEquals(
            "src/test/pkg/SecureRandomTest.java:12: Warning: It is dangerous to seed SecureRandom with the current time because that value is more predictable to an attacker than the default seed. [SecureRandom]\n" +
            "        random1.setSeed(System.currentTimeMillis()); // OK\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:14: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random1.setSeed(0); // Wrong\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:15: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random1.setSeed(1); // Wrong\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:16: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random1.setSeed((int)1023); // Wrong\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:17: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random1.setSeed(1023L); // Wrong\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:18: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random1.setSeed(FIXED_SEED); // Wrong\n" +
            "                ~~~~~~~\n" +
            "src/test/pkg/SecureRandomTest.java:28: Warning: Do not call setSeed() on a SecureRandom with a fixed seed: it is not secure. Use getSeed(). [SecureRandom]\n" +
            "        random3.setSeed(0); // Wrong: owner is java/util/Random, but applied to SecureRandom object\n" +
            "                ~~~~~~~\n" +
            "0 errors, 7 warnings\n" +
            "",
            // Missing error on line 40, using flow analysis to determine that the seed byte
            // array passed into the SecureRandom constructor is static.

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/SecureRandomTest.java.txt=>src/test/pkg/SecureRandomTest.java",
                "bytecode/SecureRandomTest.class.data=>bin/classes/test/pkg/SecureRandomTest.class"
                ));
    }
}
