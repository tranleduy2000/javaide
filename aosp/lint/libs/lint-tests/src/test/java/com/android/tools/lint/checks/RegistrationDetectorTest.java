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
public class RegistrationDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new RegistrationDetector();
    }

    public void testRegistered() throws Exception {
        assertEquals(
            "src/test/pkg/OnClickActivity.java:8: Warning: The <activity> test.pkg.OnClickActivity is not registered in the manifest [Registered]\n" +
            "public class OnClickActivity extends Activity {\n" +
            "             ~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/TestProvider.java:8: Warning: The <provider> test.pkg.TestProvider is not registered in the manifest [Registered]\n" +
            "public class TestProvider extends ContentProvider {\n" +
            "             ~~~~~~~~~~~~\n" +
            "src/test/pkg/TestProvider2.java:3: Warning: The <provider> test.pkg.TestProvider2 is not registered in the manifest [Registered]\n" +
            "public class TestProvider2 extends TestProvider {\n" +
            "^\n" +
            "src/test/pkg/TestService.java:7: Warning: The <service> test.pkg.TestService is not registered in the manifest [Registered]\n" +
            "public class TestService extends Service {\n" +
            "             ~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n" +
            "",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
                "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
                "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
                "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
                "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
                "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
                "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class",
                "bytecode/TestReceiver.java.txt=>src/test/pkg/TestReceiver.java",
                "bytecode/TestReceiver.class.data=>bin/classes/test/pkg/TestReceiver.class"
                ));
    }

    public void testNoDot() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/AndroidManifestReg.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "bytecode/CommentsActivity.java.txt=>src/test/pkg/Foo/CommentsActivity.java",
                "bytecode/CommentsActivity.class.data=>bin/classes/test/pkg/Foo/CommentsActivity.class"
                ));
    }

    public void testWrongRegistrations() throws Exception {
        assertEquals(
            "src/test/pkg/OnClickActivity.java:8: Warning: test.pkg.OnClickActivity is a <activity> but is registered in the manifest as a <receiver> [Registered]\n" +
            "public class OnClickActivity extends Activity {\n" +
            "             ~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/TestProvider.java:8: Warning: test.pkg.TestProvider is a <provider> but is registered in the manifest as a <activity> [Registered]\n" +
            "public class TestProvider extends ContentProvider {\n" +
            "             ~~~~~~~~~~~~\n" +
            "src/test/pkg/TestProvider2.java:3: Warning: test.pkg.TestProvider2 is a <provider> but is registered in the manifest as a <service> [Registered]\n" +
            "public class TestProvider2 extends TestProvider {\n" +
            "^\n" +
            "src/test/pkg/TestReceiver.java:7: Warning: test.pkg.TestReceiver is a <receiver> but is registered in the manifest as a <service> [Registered]\n" +
            "public class TestReceiver extends BroadcastReceiver {\n" +
            "             ~~~~~~~~~~~~\n" +
            "src/test/pkg/TestService.java:7: Warning: test.pkg.TestService is a <service> but is registered in the manifest as a <provider> [Registered]\n" +
            "public class TestService extends Service {\n" +
            "             ~~~~~~~~~~~\n" +
            "0 errors, 5 warnings\n" +
            "",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifestWrongRegs.xml=>AndroidManifest.xml",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
                "bytecode/AbstractActivity.java.txt=>src/test/pkg/AbstractActivity.java",
                "bytecode/AbstractActivity.class.data=>bin/classes/test/pkg/AbstractActivity.class",
                "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
                "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
                "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
                "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
                "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
                "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class",
                "bytecode/TestReceiver.java.txt=>src/test/pkg/TestReceiver.java",
                "bytecode/TestReceiver.class.data=>bin/classes/test/pkg/TestReceiver.class",
                "bytecode/TestReceiver$1.class.data=>bin/classes/test/pkg/TestReceiver$1.class"
                ));
    }

    public void testLibraryProjects() throws Exception {
        // If a library project provides additional activities, it is not an error to
        // not register all of those here
        assertEquals(
            "No warnings.",

            lintProject(
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",

                // Library project
                "multiproject/library-manifest.xml=>../LibraryProject/AndroidManifest.xml",
                "multiproject/library.properties=>../LibraryProject/project.properties",

                "bytecode/.classpath=>../LibraryProject/.classpath",
                "bytecode/OnClickActivity.java.txt=>../LibraryProject/src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>../LibraryProject/bin/classes/test/pkg/OnClickActivity.class"
                ));
    }
}
