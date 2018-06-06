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
package com.android.tools.lint.client.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.io.Files;

import java.io.File;
import java.io.StringWriter;

@SuppressWarnings("SpellCheckingInspection")
public class JarFileIssueRegistryTest extends AbstractCheckTest {
    public void testError() {
        try {
            JarFileIssueRegistry.get(new TestLintClient(), new File("bogus"));
            fail("Expected exception for bogus path");
        } catch (Throwable t) {
            // pass
        }
    }

    public void testCached() throws Exception {
        File targetDir = Files.createTempDir();
        File file1 = getTestfile(targetDir, "rules/appcompat.jar.data");
        File file2 = getTestfile(targetDir, "apicheck/unsupported.jar.data");
        assertTrue(file1.getPath(), file1.exists());
        final StringWriter mLoggedWarnings = new StringWriter();
        TestLintClient client = new TestLintClient() {
            @Override
            public void log(@NonNull Severity severity, @Nullable Throwable exception,
                    @Nullable String format, @Nullable Object... args) {
                if (format != null) {
                    mLoggedWarnings.append(String.format(format, args));
                }
            }

        };
        IssueRegistry registry1 = JarFileIssueRegistry.get(client, file1);
        IssueRegistry registry2 = JarFileIssueRegistry.get(client, new File(file1.getPath()));
        assertSame(registry1, registry2);
        IssueRegistry registry3 = JarFileIssueRegistry.get(client, file2);
        assertNotSame(registry1, registry3);

        assertEquals(1, registry1.getIssues().size());
        assertEquals("AppCompatMethod", registry1.getIssues().get(0).getId());

        assertEquals(
                "Custom lint rule jar " + file2.getPath() + " does not contain a valid "
                        + "registry manifest key (Lint-Registry).\n"
                        + "Either the custom jar is invalid, or it uses an outdated API not "
                        + "supported this lint client", mLoggedWarnings.toString());
    }

    @Override
    protected Detector getDetector() {
        fail("Not used in this test");
        return null;
    }
}
