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

package com.android.sdklib.internal.repository.archives;

import com.android.sdklib.internal.repository.DownloadCache;
import com.android.sdklib.internal.repository.ITaskMonitor;
import com.android.sdklib.internal.repository.MockEmptySdkManager;
import com.android.sdklib.internal.repository.MockMonitor;
import com.android.sdklib.internal.repository.packages.ExtraPackage;
import com.android.sdklib.internal.repository.packages.MockEmptyPackage;
import com.android.sdklib.internal.repository.packages.MockExtraPackage;
import com.android.sdklib.internal.repository.sources.SdkRepoSource;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.io.MockFileOp;
import com.android.sdklib.repository.PkgProps;
import com.android.utils.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ArchiveInstaller}.
 */
public class ArchiveInstallerTest extends TestCase {

    private MockMonitor mMon;
    private String mSdkRoot;
    private MockFileOp mFile;
    private MockArchiveInstaller mArchInst;
    private MockEmptySdkManager mSdkMan;

    private class MockArchiveInstaller extends ArchiveInstaller {

        private Map<Archive, File> mDownloadMap = new HashMap<Archive, File>();

        public MockArchiveInstaller(IFileOp fileUtils) {
            super(fileUtils);
        }

        public void setDownloadResponse(Archive archive, File response) {
            mDownloadMap.put(archive, response);
        }

        @Override
        protected Pair<File, File> downloadFile(
                Archive archive,
                String osSdkRoot,
                DownloadCache cache,
                ITaskMonitor monitor,
                boolean forceHttp) {
            File file = mDownloadMap.get(archive);
            // register the file as "created"
            ArchiveInstallerTest.this.mFile.recordExistingFile(file);
            return Pair.of(file, null);
        }

        @Override
        protected boolean unzipFolder(
                ArchiveReplacement archiveInfo,
                File archiveFile,
                File unzipDestFolder,
                ITaskMonitor monitor) {
            // Claim the unzip works if the input archiveFile is one we know about
            // and the destination actually exists.

            if (getFileOp().isDirectory(unzipDestFolder) &&
                    mDownloadMap.values().contains(archiveFile)) {
                return true;
            }

            return false;
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFile = new MockFileOp();
        mArchInst = new MockArchiveInstaller(mFile);
        mSdkRoot = "/sdk";
        mSdkMan = new MockEmptySdkManager(mSdkRoot);
        mMon = new MockMonitor();
    }

    // ----

    /** Test we don't try to install a local archive. */
    public void testInstall_SkipLocalArchive() throws Exception {
        MockEmptyPackage p = new MockEmptyPackage("testPkg");
        ArchiveReplacement ar = new ArchiveReplacement(p.getArchives()[0], null /*replaced*/);

        assertFalse(mArchInst.install(ar, mSdkRoot, false /*forceHttp*/, mSdkMan,
                null /*UrlCache*/, mMon));
        assertTrue(mMon.getCapturedLog().indexOf("Skipping already installed archive") != -1);
    }

    /** Test we can install a simple new archive. */
    public void testInstall_NewArchive() throws Exception {
        SdkSource src1 = new SdkRepoSource("http://repo.example.com/url", "repo1");
        MockEmptyPackage p = createRemoteEmptyPackage(src1, "testPkg");
        ArchiveReplacement ar = new ArchiveReplacement(p.getArchives()[0], null /*replaced*/);

        // associate the File that will be "downloaded" for this archive
        mArchInst.setDownloadResponse(
                p.getArchives()[0], createFile("/sdk", "tmp", "download1.zip"));

        assertTrue(mArchInst.install(ar, mSdkRoot, false /*forceHttp*/, mSdkMan,
                null /*UrlCache*/, mMon));

        // check what was created
        assertEquals(
                "[/sdk/mock/testPkg/source.properties]",
                Arrays.toString(mFile.getExistingFiles()));

        assertEquals(
                "[/, /sdk, /sdk/mock, /sdk/mock/testPkg]",
                Arrays.toString(mFile.getExistingFolders()));

        assertEquals(
             "[</sdk/mock/testPkg/source.properties: '### Android Tool: Source of this archive.\n" +
              "#...date...\n" +
              "Pkg.Revision=0\n" +
              "Pkg.SourceUrl=http\\://repo.example.com/url\n" +
              "'>]",
              stripDate(Arrays.toString(mFile.getOutputStreams())));

        assertEquals(
                "Installing  'testPkg'\n" +
                "Installed  'testPkg'\n",
                mMon.getCapturedLog());
    }

    /** Test we can replace and rename an Extra package. */
    public void testInstall_InstallExtraArchive() throws Exception {
        SdkSource src1 = new SdkRepoSource("http://repo.example.com/url", "repo1");

        MockExtraPackage newPkg = createRemoteExtraPackage(src1, "vendor1", "oldPath", 2, 1);
        MockExtraPackage oldPkg = new MockExtraPackage(src1, "vendor1", "oldPath", 1, 1);

        // old pkg is installed, so its directory & files exists
        mFile.recordExistingFile("/sdk/extras/vendor1/oldPath/source.properties");
        mFile.recordExistingFolder("/sdk/extras/vendor1/oldPath");

        ArchiveReplacement ar = new ArchiveReplacement(
                newPkg.getArchives()[0],
                oldPkg.getArchives()[0]);

        // associate the File that will be "downloaded" for this archive
        mArchInst.setDownloadResponse(
                newPkg.getArchives()[0], createFile("/sdk", "tmp", "download1.zip"));

        assertTrue(mArchInst.install(ar, mSdkRoot, false /*forceHttp*/, mSdkMan,
                null /*UrlCache*/, mMon));

        // check what was created
        assertEquals(
                "[/sdk/extras/vendor1/oldPath/source.properties]",
                Arrays.toString(mFile.getExistingFiles()));

        // This created the /sdk/temp folder to put the oldPath package whilst we unzipped
        // the new one. The oldPath dir was then cleaned up  but we still leave the root
        // temp dir around.
        assertEquals(
              "[/, /sdk, /sdk/extras, /sdk/extras/vendor1, /sdk/extras/vendor1/oldPath, /sdk/temp]",
              Arrays.toString(mFile.getExistingFolders()));

        assertEquals(
                (
                "[</sdk/extras/vendor1/oldPath/source.properties: " +
                     "'### Android Tool: Source of this archive.\n" +
                "#...date...\n" +
                "Extra.NameDisplay=Vendor1 OldPath\n" +
                "Extra.Path=oldPath\n" +
                "Extra.VendorDisplay=vendor1\n" +
                "Extra.VendorId=vendor1\n" +
                "Pkg.Desc=desc\n" +
                "Pkg.DescUrl=url\n" +
                "Pkg.Revision=2.0.0\n" +
                "Pkg.SourceUrl=http\\://repo.example.com/url\n" +
                "'>]"),
                stripDate(Arrays.toString(mFile.getOutputStreams())));

        assertEquals(
                "Installing Vendor1 OldPath, revision 2\n" +
                "Installed Vendor1 OldPath, revision 2\n",
                mMon.getCapturedLog());
    }

    /** Test we can replace and rename an Extra package. */
    public void testInstall_InstallRenamedExtraArchive() throws Exception {
        SdkSource src1 = new SdkRepoSource("http://repo.example.com/url", "repo1");

        MockExtraPackage newPkg = createRemoteExtraPackage(
                src1,
                "vendor1",
                "newPath",
                "oldPath",
                2,  // revision
                1); // min_platform_tools_rev
        ExtraPackage oldPkg = (ExtraPackage) ExtraPackage.create(
                src1,       // source
                null,       // props
                "vendor1",  // vendor
                "oldPath",  // path
                1,          // revision
                null,       // license
                null,       // description
                null,       // descUrl
                "/sdk/extras/vendor1/oldPath" // archiveOsPath
                );

        // old pkg is installed, so its directory & files exists
        mFile.recordExistingFile("/sdk/extras/vendor1/oldPath/source.properties");
        mFile.recordExistingFolder("/sdk/extras/vendor1/oldPath");

        ArchiveReplacement ar = new ArchiveReplacement(
                newPkg.getArchives()[0],
                oldPkg.getArchives()[0]);

        // associate the File that will be "downloaded" for this archive
        mArchInst.setDownloadResponse(
                newPkg.getArchives()[0], createFile("/sdk", "tmp", "download1.zip"));

        assertTrue(mArchInst.install(ar, mSdkRoot, false /*forceHttp*/, mSdkMan,
                null /*UrlCache*/, mMon));

        // check what was created
        assertEquals(
                "[/sdk/extras/vendor1/newPath/source.properties]",
                Arrays.toString(mFile.getExistingFiles()));

        // oldPath directory has been deleted, we only have newPath now.
        // No sdk/temp dir was created since we didn't have to move the old package dir out
        // of the way.
        assertEquals(
                "[/, /sdk, /sdk/extras, /sdk/extras/vendor1, /sdk/extras/vendor1/newPath]",
                Arrays.toString(mFile.getExistingFolders()));

        assertEquals(
                (
                "[</sdk/extras/vendor1/newPath/source.properties: " +
                     "'### Android Tool: Source of this archive.\n" +
                "#...date...\n" +
                "Extra.NameDisplay=Vendor1 NewPath\n" +
                "Extra.OldPaths=oldPath\n" +
                "Extra.Path=newPath\n" +
                "Extra.VendorDisplay=vendor1\n" +
                "Extra.VendorId=vendor1\n" +
                "Pkg.Desc=desc\n" +
                "Pkg.DescUrl=url\n" +
                "Pkg.Revision=2.0.0\n" +
                "Pkg.SourceUrl=http\\://repo.example.com/url\n" +
                "'>]"),
                stripDate(Arrays.toString(mFile.getOutputStreams())));

        assertEquals(
                "Installing Vendor1 NewPath, revision 2\n" +
                "Installed Vendor1 NewPath, revision 2\n",
                mMon.getCapturedLog());
    }

    // ----

    /**
     * Helper creator method to create a {@link MockEmptyPackage} with no local
     * archive associated.
     */
    private static MockEmptyPackage createRemoteEmptyPackage(SdkSource source, String testHandle) {
        return new MockEmptyPackage(source, testHandle, 0 /*revision*/) {
            @Override
            protected Archive[] initializeArchives(
                    Properties props,
                    String archiveOsPath) {
                // Create one remote archive for this package
                return new Archive[] {
                        new Archive(
                            this,
                            null,       // arch
                            "http://some.source/some_url",
                            1234,       // size
                            "abcdef")   // sha1
                        };
            }
        };
    }

    /**
     * Helper creator method to create a {@link MockExtraPackage} with no local
     * archive associated.
     */
    private static MockExtraPackage createRemoteExtraPackage(
            SdkSource source,
            String vendor,
            String path,
            int revision,
            int min_platform_tools_rev) {
        return new MockExtraPackage(source, vendor, path, revision, min_platform_tools_rev) {
            @Override
            protected Archive[] initializeArchives(
                    Properties props,
                    String archiveOsPath) {
                // Create one remote archive for this package
                return new Archive[] {
                        new Archive(
                            this,
                            null,       // arch
                            "http://some.source/some_url",
                            1234,       // size
                            "abcdef")   // sha1
                        };
            }
        };
    }

    /**
     * Helper creator method to create a {@link MockExtraPackage} with no local
     * archive associated and a specific oldPaths attribute.
     */
    private static MockExtraPackage createRemoteExtraPackage(
            SdkSource source,
            String vendor,
            String newPath,
            String oldPaths,
            int revision,
            int min_platform_tools_rev) {
        Properties props = new Properties();
        props.setProperty(PkgProps.EXTRA_OLD_PATHS, oldPaths);
        props.setProperty(PkgProps.MIN_PLATFORM_TOOLS_REV,
                Integer.toString((min_platform_tools_rev)));
        return new MockExtraPackage(source, props, vendor, newPath, revision) {
            @Override
            protected Archive[] initializeArchives(
                    Properties props2,
                    String archiveOsPath) {
                // Create one remote archive for this package
                return new Archive[] {
                        new Archive(
                            this,
                            null,       // arch
                            "http://some.source/some_url",
                            1234,       // size
                            "abcdef")   // sha1
                        };
            }
        };
    }

    private File createFile(String...segments) {
        File f = null;
        for (String segment : segments) {
            if (f == null) {
                f = new File(segment);
            } else {
                f = new File(f, segment);
            }
        }

        return f;
    }

    /**
     * Strips the second line of the string.
     * The source.properties generated by Java contain the generation data on the
     * second line and this is of course not suitable for unit tests.
     */
    private String stripDate(String string) {
        // We know it's a date if it looks like:
        // \n # ... YYYY\n
        Pattern p = Pattern.compile("\n#[^#][^\n]*[0-9]{4}\\w*\n", Pattern.DOTALL);
        string = p.matcher(string.replaceAll("\r\n", "\n")).replaceAll("\n#...date...\n");
        return string;
    }
}
