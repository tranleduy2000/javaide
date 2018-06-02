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

package com.android.ide.common.res2;

import static java.io.File.separator;

import com.android.testutils.TestUtils;

import java.io.File;
import java.io.IOException;

public class ResourceSetTest extends BaseTestCase {

    public void testBaseResourceSetByCount() throws Exception {
        ResourceSet resourceSet = getBaseResourceSet(false /*normalize*/);
        assertEquals(29, resourceSet.size());
    }

    public void testBaseResourceSetByName() throws Exception {
        ResourceSet resourceSet = getBaseResourceSet(false /*normalize*/);

        verifyResourceExists(resourceSet,
                "drawable/icon",
                "drawable/patch",
                "raw/foo",
                "layout/main",
                "layout/layout_ref",
                "layout/alias_replaced_by_file",
                "layout/file_replaced_by_alias",
                "drawable/color_drawable",
                "drawable/drawable_ref",
                "color/color",
                "string/basic_string",
                "string/xliff_string",
                "string/styled_string",
                "style/style",
                "array/string_array",
                "attr/dimen_attr",
                "attr/string_attr",
                "attr/enum_attr",
                "attr/flag_attr",
                "attr/blah",
                "attr/blah2",
                "attr/flagAttr",
                "declare-styleable/declare_styleable",
                "dimen/dimen",
                "dimen-sw600dp/offset",
                "id/item_id",
                "integer/integer",
                "plurals/plurals"
        );
    }

    public void testBaseResourceSetWithNormalizationByName() throws Exception {
        ResourceSet resourceSet = getBaseResourceSet(true /*normalize*/);

        verifyResourceExists(resourceSet,
                "drawable/icon",
                "drawable/patch",
                "raw/foo",
                "layout/main",
                "layout/layout_ref",
                "layout/alias_replaced_by_file",
                "layout/file_replaced_by_alias",
                "drawable/color_drawable",
                "drawable/drawable_ref",
                "color/color",
                "string/basic_string",
                "string/xliff_string",
                "string/styled_string",
                "style/style",
                "array/string_array",
                "attr/dimen_attr",
                "attr/string_attr",
                "attr/enum_attr",
                "attr/flag_attr",
                "attr/blah",
                "attr/blah2",
                "attr/flagAttr",
                "declare-styleable/declare_styleable",
                "dimen/dimen",
                "dimen-sw600dp-v13/offset",
                "id/item_id",
                "integer/integer",
                "plurals/plurals"
        );
    }

    public void testDupResourceSet() throws Exception {
        File root = TestUtils.getRoot("resources", "dupSet");

        ResourceSet set = new ResourceSet("main");
        set.addSource(new File(root, "res1"));
        set.addSource(new File(root, "res2"));
        boolean gotException = false;
        RecordingLogger logger =  new RecordingLogger();
        try {
            set.loadFromFiles(logger);
        } catch (DuplicateDataException e) {
            gotException = true;




            String message = e.getMessage();
            // Clean up paths etc for unit test
            int index = message.indexOf("dupSet");
            assertTrue(index != -1);
            String prefix = message.substring(0, index);
            message = message.replace(prefix, "<PREFIX>").replace('\\','/');
            assertEquals("<PREFIX>dupSet/res1/drawable/icon.png\t<PREFIX>dupSet/res2/drawable/icon.png: "
                    + "Error: Duplicate resources", message);
        }

        checkLogger(logger);
        assertTrue(gotException);
    }

    public void testBrokenSet() throws Exception {
        File root = TestUtils.getRoot("resources", "brokenSet");

        ResourceSet set = new ResourceSet("main");
        set.addSource(root);

        boolean gotException = false;
        RecordingLogger logger =  new RecordingLogger();
        try {
            set.loadFromFiles(logger);
        } catch (MergingException e) {
            gotException = true;
            assertEquals(new File(root, "values" + separator + "dimens.xml").getAbsolutePath() +
                    ":1:1: Error: Content is not allowed in prolog.",
                    e.getMessage());
        }

        assertTrue("ResourceSet processing should have failed, but didn't", gotException);
        assertFalse(logger.getErrorMsgs().isEmpty());
    }

    public void testBrokenSet2() throws Exception {
        File root = TestUtils.getRoot("resources", "brokenSet2");

        ResourceSet set = new ResourceSet("main");
        set.addSource(root);

        boolean gotException = false;
        RecordingLogger logger =  new RecordingLogger();
        try {
            set.loadFromFiles(logger);
        } catch (MergingException e) {
            gotException = true;
            assertEquals(new File(root, "values" + separator + "values.xml").getAbsolutePath() +
                    ": Error: Found item String/app_name more than one time",
                    e.getMessage());
        }

        assertTrue("ResourceSet processing should have failed, but didn't", gotException);
        assertFalse(logger.getErrorMsgs().isEmpty());
    }

    public void testBrokenSet3() throws Exception {
        File root = TestUtils.getRoot("resources", "brokenSet3");

        ResourceSet set = new ResourceSet("main");
        set.addSource(root);

        boolean gotException = false;
        RecordingLogger logger =  new RecordingLogger();
        try {
            set.loadFromFiles(logger);
        } catch (MergingException e) {
            gotException = true;
            assertEquals(new File(root, "values" + separator + "values.xml").getAbsolutePath() +
                    ": Error: Found item Attr/d_common_attr more than one time",
                    e.getMessage());
        }

        assertTrue("ResourceSet processing should have failed, but didn't", gotException);
        assertFalse(logger.getErrorMsgs().isEmpty());
    }

    public void testBrokenSet4() throws Exception {
        File root = TestUtils.getRoot("resources", "brokenSet4");

        ResourceSet set = new ResourceSet("main");
        set.addSource(root);

        boolean gotException = false;
        RecordingLogger logger =  new RecordingLogger();
        try {
            set.loadFromFiles(logger);
        } catch (MergingException e) {
            gotException = true;
            assertEquals(new File(root, "values" + separator + "values.xml").getAbsolutePath() +
                    ":7:6: Error: The element type \"declare-styleable\" "
                    + "must be terminated by the matching end-tag \"</declare-styleable>\".",
                    e.getMessage());
        }

        assertTrue("ResourceSet processing should have failed, but didn't", gotException);
        assertFalse(logger.getErrorMsgs().isEmpty());
    }

    static ResourceSet getBaseResourceSet(boolean normalize) throws MergingException, IOException {
        File root = TestUtils.getRoot("resources", "baseSet");

        ResourceSet resourceSet = new ResourceSet("main");
        resourceSet.setNormalizeResources(normalize);
        resourceSet.addSource(root);
        RecordingLogger logger =  new RecordingLogger();
        resourceSet.loadFromFiles(logger);

        checkLogger(logger);

        return resourceSet;
    }
}
