/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.utils;

import static com.android.utils.SdkUtils.FILENAME_PREFIX;
import static com.android.utils.SdkUtils.createPathComment;
import static com.android.utils.SdkUtils.fileToUrlString;
import static com.android.utils.SdkUtils.urlToFile;

import com.android.SdkConstants;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Locale;

@SuppressWarnings("javadoc")
public class SdkUtilsTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        // TODO: Use Files.createTempDir() to avoid this.
        if (new File("/tmp/foo").isDirectory()) {
            fail("This test will fail if /tmp/foo exists and is a directory. Please remove it.");
        }
    }

    public void testEndsWithIgnoreCase() {
        assertTrue(SdkUtils.endsWithIgnoreCase("foo", "foo"));
        assertTrue(SdkUtils.endsWithIgnoreCase("foo", "Foo"));
        assertTrue(SdkUtils.endsWithIgnoreCase("foo", "foo"));
        assertTrue(SdkUtils.endsWithIgnoreCase("Barfoo", "foo"));
        assertTrue(SdkUtils.endsWithIgnoreCase("BarFoo", "foo"));
        assertTrue(SdkUtils.endsWithIgnoreCase("BarFoo", "foO"));

        assertFalse(SdkUtils.endsWithIgnoreCase("foob", "foo"));
        assertFalse(SdkUtils.endsWithIgnoreCase("foo", "fo"));
    }

    public void testStartsWithIgnoreCase() {
        assertTrue(SdkUtils.startsWithIgnoreCase("foo", "foo"));
        assertTrue(SdkUtils.startsWithIgnoreCase("foo", "Foo"));
        assertTrue(SdkUtils.startsWithIgnoreCase("foo", "foo"));
        assertTrue(SdkUtils.startsWithIgnoreCase("barfoo", "bar"));
        assertTrue(SdkUtils.startsWithIgnoreCase("BarFoo", "bar"));
        assertTrue(SdkUtils.startsWithIgnoreCase("BarFoo", "bAr"));

        assertFalse(SdkUtils.startsWithIgnoreCase("bfoo", "foo"));
        assertFalse(SdkUtils.startsWithIgnoreCase("fo", "foo"));
    }

    public void testStartsWith() {
        assertTrue(SdkUtils.startsWith("foo", 0, "foo"));
        assertTrue(SdkUtils.startsWith("foo", 0, "Foo"));
        assertTrue(SdkUtils.startsWith("Foo", 0, "foo"));
        assertTrue(SdkUtils.startsWith("aFoo", 1, "foo"));

        assertFalse(SdkUtils.startsWith("aFoo", 0, "foo"));
        assertFalse(SdkUtils.startsWith("aFoo", 2, "foo"));
    }

    public void testEndsWith() {
        assertTrue(SdkUtils.endsWith("foo", "foo"));
        assertTrue(SdkUtils.endsWith("foobar", "obar"));
        assertTrue(SdkUtils.endsWith("foobar", "bar"));
        assertTrue(SdkUtils.endsWith("foobar", "ar"));
        assertTrue(SdkUtils.endsWith("foobar", "r"));
        assertTrue(SdkUtils.endsWith("foobar", ""));

        assertTrue(SdkUtils.endsWith(new StringBuilder("foobar"), "bar"));
        assertTrue(SdkUtils.endsWith(new StringBuilder("foobar"), new StringBuffer("obar")));
        assertTrue(SdkUtils.endsWith("foobar", new StringBuffer("obar")));

        assertFalse(SdkUtils.endsWith("foo", "fo"));
        assertFalse(SdkUtils.endsWith("foobar", "Bar"));
        assertFalse(SdkUtils.endsWith("foobar", "longfoobar"));
    }

    public void testEndsWith2() {
        assertTrue(SdkUtils.endsWith("foo", "foo".length(), "foo"));
        assertTrue(SdkUtils.endsWith("foo", "fo".length(), "fo"));
        assertTrue(SdkUtils.endsWith("foo", "f".length(), "f"));
    }

    public void testStripWhitespace() {
        assertEquals("foo", SdkUtils.stripWhitespace("foo"));
        assertEquals("foobar", SdkUtils.stripWhitespace("foo bar"));
        assertEquals("foobar", SdkUtils.stripWhitespace("  foo bar  \n\t"));
    }

    public void testWrap() {
        String s =
            "Hardcoding text attributes directly in layout files is bad for several reasons:\n" +
            "\n" +
            "* When creating configuration variations (for example for landscape or portrait)" +
            "you have to repeat the actual text (and keep it up to date when making changes)\n" +
            "\n" +
            "* The application cannot be translated to other languages by just adding new " +
            "translations for existing string resources.";
        String wrapped = SdkUtils.wrap(s, 70, "");
        assertEquals(
            "Hardcoding text attributes directly in layout files is bad for several\n" +
            "reasons:\n" +
            "\n" +
            "* When creating configuration variations (for example for landscape or\n" +
            "portrait)you have to repeat the actual text (and keep it up to date\n" +
            "when making changes)\n" +
            "\n" +
            "* The application cannot be translated to other languages by just\n" +
            "adding new translations for existing string resources.\n",
            wrapped);
    }

    public void testWrapPrefix() {
        String s =
            "Hardcoding text attributes directly in layout files is bad for several reasons:\n" +
            "\n" +
            "* When creating configuration variations (for example for landscape or portrait)" +
            "you have to repeat the actual text (and keep it up to date when making changes)\n" +
            "\n" +
            "* The application cannot be translated to other languages by just adding new " +
            "translations for existing string resources.";
        String wrapped = SdkUtils.wrap(s, 70, "    ");
        assertEquals(
            "Hardcoding text attributes directly in layout files is bad for several\n" +
            "    reasons:\n" +
            "    \n" +
            "    * When creating configuration variations (for example for\n" +
            "    landscape or portrait)you have to repeat the actual text (and keep\n" +
            "    it up to date when making changes)\n" +
            "    \n" +
            "    * The application cannot be translated to other languages by just\n" +
            "    adding new translations for existing string resources.\n",
            wrapped);
    }

    public void testParseInt() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals(1000, SdkUtils.parseLocalizedInt("1000"));
        assertEquals(0, SdkUtils.parseLocalizedInt("0"));
        assertEquals(0, SdkUtils.parseLocalizedInt(""));
        assertEquals(1, SdkUtils.parseLocalizedInt("1"));
        assertEquals(-1, SdkUtils.parseLocalizedInt("-1"));
        assertEquals(1000, SdkUtils.parseLocalizedInt("1,000"));
        assertEquals(1000000, SdkUtils.parseLocalizedInt("1,000,000"));

        Locale.setDefault(Locale.ITALIAN);
        assertSame(Locale.ITALIAN, Locale.getDefault());
        assertEquals(1000, SdkUtils.parseLocalizedInt("1000"));
        assertEquals(0, SdkUtils.parseLocalizedInt("0"));
        assertEquals(1, SdkUtils.parseLocalizedInt("1"));
        assertEquals(-1, SdkUtils.parseLocalizedInt("-1"));
        assertEquals(1000, SdkUtils.parseLocalizedInt("1.000"));
        assertEquals(1000000, SdkUtils.parseLocalizedInt("1.000.000"));

        // Make sure it throws exceptions
        try {
            SdkUtils.parseLocalizedInt("X");
            fail("Should have thrown exception");
        } catch (ParseException e) {
            // Expected
        }
    }

    public void testParseIntWithDefault() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals(1000, SdkUtils.parseLocalizedInt("1000", 0)); // Valid
        assertEquals(2, SdkUtils.parseLocalizedInt("2.X", 2)); // Parses prefix
        assertEquals(5, SdkUtils.parseLocalizedInt("X", 5)); // Parses prefix

        Locale.setDefault(Locale.ITALIAN);
        assertEquals(1000, SdkUtils.parseLocalizedInt("1000", -1)); // Valid
        assertEquals(7, SdkUtils.parseLocalizedInt("X", 7));
    }

    public void testParseDouble() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1000"));
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1000.0"));
        assertEquals(1000.5, SdkUtils.parseLocalizedDouble("1000.5"));
        assertEquals(0.0, SdkUtils.parseLocalizedDouble("0"));
        assertEquals(0.0, SdkUtils.parseLocalizedDouble(""));
        assertEquals(1.0, SdkUtils.parseLocalizedDouble("1"));
        assertEquals(-1.0, SdkUtils.parseLocalizedDouble("-1"));
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1,000"));
        assertEquals(1000.5, SdkUtils.parseLocalizedDouble("1,000.5"));
        assertEquals(1000000.0, SdkUtils.parseLocalizedDouble("1,000,000"));
        assertEquals(1000000.5, SdkUtils.parseLocalizedDouble("1,000,000.5"));

        Locale.setDefault(Locale.ITALIAN);
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1000"));
        assertEquals(1000.5, SdkUtils.parseLocalizedDouble("1000,5"));
        assertEquals(0.0, SdkUtils.parseLocalizedDouble("0"));
        assertEquals(1.0, SdkUtils.parseLocalizedDouble("1"));
        assertEquals(-1.0, SdkUtils.parseLocalizedDouble("-1"));
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1.000"));
        assertEquals(1000.5, SdkUtils.parseLocalizedDouble("1.000,5"));
        assertEquals(1000000.0, SdkUtils.parseLocalizedDouble("1.000.000"));
        assertEquals(1000000.5, SdkUtils.parseLocalizedDouble("1.000.000,5"));

        // Make sure it throws exceptions
        try {
            SdkUtils.parseLocalizedDouble("X");
            fail("Should have thrown exception");
        } catch (ParseException e) {
            // Expected
        }
    }

    public void testParseDoubleWithDefault() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1000", 0)); // Valid
        assertEquals(2.0, SdkUtils.parseLocalizedDouble("2x", 3)); // Uses prefix
        assertEquals(0.0, SdkUtils.parseLocalizedDouble("", 4));
        assertEquals(5.0, SdkUtils.parseLocalizedDouble("test", 5)); // Invalid

        Locale.setDefault(Locale.FRANCE);
        assertEquals(1000.0, SdkUtils.parseLocalizedDouble("1000", -1)); // Valid
        assertEquals(0.0, SdkUtils.parseLocalizedDouble("", 8));
    }

    public void testFileToUrl() throws Exception {
        // path -- drive "C:" used as prefix in paths, empty for mac/linux.
        String pDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "C:" : "";
        // url -- drive becomes "/C:" when used in URLs, empty for mac/linux.
        String uDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "/C:" : "";

        assertEquals(
                "file:" + uDrive + "/tmp/foo/bar",
                fileToUrlString(new File(pDrive + "/tmp/foo/bar")));
        assertEquals(
                "file:" + uDrive + "/tmp/$&+,:;=%3F@/foo%20bar%25",
                fileToUrlString(new File(pDrive + "/tmp/$&+,:;=?@/foo bar%")));
    }

    public void testUrlToFile() throws Exception {
        // path -- drive "C:" used as prefix in paths, empty for mac/linux.
        String pDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "C:" : "";
        // url -- drive becomes "/C:" when used in URLs, empty for mac/linux.
        String uDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "/C:" : "";

        assertEquals(
                new File(pDrive + "/tmp/foo/bar"),
                urlToFile("file:" + uDrive + "/tmp/foo/bar"));
        assertEquals(
                new File(pDrive + "/tmp/$&+,:;=?@/foo bar%"),
                urlToFile("file:" + uDrive + "/tmp/$&+,:;=%3F@/foo%20bar%25"));

        assertEquals(
                new File(pDrive + "/tmp/foo/bar"),
                urlToFile(new URL("file:" + uDrive + "/tmp/foo/bar")));
        assertEquals(
                new File(pDrive + "/tmp/$&+,:;=?@/foo bar%"),
                urlToFile(new URL("file:" + uDrive + "/tmp/$&+,:;=%3F@/foo%20bar%25")));
    }

    public void testCreatePathComment() throws Exception {
        // path -- drive "C:" used as prefix in paths, empty for mac/linux.
        String pDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "C:" : "";
        // url -- drive becomes "/C:" when used in URLs, empty for mac/linux.
        String uDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "/C:" : "";

        assertEquals(
                "From: file:" + uDrive + "/tmp/foo",
                createPathComment(new File(pDrive + "/tmp/foo"), false));
        assertEquals(
                " From: file:" + uDrive + "/tmp/foo ",
                createPathComment(new File(pDrive + "/tmp/foo"), true));
        assertEquals(
                "From: file:" + uDrive + "/tmp-/%2D%2D/a%2D%2Da/foo",
                createPathComment(new File(pDrive + "/tmp-/--/a--a/foo"), false));

        String path = "/tmp/foo";
        String urlString =
                createPathComment(new File(pDrive + path), false).substring(5); // 5: "From:".length()
        assertEquals(
                (pDrive + path).replace('/', File.separatorChar),
                urlToFile(new URL(urlString)).getPath());

        path = "/tmp-/--/a--a/foo";
        urlString = createPathComment(new File(pDrive + path), false).substring(5);
        assertEquals(
                (pDrive + path).replace('/', File.separatorChar),
                urlToFile(new URL(urlString)).getPath());

        // Make sure we handle file://path too, not just file:path
        urlString = "file:///tmp-/%2D%2D/a%2D%2Da/foo";
        assertEquals(
                path.replace('/', File.separatorChar),
                urlToFile(new URL(urlString)).getPath());
    }

    public void testFormattedComment() throws Exception {
        // path -- drive "C:" used as prefix in paths, empty for mac/linux.
        String pDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "C:" : "";
        // url -- drive becomes "/C:" when used in URLs, empty for mac/linux.
        String uDrive = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS ? "/C:" : "";

        Document document = XmlUtils.parseDocumentSilently("<root/>", true);
        assertNotNull(document);

        // Many invalid characters in XML, such as -- and <, and characters invalid in URLs, such
        // as spaces
        String path = pDrive + "/My Program Files/--/Q&A/X<Y/foo";
        String comment = createPathComment(new File(path), true);
        Element root = document.getDocumentElement();
        assertNotNull(root);
        root.appendChild(document.createComment(comment));
        String xml = XmlUtils.toXml(document);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<root>"
                + "<!-- From: file:" + uDrive + "/My%20Program%20Files/%2D%2D/Q&A/X%3CY/foo -->"
                + "</root>",
                xml);
        int index = xml.indexOf(FILENAME_PREFIX);
        assertTrue(index != -1);
        String urlString = xml.substring(index + FILENAME_PREFIX.length(),
                xml.indexOf("-->")).trim();
        assertEquals(
                path.replace('/', File.separatorChar),
                urlToFile(new URL(urlString)).getPath());
    }

    public void testCopyXmlWithSourceReference() throws IOException {
        File source = File.createTempFile("source", SdkConstants.DOT_XML);
        File dest = File.createTempFile("dest", SdkConstants.DOT_XML);
        Files.write(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string name=\"description_search\">Search</string>\n"
                + "    <string name=\"description_map\">Map</string>\n"
                + "    <string name=\"description_refresh\">Refresh</string>\n"
                + "    <string name=\"description_share\">Share</string>\n"
                + "</resources>",
                source, Charsets.UTF_8);
        SdkUtils.copyXmlWithSourceReference(source, dest);

        String sourceUrl = SdkUtils.fileToUrlString(source);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string name=\"description_search\">Search</string>\n"
                + "    <string name=\"description_map\">Map</string>\n"
                + "    <string name=\"description_refresh\">Refresh</string>\n"
                + "    <string name=\"description_share\">Share</string>\n"
                + "</resources><!-- From: " + sourceUrl + " -->",
                Files.toString(dest, Charsets.UTF_8));
        boolean deleted = source.delete();
        assertTrue(deleted);
        deleted = dest.delete();
        assertTrue(deleted);
    }

    public void testNameConversionRoutines() {
        assertEquals("xml-name", SdkUtils.constantNameToXmlName("XML_NAME"));
        assertEquals("XML_NAME", SdkUtils.xmlNameToConstantName("xml-name"));
        assertEquals("xmlName", SdkUtils.constantNameToCamelCase("XML_NAME"));
        assertEquals("XML_NAME", SdkUtils.camelCaseToConstantName("xmlName"));
    }

    public void testGetResourceFieldName() {
        assertEquals("", SdkUtils.getResourceFieldName(""));
        assertEquals("foo", SdkUtils.getResourceFieldName("foo"));
        assertEquals("Theme_Light", SdkUtils.getResourceFieldName("Theme.Light"));
        assertEquals("Theme_Light", SdkUtils.getResourceFieldName("Theme.Light"));
        assertEquals("abc____", SdkUtils.getResourceFieldName("abc:-._"));
    }
}
