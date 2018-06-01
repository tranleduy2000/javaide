/**
 * Copyright 2012, Red Hat Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duy.android.compiler.repo.maven.cache;


import com.duy.android.compiler.repo.maven.SignatureUtils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.security.MessageDigest;

/**
 * A class representing a download cache
 *
 * @author Mickael Istria (Red Hat Inc)
 */
public class DownloadCache {

    private final File basedir;
    private final FileIndex index;

    public DownloadCache(File cacheDirectory) {
        DownloadCache.createIfNeeded(cacheDirectory);
        this.index = new FileBackedIndex(cacheDirectory);
        this.basedir = cacheDirectory;
    }

    private static void createIfNeeded(final File basedir) {
        if (!basedir.exists()) {
            basedir.mkdirs();
        } else if (!basedir.isDirectory()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot use %s as cache directory: file is already exist",
                            basedir
                    )
            );
        }
    }

    private String getEntry(URI uri, String md5, String sha1, String sha512) throws Exception {
        if (!this.index.contains(uri)) {
            return null;
        }
        final String res = this.index.get(uri);
        File resFile = new File(this.basedir, res);
        if (!resFile.isFile()) {
            return null;
        }
        if (md5 != null && !md5.equals(SignatureUtils.getMD5(resFile))) {
            return null;
        }
        if (sha1 != null && !sha1.equals(SignatureUtils.getSHA1(resFile))) {
            return null;
        }
        if (sha512 != null && !sha512.equals(SignatureUtils.getSHA512(resFile))) {
            return null;
        }
        return res;
    }

    /**
     * Get a File in the download cache. If no cache for this URL, or
     * if expected signatures don't match cached ones, returns null.
     * available in cache,
     *
     * @param uri  URL of the file
     * @param md5  MD5 signature to verify file. Can be null =&gt; No check
     * @param sha1 Sha1 signature to verify file. Can be null =&gt; No check
     * @return A File when cache is found, null if no available cache
     */
    public File getArtifact(URI uri, String md5, String sha1, String sha512) throws Exception {
        String res = getEntry(uri, md5, sha1, sha512);
        if (res != null) {
            return new File(this.basedir, res);
        }
        return null;
    }

    public void install(URI uri, File outputFile, String md5, String sha1, String sha512) throws Exception {
        if (md5 == null) {
            md5 = SignatureUtils.computeSignatureAsString(outputFile, MessageDigest.getInstance("MD5"));
        }
        if (sha1 == null) {
            sha1 = SignatureUtils.computeSignatureAsString(outputFile, MessageDigest.getInstance("SHA1"));
        }
        if (sha512 == null) {
            sha512 = SignatureUtils.computeSignatureAsString(outputFile, MessageDigest.getInstance("SHA-512"));
        }
        String entry = getEntry(uri, md5, sha1, sha512);
        if (entry != null) {
            return; // entry already here
        }
        String fileName = outputFile.getName() + '_' + DigestUtils.md5Hex(uri.toString());
//		IOUtils.copy(outputFile.toPath(), new File(this.basedir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
        FileOutputStream output = new FileOutputStream(new File(this.basedir, fileName));
        FileInputStream input = new FileInputStream(outputFile);
        org.apache.commons.io.IOUtils.copy(input, output);
        output.close();
        input.close();
        // update index
        this.index.put(uri, fileName);
    }
}
