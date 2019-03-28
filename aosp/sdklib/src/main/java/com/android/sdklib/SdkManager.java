/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.sdklib;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.prefs.AndroidLocation;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.androidTarget.AddOnTarget;
import com.android.sdklib.internal.androidTarget.PlatformTarget;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgType;
import com.android.sdklib.repository.local.LocalExtraPkgInfo;
import com.android.sdklib.repository.local.LocalPkgInfo;
import com.android.sdklib.repository.local.LocalSdk;
import com.android.utils.ILogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The SDK manager parses the SDK folder and gives access to the content.
 * @see PlatformTarget
 * @see AddOnTarget
 */
public class SdkManager {

    @SuppressWarnings("unused")
    private static final boolean DEBUG = System.getenv("SDKMAN_DEBUG") != null;        //$NON-NLS-1$

    /** Preference file containing the usb ids for adb */
    private static final String ADB_INI_FILE = "adb_usb.ini";                          //$NON-NLS-1$
       //0--------90--------90--------90--------90--------90--------90--------90--------9
       private static final String ADB_INI_HEADER =
        "# ANDROID 3RD PARTY USB VENDOR ID LIST -- DO NOT EDIT.\n" +                   //$NON-NLS-1$
        "# USE 'android update adb' TO GENERATE.\n" +                                  //$NON-NLS-1$
        "# 1 USB VENDOR ID PER LINE.\n";                                               //$NON-NLS-1$

    /** Embedded reference to the new local SDK object. */
    private final LocalSdk mLocalSdk;

    /**
     * Create a new {@link SdkManager} instance.
     * External users should use {@link #createManager(String, ILogger)}.
     *
     * @param osSdkPath the location of the SDK.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected SdkManager(@NonNull String osSdkPath) {
        mLocalSdk = new LocalSdk(new File(osSdkPath));
    }

    /**
     * Creates an @{linkplain SdkManager} for an existing @{link LocalSdk}.
     *
     * @param localSdk the SDK to use with the SDK manager
     */
    private SdkManager(@NonNull LocalSdk localSdk) {
        mLocalSdk = localSdk;
    }

    /**
     * Creates an {@link SdkManager} for a given sdk location.
     * @param osSdkPath the location of the SDK.
     * @param log the ILogger object receiving warning/error from the parsing.
     * @return the created {@link SdkManager} or null if the location is not valid.
     */
    @Nullable
    public static SdkManager createManager(
            @NonNull String osSdkPath,
            @NonNull ILogger log) {
        try {
            SdkManager manager = new SdkManager(osSdkPath);
            manager.reloadSdk(log);

            return manager;
        } catch (Throwable throwable) {
            log.error(throwable, "Error parsing the sdk.");
        }

        return null;
    }

    /**
     * Creates an @{linkplain SdkManager} for an existing @{link LocalSdk}.
     *
     * @param localSdk the SDK to use with the SDK manager
     */
    @NonNull
    public static SdkManager createManager(@NonNull LocalSdk localSdk) {
        return new SdkManager(localSdk);
    }

    @NonNull
    public LocalSdk getLocalSdk() {
        return mLocalSdk;
    }

    /**
     * Reloads the content of the SDK.
     *
     * @param log the ILogger object receiving warning/error from the parsing.
     */
    public void reloadSdk(@NonNull ILogger log) {
        mLocalSdk.clearLocalPkg(PkgType.PKG_ALL);
    }

    /**
     * Checks whether any of the SDK platforms/add-ons/build-tools have changed on-disk
     * since we last loaded the SDK. This does not reload the SDK nor does it
     * change the underlying targets.
     *
     * @return True if at least one directory or source.prop has changed.
     */
    public boolean hasChanged() {
        return hasChanged(null);
    }

    /**
     * Checks whether any of the SDK platforms/add-ons/build-tools have changed on-disk
     * since we last loaded the SDK. This does not reload the SDK nor does it
     * change the underlying targets.
     *
     * @param log An optional logger used to print verbose info on what changed. Can be null.
     * @return True if at least one directory or source.prop has changed.
     */
    public boolean hasChanged(@Nullable ILogger log) {
        return mLocalSdk.hasChanged(EnumSet.of(PkgType.PKG_PLATFORM,
                                               PkgType.PKG_ADDON,
                                               PkgType.PKG_BUILD_TOOLS));
    }

    /**
     * Returns the location of the SDK.
     */
    @NonNull
    public String getLocation() {
        File f = mLocalSdk.getLocation();
        // Our LocalSdk is created with a file path, so we know the location won't be null.
        assert f != null;
        return f.getPath();
    }

    /**
     * Returns the targets (platforms & addons) that are available in the SDK.
     * The target list is created on demand the first time then cached.
     * It will not refreshed unless {@link #reloadSdk(ILogger)} is called.
     * <p/>
     * The array can be empty but not null.
     */
    @NonNull
    public IAndroidTarget[] getTargets() {
        return mLocalSdk.getTargets();
    }

    /**
     * Returns an unmodifiable set of known build-tools revisions. Can be empty but not null.
     * Deprecated. I don't think anything uses this.
     */
    @Deprecated
    @NonNull
    public Set<FullRevision> getBuildTools() {
        LocalPkgInfo[] pkgs = mLocalSdk.getPkgsInfos(PkgType.PKG_BUILD_TOOLS);
        TreeSet<FullRevision> bt = new TreeSet<FullRevision>();
        for (LocalPkgInfo pkg : pkgs) {
            IPkgDesc d = pkg.getDesc();
            if (d.hasFullRevision()) {
                bt.add(d.getFullRevision());
            }
        }
        return Collections.unmodifiableSet(bt);
    }

    /**
     * Returns the highest build-tool revision known. Can be null.
     *
     * @return The highest build-tool revision known, or null.
     */
    @Nullable
    public BuildToolInfo getLatestBuildTool() {
        return mLocalSdk.getLatestBuildTool();
    }

    /**
     * Returns the {@link BuildToolInfo} for the given revision.
     *
     * @param revision The requested revision.
     * @return A {@link BuildToolInfo}. Can be null if {@code revision} is null or is
     *  not part of the known set returned by {@link #getBuildTools()}.
     */
    @Nullable
    public BuildToolInfo getBuildTool(@Nullable FullRevision revision) {
        return mLocalSdk.getBuildTool(revision);
    }

    /**
     * Returns a target from a hash that was generated by {@link IAndroidTarget#hashString()}.
     *
     * @param hash the {@link IAndroidTarget} hash string.
     * @return The matching {@link IAndroidTarget} or null.
     */
    @Nullable
    public IAndroidTarget getTargetFromHashString(@Nullable String hash) {
        return mLocalSdk.getTargetFromHashString(hash);
    }

    /**
     * Updates adb with the USB devices declared in the SDK add-ons.
     * @throws AndroidLocationException
     * @throws IOException
     */
    public void updateAdb() throws AndroidLocationException, IOException {
        FileWriter writer = null;
        try {
            // get the android prefs location to know where to write the file.
            File adbIni = new File(AndroidLocation.getFolder(), ADB_INI_FILE);
            writer = new FileWriter(adbIni);

            // first, put all the vendor id in an HashSet to remove duplicate.
            HashSet<Integer> set = new HashSet<Integer>();
            IAndroidTarget[] targets = getTargets();
            for (IAndroidTarget target : targets) {
                if (target.getUsbVendorId() != IAndroidTarget.NO_USB_ID) {
                    set.add(target.getUsbVendorId());
                }
            }

            // write file header.
            writer.write(ADB_INI_HEADER);

            // now write the Id in a text file, one per line.
            for (Integer i : set) {
                writer.write(String.format("0x%04x\n", i));                            //$NON-NLS-1$
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Returns the greatest {@link LayoutlibVersion} found amongst all platform
     * targets currently loaded in the SDK.
     * <p/>
     * We only started recording Layoutlib Versions recently in the platform meta data
     * so it's possible to have an SDK with many platforms loaded but no layoutlib
     * version defined.
     *
     * @return The greatest {@link LayoutlibVersion} or null if none is found.
     * @deprecated This does NOT solve the right problem and will be changed later.
     */
    @Deprecated
    @Nullable
    public LayoutlibVersion getMaxLayoutlibVersion() {
        LayoutlibVersion maxVersion = null;

        for (IAndroidTarget target : getTargets()) {
            if (target instanceof PlatformTarget) {
                LayoutlibVersion lv = ((PlatformTarget) target).getLayoutlibVersion();
                if (lv != null) {
                    if (maxVersion == null || lv.compareTo(maxVersion) > 0) {
                        maxVersion = lv;
                    }
                }
            }
        }

        return maxVersion;
    }

    /**
     * Returns a map of the <em>root samples directories</em> located in the SDK/extras packages.
     * No guarantee is made that the extras' samples directory actually contain any valid samples.
     * The only guarantee is that the root samples directory actually exists.
     * The map is { File: Samples root directory => String: Extra package display name. }
     *
     * @return A non-null possibly empty map of extra samples directories and their associated
     *   extra package display name.
     */
    @NonNull
    public Map<File, String> getExtraSamples() {

        LocalPkgInfo[] pkgsInfos = mLocalSdk.getPkgsInfos(PkgType.PKG_EXTRA);
        Map<File, String> samples = new HashMap<File, String>();

        for (LocalPkgInfo info : pkgsInfos) {
            assert info instanceof LocalExtraPkgInfo;

            File root = info.getLocalDir();
            File path = new File(root, SdkConstants.FD_SAMPLES);
            if (path.isDirectory()) {
                samples.put(path, info.getListDescription());
                continue;
            }
            // Some old-style extras simply have a single "sample" directory.
            // Accept it if it contains an AndroidManifest.xml.
            path = new File(root, SdkConstants.FD_SAMPLE);
            if (path.isDirectory() &&
                    new File(path, SdkConstants.FN_ANDROID_MANIFEST_XML).isFile()) {
                samples.put(path, info.getListDescription());
            }
        }

        return samples;
    }

    /**
     * Returns a map of all the extras found in the <em>local</em> SDK with their major revision.
     * <p/>
     * Map keys are in the form "vendor-id/path-id". These ids uniquely identify an extra package.
     * The version is the incremental integer major revision of the package.
     *
     * @return A non-null possibly empty map of { string "vendor/path" => integer major revision }
     * @deprecated Starting with add-on schema 6, extras can have full revisions instead of just
     *   major revisions. This API only returns the major revision. Callers should be modified
     *   to use the new {code LocalSdk.getPkgInfo(PkgType.PKG_EXTRAS)} API instead.
     */
    @Deprecated
    @NonNull
    public Map<String, Integer> getExtrasVersions() {
        LocalPkgInfo[] pkgsInfos = mLocalSdk.getPkgsInfos(PkgType.PKG_EXTRA);
        Map<String, Integer> extraVersions = new TreeMap<String, Integer>();

        for (LocalPkgInfo info : pkgsInfos) {
            assert info instanceof LocalExtraPkgInfo;
            if (info instanceof LocalExtraPkgInfo) {
                LocalExtraPkgInfo ei = (LocalExtraPkgInfo) info;
                IPkgDesc d = ei.getDesc();
                String vendor = d.getVendor().getId();
                String path   = d.getPath();
                int majorRev  = d.getFullRevision().getMajor();

                extraVersions.put(vendor + '/' + path, majorRev);
            }
        }

        return extraVersions;
    }

    /** Returns the platform tools version if installed, null otherwise. */
    @Nullable
    public String getPlatformToolsVersion() {
        LocalPkgInfo info = mLocalSdk.getPkgInfo(PkgType.PKG_PLATFORM_TOOLS);
        IPkgDesc d = info == null ? null : info.getDesc();
        if (d != null && d.hasFullRevision()) {
            return d.getFullRevision().toShortString();
        }

        return null;
    }


    // -------------

    public static class LayoutlibVersion implements Comparable<LayoutlibVersion> {
        private final int mApi;
        private final int mRevision;

        public static final int NOT_SPECIFIED = 0;

        public LayoutlibVersion(int api, int revision) {
            mApi = api;
            mRevision = revision;
        }

        public int getApi() {
            return mApi;
        }

        public int getRevision() {
            return mRevision;
        }

        @Override
        public int compareTo(@NonNull LayoutlibVersion rhs) {
            boolean useRev = this.mRevision > NOT_SPECIFIED && rhs.mRevision > NOT_SPECIFIED;
            int lhsValue = (this.mApi << 16) + (useRev ? this.mRevision : 0);
            int rhsValue = (rhs.mApi  << 16) + (useRev ? rhs.mRevision  : 0);
            return lhsValue - rhsValue;
        }
    }
}
