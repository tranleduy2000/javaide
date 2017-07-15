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
package org.apache.commons.compress.compressors.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;

/**
 * Implements the "gz" compression format as an input stream.
 * This classes wraps the standard java classes for working with gz. 
 */
public class GzipCompressorInputStream extends CompressorInputStream {
    /* reference to the compressed stream */
    private final GZIPInputStream in; 

    /**
     * Constructs a new GZip compressed input stream by the referenced
     * InputStream.
     * 
     * @param inputStream the InputStream from which this object should be created of
     * @throws IOException if the stream could not be created
     */
    public GzipCompressorInputStream(InputStream inputStream) throws IOException {
        in = new GZIPInputStream(inputStream);
    }

    /** {@inheritDoc} */
    public int read() throws IOException {
        int read = in.read();
        this.count(read < 0 ? -1 : 1);
        return read;
    }

    /**
     * {@inheritDoc}
     * 
     * @since Apache Commons Compress 1.1
     */
    public int read(byte[] b) throws IOException {
        int read = in.read(b);
        this.count(read);
        return read;
    }

    /**
     * {@inheritDoc}
     * 
     * @since Apache Commons Compress 1.1
     */
    public int read(byte[] b, int from, int length) throws IOException {
        int read = in.read(b, from, length);
        this.count(read);
        return read;
    }

    /**
     * Checks if the signature matches what is expected for a gzip file.
     * 
     * @param signature
     *            the bytes to check
     * @param length
     *            the number of bytes to check
     * @return true, if this stream is a gzipped compressed stream, false otherwise
     * 
     * @since Apache Commons Compress 1.1
     */
    public static boolean matches(byte[] signature, int length) {

        if (length < 2) {
            return false;
        }
        
        if (signature[0] != 31) {
            return false;
        }

        if (signature[1] != -117) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Closes the input stream (unless it is System.in).
     * 
     * @since 1.2
     */
    public void close() throws IOException {
        if (this.in != System.in) {
            this.in.close();
        }
    }
}
