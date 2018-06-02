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

import com.android.SdkConstants;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage.LocationType;
import com.android.sdklib.SdkManager;
import com.android.sdklib.SdkManagerTestCase;
import com.android.sdklib.SystemImage;
import com.android.sdklib.internal.androidTarget.PlatformTarget;
import com.android.sdklib.internal.repository.archives.ArchFilter;
import com.android.sdklib.internal.repository.archives.HostOs;
import com.android.sdklib.internal.repository.packages.Package;
import com.android.sdklib.io.FileOp;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

public class LocalSdkParserTest extends SdkManagerTestCase {

    private SdkManager mSdkMan;
    private LocalSdkParser mParser;
    private MockMonitor mMonitor;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mSdkMan = getSdkManager();
        mParser = new LocalSdkParser();
        mMonitor = new MockMonitor();
    }

    public void testLocalSdkParser_SystemImages() throws Exception {
        // By default SdkManagerTestCase creates an SDK with one platform containing
        // a legacy armeabi system image (this is not a separate system image package)

        assertEquals(
                "[Android SDK Tools, revision 1.0.1, " +
                 "Android SDK Platform-tools, revision 17.1.2, " +
                 "Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sources for Android SDK, API 0, revision 0]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(), mSdkMan, mMonitor))));

        assertEquals(
                "[SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sources for Android SDK, API 0, revision 0]",
                 Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                         mSdkMan,
                         LocalSdkParser.PARSE_PLATFORMS | LocalSdkParser.PARSE_SOURCES,
                         mMonitor))));

        assertEquals(
                "[SDK Platform Android 0.0, API 0, revision 1]",
                Arrays.toString(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_PLATFORMS,
                        mMonitor)));

        assertEquals(
                "[Sources for Android SDK, API 0, revision 0]",
                Arrays.toString(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_SOURCES,
                        mMonitor)));

        assertEquals(
                "[Android SDK Tools, revision 1.0.1]",
                Arrays.toString(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_TOOLS,
                        mMonitor)));

        assertEquals(
                "[Android SDK Platform-tools, revision 17.1.2]",
                Arrays.toString(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_PLATFORM_TOOLS,
                        mMonitor)));

        assertEquals(
                "[Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_BUILD_TOOLS,
                        mMonitor))));

        // Now add a few "platform subfolders" system images and reload the SDK.
        // This disables the "legacy" mode but it still doesn't create any system image package

        IAndroidTarget t = mSdkMan.getTargets()[0];
        makeSystemImageFolder(new SystemImage(mSdkMan, t,
                LocationType.IN_IMAGES_SUBFOLDER,
                SystemImage.DEFAULT_TAG,
                SdkConstants.ABI_ARMEABI_V7A,
                FileOp.EMPTY_FILE_ARRAY), null);
        makeSystemImageFolder(new SystemImage(mSdkMan, t,
                LocationType.IN_IMAGES_SUBFOLDER,
                SystemImage.DEFAULT_TAG,
                SdkConstants.ABI_INTEL_ATOM,
                FileOp.EMPTY_FILE_ARRAY), null);

        mSdkMan.reloadSdk(getLog());
        t = mSdkMan.getTargets()[0];

        assertEquals(
                "[Android SDK Tools, revision 1.0.1, " +
                 "Android SDK Platform-tools, revision 17.1.2, " +
                 "Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sources for Android SDK, API 0, revision 0]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(), mSdkMan, mMonitor))));

        // Now add arm + arm v7a images using the new SDK/system-images.
        // The local parser will find the 2 system image packages which are associated
        // with the PlatformTarget in the SdkManager.

        makeSystemImageFolder(new SystemImage(mSdkMan, t,
                LocationType.IN_SYSTEM_IMAGE,
                SystemImage.DEFAULT_TAG,
                SdkConstants.ABI_ARMEABI,
                FileOp.EMPTY_FILE_ARRAY), null);
        makeSystemImageFolder(new SystemImage(mSdkMan, t,
                LocationType.IN_SYSTEM_IMAGE,
                SystemImage.DEFAULT_TAG,
                SdkConstants.ABI_ARMEABI_V7A,
                FileOp.EMPTY_FILE_ARRAY), null);

        mSdkMan.reloadSdk(getLog());

        assertEquals(
                "[Android SDK Tools, revision 1.0.1, " +
                 "Android SDK Platform-tools, revision 17.1.2, " +
                 "Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sys-Img v0 for (Default, armeabi-v7a), Android API 0, revision 0, " +
                 "Sys-Img v0 for (Default, armeabi), Android API 0, revision 0, " +
                 "Sources for Android SDK, API 0, revision 0]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(), mSdkMan, mMonitor))));

        // Now add an x86 image using the new SDK/system-images.
        // Now this time we do NOT reload the SdkManager instance. Instead the parser
        // will find an unused system image and load it as a "broken package".

        makeSystemImageFolder(new SystemImage(mSdkMan, t,
                LocationType.IN_SYSTEM_IMAGE,
                SystemImage.DEFAULT_TAG,
                SdkConstants.ABI_INTEL_ATOM,
                FileOp.EMPTY_FILE_ARRAY), null);

        assertEquals(
                "[Android SDK Tools, revision 1.0.1, " +
                 "Android SDK Platform-tools, revision 17.1.2, " +
                 "Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sys-Img v0 for (Default, armeabi-v7a), Android API 0, revision 0, " +
                 "Sys-Img v0 for (Default, armeabi), Android API 0, revision 0, " +
                 "Sources for Android SDK, API 0, revision 0, " +
                 "Broken Intel x86 Atom System Image, API 0]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(), mSdkMan, mMonitor))));

        assertEquals(
                "[Android SDK Tools, revision 1.0.1, " +
                 "Android SDK Platform-tools, revision 17.1.2, " +
                 "Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sys-Img v0 for (Default, armeabi-v7a), Android API 0, revision 0, " +
                 "Sys-Img v0 for (Default, armeabi), Android API 0, revision 0, " +
                 "Sources for Android SDK, API 0, revision 0, " +
                 "Broken Intel x86 Atom System Image, API 0]",
                 Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                         mSdkMan,
                         LocalSdkParser.PARSE_ALL,
                         mMonitor))));

        assertEquals(
                "[SDK Platform Android 0.0, API 0, revision 1, " +
                 "Sys-Img v0 for (Default, armeabi-v7a), Android API 0, revision 0, " +
                 "Sys-Img v0 for (Default, armeabi), Android API 0, revision 0, " +
                 "Sources for Android SDK, API 0, revision 0, " +
                 "Broken Intel x86 Atom System Image, API 0]",
                 Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                         mSdkMan,
                         LocalSdkParser.PARSE_PLATFORMS | // platform also loads system-images
                                 LocalSdkParser.PARSE_SOURCES,
                         mMonitor))));

        assertEquals(
                "[Sources for Android SDK, API 0, revision 0]",
                 Arrays.toString(mParser.parseSdk(mSdkMan.getLocation(),
                         mSdkMan,
                         LocalSdkParser.PARSE_SOURCES,
                         mMonitor)));
    }

    public void testLocalSdkParser_Platform_DefaultSkin() throws Exception {
        IAndroidTarget[] targets = mSdkMan.getTargets();

        assertEquals(
                "[PlatformTarget API 0 rev 1]",
                Arrays.toString(targets));

        PlatformTarget p = (PlatformTarget) targets[0];

        assertEquals("[SDK/platforms/v0_0/skins/HVGA]",
                sanitizeInput(p.getSkins()));

        assertEquals("[SystemImage tag=default, ABI=armeabi, location in legacy folder='SDK/platforms/v0_0/images']",
                sanitizeInput(p.getSystemImages()));

        // the in-platform system image has no skins
        assertEquals("[]",
                sanitizeInput(p.getSystemImages()[0].getSkins()));
    }

    public void testLocalSdkParser_Platform_CustomSkin() throws Exception {
        // add a new-style system-image with a tag and an embedded custom skin
        File siArm = makeSystemImageFolder(TARGET_DIR_NAME_0, "tag-1", "x86");
        makeFakeSkin(siArm, "Tag1ArmSkin");

        IAndroidTarget[] targets = mSdkMan.getTargets();

        assertEquals(
                "[PlatformTarget API 0 rev 1]",
                Arrays.toString(targets));

        PlatformTarget p = (PlatformTarget) targets[0];

        assertEquals(
                "[SDK/platforms/v0_0/skins/HVGA, " +
                 "SDK/system-images/v0_0/tag-1/x86/skins/Tag1ArmSkin]",
                sanitizeInput(p.getSkins()));

        assertEquals(
                "[SystemImage tag=default, ABI=armeabi, location in legacy folder='SDK/platforms/v0_0/images', " +
                 "SystemImage tag=tag-1, ABI=x86, location in system image='SDK/system-images/v0_0/tag-1/x86']",
                sanitizeInput(p.getSystemImages()));

        // the in-platform system image has no skins, the second one has a custom skin
        assertEquals("[]",
                sanitizeInput(p.getSystemImages()[0].getSkins()));
        assertEquals("[SDK/system-images/v0_0/tag-1/x86/skins/Tag1ArmSkin]",
                sanitizeInput(p.getSystemImages()[1].getSkins()));
    }

    public void testLocalSdkParser_BuildTools_InvalidOs() throws Exception {
        assertEquals(
                "[Android SDK Build-tools, revision 18.3.4 rc5, " +
                 "Android SDK Build-tools, revision 3.0.1, " +
                 "Android SDK Build-tools, revision 3, " +
                 "Platform Tools, revision 17.1.2, Tools, revision 1.0.1]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_BUILD_TOOLS |
                                LocalSdkParser.PARSE_EXTRAS,
                        mMonitor))));

        // We have many OS possible. Choose 2 that do not match the current platform.
        ArchFilter current = ArchFilter.getCurrent();
        HostOs others[] = new HostOs[2];
        int i = 0;
        for (HostOs o : HostOs.values()) {
            if (o != current.getHostOS() && i < others.length) {
                others[i++] = o;
            }
        }
        createFakeBuildTools(new File(mSdkMan.getLocation()), others[0].toString(), "5.0.1");
        createFakeBuildTools(new File(mSdkMan.getLocation()), others[1].toString(), "5.0.2");

        assertEquals(
                "[Android SDK Build-tools, revision 18.3.4 rc5, " +
                "Android SDK Build-tools, revision 3.0.1, " +
                "Android SDK Build-tools, revision 3, " +
                "Broken Build-Tools Package, revision 5.0.2, " +
                "Broken Build-Tools Package, revision 5.0.1, " +
                "Platform Tools, revision 17.1.2, Tools, revision 1.0.1]",
                Arrays.toString(sort(mParser.parseSdk(mSdkMan.getLocation(),
                        mSdkMan,
                        LocalSdkParser.PARSE_BUILD_TOOLS |
                                LocalSdkParser.PARSE_EXTRAS,
                        mMonitor))));
    }

    private static Package[] sort(Package[] pkg) {
        // Sort packages to ensure stable unit test output
        pkg = Arrays.copyOf(pkg, pkg.length);
        Arrays.sort(pkg);
        return pkg;
    }

    private String sanitizeInput(Object[] array) {
        String input = Arrays.toString(array);
        String sdkPath = mSdkMan.getLocation();
        input = input.replaceAll(Pattern.quote(sdkPath), "SDK");
        input = input.replace(File.separatorChar, '/');
        return input;
    }
}

