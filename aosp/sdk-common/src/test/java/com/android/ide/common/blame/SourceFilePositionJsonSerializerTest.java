/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.ide.common.blame;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Enclosed.class)
public class SourceFilePositionJsonSerializerTest {

    private static GsonBuilder sGsonBuilder = new GsonBuilder();

    static {
        MessageJsonSerializer.registerTypeAdapters(sGsonBuilder);
    }

    private static Object[][] allPairings(Object[] x, Object[] y) {
        int totalSize = x.length * y.length;

        final Object[][] matrix = new Object[totalSize][];
        for (int i = 0; i < totalSize; i++) {
            matrix[i] = new Object[2];
            matrix[i][0] = x[i / y.length];
            matrix[i][1] = y[i % y.length];
        }

        return matrix;

    }

    @RunWith(Parameterized.class)
    public static class RoundTripTest {

        private static Gson sGson;

        @Parameterized.Parameter
        public SourceFile mSourceFile;

        @Parameterized.Parameter(value = 1)
        public SourcePosition mSourcePosition;

        @Parameterized.Parameters(name = "SourceFilePosition({0}, {1})")
        public static Collection<Object[]> data() {

            return Arrays.asList(allPairings(new SourceFile[]{
                            new SourceFile(new File("/path/to/a/file.java")),
                            new SourceFile(new File("/path/to/a/file.java"), "Description"),
                            new SourceFile("Description"),
                            SourceFile.UNKNOWN},
                    SourcePositionJsonTypeAdapterTest.mExamples));
        }

        @BeforeClass
        public static void initGson() {
            sGson = sGsonBuilder.create();
        }

        @AfterClass
        public static void removeGson() {
            sGson = null;
        }

        @Test
        public void test() {
            SourceFilePosition item = new SourceFilePosition(mSourceFile, mSourcePosition);
            assertEquals(item, sGson.fromJson(sGson.toJson(item), SourceFilePosition.class));
        }
    }

    @RunWith(Parameterized.class)
    public static class DeserializeTest {

        private static Gson sGson;

        @Parameterized.Parameter(value = 0)
        public SourceFilePosition mSourceFilePosition;

        @Parameterized.Parameter(value = 1)
        public String jsonString;

        @Parameterized.Parameters(name = "fromJson( {1} ) → {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new SourceFilePosition(
                            new SourceFile(new File("/path/file.java")),
                            SourcePosition.UNKNOWN),
                            "{\"file\":\"/path/file.java\"}"},
                    {new SourceFilePosition(
                            new SourceFile(new File("/path/file.java"), "Description"),
                            new SourcePosition(1, 3, 4)),
                            "{file:{\"description\":\"Description\",\"path\":\"/path/file.java\"},"
                                    + "position:{startLine:1, startColumn:3,startOffset:4}}"},
                    {new SourceFilePosition(
                            new SourceFile("Description"),
                            new SourcePosition(11, 22, 33, 66, 77, 88)),
                            "{\"file\":{\"description\":\"Description\"}, " + "position: {"
                                    + "\"startLine\":11,\"startColumn\":22,\"startOffset\":33,"
                                    + "\"endLine\":66,\"endColumn\":77,\"endOffset\":88" + "}}"},
                    {new SourceFilePosition(
                            SourceFile.UNKNOWN,
                            new SourcePosition(11, 22, 33, 66, 77, 88)),
                            "{ position: {\"startLine\":11,\"startColumn\":22,\"startOffset\":33,"
                                    + "\"endLine\":66,\"endColumn\":77,\"endOffset\":88" + "}, "
                                    + "\"invalid_something\":[\"ignored tree\"]}"},
                    {new SourceFilePosition(SourceFile.UNKNOWN, SourcePosition.UNKNOWN), "{}"},});
        }

        @BeforeClass
        public static void initGson() {
            sGson = sGsonBuilder.create();
        }

        @AfterClass
        public static void removeGson() {
            sGson = null;
        }

        @Test
        public void test() {
            assertEquals(mSourceFilePosition, sGson.fromJson(jsonString, SourceFilePosition.class));
        }
    }
}
