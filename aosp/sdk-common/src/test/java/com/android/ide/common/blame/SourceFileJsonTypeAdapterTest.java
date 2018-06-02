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
public class SourceFileJsonTypeAdapterTest {

    private static GsonBuilder sGsonBuilder = new GsonBuilder()
            .registerTypeAdapter(SourceFile.class, new SourceFileJsonTypeAdapter());

    @RunWith(Parameterized.class)
    public static class RoundTripTest {

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new SourceFile(new File("/path/to/a/file.java"))},
                    {new SourceFile(new File("/path/to/a/file.java"), "Description")},
                    {new SourceFile("Description")},
                    {SourceFile.UNKNOWN},
            });
        }

        @Parameterized.Parameter
        public SourceFile mSourceFile;

        private static Gson sGson;

        @BeforeClass
        public static void initGson() {
            sGson = sGsonBuilder.create();
        }

        @Test
        public void test() {
            assertEquals(mSourceFile, sGson.fromJson(sGson.toJson(mSourceFile), SourceFile.class));
        }

        @AfterClass
        public static void removeGson() {
            sGson = null;
        }
    }

    @RunWith(Parameterized.class)
    public static class DeserializeTest {

        @Parameterized.Parameters(name = "fromJson(\"{1}\") should be {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            new SourceFile(new File("/path/file.java")),
                            "\"/path/file.java\""
                    },
                    {
                            new SourceFile(new File("/path/f.java")),
                            "{\"path\":\"/path/f.java\"}"
                    },
                    {
                            new SourceFile(new File("/path/file.java"), "Description"),
                            "{\"description\":\"Description\", \"path\":\"/path/file.java\"}"
                    },
                    {
                            new SourceFile("Description"),
                            "{\"description\":\"Description\"}"
                    },
                    {
                            SourceFile.UNKNOWN,
                            "{}"
                    },
                    {
                            SourceFile.UNKNOWN,
                            "\"\""
                    },
                    {
                            SourceFile.UNKNOWN,
                            "{\"foo\":\"\"}"
                    },
                    {
                            SourceFile.UNKNOWN,
                            "{\"foo\": {\"bar\" : \":)\"}}"
                    },
            });
        }

        @Parameterized.Parameter(value = 0)
        public SourceFile mSourceFile;

        @Parameterized.Parameter(value = 1)
        public String jsonString;


        private static Gson sGson;

        @BeforeClass
        public static void initGson() {
            sGson = sGsonBuilder.create();
        }

        @Test
        public void test() {
            assertEquals(mSourceFile, sGson.fromJson(jsonString, SourceFile.class));
        }

        @AfterClass
        public static void removeGson() {
            sGson = null;
        }
    }


}
