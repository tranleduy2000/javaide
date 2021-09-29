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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("SpellCheckingInspection")
public class SecureRandomGeneratorDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new SecureRandomGeneratorDetector();
    }

    public void testWithoutWorkaround() throws Exception {
        assertEquals(""
                + "src/test/pkg/PrngCalls.java:13: Warning: Potentially insecure random numbers "
                + "on Android 4.3 and older. Read "
                + "https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html"
                + " for more info. [TrulyRandom]\n"
                + "        KeyGenerator generator = KeyGenerator.getInstance(\"AES\", \"BC\");\n"
                + "                                              ~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/PrngCalls.java.txt=>src/test/pkg/PrngCalls.java",
                "bytecode/PrngCalls.class.data=>bin/classes/test/pkg/PrngCalls.class"
            ));
    }

    public void testWithWorkaround() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/PrngCalls.java.txt=>src/test/pkg/PrngCalls.java",
                "bytecode/PrngCalls.class.data=>bin/classes/test/pkg/PrngCalls.class",
                "bytecode/PrngWorkaround$LinuxPRNGSecureRandom.class.data=>bin/classes/test/pkg/PrngWorkaround$LinuxPRNGSecureRandom.class",
                "bytecode/PrngWorkaround$LinuxPRNGSecureRandomProvider.class.data=>bin/classes/test/pkg/PrngWorkaround$LinuxPRNGSecureRandomProvider.class",
                "bytecode/PrngWorkaround.class.data=>bin/classes/test/pkg/PrngWorkaround.class"
            ));
    }

    public void testCipherInit() throws Exception {
        assertEquals(""
                + "src/test/pkg/CipherTest1.java:11: Warning: Potentially insecure random "
                + "numbers on Android 4.3 and older. Read "
                + "https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html"
                + " for more info. [TrulyRandom]\n"
                + "        cipher.init(Cipher.WRAP_MODE, key); // FLAG\n"
                + "               ~~~~\n"
                + "0 errors, 1 warnings\n",
                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                        "bytecode/CipherTest1.java.txt=>src/test/pkg/CipherTest1.java",
                        "bytecode/CipherTest1.class.data=>bin/classes/test/pkg/CipherTest1.class"
                ));
    }

    public void testGetArity()  {
        assertEquals(2, SecureRandomGeneratorDetector.getDescArity("(ILjava/security/Key;)V"));
        assertEquals(0, SecureRandomGeneratorDetector.getDescArity("()V"));
        assertEquals(1, SecureRandomGeneratorDetector.getDescArity("(I)V"));
        assertEquals(3, SecureRandomGeneratorDetector.getDescArity(
                "(Ljava/lang/String;Ljava/lang/String;I)V"));
        assertEquals(0, SecureRandomGeneratorDetector.getDescArity("()Lfoo/bar/Baz;"));
    }
}
