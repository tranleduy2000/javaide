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

import com.android.annotations.NonNull;
import com.google.common.base.Charsets;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;

public abstract class BaseTestCase extends TestCase {

    protected void verifyResourceExists(DataMap<? extends DataItem> dataMap,
                                        String... dataItemKeys) {
        ListMultimap<String, ? extends DataItem> map = dataMap.getDataMap();

        for (String resKey : dataItemKeys) {
            List<? extends DataItem> items = map.get(resKey);
            assertTrue("resource '" + resKey + "' is missing!", !items.isEmpty());
        }
    }

    /**
     * Compares two resource maps.
     *
     * if <var>fullCompare</var> is <code>true</code> then it'll make sure the multimaps contains
     * the same number of items, otherwise it'll only checks that each resource key is present
     * in both maps.
     *
     * @param dataMap1 the first resource Map
     * @param dataMap2 the second resource Map
     * @param fullCompare whether a full compare is requested.
     */
    protected void compareResourceMaps(DataMap<? extends DataItem> dataMap1,
                                       DataMap<? extends DataItem> dataMap2,
                                       boolean fullCompare) {
        assertEquals(dataMap1.size(), dataMap2.size());

        // compare the resources are all the same
        ListMultimap<String, ? extends DataItem> map1 = dataMap1.getDataMap();
        ListMultimap<String, ? extends DataItem> map2 = dataMap2.getDataMap();
        for (String key : map1.keySet()) {
            List<? extends DataItem> items1 = map1.get(key);
            List<? extends DataItem> items2 = map2.get(key);
            if (fullCompare) {
                assertEquals("Wrong size for " + key, items1.size(), items2.size());
            } else {
                boolean map1HasItem = !items1.isEmpty();
                boolean map2HasItem = !items2.isEmpty();
                assertEquals("resource " + key + " missing from one map", map1HasItem, map2HasItem);
            }
        }
    }

    protected static void checkImageColor(File file, int expectedColor) throws IOException {
        assertTrue("File '" + file.getAbsolutePath() + "' does not exist.", file.isFile());

        BufferedImage image = ImageIO.read(file);
        int rgb = image.getRGB(0, 0);
        assertEquals(String.format("Expected: 0x%08X, actual: 0x%08X for file %s",
                expectedColor, rgb, file),
                expectedColor, rgb);
    }

    /**
     * Returns a folder containing a merger blob data for the given test data folder.
     *
     * The folder is expected to contain a blob file.
     *
     * This is to work around the fact that the merger blob data contains full path, but we don't
     * know where this project is located on the drive. This rewrites the blob to contain the
     * actual folder.
     * (The blobs written in the test data contains placeholders for the path root and path
     * separators)
     *
     * @param folder the folder to use as the root folder when recreating paths.
     *
     * @return a new file that contains the merge blob
     * @throws java.io.IOException
     */
    protected static File getMergedBlobFolder(@NonNull File folder) throws IOException {
        File originalMerger = new File(folder, DataMerger.FN_MERGER_XML);
        return getMergedBlobFolder(folder, originalMerger);
    }

    /**
     * Returns a folder containing a merger blob data for the given test data folder, and the given
     * merger file.
     *
     * This is to work around the fact that the merger blob data contains full path, but we don't
     * know where this project is located on the drive. This rewrites the blob to contain the
     * actual folder.
     * (The blobs written in the test data contains placeholders for the path root and path
     * separators)
     *
     * @param folder the folder to use as the root folder when recreating paths.
     * @param mergerFile the merger file.
     *
     * @return a new file that contains the merge blob
     * @throws java.io.IOException
     */
    protected static File getMergedBlobFolder(@NonNull File folder, @NonNull File mergerFile)
            throws IOException {

        String content = Files.toString(mergerFile, Charsets.UTF_8);

        // search and replace $TOP$ with the root and $SEP$ with the platform separator.
        content = content.replaceAll(
                "\\$TOP\\$", Matcher.quoteReplacement(folder.getAbsolutePath())).
                replaceAll("\\$SEP\\$", Matcher.quoteReplacement(File.separator));

        File tempFolder = Files.createTempDir();
        Files.write(content, new File(tempFolder, DataMerger.FN_MERGER_XML), Charsets.UTF_8);

        return tempFolder;
    }

    /**
     * Post {@link #getMergedBlobFolder(java.io.File, java.io.File)} check. After the DataMerger is created
     * from the file generated, this checks that the file replacement works and all the files are
     * where they are supposed to be.
     *
     * @param dataMerger the data merger
     */
    protected void checkSourceFolders(
            DataMerger<? extends DataItem, ? extends DataFile, ? extends DataSet> dataMerger) {

        // Loop on all the data sets.
        for (DataSet<? extends DataItem, ? extends DataFile> set : dataMerger.getDataSets()) {
            // get the source files and verify they exists.
            List<File> files = set.getSourceFiles();
            for (File file : files) {
                assertTrue("Not a folder: " + file.getAbsolutePath(), file.isDirectory());
            }

            // for each source file, also check that the files inside are in fact inside
            // them. We don't check if those files are there though because the tests could
            // be testing with missing files to simulate updates.
            ListMultimap<String, ? extends DataItem> itemMap = set.getDataMap();

            for (DataItem item : itemMap.values()) {
                DataFile dataFile = item.getSource();
                File file = dataFile.getFile();

                assertNotNull("Not in source file: " + file.getAbsolutePath(),
                        set.findMatchingSourceFile(file));
            }
        }
    }

    protected static void checkLogger(RecordingLogger logger) {
        if (!logger.getErrorMsgs().isEmpty()) {
            assertTrue(logger.getErrorMsgs().get(0), false);
        }
    }
}
