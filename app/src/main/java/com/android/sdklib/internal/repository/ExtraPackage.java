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
import com.android.sdklib.NullSdkLog;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.PkgProps;
import com.android.sdklib.repository.RepoConstants;

import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Represents a extra XML node in an SDK repository.
 */
public class ExtraPackage extends MinToolsPackage
    implements IMinApiLevelDependency {

    /**
     * The vendor folder name. It must be a non-empty single-segment path.
     * <p/>
     * The paths "add-ons", "platforms", "platform-tools", "tools" and "docs" are reserved and
     * cannot be used.
     * This limitation cannot be written in the XML Schema and must be enforced here by using
     * the method {@link #isPathValid()} *before* installing the package.
     */
    private final String mVendor;

    /**
     * The sub-folder name. It must be a non-empty single-segment path and has the same
     * rules as {@link #mVendor}.
     */
    private final String mPath;

    /**
     * The optional old_paths, if any. If present, this is a list of old "path" values that
     * we'd like to migrate to the current "path" name for this extra.
     */
    private final String mOldPaths;

    /**
     * The minimal API level required by this extra package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    private final int mMinApiLevel;

    /**
     * The project-files listed by this extra package.
     * The array can be empty but not null.
     */
    private final String[] mProjectFiles;

    /**
     * Creates a new tool package from the attributes and elements of the given XML node.
     * This constructor should throw an exception if the package cannot be created.
     *
     * @param source The {@link SdkSource} where this is loaded from.
     * @param packageNode The XML element being parsed.
     * @param nsUri The namespace URI of the originating XML document, to be able to deal with
     *          parameters that vary according to the originating XML schema.
     * @param licenses The licenses loaded from the XML originating document.
     */
    ExtraPackage(SdkSource source, Node packageNode, String nsUri, Map<String,String> licenses) {
        super(source, packageNode, nsUri, licenses);

        mPath   = XmlParserUtils.getXmlString(packageNode, RepoConstants.NODE_PATH);
        mVendor = XmlParserUtils.getXmlString(packageNode, RepoConstants.NODE_VENDOR);

        mMinApiLevel = XmlParserUtils.getXmlInt(packageNode, RepoConstants.NODE_MIN_API_LEVEL,
                MIN_API_LEVEL_NOT_SPECIFIED);

        mProjectFiles = parseProjectFiles(
                XmlParserUtils.getFirstChild(packageNode, RepoConstants.NODE_PROJECT_FILES));

        mOldPaths = XmlParserUtils.getXmlString(packageNode, RepoConstants.NODE_OLD_PATHS);
    }

    private String[] parseProjectFiles(Node projectFilesNode) {
        ArrayList<String> paths = new ArrayList<String>();

        if (projectFilesNode != null) {
            String nsUri = projectFilesNode.getNamespaceURI();
            for(Node child = projectFilesNode.getFirstChild();
                     child != null;
                     child = child.getNextSibling()) {

                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        RepoConstants.NODE_PATH.equals(child.getLocalName())) {
                    String path = child.getTextContent();
                    if (path != null) {
                        path = path.trim();
                        if (path.length() > 0) {
                            paths.add(path);
                        }
                    }
                }
            }
        }

        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Manually create a new package with one archive and the given attributes or properties.
     * This is used to create packages from local directories in which case there must be
     * one archive which URL is the actual target location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    static Package create(SdkSource source,
            Properties props,
            String vendor,
            String path,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        ExtraPackage ep = new ExtraPackage(source, props, vendor, path, revision, license,
                description, descUrl, archiveOs, archiveArch, archiveOsPath);

        if (ep.isPathValid()) {
            return ep;
        } else {
            String shortDesc = ep.getShortDescription() + " [*]";  //$NON-NLS-1$

            String longDesc = String.format(
                    "Broken Extra Package: %1$s\n" +
                    "[*] Package cannot be used due to error: Invalid install path %2$s",
                    description,
                    ep.getPath());

            BrokenPackage ba = new BrokenPackage(props, shortDesc, longDesc,
                    ep.getMinApiLevel(),
                    IExactApiLevelDependency.API_LEVEL_INVALID,
                    archiveOsPath);
            return ba;
        }
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected ExtraPackage(SdkSource source,
            Properties props,
            String vendor,
            String path,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        super(source,
                props,
                revision,
                license,
                description,
                descUrl,
                archiveOs,
                archiveArch,
                archiveOsPath);

        // The vendor argument is not supposed to be empty. However this attribute did not
        // exist prior to schema repo-v3 and tools r8, which means we need to cope with a
        // lack of it when reading back old local repositories. In this case we allow an
        // empty string.
        mVendor = vendor != null ? vendor : getProperty(props, PkgProps.EXTRA_VENDOR, "");

        // The path argument comes before whatever could be in the properties
        mPath   = path != null ? path : getProperty(props, PkgProps.EXTRA_PATH, path);

        mOldPaths = getProperty(props, PkgProps.EXTRA_OLD_PATHS, null);

        mMinApiLevel = Integer.parseInt(
            getProperty(props,
                    PkgProps.EXTRA_MIN_API_LEVEL,
                    Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));

        String projectFiles = getProperty(props, PkgProps.EXTRA_PROJECT_FILES, null);
        ArrayList<String> filePaths = new ArrayList<String>();
        if (projectFiles != null && projectFiles.length() > 0) {
            for (String filePath : projectFiles.split(Pattern.quote(File.pathSeparator))) {
                filePath = filePath.trim();
                if (filePath.length() > 0) {
                    filePaths.add(filePath);
                }
            }
        }
        mProjectFiles = filePaths.toArray(new String[filePaths.size()]);
    }

    /**
     * Save the properties of the current packages in the given {@link Properties} object.
     * These properties will later be give the constructor that takes a {@link Properties} object.
     */
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);

        props.setProperty(PkgProps.EXTRA_PATH, mPath);
        if (mVendor != null) {
            props.setProperty(PkgProps.EXTRA_VENDOR, mVendor);
        }

        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            props.setProperty(PkgProps.EXTRA_MIN_API_LEVEL, Integer.toString(getMinApiLevel()));
        }

        if (mProjectFiles.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mProjectFiles.length; i++) {
                if (i > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                sb.append(mProjectFiles[i]);
            }
            props.setProperty(PkgProps.EXTRA_PROJECT_FILES, sb.toString());
        }

        if (mOldPaths != null && mOldPaths.length() > 0) {
            props.setProperty(PkgProps.EXTRA_OLD_PATHS, mOldPaths);
        }
    }

    /**
     * Returns the minimal API level required by this extra package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    public int getMinApiLevel() {
        return mMinApiLevel;
    }

    /**
     * The project-files listed by this extra package.
     * The array can be empty but not null.
     * <p/>
     * IMPORTANT: directory separators are NOT translated and may not match
     * the {@link File#separatorChar} of the current platform. It's up to the
     * user to adequately interpret the paths.
     * Similarly, no guarantee is made on the validity of the paths.
     * Users are expected to apply all usual sanity checks such as removing
     * "./" and "../" and making sure these paths don't reference files outside
     * of the installed archive.
     *
     * @since sdk-repository-4.xsd or sdk-addon-2.xsd
     */
    public String[] getProjectFiles() {
        return mProjectFiles;
    }

    /**
     * Returns the old_paths, a list of obsolete path names for the extra package.
     * <p/>
     * These can be used by the installer to migrate an extra package using one of the
     * old paths into the new path.
     * <p/>
     * These can also be used to recognize "old" renamed packages as the same as
     * the current one.
     *
     * @return A list of old paths. Can be empty but not null.
     */
    public String[] getOldPaths() {
        if (mOldPaths == null || mOldPaths.length() == 0) {
            return new String[0];
        }
        return mOldPaths.split(";");  //$NON-NLS-1$
    }

    /**
     * Static helper to check if a given vendor and path is acceptable for an "extra" package.
     */
    public boolean isPathValid() {
        return isSegmentValid(mVendor) && isSegmentValid(mPath);
    }

    private boolean isSegmentValid(String segment) {
        if (SdkConstants.FD_ADDONS.equals(segment) ||
                SdkConstants.FD_PLATFORMS.equals(segment) ||
                SdkConstants.FD_PLATFORM_TOOLS.equals(segment) ||
                SdkConstants.FD_TOOLS.equals(segment) ||
                SdkConstants.FD_DOCS.equals(segment) ||
                RepoConstants.FD_TEMP.equals(segment)) {
            return false;
        }
        return segment != null && segment.indexOf('/') == -1 && segment.indexOf('\\') == -1;
    }

    /**
     * Returns the sanitized path folder name. It is a single-segment path.
     * <p/>
     * The package is installed in SDK/extras/vendor_name/path_name.
     * <p/>
     * The paths "add-ons", "platforms", "tools" and "docs" are reserved and cannot be used.
     * This limitation cannot be written in the XML Schema and must be enforced here by using
     * the method {@link #isPathValid()} *before* installing the package.
     */
    public String getPath() {
        // The XSD specifies the XML vendor and path should only contain [a-zA-Z0-9]+
        // and cannot be empty. Let's be defensive and enforce that anyway since things
        // like "____" are still valid values that we don't want to allow.

        // Sanitize the path
        String path = mPath.replaceAll("[^a-zA-Z0-9-]+", "_");      //$NON-NLS-1$
        if (path.length() == 0 || path.equals("_")) {               //$NON-NLS-1$
            int h = path.hashCode();
            path = String.format("extra%08x", h);                   //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the sanitized vendor folder name. It is a single-segment path.
     * <p/>
     * The package is installed in SDK/extras/vendor_name/path_name.
     * <p/>
     * An empty string is returned in case of error.
     */
    public String getVendor() {

        // The XSD specifies the XML vendor and path should only contain [a-zA-Z0-9]+
        // and cannot be empty. Let's be defensive and enforce that anyway since things
        // like "____" are still valid values that we don't want to allow.

        if (mVendor != null && mVendor.length() > 0) {
            String vendor = mVendor;
            // Sanitize the vendor
            vendor = vendor.replaceAll("[^a-zA-Z0-9-]+", "_");      //$NON-NLS-1$
            if (vendor.equals("_")) {                               //$NON-NLS-1$
                int h = vendor.hashCode();
                vendor = String.format("vendor%08x", h);            //$NON-NLS-1$
            }

            return vendor;
        }

        return ""; //$NON-NLS-1$
    }

    private String getPrettyName() {
        String name = mPath;

        // In the past, we used to save the extras in a folder vendor-path,
        // and that "vendor" would end up in the path when we reload the extra from
        // disk. Detect this and compensate.
        if (mVendor != null && mVendor.length() > 0) {
            if (name.startsWith(mVendor + "-")) {  //$NON-NLS-1$
                name = name.substring(mVendor.length() + 1);
            }
        }

        // Uniformize all spaces in the name
        if (name != null) {
            name = name.replaceAll("[ _\t\f-]+", " ").trim();   //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (name == null || name.length() == 0) {   //$NON-NLS-1$
            name = "Unkown Extra";
        }

        if (mVendor != null && mVendor.length() > 0) {
            name = mVendor + " " + name;  //$NON-NLS-1$
            name = name.replaceAll("[ _\t\f-]+", " ").trim();   //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Look at all lower case characters in range [1..n-1] and replace them by an upper
        // case if they are preceded by a space. Also upper cases the first character of the
        // string.
        boolean changed = false;
        char[] chars = name.toCharArray();
        for (int n = chars.length - 1, i = 0; i < n; i++) {
            if (Character.isLowerCase(chars[i]) && (i == 0 || chars[i - 1] == ' ')) {
                chars[i] = Character.toUpperCase(chars[i]);
                changed = true;
            }
        }
        if (changed) {
            name = new String(chars);
        }

        // Special case: reformat a few typical acronyms.
        name = name.replaceAll(" Usb ", " USB ");   //$NON-NLS-1$
        name = name.replaceAll(" Api ", " API ");   //$NON-NLS-1$

        return name;
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For extras, we use "extra-vendor-path".
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        return String.format("extra-%1$s-%2$s",     //$NON-NLS-1$
                getVendor(),
                getPath());
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        String s = String.format("%1$s package%2$s",
                getPrettyName(),
                isObsolete() ? " (Obsolete)" : "");  //$NON-NLS-2$

        return s;
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {

        String s = String.format("%1$s package, revision %2$d%3$s",
                getPrettyName(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");  //$NON-NLS-2$

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
            s = String.format("Extra %1$s package by %2$s", getPath(), getVendor());
        }

        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");  //$NON-NLS-2$
        }

        if (getMinToolsRevision() != MIN_TOOLS_REV_NOT_SPECIFIED) {
            s += String.format("\nRequires tools revision %1$d", getMinToolsRevision());
        }

        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            s += String.format("\nRequires SDK Platform Android API %1$s", getMinApiLevel());
        }

        // For a local archive, also put the install path in the long description.
        // This should help users locate the extra on their drive.
        File localPath = getLocalArchivePath();
        if (localPath != null) {
            s += String.format("\nLocation: %1$s", localPath.getAbsolutePath());
        }

        return s;
    }

    /**
     * Computes a potential installation folder if an archive of this package were
     * to be installed right away in the given SDK root.
     * <p/>
     * A "tool" package should always be located in SDK/tools.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     *                   Not used in this implementation.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {

        // First find if this extra is already installed. If so, reuse the same directory.
        LocalSdkParser localParser = new LocalSdkParser();
        Package[] pkgs = localParser.parseSdk(
                osSdkRoot,
                sdkManager,
                new NullTaskMonitor(new NullSdkLog()));

        for (Package pkg : pkgs) {
            if (sameItemAs(pkg) && pkg instanceof ExtraPackage) {
                File localPath = ((ExtraPackage) pkg).getLocalArchivePath();
                if (localPath != null) {
                    return localPath;
                }
            }
        }

        // The /extras dir at the root of the SDK
        File path = new File(osSdkRoot, SdkConstants.FD_EXTRAS);

        String vendor = getVendor();
        if (vendor != null && vendor.length() > 0) {
            path = new File(path, vendor);
        }

        String name = getPath();
        if (name != null && name.length() > 0) {
            path = new File(path, name);
        }

        return path;
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        // Extra packages are similar if they have the same path and vendor
        if (pkg instanceof ExtraPackage) {
            ExtraPackage ep = (ExtraPackage) pkg;

            String[] epOldPaths = ep.getOldPaths();
            int lenEpOldPaths = epOldPaths.length;
            for (int indexEp = -1; indexEp < lenEpOldPaths; indexEp++) {
                if (sameVendorAndPath(
                        mVendor,    mPath,
                        ep.mVendor, indexEp   < 0 ? ep.mPath : epOldPaths[indexEp])) {
                    return true;
                }
            }

            String[] thisOldPaths = getOldPaths();
            int lenThisOldPaths = thisOldPaths.length;
            for (int indexThis = -1; indexThis < lenThisOldPaths; indexThis++) {
                if (sameVendorAndPath(
                        mVendor,    indexThis < 0 ? mPath    : thisOldPaths[indexThis],
                        ep.mVendor, ep.mPath)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean sameVendorAndPath(
            String thisVendor, String thisPath,
            String otherVendor, String otherPath) {
        // To be backward compatible, we need to support the old vendor-path form
        // in either the current or the remote package.
        //
        // The vendor test below needs to account for an old installed package
        // (e.g. with an install path of vendor-name) that has then been updated
        // in-place and thus when reloaded contains the vendor name in both the
        // path and the vendor attributes.
        if (otherPath != null && thisPath != null && thisVendor != null) {
            if (otherPath.equals(thisVendor + '-' + thisPath) &&
                    (otherVendor == null ||
                     otherVendor.length() == 0 ||
                     otherVendor.equals(thisVendor))) {
                return true;
            }
        }
        if (thisPath != null && otherPath != null && otherVendor != null) {
            if (thisPath.equals(otherVendor + '-' + otherPath) &&
                    (thisVendor == null ||
                     thisVendor.length() == 0 ||
                     thisVendor.equals(otherVendor))) {
                return true;
            }
        }


        if (thisPath != null && thisPath.equals(otherPath)) {
            if ((thisVendor == null && otherVendor == null) ||
                (thisVendor != null && thisVendor.equals(otherVendor))) {
                return true;
            }
        }

        return false;
    }

    /**
     * For extra packages, we want to add vendor|path to the sorting key
     * <em>before<em/> the revision number.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    protected String comparisonKey() {
        String s = super.comparisonKey();
        int pos = s.indexOf("|r:");         //$NON-NLS-1$
        assert pos > 0;
        s = s.substring(0, pos) +
            "|ve:" + getVendor() +          //$NON-NLS-1$
            "|pa:" + getPath() +            //$NON-NLS-1$
            s.substring(pos);
        return s;
    }

    // ---

    /**
     * If this package is installed, returns the install path of the archive if valid.
     * Returns null if not installed or if the path does not exist.
     */
    private File getLocalArchivePath() {
        Archive[] archives = getArchives();
        if (archives.length == 1 && archives[0].isLocal()) {
            File path = new File(archives[0].getLocalOsPath());
            if (path.isDirectory()) {
                return path;
            }
        }

        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + mMinApiLevel;
        result = prime * result + ((mPath == null) ? 0 : mPath.hashCode());
        result = prime * result + Arrays.hashCode(mProjectFiles);
        result = prime * result + ((mVendor == null) ? 0 : mVendor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ExtraPackage)) {
            return false;
        }
        ExtraPackage other = (ExtraPackage) obj;
        if (mMinApiLevel != other.mMinApiLevel) {
            return false;
        }
        if (mPath == null) {
            if (other.mPath != null) {
                return false;
            }
        } else if (!mPath.equals(other.mPath)) {
            return false;
        }
        if (!Arrays.equals(mProjectFiles, other.mProjectFiles)) {
            return false;
        }
        if (mVendor == null) {
            if (other.mVendor != null) {
                return false;
            }
        } else if (!mVendor.equals(other.mVendor)) {
            return false;
        }
        return true;
    }
}
