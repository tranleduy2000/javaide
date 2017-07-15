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
package org.apache.commons.compress.compressors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * <p>Factory to create Compressor[In|Out]putStreams from names. To add other
 * implementations you should extend CompressorStreamFactory and override the
 * appropriate methods (and call their implementation from super of course).</p>
 * 
 * Example (Compressing a file):
 * 
 * <pre>
 * final OutputStream out = new FileOutputStream(output); 
 * CompressorOutputStream cos = 
 *      new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.BZIP2, out);
 * IOUtils.copy(new FileInputStream(input), cos);
 * cos.close();
 * </pre>    
 * 
 * Example (Compressing a file):
 * <pre>
 * final InputStream is = new FileInputStream(input); 
 * CompressorInputStream in = 
 *      new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BZIP2, is);
 * IOUtils.copy(in, new FileOutputStream(output));
 * in.close();
 * </pre>
 * 
 * @Immutable
 */
public class CompressorStreamFactory {

    /**
     * Constant used to identify the BZIP2 compression algorithm.
     * @since Commons Compress 1.1
     */
    public static final String BZIP2 = "bzip2";
    /**
     * Constant used to identify the GZIP compression algorithm.
     * @since Commons Compress 1.1
     */
    public static final String GZIP = "gz";

    /**
     * Create an compressor input stream from an input stream, autodetecting
     * the compressor type from the first few bytes of the stream. The InputStream
     * must support marks, like BufferedInputStream.
     * 
     * @param in the input stream
     * @return the compressor input stream
     * @throws CompressorException if the compressor name is not known
     * @throws IllegalArgumentException if the stream is null or does not support mark
     * @since Commons Compress 1.1
     */
    public CompressorInputStream createCompressorInputStream(final InputStream in)
            throws CompressorException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        if (!in.markSupported()) {
            throw new IllegalArgumentException("Mark is not supported.");
        }

        final byte[] signature = new byte[12];
        in.mark(signature.length);
        try {
            int signatureLength = in.read(signature);
            in.reset();
            
            if (BZip2CompressorInputStream.matches(signature, signatureLength)) {
                return new BZip2CompressorInputStream(in);
            }
            
            if (GzipCompressorInputStream.matches(signature, signatureLength)) {
                return new GzipCompressorInputStream(in);
            }

        } catch (IOException e) {
            throw new CompressorException("Failed to detect Compressor from InputStream.", e);
        }

        throw new CompressorException("No Compressor found for the stream signature.");
    }
    
    /**
     * Create a compressor input stream from a compressor name and an input stream.
     * 
     * @param name of the compressor, i.e. "gz" or "bzip2"
     * @param in the input stream
     * @return compressor input stream
     * @throws CompressorException if the compressor name is not known
     * @throws IllegalArgumentException if the name or input stream is null
     */
    public CompressorInputStream createCompressorInputStream(final String name,
            final InputStream in) throws CompressorException {
        if (name == null || in == null) {
            throw new IllegalArgumentException(
                    "Compressor name and stream must not be null.");
        }

        try {
            
            if (GZIP.equalsIgnoreCase(name)) {
                return new GzipCompressorInputStream(in);
            }
            
            if (BZIP2.equalsIgnoreCase(name)) {
                return new BZip2CompressorInputStream(in);
            }
            
        } catch (IOException e) {
            throw new CompressorException(
                    "Could not create CompressorInputStream.", e);
        }
        throw new CompressorException("Compressor: " + name + " not found.");
    }

    /**
     * Create an compressor output stream from an compressor name and an input stream.
     * 
     * @param name the compressor name, i.e. "gz" or "bzip2"
     * @param out the output stream
     * @return the compressor output stream
     * @throws CompressorException if the archiver name is not known
     * @throws IllegalArgumentException if the archiver name or stream is null
     */
    public CompressorOutputStream createCompressorOutputStream(
            final String name, final OutputStream out)
            throws CompressorException {
        if (name == null || out == null) {
            throw new IllegalArgumentException(
                    "Compressor name and stream must not be null.");
        }

        try {

            if (GZIP.equalsIgnoreCase(name)) {
                return new GzipCompressorOutputStream(out);
            }
            
            if (BZIP2.equalsIgnoreCase(name)) {
                return new BZip2CompressorOutputStream(out);
            }
        
        } catch (IOException e) {
            throw new CompressorException(
                    "Could not create CompressorOutputStream", e);
        }
        throw new CompressorException("Compressor: " + name + " not found.");
    }
}
