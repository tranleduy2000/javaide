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

package com.android.sdklib.repository.descriptors;

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.NoPreviewRevision;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

public class PkgDescTest extends TestCase {

    public final File mRoot = new File("/sdk");

    public final void testPkgDescTool_NotPreview() {
        IPkgDesc p = PkgDesc.Builder.newTool(
                new FullRevision(1, 2, 3),
                new FullRevision(5, 6, 7, 8)).create();

        assertEquals(PkgType.PKG_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3), p.getFullRevision());
        assertFalse (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertTrue  (p.hasMinPlatformToolsRev());
        assertEquals(new FullRevision(5, 6, 7, 8), p.getMinPlatformToolsRev());

        assertEquals("tools", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "tools"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=tools FullRev=1.2.3 MinPlatToolsRev=5.6.7 rc8>", p.toString());
        assertEquals("Android SDK Tools 1.2.3", p.getListDescription());
    }

    public final void testPkgDescTool_Preview() {
        IPkgDesc p = PkgDesc.Builder.newTool(
                new FullRevision(1, 2, 3, 4),
                new FullRevision(5, 6, 7, 8)).create();

        assertEquals(PkgType.PKG_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3, 4), p.getFullRevision());
        assertTrue  (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertTrue  (p.hasMinPlatformToolsRev());
        assertEquals(new FullRevision(5, 6, 7, 8), p.getMinPlatformToolsRev());

        assertEquals("tools-preview", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "tools"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=tools FullRev=1.2.3 rc4 MinPlatToolsRev=5.6.7 rc8>", p.toString());
        assertEquals("Android SDK Tools 1.2.3 rc4", p.getListDescription());
    }

    public final void testPkgDescTool_Update() {
        final FullRevision min5670 = new FullRevision(5, 6, 7, 0);
        final IPkgDesc f123  =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 0), min5670).create();
        final IPkgDesc f123b =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 0), min5670).create();

        // can't update itself
        assertFalse(f123 .isUpdateFor(f123b));
        assertFalse(f123b.isUpdateFor(f123));
        assertTrue (f123 .compareTo(f123b) == 0);
        assertTrue (f123b.compareTo(f123 ) == 0);

        // min-platform-tools-rev isn't used for updates checks
        final FullRevision min5680 = new FullRevision(5, 6, 8, 0);
        final IPkgDesc f123c =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 0), min5680).create();
        assertFalse(f123c.isUpdateFor(f123));
        // but it's used for comparisons
        assertTrue (f123c.compareTo(f123) > 0);

        // full revision is used for updated checks
        final IPkgDesc f124 =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 4, 0), min5670).create();
        assertTrue (f124.isUpdateFor(f123));
        assertFalse(f123.isUpdateFor(f124));
        assertTrue (f124.compareTo(f123) > 0);

        final IPkgDesc f122 =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 2, 0), min5670).create();
        assertTrue (f123.isUpdateFor(f122));
        assertFalse(f122.isUpdateFor(f123));
        assertTrue (f122.compareTo(f123) < 0);

        // previews are not updated by final packages
        final FullRevision min5671 = new FullRevision(5, 6, 7, 1);
        final IPkgDesc p1231 =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 1), min5671).create();
        assertFalse(p1231.isUpdateFor(f122));
        assertFalse(f122 .isUpdateFor(p1231));
        assertFalse(p1231.isUpdateFor(f122, FullRevision.PreviewComparison.COMPARE_NUMBER));
        assertFalse(p1231.isUpdateFor(f122, FullRevision.PreviewComparison.COMPARE_TYPE));
        // ...unless we ignore them explicitly
        assertTrue(p1231.isUpdateFor(f122, FullRevision.PreviewComparison.IGNORE));

        // but previews are used for comparisons
        assertTrue (p1231.compareTo(f122 ) > 0);
        assertTrue (f123 .compareTo(p1231) > 0);

        final IPkgDesc p1232 =
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 2), min5671).create();
        assertTrue (p1232.isUpdateFor(p1231));
        assertFalse(p1231.isUpdateFor(p1232));
        assertTrue (p1232.compareTo(p1231) > 0);
    }

    //----

    public final void testPkgDescPlatformTool_NotPreview() {
        IPkgDesc p = PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3)).create();

        assertEquals(PkgType.PKG_PLATFORM_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3), p.getFullRevision());
        assertFalse (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("platform-tools", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "platform-tools"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=platform_tools FullRev=1.2.3>", p.toString());
        assertEquals("Android SDK Platform-Tools 1.2.3", p.getListDescription());
    }

    public final void testPkgDescPlatformTool_Preview() {
        IPkgDesc p = PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3, 4)).create();

        assertEquals(PkgType.PKG_PLATFORM_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3, 4), p.getFullRevision());
        assertTrue  (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("platform-tools-preview", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "platform-tools"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=platform_tools FullRev=1.2.3 rc4>", p.toString());
        assertEquals("Android SDK Platform-Tools 1.2.3 rc4", p.getListDescription());
    }

    public final void testPkgDescPlatformTool_Update() {
        final IPkgDesc f123  =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3, 0)).create();
        final IPkgDesc f123b =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3, 0)).create();

        // can't update itself
        assertFalse(f123 .isUpdateFor(f123b));
        assertFalse(f123b.isUpdateFor(f123));
        assertTrue (f123 .compareTo(f123b) == 0);
        assertTrue (f123b.compareTo(f123 ) == 0);

        // full revision is used for updated checks
        final IPkgDesc f124 =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 4, 0)).create();
        assertTrue (f124.isUpdateFor(f123));
        assertFalse(f123.isUpdateFor(f124));
        assertTrue (f124.compareTo(f123) > 0);

        final IPkgDesc f122 =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 2, 0)).create();
        assertTrue (f123.isUpdateFor(f122));
        assertFalse(f122.isUpdateFor(f123));
        assertTrue (f122.compareTo(f123) < 0);

        // previews are not updated by final packages
        final IPkgDesc p1231 =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3, 1)).create();
        assertFalse(p1231.isUpdateFor(f122));
        assertFalse(f122 .isUpdateFor(p1231));
        // but previews are used for comparisons
        assertTrue (p1231.compareTo(f122 ) > 0);
        assertTrue (f123 .compareTo(p1231) > 0);

        final IPkgDesc p1232 =
                PkgDesc.Builder.newPlatformTool(new FullRevision(1, 2, 3, 2)).create();
        assertTrue (p1232.isUpdateFor(p1231));
        assertFalse(p1231.isUpdateFor(p1232));
        assertTrue (p1232.compareTo(p1231) > 0);
    }

    //----

    public final void testPkgDescDoc() throws Exception {
        IPkgDesc p =
                PkgDesc.Builder.newDoc(new AndroidVersion("19"), new MajorRevision(1)).create();

        assertEquals(PkgType.PKG_DOC, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull(p.getFullRevision());

        assertTrue(p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue(p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull(p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull(p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull(p.getMinPlatformToolsRev());

        assertEquals("doc", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "docs"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=doc Android=API 19 MajorRev=1>", p.toString());
        assertEquals("Documentation for Android SDK", p.getListDescription());
    }

    public final void testPkgDescDoc_Update() throws Exception {
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        final IPkgDesc p19_1  = PkgDesc.Builder.newDoc(api19, rev1).create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newDoc(api19, rev1).create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        final IPkgDesc p19_2  = PkgDesc.Builder.newDoc(api19, new MajorRevision(2)).create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        final IPkgDesc p18_1  = PkgDesc.Builder.newDoc(new AndroidVersion("18"), rev1).create();
        assertTrue (p19_1.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_1));
        assertTrue (p19_1.compareTo(p18_1) > 0);
    }

    //----

    public final void testPkgDescBuildTool_NotPreview() {
        IPkgDesc p = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3)).create();

        assertEquals(PkgType.PKG_BUILD_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3), p.getFullRevision());
        assertFalse (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("build-tools-1.2.3", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "build-tools", "build-tools-1.2.3"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=build_tools FullRev=1.2.3>", p.toString());
        assertEquals("Android SDK Build-Tools 1.2.3", p.getListDescription());
    }

    public final void testPkgDescBuildTool_Preview() {
        IPkgDesc p = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3, 4)).create();

        assertEquals(PkgType.PKG_BUILD_TOOLS, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3, 4), p.getFullRevision());
        assertTrue  (p.getFullRevision().isPreview());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("build-tools-1.2.3-preview", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "build-tools", "build-tools-1.2.3-preview"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=build_tools FullRev=1.2.3 rc4>", p.toString());
        assertEquals("Android SDK Build-Tools 1.2.3 rc4", p.getListDescription());
    }

    public final void testPkgDescBuildTool_Update() {
        final IPkgDesc f123  = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3, 0)).create();
        final IPkgDesc f123b = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3, 0)).create();

        // can't update itself
        assertFalse(f123 .isUpdateFor(f123b));
        assertFalse(f123b.isUpdateFor(f123));
        assertTrue (f123 .compareTo(f123b) == 0);
        assertTrue (f123b.compareTo(f123 ) == 0);

        // build-tools is different as full revisions are installed side by side
        // so they don't update each other (except for the preview bit, see below.)
        final IPkgDesc f124 = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 4, 0)).create();
        assertFalse(f124.isUpdateFor(f123));
        assertFalse(f123.isUpdateFor(f124));
        // comparison is still done on the full revision.
        assertTrue (f124.compareTo(f123) > 0);

        final IPkgDesc f122 = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 2, 0)).create();
        assertFalse(f123.isUpdateFor(f122));
        assertFalse(f122.isUpdateFor(f123));
        assertTrue (f122.compareTo(f123) < 0);

        // previews are not updated by final packages
        final IPkgDesc p1231 = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3, 1)).create();
        assertFalse(p1231.isUpdateFor(f122));
        assertFalse(f122 .isUpdateFor(p1231));
        // but previews are used for comparisons
        assertTrue (p1231.compareTo(f122 ) > 0);
        assertTrue (f123 .compareTo(p1231) > 0);

        // previews do update other packages that have the same major.minor.micro.
        final IPkgDesc p1232 = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 3, 2)).create()
                ;
        assertTrue (p1232.isUpdateFor(p1231));
        assertFalse(p1231.isUpdateFor(p1232));
        assertTrue (p1232.compareTo(p1231) > 0);

        final IPkgDesc p1222 = PkgDesc.Builder.newBuildTool(new FullRevision(1, 2, 2, 2)).create();
        assertFalse(p1232.isUpdateFor(p1222));
    }

    //----

    public final void testPkgDescExtra() {
        IdDisplay vendor = new IdDisplay("vendor", "The Vendor");
        IPkgDesc p = PkgDesc.Builder
                .newExtra(vendor,
                          "extra_path",
                          "My Extra",
                          new String[] { "old_path1", "old_path2" },
                          new NoPreviewRevision(1, 2, 3))
                .create();

        assertEquals(PkgType.PKG_EXTRA, p.getType());

        assertTrue  (p.hasFullRevision());
        assertEquals(new FullRevision(1, 2, 3), p.getFullRevision());

        assertFalse(p.hasMajorRevision());
        assertNull (p.getMajorRevision());

        assertFalse(p.hasAndroidVersion());
        assertNull (p.getAndroidVersion());

        assertTrue  (p.hasPath());
        assertEquals("extra_path", p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("extra-vendor-extra_path", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "extras", "vendor", "extra_path"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=extra Vendor=vendor [The Vendor] Path=extra_path FullRev=1.2.3>", p.toString());
        assertEquals("My Extra, rev 1.2.3", p.getListDescription());

        IPkgDescExtra e = (IPkgDescExtra) p;
        assertEquals("vendor [The Vendor]", e.getVendor().toString());
        assertEquals("extra_path", e.getPath());
        assertEquals("[old_path1, old_path2]", Arrays.toString(e.getOldPaths()));
    }

    public final void testPkgDescExtra_Update() {
        IdDisplay vendor = new IdDisplay("vendor", "The Vendor");
        final NoPreviewRevision rev123 = new NoPreviewRevision(1, 2, 3);
        final IPkgDesc p123  = PkgDesc.Builder
                .newExtra(vendor, "extra_path", "My Extra", new String[0], rev123)
                .create();
        final IPkgDesc p123b = PkgDesc.Builder
                .newExtra(vendor, "extra_path", "My Extra", new String[0], rev123)
                .create();

        // can't update itself
        assertFalse(p123 .isUpdateFor(p123b));
        assertFalse(p123b.isUpdateFor(p123));
        assertTrue (p123 .compareTo(p123b) == 0);
        assertTrue (p123b.compareTo(p123 ) == 0);

        // updates a lesser revision of the same vendor/path
        final NoPreviewRevision rev124 = new NoPreviewRevision(1, 2, 4);
        final IPkgDesc p124  = PkgDesc.Builder
                .newExtra(vendor, "extra_path", "My Extra", new String[0], rev124)
                .create();
        assertTrue (p124.isUpdateFor(p123));
        assertTrue (p124.compareTo(p123) > 0);

        // does not update a different vendor
        IdDisplay vendor2 = new IdDisplay("different-vendor", "Not the same Vendor");
        final IPkgDesc a124  = PkgDesc.Builder
                .newExtra(vendor2, "extra_path", "My Extra", new String[0], rev124)
                .create();
        assertFalse(a124.isUpdateFor(p123));
        assertTrue (a124.compareTo(p123) < 0);

        // does not update a different extra path
        final IPkgDesc n124  = PkgDesc.Builder
                .newExtra(vendor, "no_va", "Oye Como Va", new String[0], rev124)
                .create();
        assertFalse(n124.isUpdateFor(p123));
        assertTrue (n124.compareTo(p123) > 0);
        // unless the old_paths mechanism is used to provide a way to update the path
        final IPkgDesc o124  = PkgDesc.Builder
                .newExtra(vendor, "no_va", "Oye Como Va", new String[] { "extra_path" }, rev124)
                .create();
        assertTrue (o124.isUpdateFor(p123));
        assertTrue (o124.compareTo(p123) > 0);
    }

    //----

    public final void testPkgDescSource() throws Exception {
        IPkgDesc p =
                PkgDesc.Builder.newSource(new AndroidVersion("19"), new MajorRevision(1)).create();

        assertEquals(PkgType.PKG_SOURCE, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull (p.getFullRevision());

        assertTrue  (p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue  (p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("source-19", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "sources", "android-19"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals("<PkgDesc Type=source Android=API 19 MajorRev=1>", p.toString());
        assertEquals("Sources for Android 19", p.getListDescription());
    }

    public final void testPkgDescSource_Update() throws Exception {
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        final IPkgDesc p19_1  = PkgDesc.Builder.newSource(api19, rev1).create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newSource(api19, rev1).create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        // updates a lesser revision of the same API
        final IPkgDesc p19_2  = PkgDesc.Builder.newSource(api19, new MajorRevision(2)).create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        // does not update a different API
        final IPkgDesc p18_1  = PkgDesc.Builder.newSource(new AndroidVersion("18"), rev1).create();
        assertFalse(p19_2.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_2));
        assertTrue (p19_2.compareTo(p18_1) > 0);
    }

    //----

    public final void testPkgDescSample() throws Exception {
        IPkgDesc p = PkgDesc.Builder.newSample(new AndroidVersion("19"),
                                       new MajorRevision(1),
                                       new FullRevision(5, 6, 7, 8)).create();

        assertEquals(PkgType.PKG_SAMPLE, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull (p.getFullRevision());

        assertTrue  (p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue  (p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertFalse(p.hasPath());
        assertNull (p.getPath());

        assertTrue  (p.hasMinToolsRev());
        assertEquals(new FullRevision(5, 6, 7, 8), p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("sample-19", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "samples", "android-19"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals(
                "<PkgDesc Type=sample Android=API 19 MajorRev=1 MinToolsRev=5.6.7 rc8>",
                p.toString());
        assertEquals("Samples for Android 19", p.getListDescription());
    }

    public final void testPkgDescSample_Update() throws Exception {
        final FullRevision min5670 = new FullRevision(5, 6, 7, 0);
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        final IPkgDesc p19_1  = PkgDesc.Builder.newSample(api19, rev1, min5670).create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newSample(api19, rev1, min5670).create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        // min-tools-rev isn't used for updates checks
        final FullRevision min5680 = new FullRevision(5, 6, 8, 0);
        final IPkgDesc p19_1c = PkgDesc.Builder.newSample(api19, rev1, min5680).create();
        assertFalse(p19_1c.isUpdateFor(p19_1));
        // but it's used for comparisons
        assertTrue (p19_1c.compareTo(p19_1) > 0);

        // updates a lesser revision of the same API
        final IPkgDesc p19_2  =
                PkgDesc.Builder.newSample(api19, new MajorRevision(2), min5670).create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        // does not update a different API
        final IPkgDesc p18_1  =
                PkgDesc.Builder.newSample(new AndroidVersion("18"), rev1, min5670).create();
        assertFalse(p19_2.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_2));
        assertTrue (p19_2.compareTo(p18_1) > 0);
    }

    //----

    public final void testPkgDescPlatform() throws Exception {
        IPkgDesc p = PkgDesc.Builder.newPlatform(new AndroidVersion("19"),
                                         new MajorRevision(1),
                                         new FullRevision(5, 6, 7, 8)).create();

        assertEquals(PkgType.PKG_PLATFORM, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull (p.getFullRevision());

        assertTrue  (p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue  (p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertTrue  (p.hasPath());
        assertEquals("android-19", p.getPath());

        assertTrue  (p.hasMinToolsRev());
        assertEquals(new FullRevision(5, 6, 7, 8), p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("android-19", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "platforms", "android-19"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals(
                "<PkgDesc Type=platform Android=API 19 Path=android-19 MajorRev=1 MinToolsRev=5.6.7 rc8>",
                p.toString());
        assertEquals("Android SDK Platform 19", p.getListDescription());
    }

    public final void testPkgDescPlatform_Update() throws Exception {
        final FullRevision min5670 = new FullRevision(5, 6, 7, 0);
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        final IPkgDesc p19_1  = PkgDesc.Builder.newPlatform(api19, rev1, min5670).create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newPlatform(api19, rev1, min5670).create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        // min-tools-rev isn't used for updates checks
        final FullRevision min5680 = new FullRevision(5, 6, 8, 0);
        final IPkgDesc p19_1c = PkgDesc.Builder.newPlatform(api19, rev1, min5680).create();
        assertFalse(p19_1c.isUpdateFor(p19_1));
        // but it's used for comparisons
        assertTrue (p19_1c.compareTo(p19_1) > 0);

        // updates a lesser revision of the same API
        final IPkgDesc p19_2  =
                PkgDesc.Builder.newPlatform(api19, new MajorRevision(2), min5670).create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        // does not update a different API
        final IPkgDesc p18_1  =
                PkgDesc.Builder.newPlatform(new AndroidVersion("18"), rev1, min5670).create();
        assertFalse(p19_2.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_2));
        assertTrue (p19_2.compareTo(p18_1) > 0);
    }

    //----

    public final void testPkgDescAddon() throws Exception {
        IdDisplay vendor = new IdDisplay("vendor", "The Vendor");
        IdDisplay name   = new IdDisplay("addon_name", "The Add-on");
        IPkgDesc p1 = PkgDesc.Builder
                .newAddon(new AndroidVersion("19"), new MajorRevision(1), vendor, name)
                .create();

        assertEquals(PkgType.PKG_ADDON, p1.getType());

        assertFalse(p1.hasFullRevision());
        assertNull (p1.getFullRevision());

        assertTrue  (p1.hasMajorRevision());
        assertEquals(new MajorRevision(1), p1.getMajorRevision());

        assertTrue  (p1.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p1.getAndroidVersion());

        assertTrue  (p1.hasPath());
        assertEquals("The Vendor:The Add-on:19", p1.getPath());

        assertFalse(p1.hasMinToolsRev());
        assertNull (p1.getMinToolsRev());

        assertFalse(p1.hasMinPlatformToolsRev());
        assertNull (p1.getMinPlatformToolsRev());

        assertTrue(p1.hasVendor());
        assertEquals(new IdDisplay("vendor", "only the id is compared with"), p1.getVendor());

        assertEquals(new IdDisplay("addon_name", "ignored"), ((IPkgDescAddon) p1).getName());

        assertEquals("addon-addon_name-vendor-19", p1.getInstallId());
        assertEquals(FileOp.append(mRoot, "add-ons", "addon-addon_name-vendor-19"),
                p1.getCanonicalInstallFolder(mRoot));

        assertEquals(
                "<PkgDesc Type=addon Android=API 19 Vendor=vendor [The Vendor] Path=The Vendor:The Add-on:19 MajorRev=1>",
                p1.toString());
        assertEquals("The Add-on, Android 19", p1.getListDescription());
    }

    public final void testPkgDescAddon_Update() throws Exception {
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        IdDisplay vendor = new IdDisplay("vendor", "The Vendor");
        IdDisplay name   = new IdDisplay("addon_name", "The Add-on");
        final IPkgDesc p19_1  = PkgDesc.Builder.newAddon(api19, rev1, vendor, name)
                                               .create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newAddon(api19, rev1, vendor, name)
                                               .create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        // updates a lesser revision of the same API
        final MajorRevision rev2 = new MajorRevision(2);
        final IPkgDesc p19_2  = PkgDesc.Builder.newAddon(api19, rev2, vendor, name)
                                               .create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        // does not update a different API
        final AndroidVersion api18 = new AndroidVersion("18");
        final IPkgDesc p18_1  = PkgDesc.Builder.newAddon(api18, rev2, vendor, name)
                                               .create();
        assertFalse(p19_2.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_2));
        assertTrue (p19_2.compareTo(p18_1) > 0);

        // does not update a different vendor
        IdDisplay vendor2 = new IdDisplay("another_vendor", "Another Vendor");
        final IPkgDesc a19_2  = PkgDesc.Builder.newAddon(api19, rev2, vendor2, name)
                                               .create();
        assertFalse(a19_2.isUpdateFor(p19_1));
        assertTrue (a19_2.compareTo(p19_1) < 0);

        // does not update a different add-on name
        IdDisplay name2   = new IdDisplay("another_name", "Another Add-on");
        final IPkgDesc n19_2  = PkgDesc.Builder.newAddon(api19, rev2, vendor, name2)
                                               .create();
        assertFalse(n19_2.isUpdateFor(p19_1));
        assertTrue (n19_2.compareTo(p19_1) < 0);
    }

    //----

    public final void testPkgDescSysImg_Platform() throws Exception {
        IdDisplay tag = new IdDisplay("tag", "My Tag");
        IPkgDesc p = PkgDesc.Builder.newSysImg(
                new AndroidVersion("19"),
                tag,
                "eabi",
                new MajorRevision(1)).create();

        assertEquals(PkgType.PKG_SYS_IMAGE, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull (p.getFullRevision());

        assertTrue  (p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue  (p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertTrue  (p.hasPath());
        assertEquals("eabi", p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertEquals("sys-img-eabi-tag-19", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "system-images", "android-19", "tag", "eabi"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals(
                "<PkgDesc Type=sys_image Android=API 19 Tag=tag [My Tag] Path=eabi MajorRev=1>",
                p.toString());
        assertEquals("eabi System Image, Android 19", p.getListDescription());
    }

    public final void testPkgDescSysImg_Platform_Update() throws Exception {
        IdDisplay tag1 = new IdDisplay("tag1", "My Tag 1");
        final AndroidVersion api19 = new AndroidVersion("19");
        final MajorRevision rev1 = new MajorRevision(1);
        final IPkgDesc p19_1  = PkgDesc.Builder.newSysImg(api19, tag1, "eabi", rev1).create();
        final IPkgDesc p19_1b = PkgDesc.Builder.newSysImg(api19, tag1, "eabi", rev1).create();

        // can't update itself
        assertFalse(p19_1 .isUpdateFor(p19_1b));
        assertFalse(p19_1b.isUpdateFor(p19_1));
        assertTrue (p19_1 .compareTo(p19_1b) == 0);
        assertTrue (p19_1b.compareTo(p19_1 ) == 0);

        // updates a lesser revision of the same API
        final IPkgDesc p19_2  =
                PkgDesc.Builder.newSysImg(api19, tag1, "eabi", new MajorRevision(2)).create();
        assertTrue (p19_2.isUpdateFor(p19_1));
        assertTrue (p19_2.compareTo(p19_1) > 0);

        // does not update a different API
        final IPkgDesc p18_1  =
                PkgDesc.Builder.newSysImg(new AndroidVersion("18"), tag1, "eabi", rev1).create();
        assertFalse(p19_2.isUpdateFor(p18_1));
        assertFalse(p18_1.isUpdateFor(p19_2));
        assertTrue (p19_2.compareTo(p18_1) > 0);

        // does not update a different ABI
        final IPkgDesc p19_2c =
                PkgDesc.Builder.newSysImg(api19, tag1, "ppc", new MajorRevision(2)).create();
        assertFalse(p19_2c.isUpdateFor(p19_1));
        assertTrue (p19_2c.compareTo(p19_1) > 0);

        // does not update a different tag
        IdDisplay tag2 = new IdDisplay("tag2", "My Tag 2");
        final IPkgDesc p19_t2 =
                PkgDesc.Builder.newSysImg(api19, tag2, "eabi", new MajorRevision(2)).create();
        assertFalse(p19_t2.isUpdateFor(p19_1));
        assertTrue (p19_t2.compareTo(p19_1) > 0);
    }

    public final void testPkgDescSysImg_Addon() throws Exception {
        IdDisplay vendor = new IdDisplay("vendor", "The Vendor");
        IdDisplay name   = new IdDisplay("addon_name", "The Add-on");
        IPkgDesc p = PkgDesc.Builder.newAddonSysImg(
                new AndroidVersion("19"),
                vendor,
                name,
                "eabi",
                new MajorRevision(1)).create();

        assertEquals(PkgType.PKG_ADDON_SYS_IMAGE, p.getType());

        assertFalse(p.hasFullRevision());
        assertNull (p.getFullRevision());

        assertTrue  (p.hasMajorRevision());
        assertEquals(new MajorRevision(1), p.getMajorRevision());

        assertTrue  (p.hasAndroidVersion());
        assertEquals(new AndroidVersion("19"), p.getAndroidVersion());

        assertTrue  (p.hasPath());
        assertEquals("eabi", p.getPath());

        assertFalse(p.hasMinToolsRev());
        assertNull (p.getMinToolsRev());

        assertFalse(p.hasMinPlatformToolsRev());
        assertNull (p.getMinPlatformToolsRev());

        assertTrue(p.hasVendor());
        assertEquals(new IdDisplay("vendor", "only the id is compared with"), p.getVendor());

        assertTrue(p.hasTag());
        assertEquals(new IdDisplay("addon_name", "ignored"), p.getTag());

        assertEquals("sys-img-eabi-addon-addon_name-vendor-19", p.getInstallId());
        assertEquals(FileOp.append(mRoot, "system-images", "addon-addon_name-vendor-19", "eabi"),
                p.getCanonicalInstallFolder(mRoot));

        assertEquals(
                "<PkgDesc Type=addon_sys_image Android=API 19 Vendor=vendor [The Vendor] Tag=addon_name [The Add-on] Path=eabi MajorRev=1>",
                p.toString());
        assertEquals("The Vendor eabi System Image, Android 19", p.getListDescription());
    }

}
