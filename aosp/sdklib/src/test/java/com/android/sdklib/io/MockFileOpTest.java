/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.sdklib.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Unit-test for the {@link MockFileOp}, which is a mock of {@link FileOp} that doesn't
 * touch the file system. Just testing the test.
 */
public class MockFileOpTest extends TestCase {

    private MockFileOp m;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m = new MockFileOp();
    }

    private File createFile(String...segments) {
        File f = null;
        for (String segment : segments) {
            if (f == null) {
                f = new File(segment);
            } else {
                f = new File(f, segment);
            }
        }

        return f;
    }

    public void testIsFile() {
        File f1 = createFile("/dir1", "file1");
        assertFalse(m.isFile(f1));

        m.recordExistingFile("/dir1/file1");
        assertTrue(m.isFile(f1));

        assertEquals(
                "[/dir1/file1]",
                Arrays.toString(m.getExistingFiles()));
    }

    public void testIsDirectory() {
        File d4 = createFile("/dir1", "dir2", "dir3", "dir4");
        File f7 = createFile("/dir1", "dir2", "dir6", "file7");
        assertFalse(m.isDirectory(d4));

        m.recordExistingFolder("/dir1/dir2/dir3/dir4");
        m.recordExistingFile("/dir1/dir2/dir6/file7");
        assertTrue(m.isDirectory(d4));
        assertFalse(m.isDirectory(f7));

        // any intermediate directory exists implicitly
        assertTrue(m.isDirectory(createFile("/")));
        assertTrue(m.isDirectory(createFile("/dir1")));
        assertTrue(m.isDirectory(createFile("/dir1", "dir2")));
        assertTrue(m.isDirectory(createFile("/dir1", "dir2", "dir3")));
        assertTrue(m.isDirectory(createFile("/dir1", "dir2", "dir6")));

        assertEquals(
                "[/dir1/dir2/dir3/dir4]",
                Arrays.toString(m.getExistingFolders()));
    }

    public void testDelete() {
        m.recordExistingFolder("/dir1");
        m.recordExistingFile("/dir1/file1");
        m.recordExistingFile("/dir1/file2");

        assertEquals(
                "[/dir1/file1, /dir1/file2]",
                Arrays.toString(m.getExistingFiles()));

        assertTrue(m.delete(createFile("/dir1", "file1")));
        assertFalse(m.delete(createFile("/dir1", "file3")));
        assertFalse(m.delete(createFile("/dir2", "file2")));
        assertEquals(
                "[/dir1/file2]",
                Arrays.toString(m.getExistingFiles()));

        // deleting a directory with files in it fails
        assertFalse(m.delete(createFile("/dir1")));
        // but it works if the directory is empty
        assertTrue(m.delete(createFile("/dir1", "file2")));
        assertTrue(m.delete(createFile("/dir1")));
    }

    public void testListFiles() {
        m.recordExistingFolder("/dir1");
        m.recordExistingFile("/dir1/file1");
        m.recordExistingFile("/dir1/file2");
        m.recordExistingFile("/dir1/dir2/file3");
        m.recordExistingFile("/dir4/file4");

        assertEquals(
                "[]",
                m.getAgnosticAbsPath(Arrays.toString(m.listFiles(createFile("/not_a_dir")))));

        assertEquals(
                "[/dir1/dir2/file3]",
                m.getAgnosticAbsPath(Arrays.toString(m.listFiles(createFile("/dir1", "dir2")))));

        assertEquals(
                "[/dir1/dir2/file3, /dir1/file1, /dir1/file2]",
                m.getAgnosticAbsPath(Arrays.toString(m.listFiles(createFile("/dir1")))));
    }

    public void testMkDirs() {
        assertEquals("[]", Arrays.toString(m.getExistingFolders()));

        assertTrue(m.mkdirs(createFile("/dir1")));
        assertEquals("[/, /dir1]", Arrays.toString(m.getExistingFolders()));

        m.recordExistingFolder("/dir1");
        assertEquals("[/, /dir1]", Arrays.toString(m.getExistingFolders()));

        assertTrue(m.mkdirs(createFile("/dir1/dir2/dir3")));
        assertEquals(
                "[/, /dir1, /dir1/dir2, /dir1/dir2/dir3]",
                Arrays.toString(m.getExistingFolders()));
    }

    public void testRenameTo() {
        m.recordExistingFile("/dir1/dir2/dir6/file7");
        m.recordExistingFolder("/dir1/dir2/dir3/dir4");

        assertEquals("[/dir1/dir2/dir6/file7]", Arrays.toString(m.getExistingFiles()));
        assertEquals("[/dir1/dir2/dir3/dir4]", Arrays.toString(m.getExistingFolders()));

        assertTrue(m.renameTo(createFile("/dir1", "dir2"), createFile("/dir1", "newDir2")));
        assertEquals("[/dir1/newDir2/dir6/file7]", Arrays.toString(m.getExistingFiles()));
        assertEquals("[/dir1/newDir2/dir3/dir4]", Arrays.toString(m.getExistingFolders()));

        assertTrue(m.renameTo(
                createFile("/dir1", "newDir2", "dir6", "file7"),
                createFile("/dir1", "newDir2", "dir6", "newFile7")));
        assertTrue(m.renameTo(
                createFile("/dir1", "newDir2", "dir3", "dir4"),
                createFile("/dir1", "newDir2", "dir3", "newDir4")));
        assertEquals("[/dir1/newDir2/dir6/newFile7]", Arrays.toString(m.getExistingFiles()));
        assertEquals("[/dir1/newDir2/dir3/newDir4]", Arrays.toString(m.getExistingFolders()));
    }

    public void testNewFileOutputStream() throws Exception {
        assertEquals("[]", Arrays.toString(m.getOutputStreams()));

        File f = createFile("/dir1", "dir2", "simple ascii");
        OutputStream os = m.newFileOutputStream(f);
        assertNotNull(os);
        os.write("regular ascii".getBytes("UTF-8"));
        os.close();

        f = createFile("/dir1", "dir2", "utf-8 test");
        os = m.newFileOutputStream(f);
        assertNotNull(os);
        os.write("nihongo in UTF-8: 日本語".getBytes("UTF-8"));
        os.close();

        f = createFile("/dir1", "dir2", "forgot to close");
        os = m.newFileOutputStream(f);
        assertNotNull(os);
        os.write("wrote stuff but not closing the stream".getBytes("UTF-8"));

        assertEquals(
                "[</dir1/dir2/simple ascii: 'regular ascii'>, " +
                 "</dir1/dir2/utf-8 test: 'nihongo in UTF-8: 日本語'>, " +
                 "</dir1/dir2/forgot to close: (stream not closed properly)>]",
                Arrays.toString(m.getOutputStreams()));
    }

    public void testMakeRelative() throws Exception {
        assertEquals("dir3",
            FileOp.makeRelativeImpl("/dir1/dir2",
                                    "/dir1/dir2/dir3",
                                    false, "/"));

        assertEquals("../../../dir3",
                FileOp.makeRelativeImpl("/dir1/dir2/dir4/dir5/dir6",
                                        "/dir1/dir2/dir3",
                                        false, "/"));

        assertEquals("dir3/dir4/dir5/dir6",
                FileOp.makeRelativeImpl("/dir1/dir2/",
                                        "/dir1/dir2/dir3/dir4/dir5/dir6",
                                        false, "/"));

        // case-sensitive on non-Windows.
        assertEquals("../DIR2/dir3/DIR4/dir5/DIR6",
                FileOp.makeRelativeImpl("/dir1/dir2/",
                                        "/dir1/DIR2/dir3/DIR4/dir5/DIR6",
                                        false, "/"));

        // same path: empty result.
        assertEquals("",
                FileOp.makeRelativeImpl("/dir1/dir2/dir3",
                                        "/dir1/dir2/dir3",
                                        false, "/"));

        // same drive letters on Windows
        assertEquals("..\\..\\..\\dir3",
                FileOp.makeRelativeImpl("C:\\dir1\\dir2\\dir4\\dir5\\dir6",
                                        "C:\\dir1\\dir2\\dir3",
                                        true, "\\"));

        // not case-sensitive on Windows, results will be mixed.
        assertEquals("dir3/DIR4/dir5/DIR6",
                FileOp.makeRelativeImpl("/DIR1/dir2/",
                                        "/dir1/DIR2/dir3/DIR4/dir5/DIR6",
                                        true, "/"));

        // UNC path on Windows
        assertEquals("..\\..\\..\\dir3",
                FileOp.makeRelativeImpl("\\\\myserver.domain\\dir1\\dir2\\dir4\\dir5\\dir6",
                                        "\\\\myserver.domain\\dir1\\dir2\\dir3",
                                        true, "\\"));

        // different drive letters are not supported
        try {
            FileOp.makeRelativeImpl("C:\\dir1\\dir2\\dir4\\dir5\\dir6",
                                    "D:\\dir1\\dir2\\dir3",
                                    true, "\\");
            fail("Expected: IOException. Actual: no exception.");
        } catch (IOException e) {
            assertEquals("makeRelative: incompatible drive letters", e.getMessage());
        }
    }
}
