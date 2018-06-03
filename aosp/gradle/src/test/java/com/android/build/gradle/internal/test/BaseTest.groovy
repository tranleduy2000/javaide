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

package com.android.build.gradle.internal.test
import com.android.annotations.NonNull
import com.android.build.tests.AndroidProjectConnector
import com.google.common.base.Joiner
import junit.framework.TestCase

import java.security.CodeSource
/**
 * Base class for tests.
 */
public abstract class BaseTest extends TestCase {

    protected final static int COMPILE_SDK_VERSION = 21;
    protected static final String BUILD_TOOL_VERSION = "22.0.1";

    public static final String FOLDER_TEST_PROJECTS = "test-projects";

    protected File sdkDir;
    protected File ndkDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sdkDir = getSdkDir();
        ndkDir = getNdkDir();
    }

    /**
     * Returns the root dir for the gradle plugin project
     */
    protected File getRootDir() {
        CodeSource source = getClass().getProtectionDomain().getCodeSource()
        if (source != null) {
            URL location = source.getLocation();
            try {
                File dir = new File(location.toURI())
                assertTrue(dir.getPath(), dir.exists())

                File f= dir.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
                return  new File(
                        f,
                        Joiner.on(File.separator).join(
                                "tools",
                                "base",
                                "build-system",
                                "integration-test"));
            } catch (URISyntaxException e) {
                fail(e.getLocalizedMessage())
            }
        }

        fail("Fail to get the tools/build folder")
    }

    /**
     * Returns the root folder for the tests projects.
     */
    protected File getTestDir() {
        return getRootDir()
    }

    /**
     * Returns the SDK folder as built from the Android source tree.
     */
    protected File getSdkDir() {
        String androidHome = System.getenv("ANDROID_HOME");
        if (androidHome != null) {
            File f = new File(androidHome);
            if (f.isDirectory()) {
                return f;
            } else {
                System.out.println("Failed to find SDK in ANDROID_HOME=" + androidHome)
            }
        }

        // get the gradle project root dir.
        File rootDir = getRootDir()

        // go up 3 times and get the root Android dir.
        File androidRootDir = rootDir.getParentFile().getParentFile().getParentFile()

        // get the sdk folder
        String outFolder = "out" + File.separatorChar + "host" + File.separatorChar + "darwin-x86" + File.separatorChar + "sdk";
        File sdk = new File(androidRootDir, outFolder)

        File[] files = sdk.listFiles(new FilenameFilter() {

            @Override
            boolean accept(File file, String s) {
                return s.startsWith("android-sdk_") && new File(file,s ).isDirectory()
            }
        })

        if (files != null && files.length == 1) {
            return files[0]
        }

        fail(String.format(
                "Failed to find a valid SDK. Make sure %s is present at the root of the Android tree, or that ANDROID_HOME is defined.",
                outFolder))
        return null
    }

    /**
     * Returns the SDK folder as built from the Android source tree.
     * @return
     */
    protected static File getNdkDir() {
        String androidHome = System.getenv("ANDROID_NDK_HOME");
        if (androidHome != null) {
            File f = new File(androidHome);
            if (f.isDirectory()) {
                return f;
            } else {
                System.out.println("Failed to find NDK in ANDROID_NDK_HOME=" + androidHome)
            }
        }
    }

    protected File runTasksOn(
            @NonNull String testFolder,
            @NonNull String name,
            @NonNull String gradleVersion,
            @NonNull String... tasks) {
        File project = new File(new File(testDir, testFolder), name)

        return runTasksOn(
                project,
                gradleVersion,
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                tasks);
    }

    protected File runTasksOn(
            @NonNull String testFolder,
            @NonNull String name,
            @NonNull String gradleVersion,
            @NonNull List<String> arguments,
            @NonNull String... tasks) {
        File project = new File(new File(testDir, testFolder), name)

        return runTasksOn(project,
                gradleVersion,
                arguments,
                Collections.<String, String>emptyMap(),
                tasks);
    }

    protected File runTasksOn(
            @NonNull File project,
            @NonNull String gradleVersion,
            @NonNull String... tasks) {
        return runTasksOn(
                project,
                gradleVersion,
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                tasks);
    }

    protected File runTasksOn(
            @NonNull File project,
            @NonNull String gradleVersion,
            @NonNull List<String> arguments,
            @NonNull Map<String, String> jvmDefines,
            @NonNull String... tasks) {

        File buildGradle = new File(project, "build.gradle");
        assertTrue("Missing file: " + buildGradle, buildGradle.isFile());

        AndroidProjectConnector connector = new AndroidProjectConnector(sdkDir, ndkDir);
        connector.runGradleTasks(project, gradleVersion, arguments, jvmDefines, tasks)

        return project;
    }

    protected static void deleteFolder(File folder) {
        File[] files = folder.listFiles()
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file)
                } else {
                    file.delete()
                }
            }
        }

        folder.delete()
    }

    /**
     * Returns the name item from the collection of items. The items *must* have a "name" property.
     * @param items the item collection to search for a match
     * @param name the name of the item to return
     * @return the found item or null
     */
    protected static <T> T findNamedItemMaybe(@NonNull Collection<T> items,
                                              @NonNull String name) {
        for (T item : items) {
            if (name.equals(item.name)) {
                return item
            }
        }

        return null
    }

    /**
     * Returns the name item from the collection of items. The items *must* have a "name" property.
     * @param items the item collection to search for a match
     * @param name the name of the item to return
     * @return the found item or null
     */
    protected static <T> T findNamedItem(@NonNull Collection<T> items,
                                         @NonNull String name,
                                         @NonNull String typeName) {
        T foundItem = findNamedItemMaybe(items, name);
        assertNotNull("$name $typeName null-check", foundItem)
        return foundItem
    }
}
