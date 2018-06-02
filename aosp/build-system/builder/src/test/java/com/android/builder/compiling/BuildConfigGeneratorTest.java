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

package com.android.builder.compiling;

import com.android.builder.core.AndroidBuilder;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BuildConfigGeneratorTest extends TestCase {
    public void testFalse() throws Exception {
        File tempDir = Files.createTempDir();
        BuildConfigGenerator generator = new BuildConfigGenerator(tempDir, "my.app.pkg");

        generator.addField("boolean", "DEBUG", "false").generate();

        File file = generator.getBuildConfigFile();
        assertTrue(file.exists());
        String actual = Files.toString(file, Charsets.UTF_8);
        assertEquals(
                "/**\n" +
                " * Automatically generated file. DO NOT MODIFY\n" +
                " */\n" +
                "package my.app.pkg;\n" +
                "\n" +
                "public final class BuildConfig {\n" +
                "  public static final boolean DEBUG = false;\n" +
                "}\n", actual);
        file.delete();
        tempDir.delete();
    }

    public void testTrue() throws Exception {
        File tempDir = Files.createTempDir();
        BuildConfigGenerator generator = new BuildConfigGenerator(tempDir, "my.app.pkg");
        generator.addField("boolean", "DEBUG", "Boolean.parseBoolean(\"true\")").generate();

        File file = generator.getBuildConfigFile();
        assertTrue(file.exists());
        String actual = Files.toString(file, Charsets.UTF_8);
        assertEquals(
                "/**\n" +
                " * Automatically generated file. DO NOT MODIFY\n" +
                " */\n" +
                "package my.app.pkg;\n" +
                "\n" +
                "public final class BuildConfig {\n" +
                "  public static final boolean DEBUG = Boolean.parseBoolean(\"true\");\n" +
                "}\n", actual);
        file.delete();
        tempDir.delete();
    }

    public void testExtra() throws Exception {
        File tempDir = Files.createTempDir();
        BuildConfigGenerator generator = new BuildConfigGenerator(tempDir, "my.app.pkg");

        List<Object> items = Lists.newArrayList();
        items.add("Extra line");
        items.add(AndroidBuilder.createClassField("int", "EXTRA", "42"));

        generator.addItems(items).generate();

        File file = generator.getBuildConfigFile();
        assertTrue(file.exists());
        String actual = Files.toString(file, Charsets.UTF_8);
        assertEquals(
                "/**\n" +
                " * Automatically generated file. DO NOT MODIFY\n" +
                " */\n" +
                "package my.app.pkg;\n" +
                "\n" +
                "public final class BuildConfig {\n" +
                "  // Extra line\n" +
                "  public static final int EXTRA = 42;\n" +
                "}\n", actual);
        file.delete();
        tempDir.delete();
    }
}
