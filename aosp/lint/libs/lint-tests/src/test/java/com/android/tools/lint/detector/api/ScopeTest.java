/*
 * Copyright (C) 2011 The Android Open Source Project
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

import junit.framework.TestCase;

import java.util.EnumSet;

@SuppressWarnings("javadoc")
public class ScopeTest extends TestCase {
    public void testIntersect() {
        assertEquals(Scope.RESOURCE_FILE_SCOPE,
                Scope.intersect(Scope.RESOURCE_FILE_SCOPE, Scope.RESOURCE_FILE_SCOPE));

        assertEquals(EnumSet.of(Scope.RESOURCE_FILE),
                Scope.intersect(
                        EnumSet.of(Scope.RESOURCE_FILE),
                        EnumSet.of(Scope.RESOURCE_FILE)));

        assertEquals(EnumSet.of(Scope.RESOURCE_FILE),
                Scope.intersect(
                        EnumSet.of(Scope.RESOURCE_FILE, Scope.JAVA_FILE),
                        EnumSet.of(Scope.RESOURCE_FILE)));

        assertEquals(EnumSet.of(Scope.JAVA_FILE),
                Scope.intersect(
                        EnumSet.of(Scope.RESOURCE_FILE, Scope.JAVA_FILE),
                        EnumSet.of(Scope.JAVA_FILE)));

        assertEquals(EnumSet.of(Scope.RESOURCE_FILE),
                Scope.intersect(
                        EnumSet.of(Scope.RESOURCE_FILE),
                        EnumSet.of(Scope.RESOURCE_FILE, Scope.JAVA_FILE)));

        assertEquals(EnumSet.of(Scope.JAVA_FILE),
                Scope.intersect(
                        EnumSet.of(Scope.JAVA_FILE),
                        EnumSet.of(Scope.RESOURCE_FILE, Scope.JAVA_FILE)));

        assertTrue(Scope.intersect(
                    EnumSet.of(Scope.JAVA_FILE), EnumSet.of(Scope.RESOURCE_FILE)).isEmpty());
    }
}
