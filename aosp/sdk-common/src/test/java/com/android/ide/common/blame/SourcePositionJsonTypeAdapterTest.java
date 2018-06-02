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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Enclosed.class)
public class SourcePositionJsonTypeAdapterTest {

    /* package */ static SourcePosition[] mExamples = new SourcePosition[]{
            new SourcePosition(-1, -1, -1),
            new SourcePosition(11, 22, 34),
            new SourcePosition(11, 22, -1),
            new SourcePosition(11, -1, 34),
            new SourcePosition(-1, 22, 34),
            new SourcePosition(11, 22, 33, 66, 77, 88),
            new SourcePosition(11, 22, -1, 66, 77, -1),
            new SourcePosition(11, -1, -1, 66, -1, -1),
            new SourcePosition(11, -1, -1, 11, -1, -1),
            new SourcePosition(11, 22, 33, 66, 77, 88),
            new SourcePosition(-1, -1, 33, -1, -1, 88),
            new SourcePosition(-1, -1, 33, -1, -1, 33),
            new SourcePosition(11, 22, 33, 11, 22, 33)};

    @RunWith(Parameterized.class)
    public static class RoundTripTest {

        private static Gson sGson;

        @Parameterized.Parameter
        public SourcePosition mSourcePosition;

        @Parameterized.Parameter(value = 1)
        public String mIgnoredName;

        @Parameterized.Parameters(name = "{0}   new SourcePosition({1})")
        public static Iterable<Object[]> data() {
            return Iterables.transform(Arrays.asList(mExamples),
                    new Function<SourcePosition, Object[]>() {
                        @Override
                        public Object[] apply(SourcePosition input) {
                            return new Object[]{input, Joiner.on(", ").join(input.getStartLine(),
                                    input.getStartColumn(),
                                    input.getStartOffset(), input.getEndLine(),
                                    input.getEndColumn(), input.getEndOffset())};
                        }
                    });
        }

        @Test
        public void roundTrip() {
            String json = sGson.toJson(mSourcePosition);
            SourcePosition m2 = sGson.fromJson(json, SourcePosition.class);
            assertEquals(mSourcePosition, m2);
        }

        @BeforeClass
        public static void initGson() {
            sGson = newGson();
        }

        @AfterClass
        public static void removeGson() {
            sGson = null;
        }
    }

    public static class DeserializeTest {

        private Gson mGson = newGson();

        @Test
        public void testSimpleDeserialize() {
            String json2 = "{\"startLine\":245}";
            SourcePosition range2 =
                    mGson.fromJson(json2, SourcePosition.class);
            assertEquals(new SourcePosition(245, -1, -1), range2);
        }

        @Test
        public void testDeserialize() {
            String json
                    = "{\"startLine\":11,\"startColumn\":22,\"startOffset\":33,"
                    + "\"endLine\":66,\"endColumn\":77,\"endOffset\":88, "
                    + "\"invalid\":[\"ignored\"]}";
            SourcePosition range =
                    mGson.fromJson(json, SourcePosition.class);
            assertEquals(range, new SourcePosition(11, 22, 33, 66, 77, 88));
        }
    }

    private static Gson newGson() {
        return new GsonBuilder()
                .registerTypeAdapter(
                        SourcePosition.class,
                        new SourcePositionJsonTypeAdapter())
                .create();
    }
}
