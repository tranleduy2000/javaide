/*
 * Copyright (C) 2011 The Android Open Source Project
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
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.AndroidVersion.AndroidVersionException;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.SdkRepoConstants;

import org.w3c.dom.Node;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a source XML node in an SDK repository.
 * <p/>
 * Note that a source package has a version and thus implements {@link IPackageVersion}.
 * However there is no mandatory dependency that limits installation so this does not
 * implement {@link IPlatformDependency}.
 */
public class SourcePackage extends Package implements IPackageVersion {

    /** The package version, for platform, add-on and doc packages. */
    private final AndroidVersion mVersion;

    /**
     * Creates a new source package from the attributes and elements of the given XML node.
     * This constructor should throw an exception if the package cannot be created.
     *
     * @param source The {@link SdkSource} where this is loaded from.
     * @param packageNode The XML element being parsed.
     * @param nsUri The namespace URI of the originating XML document, to be able to deal with
     *          parameters that vary according to the originating XML schema.
     * @param licenses The licenses loaded from the XML originating document.
     */
    SourcePackage(SdkSource source,
            Node packageNode,
            String nsUri,
            Map<String,String> licenses) {
        super(source, packageNode, nsUri, licenses);

        int apiLevel = XmlParserUtils.getXmlInt(packageNode, SdkRepoConstants.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepoConstants.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected SourcePackage(
            AndroidVersion platformVersion,
            int revision,
            Properties props,
            String localOsPath) {
        this(null /*source*/, platformVersion, revision, props, localOsPath);
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected SourcePackage(
            SdkSource source,
            AndroidVersion platformVersion,
            int revision,
            Properties props,
            String localOsPath) {
        super(  source,                     //source
                props,                      //properties
                revision,                   //revision
                null,                       //license
                null,                       //description
                null,                       //descUrl
                Os.getCurrentOs(),          //archiveOs
                Arch.getCurrentArch(),      //archiveArch
                localOsPath                 //archiveOsPath
                );
        mVersion = platformVersion;
    }

    /**
     * Creates either a valid {@link SourcePackage} or a {@link BrokenPackage}.
     * <p/>
     * If the source directory contains valid properties, this creates a new {@link SourcePackage}
     * with the android version listed in the properties.
     * Otherwise returns a new {@link BrokenPackage} with some explanation on what failed.
     *
     * @param srcDir The SDK/sources/android-N folder
     * @param props The properties located in {@code srcDir} or null if not found.
     * @return A new {@link SourcePackage} or a new {@link BrokenPackage}.
     */
    public static Package create(File srcDir, Properties props) {
        AndroidVersion version = null;
        String error = null;

        // Try to load the android version from the sources.props.
        // If we don't find them, it would explain why this package is broken.
        if (props == null) {
            error = String.format("Missing file %1$s", SdkConstants.FN_SOURCE_PROP);
        } else {
            try {
                version = new AndroidVersion(props);
                // The constructor will extract the revision from the properties
                // and it will not consider a missing revision as being fatal.
                return new SourcePackage(version, 0 /*revision*/, props, srcDir.getAbsolutePath());
            } catch (AndroidVersionException e) {
                error = String.format("Invalid file %1$s: %2$s",
                        SdkConstants.FN_SOURCE_PROP,
                        e.getMessage());
            }
        }

        if (version == null) {
            try {
                // Try to parse the first number out of the platform folder name.
                // This is just a wild guess in case we can create a broken package using that info.
                String platform = srcDir.getParentFile().getName();
                platform = platform.replaceAll("[^0-9]+", " ").trim();  //$NON-NLS-1$ //$NON-NLS-2$
                int pos = platform.indexOf(' ');
                if (pos >= 0) {
                    platform = platform.substring(0, pos);
                }
                int apiLevel = Integer.parseInt(platform);
                version = new AndroidVersion(apiLevel, null /*codename*/);
            } catch (Exception ignore) {
            }
        }

        StringBuilder sb = new StringBuilder("Broken Source Package");
        if (version != null) {
            sb.append(String.format(", API %1$s", version.getApiString()));
        }

        String shortDesc = sb.toString();

        if (error != null) {
            sb.append('\n').append(error);
        }

        String longDesc = sb.toString();

        return new BrokenPackage(props, shortDesc, longDesc,
                IMinApiLevelDependency.MIN_API_LEVEL_NOT_SPECIFIED,
                version==null ? IExactApiLevelDependency.API_LEVEL_INVALID : version.getApiLevel(),
                srcDir.getAbsolutePath());
    }

    /**
     * Save the properties of the current packages in the given {@link Properties} object.
     * These properties will later be given to a constructor that takes a {@link Properties} object.
     */
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        mVersion.saveProperties(props);
    }

    /**
     * Returns the android version of this package.
     */
    public AndroidVersion getVersion() {
        return mVersion;
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For sources, we use "source-N" where N is the API or the preview codename.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        return "source-" + mVersion.getApiString();    //$NON-NLS-1$
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        if (mVersion.isPreview()) {
            return String.format("Sources for Android '%1$s' Preview SDK%2$s",
                    mVersion.getCodename(),
                    isObsolete() ? " (Obsolete)" : "");
        } else {
            return String.format("Sources for Android SDK%2$s",
                    mVersion.getApiLevel(),
                    isObsolete() ? " (Obsolete)" : "");
        }
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {
        if (mVersion.isPreview()) {
            return String.format("Sources for Android '%1$s' Preview SDK, revision %2$s%3$s",
                mVersion.getCodename(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        } else {
            return String.format("Sources for Android SDK, API %1$d, revision %2$s%3$s",
                mVersion.getApiLevel(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        }
    }

    /**
     * Returns a long description for an {@link IDescription}.
     *
     * The long description is whatever the XML contains for the {@code description} field,
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
     * A sources package is typically installed in SDK/sources/platform.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        File folder = new File(osSdkRoot, SdkConstants.FD_PKG_SOURCES);
        folder = new File(folder, "android-" + mVersion.getApiString());    //$NON-NLS-1$
        return folder;
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof SourcePackage) {
            SourcePackage newPkg = (SourcePackage)pkg;

            // check they are the same version.
            return getVersion().equals(newPkg.getVersion());
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mVersion == null) ? 0 : mVersion.hashCode());
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
        if (!(obj instanceof SourcePackage)) {
            return false;
        }
        SourcePackage other = (SourcePackage) obj;
        if (mVersion == null) {
            if (other.mVersion != null) {
                return false;
            }
        } else if (!mVersion.equals(other.mVersion)) {
            return false;
        }
        return true;
    }
}
