/*
 * Copyright (C) 2010 The Android Open Source Project
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
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.repository.RepoConstants;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Performs the work of installing a given {@link Archive}.
 */
public class ArchiveInstaller {

    public static final int NUM_MONITOR_INC = 100;

    /** The current {@link FileOp} to use. Never null. */
    private final IFileOp mFileOp;

    /**
     * Generates an {@link ArchiveInstaller} that relies on the default {@link FileOp}.
     */
    public ArchiveInstaller() {
        mFileOp = new FileOp();
    }

    /**
     * Generates an {@link ArchiveInstaller} that relies on the given {@link FileOp}.
     *
     * @param fileUtils An alternate version of {@link FileOp} to use for file operations.
     */
    protected ArchiveInstaller(IFileOp fileUtils) {
        mFileOp = fileUtils;
    }

    /** Returns current {@link FileOp} to use. Never null. */
    protected IFileOp getFileOp() {
        return mFileOp;
    }

    /**
     * Install this {@link ArchiveReplacement}s.
     * A "replacement" is composed of the actual new archive to install
     * (c.f. {@link ArchiveReplacement#getNewArchive()} and an <em>optional</em>
     * archive being replaced (c.f. {@link ArchiveReplacement#getReplaced()}.
     * In the case of a new install, the later should be null.
     * <p/>
     * The new archive to install will be skipped if it is incompatible.
     *
     * @return True if the archive was installed, false otherwise.
     */
    public boolean install(ArchiveReplacement archiveInfo,
            String osSdkRoot,
            boolean forceHttp,
            SdkManager sdkManager,
            ITaskMonitor monitor) {

        Archive newArchive = archiveInfo.getNewArchive();
        Package pkg = newArchive.getParentPackage();

        File archiveFile = null;
        String name = pkg.getShortDescription();

        if (pkg instanceof ExtraPackage && !((ExtraPackage) pkg).isPathValid()) {
            monitor.log("Skipping %1$s: %2$s is not a valid install path.",
                    name,
                    ((ExtraPackage) pkg).getPath());
            return false;
        }

        if (newArchive.isLocal()) {
            // This should never happen.
            monitor.log("Skipping already installed archive: %1$s for %2$s",
                    name,
                    newArchive.getOsDescription());
            return false;
        }

        if (!newArchive.isCompatible()) {
            monitor.log("Skipping incompatible archive: %1$s for %2$s",
                    name,
                    newArchive.getOsDescription());
            return false;
        }

        archiveFile = downloadFile(newArchive, osSdkRoot, monitor, forceHttp);
        if (archiveFile != null) {
            // Unarchive calls the pre/postInstallHook methods.
            if (unarchive(archiveInfo, osSdkRoot, archiveFile, sdkManager, monitor)) {
                monitor.log("Installed %1$s", name);
                // Delete the temp archive if it exists, only on success
                mFileOp.deleteFileOrFolder(archiveFile);
                return true;
            }
        }

        return false;
    }

    /**
     * Downloads an archive and returns the temp file with it.
     * Caller is responsible with deleting the temp file when done.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected File downloadFile(Archive archive,
            String osSdkRoot,
            ITaskMonitor monitor,
            boolean forceHttp) {

        String pkgName = archive.getParentPackage().getShortDescription();
        monitor.setDescription("Downloading %1$s", pkgName);
        monitor.log("Downloading %1$s", pkgName);

        String link = archive.getUrl();
        if (!link.startsWith("http://")                          //$NON-NLS-1$
                && !link.startsWith("https://")                  //$NON-NLS-1$
                && !link.startsWith("ftp://")) {                 //$NON-NLS-1$
            // Make the URL absolute by prepending the source
            Package pkg = archive.getParentPackage();
            SdkSource src = pkg.getParentSource();
            if (src == null) {
                monitor.logError("Internal error: no source for archive %1$s", pkgName);
                return null;
            }

            // take the URL to the repository.xml and remove the last component
            // to get the base
            String repoXml = src.getUrl();
            int pos = repoXml.lastIndexOf('/');
            String base = repoXml.substring(0, pos + 1);

            link = base + link;
        }

        if (forceHttp) {
            link = link.replaceAll("https://", "http://");  //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Get the basename of the file we're downloading, i.e. the last component
        // of the URL
        int pos = link.lastIndexOf('/');
        String base = link.substring(pos + 1);

        // Rather than create a real temp file in the system, we simply use our
        // temp folder (in the SDK base folder) and use the archive name for the
        // download. This allows us to reuse or continue downloads.

        File tmpFolder = getTempFolder(osSdkRoot);
        if (!mFileOp.isDirectory(tmpFolder)) {
            if (mFileOp.isFile(tmpFolder)) {
                mFileOp.deleteFileOrFolder(tmpFolder);
            }
            if (!mFileOp.mkdirs(tmpFolder)) {
                monitor.logError("Failed to create directory %1$s", tmpFolder.getPath());
                return null;
            }
        }
        File tmpFile = new File(tmpFolder, base);

        // if the file exists, check its checksum & size. Use it if complete
        if (mFileOp.exists(tmpFile)) {
            if (mFileOp.length(tmpFile) == archive.getSize()) {
                String chksum = "";                             //$NON-NLS-1$
                try {
                    chksum = fileChecksum(archive.getChecksumType().getMessageDigest(),
                                          tmpFile,
                                          monitor);
                } catch (NoSuchAlgorithmException e) {
                    // Ignore.
                }
                if (chksum.equalsIgnoreCase(archive.getChecksum())) {
                    // File is good, let's use it.
                    return tmpFile;
                }
            }

            // Existing file is either of different size or content.
            // TODO: continue download when we support continue mode.
            // Right now, let's simply remove the file and start over.
            mFileOp.deleteFileOrFolder(tmpFile);
        }

        if (fetchUrl(archive, tmpFile, link, pkgName, monitor)) {
            // Fetching was successful, let's use this file.
            return tmpFile;
        } else {
            // Delete the temp file if we aborted the download
            // TODO: disable this when we want to support partial downloads.
            mFileOp.deleteFileOrFolder(tmpFile);
            return null;
        }
    }

    /**
     * Computes the SHA-1 checksum of the content of the given file.
     * Returns an empty string on error (rather than null).
     */
    private String fileChecksum(MessageDigest digester, File tmpFile, ITaskMonitor monitor) {
        InputStream is = null;
        try {
            is = new FileInputStream(tmpFile);

            byte[] buf = new byte[65536];
            int n;

            while ((n = is.read(buf)) >= 0) {
                if (n > 0) {
                    digester.update(buf, 0, n);
                }
            }

            return getDigestChecksum(digester);

        } catch (FileNotFoundException e) {
            // The FNF message is just the URL. Make it a bit more useful.
            monitor.logError("File not found: %1$s", e.getMessage());

        } catch (Exception e) {
            monitor.logError("%1$s", e.getMessage());   //$NON-NLS-1$

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }

        return "";  //$NON-NLS-1$
    }

    /**
     * Returns the SHA-1 from a {@link MessageDigest} as an hex string
     * that can be compared with {@link Archive#getChecksum()}.
     */
    private String getDigestChecksum(MessageDigest digester) {
        int n;
        // Create an hex string from the digest
        byte[] digest = digester.digest();
        n = digest.length;
        String hex = "0123456789abcdef";                     //$NON-NLS-1$
        char[] hexDigest = new char[n * 2];
        for (int i = 0; i < n; i++) {
            int b = digest[i] & 0x0FF;
            hexDigest[i*2 + 0] = hex.charAt(b >>> 4);
            hexDigest[i*2 + 1] = hex.charAt(b & 0x0f);
        }

        return new String(hexDigest);
    }

    /**
     * Actually performs the download.
     * Also computes the SHA1 of the file on the fly.
     * <p/>
     * Success is defined as downloading as many bytes as was expected and having the same
     * SHA1 as expected. Returns true on success or false if any of those checks fail.
     * <p/>
     * Increments the monitor by {@link #NUM_MONITOR_INC}.
     */
    private boolean fetchUrl(Archive archive,
            File tmpFile,
            String urlString,
            String pkgName,
            ITaskMonitor monitor) {

        FileOutputStream os = null;
        InputStream is = null;
        try {
            is = UrlOpener.openUrl(urlString, monitor);
            os = new FileOutputStream(tmpFile);

            MessageDigest digester = archive.getChecksumType().getMessageDigest();

            byte[] buf = new byte[65536];
            int n;

            long total = 0;
            long size = archive.getSize();
            long inc = size / NUM_MONITOR_INC;
            long next_inc = inc;

            long startMs = System.currentTimeMillis();
            long nextMs = startMs + 2000;  // start update after 2 seconds

            while ((n = is.read(buf)) >= 0) {
                if (n > 0) {
                    os.write(buf, 0, n);
                    digester.update(buf, 0, n);
                }

                long timeMs = System.currentTimeMillis();

                total += n;
                if (total >= next_inc) {
                    monitor.incProgress(1);
                    next_inc += inc;
                }

                if (timeMs > nextMs) {
                    long delta = timeMs - startMs;
                    if (total > 0 && delta > 0) {
                        // percent left to download
                        int percent = (int) (100 * total / size);
                        // speed in KiB/s
                        float speed = (float)total / (float)delta * (1000.f / 1024.f);
                        // time left to download the rest at the current KiB/s rate
                        int timeLeft = (speed > 1e-3) ?
                                               (int)(((size - total) / 1024.0f) / speed) :
                                               0;
                        String timeUnit = "seconds";
                        if (timeLeft > 120) {
                            timeUnit = "minutes";
                            timeLeft /= 60;
                        }

                        monitor.setDescription(
                                "Downloading %1$s (%2$d%%, %3$.0f KiB/s, %4$d %5$s left)",
                                pkgName,
                                percent,
                                speed,
                                timeLeft,
                                timeUnit);
                    }
                    nextMs = timeMs + 1000;  // update every second
                }

                if (monitor.isCancelRequested()) {
                    monitor.log("Download aborted by user at %1$d bytes.", total);
                    return false;
                }

            }

            if (total != size) {
                monitor.logError(
                        "Download finished with wrong size. Expected %1$d bytes, got %2$d bytes.",
                        size, total);
                return false;
            }

            // Create an hex string from the digest
            String actual   = getDigestChecksum(digester);
            String expected = archive.getChecksum();
            if (!actual.equalsIgnoreCase(expected)) {
                monitor.logError("Download finished with wrong checksum. Expected %1$s, got %2$s.",
                        expected, actual);
                return false;
            }

            return true;

        } catch (FileNotFoundException e) {
            // The FNF message is just the URL. Make it a bit more useful.
            monitor.logError("File not found: %1$s", e.getMessage());

        } catch (Exception e) {
            monitor.logError("%1$s", e.getMessage());   //$NON-NLS-1$

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // pass
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }

        return false;
    }

    /**
     * Install the given archive in the given folder.
     */
    private boolean unarchive(ArchiveReplacement archiveInfo,
            String osSdkRoot,
            File archiveFile,
            SdkManager sdkManager,
            ITaskMonitor monitor) {
        boolean success = false;
        Archive newArchive = archiveInfo.getNewArchive();
        Package pkg = newArchive.getParentPackage();
        String pkgName = pkg.getShortDescription();
        monitor.setDescription("Installing %1$s", pkgName);
        monitor.log("Installing %1$s", pkgName);

        // Ideally we want to always unzip in a temp folder which name depends on the package
        // type (e.g. addon, tools, etc.) and then move the folder to the destination folder.
        // If the destination folder exists, it will be renamed and deleted at the very
        // end if everything succeeded. This provides a nice atomic swap and should leave the
        // original folder untouched in case something wrong (e.g. program crash) in the
        // middle of the unzip operation.
        //
        // However that doesn't work on Windows, we always end up not being able to move the
        // new folder. There are actually 2 cases:
        // A- A process such as a the explorer is locking the *old* folder or a file inside
        //    (e.g. adb.exe)
        //    In this case we really shouldn't be tried to work around it and we need to let
        //    the user know and let it close apps that access that folder.
        // B- A process is locking the *new* folder. Very often this turns to be a file indexer
        //    or an anti-virus that is busy scanning the new folder that we just unzipped.
        //
        // So we're going to change the strategy:
        // 1- Try to move the old folder to a temp/old folder. This might fail in case of issue A.
        //    Note: for platform-tools, we can try killing adb first.
        //    If it still fails, we do nothing and ask the user to terminate apps that can be
        //    locking that folder.
        // 2- Once the old folder is out of the way, we unzip the archive directly into the
        //    optimal new location. We no longer unzip it in a temp folder and move it since we
        //    know that's what fails in most of the cases.
        // 3- If the unzip fails, remove everything and try to restore the old folder by doing
        //    a *copy* in place and not a folder move (which will likely fail too).

        String pkgKind = pkg.getClass().getSimpleName();

        File destFolder = null;
        File oldDestFolder = null;

        try {
            // -0- Compute destination directory and check install pre-conditions

            destFolder = pkg.getInstallFolder(osSdkRoot, sdkManager);

            if (destFolder == null) {
                // this should not seriously happen.
                monitor.log("Failed to compute installation directory for %1$s.", pkgName);
                return false;
            }

            if (!pkg.preInstallHook(newArchive, monitor, osSdkRoot, destFolder)) {
                monitor.log("Skipping archive: %1$s", pkgName);
                return false;
            }

            // -1- move old folder.

            if (mFileOp.exists(destFolder)) {
                // Create a new temp/old dir
                if (oldDestFolder == null) {
                    oldDestFolder = getNewTempFolder(osSdkRoot, pkgKind, "old");  //$NON-NLS-1$
                }
                if (oldDestFolder == null) {
                    // this should not seriously happen.
                    monitor.logError("Failed to find a temp directory in %1$s.", osSdkRoot);
                    return false;
                }

                // Try to move the current dest dir to the temp/old one. Tell the user if it failed.
                while(true) {
                    if (!moveFolder(destFolder, oldDestFolder)) {
                        monitor.logError("Failed to rename directory %1$s to %2$s.",
                                destFolder.getPath(), oldDestFolder.getPath());

                        if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
                            String msg = String.format(
                                    "-= Warning ! =-\n" +
                                    "A folder failed to be moved. On Windows this " +
                                    "typically means that a program is using that folder (for " +
                                    "example Windows Explorer or your anti-virus software.)\n" +
                                    "Please momentarily deactivate your anti-virus software or " +
                                    "close any running programs that may be accessing the " +
                                    "directory '%1$s'.\n" +
                                    "When ready, press YES to try again.",
                                    destFolder.getPath());

                            if (monitor.displayPrompt("SDK Manager: failed to install", msg)) {
                                // loop, trying to rename the temp dir into the destination
                                continue;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                }
            }

            assert !mFileOp.exists(destFolder);

            // -2- Unzip new content directly in place.

            if (!mFileOp.mkdirs(destFolder)) {
                monitor.logError("Failed to create directory %1$s", destFolder.getPath());
                return false;
            }

            if (!unzipFolder(archiveFile, newArchive.getSize(), destFolder, pkgName, monitor)) {
                return false;
            }

            if (!generateSourceProperties(newArchive, destFolder)) {
                monitor.logError("Failed to generate source.properties in directory %1$s",
                        destFolder.getPath());
                return false;
            }

            // In case of success, if we were replacing an archive
            // and the older one had a different path, remove it now.
            Archive oldArchive = archiveInfo.getReplaced();
            if (oldArchive != null && oldArchive.isLocal()) {
                String oldPath = oldArchive.getLocalOsPath();
                File oldFolder = oldPath == null ? null : new File(oldPath);
                if (oldFolder == null && oldArchive.getParentPackage() != null) {
                    oldFolder = oldArchive.getParentPackage().getInstallFolder(
                            osSdkRoot, sdkManager);
                }
                if (oldFolder != null && mFileOp.exists(oldFolder) &&
                        !oldFolder.equals(destFolder)) {
                    monitor.logVerbose("Removing old archive at %1$s", oldFolder.getAbsolutePath());
                    mFileOp.deleteFileOrFolder(oldFolder);
                }
            }

            success = true;
            pkg.postInstallHook(newArchive, monitor, destFolder);
            return true;

        } finally {
            if (!success) {
                // In case of failure, we try to restore the old folder content.
                if (oldDestFolder != null) {
                    restoreFolder(oldDestFolder, destFolder);
                }

                // We also call the postInstallHool with a null directory to give a chance
                // to the archive to cleanup after preInstallHook.
                pkg.postInstallHook(newArchive, monitor, null /*installDir*/);
            }

            // Cleanup if the unzip folder is still set.
            mFileOp.deleteFileOrFolder(oldDestFolder);
        }
    }

    /**
     * Tries to rename/move a folder.
     * <p/>
     * Contract:
     * <ul>
     * <li> When we start, oldDir must exist and be a directory. newDir must not exist. </li>
     * <li> On successful completion, oldDir must not exists.
     *      newDir must exist and have the same content. </li>
     * <li> On failure completion, oldDir must have the same content as before.
     *      newDir must not exist. </li>
     * </ul>
     * <p/>
     * The simple "rename" operation on a folder can typically fail on Windows for a variety
     * of reason, in fact as soon as a single process holds a reference on a directory. The
     * most common case are the Explorer, the system's file indexer, Tortoise SVN cache or
     * an anti-virus that are busy indexing a new directory having been created.
     *
     * @param oldDir The old location to move. It must exist and be a directory.
     * @param newDir The new location where to move. It must not exist.
     * @return True if the move succeeded. On failure, we try hard to not have touched the old
     *  directory in order not to loose its content.
     */
    private boolean moveFolder(File oldDir, File newDir) {
        // This is a simple folder rename that works on Linux/Mac all the time.
        //
        // On Windows this might fail if an indexer is busy looking at a new directory
        // (e.g. right after we unzip our archive), so it fails let's be nice and give
        // it a bit of time to succeed.
        for (int i = 0; i < 5; i++) {
            if (mFileOp.renameTo(oldDir, newDir)) {
                return true;
            }
            try {
                Thread.sleep(500 /*ms*/);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        return false;
    }

    /**
     * Unzips a zip file into the given destination directory.
     *
     * The archive file MUST have a unique "root" folder.
     * This root folder is skipped when unarchiving.
     */
    @SuppressWarnings("unchecked")
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected boolean unzipFolder(File archiveFile,
            long compressedSize,
            File unzipDestFolder,
            String pkgName,
            ITaskMonitor monitor) {

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archiveFile);

            // figure if we'll need to set the unix permissions
            boolean usingUnixPerm =
                    SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_DARWIN ||
                    SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_LINUX;

            // To advance the percent and the progress bar, we don't know the number of
            // items left to unzip. However we know the size of the archive and the size of
            // each uncompressed item. The zip file format overhead is negligible so that's
            // a good approximation.
            long incStep = compressedSize / NUM_MONITOR_INC;
            long incTotal = 0;
            long incCurr = 0;
            int lastPercent = 0;

            byte[] buf = new byte[65536];

            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                String name = entry.getName();

                // ZipFile entries should have forward slashes, but not all Zip
                // implementations can be expected to do that.
                name = name.replace('\\', '/');

                // Zip entries are always packages in a top-level directory
                // (e.g. docs/index.html). However we want to use our top-level
                // directory so we drop the first segment of the path name.
                int pos = name.indexOf('/');
                if (pos < 0 || pos == name.length() - 1) {
                    continue;
                } else {
                    name = name.substring(pos + 1);
                }

                File destFile = new File(unzipDestFolder, name);

                if (name.endsWith("/")) {  //$NON-NLS-1$
                    // Create directory if it doesn't exist yet. This allows us to create
                    // empty directories.
                    if (!mFileOp.isDirectory(destFile) && !mFileOp.mkdirs(destFile)) {
                        monitor.logError("Failed to create directory %1$s",
                                destFile.getPath());
                        return false;
                    }
                    continue;
                } else if (name.indexOf('/') != -1) {
                    // Otherwise it's a file in a sub-directory.
                    // Make sure the parent directory has been created.
                    File parentDir = destFile.getParentFile();
                    if (!mFileOp.isDirectory(parentDir)) {
                        if (!mFileOp.mkdirs(parentDir)) {
                            monitor.logError("Failed to create directory %1$s",
                                    parentDir.getPath());
                            return false;
                        }
                    }
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(destFile);
                    int n;
                    InputStream entryContent = zipFile.getInputStream(entry);
                    while ((n = entryContent.read(buf)) != -1) {
                        if (n > 0) {
                            fos.write(buf, 0, n);
                        }
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }

                // if needed set the permissions.
                if (usingUnixPerm && mFileOp.isFile(destFile)) {
                    // get the mode and test if it contains the executable bit
                    int mode = entry.getUnixMode();
                    if ((mode & 0111) != 0) {
                        mFileOp.setExecutablePermission(destFile);
                    }
                }

                // Increment progress bar to match. We update only between files.
                for(incTotal += entry.getCompressedSize(); incCurr < incTotal; incCurr += incStep) {
                    monitor.incProgress(1);
                }

                int percent = (int) (100 * incTotal / compressedSize);
                if (percent != lastPercent) {
                    monitor.setDescription("Unzipping %1$s (%2$d%%)", pkgName, percent);
                    lastPercent = percent;
                }

                if (monitor.isCancelRequested()) {
                    return false;
                }
            }

            return true;

        } catch (IOException e) {
            monitor.logError("Unzip failed: %1$s", e.getMessage());

        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }

        return false;
    }

    /**
     * Returns an unused temp folder path in the form of osBasePath/temp/prefix.suffixNNN.
     * <p/>
     * This does not actually <em>create</em> the folder. It just scan the base path for
     * a free folder name to use and returns the file to use to reference it.
     * <p/>
     * This operation is not atomic so there's no guarantee the folder can't get
     * created in between. This is however unlikely and the caller can assume the
     * returned folder does not exist yet.
     * <p/>
     * Returns null if no such folder can be found (e.g. if all candidates exist,
     * which is rather unlikely) or if the base temp folder cannot be created.
     */
    private File getNewTempFolder(String osBasePath, String prefix, String suffix) {
        File baseTempFolder = getTempFolder(osBasePath);

        if (!mFileOp.isDirectory(baseTempFolder)) {
            if (mFileOp.isFile(baseTempFolder)) {
                mFileOp.deleteFileOrFolder(baseTempFolder);
            }
            if (!mFileOp.mkdirs(baseTempFolder)) {
                return null;
            }
        }

        for (int i = 1; i < 100; i++) {
            File folder = new File(baseTempFolder,
                    String.format("%1$s.%2$s%3$02d", prefix, suffix, i));  //$NON-NLS-1$
            if (!mFileOp.exists(folder)) {
                return folder;
            }
        }
        return null;
    }

    /**
     * Returns the single fixed "temp" folder used by the SDK Manager.
     * This folder is always at osBasePath/temp.
     * <p/>
     * This does not actually <em>create</em> the folder.
     */
    private File getTempFolder(String osBasePath) {
        File baseTempFolder = new File(osBasePath, RepoConstants.FD_TEMP);
        return baseTempFolder;
    }

    /**
     * Generates a source.properties in the destination folder that contains all the infos
     * relevant to this archive, this package and the source so that we can reload them
     * locally later.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected boolean generateSourceProperties(Archive archive, File unzipDestFolder) {
        Properties props = new Properties();

        archive.saveProperties(props);

        Package pkg = archive.getParentPackage();
        if (pkg != null) {
            pkg.saveProperties(props);
        }

        OutputStream fos = null;
        try {
            File f = new File(unzipDestFolder, SdkConstants.FN_SOURCE_PROP);

            fos = mFileOp.newFileOutputStream(f);

            props.store(fos, "## Android Tool: Source of this archive.");  //$NON-NLS-1$

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

        return false;
    }

    /**
     * Recursively restore srcFolder into destFolder by performing a copy of the file
     * content rather than rename/moves.
     *
     * @param srcFolder The source folder to restore.
     * @param destFolder The destination folder where to restore.
     * @return True if the folder was successfully restored, false if it was not at all or
     *         only partially restored.
     */
    private boolean restoreFolder(File srcFolder, File destFolder) {
        boolean result = true;

        // Process sub-folders first
        File[] srcFiles = mFileOp.listFiles(srcFolder);
        if (srcFiles == null) {
            // Source does not exist. That is quite odd.
            return false;
        }

        if (mFileOp.isFile(destFolder)) {
            if (!mFileOp.delete(destFolder)) {
                // There's already a file in there where we want a directory and
                // we can't delete it. This is rather unexpected. Just give up on
                // that folder.
                return false;
            }
        } else if (!mFileOp.isDirectory(destFolder)) {
            mFileOp.mkdirs(destFolder);
        }

        // Get all the files and dirs of the current destination.
        // We are not going to clean up the destination first.
        // Instead we'll copy over and just remove any remaining files or directories.
        Set<File> destDirs = new HashSet<File>();
        Set<File> destFiles = new HashSet<File>();
        File[] files = mFileOp.listFiles(destFolder);
        if (files != null) {
            for (File f : files) {
                if (mFileOp.isDirectory(f)) {
                    destDirs.add(f);
                } else {
                    destFiles.add(f);
                }
            }
        }

        // First restore all source directories.
        for (File dir : srcFiles) {
            if (mFileOp.isDirectory(dir)) {
                File d = new File(destFolder, dir.getName());
                destDirs.remove(d);
                if (!restoreFolder(dir, d)) {
                    result = false;
                }
            }
        }

        // Remove any remaining directories not processed above.
        for (File dir : destDirs) {
            mFileOp.deleteFileOrFolder(dir);
        }

        // Copy any source files over to the destination.
        for (File file : srcFiles) {
            if (mFileOp.isFile(file)) {
                File f = new File(destFolder, file.getName());
                destFiles.remove(f);
                try {
                    mFileOp.copyFile(file, f);
                } catch (IOException e) {
                    result = false;
                }
            }
        }

        // Remove any remaining files not processed above.
        for (File file : destFiles) {
            mFileOp.deleteFileOrFolder(file);
        }

        return result;
    }
}
