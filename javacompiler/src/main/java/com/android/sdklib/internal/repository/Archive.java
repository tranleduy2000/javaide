/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.repository;

import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.sdklib.io.FileOp;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


/**
 * A {@link Archive} is the base class for "something" that can be downloaded from
 * the SDK repository.
 * <p/>
 * A package has some attributes (revision, description) and a list of archives
 * which represent the downloadable bits.
 * <p/>
 * Packages are offered by a {@link SdkSource} (a download site).
 * The {@link ArchiveInstaller} takes care of downloading, unpacking and installing an archive.
 */
public class Archive implements IDescription, Comparable<Archive> {

    private static final String PROP_OS   = "Archive.Os";       //$NON-NLS-1$
    private static final String PROP_ARCH = "Archive.Arch";     //$NON-NLS-1$

    /** The checksum type. */
    public enum ChecksumType {
        /** A SHA1 checksum, represented as a 40-hex string. */
        SHA1("SHA-1");  //$NON-NLS-1$

        private final String mAlgorithmName;

        /**
         * Constructs a {@link ChecksumType} with the algorigth name
         * suitable for {@link MessageDigest#getInstance(String)}.
         * <p/>
         * These names are officially documented at
         * http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames.html#MessageDigest
         */
        private ChecksumType(String algorithmName) {
            mAlgorithmName = algorithmName;
        }

        /**
         * Returns a new {@link MessageDigest} instance for this checksum type.
         * @throws NoSuchAlgorithmException if this algorithm is not available.
         */
        public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(mAlgorithmName);
        }
    }

    /** The OS that this archive can be downloaded on. */
    public enum Os {
        ANY("Any"),
        LINUX("Linux"),
        MACOSX("MacOS X"),
        WINDOWS("Windows");

        private final String mUiName;

        private Os(String uiName) {
            mUiName = uiName;
        }

        /** Returns the UI name of the OS. */
        public String getUiName() {
            return mUiName;
        }

        /** Returns the XML name of the OS. */
        public String getXmlName() {
            return toString().toLowerCase();
        }

        /**
         * Returns the current OS as one of the {@link Os} enum values or null.
         */
        public static Os getCurrentOs() {
            String os = System.getProperty("os.name");          //$NON-NLS-1$
            if (os.startsWith("Mac")) {                         //$NON-NLS-1$
                return Os.MACOSX;

            } else if (os.startsWith("Windows")) {              //$NON-NLS-1$
                return Os.WINDOWS;

            } else if (os.startsWith("Linux")) {                //$NON-NLS-1$
                return Os.LINUX;
            }

            return null;
        }

        /** Returns true if this OS is compatible with the current one. */
        public boolean isCompatible() {
            if (this == ANY) {
                return true;
            }

            Os os = getCurrentOs();
            return this == os;
        }
    }

    /** The Architecture that this archive can be downloaded on. */
    public enum Arch {
        ANY("Any"),
        PPC("PowerPC"),
        X86("x86"),
        X86_64("x86_64");

        private final String mUiName;

        private Arch(String uiName) {
            mUiName = uiName;
        }

        /** Returns the UI name of the architecture. */
        public String getUiName() {
            return mUiName;
        }

        /** Returns the XML name of the architecture. */
        public String getXmlName() {
            return toString().toLowerCase();
        }

        /**
         * Returns the current architecture as one of the {@link Arch} enum values or null.
         */
        public static Arch getCurrentArch() {
            // Values listed from http://lopica.sourceforge.net/os.html
            String arch = System.getProperty("os.arch");

            if (arch.equalsIgnoreCase("x86_64") || arch.equalsIgnoreCase("amd64")) {
                return Arch.X86_64;

            } else if (arch.equalsIgnoreCase("x86")
                    || arch.equalsIgnoreCase("i386")
                    || arch.equalsIgnoreCase("i686")) {
                return Arch.X86;

            } else if (arch.equalsIgnoreCase("ppc") || arch.equalsIgnoreCase("PowerPC")) {
                return Arch.PPC;
            }

            return null;
        }

        /** Returns true if this architecture is compatible with the current one. */
        public boolean isCompatible() {
            if (this == ANY) {
                return true;
            }

            Arch arch = getCurrentArch();
            return this == arch;
        }
    }

    private final Os     mOs;
    private final Arch   mArch;
    private final String mUrl;
    private final long   mSize;
    private final String mChecksum;
    private final ChecksumType mChecksumType = ChecksumType.SHA1;
    private final Package mPackage;
    private final String mLocalOsPath;
    private final boolean mIsLocal;

    /**
     * Creates a new remote archive.
     */
    Archive(Package pkg, Os os, Arch arch, String url, long size, String checksum) {
        mPackage = pkg;
        mOs = os;
        mArch = arch;
        mUrl = url == null ? null : url.trim();
        mLocalOsPath = null;
        mSize = size;
        mChecksum = checksum;
        mIsLocal = false;
    }

    /**
     * Creates a new local archive.
     * Uses the properties from props first, if possible. Props can be null.
     */
    @VisibleForTesting(visibility=Visibility.PACKAGE)
    protected Archive(Package pkg, Properties props, Os os, Arch arch, String localOsPath) {
        mPackage = pkg;

        mOs   = props == null ? os   : Os.valueOf(  props.getProperty(PROP_OS,   os.toString()));
        mArch = props == null ? arch : Arch.valueOf(props.getProperty(PROP_ARCH, arch.toString()));

        mUrl = null;
        mLocalOsPath = localOsPath;
        mSize = 0;
        mChecksum = "";
        mIsLocal = localOsPath != null;
    }

    /**
     * Save the properties of the current archive in the give {@link Properties} object.
     * These properties will later be give the constructor that takes a {@link Properties} object.
     */
    void saveProperties(Properties props) {
        props.setProperty(PROP_OS,   mOs.toString());
        props.setProperty(PROP_ARCH, mArch.toString());
    }

    /**
     * Returns true if this is a locally installed archive.
     * Returns false if this is a remote archive that needs to be downloaded.
     */
    public boolean isLocal() {
        return mIsLocal;
    }

    /**
     * Returns the package that created and owns this archive.
     * It should generally not be null.
     */
    public Package getParentPackage() {
        return mPackage;
    }

    /**
     * Returns the archive size, an int > 0.
     * Size will be 0 if this a local installed folder of unknown size.
     */
    public long getSize() {
        return mSize;
    }

    /**
     * Returns the SHA1 archive checksum, as a 40-char hex.
     * Can be empty but not null for local installed folders.
     */
    public String getChecksum() {
        return mChecksum;
    }

    /**
     * Returns the checksum type, always {@link ChecksumType#SHA1} right now.
     */
    public ChecksumType getChecksumType() {
        return mChecksumType;
    }

    /**
     * Returns the download archive URL, either absolute or relative to the repository xml.
     * Always return null for a local installed folder.
     * @see #getLocalOsPath()
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Returns the local OS folder where a local archive is installed.
     * Always return null for remote archives.
     * @see #getUrl()
     */
    public String getLocalOsPath() {
        return mLocalOsPath;
    }

    /**
     * Returns the archive {@link Os} enum.
     * Can be null for a local installed folder on an unknown OS.
     */
    public Os getOs() {
        return mOs;
    }

    /**
     * Returns the archive {@link Arch} enum.
     * Can be null for a local installed folder on an unknown architecture.
     */
    public Arch getArch() {
        return mArch;
    }

    /**
     * Generates a description for this archive of the OS/Arch supported by this archive.
     */
    public String getOsDescription() {
        String os;
        if (mOs == null) {
            os = "unknown OS";
        } else if (mOs == Os.ANY) {
            os = "any OS";
        } else {
            os = mOs.getUiName();
        }

        String arch = "";                               //$NON-NLS-1$
        if (mArch != null && mArch != Arch.ANY) {
            arch = mArch.getUiName();
        }

        return String.format("%1$s%2$s%3$s",
                os,
                arch.length() > 0 ? " " : "",           //$NON-NLS-2$
                arch);
    }

    /**
     * Returns the short description of the source, if not null.
     * Otherwise returns the default Object toString result.
     * <p/>
     * This is mostly helpful for debugging.
     * For UI display, use the {@link IDescription} interface.
     */
    @Override
    public String toString() {
        String s = getShortDescription();
        if (s != null) {
            return s;
        }
        return super.toString();
    }

    /**
     * Generates a short description for this archive.
     */
    public String getShortDescription() {
        return String.format("Archive for %1$s", getOsDescription());
    }

    /**
     * Generates a longer description for this archive.
     */
    public String getLongDescription() {
        long size = getSize();
        String sizeStr;
        if (size < 1024) {
            sizeStr = String.format("%d Bytes", size);
        } else if (size < 1024 * 1024) {
            sizeStr = String.format("%d KiB", Math.round(size / 1024.0));
        } else if (size < 1024 * 1024 * 1024) {
            sizeStr = String.format("%.1f MiB",
                    Math.round(10.0 * size / (1024 * 1024.0))/ 10.0);
        } else {
            sizeStr = String.format("%.1f GiB",
                    Math.round(10.0 * size / (1024 * 1024 * 1024.0))/ 10.0);
        }

        return String.format("%1$s\nSize: %2$s\nSHA1: %3$s",
                getShortDescription(), sizeStr, getChecksum());
    }

    /**
     * Returns true if this archive can be installed on the current platform.
     */
    public boolean isCompatible() {
        return getOs().isCompatible() && getArch().isCompatible();
    }

    /**
     * Delete the archive folder if this is a local archive.
     */
    public void deleteLocal() {
        if (isLocal()) {
            new FileOp().deleteFileOrFolder(new File(getLocalOsPath()));
        }
    }

    /**
     * Archives are compared using their {@link Package} ordering.
     *
     * @see Package#compareTo(Package)
     */
    public int compareTo(Archive rhs) {
        if (mPackage != null && rhs != null) {
            return mPackage.compareTo(rhs.getParentPackage());
        }
        return 0;
    }

    /**
     * Note: An {@link Archive}'s hash code does NOT depend on the parent {@link Package} hash code.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mArch == null) ? 0 : mArch.hashCode());
        result = prime * result + ((mChecksum == null) ? 0 : mChecksum.hashCode());
        result = prime * result + ((mChecksumType == null) ? 0 : mChecksumType.hashCode());
        result = prime * result + (mIsLocal ? 1231 : 1237);
        result = prime * result + ((mLocalOsPath == null) ? 0 : mLocalOsPath.hashCode());
        result = prime * result + ((mOs == null) ? 0 : mOs.hashCode());
        result = prime * result + (int) (mSize ^ (mSize >>> 32));
        result = prime * result + ((mUrl == null) ? 0 : mUrl.hashCode());
        return result;
    }

    /**
     * Note: An {@link Archive}'s equality does NOT depend on the parent {@link Package} equality.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Archive)) {
            return false;
        }
        Archive other = (Archive) obj;
        if (mArch == null) {
            if (other.mArch != null) {
                return false;
            }
        } else if (!mArch.equals(other.mArch)) {
            return false;
        }
        if (mChecksum == null) {
            if (other.mChecksum != null) {
                return false;
            }
        } else if (!mChecksum.equals(other.mChecksum)) {
            return false;
        }
        if (mChecksumType == null) {
            if (other.mChecksumType != null) {
                return false;
            }
        } else if (!mChecksumType.equals(other.mChecksumType)) {
            return false;
        }
        if (mIsLocal != other.mIsLocal) {
            return false;
        }
        if (mLocalOsPath == null) {
            if (other.mLocalOsPath != null) {
                return false;
            }
        } else if (!mLocalOsPath.equals(other.mLocalOsPath)) {
            return false;
        }
        if (mOs == null) {
            if (other.mOs != null) {
                return false;
            }
        } else if (!mOs.equals(other.mOs)) {
            return false;
        }
        if (mSize != other.mSize) {
            return false;
        }
        if (mUrl == null) {
            if (other.mUrl != null) {
                return false;
            }
        } else if (!mUrl.equals(other.mUrl)) {
            return false;
        }
        return true;
    }
}
