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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Wraps some common {@link File} operations on files and folders.
 * <p/>
 * This makes it possible to override/mock/stub some file operations in unit tests.
 */
public interface IFileOp {

    /**
     * Helper to delete a file or a directory.
     * For a directory, recursively deletes all of its content.
     * Files that cannot be deleted right away are marked for deletion on exit.
     * The argument can be null.
     */
    public abstract void deleteFileOrFolder(File fileOrFolder);

    /**
     * Sets the executable Unix permission (+x) on a file or folder.
     * <p/>
     * This attempts to use File#setExecutable through reflection if
     * it's available.
     * If this is not available, this invokes a chmod exec instead,
     * so there is no guarantee of it being fast.
     * <p/>
     * Caller must make sure to not invoke this under Windows.
     *
     * @param file The file to set permissions on.
     * @throws IOException If an I/O error occurs
     */
    public abstract void setExecutablePermission(File file) throws IOException;

    /**
     * Copies a binary file.
     *
     * @param source the source file to copy.
     * @param dest the destination file to write.
     * @throws FileNotFoundException if the source file doesn't exist.
     * @throws IOException if there's a problem reading or writing the file.
     */
    public abstract void copyFile(File source, File dest) throws IOException;

    /**
     * Checks whether 2 binary files are the same.
     *
     * @param source the source file to copy
     * @param destination the destination file to write
     * @throws FileNotFoundException if the source files don't exist.
     * @throws IOException if there's a problem reading the files.
     */
    public abstract boolean isSameFile(File source, File destination)
            throws IOException;

    /** Invokes {@link File#exists()} on the given {@code file}. */
    public abstract boolean exists(File file);

    /** Invokes {@link File#isFile()} on the given {@code file}. */
    public abstract boolean isFile(File file);

    /** Invokes {@link File#isDirectory()} on the given {@code file}. */
    public abstract boolean isDirectory(File file);

    /** Invokes {@link File#length()} on the given {@code file}. */
    public abstract long length(File file);

    /**
     * Invokes {@link File#delete()} on the given {@code file}.
     * Note: for a recursive folder version, consider {@link #deleteFileOrFolder(File)}.
     */
    public abstract boolean delete(File file);

    /** Invokes {@link File#mkdirs()} on the given {@code file}. */
    public abstract boolean mkdirs(File file);

    /** Invokes {@link File#listFiles()} on the given {@code file}. */
    public abstract File[] listFiles(File file);

    /** Invokes {@link File#renameTo(File)} on the given files. */
    public abstract boolean renameTo(File oldDir, File newDir);

    /** Creates a new {@link FileOutputStream} for the given {@code file}. */
    public abstract OutputStream newFileOutputStream(File file) throws FileNotFoundException;

}
