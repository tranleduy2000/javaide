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
package org.apache.commons.compress.archivers.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * Implements an input stream that can read Zip archives.
 * <p>
 * Note that {@link ZipArchiveEntry#getSize()} may return -1 if the DEFLATE algorithm is used, as the size information
 * is not available from the header.
 * <p>
 * The {@link ZipFile} class is preferred when reading from files.
 *  
 * @see ZipFile
 * @NotThreadSafe
 */
public class ZipArchiveInputStream extends ArchiveInputStream {

    private static final int SHORT = 2;
    private static final int WORD = 4;

    /**
     * The zip encoding to use for filenames and the file comment.
     */
    private final ZipEncoding zipEncoding;

    /**
     * Whether to look for and use Unicode extra fields.
     */
    private final boolean useUnicodeExtraFields;

    private final InputStream in;

    private final Inflater inf = new Inflater(true);
    private final CRC32 crc = new CRC32();

    private final byte[] buf = new byte[ZipArchiveOutputStream.BUFFER_SIZE];

    private ZipArchiveEntry current = null;
    private boolean closed = false;
    private boolean hitCentralDirectory = false;
    private int offsetInBuffer = 0;
    private long readBytesOfEntry = 0, bytesReadFromStream = 0;
    private int lengthOfLastRead = 0;
    private boolean hasDataDescriptor = false;
    private ByteArrayInputStream lastStoredEntry = null;

    private boolean allowStoredEntriesWithDataDescriptor = false;

    private static final int LFH_LEN = 30;
    /*
      local file header signature     4 bytes  (0x04034b50)
      version needed to extract       2 bytes
      general purpose bit flag        2 bytes
      compression method              2 bytes
      last mod file time              2 bytes
      last mod file date              2 bytes
      crc-32                          4 bytes
      compressed size                 4 bytes
      uncompressed size               4 bytes
      file name length                2 bytes
      extra field length              2 bytes
    */

    public ZipArchiveInputStream(InputStream inputStream) {
        this(inputStream, ZipEncodingHelper.UTF8, true);
    }

    /**
     * @param encoding the encoding to use for file names, use null
     * for the platform's default encoding
     * @param useUnicodeExtraFields whether to use InfoZIP Unicode
     * Extra Fields (if present) to set the file names.
     */
    public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields) {
        this(inputStream, encoding, useUnicodeExtraFields, false);
    }

    /**
     * @param encoding the encoding to use for file names, use null
     * for the platform's default encoding
     * @param useUnicodeExtraFields whether to use InfoZIP Unicode
     * Extra Fields (if present) to set the file names.
     * @param allowStoredEntriesWithDataDescriptor whether the stream
     * will try to read STORED entries that use a data descriptor
     * @since Apache Commons Compress 1.1
     */
    public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        in = new PushbackInputStream(inputStream, buf.length);
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
    }

    public ZipArchiveEntry getNextZipEntry() throws IOException {
        if (closed || hitCentralDirectory) {
            return null;
        }
        if (current != null) {
            closeEntry();
        }
        byte[] lfh = new byte[LFH_LEN];
        try {
            readFully(lfh);
        } catch (EOFException e) {
            return null;
        }
        ZipLong sig = new ZipLong(lfh);
        if (sig.equals(ZipLong.CFH_SIG)) {
            hitCentralDirectory = true;
            return null;
        }
        if (!sig.equals(ZipLong.LFH_SIG)) {
            return null;
        }

        int off = WORD;
        current = new ZipArchiveEntry();

        int versionMadeBy = ZipShort.getValue(lfh, off);
        off += SHORT;
        current.setPlatform((versionMadeBy >> ZipFile.BYTE_SHIFT)
                            & ZipFile.NIBLET_MASK);

        final GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(lfh, off);
        final boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
        final ZipEncoding entryEncoding =
            hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : zipEncoding;
        hasDataDescriptor = gpFlag.usesDataDescriptor();
        current.setGeneralPurposeBit(gpFlag);

        off += SHORT;

        current.setMethod(ZipShort.getValue(lfh, off));
        off += SHORT;

        long time = ZipUtil.dosToJavaTime(ZipLong.getValue(lfh, off));
        current.setTime(time);
        off += WORD;

        if (!hasDataDescriptor) {
            current.setCrc(ZipLong.getValue(lfh, off));
            off += WORD;

            current.setCompressedSize(ZipLong.getValue(lfh, off));
            off += WORD;

            current.setSize(ZipLong.getValue(lfh, off));
            off += WORD;
        } else {
            off += 3 * WORD;
        }

        int fileNameLen = ZipShort.getValue(lfh, off);

        off += SHORT;

        int extraLen = ZipShort.getValue(lfh, off);
        off += SHORT;

        byte[] fileName = new byte[fileNameLen];
        readFully(fileName);
        current.setName(entryEncoding.decode(fileName), fileName);

        byte[] extraData = new byte[extraLen];
        readFully(extraData);
        current.setExtra(extraData);

        if (!hasUTF8Flag && useUnicodeExtraFields) {
            ZipUtil.setNameAndCommentFromExtraFields(current, fileName, null);
        }
        return current;
    }

    /** {@inheritDoc} */
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextZipEntry();
    }

    /**
     * Whether this class is able to read the given entry.
     *
     * <p>May return false if it is set up to use encryption or a
     * compression method that hasn't been implemented yet.</p>
     * @since Apache Commons Compress 1.1
     */
    public boolean canReadEntryData(ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            return ZipUtil.canHandleEntryData(ze)
                && supportsDataDescriptorFor(ze);

        }
        return false;
    }

    public int read(byte[] buffer, int start, int length) throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }
        if (inf.finished() || current == null) {
            return -1;
        }

        // avoid int overflow, check null buffer
        if (start <= buffer.length && length >= 0 && start >= 0
            && buffer.length - start >= length) {
            ZipUtil.checkRequestedFeatures(current);
            if (!supportsDataDescriptorFor(current)) {
                throw new UnsupportedZipFeatureException(UnsupportedZipFeatureException
                                                         .Feature
                                                         .DATA_DESCRIPTOR,
                                                         current);
            }

            if (current.getMethod() == ZipArchiveOutputStream.STORED) {
                if (hasDataDescriptor) {
                    if (lastStoredEntry == null) {
                        readStoredEntry();
                    }
                    return lastStoredEntry.read(buffer, start, length);
                }

                long csize = current.getSize();
                if (readBytesOfEntry >= csize) {
                    return -1;
                }
                if (offsetInBuffer >= lengthOfLastRead) {
                    offsetInBuffer = 0;
                    if ((lengthOfLastRead = in.read(buf)) == -1) {
                        return -1;
                    }
                    count(lengthOfLastRead);
                    bytesReadFromStream += lengthOfLastRead;
                }
                int toRead = length > lengthOfLastRead
                    ? lengthOfLastRead - offsetInBuffer
                    : length;
                if ((csize - readBytesOfEntry) < toRead) {
                    // if it is smaller than toRead then it fits into an int
                    toRead = (int) (csize - readBytesOfEntry);
                }
                System.arraycopy(buf, offsetInBuffer, buffer, start, toRead);
                offsetInBuffer += toRead;
                readBytesOfEntry += toRead;
                crc.update(buffer, start, toRead);
                return toRead;
            }

            if (inf.needsInput()) {
                fill();
                if (lengthOfLastRead > 0) {
                    bytesReadFromStream += lengthOfLastRead;
                }
            }
            int read = 0;
            try {
                read = inf.inflate(buffer, start, length);
            } catch (DataFormatException e) {
                throw new ZipException(e.getMessage());
            }
            if (read == 0) {
                if (inf.finished()) {
                    return -1;
                } else if (lengthOfLastRead == -1) {
                    throw new IOException("Truncated ZIP file");
                }
            }
            crc.update(buffer, start, read);
            return read;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public void close() throws IOException {
        if (!closed) {
            closed = true;
            in.close();
        }
    }

    /**
     * Skips over and discards value bytes of data from this input
     * stream.
     *
     * <p>This implementation may end up skipping over some smaller
     * number of bytes, possibly 0, if an only if it reaches the end
     * of the underlying stream.</p>
     *
     * <p>The actual number of bytes skipped is returned.</p>
     *
     * @param value the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException - if an I/O error occurs.
     * @throws IllegalArgumentException - if value is negative.
     */
    public long skip(long value) throws IOException {
        if (value >= 0) {
            long skipped = 0;
            byte[] b = new byte[1024];
            while (skipped < value) {
                long rem = value - skipped;
                int x = read(b, 0, (int) (b.length > rem ? rem : b.length));
                if (x == -1) {
                    return skipped;
                }
                skipped += x;
            }
            return skipped;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks if the signature matches what is expected for a zip file.
     * Does not currently handle self-extracting zips which may have arbitrary
     * leading content.
     * 
     * @param signature
     *            the bytes to check
     * @param length
     *            the number of bytes to check
     * @return true, if this stream is a zip archive stream, false otherwise
     */
    public static boolean matches(byte[] signature, int length) {
        if (length < ZipArchiveOutputStream.LFH_SIG.length) {
            return false;
        }

        return checksig(signature, ZipArchiveOutputStream.LFH_SIG) // normal file
            || checksig(signature, ZipArchiveOutputStream.EOCD_SIG); // empty zip
    }

    private static boolean checksig(byte[] signature, byte[] expected){
        for (int i = 0; i < expected.length; i++) {
            if (signature[i] != expected[i]) {
                return false;
            }
        }
        return true;        
    }

    /**
     * Closes the current ZIP archive entry and positions the underlying
     * stream to the beginning of the next entry. All per-entry variables
     * and data structures are cleared.
     * <p>
     * If the compressed size of this entry is included in the entry header,
     * then any outstanding bytes are simply skipped from the underlying
     * stream without uncompressing them. This allows an entry to be safely
     * closed even if the compression method is unsupported.
     * <p>
     * In case we don't know the compressed size of this entry or have
     * already buffered too much data from the underlying stream to support
     * uncompression, then the uncompression process is completed and the
     * end position of the stream is adjusted based on the result of that
     * process.
     *
     * @throws IOException if an error occurs
     */
    private void closeEntry() throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }
        if (current == null) {
            return;
        }

        // Ensure all entry bytes are read
        if (bytesReadFromStream <= current.getCompressedSize()
                && !hasDataDescriptor) {
            long remaining = current.getCompressedSize() - bytesReadFromStream;
            while (remaining > 0) {
                long n = in.read(buf, 0, (int) Math.min(buf.length, remaining));
                if (n < 0) {
                    throw new EOFException(
                            "Truncated ZIP entry: " + current.getName());
                } else {
                    count(n);
                    remaining -= n;
                }
            }
        } else {
            skip(Long.MAX_VALUE);

            long inB;
            if (current.getMethod() == ZipArchiveOutputStream.DEFLATED) {
                inB = ZipUtil.adjustToLong(inf.getTotalIn());
            } else {
                inB = readBytesOfEntry;
            }

            // this is at most a single read() operation and can't
            // exceed the range of int
            int diff = (int) (bytesReadFromStream - inB);

            // Pushback any required bytes
            if (diff > 0) {
                ((PushbackInputStream) in).unread(
                        buf,  lengthOfLastRead - diff, diff);
                pushedBackBytes(diff);
            }
        }

        if (lastStoredEntry == null && hasDataDescriptor) {
            readDataDescriptor();
        }

        inf.reset();
        readBytesOfEntry = bytesReadFromStream = 0L;
        offsetInBuffer = lengthOfLastRead = 0;
        crc.reset();
        current = null;
        lastStoredEntry = null;
    }

    private void fill() throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }
        if ((lengthOfLastRead = in.read(buf)) > 0) {
            count(lengthOfLastRead);
            inf.setInput(buf, 0, lengthOfLastRead);
        }
    }

    private void readFully(byte[] b) throws IOException {
        int count = 0, x = 0;
        while (count != b.length) {
            count += x = in.read(b, count, b.length - count);
            if (x == -1) {
                throw new EOFException();
            }
            count(x);
        }
    }

    private void readDataDescriptor() throws IOException {
        byte[] b = new byte[WORD];
        readFully(b);
        ZipLong val = new ZipLong(b);
        if (ZipLong.DD_SIG.equals(val)) {
            // data descriptor with signature, skip sig
            readFully(b);
            val = new ZipLong(b);
        }
        current.setCrc(val.getValue());
        readFully(b);
        current.setCompressedSize(new ZipLong(b).getValue());
        readFully(b);
        current.setSize(new ZipLong(b).getValue());
    }

    /**
     * Whether this entry requires a data descriptor this library can work with.
     *
     * @return true if allowStoredEntriesWithDataDescriptor is true,
     * the entry doesn't require any data descriptor or the method is
     * DEFLATED.
     */
    private boolean supportsDataDescriptorFor(ZipArchiveEntry entry) {
        return allowStoredEntriesWithDataDescriptor ||
            !entry.getGeneralPurposeBit().usesDataDescriptor()
            || entry.getMethod() == ZipArchiveEntry.DEFLATED;
    }

    /**
     * Caches a stored entry that uses the data descriptor.
     *
     * <ul>
     *   <li>Reads a stored entry until the signature of a local file
     *     header, central directory header or data descriptor has been
     *     found.</li>
     *   <li>Stores all entry data in lastStoredEntry.</p>
     *   <li>Rewinds the stream to position at the data
     *     descriptor.</li>
     *   <li>reads the data descriptor</li>
     * </ul>
     *
     * <p>After calling this method the entry should know its size,
     * the entry's data is cached and the stream is positioned at the
     * next local file or central directory header.</p>
     */
    private void readStoredEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] LFH = ZipLong.LFH_SIG.getBytes();
        byte[] CFH = ZipLong.CFH_SIG.getBytes();
        byte[] DD = ZipLong.DD_SIG.getBytes();
        int off = 0;
        boolean done = false;

        while (!done) {
            int r = in.read(buf, off, ZipArchiveOutputStream.BUFFER_SIZE - off);
            if (r <= 0) {
                // read the whole archive without ever finding a
                // central directory
                throw new IOException("Truncated ZIP file");
            }
            if (r + off < 4) {
                // buf is too small to check for a signature, loop
                off += r;
                continue;
            }

            int readTooMuch = 0;
            for (int i = 0; !done && i < r - 4; i++) {
                if (buf[i] == LFH[0] && buf[i + 1] == LFH[1]) {
                    if ((buf[i + 2] == LFH[2] && buf[i + 3] == LFH[3])
                        || (buf[i] == CFH[2] && buf[i + 3] == CFH[3])) {
                        // found a LFH or CFH:
                        readTooMuch = off + r - i - 12 /* dd without signature */;
                        done = true;
                    }
                    else if (buf[i + 2] == DD[2] && buf[i + 3] == DD[3]) {
                        // found DD:
                        readTooMuch = off + r - i;
                        done = true;
                    }
                    if (done) {
                        // * push back bytes read in excess as well as the data
                        //   descriptor
                        // * copy the remaining bytes to cache
                        // * read data descriptor
                        ((PushbackInputStream) in).unread(buf, off + r - readTooMuch, readTooMuch);
                        bos.write(buf, 0, i);
                        readDataDescriptor();
                    }
                }
            }
            if (!done) {
                // worst case we've read a data descriptor without a
                // signature (12 bytes) plus the first three bytes of
                // a LFH or CFH signature
                // save the last 15 bytes in the buffer, cache
                // anything in front of that, read on
                if (off + r > 15) {
                    bos.write(buf, 0, off + r - 15);
                    System.arraycopy(buf, off + r - 15, buf, 0, 15);
                    off = 15;
                } else {
                    off += r;
                }
            }
        }

        byte[] b = bos.toByteArray();
        lastStoredEntry = new ByteArrayInputStream(b);
    }
}
