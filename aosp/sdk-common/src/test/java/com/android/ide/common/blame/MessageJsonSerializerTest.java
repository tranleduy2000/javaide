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
public class MessageJsonSerializerTest {

    private static GsonBuilder sGsonBuilder = new GsonBuilder();

    static {
        MessageJsonSerializer.registerTypeAdapters(sGsonBuilder);
    }

    @RunWith(Parameterized.class)
    public static class DeserializeTest {

        private static Gson sGson;

        @Parameterized.Parameter(value = 0)
        public Message message;

        @Parameterized.Parameter(value = 1)
        public String serializedMessage;

        @Parameterized.Parameters(name = "fromJson(\"{1}\") should give {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new Message(
                                    Message.Kind.ERROR,
                                    "some error text",
                                    new SourceFilePosition(
                                            new SourceFile(new File("/path/file.java")),
                                            new SourcePosition(1, 3, 5))),
                            "{"
                                    + "\"kind\":\"Error\", "
                                    + "\"text\":\"some error text\","
                                    + "\"sources\": {"
                                    + "\"file\":\"/path/file.java\","
                                    + "\"position\":{"
                                    + "\"startLine\":1,"
                                    + "\"startColumn\":3,"
                                    + "\"startOffset\":5"
                                    + "}"
                                    + "}}"
                    }, {
                            new Message(
                                    Message.Kind.ERROR,
                                    "errorText",
                                    new SourceFilePosition(
                                            new File("error/source"),
                                            new SourcePosition(1,2,3,4,5,6))
                            ),
                            "{\"kind\":\"ERROR\",\"text\":\"errorText\",\"sourcePath\":\"error/source\","
                            + "\"position\":{\"startLine\":1,\"startColumn\":2,\"startOffset\":3,"
                            + "\"endLine\":4,\"endColumn\":5,\"endOffset\":6},\"original\":\"\"}\n"
            }, {new Message(Message.Kind.SIMPLE, "something else", new SourceFilePosition(SourceFile.UNKNOWN, SourcePosition.UNKNOWN)),
                    "{\"kind\":\"SIMPLE\","
                            + "\"text\":\"something else\",\"position\":{},\"original\":\"something else\"}"
            }, {
                    new Message(
                            Message.Kind.SIMPLE,
                            "Warning: AndroidManifest.xml already defines debuggable (in http://"
                                    + "schemas.android.com/apk/res/android); using existing value "
                                    + "in manifest.",
                            SourceFilePosition.UNKNOWN),
                    "{\"kind\":\"simple\",\"text\":\"Warning: AndroidManifest.xml already defines "
                            + "debuggable (in http://schemas.android.com/apk/res/android); using "
                            + "existing value in manifest.\",\"sources\":\"\"}"
            }, {
                    new Message(
                            Message.Kind.UNKNOWN,
                            "Text.",
                            SourceFilePosition.UNKNOWN),
                    "{\"text\":\"Text.\"}",

            }});
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
        public void check() {
            assertEquals(message, sGson.fromJson(serializedMessage, Message.class));
        }


    }

    @RunWith(Parameterized.class)
    public static class RoundTripTest {

        private static Gson sGson;

        @Parameterized.Parameter
        public Message message;


        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{{
                    new Message(
                            Message.Kind.ERROR,
                            "some error text",
                            "original error text",
                            new SourceFilePosition(
                                    new SourceFile(new File("/path/file.java")),
                                    new SourcePosition(1, 3, 5)))
                    }, {
                    new Message(
                            Message.Kind.SIMPLE,
                            "something else",
                            new SourceFilePosition(SourceFile.UNKNOWN, SourcePosition.UNKNOWN))
                    }, {
                    new Message(
                            Message.Kind.SIMPLE,
                            "Warning: AndroidManifest.xml already defines debuggable (in http://"
                                    + "schemas.android.com/apk/res/android); using existing value "
                                    + "in manifest.",
                            SourceFilePosition.UNKNOWN)
            }});
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
        public void check() {
            assertEquals(message, sGson.fromJson(sGson.toJson(message), Message.class));
        }


    }
}
