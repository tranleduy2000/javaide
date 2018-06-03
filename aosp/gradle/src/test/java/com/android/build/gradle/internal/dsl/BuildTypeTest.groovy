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

package com.android.build.gradle.internal.dsl

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.test.BaseTest
import com.android.builder.core.BuilderConstants
import com.android.builder.model.BuildType
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * test that the build type are properly initialized
 */
public class BuildTypeTest extends BaseTest {

    public void testDebug() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion 15
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        BuildType type = plugin.variantManager.buildTypes.get(BuilderConstants.DEBUG).buildType

        assertTrue(type.isDebuggable())
        assertFalse(type.isJniDebuggable())
        assertFalse(type.isRenderscriptDebuggable())
        assertNotNull(type.getSigningConfig())
        assertTrue(type.getSigningConfig().isSigningReady())
        assertTrue(type.isZipAlignEnabled())
    }

    public void testRelease() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion 15
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        BuildType type = plugin.variantManager.buildTypes.get(BuilderConstants.RELEASE).buildType

        assertFalse(type.isDebuggable())
        assertFalse(type.isJniDebuggable())
        assertFalse(type.isRenderscriptDebuggable())
        assertTrue(type.isZipAlignEnabled())
    }

    public void testInitWith() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "basic")).build()

        com.android.build.gradle.internal.dsl.BuildType object1 =
                new com.android.build.gradle.internal.dsl.BuildType("foo", project, project.getLogger())

        // change every value from their default.
        object1.setDebuggable(true)
        object1.setJniDebuggable(true)
        object1.setRenderscriptDebuggable(true)
        object1.setRenderscriptOptimLevel(0)
        object1.setApplicationIdSuffix("foo")
        object1.setVersionNameSuffix("foo")
        object1.setMinifyEnabled(true)
        object1.setSigningConfig(new SigningConfig("blah"))
        object1.setZipAlignEnabled(false)
        object1.setShrinkResources(true)
        object1.setUseJack(Boolean.FALSE)

        com.android.build.gradle.internal.dsl.BuildType object2 =
                new com.android.build.gradle.internal.dsl.BuildType(object1.name, project, project.getLogger())
        object2.initWith(object1)

        assertEquals(object1, object2)
    }
}
