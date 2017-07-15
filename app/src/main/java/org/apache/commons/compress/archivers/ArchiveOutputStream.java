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
package org.apache.commons.compress.archivers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Archive output stream implementations are expected to override the
 * {@link #write(byte[], int, int)} method to improve performance.
 * They should also override {@link #close()} to ensure that any necessary
 * trailers are added.
 * 
 * <p>
 * The normal sequence of calls for working with ArchiveOutputStreams is:
 * + create ArchiveOutputStream object
 * + write SFX header (optional, Zip only)
 * + repeat as needed:
 *      - putArchiveEntry() (writes entry header)
 *      - write() (writes entry data)
 *      - closeArchiveEntry() (closes entry)
 * + finish() (ends the addition of entries)
 * + write additional data if format supports it (optional)
 * + close()
 * </p>
 * 
 * <p>
 * Example usage:<br/>
 * TBA
 * </p>
 */
public abstract class ArchiveOutputStream extends OutputStream {
    
    /** Temporary buffer used for the {@link #write(int)} method */
    private final byte[] oneByte = new byte[1];
    static final int BYTE_MASK = 0xFF;

    /** holds the number of bytes written to this stream */
    private long bytesWritten = 0;
    // Methods specific to ArchiveOutputStream
    
    /**
     * Writes the headers for an archive entry to the output stream.
     * The caller must then write the content to the stream and call
     * {@link #closeArchiveEntry()} to complete the process.
     * 
     * @param entry describes the entry
     * @throws IOException
     */
    public abstract void putArchiveEntry(ArchiveEntry entry) throws IOException;

    /**
     * Closes the archive entry, writing any trailer information that may
     * be required.
     * @throws IOException
     */
    public abstract void closeArchiveEntry() throws IOException;
    
    /**
     * Finishes the addition of entries to this stream, without closing it.
     * Additional data can be written, if the format supports it.
     * 
     * The finish() method throws an Exception if the user forgets to close the entry
     * .
     * @throws IOException
     */
    public abstract void finish() throws IOException;

    /**
     * Create an archive entry using the inputFile and entryName provided.
     * 
     * @param inputFile
     * @param entryName 
     * @return the ArchiveEntry set up with details from the file
     * 
     * @throws IOException
     */
    public abstract ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException;
    
    // Generic implementations of OutputStream methods that may be useful to sub-classes
    
    /**
     * Writes a byte to the current archive entry.
     *
     * This method simply calls write( byte[], 0, 1 ).
     *
     * MUST be overridden if the {@link #write(byte[], int, int)} method
     * is not overridden; may be overridden otherwise.
     * 
     * @param b The byte to be written.
     * @throws IOException on error
     */
    public void write(int b) throws IOException {
        oneByte[0] = (byte) (b & BYTE_MASK);
        write(oneByte, 0, 1);
    }

    /**
     * Increments the counter of already written bytes.
     * Doesn't increment if the EOF has been hit (read == -1)
     * 
     * @param written the number of bytes written
     */
    protected void count(int written) {
        count((long) written);
    }

    /**
     * Increments the counter of already written bytes.
     * Doesn't increment if the EOF has been hit (read == -1)
     * 
     * @param written the number of bytes written
     * @since Apache Commons Compress 1.1
     */
    protected void count(long written) {
        if (written != -1) {
            bytesWritten = bytesWritten + written;
        }
    }
    
    /**
     * Returns the current number of bytes written to this stream.
     * @return the number of written bytes
     * @deprecated this method may yield wrong results for large
     * archives, use #getBytesWritten instead
     */
    public int getCount() {
        return (int) bytesWritten;
    }

    /**
     * Returns the current number of bytes written to this stream.
     * @return the number of written bytes
     * @since Apache Commons Compress 1.1
     */
    public long getBytesWritten() {
        return bytesWritten;
    }

    /**
     * Whether this stream is able to write the given entry.
     *
     * <p>Some archive formats support variants or details that are
     * not supported (yet).</p>
     *
     * <p>This implementation always returns true.
     * @since Apache Commons Compress 1.1
     */
    public boolean canWriteEntryData(ArchiveEntry ae) {
        return true;
    }
}
