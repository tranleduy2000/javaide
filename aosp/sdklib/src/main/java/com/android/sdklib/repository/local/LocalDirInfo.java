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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.Adler32;

/**
 * Keeps information on a visited directory to quickly determine if it
 * has changed later. A directory has changed if its timestamp has been
 * modified, or if an underlying source.properties file has changed in
 * timestamp or checksum.
 * <p/>
 * Note that depending on the filesystem & OS, the content of the files in
 * a directory can change without the directory's last-modified property
 * changing. To have a consistent behavior between OSes, we compute a quick
 * checksum using all the files & directories modified timestamps.
 * The content of files is not included though, except for the checksum on
 * the source.property file since this one is the most important for the SDK.
 * <p/>
 * The {@link #hashCode()} and {@link #equals(Object)} methods directly
 * defer to the underlying File object. This allows the DirInfo to be placed
 * into a map and still call {@link Map#containsKey(Object)} with a File
 * object to check whether there's a corresponding DirInfo in the map.
 */
class LocalDirInfo {
    @NonNull
    private final IFileOp mFileOp;
    @NonNull
    private final File mDir;
    private final long mDirModifiedTS;
    private final long mDirChecksum;
    private final long mPropsModifiedTS;
    private final long mPropsChecksum;

    /**
     * Creates a new immutable {@link LocalDirInfo}.
     *
     * @param fileOp The {@link FileOp} to use for all file-based interactions.
     * @param dir The platform/addon directory of the target. It should be a directory.
     */
    public LocalDirInfo(@NonNull IFileOp fileOp, @NonNull File dir) {
        mFileOp = fileOp;
        mDir = dir;
        mDirModifiedTS = mFileOp.lastModified(dir);

        // Capture some info about the source.properties file if it exists.
        // We use propsModifiedTS == 0 to mean there is no props file.
        long propsChecksum = 0;
        long propsModifiedTS = 0;
        File props = new File(dir, SdkConstants.FN_SOURCE_PROP);
        if (mFileOp.isFile(props)) {
            propsModifiedTS = mFileOp.lastModified(props);
            propsChecksum = getFileChecksum(props);
        }
        mPropsModifiedTS = propsModifiedTS;
        mPropsChecksum = propsChecksum;
        mDirChecksum = getDirChecksum(mDir);
    }

    /**
     * Checks whether the directory/source.properties attributes have changed.
     *
     * @return True if the directory modified timestamp or
     *  its source.property files have changed.
     */
    public boolean hasChanged() {
        // Does platform directory still exist?
        if (!mFileOp.isDirectory(mDir)) {
            return true;
        }
        // Has platform directory modified-timestamp changed?
        if (mDirModifiedTS != mFileOp.lastModified(mDir)) {
            return true;
        }

        File props = new File(mDir, SdkConstants.FN_SOURCE_PROP);

        // The directory did not have a props file if target was null or
        // if mPropsModifiedTS is 0.
        boolean hadProps = mPropsModifiedTS != 0;

        // Was there a props file and it vanished, or there wasn't and there's one now?
        if (hadProps != mFileOp.isFile(props)) {
            return true;
        }

        if (hadProps) {
            // Has source.props file modified-timestamp changed?
            if (mPropsModifiedTS != mFileOp.lastModified(props)) {
                return true;
            }
            // Had the content of source.props changed?
            if (mPropsChecksum != getFileChecksum(props)) {
                return true;
            }
        }

        // Has the deep directory checksum changed?
        if (mDirChecksum != getDirChecksum(mDir)) {
            return true;
        }

        return false;
    }

    /**
     * Computes an adler32 checksum (source.props are small files, so this
     * should be OK with an acceptable collision rate.)
     */
    private long getFileChecksum(@NonNull File file) {
        InputStream fis = null;
        try {
            fis = mFileOp.newFileInputStream(file);
            Adler32 a = new Adler32();
            byte[] buf = new byte[1024];
            int n;
            while ((n = fis.read(buf)) > 0) {
                a.update(buf, 0, n);
            }
            return a.getValue();
        } catch (Exception ignore) {
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch(Exception ignore) {}
        }
        return 0;
    }

    /**
     * Computes a checksum using the last-modified attributes of all
     * the files and <em>first-level</em>directories in this root directory.
     * <p/>
     * Heuristic: the SDK Manager updates package by replacing whole directories
     * so we don't need to do a recursive deep-first checksum of all files. Only
     * the top-level of the package directory should be sufficient to detect
     * SDK updates.
     */
    private long getDirChecksum(@NonNull File dir) {
        long checksum = mFileOp.lastModified(dir);

        // Get the file & directory list sorted by case-insensitive name
        // to make the checksum more consistent.
        File[] files = mFileOp.listFiles(dir);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        for (File file : files) {
            checksum = 31 * checksum | mFileOp.lastModified(file);
        }
        return checksum;
    }

    /** Returns a visual representation of this object for debugging. */
    @Override
    public String toString() {
        String s = String.format("<DirInfo %1$s TS=%2$d", mDir, mDirModifiedTS);  //$NON-NLS-1$
        if (mPropsModifiedTS != 0) {
            s += String.format(" | Props TS=%1$d, Chksum=%2$s",                   //$NON-NLS-1$
                    mPropsModifiedTS, mPropsChecksum);
        }
        return s + ">";                                                           //$NON-NLS-1$
    }

    /**
     * Returns the hashCode of the underlying File object.
     * <p/>
     * When a {@link LocalDirInfo} is placed in a map, what matters is to use the underlying
     * File object as the key so {@link #hashCode()} and {@link #equals(Object)} both
     * return the properties of the underlying File object.
     *
     * @see File#hashCode()
     */
    @Override
    public int hashCode() {
        return mDir.hashCode();
    }

    /**
     * Checks equality of the underlying File object.
     * <p/>
     * When a {@link LocalDirInfo} is placed in a map, what matters is to use the underlying
     * File object as the key so {@link #hashCode()} and {@link #equals(Object)} both
     * return the properties of the underlying File object.
     *
     * @see File#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof File) {
            return mDir.equals(obj);
        } else if (obj instanceof LocalDirInfo) {
            return mDir.equals(((LocalDirInfo) obj).mDir);
        } else if (obj instanceof MapComparator) {
            return mDir.equals(((MapComparator) obj).mDir);
        }
        return false;
    }

    /**
     * Helper for Map.contains() to make sure we're comparing the inner directory File
     * object and not the outer wrapper itself.
     */
    public static class MapComparator {
        private final File mDir;

        public MapComparator(File dir) {
            mDir = dir;
        }

        /**
         * Returns the hashCode of the underlying File object.
         * <p/>
         * When a {@link LocalDirInfo} is placed in a map, what matters is to use the underlying
         * File object as the key so {@link #hashCode()} and {@link #equals(Object)} both
         * return the properties of the underlying File object.
         *
         * @see File#hashCode()
         */
        @Override
        public int hashCode() {
            return mDir.hashCode();
        }

        /**
         * Checks equality of the underlying File object.
         * <p/>
         * When a {@link LocalDirInfo} is placed in a map, what matters is to use the underlying
         * File object as the key so {@link #hashCode()} and {@link #equals(Object)} both
         * return the properties of the underlying File object.
         *
         * @see File#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof File) {
                return mDir.equals(obj);
            } else if (obj instanceof LocalDirInfo) {
                return mDir.equals(((LocalDirInfo) obj).mDir);
            } else if (obj instanceof MapComparator) {
                return mDir.equals(((MapComparator) obj).mDir);
            }
            return false;
        }
    }
}
