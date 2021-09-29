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

package com.android.sdklib.internal.repository.packages;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.ITaskMonitor;
import com.android.sdklib.internal.repository.archives.Archive;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.FullRevision.PreviewComparison;
import com.android.sdklib.repository.IDescription;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgDesc;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a platform-tool XML node in an SDK repository.
 *
 * @deprecated
 * com.android.sdklib.internal.repository has moved into Studio as
 * com.android.tools.idea.sdk.remote.internal.
 */
@Deprecated
public class PlatformToolPackage extends FullRevisionPackage {

    /** The value returned by {@link PlatformToolPackage#installId()}. */
    public static final String INSTALL_ID = "platform-tools";                       //$NON-NLS-1$
    /** The value returned by {@link PlatformToolPackage#installId()}. */
    public static final String INSTALL_ID_PREVIEW = "platform-tools-preview";       //$NON-NLS-1$

    private final IPkgDesc mPkgDesc;

    /**
     * Creates a new platform-tool package from the attributes and elements of the given XML node.
     * This constructor should throw an exception if the package cannot be created.
     *
     * @param source The {@link SdkSource} where this is loaded from.
     * @param packageNode The XML element being parsed.
     * @param nsUri The namespace URI of the originating XML document, to be able to deal with
     *          parameters that vary according to the originating XML schema.
     * @param licenses The licenses loaded from the XML originating document.
     */
    public PlatformToolPackage(SdkSource source, Node packageNode,
            String nsUri, Map<String,String> licenses) {
        super(source, packageNode, nsUri, licenses);

        mPkgDesc = setDescriptions(PkgDesc.Builder.newPlatformTool(getRevision())).create();
    }

    /**
     * Manually create a new package with one archive and the given attributes or properties.
     * This is used to create packages from local directories in which case there must be
     * one archive which URL is the actual target location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    public static Package create(
            SdkSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            String archiveOsPath) {

        PlatformToolPackage ptp = new PlatformToolPackage(source, props, revision, license,
                description, descUrl, archiveOsPath);

        File platformToolsFolder = new File(archiveOsPath);
        String error = null;
        if (!platformToolsFolder.isDirectory()) {
            error = "platform-tools folder is missing";
        } else {
            File[] files = platformToolsFolder.listFiles();
            if (files == null || files.length == 0) {
                error = "platform-tools folder is empty";
            }
        }

        if (error != null) {
            String shortDesc = ptp.getShortDescription() + " [*]";  //$NON-NLS-1$

            String longDesc = String.format(
                    "Broken Platform-Tools Package: %1$s\n" +
                    "[*] Package cannot be used due to error: %2$s",
                    description,
                    error);

            BrokenPackage ba = new BrokenPackage(props, shortDesc, longDesc,
                    IMinApiLevelDependency.MIN_API_LEVEL_NOT_SPECIFIED,
                    IExactApiLevelDependency.API_LEVEL_INVALID,
                    archiveOsPath,
                    PkgDesc.Builder.newPlatformTool(ptp.getRevision())
                                   .setDescriptionShort(shortDesc)
                                   .create());
            return ba;
        }


        return ptp;
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected PlatformToolPackage(
                SdkSource source,
                Properties props,
                int revision,
                String license,
                String description,
                String descUrl,
                String archiveOsPath) {
        super(source,
                props,
                revision,
                license,
                description,
                descUrl,
                archiveOsPath);

        mPkgDesc = setDescriptions(PkgDesc.Builder.newPlatformTool(getRevision())).create();
    }

    @Override
    @NonNull
    public IPkgDesc getPkgDesc() {
        return mPkgDesc;
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For platform-tools, we use "platform-tools" or "platform-tools-preview" since
     * this package type is unique.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        if (getRevision().isPreview()) {
            return INSTALL_ID_PREVIEW;
        } else {
            return INSTALL_ID;
        }
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        String ld = getListDisplay();
        if (!ld.isEmpty()) {
            return String.format("%1$s%2$s", ld, isObsolete() ? " (Obsolete)" : "");
        }

        return String.format("Android SDK Platform-tools%1$s",
                isObsolete() ? " (Obsolete)" : "");
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {
        String ld = getListDisplay();
        if (!ld.isEmpty()) {
            return String.format("%1$s, revision %2$s%3$s",
                    ld,
                    getRevision().toShortString(),
                    isObsolete() ? " (Obsolete)" : "");
        }

        return String.format("Android SDK Platform-tools, revision %1$s%2$s",
                getRevision().toShortString(),
                isObsolete() ? " (Obsolete)" : "");
    }

    /** Returns a long description for an {@link IDescription}. */
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.isEmpty()) {
            s = getShortDescription();
        }

        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$s%2$s",
                    getRevision().toShortString(),
                    isObsolete() ? " (Obsolete)" : "");
        }

        return s;
    }

    /**
     * Computes a potential installation folder if an archive of this package were
     * to be installed right away in the given SDK root.
     * <p/>
     * A "platform-tool" package should always be located in SDK/platform-tools.
     * There can be only one installed at once.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        return new File(osSdkRoot, SdkConstants.FD_PLATFORM_TOOLS);
    }

    /**
     * Check whether 2 platform-tool packages are the same <em>and</em> have the
     * same preview bit.
     */
    @Override
    public boolean sameItemAs(Package pkg) {
        return sameItemAs(pkg, PreviewComparison.COMPARE_TYPE);
    }

    @Override
    public boolean sameItemAs(Package pkg, PreviewComparison comparePreview) {
        // only one platform-tool package so any platform-tool package is the same item.
        if (pkg instanceof PlatformToolPackage) {
            switch (comparePreview) {
            case IGNORE:
                return true;

            case COMPARE_NUMBER:
                // Voluntary break-through.
            case COMPARE_TYPE:
                // There's only one platform-tools so the preview number doesn't matter;
                // however previews can only match previews by default so both cases
                // are treated the same.
                return pkg.getRevision().isPreview() == getRevision().isPreview();
            }
        }
        return false;
    }

    /**
     * Hook called right before an archive is installed.
     * This is used here to stop ADB before trying to replace the platform-tool package.
     *
     * @param archive The archive that will be installed
     * @param monitor The {@link ITaskMonitor} to display errors.
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param installFolder The folder where the archive will be installed. Note that this
     *                      is <em>not</em> the folder where the archive was temporary
     *                      unzipped. The installFolder, if it exists, contains the old
     *                      archive that will soon be replaced by the new one.
     * @return True if installing this archive shall continue, false if it should be skipped.
     */
    @Override
    public boolean preInstallHook(Archive archive, ITaskMonitor monitor,
            String osSdkRoot, File installFolder) {
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }

}
