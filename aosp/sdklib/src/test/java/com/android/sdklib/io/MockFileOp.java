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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Mock version of {@link FileOp} that wraps some common {@link File}
 * operations on files and folders.
 * <p/>
 * This version does not perform any file operation. Instead it records a textual
 * representation of all the file operations performed.
 * <p/>
 * To avoid cross-platform path issues (e.g. Windows path), the methods here should
 * always use rooted (aka absolute) unix-looking paths, e.g. "/dir1/dir2/file3".
 * When processing {@link File}, you can convert them using {@link #getAgnosticAbsPath(File)}.
 */
public class MockFileOp implements IFileOp {

    private final Map<String, FileInfo> mExistingFiles = Maps.newTreeMap();
    private final Set<String> mExistingFolders = Sets.newTreeSet();
    private final List<StringOutputStream> mOutputStreams = new ArrayList<StringOutputStream>();

    public MockFileOp() {
    }

    /** Resets the internal state, as if the object had been newly created. */
    public void reset() {
        mExistingFiles.clear();
        mExistingFolders.clear();
        mOutputStreams.clear();
    }

    @NonNull
    public String getAgnosticAbsPath(@NonNull File file) {
        return getAgnosticAbsPath(file.getAbsolutePath());
    }

    @NonNull
    public String getAgnosticAbsPath(@NonNull String path) {
        if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
            // Try to convert the windows-looking path to a unix-looking one
            path = path.replace('\\', '/');
            path = path.replaceAll("^[A-Z]:", "");      //$NON-NLS-1$ //$NON-NLS-2$
        }
        return path;
    }

    /**
     * Records a new absolute file path.
     * Parent folders are not automatically created.
     */
    public void recordExistingFile(@NonNull File file) {
        recordExistingFile(getAgnosticAbsPath(file), 0, (byte[])null);
    }

    /**
     * Records a new absolute file path.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFilePath A unix-like file path, e.g. "/dir/file"
     */
    public void recordExistingFile(@NonNull String absFilePath) {
        recordExistingFile(absFilePath, 0, (byte[])null);
    }

    /**
     * Records a new absolute file path & its input stream content.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFilePath A unix-like file path, e.g. "/dir/file"
     * @param inputStream A non-null byte array of content to return
     *                    via {@link #newFileInputStream(File)}.
     */
    public void recordExistingFile(@NonNull String absFilePath, @Nullable byte[] inputStream) {
        recordExistingFile(absFilePath, 0, inputStream);
    }

    /**
     * Records a new absolute file path & its input stream content.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFilePath A unix-like file path, e.g. "/dir/file"
     * @param content A non-null UTF-8 content string to return
     *                    via {@link #newFileInputStream(File)}.
     */
    public void recordExistingFile(@NonNull String absFilePath, @NonNull String content) {
        recordExistingFile(absFilePath, 0, content.getBytes(Charsets.UTF_8));
    }

    /**
     * Records a new absolute file path & its input stream content.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFilePath A unix-like file path, e.g. "/dir/file"
     * @param inputStream A non-null byte array of content to return
     *                    via {@link #newFileInputStream(File)}.
     */
    public void recordExistingFile(@NonNull String absFilePath,
                                   long lastModified,
                                   @Nullable byte[] inputStream) {
        mExistingFiles.put(absFilePath, new FileInfo(lastModified, inputStream));
    }

    /**
     * Records a new absolute file path & its input stream content.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFilePath A unix-like file path, e.g. "/dir/file"
     * @param content A non-null UTF-8 content string to return
     *                    via {@link #newFileInputStream(File)}.
     */
    public void recordExistingFile(@NonNull String absFilePath,
                                   long lastModified,
                                   @NonNull String content) {
        recordExistingFile(absFilePath, lastModified, content.getBytes(Charsets.UTF_8));
    }

    /**
     * Records a new absolute folder path.
     * Parent folders are not automatically created.
     */
    public void recordExistingFolder(File folder) {
        mExistingFolders.add(getAgnosticAbsPath(folder));
    }

    /**
     * Records a new absolute folder path.
     * Parent folders are not automatically created.
     * <p/>
     * The syntax should always look "unix-like", e.g. "/dir/file".
     * On Windows that means you'll want to use {@link #getAgnosticAbsPath(File)}.
     * @param absFolderPath A unix-like folder path, e.g. "/dir/file"
     */
    public void recordExistingFolder(String absFolderPath) {
        mExistingFolders.add(absFolderPath);
    }

    /**
     * Returns true if a file with the given path has been recorded.
     */
    public boolean hasRecordedExistingFile(File file) {
        return mExistingFiles.containsKey(getAgnosticAbsPath(file));
    }

    /**
     * Returns true if a folder with the given path has been recorded.
     */
    public boolean hasRecordedExistingFolder(File folder) {
        return mExistingFolders.contains(getAgnosticAbsPath(folder));
    }

    /**
     * Returns the list of paths added using {@link #recordExistingFile(String)}
     * and eventually updated by {@link #delete(File)} operations.
     * <p/>
     * The returned list is sorted by alphabetic absolute path string.
     */
    @NonNull
    public String[] getExistingFiles() {
        Set<String> files = mExistingFiles.keySet();
        return files.toArray(new String[files.size()]);
    }

    /**
     * Returns the list of folder paths added using {@link #recordExistingFolder(String)}
     * and eventually updated {@link #delete(File)} or {@link #mkdirs(File)} operations.
     * <p/>
     * The returned list is sorted by alphabetic absolute path string.
     */
    @NonNull
    public String[] getExistingFolders() {
        return mExistingFolders.toArray(new String[mExistingFolders.size()]);
    }

    /**
     * Returns the {@link StringOutputStream#toString()} as an array, in creation order.
     * Array can be empty but not null.
     */
    @NonNull
    public String[] getOutputStreams() {
        int n = mOutputStreams.size();
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = mOutputStreams.get(i).toString();
        }
        return result;
    }

    /**
     * Helper to delete a file or a directory.
     * For a directory, recursively deletes all of its content.
     * Files that cannot be deleted right away are marked for deletion on exit.
     * The argument can be null.
     */
    @Override
    public void deleteFileOrFolder(@NonNull File fileOrFolder) {
        if (fileOrFolder != null) {
            if (isDirectory(fileOrFolder)) {
                // Must delete content recursively first
                for (File item : listFiles(fileOrFolder)) {
                    deleteFileOrFolder(item);
                }
            }
            delete(fileOrFolder);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <em>Note: this mock version does nothing.</em>
     */
    @Override
    public void setExecutablePermission(@NonNull File file) throws IOException {
        // pass
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <em>Note: this mock version does nothing.</em>
     */
    @Override
    public void setReadOnly(@NonNull File file) {
        // pass
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <em>Note: this mock version does nothing.</em>
     */
    @Override
    public void copyFile(@NonNull File source, @NonNull File dest) throws IOException {
        // pass
        throw new UnsupportedOperationException("MockFileUtils.copyFile is not supported."); //$NON-NLS-1$
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
        String path1 = getAgnosticAbsPath(file1);
        String path2 = getAgnosticAbsPath(file2);
        FileInfo fi1 = mExistingFiles.get(path1);
        FileInfo fi2 = mExistingFiles.get(path2);

        if (fi1 == null) {
            throw new FileNotFoundException("[isSameFile] Mock file not defined: " + path1);
        }

        if (fi1 == fi2) {
            return true;
        }

        if (fi2 == null) {
            throw new FileNotFoundException("[isSameFile] Mock file not defined: " + path2);
        }

        byte[] content1 = fi1.getContent();
        byte[] content2 = fi2.getContent();

        if (content1 == null) {
            throw new IOException("[isSameFile] Mock file has no content: " + path1);
        }
        if (content2 == null) {
            throw new IOException("[isSameFile] Mock file has no content: " + path2);
        }

        return Arrays.equals(content1, content2);
    }

    /** Invokes {@link File#isFile()} on the given {@code file}. */
    @Override
    public boolean isFile(@NonNull File file) {
        String path = getAgnosticAbsPath(file);
        return mExistingFiles.containsKey(path);
    }

    /** Invokes {@link File#isDirectory()} on the given {@code file}. */
    @Override
    public boolean isDirectory(@NonNull File file) {
        String path = getAgnosticAbsPath(file);
        if (mExistingFolders.contains(path)) {
            return true;
        }

        // If we defined a file or folder as a child of the requested file path,
        // then the directory exists implicitely.
        Pattern pathRE = Pattern.compile(
                Pattern.quote(path + (path.endsWith("/") ? "" : '/')) +  //$NON-NLS-1$ //$NON-NLS-2$
                ".*");                                                   //$NON-NLS-1$

        for (String folder : mExistingFolders) {
            if (pathRE.matcher(folder).matches()) {
                return true;
            }
        }
        for (String filePath : mExistingFiles.keySet()) {
            if (pathRE.matcher(filePath).matches()) {
                return true;
            }
        }

        return false;
    }

    /** Invokes {@link File#exists()} on the given {@code file}. */
    @Override
    public boolean exists(@NonNull File file) {
        return isFile(file) || isDirectory(file);
    }

    /** Invokes {@link File#length()} on the given {@code file}. */
    @Override
    public long length(@NonNull File file) {
        throw new UnsupportedOperationException("MockFileUtils.length is not supported."); //$NON-NLS-1$
    }

    @Override
    public boolean delete(@NonNull File file) {
        String path = getAgnosticAbsPath(file);

        if (mExistingFiles.remove(path) != null) {
            return true;
        }

        boolean hasSubfiles = false;
        for (String folder : mExistingFolders) {
            if (folder.startsWith(path) && !folder.equals(path)) {
                // the File.delete operation is not recursive and would fail to remove
                // a root dir that is not empty.
                return false;
            }
        }
        if (!hasSubfiles) {
            for (String filePath : mExistingFiles.keySet()) {
                if (filePath.startsWith(path) && !filePath.equals(path)) {
                    // the File.delete operation is not recursive and would fail to remove
                    // a root dir that is not empty.
                    return false;
                }
            }
        }

        return mExistingFolders.remove(path);
    }

    /** Invokes {@link File#mkdirs()} on the given {@code file}. */
    @Override
    public boolean mkdirs(@NonNull File file) {
        for (; file != null; file = file.getParentFile()) {
            String path = getAgnosticAbsPath(file);
            mExistingFolders.add(path);
        }
        return true;
    }

    /**
     * Invokes {@link File#listFiles()} on the given {@code file}.
     * The returned list is sorted by alphabetic absolute path string.
     * Might return an empty array but never null.
     */
    @NonNull
    @Override
    public File[] listFiles(@NonNull File file) {
        TreeSet<File> files = new TreeSet<File>();

        String path = getAgnosticAbsPath(file);
        Pattern pathRE = Pattern.compile(
                Pattern.quote(path + (path.endsWith("/") ? "" : '/')) +  //$NON-NLS-1$ //$NON-NLS-2$
                ".*");                                                   //$NON-NLS-1$

        for (String folder : mExistingFolders) {
            if (pathRE.matcher(folder).matches()) {
                files.add(new File(folder));
            }
        }
        for (String filePath : mExistingFiles.keySet()) {
            if (pathRE.matcher(filePath).matches()) {
                files.add(new File(filePath));
            }
        }
        return files.toArray(new File[files.size()]);
    }

    /** Invokes {@link File#renameTo(File)} on the given files. */
    @Override
    public boolean renameTo(@NonNull File oldFile, @NonNull File newFile) {
        boolean renamed = false;

        String oldPath = getAgnosticAbsPath(oldFile);
        String newPath = getAgnosticAbsPath(newFile);
        Pattern pathRE = Pattern.compile(
                "^(" + Pattern.quote(oldPath) + //$NON-NLS-1$
                ")($|/.*)");                    //$NON-NLS-1$

        Set<String> newFolders = Sets.newTreeSet();
        for (Iterator<String> it = mExistingFolders.iterator(); it.hasNext(); ) {
            String folder = it.next();
            Matcher m = pathRE.matcher(folder);
            if (m.matches()) {
                it.remove();
                String newFolder = newPath + m.group(2);
                newFolders.add(newFolder);
                renamed = true;
            }
        }
        mExistingFolders.addAll(newFolders);
        newFolders.clear();

        Map<String, FileInfo> newFiles = Maps.newTreeMap();
        for (Iterator<Entry<String, FileInfo>> it = mExistingFiles.entrySet().iterator();
                it.hasNext(); ) {
            Entry<String, FileInfo> entry = it.next();
            String filePath = entry.getKey();
            Matcher m = pathRE.matcher(filePath);
            if (m.matches()) {
                it.remove();
                String newFilePath = newPath + m.group(2);
                newFiles.put(newFilePath, entry.getValue());
                renamed = true;
            }
        }
        mExistingFiles.putAll(newFiles);

        return renamed;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <em>TODO: we might want to overload this to read mock properties instead of a real file.</em>
     */
    @NonNull
    @Override
    public Properties loadProperties(@NonNull File file) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            props.load(fis);
        } catch (IOException ignore) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ignore) {}
            }
        }
        return props;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <em>Note that this uses the mock version of {@link #newFileOutputStream(File)} and thus
     * records the write rather than actually performing it.</em>
     */
    @Override
    public void saveProperties(
            @NonNull File file,
            @NonNull Properties props,
            @NonNull String comments) throws IOException {
        OutputStream fos = null;
        try {
            fos = newFileOutputStream(file);
            props.store(fos, comments);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Returns an OutputStream that will capture the bytes written and associate
     * them with the given file.
     */
    @NonNull
    @Override
    public OutputStream newFileOutputStream(@NonNull File file) throws FileNotFoundException {
        StringOutputStream os = new StringOutputStream(file);
        mOutputStreams.add(os);
        return os;
    }

    /**
     * An {@link OutputStream} that will capture the stream as an UTF-8 string once properly closed
     * and associate it to the given {@link File}.
     */
    public class StringOutputStream extends ByteArrayOutputStream {
        private String mData;
        private final File mFile;

        public StringOutputStream(File file) {
            mFile = file;
            recordExistingFile(file);
        }

        public File getFile() {
            return mFile;
        }

        /** Can be null if the stream has never been properly closed. */
        public String getData() {
            return mData;
        }

        /** Once the stream is properly closed, convert the byte array to an UTF-8 string */
        @Override
        public void close() throws IOException {
            super.close();
            mData = new String(toByteArray(), "UTF-8");                         //$NON-NLS-1$
        }

        /** Returns a string representation suitable for unit tests validation. */
        @Override
        public synchronized String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('<').append(getAgnosticAbsPath(mFile)).append(": ");      //$NON-NLS-1$
            if (mData == null) {
                sb.append("(stream not closed properly)>");                     //$NON-NLS-1$
            } else {
                sb.append('\'').append(mData).append("'>");                     //$NON-NLS-1$
            }
            return sb.toString();
        }
    }

    @NonNull
    @Override
    public InputStream newFileInputStream(@NonNull File file) throws FileNotFoundException {
        FileInfo fi = mExistingFiles.get(getAgnosticAbsPath(file));
        if (fi != null) {
            byte[] content = fi.getContent();
            if (content != null) {
                return new ByteArrayInputStream(content);
            }
        }
        throw new FileNotFoundException("Mock file has no content: " + getAgnosticAbsPath(file));
    }

    @Override
    public long lastModified(@NonNull File file) {
        FileInfo fi = mExistingFiles.get(getAgnosticAbsPath(file));
        if (fi != null) {
            return fi.getLastModified();
        }
        return 0;
    }

    // -----

    private static class FileInfo {
        private long mLastModified;
        private byte[] mContent;

        public FileInfo(long lastModified, @Nullable byte[] content) {
            mLastModified = lastModified;
            mContent = content;
        }

        public long getLastModified() {
            return mLastModified;
        }

        @Nullable
        public byte[] getContent() {
            return mContent;
        }

    }
}
