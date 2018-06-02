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
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.ISystemImage.LocationType;
import com.android.sdklib.SystemImage;
import com.android.sdklib.internal.androidTarget.AddOnTarget;
import com.android.sdklib.internal.androidTarget.PlatformTarget;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.repository.AddonManifestIniProps;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.descriptors.*;
import com.android.utils.Pair;
import com.google.common.base.Objects;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("MethodMayBeStatic")
public class LocalAddonPkgInfo extends LocalPlatformPkgInfo {

    private static final Pattern PATTERN_LIB_DATA = Pattern.compile(
            "^([a-zA-Z0-9._-]+\\.jar);(.*)$", Pattern.CASE_INSENSITIVE);    //$NON-NLS-1$

    // usb ids are 16-bit hexadecimal values.
    private static final Pattern PATTERN_USB_IDS = Pattern.compile(
           "^0x[a-f0-9]{4}$", Pattern.CASE_INSENSITIVE);                    //$NON-NLS-1$

    @NonNull
    private final IPkgDescAddon mAddonDesc;

    public LocalAddonPkgInfo(@NonNull LocalSdk localSdk,
                             @NonNull File localDir,
                             @NonNull Properties sourceProps,
                             @NonNull AndroidVersion version,
                             @NonNull MajorRevision revision,
                             @NonNull IdDisplay vendor,
                             @NonNull IdDisplay name) {
        super(localSdk, localDir, sourceProps, version, revision, FullRevision.NOT_SPECIFIED);
        mAddonDesc = (IPkgDescAddon) PkgDesc.Builder.newAddon(version, revision, vendor, name)
                                                    .create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mAddonDesc;
    }

    /** The "path" of an add-on is its Target Hash. */
    @Override
    @NonNull
    public String getTargetHash() {
        return getDesc().getPath();
    }

    //-----

    /**
     * Computes a sanitized name-id based on an addon name-display.
     * This is used to provide compatibility with older add-ons that lacks the new fields.
     *
     * @param displayName A name-display field or a old-style name field.
     * @return A non-null sanitized name-id that fits in the {@code [a-zA-Z0-9_-]+} pattern.
     */
    public static String sanitizeDisplayToNameId(@NonNull String displayName) {
        String name = displayName.toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z0-9_-]+", "_");      //$NON-NLS-1$ //$NON-NLS-2$
        name = name.replaceAll("_+", "_");                //$NON-NLS-1$ //$NON-NLS-2$

        // Trim leading and trailing underscores
        if (name.length() > 1) {
            name = name.replaceAll("^_+", "");            //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (name.length() > 1) {
            name = name.replaceAll("_+$", "");            //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;
    }

    //-----

    /**
     * Creates the AddOnTarget. Invoked by {@link #getAndroidTarget()}.
     */
    @Override
    @Nullable
    protected IAndroidTarget createAndroidTarget() {
        LocalSdk sdk = getLocalSdk();
        IFileOp fileOp = sdk.getFileOp();

        // Parse the addon properties to ensure we can load it.
        Pair<Map<String, String>, String> infos = parseAddonProperties();

        Map<String, String> propertyMap = infos.getFirst();
        String error = infos.getSecond();

        if (error != null) {
            appendLoadError("Ignoring add-on '%1$s': %2$s", getLocalDir().getName(), error);
            return null;
        }

        // Since error==null we're not supposed to encounter any issues loading this add-on.
        try {
            assert propertyMap != null;

            String api = propertyMap.get(AddonManifestIniProps.ADDON_API);
            String name = propertyMap.get(AddonManifestIniProps.ADDON_NAME);
            String vendor = propertyMap.get(AddonManifestIniProps.ADDON_VENDOR);

            assert api != null;
            assert name != null;
            assert vendor != null;

            PlatformTarget baseTarget = null;

            // Look for a platform that has a matching api level or codename.
            LocalPkgInfo plat = sdk.getPkgInfo(PkgType.PKG_PLATFORM,
                                               getDesc().getAndroidVersion());
            if (plat instanceof LocalPlatformPkgInfo) {
                baseTarget = (PlatformTarget) ((LocalPlatformPkgInfo) plat).getAndroidTarget();
            }
            assert baseTarget != null;

            // get the optional description
            String description = propertyMap.get(AddonManifestIniProps.ADDON_DESCRIPTION);

            // get the add-on revision
            int revisionValue = 1;
            String revision = propertyMap.get(AddonManifestIniProps.ADDON_REVISION);
            if (revision == null) {
                revision = propertyMap.get(AddonManifestIniProps.ADDON_REVISION_OLD);
            }
            if (revision != null) {
                revisionValue = Integer.parseInt(revision);
            }

            // get the optional libraries
            String librariesValue = propertyMap.get(AddonManifestIniProps.ADDON_LIBRARIES);
            Map<String, String[]> libMap = null;

            if (librariesValue != null) {
                librariesValue = librariesValue.trim();
                if (!librariesValue.isEmpty()) {
                    // split in the string into the libraries name
                    String[] libraries = librariesValue.split(";");     //$NON-NLS-1$
                    if (libraries.length > 0) {
                        libMap = new HashMap<String, String[]>();
                        for (String libName : libraries) {
                            libName = libName.trim();

                            // get the library data from the properties
                            String libData = propertyMap.get(libName);

                            if (libData != null) {
                                // split the jar file from the description
                                Matcher m = PATTERN_LIB_DATA.matcher(libData);
                                if (m.matches()) {
                                    libMap.put(libName, new String[] {
                                            m.group(1), m.group(2) });
                                } else {
                                    appendLoadError(
                                            "Ignoring library '%1$s', property value has wrong format\n\t%2$s",
                                            libName, libData);
                                }
                            } else {
                                appendLoadError(
                                        "Ignoring library '%1$s', missing property value",
                                        libName, libData);
                            }
                        }
                    }
                }
            }

            // get the abi list.
            ISystemImage[] systemImages = getAddonSystemImages(fileOp);

            // check whether the add-on provides its own rendering info/library.
            boolean hasRenderingLibrary = false;
            boolean hasRenderingResources = false;

            File dataFolder = new File(getLocalDir(), SdkConstants.FD_DATA);
            if (fileOp.isDirectory(dataFolder)) {
                hasRenderingLibrary =
                    fileOp.isFile(new File(dataFolder, SdkConstants.FN_LAYOUTLIB_JAR));
                hasRenderingResources =
                    fileOp.isDirectory(new File(dataFolder, SdkConstants.FD_RES)) &&
                    fileOp.isDirectory(new File(dataFolder, SdkConstants.FD_FONTS));
            }

            AddOnTarget target = new AddOnTarget(
                    getLocalDir().getAbsolutePath(),
                    name,
                    vendor,
                    revisionValue,
                    description,
                    systemImages,
                    libMap,
                    hasRenderingLibrary,
                    hasRenderingResources,
                    baseTarget);

            // parse the legacy skins, located under SDK/addons/addon-name/skins/[skin-name]
            // and merge with the system-image skins, if any, merging them by name.
            File targetSkinFolder = target.getFile(IAndroidTarget.SKINS);

            Map<String, File> skinsMap = new TreeMap<String, File>();

            for (File f : PackageParserUtils.parseSkinFolder(targetSkinFolder, fileOp)) {
                skinsMap.put(f.getName().toLowerCase(Locale.US), f);
            }
            for (ISystemImage si : systemImages) {
                for (File f : si.getSkins()) {
                    skinsMap.put(f.getName().toLowerCase(Locale.US), f);
                }
            }

            List<File> skins = new ArrayList<File>(skinsMap.values());
            Collections.sort(skins);

            // get the default skin
            File defaultSkin = null;
            String defaultSkinName = propertyMap.get(AddonManifestIniProps.ADDON_DEFAULT_SKIN);
            if (defaultSkinName != null) {
                defaultSkin = new File(targetSkinFolder, defaultSkinName);
            } else {
                // No default skin name specified, use the first one from the addon
                // or the default from the platform.
                if (skins.size() == 1) {
                    defaultSkin = skins.get(0);
                } else {
                    defaultSkin = baseTarget.getDefaultSkin();
                }
            }

            // get the USB ID (if available)
            int usbVendorId = convertId(propertyMap.get(AddonManifestIniProps.ADDON_USB_VENDOR));
            if (usbVendorId != IAndroidTarget.NO_USB_ID) {
                target.setUsbVendorId(usbVendorId);
            }

            target.setSkins(skins.toArray(new File[skins.size()]), defaultSkin);

            return target;

        } catch (Exception e) {
            appendLoadError("Ignoring add-on '%1$s': error %2$s.",
                    getLocalDir().getName(), e.toString());
        }

        return null;

    }

    /**
     * Parses the add-on properties and decodes any error that occurs when loading an addon.
     *
     * @return A pair with the property map and an error string. Both can be null but not at the
     *  same time. If a non-null error is present then the property map must be ignored. The error
     *  should be translatable as it might show up in the SdkManager UI.
     */
    @NonNull
    private Pair<Map<String, String>, String> parseAddonProperties() {
        Map<String, String> propertyMap = null;
        String error = null;

        IFileOp fileOp = getLocalSdk().getFileOp();
        File addOnManifest = new File(getLocalDir(), SdkConstants.FN_MANIFEST_INI);

        do {
            if (!fileOp.isFile(addOnManifest)) {
                error = String.format("File not found: %1$s", SdkConstants.FN_MANIFEST_INI);
                break;
            }

            try {
                propertyMap = ProjectProperties.parsePropertyStream(
                        fileOp.newFileInputStream(addOnManifest),
                        addOnManifest.getPath(),
                        null /*log*/);
                if (propertyMap == null) {
                    error = String.format("Failed to parse properties from %1$s",
                            SdkConstants.FN_MANIFEST_INI);
                    break;
                }
            } catch (FileNotFoundException e) {
                // this can happen if the system fails to open the file because of too many
                // open files.
                error = String.format("Failed to parse properties from %1$s: %2$s",
                        SdkConstants.FN_MANIFEST_INI, e.getMessage());
                break;
            }

            // look for some specific values in the map.
            // we require name, vendor, and api
            String name = propertyMap.get(AddonManifestIniProps.ADDON_NAME);
            if (name == null) {
                error = addonManifestWarning(AddonManifestIniProps.ADDON_NAME);
                break;
            }

            String vendor = propertyMap.get(AddonManifestIniProps.ADDON_VENDOR);
            if (vendor == null) {
                error = addonManifestWarning(AddonManifestIniProps.ADDON_VENDOR);
                break;
            }

            String api = propertyMap.get(AddonManifestIniProps.ADDON_API);
            if (api == null) {
                error = addonManifestWarning(AddonManifestIniProps.ADDON_API);
                break;
            }

            // Look for a platform that has a matching api level or codename.
            IAndroidTarget baseTarget = null;
            LocalPkgInfo plat = getLocalSdk().getPkgInfo(PkgType.PKG_PLATFORM,
                                                         getDesc().getAndroidVersion());
            if (plat instanceof LocalPlatformPkgInfo) {
                baseTarget = ((LocalPlatformPkgInfo) plat).getAndroidTarget();
            }

            if (baseTarget == null) {
                error = String.format("Unable to find base platform with API level '%1$s'", api);
                break;
            }

            // get the add-on revision
            String revision = propertyMap.get(AddonManifestIniProps.ADDON_REVISION);
            if (revision == null) {
                revision = propertyMap.get(AddonManifestIniProps.ADDON_REVISION_OLD);
            }
            if (revision != null) {
                try {
                    Integer.parseInt(revision);
                } catch (NumberFormatException e) {
                    // looks like revision does not parse to a number.
                    error = String.format("%1$s is not a valid number in %2$s.",
                            AddonManifestIniProps.ADDON_REVISION, SdkConstants.FN_BUILD_PROP);
                    break;
                }
            }

        } while(false);

        return Pair.of(propertyMap, error);
    }

    /**
     * Prepares a warning about the addon being ignored due to a missing manifest value.
     * This string will show up in the SdkManager UI.
     *
     * @param valueName The missing manifest value, for display.
     */
    @NonNull
    private static String addonManifestWarning(@NonNull String valueName) {
        return String.format("'%1$s' is missing from %2$s.",
                valueName, SdkConstants.FN_MANIFEST_INI);
    }

    /**
     * Converts a string representation of an hexadecimal ID into an int.
     * @param value the string to convert.
     * @return the int value, or {@link IAndroidTarget#NO_USB_ID} if the conversion failed.
     */
    private int convertId(@Nullable String value) {
        if (value != null && !value.isEmpty()) {
            if (PATTERN_USB_IDS.matcher(value).matches()) {
                String v = value.substring(2);
                try {
                    return Integer.parseInt(v, 16);
                } catch (NumberFormatException e) {
                    // this shouldn't happen since we check the pattern above, but this is safer.
                    // the method will return 0 below.
                }
            }
        }

        return IAndroidTarget.NO_USB_ID;
    }

    /**
     * Get all the system images supported by an add-on target.
     * For an add-on,  we first look in the new sdk/system-images folders then we look
     * for sub-folders in the addon/images directory.
     * If none are found but the directory exists and is not empty, assume it's a legacy
     * arm eabi system image.
     * If any given API appears twice or more, the first occurrence wins.
     * <p/>
     * Note that it's OK for an add-on to have no system-images at all, since it can always
     * rely on the ones from its base platform.
     *
     * @param fileOp File operation wrapper.
     * @return an array of ISystemImage containing all the system images for the target.
     *              The list can be empty but not null.
    */
    @NonNull
    private ISystemImage[] getAddonSystemImages(IFileOp fileOp) {
        Set<ISystemImage> found = new TreeSet<ISystemImage>();
        SetMultimap<IdDisplay, String> tagToAbiFound = TreeMultimap.create();


        // Look in the system images folders:
        // - SDK/system-image/platform/addon-id-tag/abi
        // - SDK/system-image/addon-id-tag/abi (many abi possible)
        // Optional: look for skins under
        // - SDK/system-image/platform/addon-id-tag/abi/skins/skin-name
        // - SDK/system-image/addon-id-tag/abi/skins/skin-name
        // If we find multiple occurrences of the same platform/abi, the first one read wins.

        LocalPkgInfo[] sysImgInfos = getLocalSdk().getPkgsInfos(PkgType.PKG_ADDON_SYS_IMAGE);
        for (LocalPkgInfo pkg : sysImgInfos) {
            IPkgDesc d = pkg.getDesc();
            if (pkg instanceof LocalAddonSysImgPkgInfo &&
                    d.hasVendor() &&
                    mAddonDesc.getVendor().equals(d.getVendor()) &&
                    mAddonDesc.getName().equals(d.getTag()) &&
                    Objects.equal(mAddonDesc.getAndroidVersion(), pkg.getDesc().getAndroidVersion())) {
                final IdDisplay tag = mAddonDesc.getName();
                final String abi = d.getPath();
                if (abi != null && !tagToAbiFound.containsEntry(tag, abi)) {
                    found.add(((LocalAddonSysImgPkgInfo)pkg).getSystemImage());
                    tagToAbiFound.put(tag, abi);
                }
            }
        }

        // Look for sub-directories:
        // - SDK/addons/addon-name/images/abi (multiple abi possible)
        // - SDK/addons/addon-name/armeabi (legacy support)
        boolean useLegacy = true;
        boolean hasImgFiles = false;
        final IdDisplay defaultTag = SystemImage.DEFAULT_TAG;

        File imagesDir = new File(getLocalDir(), SdkConstants.OS_IMAGES_FOLDER);
        File[] files = fileOp.listFiles(imagesDir);
        for (File file : files) {
            if (fileOp.isDirectory(file)) {
                useLegacy = false;
                String abi = file.getName();
                if (!tagToAbiFound.containsEntry(defaultTag, abi)) {
                    found.add(new SystemImage(
                            file,
                            LocationType.IN_IMAGES_SUBFOLDER,
                            SystemImage.DEFAULT_TAG,
                            mAddonDesc.getVendor(),
                            abi,
                            FileOp.EMPTY_FILE_ARRAY));
                    tagToAbiFound.put(defaultTag, abi);
                }
            } else if (!hasImgFiles && fileOp.isFile(file)) {
                if (file.getName().endsWith(".img")) {                  //$NON-NLS-1$
                    // The legacy images folder is only valid if it contains some .img files
                    hasImgFiles = true;
                }
            }
        }

        if (useLegacy &&
                hasImgFiles &&
                fileOp.isDirectory(imagesDir) &&
                !tagToAbiFound.containsEntry(defaultTag, SdkConstants.ABI_ARMEABI)) {
            // We found no sub-folder system images but it looks like the top directory
            // has some img files in it. It must be a legacy ARM EABI system image folder.
            found.add(new SystemImage(
                    imagesDir,
                    LocationType.IN_LEGACY_FOLDER,
                    SystemImage.DEFAULT_TAG,
                    SdkConstants.ABI_ARMEABI,
                    FileOp.EMPTY_FILE_ARRAY));
        }

        return found.toArray(new ISystemImage[found.size()]);
    }
}
