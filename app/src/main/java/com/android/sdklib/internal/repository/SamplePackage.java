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

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.AndroidVersion.AndroidVersionException;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.SdkRepository;

import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a sample XML node in an SDK repository.
 */
public class SamplePackage extends MinToolsPackage
    implements IPackageVersion, IMinApiLevelDependency, IMinToolsDependency {

    private static final String PROP_MIN_API_LEVEL = "Sample.MinApiLevel";  //$NON-NLS-1$

    /** The matching platform version. */
    private final AndroidVersion mVersion;

    /**
     * The minimal API level required by this extra package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    private final int mMinApiLevel;

    /**
     * Creates a new sample package from the attributes and elements of the given XML node.
     * <p/>
     * This constructor should throw an exception if the package cannot be created.
     */
    SamplePackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);

        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);

        mMinApiLevel = XmlParserUtils.getXmlInt(packageNode, SdkRepository.NODE_MIN_API_LEVEL,
                MIN_API_LEVEL_NOT_SPECIFIED);
    }

    /**
     * Creates a new sample package based on an actual {@link IAndroidTarget} (which
     * must have {@link IAndroidTarget#isPlatform()} true) from the {@link SdkManager}.
     * <p/>
     * The target <em>must</em> have an existing sample directory that uses the /samples
     * root form rather than the old form where the samples dir was located under the
     * platform dir.
     * <p/>
     * This is used to list local SDK folders in which case there is one archive which
     * URL is the actual samples path location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    SamplePackage(IAndroidTarget target, Properties props) {
        super(  null,                                   //source
                props,                                  //properties
                0,                                      //revision will be taken from props
                null,                                   //license
                null,                                   //description
                null,                                   //descUrl
                Os.ANY,                                 //archiveOs
                Arch.ANY,                               //archiveArch
                target.getPath(IAndroidTarget.SAMPLES)  //archiveOsPath
                );

        mVersion = target.getVersion();

        mMinApiLevel = Integer.parseInt(
            getProperty(props, PROP_MIN_API_LEVEL, Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));
    }

    /**
     * Creates a new sample package from an actual directory path and previously
     * saved properties.
     * <p/>
     * This is used to list local SDK folders in which case there is one archive which
     * URL is the actual samples path location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     *
     * @throws AndroidVersionException if the {@link AndroidVersion} can't be restored
     *                                 from properties.
     */
    SamplePackage(String archiveOsPath, Properties props) throws AndroidVersionException {
        super(null,                                   //source
              props,                                  //properties
              0,                                      //revision will be taken from props
              null,                                   //license
              null,                                   //description
              null,                                   //descUrl
              Os.ANY,                                 //archiveOs
              Arch.ANY,                               //archiveArch
              archiveOsPath                           //archiveOsPath
              );

        mVersion = new AndroidVersion(props);

        mMinApiLevel = Integer.parseInt(
            getProperty(props, PROP_MIN_API_LEVEL, Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));
    }

    /**
     * Save the properties of the current packages in the given {@link Properties} object.
     * These properties will later be given to a constructor that takes a {@link Properties} object.
     */
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);

        mVersion.saveProperties(props);

        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            props.setProperty(PROP_MIN_API_LEVEL, Integer.toString(getMinApiLevel()));
        }
    }

    /**
     * Returns the minimal API level required by this extra package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    public int getMinApiLevel() {
        return mMinApiLevel;
    }

    /** Returns the matching platform version. */
    public AndroidVersion getVersion() {
        return mVersion;
    }

    /** Returns a short description for an {@link IDescription}. */
    @Override
    public String getShortDescription() {
        String s = String.format("Samples for SDK API %1$s%2$s, revision %3$d%4$s",
                mVersion.getApiString(),
                mVersion.isPreview() ? " Preview" : "",
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        return s;
    }

    /**
     * Returns a long description for an {@link IDescription}.
     *
     * The long description is whatever the XML contains for the &lt;description&gt; field,
     * or the short description if the former is empty.
     */
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = getShortDescription();
        }

        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }

        return s;
    }

    /**
     * Computes a potential installation folder if an archive of this package were
     * to be installed right away in the given SDK root.
     * <p/>
     * A sample package is typically installed in SDK/samples/android-"version".
     * However if we can find a different directory that already has this sample
     * version installed, we'll use that one.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param suggestedDir A suggestion for the installation folder name, based on the root
     *                     folder used in the zip archive.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {

        // The /samples dir at the root of the SDK
        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);

        // First find if this platform is already installed. If so, reuse the same directory.
        for (IAndroidTarget target : sdkManager.getTargets()) {
            if (target.isPlatform() &&
                    target.getVersion().equals(mVersion)) {
                String p = target.getPath(IAndroidTarget.SAMPLES);
                File f = new File(p);
                if (f.isDirectory()) {
                    // We *only* use this directory if it's using the "new" location
                    // under SDK/samples. We explicitly do not reuse the "old" location
                    // under SDK/platform/android-N/samples.
                    if (f.getParentFile().equals(samplesRoot)) {
                        return f;
                    }
                }
            }
        }

        // Otherwise, get a suitable default
        File folder = new File(samplesRoot,
                String.format("android-%s", getVersion().getApiString())); //$NON-NLS-1$

        for (int n = 1; folder.exists(); n++) {
            // Keep trying till we find an unused directory.
            folder = new File(samplesRoot,
                    String.format("android-%s_%d", getVersion().getApiString(), n)); //$NON-NLS-1$
        }

        return folder;
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof SamplePackage) {
            SamplePackage newPkg = (SamplePackage)pkg;

            // check they are the same platform.
            return newPkg.getVersion().equals(this.getVersion());
        }

        return false;
    }

    /**
     * Makes sure the base /samples folder exists before installing.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean preInstallHook(Archive archive,
            ITaskMonitor monitor,
            String osSdkRoot,
            File installFolder) {
        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);
        if (!samplesRoot.isDirectory()) {
            samplesRoot.mkdir();
        }

        if (installFolder != null && installFolder.isDirectory()) {
            // Get the hash computed during the last installation
            String storedHash = readContentHash(installFolder);
            if (storedHash != null && storedHash.length() > 0) {

                // Get the hash of the folder now
                String currentHash = computeContentHash(installFolder);

                if (!storedHash.equals(currentHash)) {
                    // The hashes differ. The content was modified.
                    // Ask the user if we should still wipe the old samples.

                    String pkgName = archive.getParentPackage().getShortDescription();

                    String msg = String.format(
                            "-= Warning ! =-\n" +
                            "You are about to replace the content of the folder:\n " +
                            "  %1$s\n" +
                            "by the new package:\n" +
                            "  %2$s.\n" +
                            "\n" +
                            "However it seems that the content of the existing samples " +
                            "has been modified since it was last installed. Are you sure " +
                            "you want to DELETE the existing samples? This cannot be undone.\n" +
                            "Please select YES to delete the existing sample and replace them " +
                            "by the new ones.\n" +
                            "Please select NO to skip this package. You can always install it later.",
                            installFolder.getAbsolutePath(),
                            pkgName);

                    // Returns true if we can wipe & replace.
                    return monitor.displayPrompt("SDK Manager: overwrite samples?", msg);
                }
            }
        }

        // The default is to allow installation
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }

    /**
     * Computes a hash of the installed content (in case of successful install.)
     *
     * {@inheritDoc}
     */
    @Override
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
        super.postInstallHook(archive, monitor, installFolder);

        if (installFolder == null) {
            return;
        }

        String h = computeContentHash(installFolder);
        saveContentHash(installFolder, h);
    }

    /**
     * Reads the hash from the properties file, if it exists.
     * Returns null if something goes wrong, e.g. there's no property file or
     * it doesn't contain our hash. Returns an empty string if the hash wasn't
     * correctly computed last time by {@link #saveContentHash(File, String)}.
     */
    private String readContentHash(File folder) {
        Properties props = new Properties();

        FileInputStream fis = null;
        try {
            File f = new File(folder, SdkConstants.FN_CONTENT_HASH_PROP);
            if (f.isFile()) {
                fis = new FileInputStream(f);
                props.load(fis);
                return props.getProperty("content-hash", null);  //$NON-NLS-1$
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    /**
     * Saves the hash using a properties file
     */
    private void saveContentHash(File folder, String hash) {
        Properties props = new Properties();

        props.setProperty("content-hash", hash == null ? "" : hash);  //$NON-NLS-1$ //$NON-NLS-2$

        FileOutputStream fos = null;
        try {
            File f = new File(folder, SdkConstants.FN_CONTENT_HASH_PROP);
            fos = new FileOutputStream(f);
            props.store( fos, "## Android - hash of this archive.");  //$NON-NLS-1$
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
    }

    /**
     * Computes a hash of the files names and sizes installed in the folder
     * using the SHA-1 digest.
     * Returns null if the digest algorithm is not available.
     */
    private String computeContentHash(File installFolder) {
        MessageDigest md = null;
        try {
            // SHA-1 is a standard algorithm.
            // http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB
            md = MessageDigest.getInstance("SHA-1");    //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            // We're unlikely to get there unless this JVM is not spec conforming
            // in which case there won't be any hash available.
        }

        if (md != null) {
            hashDirectoryContent(installFolder, md);
            return getDigestHexString(md);
        }

        return null;
    }

    /**
     * Computes a hash of the *content* of this directory. The hash only uses
     * the files names and the file sizes.
     */
    private void hashDirectoryContent(File folder, MessageDigest md) {
        if (folder == null || md == null || !folder.isDirectory()) {
            return;
        }

        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                hashDirectoryContent(f, md);

            } else {
                String name = f.getName();

                // Skip the file we use to store the content hash
                if (name == null || SdkConstants.FN_CONTENT_HASH_PROP.equals(name)) {
                    continue;
                }

                try {
                    md.update(name.getBytes("UTF-8"));   //$NON-NLS-1$
                } catch (UnsupportedEncodingException e) {
                    // There is no valid reason for UTF-8 to be unsupported. Ignore.
                }
                try {
                    long len = f.length();
                    md.update((byte) (len & 0x0FF));
                    md.update((byte) ((len >> 8) & 0x0FF));
                    md.update((byte) ((len >> 16) & 0x0FF));
                    md.update((byte) ((len >> 24) & 0x0FF));

                } catch (SecurityException e) {
                    // Might happen if file is not readable. Ignore.
                }
            }
        }
    }

    /**
     * Returns a digest as an hex string.
     */
    private String getDigestHexString(MessageDigest digester) {
        // Create an hex string from the digest
        byte[] digest = digester.digest();
        int n = digest.length;
        String hex = "0123456789abcdef";                     //$NON-NLS-1$
        char[] hexDigest = new char[n * 2];
        for (int i = 0; i < n; i++) {
            int b = digest[i] & 0x0FF;
            hexDigest[i*2 + 0] = hex.charAt(b >>> 4);
            hexDigest[i*2 + 1] = hex.charAt(b & 0x0f);
        }

        return new String(hexDigest);
    }
}
