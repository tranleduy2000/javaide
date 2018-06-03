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
package com.android.build.gradle

import com.android.annotations.NonNull
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.SdkHandler
import com.android.build.gradle.internal.test.BaseTest
import com.android.builder.model.SigningConfig
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * Tests for the public DSL of the Lib plugin ('com.android.library')
 */
public class LibraryPluginDslTest extends BaseTest {

    @Override
    protected void setUp() throws Exception {
        SdkHandler.testSdkFolder = new File(System.getenv("ANDROID_HOME"))
    }

    public void testBasic() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "basic")).build()

        project.apply plugin: 'com.android.library'

        project.android {
            compileSdkVersion 21
        }

        project.afterEvaluate {
            Set<LibraryVariant> variants = project.android.libraryVariants
            assertEquals(2, variants.size())

            Set<TestVariant> testVariants = project.android.testVariants
            assertEquals(1, testVariants.size())

            checkTestedVariant("Debug", "Test", variants, testVariants)
            checkNonTestedVariant("Release", variants)
        }
    }

    /**
     * test that debug build type maps to the SigningConfig object as the signingConfig container
     * @throws Exception
     */
    public void testDebugSigningConfig() throws Exception {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "basic")).build()

        project.apply plugin: 'com.android.library'

        project.android {
            compileSdkVersion 15

            signingConfigs {
                debug {
                    storePassword = "foo"
                }
            }
        }

        SigningConfig signingConfig = project.android.buildTypes.debug.signingConfig

        assertEquals(project.android.signingConfigs.debug, signingConfig)
        assertEquals("foo", signingConfig.storePassword)
    }

    private static void checkTestedVariant(@NonNull String variantName,
                                           @NonNull String testedVariantName,
                                           @NonNull Set<LibraryVariant> variants,
                                           @NonNull Set<TestVariant> testVariants) {
        LibraryVariant variant = findNamedItem(variants, variantName)
        assertNotNull(variant)
        assertNotNull(variant.testVariant)
        assertEquals(testedVariantName, variant.testVariant.name)
        assertEquals(variant.testVariant, findNamedItem(testVariants, testedVariantName))
        checkLibraryTasks(variant)
        checkTestTasks(variant.testVariant)
    }

    private static void checkNonTestedVariant(@NonNull String variantName,
                                              @NonNull Set<LibraryVariant> variants) {
        LibraryVariant variant = findNamedItem(variants, variantName)
        assertNotNull(variant)
        assertNull(variant.testVariant)
        checkLibraryTasks(variant)
    }

    private static void checkTestTasks(@NonNull TestVariant variant) {
        assertNotNull(variant.processManifest)
        assertNotNull(variant.aidlCompile)
        assertNotNull(variant.mergeResources)
        assertNotNull(variant.mergeAssets)
        assertNotNull(variant.processResources)
        assertNotNull(variant.generateBuildConfig)
        assertNotNull(variant.javaCompile)
        assertNotNull(variant.processJavaResources)
        assertNotNull(variant.dex)
        assertNotNull(variant.packageApplication)

        assertNotNull(variant.assemble)
        assertNotNull(variant.uninstall)

        assertNull(variant.zipAlign)

        if (variant.isSigningReady()) {
            assertNotNull(variant.install)
        } else {
            assertNull(variant.install)
        }

        assertNotNull(variant.connectedInstrumentTest)
    }

    private static void checkLibraryTasks(@NonNull LibraryVariant variant) {
        assertNotNull(variant.processManifest)
        assertNotNull(variant.aidlCompile)
        assertNotNull(variant.processResources)
        assertNotNull(variant.generateBuildConfig)
        assertNotNull(variant.javaCompile)
        assertNotNull(variant.processJavaResources)

        assertNotNull(variant.assemble)
    }
}