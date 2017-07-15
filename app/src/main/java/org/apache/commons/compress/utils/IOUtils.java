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
package org.apache.commons.compress.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility functions
 * @Immutable
 */
public final class IOUtils {

    /** Private constructor to prevent instantiation of this utility class. */
    private IOUtils(){
    }

    /**
     * Copies the content of a InputStream into an OutputStream.
     * Uses a default buffer size of 8024 bytes.
     *
     * @param input
     *            the InputStream to copy
     * @param output
     *            the target Stream
     * @throws IOException
     *             if an error occurs
     */
    public static long copy(final InputStream input, final OutputStream output) throws IOException {
        return copy(input, output, 8024);
    }

    /**
     * Copies the content of a InputStream into an OutputStream
     *
     * @param input
     *            the InputStream to copy
     * @param output
     *            the target Stream
     * @param buffersize
     *            the buffer size to use
     * @throws IOException
     *             if an error occurs
     */
    public static long copy(final InputStream input, final OutputStream output, int buffersize) throws IOException {
        final byte[] buffer = new byte[buffersize];
        int n = 0;
        long count=0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
