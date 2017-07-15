/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * Replacement for <code>java.util.ZipFile</code>.
 *
 * <p>This class adds support for file name encodings other than UTF-8
 * (which is required to work on ZIP files created by native zip tools
 * and is able to skip a preamble like the one found in self
 * extracting archives.  Furthermore it returns instances of
 * <code>org.apache.commons.compress.archivers.zip.ZipArchiveEntry</code>
 * instead of <code>java.util.zip.ZipEntry</code>.</p>
 *
 * <p>It doesn't extend <code>java.util.zip.ZipFile</code> as it would
 * have to reimplement all methods anyway.  Like
 * <code>java.util.ZipFile</code>, it uses RandomAccessFile under the
 * covers and supports compressed and uncompressed entries.</p>
 *
 * <p>The method signatures mimic the ones of
 * <code>java.util.zip.ZipFile</code>, with a couple of exceptions:
 *
 * <ul>
 *   <li>There is no getName method.</li>
 *   <li>entries has been renamed to getEntries.</li>
 *   <li>getEntries and getEntry return
 *   <code>org.apache.commons.compress.archivers.zip.ZipArchiveEntry</code>
 *   instances.</li>
 *   <li>close is allowed to throw IOException.</li>
 * </ul>
 *
 */
public class ZipFile {
    private static final int HASH_SIZE = 509;
    private static final int SHORT     =   2;
    private static final int WORD      =   4;
    static final int NIBLET_MASK = 0x0f;
    static final int BYTE_SHIFT = 8;
    private static final int POS_0 = 0;
    private static final int POS_1 = 1;
    private static final int POS_2 = 2;
    private static final int POS_3 = 3;

    /**
     * Maps ZipArchiveEntrys to Longs, recording the offsets of the local
     * file headers.
     */
    private final Map entries = new LinkedHashMap(HASH_SIZE);

    /**
     * Maps String to ZipArchiveEntrys, name -> actual entry.
     */
    private final Map nameMap = new HashMap(HASH_SIZE);

    private static final class OffsetEntry {
        private long headerOffset = -1;
        private long dataOffset = -1;
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * <p>For a list of possible values see <a
     * href="http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html">http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html</a>.
     * Defaults to UTF-8.</p>
     */
    private final String encoding;

    /**
     * The zip encoding to use for filenames and the file comment.
     */
    private final ZipEncoding zipEncoding;

    /**
     * File name of actual source.
     */
    private final String archiveName;

    /**
     * The actual data source.
     */
    private final RandomAccessFile archive;

    /**
     * Whether to look for and use Unicode extra fields.
     */
    private final boolean useUnicodeExtraFields;

    /**
     * Whether the file is closed.
     */
    private boolean closed;

    /**
     * Opens the given file for reading, assuming "UTF8" for file names.
     *
     * @param f the archive.
     *
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipFile(File f) throws IOException {
        this(f, ZipEncodingHelper.UTF8);
    }

    /**
     * Opens the given file for reading, assuming "UTF8".
     *
     * @param name name of the archive.
     *
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipFile(String name) throws IOException {
        this(new File(name), ZipEncodingHelper.UTF8);
    }

    /**
     * Opens the given file for reading, assuming the specified
     * encoding for file names, scanning unicode extra fields.
     *
     * @param name name of the archive.
     * @param encoding the encoding to use for file names, use null
     * for the platform's default encoding
     *
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipFile(String name, String encoding) throws IOException {
        this(new File(name), encoding, true);
    }

    /**
     * Opens the given file for reading, assuming the specified
     * encoding for file names and scanning for unicode extra fields.
     *
     * @param f the archive.
     * @param encoding the encoding to use for file names, use null
     * for the platform's default encoding
     *
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipFile(File f, String encoding) throws IOException {
        this(f, encoding, true);
    }

    /**
     * Opens the given file for reading, assuming the specified
     * encoding for file names.
     *
     * @param f the archive.
     * @param encoding the encoding to use for file names, use null
     * for the platform's default encoding
     * @param useUnicodeExtraFields whether to use InfoZIP Unicode
     * Extra Fields (if present) to set the file names.
     *
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipFile(File f, String encoding, boolean useUnicodeExtraFields)
        throws IOException {
        this.archiveName = f.getAbsolutePath();
        this.encoding = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        archive = new RandomAccessFile(f, "r");
        boolean success = false;
        try {
            Map entriesWithoutUTF8Flag = populateFromCentralDirectory();
            resolveLocalFileHeaderData(entriesWithoutUTF8Flag);
            success = true;
        } finally {
            if (!success) {
                try {
                    closed = true;
                    archive.close();
                } catch (IOException e2) { // NOPMD
                    // swallow, throw the original exception instead
                }
            }
        }
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return null if using the platform's default character encoding.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Closes the archive.
     * @throws IOException if an error occurs closing the archive.
     */
    public void close() throws IOException {
        // this flag is only written here and read in finalize() which
        // can never be run in parallel.
        // no synchronization needed.
        closed = true;

        archive.close();
    }

    /**
     * close a zipfile quietly; throw no io fault, do nothing
     * on a null parameter
     * @param zipfile file to close, can be null
     */
    public static void closeQuietly(ZipFile zipfile) {
        if (zipfile != null) {
            try {
                zipfile.close();
            } catch (IOException e) { // NOPMD
                //ignore, that's why the method is called "quietly"
            }
        }
    }

    /**
     * Returns all entries.
     *
     * <p>Entries will be returned in the same order they appear
     * within the archive's central directory.</p>
     *
     * @return all entries as {@link ZipArchiveEntry} instances
     */
    public Enumeration getEntries() {
        return Collections.enumeration(entries.keySet());
    }

    /**
     * Returns all entries in physical order.
     *
     * <p>Entries will be returned in the same order their contents
     * appear within the archive.</p>
     *
     * @return all entries as {@link ZipArchiveEntry} instances
     *
     * @since Commons Compress 1.1
     */
    public Enumeration getEntriesInPhysicalOrder() {
        Object[] allEntries = entries.keySet().toArray();
        Arrays.sort(allEntries, OFFSET_COMPARATOR);
        return Collections.enumeration(Arrays.asList(allEntries));
    }

    /**
     * Returns a named entry - or <code>null</code> if no entry by
     * that name exists.
     * @param name name of the entry.
     * @return the ZipArchiveEntry corresponding to the given name - or
     * <code>null</code> if not present.
     */
    public ZipArchiveEntry getEntry(String name) {
        return (ZipArchiveEntry) nameMap.get(name);
    }

    /**
     * Whether this class is able to read the given entry.
     *
     * <p>May return false if it is set up to use encryption or a
     * compression method that hasn't been implemented yet.</p>
     * @since Apache Commons Compress 1.1
     */
    public boolean canReadEntryData(ZipArchiveEntry ze) {
        return ZipUtil.canHandleEntryData(ze);
    }

    /**
     * Returns an InputStream for reading the contents of the given entry.
     *
     * @param ze the entry to get the stream for.
     * @return a stream to read the entry from.
     * @throws IOException if unable to create an input stream from the zipenty
     * @throws ZipException if the zipentry uses an unsupported feature
     */
    public InputStream getInputStream(ZipArchiveEntry ze)
        throws IOException, ZipException {
        OffsetEntry offsetEntry = (OffsetEntry) entries.get(ze);
        if (offsetEntry == null) {
            return null;
        }
        ZipUtil.checkRequestedFeatures(ze);
        long start = offsetEntry.dataOffset;
        BoundedInputStream bis =
            new BoundedInputStream(start, ze.getCompressedSize());
        switch (ze.getMethod()) {
            case ZipArchiveEntry.STORED:
                return bis;
            case ZipArchiveEntry.DEFLATED:
                bis.addDummy();
                final Inflater inflater = new Inflater(true);
                return new InflaterInputStream(bis, inflater) {
                    public void close() throws IOException {
                        super.close();
                        inflater.end();
                    }
                };
            default:
                throw new ZipException("Found unsupported compression method "
                                       + ze.getMethod());
        }
    }

    /**
     * Ensures that the close method of this zipfile is called when
     * there are no more references to it.
     * @see #close()
     */
    protected void finalize() throws Throwable {
        try {
            if (!closed) {
                System.err.println("Cleaning up unclosed ZipFile for archive "
                                   + archiveName);
                close();
            }
        } finally {
            super.finalize();
        }
    }

    private static final int CFH_LEN =
        /* version made by                 */ SHORT
        /* version needed to extract       */ + SHORT
        /* general purpose bit flag        */ + SHORT
        /* compression method              */ + SHORT
        /* last mod file time              */ + SHORT
        /* last mod file date              */ + SHORT
        /* crc-32                          */ + WORD
        /* compressed size                 */ + WORD
        /* uncompressed size               */ + WORD
        /* filename length                 */ + SHORT
        /* extra field length              */ + SHORT
        /* file comment length             */ + SHORT
        /* disk number start               */ + SHORT
        /* internal file attributes        */ + SHORT
        /* external file attributes        */ + WORD
        /* relative offset of local header */ + WORD;

    /**
     * Reads the central directory of the given archive and populates
     * the internal tables with ZipArchiveEntry instances.
     *
     * <p>The ZipArchiveEntrys will know all data that can be obtained from
     * the central directory alone, but not the data that requires the
     * local file header or additional data to be read.</p>
     *
     * @return a Map&lt;ZipArchiveEntry, NameAndComment>&gt; of
     * zipentries that didn't have the language encoding flag set when
     * read.
     */
    private Map populateFromCentralDirectory()
        throws IOException {
        HashMap noUTF8Flag = new HashMap();

        positionAtCentralDirectory();

        byte[] cfh = new byte[CFH_LEN];

        byte[] signatureBytes = new byte[WORD];
        archive.readFully(signatureBytes);
        long sig = ZipLong.getValue(signatureBytes);
        final long cfhSig = ZipLong.getValue(ZipArchiveOutputStream.CFH_SIG);
        if (sig != cfhSig && startsWithLocalFileHeader()) {
            throw new IOException("central directory is empty, can't expand"
                                  + " corrupt archive.");
        }
        while (sig == cfhSig) {
            archive.readFully(cfh);
            int off = 0;
            ZipArchiveEntry ze = new ZipArchiveEntry();

            int versionMadeBy = ZipShort.getValue(cfh, off);
            off += SHORT;
            ze.setPlatform((versionMadeBy >> BYTE_SHIFT) & NIBLET_MASK);

            off += SHORT; // skip version info

            final GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(cfh, off);
            final boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
            final ZipEncoding entryEncoding =
                hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : zipEncoding;
            ze.setGeneralPurposeBit(gpFlag);

            off += SHORT;

            ze.setMethod(ZipShort.getValue(cfh, off));
            off += SHORT;

            // FIXME this is actually not very cpu cycles friendly as we are converting from
            // dos to java while the underlying Sun implementation will convert
            // from java to dos time for internal storage...
            long time = ZipUtil.dosToJavaTime(ZipLong.getValue(cfh, off));
            ze.setTime(time);
            off += WORD;

            ze.setCrc(ZipLong.getValue(cfh, off));
            off += WORD;

            ze.setCompressedSize(ZipLong.getValue(cfh, off));
            off += WORD;

            ze.setSize(ZipLong.getValue(cfh, off));
            off += WORD;

            int fileNameLen = ZipShort.getValue(cfh, off);
            off += SHORT;

            int extraLen = ZipShort.getValue(cfh, off);
            off += SHORT;

            int commentLen = ZipShort.getValue(cfh, off);
            off += SHORT;

            off += SHORT; // disk number

            ze.setInternalAttributes(ZipShort.getValue(cfh, off));
            off += SHORT;

            ze.setExternalAttributes(ZipLong.getValue(cfh, off));
            off += WORD;

            byte[] fileName = new byte[fileNameLen];
            archive.readFully(fileName);
            ze.setName(entryEncoding.decode(fileName), fileName);

            // LFH offset,
            OffsetEntry offset = new OffsetEntry();
            offset.headerOffset = ZipLong.getValue(cfh, off);
            // data offset will be filled later
            entries.put(ze, offset);

            nameMap.put(ze.getName(), ze);

            byte[] cdExtraData = new byte[extraLen];
            archive.readFully(cdExtraData);
            ze.setCentralDirectoryExtra(cdExtraData);

            byte[] comment = new byte[commentLen];
            archive.readFully(comment);
            ze.setComment(entryEncoding.decode(comment));

            archive.readFully(signatureBytes);
            sig = ZipLong.getValue(signatureBytes);

            if (!hasUTF8Flag && useUnicodeExtraFields) {
                noUTF8Flag.put(ze, new NameAndComment(fileName, comment));
            }
        }
        return noUTF8Flag;
    }

    private static final int MIN_EOCD_SIZE =
        /* end of central dir signature    */ WORD
        /* number of this disk             */ + SHORT
        /* number of the disk with the     */
        /* start of the central directory  */ + SHORT
        /* total number of entries in      */
        /* the central dir on this disk    */ + SHORT
        /* total number of entries in      */
        /* the central dir                 */ + SHORT
        /* size of the central directory   */ + WORD
        /* offset of start of central      */
        /* directory with respect to       */
        /* the starting disk number        */ + WORD
        /* zipfile comment length          */ + SHORT;

    private static final int MAX_EOCD_SIZE = MIN_EOCD_SIZE
        /* maximum length of zipfile comment */ + 0xFFFF;

    private static final int CFD_LOCATOR_OFFSET =
        /* end of central dir signature    */ WORD
        /* number of this disk             */ + SHORT
        /* number of the disk with the     */
        /* start of the central directory  */ + SHORT
        /* total number of entries in      */
        /* the central dir on this disk    */ + SHORT
        /* total number of entries in      */
        /* the central dir                 */ + SHORT
        /* size of the central directory   */ + WORD;

    /**
     * Searches for the &quot;End of central dir record&quot;, parses
     * it and positions the stream at the first central directory
     * record.
     */
    private void positionAtCentralDirectory()
        throws IOException {
        boolean found = false;
        long off = archive.length() - MIN_EOCD_SIZE;
        long stopSearching = Math.max(0L, archive.length() - MAX_EOCD_SIZE);
        if (off >= 0) {
            archive.seek(off);
            byte[] sig = ZipArchiveOutputStream.EOCD_SIG;
            int curr = archive.read();
            while (off >= stopSearching && curr != -1) {
                if (curr == sig[POS_0]) {
                    curr = archive.read();
                    if (curr == sig[POS_1]) {
                        curr = archive.read();
                        if (curr == sig[POS_2]) {
                            curr = archive.read();
                            if (curr == sig[POS_3]) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
                archive.seek(--off);
                curr = archive.read();
            }
        }
        if (!found) {
            throw new ZipException("archive is not a ZIP archive");
        }
        archive.seek(off + CFD_LOCATOR_OFFSET);
        byte[] cfdOffset = new byte[WORD];
        archive.readFully(cfdOffset);
        archive.seek(ZipLong.getValue(cfdOffset));
    }

    /**
     * Number of bytes in local file header up to the &quot;length of
     * filename&quot; entry.
     */
    private static final long LFH_OFFSET_FOR_FILENAME_LENGTH =
        /* local file header signature     */ WORD
        /* version needed to extract       */ + SHORT
        /* general purpose bit flag        */ + SHORT
        /* compression method              */ + SHORT
        /* last mod file time              */ + SHORT
        /* last mod file date              */ + SHORT
        /* crc-32                          */ + WORD
        /* compressed size                 */ + WORD
        /* uncompressed size               */ + WORD;

    /**
     * Walks through all recorded entries and adds the data available
     * from the local file header.
     *
     * <p>Also records the offsets for the data to read from the
     * entries.</p>
     */
    private void resolveLocalFileHeaderData(Map entriesWithoutUTF8Flag)
        throws IOException {
        Enumeration e = getEntries();
        while (e.hasMoreElements()) {
            ZipArchiveEntry ze = (ZipArchiveEntry) e.nextElement();
            OffsetEntry offsetEntry = (OffsetEntry) entries.get(ze);
            long offset = offsetEntry.headerOffset;
            archive.seek(offset + LFH_OFFSET_FOR_FILENAME_LENGTH);
            byte[] b = new byte[SHORT];
            archive.readFully(b);
            int fileNameLen = ZipShort.getValue(b);
            archive.readFully(b);
            int extraFieldLen = ZipShort.getValue(b);
            int lenToSkip = fileNameLen;
            while (lenToSkip > 0) {
                int skipped = archive.skipBytes(lenToSkip);
                if (skipped <= 0) {
                    throw new RuntimeException("failed to skip file name in"
                                               + " local file header");
                }
                lenToSkip -= skipped;
            }
            byte[] localExtraData = new byte[extraFieldLen];
            archive.readFully(localExtraData);
            ze.setExtra(localExtraData);
            /*dataOffsets.put(ze,
                            new Long(offset + LFH_OFFSET_FOR_FILENAME_LENGTH
                                     + SHORT + SHORT + fileNameLen + extraFieldLen));
            */
            offsetEntry.dataOffset = offset + LFH_OFFSET_FOR_FILENAME_LENGTH
                + SHORT + SHORT + fileNameLen + extraFieldLen;

            if (entriesWithoutUTF8Flag.containsKey(ze)) {
                String orig = ze.getName();
                NameAndComment nc = (NameAndComment) entriesWithoutUTF8Flag.get(ze);
                ZipUtil.setNameAndCommentFromExtraFields(ze, nc.name,
                                                         nc.comment);
                if (!orig.equals(ze.getName())) {
                    nameMap.remove(orig);
                    nameMap.put(ze.getName(), ze);
                }
            }
        }
    }

    /**
     * Checks whether the archive starts with a LFH.  If it doesn't,
     * it may be an empty archive.
     */
    private boolean startsWithLocalFileHeader() throws IOException {
        archive.seek(0);
        final byte[] start = new byte[WORD];
        archive.readFully(start);
        for (int i = 0; i < start.length; i++) {
            if (start[i] != ZipArchiveOutputStream.LFH_SIG[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * InputStream that delegates requests to the underlying
     * RandomAccessFile, making sure that only bytes from a certain
     * range can be read.
     */
    private class BoundedInputStream extends InputStream {
        private long remaining;
        private long loc;
        private boolean addDummyByte = false;

        BoundedInputStream(long start, long remaining) {
            this.remaining = remaining;
            loc = start;
        }

        public int read() throws IOException {
            if (remaining-- <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    return 0;
                }
                return -1;
            }
            synchronized (archive) {
                archive.seek(loc++);
                return archive.read();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    b[off] = 0;
                    return 1;
                }
                return -1;
            }

            if (len <= 0) {
                return 0;
            }

            if (len > remaining) {
                len = (int) remaining;
            }
            int ret = -1;
            synchronized (archive) {
                archive.seek(loc);
                ret = archive.read(b, off, len);
            }
            if (ret > 0) {
                loc += ret;
                remaining -= ret;
            }
            return ret;
        }

        /**
         * Inflater needs an extra dummy byte for nowrap - see
         * Inflater's javadocs.
         */
        void addDummy() {
            addDummyByte = true;
        }
    }

    private static final class NameAndComment {
        private final byte[] name;
        private final byte[] comment;
        private NameAndComment(byte[] name, byte[] comment) {
            this.name = name;
            this.comment = comment;
        }
    }

    /**
     * Compares two ZipArchiveEntries based on their offset within the archive.
     *
     * <p>Won't return any meaningful results if one of the entries
     * isn't part of the archive at all.</p>
     *
     * @since Commons Compress 1.1
     */
    private final Comparator OFFSET_COMPARATOR =
        new Comparator() {
            public int compare(Object o1, Object o2) {
                if (o1 == o2)
                    return 0;

                ZipArchiveEntry e1 = (ZipArchiveEntry) o1;
                ZipArchiveEntry e2 = (ZipArchiveEntry) o2;

                OffsetEntry off1 = (OffsetEntry) entries.get(e1);
                OffsetEntry off2 = (OffsetEntry) entries.get(e2);
                if (off1 == null) {
                    return 1;
                }
                if (off2 == null) {
                    return -1;
                }
                long val = (off1.headerOffset - off2.headerOffset);
                return val == 0 ? 0 : val < 0 ? -1 : +1;
            }
        };
}
