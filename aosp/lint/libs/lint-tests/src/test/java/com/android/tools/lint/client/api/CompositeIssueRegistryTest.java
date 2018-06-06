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

import static com.android.tools.lint.checks.HardcodedValuesDetector.ISSUE;
import static com.android.tools.lint.checks.IconDetector.ICON_COLORS;
import static com.android.tools.lint.checks.IconDetector.ICON_DIP_SIZE;
import static com.android.tools.lint.checks.ManifestDetector.APPLICATION_ICON;
import static com.android.tools.lint.checks.ManifestDetector.DEVICE_ADMIN;
import static com.android.tools.lint.checks.ManifestDetector.DUPLICATE_ACTIVITY;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Issue;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeIssueRegistryTest extends TestCase {
    public void test() {
        IssueRegistry registry1 = new IssueRegistry() {
            @NonNull
            @Override
            public List<Issue> getIssues() {
                return Collections.singletonList(ISSUE);
            }
        };
        IssueRegistry registry2 = new IssueRegistry() {
            @NonNull
            @Override
            public List<Issue> getIssues() {
                return Arrays.asList(APPLICATION_ICON, DEVICE_ADMIN, DUPLICATE_ACTIVITY);
            }
        };
        IssueRegistry registry3 = new IssueRegistry() {
            @NonNull
            @Override
            public List<Issue> getIssues() {
                return Arrays.asList(ICON_COLORS, ICON_DIP_SIZE);
            }
        };

        assertEquals(Collections.singletonList(ISSUE),
                new CompositeIssueRegistry(Collections.singletonList(registry1)).getIssues());

        assertEquals(Arrays.asList(ISSUE, ICON_COLORS, ICON_DIP_SIZE),
                new CompositeIssueRegistry(Arrays.asList(registry1, registry3)).getIssues());

        assertEquals(Arrays.asList(ISSUE, APPLICATION_ICON, DEVICE_ADMIN, DUPLICATE_ACTIVITY,
                ICON_COLORS, ICON_DIP_SIZE), new CompositeIssueRegistry(Arrays.asList(registry1,
                registry2, registry3)).getIssues());
    }
}
