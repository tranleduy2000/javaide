/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.ITaskMonitor;
import com.android.sdklib.internal.repository.MockEmptySdkManager;
import com.android.sdklib.internal.repository.MockMonitor;
import com.android.sdklib.internal.repository.packages.ExtraPackage;
import com.android.sdklib.internal.repository.packages.IMinPlatformToolsDependency;
import com.android.sdklib.internal.repository.packages.IMinToolsDependency;
import com.android.sdklib.internal.repository.packages.Package;
import com.android.sdklib.internal.repository.packages.PlatformPackage;
import com.android.sdklib.internal.repository.packages.PlatformToolPackage;
import com.android.sdklib.internal.repository.packages.SourcePackage;
import com.android.sdklib.internal.repository.packages.SystemImagePackage;
import com.android.sdklib.internal.repository.packages.ToolPackage;
import com.android.sdklib.repository.SdkRepoConstants;
import com.android.utils.Pair;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests for {@link SdkRepoSource}
 */
public class SdkRepoSourceTest extends TestCase {

    /**
     * An internal helper class to give us visibility to the protected members we want
     * to test.
     */
    private static class MockSdkRepoSource extends SdkRepoSource {
        public MockSdkRepoSource() {
            super("fake-url", null /*uiName*/);
        }

        /**
         * Returns a pair of Document (which can be null) and the captured stdout/stderr output
         * (which is the empty string by default).
         */
        public Pair<Document, String> _findAlternateToolsXml(InputStream xml) throws IOException {

            final StringBuilder output = new StringBuilder();
            Document doc = super.findAlternateToolsXml(xml, new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    output.append("WARN: " + exception.getMessage()).append('\n');
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    output.append("FATAL: " + exception.getMessage()).append('\n');
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    output.append("ERROR: " + exception.getMessage()).append('\n');
                }
            });

            return Pair.of(doc, output.toString());
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

    private MockSdkRepoSource mSource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("THROW_DEEP_EXCEPTION_DURING_TESTING", "1");

        mSource = new MockSdkRepoSource();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mSource = null;
    }

    public void testFindAlternateToolsXml_Errors() throws Exception {
        // Support null as input
        Pair<Document, String> result = mSource._findAlternateToolsXml(null);
        assertEquals(Pair.of((Document) null, ""), result);

        // Support an empty input
        String str = "";
        ByteArrayInputStream input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertEquals(
                Pair.of((Document) null, "FATAL: Premature end of file.\n"),
                result);

        // Support a random string as input
        str = "Some random string, not even HTML nor XML";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertEquals(
                Pair.of((Document) null, "FATAL: Content is not allowed in prolog.\n"),
                result);

        // Support an HTML input, e.g. a typical 404 document as returned by DL
        str = "<html><head> " +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"> " +
        "<title>404 Not Found</title> " + "<style><!--" + "body {font-family: arial,sans-serif}" +
        "div.nav { ... blah blah more css here ... color: green}" +
        "//--></style> " + "<script><!--" + "var rc=404;" + "//-->" + "</script> " + "</head> " +
        "<body text=#000000 bgcolor=#ffffff> " +
        "<table border=0 cellpadding=2 cellspacing=0 width=100%><tr><td rowspan=3 width=1% nowrap> " +
        "<b><font face=times color=#0039b6 size=10>G</font><font face=times color=#c41200 size=10>o</font><font face=times color=#f3c518 size=10>o</font><font face=times color=#0039b6 size=10>g</font><font face=times color=#30a72f size=10>l</font><font face=times color=#c41200 size=10>e</font>&nbsp;&nbsp;</b> " +
        "<td>&nbsp;</td></tr> " +
        "<tr><td bgcolor=\"#3366cc\"><font face=arial,sans-serif color=\"#ffffff\"><b>Error</b></td></tr> " +
        "<tr><td>&nbsp;</td></tr></table> " + "<blockquote> " + "<H1>Not Found</H1> " +
        "The requested URL <code>/404</code> was not found on this server." + " " + "<p> " +
        "</blockquote> " +
        "<table width=100% cellpadding=0 cellspacing=0><tr><td bgcolor=\"#3366cc\"><img alt=\"\" width=1 height=4></td></tr></table> " +
        "</body></html> ";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertEquals(
                Pair.of((Document) null, "FATAL: The element type \"meta\" must be terminated by the matching end-tag \"</meta>\".\n"),
                result);

        // Support some random XML document, totally unrelated to our sdk-repository schema
        str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
        "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"" +
        "    package=\"some.cool.app\" android:versionName=\"1.6.04\" android:versionCode=\"1604\">" +
        "    <application android:label=\"@string/app_name\" android:icon=\"@drawable/icon\"/>" +
        "</manifest>";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertEquals(Pair.of((Document) null, ""), result);
    }

    /**
     * Validate we can load a new schema version 3 using the "alternate future tool" mode.
     */
    public void testFindAlternateToolsXml_3() throws Exception {
        InputStream xmlStream = getTestResource(
                    "/com/android/sdklib/testdata/repository_sample_03.xml");

        Pair<Document, String> result = mSource._findAlternateToolsXml(xmlStream);
        assertNotNull(result.getFirst());
        assertEquals("", result.getSecond());
        MockMonitor monitor = new MockMonitor();
        assertTrue(mSource._parsePackages(result.getFirst(), SdkRepoConstants.NS_URI, monitor));

        assertEquals("Found Android SDK Tools, revision 1\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 2 tool packages and 1
        // platform-tools package, with at least 1 archive each.
        Package[] pkgs = mSource.getPackages();
        assertEquals(3, pkgs.length);
        for (Package p : pkgs) {
            assertTrue((p instanceof ToolPackage) || (p instanceof PlatformToolPackage));
            assertTrue(p.getArchives().length >= 1);
        }
    }

    /**
     * Validate we can still load an old repository in schema version 1
     */
    public void testLoadRepoXml_01() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_01.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(1, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(1), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found My First add-on, Android API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found My Second add-on, Android API 2, revision 42\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found This add-on has no libraries, Android API 4, revision 3\n" +
                     "Found Usb Driver, revision 43\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 11 packages with each at least
        // one archive.
        Package[] pkgs = mSource.getPackages();
        assertEquals(11, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the extra packages path, vendor, install folder

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths   = new ArrayList<String>();
        ArrayList<String> extraVendors = new ArrayList<String>();
        ArrayList<File>   extraInstall = new ArrayList<File>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                extraPaths.add(ep.getPath());
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));
            }
        }
        assertEquals(
                "[usb_driver]",
                Arrays.toString(extraPaths.toArray()));
        assertEquals(
                "[/]",
                Arrays.toString(extraVendors.toArray()));
        assertEquals(
                "[SDK/extras/usb_driver]".replace('/', File.separatorChar),
                Arrays.toString(extraInstall.toArray()));
    }

    /**
     * Validate we can still load an old repository in schema version 2
     */
    public void testLoadRepoXml_02() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_02.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(2, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(2), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found My First add-on, Android API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found My Second add-on, Android API 2, revision 42\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found This add-on has no libraries, Android API 4, revision 3\n" +
                     "Found Usb Driver, revision 43 (Obsolete)\n" +
                     "Found Extra API Dep, revision 2 (Obsolete)\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        Package[] pkgs = mSource.getPackages();
        assertEquals(13, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the extra packages path, vendor, install folder

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths   = new ArrayList<String>();
        ArrayList<String> extraVendors = new ArrayList<String>();
        ArrayList<File>   extraInstall = new ArrayList<File>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                extraPaths.add(ep.getPath());
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));
            }
        }
        assertEquals(
                "[extra_api_dep, usb_driver]",
                Arrays.toString(extraPaths.toArray()));
        assertEquals(
                "[/, /]",
                Arrays.toString(extraVendors.toArray()));
        assertEquals(
                "[SDK/extras/extra_api_dep, SDK/extras/usb_driver]".replace('/', File.separatorChar),
                Arrays.toString(extraInstall.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 3
     */
    public void testLoadRepoXml_03() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_03.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(3, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(3), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                    "Found Documentation for Android SDK, API 1, revision 1\n" +
                    "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                    "Found SDK Platform Android Pastry Preview, revision 3\n" +
                    "Found Android SDK Tools, revision 1\n" +
                    "Found Documentation for Android SDK, API 2, revision 42\n" +
                    "Found Android SDK Tools, revision 42\n" +
                    "Found Android SDK Platform-tools, revision 3\n" +
                    "Found A USB Driver, revision 43 (Obsolete)\n" +
                    "Found Android Vendor Extra API Dep, revision 2 (Obsolete)\n" +
                    "Found Samples for SDK API 14, revision 24 (Obsolete)\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        Package[] pkgs = mSource.getPackages();
        assertEquals(11, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the extra packages path, vendor, install folder

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths   = new ArrayList<String>();
        ArrayList<String> extraVendors = new ArrayList<String>();
        ArrayList<File>   extraInstall = new ArrayList<File>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                extraPaths.add(ep.getPath());
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));
            }
        }
        assertEquals(
                "[extra_api_dep, usb_driver]",
                Arrays.toString(extraPaths.toArray()));
        assertEquals(
                "[android_vendor/android_vendor, a/a]",
                Arrays.toString(extraVendors.toArray()));
        assertEquals(
                "[SDK/extras/android_vendor/extra_api_dep, SDK/extras/a/usb_driver]"
                 .replace('/', File.separatorChar),
                Arrays.toString(extraInstall.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 4
     */
    public void testLoadRepoXml_04() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_04.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(4, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(4), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                    "Found Documentation for Android SDK, API 1, revision 1\n" +
                    "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                    "Found SDK Platform Android Pastry Preview, revision 3\n" +
                    "Found Android SDK Tools, revision 1\n" +
                    "Found Documentation for Android SDK, API 2, revision 42\n" +
                    "Found Android SDK Tools, revision 42\n" +
                    "Found Android SDK Platform-tools, revision 3\n" +
                    "Found A USB Driver, revision 43 (Obsolete)\n" +
                    "Found Android Vendor Extra API Dep, revision 2 (Obsolete)\n" +
                    "Found Samples for SDK API 14, revision 24 (Obsolete)\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        Package[] pkgs = mSource.getPackages();

        assertEquals(11, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));

        // Check the extra packages path, vendor, install folder and project-files

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                extraPaths.add(ep.getPath());
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }
        assertEquals(
                "[extra_api_dep, usb_driver]",
                Arrays.toString(extraPaths.toArray()));
        assertEquals(
                "[android_vendor/android_vendor, a/a]",
                Arrays.toString(extraVendors.toArray()));
        assertEquals(
                "[SDK/extras/android_vendor/extra_api_dep, SDK/extras/a/usb_driver]"
                 .replace('/', File.separatorChar),
                Arrays.toString(extraInstall.toArray()));
        assertEquals(
                "[[v8/veggies_8.jar, readme.txt, dir1/dir 2 with space/mylib.jar], " +
                "[]]",
                Arrays.toString(extraFilePaths.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 5
     */
    public void testLoadRepoXml_05() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_05.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(5, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(5), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found Sources for Android SDK, API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                     "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3\n" +
                     "Found A USB Driver, revision 43 (Obsolete)\n" +
                     "Found Android Vendor Extra API Dep, revision 2 (Obsolete)\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n" +
                     "Found ARM EABI System Image, Android API 42, revision 12\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        Package[] pkgs = mSource.getPackages();

        assertEquals(17, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }
        assertEquals(
                "[extra_api_dep [path1, old_path2, oldPath3], " +
                 "usb_driver []]",
                Arrays.toString(extraPaths.toArray()));
        assertEquals(
                "[android_vendor/android_vendor, " +
                 "a/a]",
                Arrays.toString(extraVendors.toArray()));
        assertEquals(
                ("[SDK/extras/android_vendor/extra_api_dep, " +
                  "SDK/extras/a/usb_driver]").replace('/', File.separatorChar),
                Arrays.toString(extraInstall.toArray()));
        assertEquals(
                "[[v8/veggies_8.jar, readme.txt, dir1/dir 2 with space/mylib.jar], " +
                 "[]]",
                Arrays.toString(extraFilePaths.toArray()));

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
                "2 armeabi-v7a, " +
                "2 x86]",
                Arrays.toString(sysImgVersionAbi.toArray()));

        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 6
     */
    public void testLoadRepoXml_06() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_06.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(6, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(6), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found Sources for Android SDK, API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                     "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n" +
                     "Found ARM EABI System Image, Android API 42, revision 12\n" +
                     "Found MIPS System Image, Android API 42, revision 12\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        Package[] pkgs = mSource.getPackages();

        assertEquals(16, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }

        // There are no extra packages anymore in repository-6
        assertEquals("[]", Arrays.toString(extraPaths.toArray()));
        assertEquals("[]", Arrays.toString(extraVendors.toArray()));
        assertEquals("[]", Arrays.toString(extraInstall.toArray()));
        assertEquals("[]", Arrays.toString(extraFilePaths.toArray()));

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

        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 7
     */
    public void testLoadRepoXml_07() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_07.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(7, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(7), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found Sources for Android SDK, API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                     "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1.2.3 rc4\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3 rc5\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n" +
                     "Found Samples for SDK API 14, revision 25 (Obsolete)\n" +
                     "Found ARM EABI System Image, Android API 42, revision 12\n" +
                     "Found MIPS System Image, Android API 42, revision 12\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        Package[] pkgs = mSource.getPackages();

        assertEquals(17, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }

        // There are no extra packages anymore in repository-6
        assertEquals("[]", Arrays.toString(extraPaths.toArray()));
        assertEquals("[]", Arrays.toString(extraVendors.toArray()));
        assertEquals("[]", Arrays.toString(extraInstall.toArray()));
        assertEquals("[]", Arrays.toString(extraFilePaths.toArray()));


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


        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));


        // Check the min-tools-rev
        ArrayList<String> minToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinToolsDependency) {
                minToolsRevs.add(p.getListDescription() + ": " +
                        ((IMinToolsDependency) p).getMinToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[SDK Platform Android Pastry Preview: 0, " +
                 "SDK Platform Android 1.1: 0, " +
                 "SDK Platform Android 1.0: 2.0.1, " +
                 "Samples for SDK API 14 (Obsolete): 5, " +
                 "Samples for SDK API 14 (Obsolete): 5.1.2 rc3]",
                Arrays.toString(minToolsRevs.toArray()));


        // Check the min-platform-tools-rev
        ArrayList<String> minPlatToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinPlatformToolsDependency) {
                minPlatToolsRevs.add(p.getListDescription() + ": " +
                  ((IMinPlatformToolsDependency) p).getMinPlatformToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[Android SDK Tools: 4, " +
                 "Android SDK Tools: 4 rc5]",
                Arrays.toString(minPlatToolsRevs.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 8
     */
    public void testLoadRepoXml_08() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_08.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(8, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(8), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found Sources for Android SDK, API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                     "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1.2.3 rc4\n" +
                     "Found Android SDK Build-tools, revision 3 rc5\n" +
                     "Found Android SDK Build-tools, revision 3.0.1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3 rc5\n" +
                     "Found Android SDK Build-tools, revision 3\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n" +
                     "Found Samples for SDK API 14, revision 25 (Obsolete)\n" +
                     "Found ARM EABI System Image, Android API 42, revision 12\n" +
                     "Found MIPS System Image, Android API 42, revision 12\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n" +
                     "Found Android SDK Build-tools, revision 12.13.14\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        Package[] pkgs = mSource.getPackages();

        assertEquals(21, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }

        // There are no extra packages anymore in repository-6
        assertEquals("[]", Arrays.toString(extraPaths.toArray()));
        assertEquals("[]", Arrays.toString(extraVendors.toArray()));
        assertEquals("[]", Arrays.toString(extraInstall.toArray()));
        assertEquals("[]", Arrays.toString(extraFilePaths.toArray()));


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


        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));


        // Check the min-tools-rev
        ArrayList<String> minToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinToolsDependency) {
                minToolsRevs.add(p.getListDescription() + ": " +
                        ((IMinToolsDependency) p).getMinToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[SDK Platform Android Pastry Preview: 0, " +
                 "SDK Platform Android 1.1: 0, " +
                 "SDK Platform Android 1.0: 2.0.1, " +
                 "Samples for SDK API 14 (Obsolete): 5, " +
                 "Samples for SDK API 14 (Obsolete): 5.1.2 rc3]",
                Arrays.toString(minToolsRevs.toArray()));


        // Check the min-platform-tools-rev
        ArrayList<String> minPlatToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinPlatformToolsDependency) {
                minPlatToolsRevs.add(p.getListDescription() + ": " +
                  ((IMinPlatformToolsDependency) p).getMinPlatformToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[Android SDK Tools: 4, " +
                 "Android SDK Tools: 4 rc5]",
                Arrays.toString(minPlatToolsRevs.toArray()));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[Android SDK Tools, " +
                "Android SDK Tools, " +
                "Android SDK Platform-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Documentation for Android SDK, " +
                "Documentation for Android SDK, " +
                "SDK Platform Android Pastry Preview, " +
                "SDK Platform Android 1.1, " +
                "SDK Platform Android 1.0, " +
                "Samples for SDK API 14 (Obsolete), " +
                "Samples for SDK API 14 (Obsolete), " +
                "ARM EABI System Image, " +
                "MIPS System Image, " +
                "ARM EABI v7a System Image, " +
                "Intel x86 Atom System Image, " +
                "Sources for Android SDK, " +
                "Sources for Android SDK, " +
                "Sources for Android SDK]",
                Arrays.toString(listDescs.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 9
     */
    public void testLoadRepoXml_09() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_09.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(9, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(9), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        // Verbose log order matches the XML order and not the sorted display order.
        assertEquals("Found SDK Platform Android 1.0, API 1, revision 3\n" +
                     "Found Documentation for Android SDK, API 1, revision 1\n" +
                     "Found Sources for Android SDK, API 1, revision 1\n" +
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Intel x86 Atom System Image, Android API 2, revision 1\n" +
                     "Found ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Custom Thing ARM EABI v7a System Image, Android API 2, revision 2\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Android SDK Tools, revision 1.2.3 rc4\n" +
                     "Found Android SDK Build-tools, revision 3 rc5\n" +
                     "Found Android SDK Build-tools, revision 3.0.1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3 rc5\n" +
                     "Found Android SDK Build-tools, revision 3\n" +
                     "Found Samples for SDK API 14, revision 24 (Obsolete)\n" +
                     "Found Samples for SDK API 14, revision 25 (Obsolete)\n" +
                     "Found Variant 1 ARM EABI System Image, Android API 42, revision 12\n" +
                     "Found Variant 1 MIPS System Image, Android API 42, revision 12\n" +
                     "Found Variant 2 MIPS System Image, Android API 42, revision 12\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n" +
                     "Found Android SDK Build-tools, revision 12.13.14\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        // Order is defined by
        // com.android.sdklib.internal.repository.packages.Package.comparisonKey()
        Package[] pkgs = mSource.getPackages();

        assertEquals(23, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }

        // There are no extra packages anymore in repository-6
        assertEquals("[]", Arrays.toString(extraPaths.toArray()));
        assertEquals("[]", Arrays.toString(extraVendors.toArray()));
        assertEquals("[]", Arrays.toString(extraInstall.toArray()));
        assertEquals("[]", Arrays.toString(extraFilePaths.toArray()));


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
                "[42 armeabi: variant-1 [Variant 1], " +
                 "42 mips: variant-1 [Variant 1], " +
                 "42 mips: variant-2 [Variant 2], " +
                 "2 armeabi-v7a: coolThing [Custom Thing], " +
                 "2 armeabi-v7a: default [Default], " +
                 "2 x86: default [Default]]",
                Arrays.toString(sysImgInfo.toArray()));


        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));


        // Check the min-tools-rev
        ArrayList<String> minToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinToolsDependency) {
                minToolsRevs.add(p.getListDescription() + ": " +
                        ((IMinToolsDependency) p).getMinToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[SDK Platform Android Pastry Preview: 0, " +
                 "SDK Platform Android 1.1: 0, " +
                 "SDK Platform Android 1.0: 2.0.1, " +
                 "Samples for SDK API 14 (Obsolete): 5, " +
                 "Samples for SDK API 14 (Obsolete): 5.1.2 rc3]",
                Arrays.toString(minToolsRevs.toArray()));


        // Check the min-platform-tools-rev
        ArrayList<String> minPlatToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinPlatformToolsDependency) {
                minPlatToolsRevs.add(p.getListDescription() + ": " +
                  ((IMinPlatformToolsDependency) p).getMinPlatformToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[Android SDK Tools: 4, " +
                 "Android SDK Tools: 4 rc5]",
                Arrays.toString(minPlatToolsRevs.toArray()));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[Android SDK Tools, " +
                "Android SDK Tools, " +
                "Android SDK Platform-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Android SDK Build-tools, " +
                "Documentation for Android SDK, " +
                "Documentation for Android SDK, " +
                "SDK Platform Android Pastry Preview, " +
                "SDK Platform Android 1.1, " +
                "SDK Platform Android 1.0, " +
                "Samples for SDK API 14 (Obsolete), " +
                "Samples for SDK API 14 (Obsolete), " +
                "Variant 1 ARM EABI System Image, " +
                "Variant 1 MIPS System Image, " +
                "Variant 2 MIPS System Image, " +
                "Custom Thing ARM EABI v7a System Image, " +
                "ARM EABI v7a System Image, " +
                "Intel x86 Atom System Image, " +
                "Sources for Android SDK, " +
                "Sources for Android SDK, " +
                "Sources for Android SDK]",
                Arrays.toString(listDescs.toArray()));
    }

    /**
     * Validate what we can load from repository in schema version 10
     */
    public void testLoadRepoXml_10() throws Exception {
        String filename = "/com/android/sdklib/testdata/repository_sample_10.xml";
        InputStream xmlStream = getTestResource(filename);
        assertNotNull("Missing test file: " + filename, xmlStream);

        // guess the version from the XML document
        int version = mSource._getXmlSchemaVersion(xmlStream);
        assertEquals(10, version);

        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        String url = "not-a-valid-url://" + SdkRepoConstants.URL_DEFAULT_FILENAME;

        String uri = mSource._validateXml(xmlStream, url, version, validationError, validatorFound);
        assertEquals(Boolean.TRUE, validatorFound[0]);
        assertEquals(null, validationError[0]);
        assertEquals(SdkRepoConstants.getSchemaUri(10), uri);

        // Validation was successful, load the document
        MockMonitor monitor = new MockMonitor();
        Document doc = mSource._getDocument(xmlStream, monitor);
        assertNotNull(doc);

        // Get the packages
        assertTrue(mSource._parsePackages(doc, uri, monitor));

        // Verbose log order matches the XML order and not the sorted display order.
        assertEquals("Found The first Android platform ever, revision 3\n" +        // list-display
                     "Found Doc for first platform, revision 1\n" +                 // list-display
                     "Found Sources for first platform, revision 1\n" +             // list-display
                     "Found SDK Platform Android 1.1, API 2, revision 12\n" +
                     "Found Sources for Android SDK, API 2, revision 2\n" +
                     "Found SDK Platform Android Pastry Preview, revision 3\n" +
                     "Found Tools in version 1.2.3.4, revision 1.2.3 rc4\n" +       // list-display
                     "Found Build tools v3 (preview 5), revision 3 rc5\n" +         // list-display
                     "Found Android SDK Build-tools, revision 3.0.1\n" +
                     "Found Documentation for Android SDK, API 2, revision 42\n" +
                     "Found Android SDK Tools, revision 42\n" +
                     "Found Android SDK Platform-tools, revision 3 rc5\n" +
                     "Found Android SDK Build-tools, revision 3\n" +
                     "Found Samples from Android 14, revision 24 (Obsolete)\n" +    // list-display
                     "Found Samples for SDK API 14, revision 25 (Obsolete)\n" +
                     "Found Sources for Android SDK, API 42, revision 12\n" +
                     "Found Android SDK Build-tools, revision 12.13.14\n",
                monitor.getCapturedVerboseLog());
        assertEquals("", monitor.getCapturedLog());
        assertEquals("", monitor.getCapturedErrorLog());

        // check the packages we found... we expected to find 13 packages with each at least
        // one archive.
        // Note the order doesn't necessary match the one from the
        // assertEquald(getCapturedVerboseLog) because packages are sorted using the
        // Packages' sorting order, e.g. all platforms are sorted by descending API level, etc.
        // Order is defined by
        // com.android.sdklib.internal.repository.packages.Package.comparisonKey()
        Package[] pkgs = mSource.getPackages();

        assertEquals(17, pkgs.length);
        for (Package p : pkgs) {
            assertTrue(p.getArchives().length >= 1);
        }

        // Check the layoutlib & included-abi of the platform packages.
        ArrayList<Pair<Integer, Integer>> layoutlibVers = new ArrayList<Pair<Integer,Integer>>();
        ArrayList<String> includedAbi = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof PlatformPackage) {
                layoutlibVers.add(((PlatformPackage) p).getLayoutlibVersion());
                String abi = ((PlatformPackage) p).getIncludedAbi();
                includedAbi.add(abi == null ? "(null)" : abi);
            }
        }
        assertEquals(
                "[Pair [first=1, second=0], " +         // platform API 5 preview
                 "Pair [first=5, second=31415], " +     // platform API 2
                 "Pair [first=5, second=0]]",           // platform API 1
                Arrays.toString(layoutlibVers.toArray()));
        assertEquals(
                "[(null), " +                           // platform API 5 preview
                 "x86, " +                              // platform API 2
                 "armeabi]",                            // platform API 1
                Arrays.toString(includedAbi.toArray()));

        // Check the extra packages path, vendor, install folder, project-files, old-paths

        final String osSdkPath = "SDK";
        final SdkManager sdkManager = new MockEmptySdkManager(osSdkPath);

        ArrayList<String> extraPaths    = new ArrayList<String>();
        ArrayList<String> extraVendors  = new ArrayList<String>();
        ArrayList<File>   extraInstall  = new ArrayList<File>();
        ArrayList<ArrayList<String>> extraFilePaths = new ArrayList<ArrayList<String>>();
        for (Package p : pkgs) {
            if (p instanceof ExtraPackage) {
                ExtraPackage ep = (ExtraPackage) p;
                // combine path and old-paths in the form "path [old_path1, old_path2]"
                extraPaths.add(ep.getPath() + " " + Arrays.toString(ep.getOldPaths()));
                extraVendors.add(ep.getVendorId() + "/" + ep.getVendorDisplay());
                extraInstall.add(ep.getInstallFolder(osSdkPath, sdkManager));

                ArrayList<String> filePaths = new ArrayList<String>();
                for (String filePath : ep.getProjectFiles()) {
                    filePaths.add(filePath);
                }
                extraFilePaths.add(filePaths);
            }
        }

        // There are no extra packages anymore in repository-6
        assertEquals("[]", Arrays.toString(extraPaths.toArray()));
        assertEquals("[]", Arrays.toString(extraVendors.toArray()));
        assertEquals("[]", Arrays.toString(extraInstall.toArray()));
        assertEquals("[]", Arrays.toString(extraFilePaths.toArray()));


        // Check the system-image packages -- there can't be any in schema 10 anymore
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
        assertEquals("[]", Arrays.toString(sysImgInfo.toArray()));


        // Check the source packages
        ArrayList<String> sourceVersion = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof SourcePackage) {
                SourcePackage sp = (SourcePackage) p;
                String v = sp.getAndroidVersion().getApiString();
                sourceVersion.add(v);
            }
        }
        assertEquals(
                "[42, 2, 1]",
                Arrays.toString(sourceVersion.toArray()));


        // Check the min-tools-rev
        ArrayList<String> minToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinToolsDependency) {
                minToolsRevs.add(p.getListDescription() + ": " +
                        ((IMinToolsDependency) p).getMinToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[SDK Platform Android Pastry Preview: 0, " +
                 "SDK Platform Android 1.1: 0, " +
                 "The first Android platform ever: 2.0.1, " +           // list-display
                 "Samples from Android 14 (Obsolete): 5, " +            // list-display
                 "Samples for SDK API 14 (Obsolete): 5.1.2 rc3]",
                Arrays.toString(minToolsRevs.toArray()));


        // Check the min-platform-tools-rev
        ArrayList<String> minPlatToolsRevs = new ArrayList<String>();
        for (Package p : pkgs) {
            if (p instanceof IMinPlatformToolsDependency) {
                minPlatToolsRevs.add(p.getListDescription() + ": " +
                  ((IMinPlatformToolsDependency) p).getMinPlatformToolsRevision().toShortString());
            }
        }
        assertEquals(
                "[Tools in version 1.2.3.4: 4, " +                      // list-display
                 "Android SDK Tools: 4 rc5]",
                Arrays.toString(minPlatToolsRevs.toArray()));

        // Check the list display of the packages
        ArrayList<String> listDescs = new ArrayList<String>();
        for (Package p : pkgs) {
            listDescs.add(p.getListDescription());
        }
        assertEquals(
                "[Tools in version 1.2.3.4, " +                         // list-display
                 "Android SDK Tools, " +
                 "Android SDK Platform-tools, " +
                 "Android SDK Build-tools, " +
                 "Android SDK Build-tools, " +
                 "Android SDK Build-tools, " +
                 "Build tools v3 (preview 5), " +                       // list-display
                 "Documentation for Android SDK, " +
                 "Doc for first platform, " +                           // list-display
                 "SDK Platform Android Pastry Preview, " +
                 "SDK Platform Android 1.1, " +
                 "The first Android platform ever, " +                  // list-display
                 "Samples from Android 14 (Obsolete), " +               // list-display
                 "Samples for SDK API 14 (Obsolete), " +
                 "Sources for Android SDK, " +
                 "Sources for Android SDK, " +
                 "Sources for first platform]",                         // list-display
                Arrays.toString(listDescs.toArray()));
    }

    /**
     * Validate there isn't a next-version we haven't tested yet
     */
    public void testLoadRepoXml_11() throws Exception {
        InputStream xmlStream = getTestResource("/com/android/sdklib/testdata/repository_sample_11.xml");
        assertNull("There is a sample for repository-11.xsd but there is not corresponding unit test", xmlStream);
    }

    // ---- helper ---

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
