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

import com.android.SdkConstants;
import com.android.testutils.TestUtils;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class AssetMergerTest extends BaseTestCase {

    private static AssetMerger sAssetMerger = null;

    public void testMergeByCount() throws Exception {
        AssetMerger merger = getAssetMerger();

        assertEquals(5, merger.size());
    }

    public void testMergedAssetsByName() throws Exception {
        AssetMerger merger = getAssetMerger();

        verifyResourceExists(merger,
                "foo/icon.png",
                "icon2.png",
                "main.xml",
                "values.xml",
                "foo/foo.dat"
        );
    }

    public void testMergeWrite() throws Exception {
        AssetMerger merger = getAssetMerger();

        File folder = getWrittenResources();

        RecordingLogger logger = new RecordingLogger();

        AssetSet writtenSet = new AssetSet("unused");
        writtenSet.addSource(folder);
        writtenSet.loadFromFiles(logger);

        checkLogger(logger);

        // compare the two maps, but not using the full map as the set loaded from the output
        // won't contains all versions of each AssetItem item.
        compareResourceMaps(merger, writtenSet, false /*full compare*/);
    }

    public void testMergeBlob() throws Exception {
        AssetMerger merger = getAssetMerger();

        File folder = Files.createTempDir();
        merger.writeBlobTo(folder, new MergedAssetWriter(Files.createTempDir()));

        AssetMerger loadedMerger = new AssetMerger();
        loadedMerger.loadFromBlob(folder, true /*incrementalState*/);

        compareResourceMaps(merger, loadedMerger, true /*full compare*/);
    }

    /**
     * Tests the path replacement in the merger.xml file loaded from testData/
     * @throws Exception
     */
    public void testLoadingTestPathReplacement() throws Exception {
        File root = TestUtils.getRoot("assets", "baseMerge");
        File fakeRoot = getMergedBlobFolder(root);

        AssetMerger assetMerger = new AssetMerger();
        assetMerger.loadFromBlob(fakeRoot, true /*incrementalState*/);
        checkSourceFolders(assetMerger);

        List<AssetSet> sets = assetMerger.getDataSets();
        for (AssetSet set : sets) {
            List<File> sourceFiles = set.getSourceFiles();

            // there should only be one
            assertEquals(1, sourceFiles.size());

            File sourceFile = sourceFiles.get(0);
            assertTrue(String.format("File %s is located in %s", sourceFile, root),
                    sourceFile.getAbsolutePath().startsWith(root.getAbsolutePath()));
        }
    }

    public void testUpdate() throws Exception {
        File root = getIncMergeRoot("basicFiles");
        File fakeRoot = getMergedBlobFolder(root);
        AssetMerger assetMerger = new AssetMerger();
        assetMerger.loadFromBlob(fakeRoot, true /*incrementalState*/);
        checkSourceFolders(assetMerger);

        List<AssetSet> sets = assetMerger.getDataSets();
        assertEquals(2, sets.size());

        RecordingLogger logger = new RecordingLogger();

        // ----------------
        // first set is the main one, no change here
        AssetSet mainSet = sets.get(0);
        File mainFolder = new File(root, "main");

        // touched/removed files:
        File mainTouched = new File(mainFolder, "touched.png");
        mainSet.updateWith(mainFolder, mainTouched, FileStatus.CHANGED, logger);
        checkLogger(logger);

        File mainRemoved = new File(mainFolder, "removed.png");
        mainSet.updateWith(mainFolder, mainRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        File mainAdded = new File(mainFolder, "added.png");
        mainSet.updateWith(mainFolder, mainAdded, FileStatus.NEW, logger);
        checkLogger(logger);

        // ----------------
        // second set is the overlay one
        AssetSet overlaySet = sets.get(1);
        File overlayFolder = new File(root, "overlay");

        // new/removed files:
        File overlayAdded = new File(new File(overlayFolder, "foo"), "overlay_added.png");
        overlaySet.updateWith(overlayFolder, overlayAdded, FileStatus.NEW, logger);
        checkLogger(logger);

        File overlayRemoved = new File(overlayFolder, "overlay_removed.png");
        overlaySet.updateWith(overlayFolder, overlayRemoved, FileStatus.REMOVED, logger);
        checkLogger(logger);

        // validate for duplicates
        assetMerger.validateDataSets();

        // check the content.
        ListMultimap<String, AssetItem> mergedMap = assetMerger.getDataMap();

        // check untouched.png file is WRITTEN
        List<AssetItem> untouchedItem = mergedMap.get("untouched.png");
        assertEquals(1, untouchedItem.size());
        assertTrue(untouchedItem.get(0).isWritten());
        assertFalse(untouchedItem.get(0).isTouched());
        assertFalse(untouchedItem.get(0).isRemoved());

        // check touched.png file is TOUCHED
        List<AssetItem> touchedItem = mergedMap.get("touched.png");
        assertEquals(1, touchedItem.size());
        assertTrue(touchedItem.get(0).isWritten());
        assertTrue(touchedItem.get(0).isTouched());
        assertFalse(touchedItem.get(0).isRemoved());

        // check removed file is REMOVED
        List<AssetItem> removedItem = mergedMap.get("removed.png");
        assertEquals(1, removedItem.size());
        assertTrue(removedItem.get(0).isWritten());
        assertTrue(removedItem.get(0).isRemoved());

        // check new overlay: two objects, last one is TOUCHED
        List<AssetItem> overlayAddedItem = mergedMap.get("foo/overlay_added.png");
        assertEquals(2, overlayAddedItem.size());
        AssetItem newOverlay0 = overlayAddedItem.get(0);
        assertTrue(newOverlay0.isWritten());
        assertFalse(newOverlay0.isTouched());
        AssetItem newOverlay1 = overlayAddedItem.get(1);
        assertEquals(overlayAdded, newOverlay1.getSource().getFile());
        assertFalse(newOverlay1.isWritten());
        assertTrue(newOverlay1.isTouched());

        // check removed overlay: two objects, last one is removed
        List<AssetItem> overlayRemovedItem = mergedMap.get("overlay_removed.png");
        assertEquals(2, overlayRemovedItem.size());
        AssetItem overlayRemovedItem0 = overlayRemovedItem.get(0);
        assertFalse(overlayRemovedItem0.isWritten());
        assertFalse(overlayRemovedItem0.isTouched());
        AssetItem overlayRemovedItem1 = overlayRemovedItem.get(1);
        assertEquals(overlayRemoved, overlayRemovedItem1.getSource().getFile());
        assertTrue(overlayRemovedItem1.isWritten());
        assertTrue(overlayRemovedItem1.isRemoved());

        // write and check the result of writeResourceFolder
        // copy the current resOut which serves as pre incremental update state.
        File resFolder = getFolderCopy(new File(root, "assetOut"));

        // write the content of the resource merger.
        MergedAssetWriter writer = new MergedAssetWriter(resFolder);
        assetMerger.mergeData(writer, false /*doCleanUp*/);

        // Check the content by checking the colors. All files should be green
        checkImageColor(new File(resFolder, "untouched.png"), (int) 0xFF00FF00);
        checkImageColor(new File(resFolder, "touched.png"), (int) 0xFF00FF00);
        checkImageColor(new File(resFolder, "added.png"), (int) 0xFF00FF00);
        checkImageColor(new File(resFolder, "overlay_removed.png"), (int) 0xFF00FF00);
        checkImageColor(new File(new File(resFolder, "foo"), "overlay_added.png"), (int) 0xFF00FF00);

        // also check the removed file is not there.
        assertFalse(new File(resFolder, "removed.png").isFile());
    }

    public void testCheckValidUpdate() throws Exception {
        // first merger
        AssetMerger merger1 = createMerger(new String[][] {
                new String[] { "main",    ("/main/res1"), ("/main/res2") },
                new String[] { "overlay", ("/overlay/res1"), ("/overlay/res2") },
        });

        // 2nd merger with different order source files in sets.
        AssetMerger merger2 = createMerger(new String[][] {
                new String[] { "main",    ("/main/res2"), ("/main/res1") },
                new String[] { "overlay", ("/overlay/res1"), ("/overlay/res2") },
        });

        assertTrue(merger1.checkValidUpdate(merger2.getDataSets()));

        // write merger1 on disk to test writing empty AssetSets.
        File folder = Files.createTempDir();
        merger1.writeBlobTo(folder, new MergedAssetWriter(Files.createTempDir()));

        // reload it
        AssetMerger loadedMerger = new AssetMerger();
        loadedMerger.loadFromBlob(folder, true /*incrementalState*/);

        String expected = merger1.toString();
        String actual = loadedMerger.toString();
        if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
            expected = expected.replaceAll(Pattern.quote(File.separator), "/").
                                replaceAll("[A-Z]:/", "/");
            actual = actual.replaceAll(Pattern.quote(File.separator), "/").
                            replaceAll("[A-Z]:/", "/");
            assertEquals("Actual: " + actual + "\nExpected: " + expected, expected, actual);
        } else {
            assertTrue("Actual: " + actual + "\nExpected: " + expected,
                       loadedMerger.checkValidUpdate(merger1.getDataSets()));
        }
    }


    public void testUpdateWithRemovedOverlay() throws Exception {
        // Test with removed overlay
        AssetMerger merger1 = createMerger(new String[][] {
                new String[] { "main",    "/main/res1", "/main/res2" },
                new String[] { "overlay", "/overlay/res1", "/overlay/res2" },
        });

        // 2nd merger with different order source files in sets.
        AssetMerger merger2 = createMerger(new String[][] {
                new String[] { "main",    "/main/res2", "/main/res1" },
        });

        assertFalse(merger1.checkValidUpdate(merger2.getDataSets()));
    }

    public void testUpdateWithReplacedOverlays() throws Exception {
        // Test with different overlays
        AssetMerger merger1 = createMerger(new String[][] {
                new String[] { "main",    "/main/res1", "/main/res2" },
                new String[] { "overlay", "/overlay/res1", "/overlay/res2" },
        });

        // 2nd merger with different order source files in sets.
        AssetMerger merger2 = createMerger(new String[][] {
                new String[] { "main",    "/main/res2", "/main/res1" },
                new String[] { "overlay2", "/overlay2/res1", "/overlay2/res2" },
        });

        assertFalse(merger1.checkValidUpdate(merger2.getDataSets()));
    }

    public void testUpdateWithReorderedOverlays() throws Exception {
        // Test with different overlays
        AssetMerger merger1 = createMerger(new String[][] {
                new String[] { "main",    "/main/res1", "/main/res2" },
                new String[] { "overlay1", "/overlay1/res1", "/overlay1/res2" },
                new String[] { "overlay2", "/overlay2/res1", "/overlay2/res2" },
        });

        // 2nd merger with different order source files in sets.
        AssetMerger merger2 = createMerger(new String[][] {
                new String[] { "main",    "/main/res2", "/main/res1" },
                new String[] { "overlay2", "/overlay2/res1", "/overlay2/res2" },
                new String[] { "overlay1", "/overlay1/res1", "/overlay1/res2" },
        });

        assertFalse(merger1.checkValidUpdate(merger2.getDataSets()));
    }

    public void testUpdateWithRemovedSourceFile() throws Exception {
        // Test with different source files
        AssetMerger merger1 = createMerger(new String[][] {
                new String[] { "main",    "/main/res1", "/main/res2" },
        });

        // 2nd merger with different order source files in sets.
        AssetMerger merger2 = createMerger(new String[][] {
                new String[] { "main",    "/main/res1" },
        });

        assertFalse(merger1.checkValidUpdate(merger2.getDataSets()));
    }

    public void testChangedIgnoredFile() throws Exception {
        AssetSet assetSet = AssetSetTest.getBaseAssetSet();

        AssetMerger assetMerger = new AssetMerger();
        assetMerger.addDataSet(assetSet);

        File root = TestUtils.getRoot("assets", "baseSet");
        File changedCVSFoo = new File(root, "CVS/foo.txt");
        FileValidity<AssetSet> fileValidity = assetMerger.findDataSetContaining(changedCVSFoo);

        assertEquals(FileValidity.FileStatus.IGNORED_FILE, fileValidity.status);
    }

    /**
     * Creates a fake merge with given sets.
     *
     * the data is an array of sets.
     *
     * Each set is [ setName, folder1, folder2, ...]
     *
     * @param data
     * @return
     */
    private static AssetMerger createMerger(String[][] data) {
        AssetMerger merger = new AssetMerger();
        for (String[] setData : data) {
            AssetSet set = new AssetSet(setData[0]);
            merger.addDataSet(set);
            for (int i = 1, n = setData.length; i < n; i++) {
                set.addSource(new File(setData[i]));
            }
        }

        return merger;
    }

    private static AssetMerger getAssetMerger()
            throws IOException, MergingException {
        if (sAssetMerger == null) {
            File root = TestUtils.getRoot("assets", "baseMerge");

            AssetSet res = AssetSetTest.getBaseAssetSet();

            RecordingLogger logger = new RecordingLogger();

            AssetSet overlay = new AssetSet("overlay");
            overlay.addSource(new File(root, "overlay"));
            overlay.loadFromFiles(logger);

            checkLogger(logger);

            sAssetMerger = new AssetMerger();
            sAssetMerger.addDataSet(res);
            sAssetMerger.addDataSet(overlay);
        }

        return sAssetMerger;
    }

    private static File getWrittenResources() throws MergingException, IOException {
        AssetMerger assetMerger = getAssetMerger();

        File folder = Files.createTempDir();

        MergedAssetWriter writer = new MergedAssetWriter(folder);
        assetMerger.mergeData(writer, false /*doCleanUp*/);

        return folder;
    }

    private File getIncMergeRoot(String name) throws IOException {
        File root = TestUtils.getCanonicalRoot("assets", "incMergeData");
        return new File(root, name);
    }

    private static File getFolderCopy(File folder) throws IOException {
        File dest = Files.createTempDir();
        copyFolder(folder, dest);
        return dest;
    }

    private static void copyFolder(File from, File to) throws IOException {
        if (from.isFile()) {
            Files.copy(from, to);
        } else if (from.isDirectory()) {
            if (!to.exists()) {
                to.mkdirs();
            }

            File[] children = from.listFiles();
            if (children != null) {
                for (File f : children) {
                    copyFolder(f, new File(to, f.getName()));
                }
            }
        }
    }
}
