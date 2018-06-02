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

package com.android.ide.common.res2;

import com.android.ide.common.rendering.api.AttrResourceValue;
import com.android.ide.common.rendering.api.ItemResourceValue;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.testutils.TestUtils;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResourceRepositoryTest extends BaseTestCase {

    public void testMergeByCount() throws Exception {
        ResourceRepository repo = getResourceRepository();

        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repo.getItems();

        assertEquals(6, items.get(ResourceType.DRAWABLE).size());
        assertEquals(1, items.get(ResourceType.RAW).size());
        assertEquals(4, items.get(ResourceType.LAYOUT).size());
        assertEquals(1, items.get(ResourceType.COLOR).size());
        assertEquals(4, items.get(ResourceType.STRING).size());
        assertEquals(1, items.get(ResourceType.STYLE).size());
        assertEquals(1, items.get(ResourceType.ARRAY).size());
        assertEquals(7, items.get(ResourceType.ATTR).size());
        assertEquals(1, items.get(ResourceType.DECLARE_STYLEABLE).size());
        assertEquals(3, items.get(ResourceType.DIMEN).size());
        assertEquals(1, items.get(ResourceType.ID).size());
        assertEquals(1, items.get(ResourceType.INTEGER).size());
    }

    public void testMergedResourcesByName() throws Exception {
        ResourceRepository repo = getResourceRepository();

        // use ? between type and qualifier because of declare-styleable
        verifyResourceExists(repo,
                "drawable/icon",
                "drawable?ldpi/icon",
                "drawable/icon2",
                "drawable/patch",
                "drawable/color_drawable",
                "drawable/drawable_ref",
                "raw/foo",
                "layout/main",
                "layout/layout_ref",
                "layout/alias_replaced_by_file",
                "layout/file_replaced_by_alias",
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
                "dimen?sw600dp/offset",
                "dimen?sw600dp-v13/offset",
                "id/item_id",
                "integer/integer"
        );
    }

    public void testBaseStringValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.STRING, "basic_string");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);

        assertEquals("overlay_string", value.getValue());
    }

    public void testBaseStyledStringValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.STRING, "styled_string");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);

        assertEquals("Forgot your username or password?\nVisit google.com/accounts/recovery.",
                value.getValue());
    }

    public void testBaseColorValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.COLOR, "color");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);

        assertEquals("#FFFFFFFF", value.getValue());
    }

    public void testBaseLayoutAliasValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.LAYOUT, "layout_ref");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);

        assertEquals("@layout/ref", value.getValue());
    }

    public void testBaseAttrValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.ATTR, "flag_attr");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);
        assertTrue(value instanceof AttrResourceValue);
        AttrResourceValue attrValue = (AttrResourceValue) value;

        Map<String, Integer> attrValues = attrValue.getAttributeValues();
        assertNotNull(attrValues);
        assertEquals(3, attrValues.size());

        Integer i = attrValues.get("normal");
        assertNotNull(i);
        assertEquals(Integer.valueOf(0), i);

        i = attrValues.get("bold");
        assertNotNull(i);
        assertEquals(Integer.valueOf(1), i);

        i = attrValues.get("italic");
        assertNotNull(i);
        assertEquals(Integer.valueOf(2), i);
    }

    public void testBaseStyleValue() throws Exception {
        ResourceRepository repo = getResourceRepository();

        List<ResourceItem> itemList = repo.getResourceItem(ResourceType.STYLE, "style");
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        ResourceValue value = itemList.get(0).getResourceValue(false);
        assertNotNull(value);
        assertTrue(value instanceof StyleResourceValue);
        StyleResourceValue styleResourceValue = (StyleResourceValue) value;

        assertEquals("@android:style/Holo.Light", styleResourceValue.getParentStyle());

        ItemResourceValue styleValue = styleResourceValue.getItem("singleLine", true /*framework*/);
        assertNotNull(styleValue);
        assertEquals("true", styleValue.getValue());

        styleValue = styleResourceValue.getItem("textAppearance", true /*framework*/);
        assertNotNull(styleValue);
        assertEquals("@style/TextAppearance.WindowTitle", styleValue.getValue());

        styleValue = styleResourceValue.getItem("shadowColor", true /*framework*/);
        assertNotNull(styleValue);
        assertEquals("#BB000000", styleValue.getValue());

        styleValue = styleResourceValue.getItem("shadowRadius", true /*framework*/);
        assertNotNull(styleValue);
        assertEquals("2.75", styleValue.getValue());

        styleValue = styleResourceValue.getItem("foo", false /*framework*/);
        assertNotNull(styleValue);
        assertEquals("foo", styleValue.getValue());
    }

    public void testUpdateWithBasicFiles() throws Exception {
        File root = getIncMergeRoot("basicFiles");
        File fakeRoot = getMergedBlobFolder(root);
        ResourceMerger resourceMerger = new ResourceMerger();
        resourceMerger.loadFromBlob(fakeRoot, false /*incrementalState*/);
        checkSourceFolders(resourceMerger);

        List<ResourceSet> sets = resourceMerger.getDataSets();
        assertEquals(2, sets.size());

        // write the content in a repo.
        ResourceRepository repo = new ResourceRepository(false);
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        // checks the initial state of the repo
        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repo.getItems();
        ListMultimap<String, ResourceItem> drawables = items.get(ResourceType.DRAWABLE);
        assertNotNull("Drawable null check", drawables);
        assertEquals("Drawable size check", 6, drawables.size());
        verifyResourceExists(repo,
                "drawable/new_overlay",
                "drawable/removed",
                "drawable?ldpi/removed",
                "drawable/touched",
                "drawable/removed_overlay",
                "drawable/untouched");

        // Apply updates
        RecordingLogger logger =  new RecordingLogger();

        // ----------------
        // first set is the main one, no change here
        ResourceSet mainSet = sets.get(0);
        File mainBase = new File(root, "main");
        File mainDrawable = new File(mainBase, "drawable");
        File mainDrawableLdpi = new File(mainBase, "drawable-ldpi");

        // touched/removed files:
        File mainDrawableTouched = new File(mainDrawable, "touched.png");
        mainSet.updateWith(mainBase, mainDrawableTouched, FileStatus.CHANGED, logger);
        checkLogger(logger);

        File mainDrawableRemoved = new File(mainDrawable, "removed.png");
        mainSet.updateWith(mainBase, mainDrawableRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        File mainDrawableLdpiRemoved = new File(mainDrawableLdpi, "removed.png");
        mainSet.updateWith(mainBase, mainDrawableLdpiRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        // ----------------
        // second set is the overlay one
        ResourceSet overlaySet = sets.get(1);
        File overlayBase = new File(root, "overlay");
        File overlayDrawable = new File(overlayBase, "drawable");
        File overlayDrawableHdpi = new File(overlayBase, "drawable-hdpi");

        // new/removed files:
        File overlayDrawableNewOverlay = new File(overlayDrawable, "new_overlay.png");
        overlaySet.updateWith(overlayBase, overlayDrawableNewOverlay, FileStatus.NEW, logger);
        checkLogger(logger);

        File overlayDrawableRemovedOverlay = new File(overlayDrawable, "removed_overlay.png");
        overlaySet.updateWith(overlayBase, overlayDrawableRemovedOverlay, FileStatus.REMOVED,
                logger);
        checkLogger(logger);

        File overlayDrawableHdpiNewAlternate = new File(overlayDrawableHdpi, "new_alternate.png");
        overlaySet.updateWith(overlayBase, overlayDrawableHdpiNewAlternate, FileStatus.NEW, logger);
        checkLogger(logger);

        // validate for duplicates
        resourceMerger.validateDataSets();

        // check the new content.
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        drawables = items.get(ResourceType.DRAWABLE);
        assertNotNull("Drawable null check", drawables);
        assertEquals("Drawable size check", 5, drawables.size());
        verifyResourceExists(repo,
                "drawable/new_overlay",
                "drawable/touched",
                "drawable/removed_overlay",
                "drawable/untouched",
                "drawable?hdpi/new_alternate");
        checkRemovedItems(resourceMerger);
    }

    public void testUpdateWithBasicValues() throws Exception {
        File root = getIncMergeRoot("basicValues");
        File fakeRoot = getMergedBlobFolder(root);
        ResourceMerger resourceMerger = new ResourceMerger();
        resourceMerger.loadFromBlob(fakeRoot, false /*incrementalState*/);
        checkSourceFolders(resourceMerger);

        List<ResourceSet> sets = resourceMerger.getDataSets();
        assertEquals(2, sets.size());

        // write the content in a repo.
        ResourceRepository repo = new ResourceRepository(false);
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        // checks the initial state of the repo
        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repo.getItems();
        ListMultimap<String, ResourceItem> strings = items.get(ResourceType.STRING);
        assertNotNull("String null check", strings);
        assertEquals("String size check", 5, strings.size());
        verifyResourceExists(repo,
                "string/untouched",
                "string/touched",
                "string/removed",
                "string?en/removed",
                "string/new_overlay");

        // apply updates
        RecordingLogger logger = new RecordingLogger();

        // ----------------
        // first set is the main one, no change here
        ResourceSet mainSet = sets.get(0);
        File mainBase = new File(root, "main");
        File mainValues = new File(mainBase, "values");
        File mainValuesEn = new File(mainBase, "values-en");

        // touched file:
        File mainValuesTouched = new File(mainValues, "values.xml");
        mainSet.updateWith(mainBase, mainValuesTouched, FileStatus.CHANGED, logger);
        checkLogger(logger);

        // removed files
        File mainValuesEnRemoved = new File(mainValuesEn, "values.xml");
        mainSet.updateWith(mainBase, mainValuesEnRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        // ----------------
        // second set is the overlay one
        ResourceSet overlaySet = sets.get(1);
        File overlayBase = new File(root, "overlay");
        File overlayValues = new File(overlayBase, "values");
        File overlayValuesFr = new File(overlayBase, "values-fr");

        // new files:
        File overlayValuesNew = new File(overlayValues, "values.xml");
        overlaySet.updateWith(overlayBase, overlayValuesNew, FileStatus.NEW, logger);
        checkLogger(logger);

        File overlayValuesFrNew = new File(overlayValuesFr, "values.xml");
        overlaySet.updateWith(overlayBase, overlayValuesFrNew, FileStatus.NEW, logger);
        checkLogger(logger);

        // validate for duplicates
        resourceMerger.validateDataSets();

        // check the new content.
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        strings = items.get(ResourceType.STRING);
        assertNotNull("String null check", strings);
        assertEquals("String size check", 4, strings.size());
        verifyResourceExists(repo,
                "string/untouched",
                "string/touched",
                "string/new_overlay",
                "string?fr/new_alternate");
        checkRemovedItems(resourceMerger);
    }

    public void testUpdateWithBasicValues2() throws Exception {
        File root = getIncMergeRoot("basicValues2");
        File fakeRoot = getMergedBlobFolder(root);
        ResourceMerger resourceMerger = new ResourceMerger();
        resourceMerger.loadFromBlob(fakeRoot, false /*incrementalState*/);
        checkSourceFolders(resourceMerger);

        List<ResourceSet> sets = resourceMerger.getDataSets();
        assertEquals(2, sets.size());

        // write the content in a repo.
        ResourceRepository repo = new ResourceRepository(false);
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        // checks the initial state of the repo
        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repo.getItems();
        ListMultimap<String, ResourceItem> strings = items.get(ResourceType.STRING);
        assertNotNull("String null check", strings);
        assertEquals("String size check", 2, strings.size());
        verifyResourceExists(repo,
                "string/untouched",
                "string/removed_overlay");

        // apply updates
        RecordingLogger logger = new RecordingLogger();

        // ----------------
        // first set is the main one, no change here

        // ----------------
        // second set is the overlay one
        ResourceSet overlaySet = sets.get(1);
        File overlayBase = new File(root, "overlay");
        File overlayValues = new File(overlayBase, "values");

        // new files:
        File overlayValuesNew = new File(overlayValues, "values.xml");
        overlaySet.updateWith(overlayBase, overlayValuesNew, FileStatus.REMOVED, logger);
        checkLogger(logger);

        // validate for duplicates
        resourceMerger.validateDataSets();

        // check the new content.
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        strings = items.get(ResourceType.STRING);
        assertNotNull("String null check", strings);
        assertEquals("String size check", 2, strings.size());
        verifyResourceExists(repo,
                "string/untouched",
                "string/removed_overlay");
        checkRemovedItems(resourceMerger);
    }

    public void testUpdateWithFilesVsValues() throws Exception {
        File root = getIncMergeRoot("filesVsValues");
        File fakeRoot = getMergedBlobFolder(root);
        ResourceMerger resourceMerger = new ResourceMerger();
        resourceMerger.loadFromBlob(fakeRoot, false /*incrementalState*/);
        checkSourceFolders(resourceMerger);

        List<ResourceSet> sets = resourceMerger.getDataSets();
        assertEquals(1, sets.size());

        // write the content in a repo.
        ResourceRepository repo = new ResourceRepository(false);
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        // checks the initial state of the repo
        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repo.getItems();
        ListMultimap<String, ResourceItem> layouts = items.get(ResourceType.LAYOUT);
        assertNotNull("String null check", layouts);
        assertEquals("String size check", 3, layouts.size());
        verifyResourceExists(repo,
                "layout/main",
                "layout/file_replaced_by_alias",
                "layout/alias_replaced_by_file");

        // apply updates
        RecordingLogger logger = new RecordingLogger();

        // ----------------
        // Load the main set
        ResourceSet mainSet = sets.get(0);
        File mainBase = new File(root, "main");
        File mainValues = new File(mainBase, ResourceFolderType.VALUES.getName());
        File mainLayout = new File(mainBase, ResourceFolderType.LAYOUT.getName());

        // touched file:
        File mainValuesTouched = new File(mainValues, "values.xml");
        mainSet.updateWith(mainBase, mainValuesTouched, FileStatus.CHANGED, logger);
        checkLogger(logger);

        // new file:
        File mainLayoutNew = new File(mainLayout, "alias_replaced_by_file.xml");
        mainSet.updateWith(mainBase, mainLayoutNew, FileStatus.NEW, logger);
        checkLogger(logger);

        // removed file
        File mainLayoutRemoved = new File(mainLayout, "file_replaced_by_alias.xml");
        mainSet.updateWith(mainBase, mainLayoutRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        // validate for duplicates
        resourceMerger.validateDataSets();

        // check the new content.
        resourceMerger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);

        layouts = items.get(ResourceType.LAYOUT);
        assertNotNull("String null check", layouts);
        assertEquals("String size check", 3, layouts.size());
        verifyResourceExists(repo,
                "layout/main",
                "layout/file_replaced_by_alias",
                "layout/alias_replaced_by_file");
        checkRemovedItems(resourceMerger);
    }

    public void testUpdateFromOldFile() throws Exception {
        File root = getIncMergeRoot("oldMerge");
        File fakeRoot = getMergedBlobFolder(root);
        ResourceMerger resourceMerger = new ResourceMerger();
        assertFalse(resourceMerger.loadFromBlob(fakeRoot, false /*incrementalState*/));
    }

    private static void checkRemovedItems(DataMap<? extends DataItem> dataMap) {
        for (DataItem item : dataMap.getDataMap().values()) {
            if (item.isRemoved()) {
                fail("Removed item found: " + item);
            }
        }
    }

    /**
     * Creates a fake merge with given sets.
     *
     * the data is an array of sets.
     *
     * Each set is [ setName, folder1, folder2, ...]
     *
     */
    private static ResourceMerger createMerger(String[][] data) {
        ResourceMerger merger = new ResourceMerger();
        for (String[] setData : data) {
            ResourceSet set = new ResourceSet(setData[0]);
            merger.addDataSet(set);
            for (int i = 1, n = setData.length; i < n; i++) {
                set.addSource(new File(setData[i]));
            }
        }

        return merger;
    }

    /**
     * Returns a merger with the baseSet and baseMerge content.
     */
    private static ResourceMerger getBaseResourceMerger()
            throws MergingException, IOException {
        File root = TestUtils.getRoot("resources", "baseMerge");

        ResourceSet res = ResourceSetTest.getBaseResourceSet(false /*normalize*/);

        RecordingLogger logger = new RecordingLogger();

        ResourceSet overlay = new ResourceSet("overlay");
        overlay.addSource(new File(root, "overlay"));
        overlay.loadFromFiles(logger);

        checkLogger(logger);

        ResourceMerger resourceMerger = new ResourceMerger();
        resourceMerger.addDataSet(res);
        resourceMerger.addDataSet(overlay);

        return resourceMerger;
    }

    /**
     * Returns a merger from incMergeData initialized from the files, not from the merger
     * state blog.
     */
    private static ResourceMerger getIncResourceMerger(String rootName, String... sets)
            throws MergingException, IOException {

        File root = getIncMergeRoot(rootName);
        RecordingLogger logger = new RecordingLogger();

        ResourceMerger resourceMerger = new ResourceMerger();

        for (String setName : sets) {
            ResourceSet resourceSet = new ResourceSet(setName);
            resourceSet.addSource(new File(root, setName));
            resourceSet.loadFromFiles(logger);
            checkLogger(logger);

            resourceMerger.addDataSet(resourceSet);
        }

        return resourceMerger;
    }

    private static ResourceRepository getResourceRepository()
            throws MergingException, IOException {
        ResourceMerger merger = getBaseResourceMerger();

        ResourceRepository repo = new ResourceRepository(false);

        merger.mergeData(repo.createMergeConsumer(), true /*doCleanUp*/);
        return repo;
    }

    private static File getIncMergeRoot(String name) throws IOException {
        File root = TestUtils.getCanonicalRoot("resources", "incMergeData");
        return new File(root, name);
    }

    private static void verifyResourceExists(ResourceRepository repository,
            String... dataItemKeys) {
        Map<ResourceType, ListMultimap<String, ResourceItem>> items = repository.getItems();

        for (String resKey : dataItemKeys) {
            String type, name, qualifier = "";

            int pos = resKey.indexOf('/');
            if (pos != -1) {
                name = resKey.substring(pos + 1);
                type = resKey.substring(0, pos);
            } else {
                throw new IllegalArgumentException("Invalid key " + resKey);
            }

            // use ? as a qualifier delimiter because of
            // declare-styleable
            pos = type.indexOf('?');
            if (pos != -1) {
                qualifier = type.substring(pos + 1);
                type = type.substring(0, pos);
            }

            ResourceType resourceType = ResourceType.getEnum(type);
            assertNotNull("Type check for " + resKey, resourceType);

            Multimap<String, ResourceItem> map = items.get(resourceType);
            assertNotNull("Map check for " + resKey, map);

            Collection<ResourceItem> list = map.get(name);
            int found = 0;
            for (ResourceItem resourceItem : list) {
                if (resourceItem.getName().equals(name)) {
                    String fileQualifier = resourceItem.getSource() != null ?
                            resourceItem.getSource().getQualifiers() : "";

                    if (qualifier.equals(fileQualifier)) {
                        found++;
                    }
                }
            }

            assertEquals("Match for " + resKey, 1, found);
        }
    }

    // This utility method has been pretty handy in tracking down resource repository bugs
    // so keeping it for future potential use, but in unit test code so no runtime overhead
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public static String dumpRepository(ResourceRepository repository) {
        Map<ResourceType, ListMultimap<String, ResourceItem>> mItems = repository.getMap();
        Comparator<ResourceItem> comparator = new Comparator<ResourceItem>() {
            @Override
            public int compare(ResourceItem item1, ResourceItem item2) {
                assert item1.getType() == item2.getType();
                String qualifiers = item2.getSource().getQualifiers();
                return item1.getSource().getQualifiers().compareTo(qualifiers);
            }
        };

        StringBuilder sb = new StringBuilder(5000);
        sb.append("Resource Map Dump For Repository ").append(repository)
                .append("\n------------------------------------------------\n");
        for (ResourceType type : ResourceType.values()) {
            ListMultimap<String, ResourceItem> map = mItems.get(type);
            if (map == null) {
                continue;
            }
            sb.append(type.getName()).append(':').append('\n');

            List<String> keys = new ArrayList<String>(map.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                List<ResourceItem> items = map.get(key);
                List<ResourceItem> sorted = new ArrayList<ResourceItem>(items);
                Collections.sort(sorted, comparator);
                sb.append("  ").append(type.getName()).append(" ").append(key).append(": ");
                boolean first = true;
                for (ResourceItem item : sorted) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    String qualifiers = item.getSource().getQualifiers();
                    if (qualifiers.isEmpty()) {
                        qualifiers = "default";
                    }
                    sb.append(qualifiers);
                }

                if (!sorted.isEmpty()) {
                    ResourceItem item = sorted.get(0);
                    ResourceValue resourceValue = item.getResourceValue(repository.isFramework());
                    if (resourceValue == null) {
                        sb.append(" <no value found>");
                    } else {
                        String value = resourceValue.getValue();
                        if (value == null || value.isEmpty()) {
                            if (resourceValue instanceof StyleResourceValue) {
                                StyleResourceValue srv = (StyleResourceValue) resourceValue;
                                sb.append("  parentStyle=").append(srv.getParentStyle())
                                        .append("\n");
                                for (String name : srv.getNames()) {
                                    ItemResourceValue value1 = srv.getItem(name, false);
                                    ItemResourceValue value2 = srv.getItem(name, true);
                                    if (value1 != null) {
                                        Boolean framework = false;
                                        sb.append("    ");
                                        sb.append(name).append(" ").append(framework).append(" ");
                                        sb.append(" = ");
                                        sb.append('"');
                                        String strValue = value1.getValue();
                                        if (strValue != null) {
                                            sb.append(strValue.replace("\n", "\\n"));
                                        } else {
                                            sb.append("???");
                                        }
                                        sb.append('"');
                                    }
                                    if (value2 != null) {
                                        Boolean framework = true;
                                        sb.append("    ");
                                        sb.append(name).append(" ").append(framework).append(" ");
                                        sb.append(" = ");
                                        sb.append('"');
                                        String strValue = value2.getValue();
                                        if (strValue != null) {
                                            sb.append(strValue.replace("\n", "\\n"));
                                        } else {
                                            sb.append("???");
                                        }
                                        sb.append('"');
                                    }
                                }
                            } else {
                                sb.append(" = \"\"");
                            }
                        } else {
                            sb.append(" = ");
                            sb.append('"');
                            sb.append(value.replace("\n", "\\n"));
                            sb.append('"');
                        }
                    }
                }

                sb.append("\n");
            }

            sb.append("\n\n");
        }

        return sb.toString();
    }
}
