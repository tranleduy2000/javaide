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
package org.apache.commons.compress.archivers.tar;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * This class represents an entry in a Tar archive. It consists
 * of the entry's header, as well as the entry's File. Entries
 * can be instantiated in one of three ways, depending on how
 * they are to be used.
 * <p>
 * TarEntries that are created from the header bytes read from
 * an archive are instantiated with the TarEntry( byte[] )
 * constructor. These entries will be used when extracting from
 * or listing the contents of an archive. These entries have their
 * header filled in using the header bytes. They also set the File
 * to null, since they reference an archive entry not a file.
 * <p>
 * TarEntries that are created from Files that are to be written
 * into an archive are instantiated with the TarEntry( File )
 * constructor. These entries have their header filled in using
 * the File's information. They also keep a reference to the File
 * for convenience when writing entries.
 * <p>
 * Finally, TarEntries can be constructed from nothing but a name.
 * This allows the programmer to construct the entry by hand, for
 * instance when only an InputStream is available for writing to
 * the archive, and the header information is constructed from
 * other information. In this case the header fields are set to
 * defaults and the File is set to null.
 *
 * <p>
 * The C structure for a Tar Entry's header is:
 * <pre>
 * struct header {
 * char name[100];     // TarConstants.NAMELEN    - offset   0
 * char mode[8];       // TarConstants.MODELEN    - offset 100
 * char uid[8];        // TarConstants.UIDLEN     - offset 108
 * char gid[8];        // TarConstants.GIDLEN     - offset 116
 * char size[12];      // TarConstants.SIZELEN    - offset 124
 * char mtime[12];     // TarConstants.MODTIMELEN - offset 136
 * char chksum[8];     // TarConstants.CHKSUMLEN  - offset 148
 * char linkflag[1];   //                         - offset 156
 * char linkname[100]; // TarConstants.NAMELEN    - offset 157
 * The following fields are only present in new-style POSIX tar archives:
 * char magic[6];      // TarConstants.MAGICLEN   - offset 257
 * char version[2];    // TarConstants.VERSIONLEN - offset 263
 * char uname[32];     // TarConstants.UNAMELEN   - offset 265
 * char gname[32];     // TarConstants.GNAMELEN   - offset 297
 * char devmajor[8];   // TarConstants.DEVLEN     - offset 329
 * char devminor[8];   // TarConstants.DEVLEN     - offset 337
 * char prefix[155];   // TarConstants.PREFIXLEN  - offset 345
 * // Used if "name" field is not long enough to hold the path
 * char pad[12];       // NULs                    - offset 500
 * } header;
 * All unused bytes are set to null.
 * New-style GNU tar files are slightly different from the above.
 * </pre>
 * 
 * <p>
 * The C structure for a old GNU Tar Entry's header is:
 * <pre>
 * struct oldgnu_header {
 * char unused_pad1[345]; // TarConstants.PAD1LEN_GNU       - offset 0
 * char atime[12];        // TarConstants.ATIMELEN_GNU      - offset 345
 * char ctime[12];        // TarConstants.CTIMELEN_GNU      - offset 357
 * char offset[12];       // TarConstants.OFFSETLEN_GNU     - offset 369
 * char longnames[4];     // TarConstants.LONGNAMESLEN_GNU  - offset 381
 * char unused_pad2;      // TarConstants.PAD2LEN_GNU       - offset 385
 * struct sparse sp[4];   // TarConstants.SPARSELEN_GNU     - offset 386
 * char isextended;       // TarConstants.ISEXTENDEDLEN_GNU - offset 482
 * char realsize[12];     // TarConstants.REALSIZELEN_GNU   - offset 483
 * char unused_pad[17];   // TarConstants.PAD3LEN_GNU       - offset 495
 * };
 * </pre>
 * Whereas, "struct sparse" is:
 * <pre>
 * struct sparse {
 * char offset[12];   // offset 0
 * char numbytes[12]; // offset 12
 * };
 * </pre>
 *
 * @NotThreadSafe
 */

public class TarArchiveEntry implements TarConstants, ArchiveEntry {
    /** The entry's name. */
    private String name;

    /** The entry's permission mode. */
    private int mode;

    /** The entry's user id. */
    private int userId;

    /** The entry's group id. */
    private int groupId;

    /** The entry's size. */
    private long size;

    /** The entry's modification time. */
    private long modTime;

    /** The entry's link flag. */
    private byte linkFlag;

    /** The entry's link name. */
    private String linkName;

    /** The entry's magic tag. */
    private String magic;
    /** The version of the format */
    private String version;

    /** The entry's user name. */
    private String userName;

    /** The entry's group name. */
    private String groupName;

    /** The entry's major device number. */
    private int devMajor;

    /** The entry's minor device number. */
    private int devMinor;

    /** If an extension sparse header follows. */
    private boolean isExtended;

    /** The entry's real size in case of a sparse file. */
    private long realSize;

    /** The entry's file reference */
    private File file;

    /** Maximum length of a user's name in the tar file */
    public static final int MAX_NAMELEN = 31;

    /** Default permissions bits for directories */
    public static final int DEFAULT_DIR_MODE = 040755;

    /** Default permissions bits for files */
    public static final int DEFAULT_FILE_MODE = 0100644;

    /** Convert millis to seconds */
    public static final int MILLIS_PER_SECOND = 1000;

    /**
     * Construct an empty entry and prepares the header values.
     */
    private TarArchiveEntry () {
        this.magic = MAGIC_POSIX;
        this.version = VERSION_POSIX;
        this.name = "";
        this.linkName = "";

        String user = System.getProperty("user.name", "");

        if (user.length() > MAX_NAMELEN) {
            user = user.substring(0, MAX_NAMELEN);
        }

        this.userId = 0;
        this.groupId = 0;
        this.userName = user;
        this.groupName = "";
        this.file = null;
    }

    /**
     * Construct an entry with only a name. This allows the programmer
     * to construct the entry's header "by hand". File is set to null.
     *
     * @param name the entry name
     */
    public TarArchiveEntry(String name) {
        this(name, false);
    }

    /**
     * Construct an entry with only a name. This allows the programmer
     * to construct the entry's header "by hand". File is set to null.
     *
     * @param name the entry name
     * @param preserveLeadingSlashes whether to allow leading slashes
     * in the name.
     * 
     * @since Apache Commons Compress 1.1
     */
    public TarArchiveEntry(String name, boolean preserveLeadingSlashes) {
        this();

        name = normalizeFileName(name, preserveLeadingSlashes);
        boolean isDir = name.endsWith("/");

        this.devMajor = 0;
        this.devMinor = 0;
        this.name = name;
        this.mode = isDir ? DEFAULT_DIR_MODE : DEFAULT_FILE_MODE;
        this.linkFlag = isDir ? LF_DIR : LF_NORMAL;
        this.userId = 0;
        this.groupId = 0;
        this.size = 0;
        this.modTime = (new Date()).getTime() / MILLIS_PER_SECOND;
        this.linkName = "";
        this.userName = "";
        this.groupName = "";
        this.devMajor = 0;
        this.devMinor = 0;

    }

    /**
     * Construct an entry with a name and a link flag.
     *
     * @param name the entry name
     * @param linkFlag the entry link flag.
     */
    public TarArchiveEntry(String name, byte linkFlag) {
        this(name);
        this.linkFlag = linkFlag;
        if (linkFlag == LF_GNUTYPE_LONGNAME) {
            magic = MAGIC_GNU;
            version = VERSION_GNU_SPACE;
        }
    }

    /**
     * Construct an entry for a file. File is set to file, and the
     * header is constructed from information from the file.
     * The name is set from the normalized file path.
     *
     * @param file The file that the entry represents.
     */
    public TarArchiveEntry(File file) {
        this(file, normalizeFileName(file.getPath(), false));
    }

    /**
     * Construct an entry for a file. File is set to file, and the
     * header is constructed from information from the file.
     *
     * @param file The file that the entry represents.
     * @param fileName the name to be used for the entry.
     */
    public TarArchiveEntry(File file, String fileName) {
        this();

        this.file = file;

        this.linkName = "";

        if (file.isDirectory()) {
            this.mode = DEFAULT_DIR_MODE;
            this.linkFlag = LF_DIR;

            int nameLength = fileName.length();
            if (nameLength == 0 || fileName.charAt(nameLength - 1) != '/') {
                this.name = fileName + "/";
            } else {
                this.name = fileName;
            }
            this.size = 0;
        } else {
            this.mode = DEFAULT_FILE_MODE;
            this.linkFlag = LF_NORMAL;
            this.size = file.length();
            this.name = fileName;
        }

        this.modTime = file.lastModified() / MILLIS_PER_SECOND;
        this.devMajor = 0;
        this.devMinor = 0;
    }

    /**
     * Construct an entry from an archive's header bytes. File is set
     * to null.
     *
     * @param headerBuf The header bytes from a tar archive entry.
     */
    public TarArchiveEntry(byte[] headerBuf) {
        this();
        parseTarHeader(headerBuf);
    }

    /**
     * Determine if the two entries are equal. Equality is determined
     * by the header names being equal.
     *
     * @param it Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals(TarArchiveEntry it) {
        return getName().equals(it.getName());
    }

    /**
     * Determine if the two entries are equal. Equality is determined
     * by the header names being equal.
     *
     * @param it Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals(Object it) {
        if (it == null || getClass() != it.getClass()) {
            return false;
        }
        return equals((TarArchiveEntry) it);
    }

    /**
     * Hashcodes are based on entry names.
     *
     * @return the entry hashcode
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Determine if the given entry is a descendant of this entry.
     * Descendancy is determined by the name of the descendant
     * starting with this entry's name.
     *
     * @param desc Entry to be checked as a descendent of this.
     * @return True if entry is a descendant of this.
     */
    public boolean isDescendent(TarArchiveEntry desc) {
        return desc.getName().startsWith(getName());
    }

    /**
     * Get this entry's name.
     *
     * @return This entry's name.
     */
    public String getName() {
        return name.toString();
    }

    /**
     * Set this entry's name.
     *
     * @param name This entry's new name.
     */
    public void setName(String name) {
        this.name = normalizeFileName(name, false);
    }

    /**
     * Set the mode for this entry
     *
     * @param mode the mode for this entry
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Get this entry's link name.
     *
     * @return This entry's link name.
     */
    public String getLinkName() {
        return linkName.toString();
    }

    /**
     * Set this entry's link name.
     * 
     * @param link the link name to use.
     * 
     * @since Apache Commons Compress 1.1
     */
    public void setLinkName(String link) {
        this.linkName = link;
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Get this entry's user name.
     *
     * @return This entry's user name.
     */
    public String getUserName() {
        return userName.toString();
    }

    /**
     * Set this entry's user name.
     *
     * @param userName This entry's new user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get this entry's group name.
     *
     * @return This entry's group name.
     */
    public String getGroupName() {
        return groupName.toString();
    }

    /**
     * Set this entry's group name.
     *
     * @param groupName This entry's new group name.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Convenience method to set this entry's group and user ids.
     *
     * @param userId This entry's new user id.
     * @param groupId This entry's new group id.
     */
    public void setIds(int userId, int groupId) {
        setUserId(userId);
        setGroupId(groupId);
    }

    /**
     * Convenience method to set this entry's group and user names.
     *
     * @param userName This entry's new user name.
     * @param groupName This entry's new group name.
     */
    public void setNames(String userName, String groupName) {
        setUserName(userName);
        setGroupName(groupName);
    }

    /**
     * Set this entry's modification time. The parameter passed
     * to this method is in "Java time".
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(long time) {
        modTime = time / MILLIS_PER_SECOND;
    }

    /**
     * Set this entry's modification time.
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(Date time) {
        modTime = time.getTime() / MILLIS_PER_SECOND;
    }

    /**
     * Set this entry's modification time.
     *
     * @return time This entry's new modification time.
     */
    public Date getModTime() {
        return new Date(modTime * MILLIS_PER_SECOND);
    }

    /** {@inheritDoc} */
    public Date getLastModifiedDate() {
        return getModTime();
    }

    /**
     * Get this entry's file.
     *
     * @return This entry's file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get this entry's mode.
     *
     * @return This entry's mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get this entry's file size.
     *
     * @return This entry's file size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Set this entry's file size.
     *
     * @param size This entry's new file size.
     * @throws IllegalArgumentException if the size is < 0
     * or > {@link TarConstants#MAXSIZE} (077777777777L).
     */
    public void setSize(long size) {
        if (size > MAXSIZE || size < 0){
            throw new IllegalArgumentException("Size is out of range: "+size);
        }
        this.size = size;
    }

    /**
     * Indicates in case of a sparse file if an extension sparse header
     * follows.
     *
     * @return true if an extension sparse header follows.
     */
    public boolean isExtended() {
        return isExtended;
    }

    /**
     * Get this entry's real file size in case of a sparse file.
     *
     * @return This entry's real file size.
     */
    public long getRealSize() {
        return realSize;
    }

    /**
     * Indicate if this entry is a GNU sparse block 
     *
     * @return true if this is a sparse extension provided by GNU tar
     */
    public boolean isGNUSparse() {
        return linkFlag == LF_GNUTYPE_SPARSE;
    }

    /**
     * Indicate if this entry is a GNU long name block
     *
     * @return true if this is a long name extension provided by GNU tar
     */
    public boolean isGNULongNameEntry() {
        return linkFlag == LF_GNUTYPE_LONGNAME
            && name.toString().equals(GNU_LONGLINK);
    }

    /**
     * Check if this is a Pax header.
     * 
     * @return <code>true</code> if this is a Pax header.
     * 
     * @since Apache Commons Compress 1.1
     */
    public boolean isPaxHeader(){
        return linkFlag == LF_PAX_EXTENDED_HEADER_LC
            || linkFlag == LF_PAX_EXTENDED_HEADER_UC;
    }

    /**
     * Check if this is a Pax header.
     * 
     * @return <code>true</code> if this is a Pax header.
     * 
     * @since Apache Commons Compress 1.1
     */
    public boolean isGlobalPaxHeader(){
        return linkFlag == LF_PAX_GLOBAL_EXTENDED_HEADER;
    }

    /**
     * Return whether or not this entry represents a directory.
     *
     * @return True if this entry is a directory.
     */
    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        if (getName().endsWith("/")) {
            return true;
        }

        return false;
    }

    /**
     * Check if this is a "normal file"
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isFile() {
        if (file != null) {
            return file.isFile();
        }
        if (linkFlag == LF_OLDNORM || linkFlag == LF_NORMAL) {
            return true;
        }
        return !getName().endsWith("/");
    }

    /**
     * Check if this is a symbolic link entry.
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isSymbolicLink() {
        return linkFlag == LF_SYMLINK;
    }

    /**
     * Check if this is a link entry.
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isLink() {
        return linkFlag == LF_LINK;
    }

    /**
     * Check if this is a character device entry.
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isCharacterDevice() {
        return linkFlag == LF_CHR;
    }

    /**
     * Check if this is a block device entry.
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isBlockDevice() {
        return linkFlag == LF_BLK;
    }

    /**
     * Check if this is a FIFO (pipe) entry.
     *
     * @since Apache Commons Compress 1.2
     */
    public boolean isFIFO() {
        return linkFlag == LF_FIFO;
    }

    /**
     * If this entry represents a file, and the file is a directory, return
     * an array of TarEntries for this entry's children.
     *
     * @return An array of TarEntry's for this entry's children.
     */
    public TarArchiveEntry[] getDirectoryEntries() {
        if (file == null || !file.isDirectory()) {
            return new TarArchiveEntry[0];
        }

        String[]   list = file.list();
        TarArchiveEntry[] result = new TarArchiveEntry[list.length];

        for (int i = 0; i < list.length; ++i) {
            result[i] = new TarArchiveEntry(new File(file, list[i]));
        }

        return result;
    }

    /**
     * Write an entry's header information to a header buffer.
     *
     * @param outbuf The tar entry header buffer to fill in.
     */
    public void writeEntryHeader(byte[] outbuf) {
        int offset = 0;

        offset = TarUtils.formatNameBytes(name, outbuf, offset, NAMELEN);
        offset = TarUtils.formatOctalBytes(mode, outbuf, offset, MODELEN);
        offset = TarUtils.formatOctalBytes(userId, outbuf, offset, UIDLEN);
        offset = TarUtils.formatOctalBytes(groupId, outbuf, offset, GIDLEN);
        offset = TarUtils.formatLongOctalBytes(size, outbuf, offset, SIZELEN);
        offset = TarUtils.formatLongOctalBytes(modTime, outbuf, offset, MODTIMELEN);

        int csOffset = offset;

        for (int c = 0; c < CHKSUMLEN; ++c) {
            outbuf[offset++] = (byte) ' ';
        }

        outbuf[offset++] = linkFlag;
        offset = TarUtils.formatNameBytes(linkName, outbuf, offset, NAMELEN);
        offset = TarUtils.formatNameBytes(magic, outbuf, offset, MAGICLEN);
        offset = TarUtils.formatNameBytes(version, outbuf, offset, VERSIONLEN);
        offset = TarUtils.formatNameBytes(userName, outbuf, offset, UNAMELEN);
        offset = TarUtils.formatNameBytes(groupName, outbuf, offset, GNAMELEN);
        offset = TarUtils.formatOctalBytes(devMajor, outbuf, offset, DEVLEN);
        offset = TarUtils.formatOctalBytes(devMinor, outbuf, offset, DEVLEN);

        while (offset < outbuf.length) {
            outbuf[offset++] = 0;
        }

        long chk = TarUtils.computeCheckSum(outbuf);

        TarUtils.formatCheckSumOctalBytes(chk, outbuf, csOffset, CHKSUMLEN);
    }

    /**
     * Parse an entry's header information from a header buffer.
     *
     * @param header The tar entry header buffer to get information from.
     */
    public void parseTarHeader(byte[] header) {
        int offset = 0;

        name = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        mode = (int) TarUtils.parseOctal(header, offset, MODELEN);
        offset += MODELEN;
        userId = (int) TarUtils.parseOctal(header, offset, UIDLEN);
        offset += UIDLEN;
        groupId = (int) TarUtils.parseOctal(header, offset, GIDLEN);
        offset += GIDLEN;
        size = TarUtils.parseOctal(header, offset, SIZELEN);
        offset += SIZELEN;
        modTime = TarUtils.parseOctal(header, offset, MODTIMELEN);
        offset += MODTIMELEN;
        offset += CHKSUMLEN;
        linkFlag = header[offset++];
        linkName = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        magic = TarUtils.parseName(header, offset, MAGICLEN);
        offset += MAGICLEN;
        version = TarUtils.parseName(header, offset, VERSIONLEN);
        offset += VERSIONLEN;
        userName = TarUtils.parseName(header, offset, UNAMELEN);
        offset += UNAMELEN;
        groupName = TarUtils.parseName(header, offset, GNAMELEN);
        offset += GNAMELEN;
        devMajor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
        offset += DEVLEN;
        devMinor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
        offset += DEVLEN;

        int type = evaluateType(header);
        switch (type) {
        case FORMAT_OLDGNU: {
            offset += ATIMELEN_GNU;
            offset += CTIMELEN_GNU;
            offset += OFFSETLEN_GNU;
            offset += LONGNAMESLEN_GNU;
            offset += PAD2LEN_GNU;
            offset += SPARSELEN_GNU;
            isExtended = TarUtils.parseBoolean(header, offset);
            offset += ISEXTENDEDLEN_GNU;
            realSize = TarUtils.parseOctal(header, offset, REALSIZELEN_GNU);
            offset += REALSIZELEN_GNU;
            break;
        }
        case FORMAT_POSIX:
        default: {
            String prefix = TarUtils.parseName(header, offset, PREFIXLEN);
            // SunOS tar -E does not add / to directory names, so fix
            // up to be consistent
            if (isDirectory() && !name.endsWith("/")){
                name = name + "/";
            }
            if (prefix.length() > 0){
                name = prefix + "/" + name;
            }
        }
        }
    }

    /**
     * Strips Windows' drive letter as well as any leading slashes,
     * turns path separators into forward slahes.
     */
    private static String normalizeFileName(String fileName,
                                            boolean preserveLeadingSlashes) {
        String osname = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (osname != null) {

            // Strip off drive letters!
            // REVIEW Would a better check be "(File.separator == '\')"?

            if (osname.startsWith("windows")) {
                if (fileName.length() > 2) {
                    char ch1 = fileName.charAt(0);
                    char ch2 = fileName.charAt(1);

                    if (ch2 == ':'
                        && ((ch1 >= 'a' && ch1 <= 'z')
                            || (ch1 >= 'A' && ch1 <= 'Z'))) {
                        fileName = fileName.substring(2);
                    }
                }
            } else if (osname.indexOf("netware") > -1) {
                int colon = fileName.indexOf(':');
                if (colon != -1) {
                    fileName = fileName.substring(colon + 1);
                }
            }
        }

        fileName = fileName.replace(File.separatorChar, '/');

        // No absolute pathnames
        // Windows (and Posix?) paths can start with "\\NetworkDrive\",
        // so we loop on starting /'s.
        while (!preserveLeadingSlashes && fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    /**
     * Evaluate an entry's header format from a header buffer.
     *
     * @param header The tar entry header buffer to evaluate the format for.
     * @return format type
     */
    private int evaluateType(byte[] header) {
        final ByteBuffer magic = ByteBuffer.wrap(header, MAGIC_OFFSET, MAGICLEN);
        if (magic.compareTo(ByteBuffer.wrap(MAGIC_GNU.getBytes())) == 0)
            return FORMAT_OLDGNU;
        if (magic.compareTo(ByteBuffer.wrap(MAGIC_POSIX.getBytes())) == 0)
            return FORMAT_POSIX;
        return 0;
    }
}

