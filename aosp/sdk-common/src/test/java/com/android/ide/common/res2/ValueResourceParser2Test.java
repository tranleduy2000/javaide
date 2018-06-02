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

import com.android.SdkConstants;
import com.android.testutils.TestUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import org.w3c.dom.Document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 */
public class ValueResourceParser2Test extends BaseTestCase {

    private static List<ResourceItem> sResources = null;

    public void testParsedResourcesByCount() throws Exception {
        List<ResourceItem> resources = getParsedResources();

        assertEquals(23, resources.size());
    }

    public void testParsedResourcesByName() throws Exception {
        List<ResourceItem> resources = getParsedResources();
        Map<String, ResourceItem> resourceMap = Maps.newHashMapWithExpectedSize(resources.size());
        for (ResourceItem item : resources) {
            resourceMap.put(item.getKey(), item);
        }

        String[] resourceNames = new String[] {
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
                "id/item_id",
                "integer/integer",
                "layout/layout_ref",
                "plurals/plurals"
        };

        for (String name : resourceNames) {
            assertNotNull(name, resourceMap.get(name));
        }
    }

    private static List<ResourceItem> getParsedResources() throws MergingException {
        if (sResources == null) {
            File root = TestUtils.getRoot("resources", "baseSet");
            File values = new File(root, "values");
            File valuesXml = new File(values, "values.xml");

            ValueResourceParser2 parser = new ValueResourceParser2(valuesXml);
            sResources = parser.parseFile();

            // create a fake resource file to allow calling ResourceItem.getKey()
            //noinspection ResultOfObjectAllocationIgnored
            new ResourceFile(valuesXml, sResources, "");
        }

        return sResources;
    }

    public void testUtfBom() throws IOException, MergingException {
        File file = File.createTempFile(getName(), SdkConstants.DOT_XML);
        String xml = "" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"wrap_content\"\n" +
                "    android:orientation=\"vertical\" >\n" +
                "\n" +
                "    <Button\n" +
                "        android:id=\"@+id/button1\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Button\" />\n" +
                "          some text\n" +
                "\n" +
                "</LinearLayout>\n";

        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter writer = new OutputStreamWriter(stream, Charsets.UTF_8);
        stream.write(0xef);
        stream.write(0xbb);
        stream.write(0xbf);
        writer.write(xml);
        writer.close();

        Document document = ValueResourceParser2.parseDocument(file);
        assertNotNull(document);
        assertNotNull(document.getDocumentElement());
        assertEquals("LinearLayout", document.getDocumentElement().getTagName());

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
