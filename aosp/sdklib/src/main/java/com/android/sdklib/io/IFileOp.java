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

import com.android.annotations.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


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
     * It's ok for the file or folder to not exist at all.
     * The argument can be null.
     */
    void deleteFileOrFolder(@NonNull File fileOrFolder);

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
    void setExecutablePermission(@NonNull File file) throws IOException;

    /**
     * Sets the file or directory as read-only.
     *
     * @param file The file or directory to set permissions on.
     */
    void setReadOnly(@NonNull File file);

    /**
     * Copies a binary file.
     *
     * @param source the source file to copy.
     * @param dest the destination file to write.
     * @throws FileNotFoundException if the source file doesn't exist.
     * @throws IOException if there's a problem reading or writing the file.
     */
    void copyFile(@NonNull File source, @NonNull File dest) throws IOException;

    /**
     * Checks whether 2 binary files are the same.
     *
     * @param file1 the source file to copy
     * @param file2 the destination file to write
     * @throws FileNotFoundException if the source files don't exist.
     * @throws IOException if there's a problem reading the files.
     */
    boolean isSameFile(@NonNull File file1, @NonNull File file2)
            throws IOException;

    /** Invokes {@link File#exists()} on the given {@code file}. */
    boolean exists(@NonNull File file);

    /** Invokes {@link File#isFile()} on the given {@code file}. */
    boolean isFile(@NonNull File file);

    /** Invokes {@link File#isDirectory()} on the given {@code file}. */
    boolean isDirectory(@NonNull File file);

    /** Invokes {@link File#length()} on the given {@code file}. */
    long length(@NonNull File file);

    /**
     * Invokes {@link File#delete()} on the given {@code file}.
     * Note: for a recursive folder version, consider {@link #deleteFileOrFolder(File)}.
     */
    boolean delete(@NonNull File file);

    /** Invokes {@link File#mkdirs()} on the given {@code file}. */
    boolean mkdirs(@NonNull File file);

    /**
     * Invokes {@link File#listFiles()} on the given {@code file}.
     * Contrary to the Java API, this returns an empty array instead of null when the
     * directory does not exist.
     */
    @NonNull
    File[] listFiles(@NonNull File file);

    /** Invokes {@link File#renameTo(File)} on the given files. */
    boolean renameTo(@NonNull File oldDir, @NonNull File newDir);

    /** Creates a new {@link OutputStream} for the given {@code file}. */
    @NonNull
    OutputStream newFileOutputStream(@NonNull File file)
            throws FileNotFoundException;

    /** Creates a new {@link InputStream} for the given {@code file}. */
    @NonNull
    InputStream newFileInputStream(@NonNull File file)
            throws FileNotFoundException;

    /**
     * Load {@link Properties} from a file. Returns an empty property set on error.
     *
     * @param file A non-null file to load from. File may not exist.
     * @return A new {@link Properties} with the properties loaded from the file,
     *          or an empty property set in case of error.
     */
    @NonNull
    Properties loadProperties(@NonNull File file);

    /**
     * Saves (write, store) the given {@link Properties} into the given {@link File}.
     *
     * @param file A non-null file to write to.
     * @param props The properties to write.
     * @param comments A non-null description of the properly list, written in the file.
     * @throws IOException if the write operation failed.
     */
    void saveProperties(
            @NonNull File file,
            @NonNull Properties props,
            @NonNull String comments) throws IOException;

    /**
     * Returns the lastModified attribute of the file.
     *
     * @see File#lastModified()
     * @param file The non-null file of which to retrieve the lastModified attribute.
     * @return The last-modified attribute of the file, in milliseconds since The Epoch.
     */
    long lastModified(@NonNull File file);
}
