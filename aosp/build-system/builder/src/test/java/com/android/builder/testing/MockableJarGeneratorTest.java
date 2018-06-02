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

package com.android.builder.testing;

import com.android.testutils.TestUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MockableJarGeneratorTest extends TestCase {

    public void testJarRewriting() throws Exception {
        MockableJarGenerator generator = new MockableJarGenerator(true);

        File inputJar = new File(TestUtils.getRoot("testing"), "non-mockable.jar");
        File outputJar = new File(Files.createTempDir(), "mockable.jar");

        generator.createMockableJar(inputJar, outputJar);

        assertTrue(outputJar.exists());

        Set<String> expectedEntries = ImmutableSet.of(
                "META-INF/",
                "META-INF/MANIFEST.MF",
                "NonFinalClass.class",
                "FinalClass.class");

        Set<String> actualEntries = Sets.newHashSet();
        JarFile jarFile = new JarFile(outputJar);
        for (JarEntry entry : Collections.list(jarFile.entries())) {
            actualEntries.add(entry.getName());
        }

        assertEquals(expectedEntries, actualEntries);
        // TODO: Verify bytecode?

        jarFile.close();
    }
}