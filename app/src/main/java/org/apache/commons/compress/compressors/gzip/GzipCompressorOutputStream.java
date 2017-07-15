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
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.CompressorOutputStream;

public class GzipCompressorOutputStream extends CompressorOutputStream {

    private final GZIPOutputStream out;

    public GzipCompressorOutputStream( final OutputStream outputStream ) throws IOException {
        out = new GZIPOutputStream(outputStream);
    }

    /** {@inheritDoc} */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @since Apache Commons Compress 1.1
     */
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @since Apache Commons Compress 1.1
     */
    public void write(byte[] b, int from, int length) throws IOException {
        out.write(b, from, length);
    }

    public void close() throws IOException {
        out.close();
    }

}
