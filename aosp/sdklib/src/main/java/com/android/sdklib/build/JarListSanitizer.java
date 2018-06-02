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

package com.android.sdklib.build;

import com.android.SdkConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Class to handle a list of jar files, finding and removing duplicates.
 *
 * Right now duplicates are based on:
 * - same filename
 * - same length
 * - same content: using sha1 comparison.
 *
 * The length/sha1 are kept in a cache and only updated if the library is changed.
 */
public class JarListSanitizer {

    private static final byte[] sBuffer = new byte[4096];
    private static final String CACHE_FILENAME = "jarlist.cache";
    private static final Pattern READ_PATTERN = Pattern.compile("^(\\d+) (\\d+) ([0-9a-f]+) (.+)$");

    /**
     * Simple class holding the data regarding a jar dependency.
     *
     */
    private static final class JarEntity {
        private final File mFile;
        private final long mLastModified;
        private long mLength;
        private String mSha1;

        /**
         * Creates an entity from cached data.
         * @param path the file path
         * @param lastModified when it was last modified
         * @param length its length
         * @param sha1 its sha1
         */
        private JarEntity(String path, long lastModified, long length, String sha1) {
            mFile = new File(path);
            mLastModified = lastModified;
            mLength = length;
            mSha1 = sha1;
        }

        /**
         * Creates an entity from a {@link File}.
         * @param file the file.
         */
        private JarEntity(File file) {
            mFile = file;
            mLastModified = file.lastModified();
            mLength = file.length();
        }

        /**
         * Checks whether the {@link File#lastModified()} matches the cached value. If not, length
         * is updated and the sha1 is reset (but not recomputed, this is done on demand).
         * @return return whether the file was changed.
         */
        private boolean checkValidity() {
            if (mLastModified != mFile.lastModified()) {
                mLength = mFile.length();
                mSha1 = null;
                return true;
            }

            return false;
        }

        private File getFile() {
            return mFile;
        }

        private long getLastModified() {
            return mLastModified;
        }

        private long getLength() {
            return mLength;
        }

        /**
         * Returns the file's sha1, computing it if necessary.
         * @return the sha1
         * @throws Sha1Exception
         */
        private String getSha1() throws Sha1Exception {
            if (mSha1 == null) {
                mSha1 = JarListSanitizer.getSha1(mFile);
            }
            return mSha1;
        }

        private boolean hasSha1() {
            return mSha1 != null;
        }
    }

    /**
     * Exception used to indicate the sanitized list of jar dependency cannot be computed due
     * to inconsistency in duplicate jar files.
     */
    public static final class DifferentLibException extends Exception {
        private static final long serialVersionUID = 1L;
        private final String[] mDetails;

        public DifferentLibException(String message, String[] details) {
            super(message);
            mDetails = details;
        }

        public String[] getDetails() {
            return mDetails;
        }
    }

    /**
     * Exception to indicate a failure to check a jar file's content.
     */
    public static final class Sha1Exception extends Exception {
        private static final long serialVersionUID = 1L;
        private final File mJarFile;

        public Sha1Exception(File jarFile, Throwable cause) {
            super(cause);
            mJarFile = jarFile;
        }

        public File getJarFile() {
            return mJarFile;
        }
    }

    private final File mOut;
    private final PrintStream mOutStream;

    /**
     * Creates a sanitizer.
     * @param out the project output where the cache is to be stored.
     */
    public JarListSanitizer(File out) {
        mOut = out;
        mOutStream = System.out;
    }

    public JarListSanitizer(File out, PrintStream outStream) {
        mOut = out;
        mOutStream = outStream;
    }

    /**
     * Sanitize a given list of files
     * @param files the list to sanitize
     * @return a new list containing no duplicates.
     * @throws DifferentLibException
     * @throws Sha1Exception
     */
    public List<File> sanitize(Collection<File> files) throws DifferentLibException, Sha1Exception {
        List<File> results = new ArrayList<File>();

        // get the cache list.
        Map<String, JarEntity> jarList = getCachedJarList();

        boolean updateJarList = false;

        // clean it up of removed files.
        // use results as a temp storage to store the files to remove as we go through the map.
        for (JarEntity entity : jarList.values()) {
            if (entity.getFile().exists() == false) {
                results.add(entity.getFile());
            }
        }

        // the actual clean up.
        if (!results.isEmpty()) {
            for (File f : results) {
                jarList.remove(f.getAbsolutePath());
            }

            results.clear();
            updateJarList = true;
        }

        Map<String, List<JarEntity>> nameMap = new HashMap<String, List<JarEntity>>();

        // update the current jar list if needed, while building a secondary map based on
        // filename only.
        for (File file : files) {
            String path = file.getAbsolutePath();
            JarEntity entity = jarList.get(path);

            if (entity == null) {
                entity = new JarEntity(file);
                jarList.put(path, entity);
                updateJarList = true;
            } else {
                updateJarList |= entity.checkValidity();
            }

            String filename = file.getName();
            List<JarEntity> nameList = nameMap.get(filename);
            if (nameList == null) {
                nameList = new ArrayList<JarEntity>();
                nameMap.put(filename, nameList);
            }
            nameList.add(entity);
        }

        try {
            // now look for duplicates. Each name list can have more than one file but they must
            // have the same size/sha1
            for (Entry<String, List<JarEntity>> entry : nameMap.entrySet()) {
                List<JarEntity> list = entry.getValue();
                checkEntities(entry.getKey(), list);

                // if we are here, there's no issue. Add the first of the list to the results.
                results.add(list.get(0).getFile());
            }

            // special case for android-support-v4/13
            checkSupportLibs(nameMap, results);
        } finally {
            if (updateJarList) {
                writeJarList(nameMap);
            }
        }

        return results;
    }

    /**
     * Checks whether a given list of duplicates can be replaced by a single one.
     * @param filename the filename of the files
     * @param list the list of dup files
     * @throws DifferentLibException
     * @throws Sha1Exception
     */
    private void checkEntities(String filename, List<JarEntity> list)
            throws DifferentLibException, Sha1Exception {
        if (list.size() == 1) {
            return;
        }

        JarEntity baseEntity = list.get(0);
        long baseLength = baseEntity.getLength();
        String baseSha1 = baseEntity.getSha1();

        final int count = list.size();
        for (int i = 1; i < count ; i++) {
            JarEntity entity = list.get(i);
            if (entity.getLength() != baseLength || entity.getSha1().equals(baseSha1) == false) {
                throw new DifferentLibException("Jar mismatch! Fix your dependencies",
                        getEntityDetails(filename, list));
            }

        }
    }

    /**
     * Checks for present of both support libraries in v4 and v13. If both are detected,
     * v4 is removed from <var>results</var>
     * @param nameMap the list of jar as a map of (filename, list of files).
     * @param results the current list of jar file set to be used. it's already been cleaned of
     *           duplicates.
     */
    private void checkSupportLibs(Map<String, List<JarEntity>> nameMap, List<File> results) {
        List<JarEntity> v4 = nameMap.get("android-support-v4.jar");
        List<JarEntity> v13 = nameMap.get("android-support-v13.jar");

        if (v13 != null && v4 != null) {
            mOutStream.println("WARNING: Found both android-support-v4 and android-support-v13 in the dependency list.");
            mOutStream.println("Because v13 includes v4, using only v13.");
            results.remove(v4.get(0).getFile());
        }
    }

    private Map<String, JarEntity> getCachedJarList() {
        Map<String, JarEntity> cache = new HashMap<String, JarListSanitizer.JarEntity>();

        File cacheFile = new File(mOut, CACHE_FILENAME);
        if (cacheFile.exists() == false) {
            return cache;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile),
                    SdkConstants.UTF_8));

            String line = null;
            while ((line = reader.readLine()) != null) {
                // skip comments
                if (line.charAt(0) == '#') {
                    continue;
                }

                // get the data with a regexp
                Matcher m = READ_PATTERN.matcher(line);
                if (m.matches()) {
                    String path = m.group(4);

                    JarEntity entity = new JarEntity(
                            path,
                            Long.parseLong(m.group(1)),
                            Long.parseLong(m.group(2)),
                            m.group(3));

                    cache.put(path, entity);
                }
            }

        } catch (FileNotFoundException e) {
            // won't happen, we check up front.
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen, but if it does, we just won't have a cache.
        } catch (IOException e) {
            // shouldn't happen, but if it does, we just won't have a cache.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return cache;
    }

    private void writeJarList(Map<String, List<JarEntity>> nameMap) {
        File cacheFile = new File(mOut, CACHE_FILENAME);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(
                    new FileOutputStream(cacheFile), SdkConstants.UTF_8);

            writer.write("# cache for current jar dependency. DO NOT EDIT.\n");
            writer.write("# format is <lastModified> <length> <SHA-1> <path>\n");
            writer.write("# Encoding is UTF-8\n");

            for (List<JarEntity> list : nameMap.values()) {
                // clean up the list of files that don't have a sha1.
                for (int i = 0 ; i < list.size() ; ) {
                    JarEntity entity = list.get(i);
                    if (entity.hasSha1()) {
                        i++;
                    } else {
                        list.remove(i);
                    }
                }

                if (list.size() > 1) {
                    for (JarEntity entity : list) {
                        writer.write(String.format("%d %d %s %s\n",
                                entity.getLastModified(),
                                entity.getLength(),
                                entity.getSha1(),
                                entity.getFile().getAbsolutePath()));
                    }
                }
            }
        } catch (IOException e) {
            mOutStream.println("WARNING: unable to write jarlist cache file " +
                    cacheFile.getAbsolutePath());
        } catch (Sha1Exception e) {
            // shouldn't happen here since we check that the sha1 is present first, meaning it's
            // already been computing.
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String[] getEntityDetails(String filename, List<JarEntity> list) throws Sha1Exception {
        ArrayList<String> result = new ArrayList<String>();
        result.add(
                String.format("Found %d versions of %s in the dependency list,",
                        list.size(), filename));
        result.add("but not all the versions are identical (check is based on SHA-1 only at this time).");
        result.add("All versions of the libraries must be the same at this time.");
        result.add("Versions found are:");
        for (JarEntity entity : list) {
            result.add("Path: " + entity.getFile().getAbsolutePath());
            result.add("\tLength: " + entity.getLength());
            result.add("\tSHA-1: " + entity.getSha1());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Computes the sha1 of a file and returns it.
     * @param f the file to compute the sha1 for.
     * @return the sha1 value
     * @throws Sha1Exception if the sha1 value cannot be computed.
     */
    private static String getSha1(File f) throws Sha1Exception {
        synchronized (sBuffer) {
            FileInputStream fis = null;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");

                fis = new FileInputStream(f);
                while (true) {
                    int length = fis.read(sBuffer);
                    if (length > 0) {
                        md.update(sBuffer, 0, length);
                    } else {
                        break;
                    }
                }

                return byteArray2Hex(md.digest());

            } catch (Exception e) {
                throw new Sha1Exception(f, e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        try {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } finally {
            formatter.close();
        }
    }
}
