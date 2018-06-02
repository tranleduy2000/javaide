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
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.BuildToolInfo.PathId;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.io.MockFileOp;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.descriptors.PkgType;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Pattern;

@SuppressWarnings("MethodMayBeStatic")
public class LocalSdkTest extends TestCase {

    private MockFileOp mFOp;
    private LocalSdk mLS;

    @Override
    protected void setUp() {
        mFOp = new MockFileOp();
        mLS = new LocalSdk(mFOp);
        mLS.setLocation(new File("/sdk"));
    }

    public final void testLocalSdkTest_allPkgTypes() {
        // Make sure getPkgInfo() can handle all defined package types.
        for(PkgType type : PkgType.values()) {
            mLS.getPkgsInfos(EnumSet.of(type));
        }

        // And do the same thing differently, using PKG_ALL
        assertNotNull(mLS.getPkgsInfos(PkgType.PKG_ALL));
    }

    public final void testLocalSdkTest_getLocation() {
        MockFileOp fop = new MockFileOp();
        LocalSdk ls = new LocalSdk(fop);
        assertNull(ls.getLocation());
        ls.setLocation(new File("/sdk"));
        assertEquals(new File("/sdk"), ls.getLocation());
    }

    public final void testLocalSdkTest_getPkgInfo_Tools() {
        // check empty
        assertNull(mLS.getPkgInfo(PkgType.PKG_TOOLS));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/tools");
        mFOp.recordExistingFile("/sdk/tools/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=22.3.4\n" +
                "Platform.MinPlatformToolsRev=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");
        mFOp.recordExistingFile("/sdk/tools/" + SdkConstants.androidCmdName(), "placeholder");
        mFOp.recordExistingFile("/sdk/tools/" + SdkConstants.FN_EMULATOR, "placeholder");

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_TOOLS);
        assertNotNull(pi);
        assertTrue(pi instanceof LocalToolPkgInfo);
        assertEquals(new File("/sdk/tools"), pi.getLocalDir());
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new FullRevision(22, 3, 4), pi.getDesc().getFullRevision());
        assertEquals(
                "<LocalToolPkgInfo <PkgDesc Type=tools FullRev=22.3.4 MinPlatToolsRev=18.0.0>>",
                pi.toString());
        assertEquals("Android SDK Tools 22.3.4", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));
    }

    public final void testLocalSdkTest_getPkgInfo_PlatformTools() {
        // check empty
        assertNull(mLS.getPkgInfo(PkgType.PKG_PLATFORM_TOOLS));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/platform-tools");
        mFOp.recordExistingFile("/sdk/platform-tools/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=18.19.20\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_PLATFORM_TOOLS);
        assertNotNull(pi);
        assertTrue(pi instanceof LocalPlatformToolPkgInfo);
        assertEquals(new File("/sdk/platform-tools"), pi.getLocalDir());
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new FullRevision(18, 19, 20), pi.getDesc().getFullRevision());
        assertEquals("<LocalPlatformToolPkgInfo <PkgDesc Type=platform_tools FullRev=18.19.20>>", pi.toString());
        assertEquals("Android SDK Platform-Tools 18.19.20", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));
    }

    public final void testLocalSdkTest_getPkgInfo_Docs() {
        // check empty
        assertNull(mLS.getPkgInfo(PkgType.PKG_DOC));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/docs");
        mFOp.recordExistingFile("/sdk/docs/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.Revision=2\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");
        mFOp.recordExistingFile("/sdk/docs/index.html", "placeholder");

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_DOC);
        assertNotNull(pi);
        assertTrue(pi instanceof LocalDocPkgInfo);
        assertEquals(new File("/sdk/docs"), pi.getLocalDir());
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new MajorRevision(2), pi.getDesc().getMajorRevision());
        assertEquals("<LocalDocPkgInfo <PkgDesc Type=doc Android=API 18 MajorRev=2>>", pi.toString());
        assertEquals("Documentation for Android SDK", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));
    }

    public final void testLocalSdkTest_getPkgInfo_BuildTools() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_BUILD_TOOLS)));

        // We haven't defined any mock build-tools so the API will return
        // a legacy build-tools based on top of platform tools if there's one with
        // a revision < 17.
        mFOp.recordExistingFolder("/sdk/platform-tools");
        mFOp.recordExistingFile("/sdk/platform-tools/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=16\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");

        // -- get latest build tool in legacy/compatibility mode

        BuildToolInfo bt = mLS.getLatestBuildTool();
        assertNotNull(bt);
        assertEquals(new FullRevision(16), bt.getRevision());
        assertEquals(new File("/sdk/platform-tools"), bt.getLocation());
        assertEquals("/sdk/platform-tools/" + SdkConstants.FN_AAPT,
                     mFOp.getAgnosticAbsPath(bt.getPath(PathId.AAPT)));

        // clearing local packages also clears the legacy build-tools
        mLS.clearLocalPkg(PkgType.PKG_ALL);

        // setup fake files
        mFOp.recordExistingFolder("/sdk/build-tools");
        mFOp.recordExistingFolder("/sdk/build-tools/17");
        mFOp.recordExistingFolder("/sdk/build-tools/18.1.2");
        mFOp.recordExistingFolder("/sdk/build-tools/12.2.3");
        mFOp.recordExistingFile("/sdk/build-tools/17/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=17\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");
        mFOp.recordExistingFile("/sdk/build-tools/18.1.2/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=18.1.2\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");
        mFOp.recordExistingFile("/sdk/build-tools/12.2.3/source.properties",
                "Pkg.License=Terms and Conditions\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=12.2.3\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n" +
                "Pkg.SourceUrl=https\\://example.com/repository-8.xml");

        // -- get latest build tool 18.1.2

        BuildToolInfo bt18a = mLS.getLatestBuildTool();
        assertNotNull(bt18a);
        assertEquals(new FullRevision(18, 1, 2), bt18a.getRevision());
        assertEquals(new File("/sdk/build-tools/18.1.2"), bt18a.getLocation());
        assertEquals("/sdk/build-tools/18.1.2/" + SdkConstants.FN_AAPT,
                     mFOp.getAgnosticAbsPath(bt18a.getPath(PathId.AAPT)));

        // -- get specific build tools by version

        BuildToolInfo bt18b = mLS.getBuildTool(new FullRevision(18, 1, 2));
        assertSame(bt18a, bt18b);

        BuildToolInfo bt17 = mLS.getBuildTool(new FullRevision(17));
        assertNotNull(bt17);
        assertEquals(new FullRevision(17), bt17.getRevision());
        assertEquals(new File("/sdk/build-tools/17"), bt17.getLocation());
        assertEquals("/sdk/build-tools/17/" + SdkConstants.FN_AAPT,
                     mFOp.getAgnosticAbsPath(bt17.getPath(PathId.AAPT)));

        assertNull(mLS.getBuildTool(new FullRevision(0)));
        assertNull(mLS.getBuildTool(new FullRevision(16, 17, 18)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_BUILD_TOOLS, new FullRevision(18, 1, 2));
        assertNotNull(pi);
        assertTrue(pi instanceof LocalBuildToolPkgInfo);
        assertSame(bt18a, ((LocalBuildToolPkgInfo)pi).getBuildToolInfo());
        assertEquals(new File("/sdk/build-tools/18.1.2"), pi.getLocalDir());
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new FullRevision(18, 1, 2), pi.getDesc().getFullRevision());
        assertEquals("Android SDK Build-Tools 18.1.2", pi.getListDescription());

        // -- get all build-tools and iterate, sorted by revision.

        assertEquals("[<LocalBuildToolPkgInfo <PkgDesc Type=build_tools FullRev=12.2.3>>, " +
                      "<LocalBuildToolPkgInfo <PkgDesc Type=build_tools FullRev=17.0.0>>, " +
                      "<LocalBuildToolPkgInfo <PkgDesc Type=build_tools FullRev=18.1.2>>]",
                     Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_BUILD_TOOLS)));
    }

    public final void testLocalSdkTest_getPkgInfo_Extra() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_EXTRA)));
        assertNull(mLS.getPkgInfo(PkgType.PKG_EXTRA, "vendor1", "path1"));
        assertNull(mLS.getExtra("vendor1", "path1"));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/extras");
        mFOp.recordExistingFolder("/sdk/extras/vendor1");
        mFOp.recordExistingFolder("/sdk/extras/vendor1/path1");
        mFOp.recordExistingFolder("/sdk/extras/vendor1/path2");
        mFOp.recordExistingFolder("/sdk/extras/vendor2");
        mFOp.recordExistingFolder("/sdk/extras/vendor2/path1");
        mFOp.recordExistingFolder("/sdk/extras/vendor2/path2");
        mFOp.recordExistingFolder("/sdk/extras/vendor3");
        mFOp.recordExistingFolder("/sdk/extras/vendor3/path3");
        mFOp.recordExistingFile("/sdk/extras/vendor1/path1/source.properties",
                "Extra.NameDisplay=Android Support Library\n" +
                "Extra.VendorDisplay=First Vendor\n" +
                "Extra.VendorId=vendor1\n" +
                "Extra.Path=path1\n" +
                "Extra.OldPaths=compatibility\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=11\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/extras/vendor1/path2/source.properties",
                "Extra.NameDisplay=Some Extra\n" +
                "Extra.VendorDisplay=First Vendor\n" +
                "Extra.VendorId=vendor1\n" +
                "Extra.Path=path2\n" +
                "Archive.Os=ANY\n" +
                "Pkg.Revision=21\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/extras/vendor2/path1/source.properties",
                "Extra.NameDisplay=Another Extra\n" +
                "Extra.VendorDisplay=Another Vendor\n" +
                "Extra.VendorId=vendor2\n" +
                "Extra.Path=path1\n" +
                "Extra.OldPaths=compatibility\n" +
                "Archive.Os=WINDOWS\n" +
                "Pkg.Revision=21\n" +
                "Archive.Arch=ANY\n");

        LocalPkgInfo pi1 = mLS.getPkgInfo(PkgType.PKG_EXTRA, "vendor1", "path1");
        assertNotNull(pi1);
        assertTrue(pi1 instanceof LocalExtraPkgInfo);
        assertEquals(
                "vendor1 [First Vendor]",
                ((LocalExtraPkgInfo)pi1).getDesc().getVendor().toString());
        assertEquals(
                "path1",
                ((LocalExtraPkgInfo)pi1).getDesc().getPath());
        assertEquals(new File("/sdk/extras/vendor1/path1"), pi1.getLocalDir());
        assertSame(mLS, pi1.getLocalSdk());
        assertEquals(null, pi1.getLoadError());
        assertEquals(new FullRevision(11), pi1.getDesc().getFullRevision());
        assertEquals("Android Support Library, rev 11", pi1.getListDescription());
        assertSame(pi1, mLS.getPkgInfo(pi1.getDesc()));

        LocalExtraPkgInfo pi2 = mLS.getExtra("vendor1", "path1");
        assertSame(pi1, pi2);

        // -- get all extras and iterate, sorted by revision.

        assertEquals("[<LocalExtraPkgInfo <PkgDesc Type=extra Vendor=vendor1 [First Vendor] Path=path1 FullRev=11.0.0>>, " +
                      "<LocalExtraPkgInfo <PkgDesc Type=extra Vendor=vendor1 [First Vendor] Path=path2 FullRev=21.0.0>>, " +
                      "<LocalExtraPkgInfo <PkgDesc Type=extra Vendor=vendor2 [Another Vendor] Path=path1 FullRev=21.0.0>>]",
                     Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_EXTRA)));
    }

    public final void testLocalSdkTest_getPkgInfo_Sources() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SOURCE)));
        assertNull(mLS.getPkgInfo(PkgType.PKG_SOURCE, new AndroidVersion(18, null)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/sources");
        mFOp.recordExistingFolder("/sdk/sources/android-CUPCAKE");
        mFOp.recordExistingFolder("/sdk/sources/android-18");
        mFOp.recordExistingFolder("/sdk/sources/android-42");
        mFOp.recordExistingFile("/sdk/sources/android-CUPCAKE/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=3\n" +
                "AndroidVersion.CodeName=CUPCAKE\n" +
                "Pkg.Revision=1\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/sources/android-18/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.Revision=2\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/sources/android-42/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.Revision=3\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");

        LocalPkgInfo pi18 = mLS.getPkgInfo(PkgType.PKG_SOURCE, new AndroidVersion(18, null));
        assertNotNull(pi18);
        assertTrue(pi18 instanceof LocalSourcePkgInfo);
        assertSame(mLS, pi18.getLocalSdk());
        assertEquals(null, pi18.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi18.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi18.getDesc().getMajorRevision());
        assertEquals("Sources for Android 18, rev 2", pi18.getListDescription());

        LocalPkgInfo pi1 = mLS.getPkgInfo(PkgType.PKG_SOURCE, new AndroidVersion(3, "CUPCAKE"));
        assertNotNull(pi1);
        assertEquals(new AndroidVersion(3, "CUPCAKE"), pi1.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(1), pi1.getDesc().getMajorRevision());
        assertEquals("Sources for Android CUPCAKE", pi1.getListDescription());
        assertSame(pi1, mLS.getPkgInfo(pi1.getDesc()));

        // -- get all extras and iterate, sorted by revision.

        assertEquals("[<LocalSourcePkgInfo <PkgDesc Type=source Android=API 3, CUPCAKE preview MajorRev=1>>, " +
                      "<LocalSourcePkgInfo <PkgDesc Type=source Android=API 18 MajorRev=2>>, " +
                      "<LocalSourcePkgInfo <PkgDesc Type=source Android=API 42 MajorRev=3>>]",
                     Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SOURCE)));
    }

    public final void testLocalSdkTest_getPkgInfo_Samples() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SAMPLE)));
        assertNull(mLS.getPkgInfo(PkgType.PKG_SAMPLE, new AndroidVersion(18, null)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/samples");
        mFOp.recordExistingFolder("/sdk/samples/android-18");
        mFOp.recordExistingFolder("/sdk/samples/android-42");
        mFOp.recordExistingFile("/sdk/samples/android-18/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.Revision=2\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/samples/android-42/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.Revision=3\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");

        LocalPkgInfo pi18 = mLS.getPkgInfo(PkgType.PKG_SAMPLE, new AndroidVersion(18, null));
        assertNotNull(pi18);
        assertTrue(pi18 instanceof LocalSamplePkgInfo);
        assertSame(mLS, pi18.getLocalSdk());
        assertEquals(null, pi18.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi18.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi18.getDesc().getMajorRevision());
        assertEquals("Samples for Android 18, rev 2", pi18.getListDescription());
        assertSame(pi18, mLS.getPkgInfo(pi18.getDesc()));

        // -- get all extras and iterate, sorted by revision.

        assertEquals(
                "[<LocalSamplePkgInfo <PkgDesc Type=sample Android=API 18 MajorRev=2 MinToolsRev=0.0.0>>, " +
                 "<LocalSamplePkgInfo <PkgDesc Type=sample Android=API 42 MajorRev=3 MinToolsRev=0.0.0>>]",
                 Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SAMPLE)));
    }

    public final void testLocalSdkTest_getPkgInfo_SysImages() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SYS_IMAGE)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/system-images");
        mFOp.recordExistingFolder("/sdk/system-images/android-18");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/armeabi-v7a");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/x86");
        mFOp.recordExistingFolder("/sdk/system-images/android-42");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/armeabi");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/x86");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/mips");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/somedir/armeabi-v7a");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/tag-1/x86");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/tag-2/mips");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/tag-2/mips/skins");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/tag-2/mips/skins/skinA");
        mFOp.recordExistingFolder("/sdk/system-images/android-42/tag-2/mips/skins/skinB");
        // without tags
        mFOp.recordExistingFile("/sdk/system-images/android-18/armeabi-v7a/source.properties",
                "Pkg.Revision=1\n" +
                "SystemImage.Abi=armeabi-v7a\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-18/x86/source.properties",
                "Pkg.Revision=2\n" +
                "SystemImage.Abi=x86\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/x86/source.properties",
                "Pkg.Revision=3\n" +
                "SystemImage.Abi=x86\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/mips/source.properties",
                "Pkg.Revision=4\n" +
                "SystemImage.Abi=mips\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/armeabi-v7a/source.properties",
                "Pkg.Revision=5\n" +
                "SystemImage.Abi=armeabi-v7a\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        // with tags
        mFOp.recordExistingFile("/sdk/system-images/android-42/somedir/armeabi-v7a/source.properties",
                "Pkg.Revision=6\n" +
                "SystemImage.TagId=default\n" +  // Prop TagId is used instead of the "somedir" name
                "SystemImage.Abi=armeabi-v7a\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/tag-1/x86/source.properties",
                "Pkg.Revision=7\n" +
                "SystemImage.TagId=tag-1\n" +
                "SystemImage.TagDisplay=My Tag 1\n" +
                "SystemImage.Abi=x86\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/tag-2/mips/source.properties",
                "Pkg.Revision=8\n" +
                "SystemImage.TagId=tag-2\n" +
                "SystemImage.TagDisplay=My Tag 2\n" +
                "SystemImage.Abi=mips\n" +
                "AndroidVersion.ApiLevel=42\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/tag-2/mips/skins/skinA/layout",
                "part {\n" +
                "}\n");
        mFOp.recordExistingFile("/sdk/system-images/android-42/tag-2/mips/skins/skinB/layout",
                "part {\n" +
                "}\n");

        assertEquals("[<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 18 Tag=default [Default] Path=armeabi-v7a MajorRev=1>>, " +
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 18 Tag=default [Default] Path=x86 MajorRev=2>>, " +
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 42 Tag=default [Default] Path=armeabi-v7a MajorRev=6>>, " +
                      // Tag=default Path=armeabi-v7a MajorRev=5 is overriden by the MajorRev=6 above
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 42 Tag=default [Default] Path=mips MajorRev=4>>, " +
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 42 Tag=default [Default] Path=x86 MajorRev=3>>, " +
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 42 Tag=tag-1 [My Tag 1] Path=x86 MajorRev=7>>, " +
                      "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 42 Tag=tag-2 [My Tag 2] Path=mips MajorRev=8>>]",
                     Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SYS_IMAGE)));

        LocalPkgInfo pi = mLS.getPkgsInfos(PkgType.PKG_SYS_IMAGE)[0];
        assertNotNull(pi);
        assertTrue(pi instanceof LocalSysImgPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new MajorRevision(1), pi.getDesc().getMajorRevision());
        assertEquals("armeabi-v7a", pi.getDesc().getPath());
        assertEquals("armeabi-v7a System Image, Android 18", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));
    }

    public final void testLocalSdkTest_getPkgInfo_Platforms() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_PLATFORM)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);

        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_PLATFORM)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_PLATFORM, new AndroidVersion(18, null));
        assertNotNull(pi);
        assertTrue(pi instanceof LocalPlatformPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(1), pi.getDesc().getMajorRevision());
        assertEquals("Android SDK Platform 18", pi.getListDescription());

        IAndroidTarget t1 = ((LocalPlatformPkgInfo)pi).getAndroidTarget();
        assertNotNull(t1);

        LocalPkgInfo pi2 = mLS.getPkgInfo(PkgType.PKG_PLATFORM, "android-18");
        assertSame(pi, pi2);

        IAndroidTarget t2 = mLS.getTargetFromHashString("android-18");
        assertSame(t1, t2);
    }

    public final void testLocalSdkTest_getPkgInfo_Platforms_SysImages_Skins() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_SYS_IMAGE)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);

        mFOp.recordExistingFolder("/sdk/system-images");
        mFOp.recordExistingFolder("/sdk/system-images/android-18");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/tag-1/x86");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/tag-2/mips");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/tag-2/mips/skins");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/tag-2/mips/skins/skinA");
        mFOp.recordExistingFolder("/sdk/system-images/android-18/tag-2/mips/skins/skinB");
        mFOp.recordExistingFile("/sdk/system-images/android-18/tag-1/x86/source.properties",
                "Pkg.Revision=7\n" +
                "SystemImage.TagId=tag-1\n" +
                "SystemImage.TagDisplay=My Tag 1\n" +
                "SystemImage.Abi=x86\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-18/tag-2/mips/source.properties",
                "Pkg.Revision=8\n" +
                "SystemImage.TagId=tag-2\n" +
                "SystemImage.TagDisplay=My Tag 2\n" +
                "SystemImage.Abi=mips\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/android-18/tag-2/mips/skins/skinA/layout",
                "part {\n" +
                "}\n");
        mFOp.recordExistingFile("/sdk/system-images/android-18/tag-2/mips/skins/skinB/layout",
                "part {\n" +
                "}\n");

        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                   "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 18 Tag=tag-1 [My Tag 1] Path=x86 MajorRev=7>>, " +
                   "<LocalSysImgPkgInfo <PkgDesc Type=sys_image Android=API 18 Tag=tag-2 [My Tag 2] Path=mips MajorRev=8>>]",
                 Arrays.toString(
                      mLS.getPkgsInfos(EnumSet.of(PkgType.PKG_PLATFORM, PkgType.PKG_SYS_IMAGE))));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_PLATFORM, new AndroidVersion(18, null));
        assertNotNull(pi);
        assertTrue(pi instanceof LocalPlatformPkgInfo);

        IAndroidTarget t = ((LocalPlatformPkgInfo)pi).getAndroidTarget();
        assertNotNull(t);

        assertEquals(
                "[SystemImage tag=tag-1, ABI=x86, location in system image='/sdk/system-images/android-18/tag-1/x86', " +
                 "SystemImage tag=tag-2, ABI=mips, location in system image='/sdk/system-images/android-18/tag-2/mips']",
                 sanitizePath(Arrays.toString(t.getSystemImages())));

        assertEquals("/sdk/platforms/android-18/skins/WVGA800",
                sanitizePath(t.getDefaultSkin().toString()));

        assertEquals(
                "[/sdk/system-images/android-18/tag-2/mips/skins/skinA, " +
                 "/sdk/system-images/android-18/tag-2/mips/skins/skinB]",
                sanitizePath(Arrays.toString(t.getSkins())));

        // check the skins paths from the system image also match what's in the platform
        assertEquals(
                "[/sdk/system-images/android-18/tag-2/mips/skins/skinA, " +
                 "/sdk/system-images/android-18/tag-2/mips/skins/skinB]",
                sanitizePath(Arrays.toString(t.getSystemImages()[1].getSkins())));

        assertEquals("Android SDK Platform 18", pi.getListDescription());
    }

    private String sanitizePath(String path) {
        // On Windows the "/sdk" paths get transformed into an absolute "C:\\sdk"
        // so we sanitize them back to "/sdk". On Linux/Mac, this is mostly a no-op.
        String sdk = mLS.getLocation().getAbsolutePath();
        path = path.replaceAll(Pattern.quote(sdk), "/sdk");
        path = path.replace(File.separatorChar, '/');
        return path;
    }

    public final void testLocalSdkTest_getPkgInfo_Platforms_Sources() {
        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);
        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>]",
                Arrays.toString(
                    mLS.getPkgsInfos(EnumSet.of(PkgType.PKG_PLATFORM, PkgType.PKG_SOURCE))));

        // By default, IAndroidTarget returns the legacy path to a platform source,
        // whether that directory exist or not.
        LocalPkgInfo pi1 = mLS.getPkgInfo(PkgType.PKG_PLATFORM, new AndroidVersion(18, null));
        IAndroidTarget t1 = ((LocalPlatformPkgInfo)pi1).getAndroidTarget();
        assertEquals("/sdk/platforms/android-18/sources",
                     mFOp.getAgnosticAbsPath(t1.getPath(IAndroidTarget.SOURCES)));
        assertEquals("Android SDK Platform 18", pi1.getListDescription());
        assertSame(pi1, mLS.getPkgInfo(pi1.getDesc()));

        // However if a separate sources package folder is installed, it is returned instead.
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        mFOp.recordExistingFolder("/sdk/sources");
        mFOp.recordExistingFolder("/sdk/sources/android-18");
        mFOp.recordExistingFile("/sdk/sources/android-18/source.properties",
                "Archive.Os=ANY\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.Revision=2\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Arch=ANY\n");

        LocalPkgInfo pi2 = mLS.getPkgInfo(PkgType.PKG_PLATFORM, new AndroidVersion(18, null));
        IAndroidTarget t2 = ((LocalPlatformPkgInfo)pi2).getAndroidTarget();
        assertEquals("[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                      "<LocalSourcePkgInfo <PkgDesc Type=source Android=API 18 MajorRev=2>>]",
                 Arrays.toString(mLS.getPkgsInfos(
                         EnumSet.of(PkgType.PKG_PLATFORM, PkgType.PKG_SOURCE))));
        assertEquals("Android SDK Platform 18", pi2.getListDescription());
        assertSame(pi2, mLS.getPkgInfo(pi2.getDesc()));

        assertEquals("/sdk/sources/android-18",
                mFOp.getAgnosticAbsPath(t2.getPath(IAndroidTarget.SOURCES)));
    }

    public final void testLocalSdkTest_getPkgInfo_Addon_NoSysImg() {
        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);
        mFOp.recordExistingFolder("/sdk/add-ons");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/source.properties",
                "Pkg.Revision=2\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "Addon.NameId=name\n" +
                "Addon.NameDisplay=Some Name\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/manifest.ini",
                "revision=2\n" +
                "name=Some Name\n" +
                "name-id=name\n" +
                "vendor=Some Vendor\n" +
                "vendor-id=vendor\n" +
                "api=18\n" +
                "libraries=com.foo.lib1;com.blah.lib2\n" +
                "com.foo.lib1=foo.jar;API for Foo\n" +
                "com.blah.lib2=blah.jar;API for Blah\n");

        assertEquals(
                "[<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));
        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                 "<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                 Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ALL)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_ADDON, "Some Vendor:Some Name:18");
        assertNotNull(pi);
        assertTrue(pi instanceof LocalAddonPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi.getDesc().getMajorRevision());
        assertEquals("Some Vendor:Some Name:18", pi.getDesc().getPath());
        assertEquals("Some Name, Android 18, rev 2", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));

        IAndroidTarget t = mLS.getTargetFromHashString("Some Vendor:Some Name:18");
        assertSame(t, ((LocalAddonPkgInfo) pi).getAndroidTarget());
        assertNotNull(t);

        assertEquals(
                "[]",
                 sanitizePath(Arrays.toString(t.getSystemImages())));
    }

    public final void testLocalSdkTest_getPkgInfo_Addon_SysImgInLegacyFolder() {
        // "Legacy sys-img" means there's only one sys-img of armeabi type directly
        // in the folder addons/addon-name/images. This case is only supported for
        // backward compatibility and we default to it when there's an images/ folder
        // in the addon and that folder doesn't contain per-ABI subfolders and instead
        // contains at least one .img file.


        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);
        mFOp.recordExistingFolder("/sdk/add-ons");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/images");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins/skin_one");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins/skin_two");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/source.properties",
                "Pkg.Revision=2\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "Addon.NameId=name\n" +
                "Addon.NameDisplay=Some Name\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/manifest.ini",
                "revision=2\n" +
                "name=Some Name\n" +
                "name-id=name\n" +
                "vendor=Some Vendor\n" +
                "vendor-id=vendor\n" +
                "api=18\n" +
                "libraries=com.foo.lib1;com.blah.lib2\n" +
                "com.foo.lib1=foo.jar;API for Foo\n" +
                "com.blah.lib2=blah.jar;API for Blah\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/images/system.img",
                "placeholder\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/skins/skin_one/layout",
                "parts {\n" +
                "}\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/skins/skin_two/layout",
                "parts {\n" +
                "}\n");

        assertEquals(
                "[<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));
        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                 "<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                 Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ALL)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_ADDON, "Some Vendor:Some Name:18");
        assertNotNull(pi);
        assertTrue(pi instanceof LocalAddonPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi.getDesc().getMajorRevision());
        assertEquals("Some Vendor:Some Name:18", pi.getDesc().getPath());
        assertEquals("Some Name, Android 18, rev 2", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));

        IAndroidTarget t = mLS.getTargetFromHashString("Some Vendor:Some Name:18");
        assertSame(t, ((LocalAddonPkgInfo) pi).getAndroidTarget());
        assertNotNull(t);

        assertEquals(
                "[SystemImage tag=default, ABI=armeabi, location in legacy folder='/sdk/add-ons/addon-vendor_name-2/images']",
                 sanitizePath(Arrays.toString(t.getSystemImages())));

        assertEquals(
                "[/sdk/add-ons/addon-vendor_name-2/skins/skin_one, " +
                 "/sdk/add-ons/addon-vendor_name-2/skins/skin_two]",
                sanitizePath(Arrays.toString(t.getSkins())));
    }

    public final void testLocalSdkTest_getPkgInfo_Addon_SysImgInSubfolder() {
        // "sys-img in subfolder" means there is an addons/addon-name/images/ folder
        // which in turns contains any number of folders named after the system-image ABI.

        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);
        mFOp.recordExistingFolder("/sdk/add-ons");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/images");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/images/armeabi-v7a");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/images/x86");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins/skin_one");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2/skins/skin_two");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/source.properties",
                "Pkg.Revision=2\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "Addon.NameId=name\n" +
                "Addon.NameDisplay=Some Name\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/manifest.ini",
                "revision=2\n" +
                "name=Some Name\n" +
                "name-id=name\n" +
                "vendor=Some Vendor\n" +
                "vendor-id=vendor\n" +
                "api=18\n" +
                "libraries=com.foo.lib1;com.blah.lib2\n" +
                "com.foo.lib1=foo.jar;API for Foo\n" +
                "com.blah.lib2=blah.jar;API for Blah\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/images/armeabi-v7a/build.prop",
                "ro.build.id=a18\n" +
                "ro.build.display.id=addon_armeabi-v7a-18\n" +
                "ro.build.version.sdk=18\n" +
                "ro.build.version.codename=REL\n" +
                "ro.product.brand=generic_armeabi-v7a\n" +
                "ro.product.name=google_sdk_armeabi-v7a\n" +
                "ro.product.device=generic_armeabi-v7a\n" +
                "ro.product.board=\n" +
                "ro.product.cpu.abi=armeabi-v7a\n" +
                "ro.product.manufacturer=unknown\n" +
                "ro.product.locale.language=en\n" +
                "ro.product.locale.region=US\n" +
                "ro.build.product=generic_armeabi-v7a\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/images/x86/build.prop",
                "ro.build.id=a18\n" +
                "ro.build.display.id=addon_x86-18\n" +
                "ro.build.version.sdk=18\n" +
                "ro.build.version.codename=REL\n" +
                "ro.product.brand=generic_x86\n" +
                "ro.product.name=google_sdk_x86\n" +
                "ro.product.device=generic_x86\n" +
                "ro.product.board=\n" +
                "ro.product.cpu.abi=x86\n" +
                "ro.product.manufacturer=unknown\n" +
                "ro.product.locale.language=en\n" +
                "ro.product.locale.region=US\n" +
                "ro.build.product=generic_x86\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/skins/skin_one/layout",
                "parts {\n" +
                "}\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/skins/skin_two/layout",
                "parts {\n" +
                "}\n");

        assertEquals(
                "[<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));
        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                 "<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                 Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ALL)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_ADDON, "Some Vendor:Some Name:18");
        assertNotNull(pi);
        assertTrue(pi instanceof LocalAddonPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi.getDesc().getMajorRevision());
        assertEquals("Some Vendor:Some Name:18", pi.getDesc().getPath());
        assertEquals("Some Name, Android 18, rev 2", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));

        IAndroidTarget t = mLS.getTargetFromHashString("Some Vendor:Some Name:18");
        assertSame(t, ((LocalAddonPkgInfo) pi).getAndroidTarget());
        assertNotNull(t);

        assertEquals(
                "[SystemImage addon-vendor=vendor, tag=default, ABI=armeabi-v7a, " +
                "location in images subfolder='/sdk/add-ons/addon-vendor_name-2/images/armeabi-v7a', " +
                "SystemImage addon-vendor=vendor, tag=default, ABI=x86, " +
                "location in images subfolder='/sdk/add-ons/addon-vendor_name-2/images/x86']",
                 sanitizePath(Arrays.toString(t.getSystemImages())));

        assertEquals(
                "[/sdk/add-ons/addon-vendor_name-2/skins/skin_one, " +
                 "/sdk/add-ons/addon-vendor_name-2/skins/skin_two]",
                sanitizePath(Arrays.toString(t.getSkins())));
    }

    public final void testLocalSdkTest_getPkgInfo_Addon_SysImgFolder() {
        // sys-img stored separately in the SDK/system-images/addon-id-name/abi/ folder.

        // check empty
        assertEquals("[]", Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));

        // setup fake files
        mLS.clearLocalPkg(PkgType.PKG_ALL);
        recordPlatform18(mFOp);
        mFOp.recordExistingFolder("/sdk/add-ons");
        mFOp.recordExistingFolder("/sdk/add-ons/addon-vendor_name-2");
        mFOp.recordExistingFolder("/sdk/system-images");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/armeabi-v7a");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/x86");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/armeabi-v7a/skins");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/armeabi-v7a/skins/skin_one");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/x86/skins");
        mFOp.recordExistingFolder("/sdk/system-images/addon-vendor_name-2/x86/skins/skin_two");
        mFOp.recordExistingFile  ("/sdk/add-ons/addon-vendor_name-2/source.properties",
                "Pkg.Revision=2\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "Addon.NameId=name\n" +
                "Addon.NameDisplay=Some Name\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/add-ons/addon-vendor_name-2/manifest.ini",
                "revision=2\n" +
                "name=Some Name\n" +
                "name-id=name\n" +
                "vendor=Some Vendor\n" +
                "vendor-id=vendor\n" +
                "api=18\n" +
                "libraries=com.foo.lib1;com.blah.lib2\n" +
                "com.foo.lib1=foo.jar;API for Foo\n" +
                "com.blah.lib2=blah.jar;API for Blah\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/armeabi-v7a/source.properties",
                "Pkg.Revision=1\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "SystemImage.TagId=name\n" +
                "SystemImage.TagDisplay=Some Name\n" +
                "SystemImage.Abi=armeabi-v7a\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/armeabi-v7a/build.prop",
                "ro.build.id=a18\n" +
                "ro.build.display.id=addon_armeabi-v7a-18\n" +
                "ro.build.version.sdk=18\n" +
                "ro.build.version.codename=REL\n" +
                "ro.product.brand=generic_armeabi-v7a\n" +
                "ro.product.name=google_sdk_armeabi-v7a\n" +
                "ro.product.device=generic_armeabi-v7a\n" +
                "ro.product.board=\n" +
                "ro.product.cpu.abi=armeabi-v7a\n" +
                "ro.product.manufacturer=unknown\n" +
                "ro.product.locale.language=en\n" +
                "ro.product.locale.region=US\n" +
                "ro.build.product=generic_armeabi-v7a\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/x86/source.properties",
                "Pkg.Revision=1\n" +
                "Addon.VendorId=vendor\n" +
                "Addon.VendorDisplay=Some Vendor\n" +
                "SystemImage.TagId=name\n" +
                "SystemImage.TagDisplay=Some Name\n" +
                "SystemImage.Abi=x86\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/x86/build.prop",
                "ro.build.id=a18\n" +
                "ro.build.display.id=addon_x86-18\n" +
                "ro.build.version.sdk=18\n" +
                "ro.build.version.codename=REL\n" +
                "ro.product.brand=generic_x86\n" +
                "ro.product.name=google_sdk_x86\n" +
                "ro.product.device=generic_x86\n" +
                "ro.product.board=\n" +
                "ro.product.cpu.abi=x86\n" +
                "ro.product.manufacturer=unknown\n" +
                "ro.product.locale.language=en\n" +
                "ro.product.locale.region=US\n" +
                "ro.build.product=generic_x86\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/armeabi-v7a/skins/skin_one/layout",
                "parts {\n" +
                "}\n");
        mFOp.recordExistingFile("/sdk/system-images/addon-vendor_name-2/x86/skins/skin_two/layout",
                "parts {\n" +
                "}\n");

        assertEquals(
                "[<LocalAddonSysImgPkgInfo <PkgDesc Type=addon_sys_image Android=API 18 Vendor=vendor [Some Vendor] Tag=name [Some Name] Path=armeabi-v7a MajorRev=1>>, " +
                 "<LocalAddonSysImgPkgInfo <PkgDesc Type=addon_sys_image Android=API 18 Vendor=vendor [Some Vendor] Tag=name [Some Name] Path=x86 MajorRev=1>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON_SYS_IMAGE)));
        assertEquals(
                "[<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>]",
                Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ADDON)));
        assertEquals(
                "[<LocalPlatformPkgInfo <PkgDesc Type=platform Android=API 18 Path=android-18 MajorRev=1 MinToolsRev=21.0.0>>, " +
                 "<LocalAddonPkgInfo <PkgDesc Type=addon Android=API 18 Vendor=vendor [Some Vendor] Path=Some Vendor:Some Name:18 MajorRev=2>>, " +
                 "<LocalAddonSysImgPkgInfo <PkgDesc Type=addon_sys_image Android=API 18 Vendor=vendor [Some Vendor] Tag=name [Some Name] Path=armeabi-v7a MajorRev=1>>, " +
                 "<LocalAddonSysImgPkgInfo <PkgDesc Type=addon_sys_image Android=API 18 Vendor=vendor [Some Vendor] Tag=name [Some Name] Path=x86 MajorRev=1>>]",
                 Arrays.toString(mLS.getPkgsInfos(PkgType.PKG_ALL)));

        LocalPkgInfo pi = mLS.getPkgInfo(PkgType.PKG_ADDON, "Some Vendor:Some Name:18");
        assertNotNull(pi);
        assertTrue(pi instanceof LocalAddonPkgInfo);
        assertSame(mLS, pi.getLocalSdk());
        assertEquals(null, pi.getLoadError());
        assertEquals(new AndroidVersion(18, null), pi.getDesc().getAndroidVersion());
        assertEquals(new MajorRevision(2), pi.getDesc().getMajorRevision());
        assertEquals("Some Vendor:Some Name:18", pi.getDesc().getPath());
        assertEquals("Some Name, Android 18, rev 2", pi.getListDescription());
        assertSame(pi, mLS.getPkgInfo(pi.getDesc()));

        IAndroidTarget t = mLS.getTargetFromHashString("Some Vendor:Some Name:18");
        assertSame(t, ((LocalAddonPkgInfo) pi).getAndroidTarget());
        assertNotNull(t);

        assertEquals(
                "[SystemImage addon-vendor=vendor, tag=name, ABI=armeabi-v7a, location in system image='/sdk/system-images/addon-vendor_name-2/armeabi-v7a', " +
                 "SystemImage addon-vendor=vendor, tag=name, ABI=x86, location in system image='/sdk/system-images/addon-vendor_name-2/x86']",
                 sanitizePath(Arrays.toString(t.getSystemImages())));

        assertEquals(
                "[/sdk/system-images/addon-vendor_name-2/armeabi-v7a/skins/skin_one, " +
                 "/sdk/system-images/addon-vendor_name-2/x86/skins/skin_two]",
                sanitizePath(Arrays.toString(t.getSkins())));

    }

    //-----

    private void recordPlatform18(MockFileOp fop) {
        fop.recordExistingFolder("/sdk/platforms");
        fop.recordExistingFolder("/sdk/platforms/android-18");
        fop.recordExistingFile  ("/sdk/platforms/android-18/android.jar");
        fop.recordExistingFile  ("/sdk/platforms/android-18/framework.aidl");
        fop.recordExistingFile  ("/sdk/platforms/android-18/source.properties",
                "Pkg.Revision=1\n" +
                "Platform.Version=4.3\n" +
                "AndroidVersion.ApiLevel=18\n" +
                "Layoutlib.Api=10\n" +
                "Layoutlib.Revision=1\n" +
                "Platform.MinToolsRev=21\n" +
                "Pkg.LicenseRef=android-sdk-license\n" +
                "Archive.Os=ANY\n" +
                "Archive.Arch=ANY\n");
        fop.recordExistingFile("/sdk/platforms/android-18/sdk.properties",
                "sdk.ant.templates.revision=1\n" +
                "sdk.skin.default=WVGA800\n");
        fop.recordExistingFile("/sdk/platforms/android-18/build.prop",
                "ro.build.id=JB_MR2\n" +
                "ro.build.display.id=sdk-eng 4.3 JB_MR2 819563 test-keys\n" +
                "ro.build.version.incremental=819563\n" +
                "ro.build.version.sdk=18\n" +
                "ro.build.version.codename=REL\n" +
                "ro.build.version.release=4.3\n" +
                "ro.build.date=Tue Sep 10 18:43:31 UTC 2013\n" +
                "ro.build.date.utc=1378838611\n" +
                "ro.build.type=eng\n" +
                "ro.build.tags=test-keys\n" +
                "ro.product.model=sdk\n" +
                "ro.product.name=sdk\n" +
                "ro.product.board=\n" +
                "ro.product.cpu.abi=armeabi-v7a\n" +
                "ro.product.cpu.abi2=armeabi\n" +
                "ro.product.locale.language=en\n" +
                "ro.product.locale.region=US\n" +
                "ro.wifi.channels=\n" +
                "ro.board.platform=\n" +
                "# ro.build.product is obsolete; use ro.product.device\n" +
                "# Do not try to parse ro.build.description or .fingerprint\n" +
                "ro.build.description=sdk-eng 4.3 JB_MR2 819563 test-keys\n" +
                "ro.build.fingerprint=generic/sdk/generic:4.3/JB_MR2/819563:eng/test-keys\n" +
                "ro.build.characteristics=default\n" +
                "rild.libpath=/system/lib/libreference-ril.so\n" +
                "rild.libargs=-d /dev/ttyS0\n" +
                "ro.config.notification_sound=OnTheHunt.ogg\n" +
                "ro.config.alarm_alert=Alarm_Classic.ogg\n" +
                "ro.kernel.android.checkjni=1\n" +
                "xmpp.auto-presence=true\n" +
                "ro.config.nocheckin=yes\n" +
                "net.bt.name=Android\n" +
                "dalvik.vm.stack-trace-file=/data/anr/traces.txt\n" +
                "ro.build.user=generic\n" +
                "ro.build.host=generic\n" +
                "ro.product.brand=generic\n" +
                "ro.product.manufacturer=generic\n" +
                "ro.product.device=generic\n" +
                "ro.build.product=generic\n");
    }
}
