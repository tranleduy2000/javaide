/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sdklib.io.FileOp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import junit.framework.TestCase;

public class LocalDirInfoTest extends TestCase {

    private final FileOp mFOp = new FileOp();
    private File mTempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTempDir = File.createTempFile("test", "dir");
        assertTrue(mTempDir.delete());
        assertTrue(mTempDir.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mFOp.deleteFileOrFolder(mTempDir);
        mTempDir = null;
    }

    // Test: start with empty directory, removing the dir marks it as changed.
    public final void testHasChanged_Empty() {
        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Removing the dir marks it as changed
        mFOp.deleteFileOrFolder(mTempDir);
        assertTrue(di.hasChanged());
    }

    // Test: start with empty directory, adding a file marks it as changed.
    public final void testHasChanged_AddFile() throws Exception {
        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Adding a file inside the dir marks it as changed
        createFileContent(mTempDir, "some_file.txt", "whatever content");
        assertTrue(di.hasChanged());
    }

    // Test: removing any file marks it as changed.
    public final void testHasChanged_RemoveFile() throws Exception {
        File f = createFileContent(mTempDir, "some_file.txt", "whatever content");

        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Removing a file inside the dir marks it as changed
        mFOp.deleteFileOrFolder(f);
        assertTrue(di.hasChanged());
    }

    // Test: start with empty directory, adding a directory marks it as changed.
    public final void testHasChanged_AddDir() throws Exception {
        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Adding a file inside the dir marks it as changed
        File dir = new File(mTempDir, "some_dir");
        assertTrue(dir.mkdirs());
        assertTrue(di.hasChanged());
    }

    // Test: removing any directory marks it as changed.
    public final void testHasChanged_RemoveDir() throws Exception {
        File dir = new File(mTempDir, "some_dir");
        assertTrue(dir.mkdirs());

        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Removing a dir marks it as changed
        mFOp.deleteFileOrFolder(dir);
        assertTrue(di.hasChanged());
    }

    // Test: directory that contains a source.properties, change source.properties's content
    public final void testHasChanged_SourceProps_Changed() throws Exception {
        createFileContent(mTempDir, "source.properties", "key=value");

        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Change source.properties's content
        createFileContent(mTempDir, "source.properties", "other_key=other_value");
        assertTrue(di.hasChanged());
    }

    // Test: directory that contains a source.properties, change source.properties's timestamp
    public final void testHasChanged_SourceProps_Timestamp() throws Exception {
        createFileContent(mTempDir, "source.properties", "key=value");

        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Recreate source.properties with the same content, this changes its timestamp.
        // Note: the last-modified resolution on Linux is 1 second on ext3/ext4 file systems
        // so we need at least 1 second in between both edits other they will have the same
        // last-modified value.
        Thread.sleep(1100);
        createFileContent(mTempDir, "source.properties", "key=value");
        assertTrue(di.hasChanged());
    }

    // Test: directory that contains a source.properties, change delete source.properties
    public final void testHasChanged_SourceProps_Deleted() throws Exception {
        File sp = createFileContent(mTempDir, "source.properties", "key=value");

        LocalDirInfo di = new LocalDirInfo(mFOp, mTempDir);
        assertFalse(di.hasChanged());

        // Removing the source.properties marks it as changed
        mFOp.deleteFileOrFolder(sp);
        assertTrue(di.hasChanged());
    }

    //---- Helpers

    // Creates a new file with the specified name, in the specified
    // parent directory with the given UTF-8 content.
    private static File createFileContent(File parentDir,
                                          String fileName,
                                          String fileContent) throws IOException {
        File f = new File(parentDir, fileName);
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(f),
                                                       Charset.forName("UTF-8"));
        try {
          fw.write(fileContent);
        } finally {
          fw.close();
        }
        return f;
      }


}
