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
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.SdkHandler
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.internal.test.BaseTest
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import static com.android.build.gradle.DslTestUtil.DEFAULT_VARIANTS
import static com.android.build.gradle.DslTestUtil.countVariants
/**
 * Tests for the public DSL of the App plugin ("android")
 */
public class AppPluginDslTest extends BaseTest {

    @Override
    protected void setUp() throws Exception {
        SdkHandler.testSdkFolder = new File(System.getenv("ANDROID_HOME"))
    }

    public void testBasic() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(DEFAULT_VARIANTS.size(), plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(2, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(1, testVariants.size())

        checkTestedVariant("debug", "debugAndroidTest", variants, testVariants)
        checkNonTestedVariant("release", variants)
    }

    /**
     * Same as Basic but with a slightly different DSL.
     */
    public void testBasic2() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(DEFAULT_VARIANTS.size(), plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(2, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(1, testVariants.size())

        checkTestedVariant("debug", "debugAndroidTest", variants, testVariants)
        checkNonTestedVariant("release", variants)
    }

    public void testBasicWithStringTarget() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion "android-" + COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(DEFAULT_VARIANTS.size(), plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(2, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(1, testVariants.size())

        checkTestedVariant("debug", "debugAndroidTest", variants, testVariants)
        checkNonTestedVariant("release", variants)
    }

    public void testMultiRes() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/multires")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            sourceSets {
                main {
                    res {
                        srcDirs 'src/main/res1', 'src/main/res2'
                    }
                }
            }
        }

        // nothing to be done here. If the DSL fails, it'll throw an exception
    }

    public void testBuildTypes() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
            testBuildType "staging"

            buildTypes {
                staging {
                    signingConfig signingConfigs.debug
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(
                countVariants(appVariants: 3, unitTest: 3, androidTests: 1),
                plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created

        // does not include tests
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(3, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(1, testVariants.size())

        checkTestedVariant("staging", "stagingAndroidTest", variants, testVariants)

        checkNonTestedVariant("debug", variants)
        checkNonTestedVariant("release", variants)
    }

    public void testFlavors() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            productFlavors {
                flavor1 {

                }
                flavor2 {

                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(
                countVariants(appVariants: 4, unitTest: 4, androidTests: 2),
                plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created

        // does not include tests
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(4, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(2, testVariants.size())

        checkTestedVariant("flavor1Debug", "flavor1DebugAndroidTest", variants, testVariants)
        checkTestedVariant("flavor2Debug", "flavor2DebugAndroidTest", variants, testVariants)

        checkNonTestedVariant("flavor1Release", variants)
        checkNonTestedVariant("flavor2Release", variants)
    }

    public void testMultiFlavors() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            flavorDimensions   "dimension1", "dimension2"

            productFlavors {
                f1 {
                    flavorDimension   "dimension1"
                }
                f2 {
                    flavorDimension   "dimension1"
                }

                fa {
                    flavorDimension   "dimension2"
                }
                fb {
                    flavorDimension   "dimension2"
                }
                fc {
                    flavorDimension   "dimension2"
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(
                countVariants(appVariants: 12, unitTest: 12, androidTests: 6),
                plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created

        // does not include tests
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(12, variants.size())

        Set<TestVariant> testVariants = project.android.testVariants
        assertEquals(6, testVariants.size())

        checkTestedVariant("f1FaDebug", "f1FaDebugAndroidTest", variants, testVariants)
        checkTestedVariant("f1FbDebug", "f1FbDebugAndroidTest", variants, testVariants)
        checkTestedVariant("f1FcDebug", "f1FcDebugAndroidTest", variants, testVariants)
        checkTestedVariant("f2FaDebug", "f2FaDebugAndroidTest", variants, testVariants)
        checkTestedVariant("f2FbDebug", "f2FbDebugAndroidTest", variants, testVariants)
        checkTestedVariant("f2FcDebug", "f2FcDebugAndroidTest", variants, testVariants)

        checkNonTestedVariant("f1FaRelease", variants)
        checkNonTestedVariant("f1FbRelease", variants)
        checkNonTestedVariant("f1FcRelease", variants)
        checkNonTestedVariant("f2FaRelease", variants)
        checkNonTestedVariant("f2FbRelease", variants)
        checkNonTestedVariant("f2FcRelease", variants)
    }

    public void testSourceSetsApi() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
        }

        // query the sourceSets, will throw if missing
        println project.android.sourceSets.main.java.srcDirs
        println project.android.sourceSets.main.resources.srcDirs
        println project.android.sourceSets.main.manifest.srcFile
        println project.android.sourceSets.main.res.srcDirs
        println project.android.sourceSets.main.assets.srcDirs
    }

    public void testObfuscationMappingFile() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            buildTypes {
                release {
                    minifyEnabled true
                    proguardFile getDefaultProguardFile('proguard-android.txt')
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)

        plugin.createAndroidTasks(false)
        assertEquals(DEFAULT_VARIANTS.size(), plugin.variantManager.variantDataList.size())

        // we can now call this since the variants/tasks have been created

        // does not include tests
        Set<ApplicationVariant> variants = project.android.applicationVariants
        assertEquals(2, variants.size())

        for (ApplicationVariant variant : variants) {
            if ("release".equals(variant.getBuildType().getName())) {
                assertNotNull(variant.getMappingFile())
            } else {
                assertNull(variant.getMappingFile())
            }
        }
    }

    public void testProguardDsl() throws Exception {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            buildTypes {
                release {
                    proguardFile 'file1.1'
                    proguardFiles 'file1.2', 'file1.3'
                }

                custom {
                    proguardFile 'file3.1'
                    proguardFiles 'file3.2', 'file3.3'
                    proguardFiles = ['file3.1']
                }
            }

            productFlavors {
                f1 {
                    proguardFile 'file2.1'
                    proguardFiles 'file2.2', 'file2.3'
                }

                f2  {

                }

                f3 {
                    proguardFile 'file4.1'
                    proguardFiles 'file4.2', 'file4.3'
                    proguardFiles = ['file4.1']
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        def variantsData = plugin.variantManager.variantDataList
        Map<String, GradleVariantConfiguration> variantMap =
                variantsData.collectEntries {[it.name, it.variantConfiguration]}

        def expectedFiles = [
                f1Release: ["file1.1", "file1.2", "file1.3", "file2.1", "file2.2", "file2.3"],
                f1Debug: ["file2.1", "file2.2", "file2.3"],
                f2Release: ["file1.1", "file1.2", "file1.3"],
                f2Debug: [],
                f2Custom: ["file3.1"],
                f3Custom: ["file3.1", "file4.1"],
        ]

        expectedFiles.each { name, expected ->
            def actual = variantMap[name].getProguardFiles(false, [])
            assert (actual*.name as Set) == (expected as Set), name
        }
    }

    public void testProguardDsl_initWith() throws Exception {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            buildTypes {
                common {
                    testProguardFile 'file1.1'
                }

                custom1.initWith(buildTypes.common)
                custom2.initWith(buildTypes.common)

                custom1 {
                    testProguardFile 'file2.1'
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        def variantsData = plugin.variantManager.variantDataList
        Map<String, GradleVariantConfiguration> variantMap =
                variantsData.collectEntries {[it.name, it.variantConfiguration]}

        def expectedFiles = [
                common: ["file1.1"],
                custom1: ["file1.1", "file2.1"],
                custom2: ["file1.1"],
        ]

        expectedFiles.each { name, expected ->
            List<File> actual = variantMap[name].testProguardFiles
            assert (actual*.name as Set) == (expected as Set), name
        }
    }

    public void testSettingLanguageLevelFromCompileSdk() {
        def testLanguageLevel = { version, expectedLanguageLevel, useJack ->
            Project project = ProjectBuilder.builder().withProjectDir(
                    new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

            project.apply plugin: 'com.android.application'
            project.android {
                compileSdkVersion version
                buildToolsVersion '20.0.0'
            }

            AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
            plugin.createAndroidTasks(false)

            assertEquals(
                    "target compatibility for ${version}",
                    expectedLanguageLevel.toString(),
                    project.compileReleaseJavaWithJavac.targetCompatibility)
            assertEquals(
                    "source compatibility for ${version}",
                    expectedLanguageLevel.toString(),
                    project.compileReleaseJavaWithJavac.sourceCompatibility)
        }

        for (useJack in [true, false]) {
            def propName = 'java.specification.version'
            String originalVersion = System.getProperty(propName)
            try{
                System.setProperty(propName, '1.7')
                testLanguageLevel('android-15', JavaVersion.VERSION_1_6, useJack)
                testLanguageLevel('android-21', JavaVersion.VERSION_1_7, useJack)
                testLanguageLevel('android-21', JavaVersion.VERSION_1_7, useJack)
                testLanguageLevel('Google Inc.:Google APIs:22', JavaVersion.VERSION_1_7, useJack)

                System.setProperty(propName, '1.6')
                testLanguageLevel(15, JavaVersion.VERSION_1_6, useJack)
                testLanguageLevel(21, JavaVersion.VERSION_1_6, useJack)
                testLanguageLevel('android-21', JavaVersion.VERSION_1_6, useJack)
                testLanguageLevel('Google Inc.:Google APIs:22', JavaVersion.VERSION_1_6, useJack)
            } finally {
                System.setProperty(propName, originalVersion)
            }
        }
    }

    public void testSettingLanguageLevelFromCompileSdk_dontOverride() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'
        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            compileOptions {
                sourceCompatibility JavaVersion.VERSION_1_6
                targetCompatibility JavaVersion.VERSION_1_6
            }
        }
        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        assertEquals(
                JavaVersion.VERSION_1_6.toString(),
                project.compileReleaseJavaWithJavac.targetCompatibility)
        assertEquals(
                JavaVersion.VERSION_1_6.toString(),
                project.compileReleaseJavaWithJavac.sourceCompatibility)
    }

    public void testMockableJarName() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion "Google Inc.:Google APIs:" + COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        def mockableJarFile = plugin.taskManager.createMockableJar.outputFile
        assertFalse(mockableJarFile.absolutePath.contains(":"))
        assertEquals("mockable-Google-Inc.-Google-APIs-21.jar", mockableJarFile.name)
    }

    public void testEncoding() {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'
        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            compileOptions {
                encoding "foo"
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        assertEquals(
                "foo",
                project.compileReleaseJavaWithJavac.options.encoding)
    }

    public void testInstrumentationRunnerArguments_merging() throws Exception {
        Project project = ProjectBuilder.builder().withProjectDir(
                new File(testDir, "${FOLDER_TEST_PROJECTS}/basic")).build()

        project.apply plugin: 'com.android.application'

        project.android {
            compileSdkVersion COMPILE_SDK_VERSION
            buildToolsVersion '20.0.0'

            defaultConfig {
                testInstrumentationRunnerArguments(value: "default", size: "small")
            }

            productFlavors {
                f1 {
                }

                f2  {
                    testInstrumentationRunnerArgument "value", "f2"
                }

                f3  {
                    testInstrumentationRunnerArguments["otherValue"] = "f3"
                }

                f4  {
                    testInstrumentationRunnerArguments(otherValue: "f4.1")
                    testInstrumentationRunnerArguments = [otherValue: "f4.2"]
                }
            }
        }

        AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
        plugin.createAndroidTasks(false)

        def variantsData = plugin.variantManager.variantDataList
        Map<String, GradleVariantConfiguration> variantMap =
                variantsData.collectEntries {[it.name, it.variantConfiguration]}

        def expectedArgs = [
                f1Debug: [value: "default", size: "small"],
                f2Debug: [value: "f2", size: "small"],
                f3Debug: [value: "default", size: "small", otherValue: "f3"],
                f4Debug: [value: "default", size: "small", otherValue: "f4.2"],
        ]

        expectedArgs.each { name, expected ->
            assert expected == variantMap[name].instrumentationRunnerArguments
        }
    }

    private static void checkTestedVariant(@NonNull String variantName,
                                           @NonNull String testedVariantName,
                                           @NonNull Collection<ApplicationVariant> variants,
                                           @NonNull Set<TestVariant> testVariants) {
        ApplicationVariant variant = findNamedItem(variants, variantName, "variantData")
        assertNotNull(variant.testVariant)
        assertEquals(testedVariantName, variant.testVariant.name)
        assertEquals(variant.testVariant, findNamedItemMaybe(testVariants, testedVariantName))
        checkTasks(variant)
        checkTasks(variant.testVariant)
    }

    private static void checkNonTestedVariant(@NonNull String variantName,
                                              @NonNull Set<ApplicationVariant> variants) {
        ApplicationVariant variant = findNamedItem(variants, variantName, "variantData")
        assertNull(variant.testVariant)
        checkTasks(variant)
    }

    private static void checkTasks(@NonNull ApkVariant variant) {
        boolean isTestVariant = variant instanceof TestVariant;

        assertNotNull(variant.aidlCompile)
        assertNotNull(variant.mergeResources)
        assertNotNull(variant.mergeAssets)
        assertNotNull(variant.generateBuildConfig)
        assertNotNull(variant.javaCompile)
        assertNotNull(variant.processJavaResources)
        assertNotNull(variant.dex)
        assertNotNull(variant.assemble)
        assertNotNull(variant.uninstall)

        assertFalse(variant.outputs.isEmpty())

        for (BaseVariantOutput baseVariantOutput : variant.outputs) {
            assertTrue(baseVariantOutput instanceof ApkVariantOutput)
            ApkVariantOutput apkVariantOutput = (ApkVariantOutput) baseVariantOutput

            assertNotNull(apkVariantOutput.processManifest)
            assertNotNull(apkVariantOutput.processResources)
            assertNotNull(apkVariantOutput.packageApplication)
        }

        if (variant.isSigningReady()) {
            assertNotNull(variant.install)

            for (BaseVariantOutput baseVariantOutput : variant.outputs) {
                ApkVariantOutput apkVariantOutput = (ApkVariantOutput) baseVariantOutput

                // tested variant are never zipAligned.
                if (!isTestVariant && variant.buildType.zipAlignEnabled) {
                    assertNotNull(apkVariantOutput.zipAlign)
                } else {
                    assertNull(apkVariantOutput.zipAlign)
                }
            }
        } else {
            assertNull(variant.install)
        }

        if (isTestVariant) {
            TestVariant testVariant = variant as TestVariant
            assertNotNull(testVariant.connectedInstrumentTest)
            assertNotNull(testVariant.testedVariant)
        }
    }
}
