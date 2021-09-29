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

package com.android.tools.lint.detector.api;

import static com.android.tools.lint.detector.api.Scope.ALL;
import static com.android.tools.lint.detector.api.Scope.ALL_RESOURCES_SCOPE;
import static com.android.tools.lint.detector.api.Scope.ALL_RESOURCE_FILES;
import static com.android.tools.lint.detector.api.Scope.CLASS_FILE;
import static com.android.tools.lint.detector.api.Scope.CLASS_FILE_SCOPE;
import static com.android.tools.lint.detector.api.Scope.JAVA_FILE;
import static com.android.tools.lint.detector.api.Scope.JAVA_FILE_SCOPE;
import static com.android.tools.lint.detector.api.Scope.MANIFEST_SCOPE;
import static com.android.tools.lint.detector.api.Scope.RESOURCE_FILE;
import static com.android.tools.lint.detector.api.Scope.RESOURCE_FILE_SCOPE;

import com.android.tools.lint.checks.ApiDetector;
import com.android.tools.lint.checks.DetectMissingPrefix;
import com.android.tools.lint.checks.DuplicateResourceDetector;
import com.android.tools.lint.checks.RtlDetector;

import junit.framework.TestCase;

import java.util.EnumSet;

public class ImplementationTest extends TestCase {
    @SuppressWarnings("unchecked")
    public void testIsAdequate() throws Exception {
        Implementation implementation = new Implementation(Detector.class, ALL_RESOURCES_SCOPE);
        assertTrue(implementation.isAdequate(ALL_RESOURCES_SCOPE));
        assertTrue(implementation.isAdequate(ALL));
        assertTrue(implementation.isAdequate(EnumSet.of(ALL_RESOURCE_FILES)));
        assertFalse(implementation.isAdequate(JAVA_FILE_SCOPE));
        assertFalse(implementation.isAdequate(RESOURCE_FILE_SCOPE));
        assertTrue(implementation.isAdequate(EnumSet.of(ALL_RESOURCE_FILES, JAVA_FILE)));

        implementation = new Implementation(Detector.class, ALL_RESOURCES_SCOPE,
                RESOURCE_FILE_SCOPE);
        assertTrue(implementation.isAdequate(ALL_RESOURCES_SCOPE));
        assertTrue(implementation.isAdequate(EnumSet.of(ALL_RESOURCE_FILES)));
        assertFalse(implementation.isAdequate(JAVA_FILE_SCOPE));
        assertTrue(implementation.isAdequate(EnumSet.of(ALL_RESOURCE_FILES, JAVA_FILE)));
        assertTrue(implementation.isAdequate(RESOURCE_FILE_SCOPE));

        implementation = new Implementation(Detector.class, EnumSet.of(RESOURCE_FILE, JAVA_FILE));
        assertTrue(implementation.isAdequate(EnumSet.of(RESOURCE_FILE, JAVA_FILE)));
        assertTrue(implementation.isAdequate(EnumSet.of(RESOURCE_FILE, JAVA_FILE, CLASS_FILE)));
        assertFalse(implementation.isAdequate(ALL_RESOURCES_SCOPE));
        assertFalse(implementation.isAdequate(JAVA_FILE_SCOPE));
        assertFalse(implementation.isAdequate(RESOURCE_FILE_SCOPE));
        assertFalse(implementation.isAdequate(EnumSet.of(ALL_RESOURCE_FILES, JAVA_FILE)));
        assertFalse(implementation.isAdequate(EnumSet.of(RESOURCE_FILE, CLASS_FILE)));
        assertTrue(implementation.isAdequate(ALL));

        implementation = new Implementation(Detector.class, EnumSet.of(RESOURCE_FILE, JAVA_FILE),
                RESOURCE_FILE_SCOPE, JAVA_FILE_SCOPE);
        assertTrue(implementation.isAdequate(JAVA_FILE_SCOPE));
        assertTrue(implementation.isAdequate(RESOURCE_FILE_SCOPE));
        assertTrue(implementation.isAdequate(ALL));

        assertFalse(ApiDetector.UNSUPPORTED.getImplementation().isAdequate(JAVA_FILE_SCOPE));
        assertTrue(ApiDetector.UNSUPPORTED.getImplementation().isAdequate(CLASS_FILE_SCOPE));
        assertTrue(ApiDetector.UNSUPPORTED.getImplementation().isAdequate(RESOURCE_FILE_SCOPE));
        assertTrue(ApiDetector.UNSUPPORTED.getImplementation().isAdequate(MANIFEST_SCOPE));
        assertTrue(DetectMissingPrefix.MISSING_NAMESPACE.getImplementation().isAdequate(
                RESOURCE_FILE_SCOPE));
        assertTrue(DetectMissingPrefix.MISSING_NAMESPACE.getImplementation().isAdequate(
                MANIFEST_SCOPE));
        assertFalse(DetectMissingPrefix.MISSING_NAMESPACE.getImplementation().isAdequate(
                JAVA_FILE_SCOPE));
        assertTrue(RtlDetector.COMPAT.getImplementation().isAdequate(MANIFEST_SCOPE));
        assertTrue(DuplicateResourceDetector.ISSUE.getImplementation().isAdequate(
                RESOURCE_FILE_SCOPE));
    }
}
