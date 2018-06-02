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

package com.android.sdklib.repository.local;

import static org.junit.Assert.*;

import com.android.annotations.NonNull;
import com.android.sdklib.IAndroidTarget;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocalPlatformPkgInfoTest {

    @Test
    public void testOptionalLibWith1() throws Exception {
        String content =
                "[\n" +
                        "  {\n" +
                        "    \"name\": \"org.apache.http.legacy\",\n" +
                        "    \"jar\": \"org.apache.http.legacy.jar\",\n" +
                        "    \"manifest\": false\n" +
                        "  }\n" +
                        "]\n";

        File json = getJsonFile(content);

        List<IAndroidTarget.OptionalLibrary> libs = LocalPlatformPkgInfo.getLibsFromJson(json);

        assertEquals(1, libs.size());
        IAndroidTarget.OptionalLibrary lib = libs.get(0);
        assertEquals("org.apache.http.legacy", lib.getName());
        assertEquals(new File(json.getParentFile(), "org.apache.http.legacy.jar"), lib.getJar());
        assertEquals(false, lib.isManifestEntryRequired());
    }

    @NonNull
    private static File getJsonFile(String content) throws IOException {
        File json = File.createTempFile("testGetLibsFromJson", "");
        json.deleteOnExit();

        Files.write(content, json, Charsets.UTF_8);
        return json;
    }


}