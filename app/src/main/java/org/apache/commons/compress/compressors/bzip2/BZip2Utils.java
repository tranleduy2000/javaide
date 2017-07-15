/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.compressors.bzip2;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility code for the BZip2 compression format.
 * @ThreadSafe
 * @since Commons Compress 1.1
 */
public abstract class BZip2Utils {

    /**
     * Map from common filename suffixes of bzip2ed files to the corresponding
     * suffixes of uncompressed files. For example: from ".tbz2" to ".tar".
     * <p>
     * This map also contains bzip2-specific suffixes like ".bz2". These
     * suffixes are mapped to the empty string, as they should simply be
     * removed from the filename when the file is uncompressed.
     */
    private static final Map uncompressSuffix = new HashMap();

    static {
        uncompressSuffix.put(".tbz2", ".tar");
        uncompressSuffix.put(".tbz", ".tar");
        uncompressSuffix.put(".bz2", "");
        uncompressSuffix.put(".bz", "");
    }
    // N.B. if any shorter or longer keys are added, ensure the for loop limits are changed

    /** Private constructor to prevent instantiation of this utility class. */
    private BZip2Utils() {
    }

    /**
     * Detects common bzip2 suffixes in the given filename.
     *
     * @param filename name of a file
     * @return <code>true</code> if the filename has a common bzip2 suffix,
     *         <code>false</code> otherwise
     */
    public static boolean isCompressedFilename(String filename) {
        String lower = filename.toLowerCase(Locale.ENGLISH);
        int n = lower.length();
        // Shortest suffix is three letters (.bz), longest is five (.tbz2)
        for (int i = 3; i <= 5 && i < n; i++) {
            if (uncompressSuffix.containsKey(lower.substring(n - i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Maps the given name of a bzip2-compressed file to the name that the
     * file should have after uncompression. Commonly used file type specific
     * suffixes like ".tbz" or ".tbz2" are automatically detected and
     * correctly mapped. For example the name "package.tbz2" is mapped to
     * "package.tar". And any filenames with the generic ".bz2" suffix
     * (or any other generic bzip2 suffix) is mapped to a name without that
     * suffix. If no bzip2 suffix is detected, then the filename is returned
     * unmapped.
     *
     * @param filename name of a file
     * @return name of the corresponding uncompressed file
     */
    public static String getUncompressedFilename(String filename) {
        String lower = filename.toLowerCase(Locale.ENGLISH);
        int n = lower.length();
        // Shortest suffix is three letters (.bz), longest is five (.tbz2)
        for (int i = 3; i <= 5 && i < n; i++) {
            Object suffix = uncompressSuffix.get(lower.substring(n - i));
            if (suffix != null) {
                return filename.substring(0, n - i) + suffix;
            }
        }
        return filename;
    }

    /**
     * Maps the given filename to the name that the file should have after
     * compression with bzip2. Currently this method simply appends the suffix
     * ".bz2" to the filename based on the standard behaviour of the "bzip2"
     * program, but a future version may implement a more complex mapping if
     * a new widely used naming pattern emerges.
     *
     * @param filename name of a file
     * @return name of the corresponding compressed file
     */
    public static String getCompressedFilename(String filename) {
        return filename + ".bz2";
    }

}
