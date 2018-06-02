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

package com.android.ide.common.res2;

import com.android.resources.ResourceType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ValueResourceNameValidatorTest {

    public static File DUMMY_FILE = new File("DUMMY_FILE");

    @Parameterized.Parameters(name="name=\"{0}\", resourceType={1}, file={2} gives  error {3}")
    public static Collection<Object[]> expected() {
        return Arrays.asList(new Object[][] {
                //{ resourceName, resourceType, sourceFile, expectedException }
                { "foo.png", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo.xml", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo.9.xml", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo.png", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo.txt", ResourceType.RAW, DUMMY_FILE, null},
                { "foo.txt", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "foo.other.png", ResourceType.DRAWABLE, DUMMY_FILE, null},
                { "android:q", ResourceType.STRING, DUMMY_FILE, "':' is not a valid resource name character"},
                { "android:q", ResourceType.STRING, null, "':' is not a valid resource name character"},
                { "android:q", ResourceType.ATTR, DUMMY_FILE, null},
                { "foo.s_3", ResourceType.STRING, null, null},
                { "FOO$", ResourceType.STRING, null, null},
                { "1st", ResourceType.STRING, null, "The resource name must start with a letter"},
                { "Foo#", ResourceType.STRING, null, "'#' is not a valid resource name character"},
                { "void", ResourceType.STRING, null, "void is not a valid resource name (reserved Java keyword)"},
        });
    }

    @Parameterized.Parameter
    public String mResourceName;

    @Parameterized.Parameter(value=1)
    public ResourceType mResourceType;

    @Parameterized.Parameter(value=2)
    public File mSourceFile;

    @Parameterized.Parameter(value=3)
    public String mExpectedErrorMessage;


    @Test
    public void validate() {
        String errorMessage = null;
        try {
            ValueResourceNameValidator.validate(mResourceName, mResourceType, mSourceFile);
        } catch (MergingException e) {
            errorMessage = e.getMessage();
        }
        FileResourceNameValidatorTest.assertErrorMessageCorrect(
                mExpectedErrorMessage, errorMessage, mSourceFile);
    }

}
