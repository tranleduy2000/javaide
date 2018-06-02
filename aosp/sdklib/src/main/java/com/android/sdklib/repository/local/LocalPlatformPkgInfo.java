/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sdklib.repository.local;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.IAndroidTarget.OptionalLibrary;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.ISystemImage.LocationType;
import com.android.sdklib.SdkManager.LayoutlibVersion;
import com.android.sdklib.SystemImage;
import com.android.sdklib.internal.androidTarget.OptionalLibraryImpl;
import com.android.sdklib.internal.androidTarget.PlatformTarget;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.PkgProps;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.sdklib.repository.descriptors.PkgDesc;
import com.android.sdklib.repository.descriptors.PkgType;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("ConstantConditions")
public class LocalPlatformPkgInfo extends LocalPkgInfo {

    public static final String PROP_VERSION_SDK      = "ro.build.version.sdk";      //$NON-NLS-1$
    public static final String PROP_VERSION_CODENAME = "ro.build.version.codename"; //$NON-NLS-1$
    public static final String PROP_VERSION_RELEASE  = "ro.build.version.release";  //$NON-NLS-1$

    @NonNull
    private final IPkgDesc mDesc;

    /** Android target, lazyly loaded from #getAndroidTarget */
    private IAndroidTarget mTarget;
    private boolean mLoaded;

    public LocalPlatformPkgInfo(@NonNull LocalSdk localSdk,
                                @NonNull File localDir,
                                @NonNull Properties sourceProps,
                                @NonNull AndroidVersion version,
                                @NonNull MajorRevision revision,
                                @NonNull FullRevision minToolsRev) {
        super(localSdk, localDir, sourceProps);
        mDesc = PkgDesc.Builder.newPlatform(version, revision, minToolsRev).create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mDesc;
    }

    /** The "path" of a Platform is its Target Hash. */
    @NonNull
    public String getTargetHash() {
        return getDesc().getPath();
    }

    @Nullable
    public IAndroidTarget getAndroidTarget() {
        if (!mLoaded) {
            mTarget = createAndroidTarget();
            mLoaded = true;
        }
        return mTarget;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    //-----

    /**
     * Creates the PlatformTarget. Invoked by {@link #getAndroidTarget()}.
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected IAndroidTarget createAndroidTarget() {
        LocalSdk sdk = getLocalSdk();
        IFileOp fileOp = sdk.getFileOp();
        File platformFolder = getLocalDir();
        File buildProp = new File(platformFolder, SdkConstants.FN_BUILD_PROP);
        File sourcePropFile = new File(platformFolder, SdkConstants.FN_SOURCE_PROP);

        if (!fileOp.isFile(buildProp) || !fileOp.isFile(sourcePropFile)) {
            appendLoadError("Ignoring platform '%1$s': %2$s is missing.",   //$NON-NLS-1$
                    platformFolder.getName(),
                    SdkConstants.FN_BUILD_PROP);
            return null;
        }

        Map<String, String> platformProp = new HashMap<String, String>();

        // add all the property files
        Map<String, String> map = null;

        try {
            map = ProjectProperties.parsePropertyStream(
                    fileOp.newFileInputStream(buildProp),
                    buildProp.getPath(),
                    null /*log*/);
            if (map != null) {
                platformProp.putAll(map);
            }
        } catch (FileNotFoundException ignore) {}

        try {
            map = ProjectProperties.parsePropertyStream(
                    fileOp.newFileInputStream(sourcePropFile),
                    sourcePropFile.getPath(),
                    null /*log*/);
            if (map != null) {
                platformProp.putAll(map);
            }
        } catch (FileNotFoundException ignore) {}

        File sdkPropFile = new File(platformFolder, SdkConstants.FN_SDK_PROP);
        if (fileOp.isFile(sdkPropFile)) { // obsolete platforms don't have this.
            try {
                map = ProjectProperties.parsePropertyStream(
                        fileOp.newFileInputStream(sdkPropFile),
                        sdkPropFile.getPath(),
                        null /*log*/);
                if (map != null) {
                    platformProp.putAll(map);
                }
            } catch (FileNotFoundException ignore) {}
        }

        // look for some specific values in the map.

        // api level
        int apiNumber;
        String stringValue = platformProp.get(PROP_VERSION_SDK);
        if (stringValue == null) {
            appendLoadError("Ignoring platform '%1$s': %2$s is missing from '%3$s'",
                    platformFolder.getName(), PROP_VERSION_SDK,
                    SdkConstants.FN_BUILD_PROP);
            return null;
        } else {
            try {
                 apiNumber = Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                // looks like apiNumber does not parse to a number.
                // Ignore this platform.
                appendLoadError(
                        "Ignoring platform '%1$s': %2$s is not a valid number in %3$s.",
                        platformFolder.getName(), PROP_VERSION_SDK,
                        SdkConstants.FN_BUILD_PROP);
                return null;
            }
        }

        // Codename must be either null or a platform codename.
        // REL means it's a release version and therefore the codename should be null.
        AndroidVersion apiVersion =
            new AndroidVersion(apiNumber, platformProp.get(PROP_VERSION_CODENAME));

        // version string
        String apiName = platformProp.get(PkgProps.PLATFORM_VERSION);
        if (apiName == null) {
            apiName = platformProp.get(PROP_VERSION_RELEASE);
        }
        if (apiName == null) {
            appendLoadError(
                    "Ignoring platform '%1$s': %2$s is missing from '%3$s'",
                    platformFolder.getName(), PROP_VERSION_RELEASE,
                    SdkConstants.FN_BUILD_PROP);
            return null;
        }

        // platform rev number & layoutlib version are extracted from the source.properties
        // saved by the SDK Manager when installing the package.

        int revision = 1;
        LayoutlibVersion layoutlibVersion = null;
        try {
            revision = Integer.parseInt(platformProp.get(PkgProps.PKG_REVISION));
        } catch (NumberFormatException e) {
            // do nothing, we'll keep the default value of 1.
        }

        try {
            String propApi = platformProp.get(PkgProps.LAYOUTLIB_API);
            String propRev = platformProp.get(PkgProps.LAYOUTLIB_REV);
            int llApi = propApi == null ? LayoutlibVersion.NOT_SPECIFIED :
                                          Integer.parseInt(propApi);
            int llRev = propRev == null ? LayoutlibVersion.NOT_SPECIFIED :
                                          Integer.parseInt(propRev);
            if (llApi > LayoutlibVersion.NOT_SPECIFIED &&
                    llRev >= LayoutlibVersion.NOT_SPECIFIED) {
                layoutlibVersion = new LayoutlibVersion(llApi, llRev);
            }
        } catch (NumberFormatException e) {
            // do nothing, we'll ignore the layoutlib version if it's invalid
        }

        // api number and name look valid, perform a few more checks
        String err = checkPlatformContent(fileOp, platformFolder);
        if (err != null) {
            appendLoadError("%s", err); //$NLN-NLS-1$
            return null;
        }

        ISystemImage[] systemImages = getPlatformSystemImages(fileOp, platformFolder, apiVersion);

        // create the target.
        PlatformTarget pt = new PlatformTarget(
                sdk.getLocation().getPath(),
                platformFolder.getAbsolutePath(),
                apiVersion,
                apiName,
                revision,
                layoutlibVersion,
                systemImages,
                platformProp,
                getOptionalLibraries(platformFolder),
                sdk.getLatestBuildTool());

        // add the skins from the platform. Make a copy to not modify the original collection.
        List<File> skins = new ArrayList<File>(PackageParserUtils.parseSkinFolder(pt.getFile(IAndroidTarget.SKINS), fileOp));

        // add the system-image specific skins, if any.
        for (ISystemImage systemImage : systemImages) {
            skins.addAll(Arrays.asList(systemImage.getSkins()));
        }

        pt.setSkins(skins.toArray(new File[skins.size()]));

        // add path to the non-legacy samples package if it exists
        LocalPkgInfo samples = sdk.getPkgInfo(PkgType.PKG_SAMPLE, getDesc().getAndroidVersion());
        if (samples != null) {
            pt.setSamplesPath(samples.getLocalDir().getAbsolutePath());
        }

        // add path to the non-legacy sources package if it exists
        LocalPkgInfo sources = sdk.getPkgInfo(PkgType.PKG_SOURCE, getDesc().getAndroidVersion());
        if (sources != null) {
            pt.setSourcesPath(sources.getLocalDir().getAbsolutePath());
        }

        return pt;
    }

    /**
     * Get all the system images supported by a platform target.
     * For a platform, we first look in the new sdk/system-images folders then we
     * look for sub-folders in the platform/images directory and/or the one legacy
     * folder.
     * If any given API appears twice or more, the first occurrence wins.
     *
     * @param fileOp File operation wrapper.
     * @param platformDir Root of the platform target being loaded.
     * @param apiVersion API level + codename of platform being loaded.
     * @return an array of ISystemImage containing all the system images for the target.
     *              The list can be empty but not null.
     */
    @NonNull
    private ISystemImage[] getPlatformSystemImages(IFileOp fileOp,
                                                   File platformDir,
                                                   AndroidVersion apiVersion) {
        Set<ISystemImage> found = new TreeSet<ISystemImage>();
        SetMultimap<IdDisplay, String> tagToAbiFound = TreeMultimap.create();


        // Look in the SDK/system-image/platform-n/tag/abi folders.
        // Look in the SDK/system-image/platform-n/abi folders.
        // If we find multiple occurrences of the same platform/abi, the first one read wins.

        LocalPkgInfo[] sysImgInfos = getLocalSdk().getPkgsInfos(PkgType.PKG_SYS_IMAGE);
        for (LocalPkgInfo pkg : sysImgInfos) {
            IPkgDesc d = pkg.getDesc();
            if (pkg instanceof LocalSysImgPkgInfo &&
                    !d.hasVendor() &&
                    apiVersion.equals(d.getAndroidVersion())) {
                IdDisplay tag = d.getTag();
                String abi = d.getPath();
                if (tag != null && abi != null && !tagToAbiFound.containsEntry(tag, abi)) {
                    found.add(((LocalSysImgPkgInfo)pkg).getSystemImage());
                    tagToAbiFound.put(tag, abi);
                }
            }
        }

        // Look in either the platform/images/abi or the legacy folder
        File imgDir = new File(platformDir, SdkConstants.OS_IMAGES_FOLDER);
        File[] files =  fileOp.listFiles(imgDir);
        boolean useLegacy = true;
        boolean hasImgFiles = false;
        final IdDisplay defaultTag = SystemImage.DEFAULT_TAG;

        // Look for sub-directories
        for (File file : files) {
            if (fileOp.isDirectory(file)) {
                useLegacy = false;
                String abi = file.getName();
                if (!tagToAbiFound.containsEntry(defaultTag, abi)) {
                    found.add(new SystemImage(
                            file,
                            LocationType.IN_IMAGES_SUBFOLDER,
                            defaultTag,
                            abi,
                            FileOp.EMPTY_FILE_ARRAY));
                    tagToAbiFound.put(defaultTag, abi);
                }
            } else if (!hasImgFiles && fileOp.isFile(file)) {
                if (file.getName().endsWith(".img")) {      //$NON-NLS-1$
                    hasImgFiles = true;
                }
            }
        }

        if (useLegacy &&
                hasImgFiles &&
                fileOp.isDirectory(imgDir) &&
                !tagToAbiFound.containsEntry(defaultTag, SdkConstants.ABI_ARMEABI)) {
            // We found no sub-folder system images but it looks like the top directory
            // has some img files in it. It must be a legacy ARM EABI system image folder.
            found.add(new SystemImage(
                    imgDir,
                    LocationType.IN_LEGACY_FOLDER,
                    defaultTag,
                    SdkConstants.ABI_ARMEABI,
                    FileOp.EMPTY_FILE_ARRAY));
        }

        return found.toArray(new ISystemImage[found.size()]);
    }

    private List<OptionalLibrary> getOptionalLibraries(@NonNull File platformDir) {
        File optionalDir = new File(platformDir, "optional");
        if (!optionalDir.isDirectory()) {
            return Collections.emptyList();
        }

        File optionalJson = new File(optionalDir, "optional.json");
        if (!optionalJson.isFile()) {
            return Collections.emptyList();
        }

        return getLibsFromJson(optionalJson);
    }

    public static class Library {
        String name;
        String jar;
        boolean manifest;
    }


    @VisibleForTesting
    static List<OptionalLibrary> getLibsFromJson(@NonNull File jsonFile) {

        Gson gson = new Gson();

        try {
            Type collectionType = new TypeToken<Collection<Library>>() {
            }.getType();
            Collection<Library> libs = gson
                    .fromJson(Files.newReader(jsonFile, Charsets.UTF_8), collectionType);

            // convert into the right format.
            List<OptionalLibrary> optionalLibraries = Lists.newArrayListWithCapacity(libs.size());

            File rootFolder = jsonFile.getParentFile();
            for (Library lib : libs) {
                optionalLibraries.add(new OptionalLibraryImpl(
                        lib.name,
                        new File(rootFolder, lib.jar),
                        lib.name,
                        lib.manifest));
            }

            return optionalLibraries;
        } catch (FileNotFoundException e) {
            // shouldn't happen since we've checked the file is here, but can happen in
            // some cases (too many files open).
            return Collections.emptyList();
        }
    }

    /** List of items in the platform to check when parsing it. These paths are relative to the
     * platform root folder. */
    private static final String[] sPlatformContentList = new String[] {
        SdkConstants.FN_FRAMEWORK_LIBRARY,
        SdkConstants.FN_FRAMEWORK_AIDL,
    };

    /**
     * Checks the given platform has all the required files, and returns true if they are all
     * present.
     * <p/>This checks the presence of the following files: android.jar, framework.aidl, aapt(.exe),
     * aidl(.exe), dx(.bat), and dx.jar
     *
     * @param fileOp File operation wrapper.
     * @param platform The folder containing the platform.
     * @return An error description if platform is rejected; null if no error is detected.
     */
    @NonNull
    private static String checkPlatformContent(IFileOp fileOp, @NonNull File platform) {
        for (String relativePath : sPlatformContentList) {
            File f = new File(platform, relativePath);
            if (!fileOp.exists(f)) {
                return String.format(
                        "Ignoring platform '%1$s': %2$s is missing.",                  //$NON-NLS-1$
                        platform.getName(), relativePath);
            }
        }
        return null;
    }
}
