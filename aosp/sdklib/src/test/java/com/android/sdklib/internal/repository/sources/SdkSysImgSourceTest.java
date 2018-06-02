/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.repository.sources;

import com.android.sdklib.internal.repository.ITaskMonitor;
import com.android.sdklib.internal.repository.MockMonitor;
import com.android.sdklib.internal.repository.packages.Package;
import com.android.sdklib.internal.repository.packages.SystemImagePackage;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.repository.SdkSysImgConstants;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests for {@link SdkSysImgSource}.
 */
public class SdkSysImgSourceTest extends TestCase {

    /**
     * An internal helper class to give us visibility to the protected members we want
     * to test.
     */
    private static class MockSdkSysImgSource extends SdkSysImgSource {
        public MockSdkSysImgSource() {
            super("fake-url", null /*uiName*/);
        }

        public Document _findAlternateToolsXml(InputStream xml) {
            return super.findAlternateToolsXml(xml);
        }

        public boolean _parsePackages(Document doc, String nsUri, ITaskMonitor monitor) {
            return super.parsePackages(doc, nsUri, monitor);
        }

        public int _getXmlSchemaVersion(InputStream xml) {
            return super.getXmlSchemaVersion(xml);
        }

        public String _validateXml(InputStream xml, String url, int version,
                                   String[] outError, Boolean[] validatorFound) {
            return super.validateXml(xml, url, version, outError, validatorFound);
        }

        public Document _getDocument(InputStream xml, ITaskMonitor monitor) {
            return super.getDocument(xml, monitor);
        }

    }

    private MockSdkSysImgSource mSource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mSource = new MockSdkSysImgSource();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mSource = null;
    }

    /**
     * Validate that findAlternateToolsXml doesn't work for this source even
     * when trying to load a valid xml. That's because finding alternate tools
     * is not supported by this kind of source.
     */
    public void testFindAlternateToolsXml_1() throws Exception {
        InputStream xmlStream =
            getTestResource("/com/android/sdklib/testdata/sys_img_sample_1.xml");

        Document result = mSource._findAlternateToolsXml(xmlStream);
        assertNull(result);
    }

    /**
     * Validate we can load a valid schema version 1
     */
    public void testLoadSysImgXml_1() throws Exception {
        InputStream xmlStream =
            getTestResource("/com/android/sdklib/testdata/sys_img_sample_1.xml");

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(1, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkSysImgConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkSysImgConstants.getSchemaUri(1), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals(
                "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                "Found ARM EABI System Image, Android API 42, revision 12\n" +
                "Found MIPS System Image, Android API 42, revision 12\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found...
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.

        Package[] pkgs = mSource.getPackages();

        assertEquals(4, pkgs.length);
        for (Package p : pkgs) {
            // We expected to find packages with each at least one archive.
            assertTrue(p.getArchives().length >= 1);
            // And only system images are supported by this source
            assertTrue(p instanceof SystemImagePackage);
        }

        // Check the system-image packages
        ArrayList<String> sysImgVersionAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                String v = sip.getAndroidVersion().getApiString();
                String a = sip.getAbi();
                sysImgVersionAbi.add(String.format("%1$s %2$s", v, a)); //$NON-NLS-1$
            }
        }
        assertEquals(
                "[42 armeabi, " +
                 "42 mips, " +
                 "2 armeabi-v7a, " +
                 "2 x86]",
                Arrays.toString(sysImgVersionAbi.toArray()));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[ARM EABI System Image, " +
                "MIPS System Image, " +
                "ARM EABI v7a System Image, " +
                "Intel x86 Atom System Image]",
                Arrays.toString(listDescs.toArray()));
    }

    /**
     * Validate we can load a valid schema version 2
     */
    public void testLoadSysImgXml_2() throws Exception {
        InputStream xmlStream =
            getTestResource("/com/android/sdklib/testdata/sys_img_sample_2.xml");

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(2, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkSysImgConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkSysImgConstants.getSchemaUri(2), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        // Verbose log order matches the XML order and not the sorted display order.
        assertEquals(
                "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                "Found Another tag name ARM EABI v7a System Image, Android API 2, revision 2\n" +
                "Found ARM EABI System Image, Android API 42, revision 12\n" +
                "Found MIPS System Image, Android API 42, revision 12\n" +
                "Found This is an arbitrary string, MIPS System Image, Android API 44, revision 14\n" +
                "Found Tag name is Sanitized if Display is Missing MIPS System Image, Android API 45, revision 15\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found...
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        // Order is defined by
        // com.android.sdklib.internal.repository.packages.SystemImagePackage.comparisonKey()

        Package[] pkgs = mSource.getPackages();

        assertEquals(7, pkgs.length);
        for (Package p : pkgs) {
            // We expected to find packages with each at least one archive.
            assertTrue(p.getArchives().length >= 1);
            // And only system images are supported by this source
            assertTrue(p instanceof SystemImagePackage);
        }

        // Check the system-image packages
        ArrayList<String> sysImgInfo = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                sysImgInfo.add(String.format("%1$s %2$s: %3$s",     //$NON-NLS-1$
                        sip.getAndroidVersion().getApiString(),
                        sip.getAbi(),
                        sip.getTag()));
            }
        }
        assertEquals(
                "[45 mips: tag-name---is-Sanitized----if-Display-is-Missing [Tag name is Sanitized if Display is Missing], " +
                 "44 mips: mips-only [This is an arbitrary string,], " +
                 "42 armeabi: default [Default], " +
                 "42 mips: default [Default], " +
                 "2 armeabi-v7a: default [Ignored in description for default tag], " +
                 "2 x86: default [Default], " +
                 "2 armeabi-v7a: other [Another tag name]]",
                Arrays.toString(sysImgInfo.toArray()));

        // Check the default install-paths of the packages
        ArrayList<File> sysImgPath = new ArrayList<File>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                sysImgPath.add(sip.getInstallFolder("root", null /*sdkManager*/));    //$NON-NLS-1$
            }
        }
        assertEquals(Arrays.toString(new File[] {
                FileOp.append("root", "system-images", "android-45", "tag-name-is-sanitized-if-display-is-missing", "mips"),
                FileOp.append("root", "system-images", "android-44", "mips-only", "mips"),
                FileOp.append("root", "system-images", "android-42", "default", "armeabi"),
                FileOp.append("root", "system-images", "android-42", "default", "mips"),
                FileOp.append("root", "system-images", "android-2" , "default", "armeabi-v7a"),
                FileOp.append("root", "system-images", "android-2" , "default", "x86"),
                FileOp.append("root", "system-images", "android-2" , "other",   "armeabi-v7a"),
                }).replace(File.separatorChar, '/'),
                Arrays.toString(sysImgPath.toArray()).replace(File.separatorChar, '/'));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[Tag name is Sanitized if Display is Missing MIPS System Image, " +
                "This is an arbitrary string, " +
                "MIPS System Image, " +
                "ARM EABI System Image, " +
                "MIPS System Image, " +
                "ARM EABI v7a System Image, " +
                "Intel x86 Atom System Image, " +
                "Another tag name ARM EABI v7a System Image]",
                Arrays.toString(listDescs.toArray()));
    }

    /**
     * Validate we can load a valid schema version 3
     */
    public void testLoadSysImgXml_3() throws Exception {
        InputStream xmlStream =
            getTestResource("/com/android/sdklib/testdata/sys_img_sample_3.xml");

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(3, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkSysImgConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkSysImgConstants.getSchemaUri(3), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        // Verbose log order matches the XML order and not the sorted display order.
        assertEquals(
                "Found System Image for x86 CPU for API 2, Android API 2, revision 1\n" +
                "Found System Image for x86-64 CPU for API 2, Android API 2, revision 1\n" +
                "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                "Found ARM 64 v8a System Image, Android API 2, revision 2\n" +
                "Found Another tag name ARM EABI v7a System Image, Android API 2, revision 2\n" +
                "Found ARM EABI System Image, Android API 42, revision 12\n" +
                "Found MIPS64 System Image, Android API 42, revision 12\n" +
                "Found MIPS system image for tag MIPS-only, Android API 44, revision 14\n" +
                "Found Tag name is Sanitized if Display is Missing MIPS System Image, Android API 45, revision 15\n" +
                "Found x86 System Image for some add-on, Acme Vendor Inc. API 2, revision 1\n" +
                "Found Some Add-on ARM EABI v7a System Image, Acme Vendor Inc. API 2, revision 2\n" +
                "Found Some Add-on Intel x86 Atom_64 System Image, Acme Vendor Inc. API 2, revision 3\n" +
                "Found Some Add-on ARM 64 v8a System Image, Acme Vendor Inc. API 2, revision 4\n" +
                "Found Some Add-on MIPS System Image, Acme Vendor Inc. API 2, revision 5\n" +
                "Found Some Add-on MIPS64 System Image, Acme Vendor Inc. API 2, revision 6\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found...
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        // Order is defined by
        // com.android.sdklib.internal.repository.packages.SystemImagePackage.comparisonKey()

        Package[] pkgs = mSource.getPackages();

        assertEquals(15, pkgs.length);
        for (Package p : pkgs) {
            // We expected to find packages with each at least one archive.
            assertTrue(p.getArchives().length >= 1);
            // And only system images are supported by this source
            assertTrue(p instanceof SystemImagePackage);
        }

        // Check the system-image packages
        ArrayList<String> sysImgInfo = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                sysImgInfo.add(String.format("%1$s %2$s: %3$s",     //$NON-NLS-1$
                        sip.getAndroidVersion().getApiString(),
                        sip.getAbi(),
                        sip.getTag()));
            }
        }
        assertEquals(
                "[45 mips: tag-name---is-Sanitized----if-Display-is-Missing [Tag name is Sanitized if Display is Missing]\n" +
                 "44 mips: mips-only [This is an arbitrary string,]\n" +
                 "42 armeabi: default [Default]\n" +
                 "42 mips64: default [Default]\n" +
                  "2 arm64-v8a: default [Ignored in description for default tag]\n" +
                  "2 armeabi-v7a: default [Ignored in description for default tag]\n" +
                  "2 x86_64: default [Default]\n" +
                  "2 x86: default [Default]\n" +
                  "2 armeabi-v7a: other [Another tag name]\n" +
                  "2 arm64-v8a: some-addon [Some Add-on]\n" +
                  "2 armeabi-v7a: some-addon [Some Add-on]\n" +
                  "2 x86_64: some-addon [Some Add-on]\n" +
                  "2 x86: some-addon [Some Add-on]\n" +
                  "2 mips64: some-addon [Some Add-on]\n" +
                  "2 mips: some-addon [Some Add-on]]",
                Arrays.toString(sysImgInfo.toArray()).replace(", ", "\n"));

        // Check the default install-paths of the packages
        ArrayList<File> sysImgPath = new ArrayList<File>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                sysImgPath.add(sip.getInstallFolder("root", null /*sdkManager*/));    //$NON-NLS-1$
            }
        }
        assertEquals(Arrays.toString(new File[] {
                FileOp.append("root", "system-images", "android-45", "tag-name-is-sanitized-if-display-is-missing", "mips"),
                FileOp.append("root", "system-images", "android-44", "mips-only",  "mips"),
                FileOp.append("root", "system-images", "android-42", "default",    "armeabi"),
                FileOp.append("root", "system-images", "android-42", "default",    "mips64"),
                FileOp.append("root", "system-images", "android-2" , "default",    "arm64-v8a"),
                FileOp.append("root", "system-images", "android-2" , "default",    "armeabi-v7a"),
                FileOp.append("root", "system-images", "android-2" , "default",    "x86_64"),
                FileOp.append("root", "system-images", "android-2" , "default",    "x86"),
                FileOp.append("root", "system-images", "android-2" , "other",      "armeabi-v7a"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "arm64-v8a"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "armeabi-v7a"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "x86_64"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "x86"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "mips64"),
                FileOp.append("root", "system-images", "android-2" , "some-addon", "mips"),
                }).replace(File.separatorChar, '/').replace(", ", "\n"),
                Arrays.toString(sysImgPath.toArray()).replace(File.separatorChar, '/').replace(", ", "\n"));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[Tag name is Sanitized if Display is Missing MIPS System Image\n" +
                 "MIPS system image for tag MIPS-only\n" +          // list-display override
                 "ARM EABI System Image\n" +
                 "MIPS64 System Image\n" +
                 "ARM 64 v8a System Image\n" +
                 "ARM EABI v7a System Image\n" +
                 "System Image for x86-64 CPU for API 2\n" +        // list-display override
                 "System Image for x86 CPU for API 2\n" +           // list-display override
                 "Another tag name ARM EABI v7a System Image\n" +
                 "Some Add-on ARM 64 v8a System Image\n" +
                 "Some Add-on ARM EABI v7a System Image\n" +
                 "Some Add-on Intel x86 Atom_64 System Image\n" +
                 "x86 System Image for some add-on\n" +             // list-display override
                 "Some Add-on MIPS64 System Image\n" +
                 "Some Add-on MIPS System Image]",
                Arrays.toString(listDescs.toArray()).replace(", ", "\n"));

        // Check platfomr vs add-ons system-images
        ArrayList<String> addonProps = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SystemImagePackage) {
                SystemImagePackage sip = (SystemImagePackage) p;
                String s = sip.isPlatform() ? "Platform" : "Addon: ";
                if (!sip.isPlatform()) {
                    s += sip.getTag().toString() + " by " + sip.getAddonVendor().toString();
                }
                addonProps.add(s);
            }
        }
        assertEquals(
                "[Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Platform\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]\n" +
                 "Addon: some-addon [Some Add-on] by some-vendor [Acme Vendor Inc.]]",
                Arrays.toString(addonProps.toArray()).replace(", ", "\n"));

    }

    /**
     * Validate there isn't a next-version we haven't tested yet
     */
    public void testLoadSysImgXml_4() throws Exception {
        InputStream xmlStream = getTestResource("/com/android/sdklib/testdata/sys_img_sample_4.xml");
        assertNull("There is a sample for sys-img-4.xsd but there is not corresponding unit test", xmlStream);
    }


    //-----

    /**
     * Returns an SdkLib file resource as a {@link ByteArrayInputStream},
     * which has the advantage that we can use {@link InputStream#reset()} on it
     * at any time to read it multiple times.
     * <p/>
     * The default for getResourceAsStream() is to return a {@link FileInputStream} that
     * does not support reset(), yet we need it in the tested code.
     *
     * @throws IOException if some I/O read fails
     */
    private ByteArrayInputStream getTestResource(String filename) throws IOException {
        InputStream xmlStream = this.getClass().getResourceAsStream(filename);
        if (xmlStream == null) {
            return null;
        }
        try {
            byte[] data = new byte[8192];
            int offset = 0;
            int n;

            while ((n = xmlStream.read(data, offset, data.length - offset)) != -1) {
                offset += n;

                if (offset == data.length) {
                    byte[] newData = new byte[offset + 8192];
                    System.arraycopy(data, 0, newData, 0, offset);
                    data = newData;
                }
            }

            return new ByteArrayInputStream(data, 0, offset);
        } finally {
            if (xmlStream != null) {
                xmlStream.close();
            }
        }
    }
}
