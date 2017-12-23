/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.sdklib.SdkConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Wraps some common {@link File} operations on files and folders.
 * <p/>
 * This makes it possible to override/mock/stub some file operations in unit tests.
 */
public class FileOp implements IFileOp {

    /**
     * Reflection method for File.setExecutable(boolean, boolean). Only present in Java 6.
     */
    private static Method sFileSetExecutable = null;

    /**
     * Parameters to call File.setExecutable through reflection.
     */
    private final static Object[] sFileSetExecutableParams = new Object[] {
        Boolean.TRUE, Boolean.FALSE };

    // static initialization of sFileSetExecutable.
    static {
        try {
            sFileSetExecutable = File.class.getMethod("setExecutable", //$NON-NLS-1$
                    boolean.class, boolean.class);

        } catch (SecurityException e) {
            // do nothing we'll use chdmod instead
        } catch (NoSuchMethodException e) {
            // do nothing we'll use chdmod instead
        }
    }

    /**
     * Appends the given {@code segments} to the {@code base} file.
     *
     * @param base A base file, non-null.
     * @param segments Individual folder or filename segments to append to the base file.
     * @return A new file representing the concatenation of the base path with all the segments.
     */
    public static File append(File base, String...segments) {
        for (String segment : segments) {
            base = new File(base, segment);
        }
        return base;
    }

    /**
     * Appends the given {@code segments} to the {@code base} file.
     *
     * @param base A base file path, non-empty and non-null.
     * @param segments Individual folder or filename segments to append to the base path.
     * @return A new file representing the concatenation of the base path with all the segments.
     */
    public static File append(String base, String...segments) {
        return append(new File(base), segments);
    }

    /**
     * Helper to delete a file or a directory.
     * For a directory, recursively deletes all of its content.
     * Files that cannot be deleted right away are marked for deletion on exit.
     * The argument can be null.
     */
    public void deleteFileOrFolder(File fileOrFolder) {
        if (fileOrFolder != null) {
            if (isDirectory(fileOrFolder)) {
                // Must delete content recursively first
                File[] files = fileOrFolder.listFiles();
                if (files != null) {
                    for (File item : files) {
                        deleteFileOrFolder(item);
                    }
                }
            }

            if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
                // Trying to delete a resource on windows might fail if there's a file
                // indexer locking the resource. Generally retrying will be enough to
                // make it work.
                //
                // Try for half a second before giving up.

                for (int i = 0; i < 5; i++) {
                    if (fileOrFolder.delete()) {
                        return;
                    }

                    try {
                        Thread.sleep(100 /*ms*/);
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }

                fileOrFolder.deleteOnExit();

            } else {
                // On Linux or Mac, just straight deleting it should just work.

                if (!fileOrFolder.delete()) {
                    fileOrFolder.deleteOnExit();
                }
            }
        }
    }

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
    public void setExecutablePermission(File file) throws IOException {

        if (sFileSetExecutable != null) {
            try {
                sFileSetExecutable.invoke(file, sFileSetExecutableParams);
                return;
            } catch (IllegalArgumentException e) {
                // we'll run chmod below
            } catch (IllegalAccessException e) {
                // we'll run chmod below
            } catch (InvocationTargetException e) {
                // we'll run chmod below
            }
        }

        Runtime.getRuntime().exec(new String[] {
               "chmod", "+x", file.getAbsolutePath()  //$NON-NLS-1$ //$NON-NLS-2$
            });
    }

    /**
     * Copies a binary file.
     *
     * @param source the source file to copy.
     * @param dest the destination file to write.
     * @throws FileNotFoundException if the source file doesn't exist.
     * @throws IOException if there's a problem reading or writing the file.
     */
    public void copyFile(File source, File dest) throws IOException {
        byte[] buffer = new byte[8192];

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(dest);

            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }

    /**
     * Checks whether 2 binary files are the same.
     *
     * @param source the source file to copy
     * @param destination the destination file to write
     * @throws FileNotFoundException if the source files don't exist.
     * @throws IOException if there's a problem reading the files.
     */
    public boolean isSameFile(File source, File destination) throws IOException {

        if (source.length() != destination.length()) {
            return false;
        }

        FileInputStream fis1 = null;
        FileInputStream fis2 = null;

        try {
            fis1 = new FileInputStream(source);
            fis2 = new FileInputStream(destination);

            byte[] buffer1 = new byte[8192];
            byte[] buffer2 = new byte[8192];

            int read1;
            while ((read1 = fis1.read(buffer1)) != -1) {
                int read2 = 0;
                while (read2 < read1) {
                    int n = fis2.read(buffer2, read2, read1 - read2);
                    if (n == -1) {
                        break;
                    }
                }

                if (read2 != read1) {
                    return false;
                }

                if (!Arrays.equals(buffer1, buffer2)) {
                    return false;
                }
            }
        } finally {
            if (fis2 != null) {
                try {
                    fis2.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (fis1 != null) {
                try {
                    fis1.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return true;
    }

    /** Invokes {@link File#isFile()} on the given {@code file}. */
    public boolean isFile(File file) {
        return file.isFile();
    }

    /** Invokes {@link File#isDirectory()} on the given {@code file}. */
    public boolean isDirectory(File file) {
        return file.isDirectory();
    }

    /** Invokes {@link File#exists()} on the given {@code file}. */
    public boolean exists(File file) {
        return file.exists();
    }

    /** Invokes {@link File#length()} on the given {@code file}. */
    public long length(File file) {
        return file.length();
    }

    /**
     * Invokes {@link File#delete()} on the given {@code file}.
     * Note: for a recursive folder version, consider {@link #deleteFileOrFolder(File)}.
     */
    public boolean delete(File file) {
        return file.delete();
    }

    /** Invokes {@link File#mkdirs()} on the given {@code file}. */
    public boolean mkdirs(File file) {
        return file.mkdirs();
    }

    /** Invokes {@link File#listFiles()} on the given {@code file}. */
    public File[] listFiles(File file) {
        return file.listFiles();
    }

    /** Invokes {@link File#renameTo(File)} on the given files. */
    public boolean renameTo(File oldFile, File newFile) {
        return oldFile.renameTo(newFile);
    }

    /** Creates a new {@link FileOutputStream} for the given {@code file}. */
    public OutputStream newFileOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }
}
