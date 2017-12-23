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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.google.common.io.Closer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * Wraps some common {@link File} operations on files and folders.
 * <p/>
 * This makes it possible to override/mock/stub some file operations in unit tests.
 */
public class FileOp implements IFileOp {

    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * Reflection method for File.setExecutable(boolean, boolean). Only present in Java 6.
     */
    private static Method sFileSetExecutable = null;

    /**
     * Parameters to call File.setExecutable through reflection.
     */
    private static final Object[] sFileSetExecutableParams = new Object[] {
        Boolean.TRUE, Boolean.FALSE };

    // static initialization of sFileSetExecutable.
    static {
        try {
            sFileSetExecutable = File.class.getMethod("setExecutable", //$NON-NLS-1$
                    boolean.class, boolean.class);

        } catch (SecurityException e) {
            // do nothing we'll use chmod instead
        } catch (NoSuchMethodException e) {
            // do nothing we'll use chmod instead
        }
    }

    /**
     * Appends the given {@code segments} to the {@code base} file.
     *
     * @param base A base file, non-null.
     * @param segments Individual folder or filename segments to append to the base file.
     * @return A new file representing the concatenation of the base path with all the segments.
     */
    public static File append(@NonNull File base, @NonNull String...segments) {
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
    public static File append(@NonNull String base, @NonNull String...segments) {
        return append(new File(base), segments);
    }

    /**
     * Helper to delete a file or a directory.
     * For a directory, recursively deletes all of its content.
     * Files that cannot be deleted right away are marked for deletion on exit.
     * It's ok for the file or folder to not exist at all.
     * The argument can be null.
     */
    @Override
    public void deleteFileOrFolder(@NonNull File fileOrFolder) {
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

            // Don't try to delete it if it doesn't exist.
            if (!exists(fileOrFolder)) {
                return;
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
    @Override
    public void setExecutablePermission(@NonNull File file) throws IOException {

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

    @Override
    public void setReadOnly(@NonNull File file) {
        file.setReadOnly();
    }

    /**
     * Copies a binary file.
     *
     * @param source the source file to copy.
     * @param dest the destination file to write.
     * @throws FileNotFoundException if the source file doesn't exist.
     * @throws IOException if there's a problem reading or writing the file.
     */
    @Override
    public void copyFile(@NonNull File source, @NonNull File dest) throws IOException {
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
     * @param file1 the source file to copy
     * @param file2 the destination file to write
     * @throws FileNotFoundException if the source files don't exist.
     * @throws IOException if there's a problem reading the files.
     */
    @Override
    public boolean isSameFile(@NonNull File file1, @NonNull File file2) throws IOException {

        if (file1.length() != file2.length()) {
            return false;
        }

        FileInputStream fis1 = null;
        FileInputStream fis2 = null;

        try {
            fis1 = new FileInputStream(file1);
            fis2 = new FileInputStream(file2);

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
    @Override
    public boolean isFile(@NonNull File file) {
        return file.isFile();
    }

    /** Invokes {@link File#isDirectory()} on the given {@code file}. */
    @Override
    public boolean isDirectory(@NonNull File file) {
        return file.isDirectory();
    }

    /** Invokes {@link File#exists()} on the given {@code file}. */
    @Override
    public boolean exists(@NonNull File file) {
        return file.exists();
    }

    /** Invokes {@link File#length()} on the given {@code file}. */
    @Override
    public long length(@NonNull File file) {
        return file.length();
    }

    /**
     * Invokes {@link File#delete()} on the given {@code file}.
     * Note: for a recursive folder version, consider {@link #deleteFileOrFolder(File)}.
     */
    @Override
    public boolean delete(@NonNull File file) {
        return file.delete();
    }

    /** Invokes {@link File#mkdirs()} on the given {@code file}. */
    @Override
    public boolean mkdirs(@NonNull File file) {
        return file.mkdirs();
    }

    /**
     * Invokes {@link File#listFiles()} on the given {@code file}.
     * Contrary to the Java API, this returns an empty array instead of null when the
     * directory does not exist.
     */
    @Override
    @NonNull
    public File[] listFiles(@NonNull File file) {
        File[] r = file.listFiles();
        if (r == null) {
            return EMPTY_FILE_ARRAY;
        } else {
            return r;
        }
    }

    /** Invokes {@link File#renameTo(File)} on the given files. */
    @Override
    public boolean renameTo(@NonNull File oldFile, @NonNull File newFile) {
        return oldFile.renameTo(newFile);
    }

    /** Creates a new {@link OutputStream} for the given {@code file}. */
    @Override
    @NonNull
    public OutputStream newFileOutputStream(@NonNull File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    /** Creates a new {@link InputStream} for the given {@code file}. */
    @Override
    @NonNull
    public InputStream newFileInputStream(@NonNull File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    @NonNull
    public Properties loadProperties(@NonNull File file) {
        Properties props = new Properties();
        Closer closer = Closer.create();
        try {
            FileInputStream fis = closer.register(new FileInputStream(file));
            props.load(fis);
        } catch (IOException ignore) {
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
            }
        }
        return props;
    }

    @Override
    public void saveProperties(
            @NonNull File file,
            @NonNull Properties props,
            @NonNull String comments) throws IOException {
        Closer closer = Closer.create();
        try {
            OutputStream fos = closer.register(newFileOutputStream(file));
            props.store(fos, comments);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Override
    public long lastModified(@NonNull File file) {
        return file.lastModified();
    }

    /**
     * Computes a relative path from "toBeRelative" relative to "baseDir".
     *
     * Rule:
     * - let relative2 = makeRelative(path1, path2)
     * - then pathJoin(path1 + relative2) == path2 after canonicalization.
     *
     * Principle:
     * - let base         = /c1/c2.../cN/a1/a2../aN
     * - let toBeRelative = /c1/c2.../cN/b1/b2../bN
     * - result is removes the common paths, goes back from aN to cN then to bN:
     * - result           =              ../..../../1/b2../bN
     *
     * @param baseDir The base directory to be relative to.
     * @param toBeRelative The file or directory to make relative to the base.
     * @return A path that makes toBeRelative relative to baseDir.
     * @throws IOException If drive letters don't match on Windows or path canonicalization fails.
     */
    @NonNull
    public static String makeRelative(@NonNull File baseDir, @NonNull File toBeRelative)
            throws IOException {
        return makeRelativeImpl(
                baseDir.getCanonicalPath(),
                toBeRelative.getCanonicalPath(),
                SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS,
                File.separator);
    }

    /**
     * Implementation detail of makeRelative to make it testable
     * Independently of the platform.
     */
    @NonNull
    static String makeRelativeImpl(@NonNull String path1,
                                   @NonNull String path2,
                                   boolean isWindows,
                                   @NonNull String dirSeparator)
            throws IOException {
        if (isWindows) {
            // Check whether both path are on the same drive letter, if any.
            String p1 = path1;
            String p2 = path2;
            char drive1 = (p1.length() >= 2 && p1.charAt(1) == ':') ? p1.charAt(0) : 0;
            char drive2 = (p2.length() >= 2 && p2.charAt(1) == ':') ? p2.charAt(0) : 0;
            if (drive1 != drive2) {
                // Either a mix of UNC vs drive or not the same drives.
                throw new IOException("makeRelative: incompatible drive letters");
            }
        }

        String[] segments1 = path1.split(Pattern.quote(dirSeparator));
        String[] segments2 = path2.split(Pattern.quote(dirSeparator));

        int len1 = segments1.length;
        int len2 = segments2.length;
        int len = Math.min(len1, len2);
        int start = 0;
        for (; start < len; start++) {
            // On Windows should compare in case-insensitive.
            // Mac & Linux file systems can be both type, although their default
            // is generally to have a case-sensitive file system.
            if (( isWindows && !segments1[start].equalsIgnoreCase(segments2[start])) ||
                (!isWindows && !segments1[start].equals(segments2[start]))) {
                break;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = start; i < len1; i++) {
            result.append("..").append(dirSeparator);
        }
        while (start < len2) {
            result.append(segments2[start]);
            if (++start < len2) {
                result.append(dirSeparator);
            }
        }

        return result.toString();
    }
}
