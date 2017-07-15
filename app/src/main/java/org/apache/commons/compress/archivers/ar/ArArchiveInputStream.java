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
package org.apache.commons.compress.archivers.ar;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.utils.ArchiveUtils;

/**
 * Implements the "ar" archive format as an input stream.
 * 
 * @NotThreadSafe
 * 
 */
public class ArArchiveInputStream extends ArchiveInputStream {

    private final InputStream input;
    private long offset = 0;
    private boolean closed;

    /*
     * If getNextEnxtry has been called, the entry metadata is stored in
     * currentEntry.
     */
    private ArArchiveEntry currentEntry = null;

    // Storage area for extra long names (GNU ar)
    private byte[] namebuffer = null;

    /*
     * The offset where the current entry started. -1 if no entry has been
     * called
     */
    private long entryOffset = -1;

    /**
     * Constructs an Ar input stream with the referenced stream
     * 
     * @param pInput
     *            the ar input stream
     */
    public ArArchiveInputStream(final InputStream pInput) {
        input = pInput;
        closed = false;
    }

    /**
     * Returns the next AR entry in this stream.
     * 
     * @return the next AR entry.
     * @throws IOException
     *             if the entry could not be read
     */
    public ArArchiveEntry getNextArEntry() throws IOException {
        if (currentEntry != null) {
            final long entryEnd = entryOffset + currentEntry.getLength();
            while (offset < entryEnd) {
                int x = read();
                if (x == -1) {
                    // hit EOF before previous entry was complete
                    // TODO: throw an exception instead?
                    return null;
                }
            }
            currentEntry = null;
        }

        if (offset == 0) {
            final byte[] expected = ArchiveUtils.toAsciiBytes(ArArchiveEntry.HEADER);
            final byte[] realized = new byte[expected.length];
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read header. Occured at byte: " + getBytesRead());
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid header " + ArchiveUtils.toAsciiString(realized));
                }
            }
        }

        if (offset % 2 != 0 && read() < 0) {
            // hit eof
            return null;
        }

        if (input.available() == 0) {
            return null;
        }

        final byte[] name = new byte[16];
        final byte[] lastmodified = new byte[12];
        final byte[] userid = new byte[6];
        final byte[] groupid = new byte[6];
        final byte[] filemode = new byte[8];
        final byte[] length = new byte[10];

        read(name);
        read(lastmodified);
        read(userid);
        read(groupid);
        read(filemode);
        read(length);

        {
            final byte[] expected = ArchiveUtils.toAsciiBytes(ArArchiveEntry.TRAILER);
            final byte[] realized = new byte[expected.length];
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read entry trailer. Occured at byte: " + getBytesRead());
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid entry trailer. not read the content? Occured at byte: " + getBytesRead());
                }
            }
        }

        entryOffset = offset;

//        GNU ar stores multiple extended filenames in the data section of a file with the name "//", this record is referred to by future headers. A header references an extended filename by storing a "/" followed by a decimal offset to the start of the filename in the extended filename data section. The format of this "//" file itself is simply a list of the long filenames, each separated by one or more LF characters. Note that the decimal offsets are number of characters, not line or string number within the "//" file.
//
//        GNU ar uses a '/' to mark the end of the filename; this allows for the use of spaces without the use of an extended filename.

        // entry name is stored as ASCII string
        String temp = ArchiveUtils.toAsciiString(name).trim();

        if (temp.equals("//")){ // GNU extended filenames entry
            int bufflen = asInt(length); // Assume length will fit in an int
            namebuffer = new byte[bufflen];
            int read = read(namebuffer, 0, bufflen);
            if (read != bufflen){
                throw new IOException("Failed to read complete // record: expected="+bufflen+" read="+read);
            }
            currentEntry = new ArArchiveEntry(temp, bufflen);
            return getNextArEntry();
        } else if (temp.endsWith("/")) { // GNU terminator
            temp = temp.substring(0, temp.length() - 1);
        } else if (temp.matches("^/\\d+")) {// GNU long filename ref.
            int offset = Integer.parseInt(temp.substring(1));// get the offset
            temp = getExtendedName(offset); // convert to the long name
        }
        currentEntry = new ArArchiveEntry(temp, asLong(length), asInt(userid, true),
                                          asInt(groupid, true), asInt(filemode, 8),
                                          asLong(lastmodified));
        return currentEntry;
    }

    /**
     * Get an extended name from the GNU extended name buffer.
     * 
     * @param offset pointer to entry within the buffer
     * @return the extended file name; without trailing "/" if present.
     * @throws IOException if name not found or buffer not set up
     */
    private String getExtendedName(int offset) throws IOException{
        if (namebuffer == null) {
            throw new IOException("Cannot process GNU long filename as no // record was found");
        }
        for(int i=offset; i < namebuffer.length; i++){
            if (namebuffer[i]=='\012'){
                if (namebuffer[i-1]=='/') {
                    i--; // drop trailing /
                }
                return ArchiveUtils.toAsciiString(namebuffer, offset, i-offset);
            }
        }
        throw new IOException("Failed to read entry: "+offset);
    }
    private long asLong(byte[] input) {
        return Long.parseLong(new String(input).trim());
    }

    private int asInt(byte[] input) {
        return asInt(input, 10, false);
    }

    private int asInt(byte[] input, boolean treatBlankAsZero) {
        return asInt(input, 10, treatBlankAsZero);
    }

    private int asInt(byte[] input, int base) {
        return asInt(input, base, false);
    }

    private int asInt(byte[] input, int base, boolean treatBlankAsZero) {
        String string = new String(input).trim();
        if (string.length() == 0 && treatBlankAsZero) {
            return 0;
        }
        return Integer.parseInt(string, base);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.commons.compress.archivers.ArchiveInputStream#getNextEntry()
     */
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextArEntry();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            input.close();
        }
        currentEntry = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, final int off, final int len) throws IOException {
        int toRead = len;
        if (currentEntry != null) {
            final long entryEnd = entryOffset + currentEntry.getLength();
            if (len > 0 && entryEnd > offset) {
                toRead = (int) Math.min(len, entryEnd - offset);
            } else {
                return -1;
            }
        }
        final int ret = this.input.read(b, off, toRead);
        count(ret);
        offset += (ret > 0 ? ret : 0);
        return ret;
    }

    /**
     * Checks if the signature matches ASCII "!<arch>" followed by a single LF
     * control character
     * 
     * @param signature
     *            the bytes to check
     * @param length
     *            the number of bytes to check
     * @return true, if this stream is an Ar archive stream, false otherwise
     */
    public static boolean matches(byte[] signature, int length) {
        // 3c21 7261 6863 0a3e

        if (length < 8) {
            return false;
        }
        if (signature[0] != 0x21) {
            return false;
        }
        if (signature[1] != 0x3c) {
            return false;
        }
        if (signature[2] != 0x61) {
            return false;
        }
        if (signature[3] != 0x72) {
            return false;
        }
        if (signature[4] != 0x63) {
            return false;
        }
        if (signature[5] != 0x68) {
            return false;
        }
        if (signature[6] != 0x3e) {
            return false;
        }
        if (signature[7] != 0x0a) {
            return false;
        }

        return true;
    }

}
