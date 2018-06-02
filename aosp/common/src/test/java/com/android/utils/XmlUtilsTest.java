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

import static com.android.SdkConstants.XMLNS;

import com.android.SdkConstants;
import com.android.annotations.Nullable;
import com.google.common.base.Charsets;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@SuppressWarnings("javadoc")
public class XmlUtilsTest extends TestCase {
    public void testlookupNamespacePrefix() throws Exception {
        // Setup
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = document.createElement("root");
        Attr attr = document.createAttributeNS(SdkConstants.XMLNS_URI,
                "xmlns:customPrefix");
        attr.setValue(SdkConstants.ANDROID_URI);
        rootElement.getAttributes().setNamedItemNS(attr);
        document.appendChild(rootElement);
        Element root = document.getDocumentElement();
        root.appendChild(document.createTextNode("    "));
        Element foo = document.createElement("foo");
        root.appendChild(foo);
        root.appendChild(document.createTextNode("    "));
        Element bar = document.createElement("bar");
        root.appendChild(bar);
        Element baz = document.createElement("baz");
        root.appendChild(baz);

        String prefix = XmlUtils.lookupNamespacePrefix(baz, SdkConstants.ANDROID_URI);
        assertEquals("customPrefix", prefix);

        prefix = XmlUtils.lookupNamespacePrefix(baz,
                "http://schemas.android.com/tools", "tools", false);
        assertEquals("tools", prefix);

        prefix = XmlUtils.lookupNamespacePrefix(baz,
                "http://schemas.android.com/apk/res/my/pkg", "app", false);
        assertEquals("app", prefix);
        assertFalse(declaresNamespace(document, "http://schemas.android.com/apk/res/my/pkg"));

        prefix = XmlUtils.lookupNamespacePrefix(baz,
                "http://schemas.android.com/apk/res/my/pkg", "app", true /*create*/);
        assertEquals("app", prefix);
        assertTrue(declaresNamespace(document, "http://schemas.android.com/apk/res/my/pkg"));
    }

    private static boolean declaresNamespace(Document document, String uri) {
        NamedNodeMap attributes = document.getDocumentElement().getAttributes();
        for (int i = 0, n = attributes.getLength(); i < n; i++) {
            Attr attribute = (Attr) attributes.item(i);
            String name = attribute.getName();
            if (name.startsWith(XMLNS) && uri.equals(attribute.getValue())) {
                return true;
            }
        }

        return false;
    }

    public void testToXmlAttributeValue() throws Exception {
        assertEquals("", XmlUtils.toXmlAttributeValue(""));
        assertEquals("foo", XmlUtils.toXmlAttributeValue("foo"));
        assertEquals("foo&lt;bar", XmlUtils.toXmlAttributeValue("foo<bar"));
        assertEquals("foo>bar", XmlUtils.toXmlAttributeValue("foo>bar"));

        assertEquals("&quot;", XmlUtils.toXmlAttributeValue("\""));
        assertEquals("&apos;", XmlUtils.toXmlAttributeValue("'"));
        assertEquals("foo&quot;b&apos;&apos;ar",
                XmlUtils.toXmlAttributeValue("foo\"b''ar"));
        assertEquals("&lt;&quot;&apos;>&amp;", XmlUtils.toXmlAttributeValue("<\"'>&"));
    }

    public void testFromXmlAttributeValue() throws Exception {
        assertEquals("", XmlUtils.fromXmlAttributeValue(""));
        assertEquals("foo", XmlUtils.fromXmlAttributeValue("foo"));
        assertEquals("foo<bar", XmlUtils.fromXmlAttributeValue("foo&lt;bar"));
        assertEquals("foo<bar<bar>foo", XmlUtils.fromXmlAttributeValue("foo&lt;bar&lt;bar&gt;foo"));
        assertEquals("foo>bar", XmlUtils.fromXmlAttributeValue("foo>bar"));

        assertEquals("\"", XmlUtils.fromXmlAttributeValue("&quot;"));
        assertEquals("'", XmlUtils.fromXmlAttributeValue("&apos;"));
        assertEquals("foo\"b''ar", XmlUtils.fromXmlAttributeValue("foo&quot;b&apos;&apos;ar"));
        assertEquals("<\"'>&", XmlUtils.fromXmlAttributeValue("&lt;&quot;&apos;>&amp;"));
    }

    public void testAppendXmlAttributeValue() throws Exception {
        StringBuilder sb = new StringBuilder();
        XmlUtils.appendXmlAttributeValue(sb, "<\"'>&");
        assertEquals("&lt;&quot;&apos;>&amp;", sb.toString());
    }

    public void testToXmlTextValue() throws Exception {
        assertEquals("&lt;\"'>&amp;", XmlUtils.toXmlTextValue("<\"'>&"));
    }

    public void testAppendXmlTextValue() throws Exception {
        StringBuilder sb = new StringBuilder();
        XmlUtils.appendXmlTextValue(sb, "<\"'>&");
        assertEquals("&lt;\"'>&amp;", sb.toString());
    }

    public void testHasChildren() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        assertFalse(XmlUtils.hasElementChildren(document));
        document.appendChild(document.createElement("A"));
        Element a = document.getDocumentElement();
        assertFalse(XmlUtils.hasElementChildren(a));
        a.appendChild(document.createTextNode("foo"));
        assertFalse(XmlUtils.hasElementChildren(a));
        Element b = document.createElement("B");
        a.appendChild(b);
        assertTrue(XmlUtils.hasElementChildren(a));
        assertFalse(XmlUtils.hasElementChildren(b));
    }

    public void testToXml() throws Exception {
        Document doc = createEmptyPlainDocument();
        assertNotNull(doc);
        Element root = doc.createElement("myroot");
        doc.appendChild(root);
        root.setAttribute("foo", "bar");
        root.setAttribute("baz", "baz");
        Element child = doc.createElement("mychild");
        root.appendChild(child);
        Element child2 = doc.createElement("hasComment");
        root.appendChild(child2);
        Node comment = doc.createComment("This is my comment");
        child2.appendChild(comment);
        Element child3 = doc.createElement("hasText");
        root.appendChild(child3);
        Node text = doc.createTextNode("  This is my text  ");
        child3.appendChild(text);

        String xml = XmlUtils.toXml(doc);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<myroot baz=\"baz\" foo=\"bar\"><mychild/><hasComment><!--This is my comment--></hasComment><hasText>  This is my text  </hasText></myroot>",
                xml);
    }

    public void testToXml2() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string \n"
                + "        name=\"description_search\">Search</string>\n"
                + "    <string \n"
                + "        name=\"description_map\">Map</string>\n"
                + "    <string\n"
                + "         name=\"description_refresh\">Refresh</string>\n"
                + "    <string \n"
                + "        name=\"description_share\">Share</string>\n"
                + "</resources>";

        Document doc = parse(xml);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string name=\"description_search\">Search</string>\n"
                + "    <string name=\"description_map\">Map</string>\n"
                + "    <string name=\"description_refresh\">Refresh</string>\n"
                + "    <string name=\"description_share\">Share</string>\n"
                + "</resources>",
                formatted);
    }

    public void testToXml3() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<root>\n"
                + "    <!-- ============== -->\n"
                + "    <!-- Generic styles -->\n"
                + "    <!-- ============== -->\n"
                + "</root>";
        Document doc = parse(xml);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(xml, formatted);
    }

    public void testToXml3b() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "  <!-- ============== -->\n"
                + "  <!-- Generic styles -->\n"
                + "         <!-- ============== -->\n"
                + " <string     name=\"test\">test</string>\n"
                + "</resources>";
        Document doc = parse(xml);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "  <!-- ============== -->\n"
                + "  <!-- Generic styles -->\n"
                + "         <!-- ============== -->\n"
                + " <string name=\"test\">test</string>\n"
                + "</resources>",
                formatted);
    }


    public void testToXml4() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- ============== -->\n"
                + "<!-- Generic styles -->\n"
                + "<!-- ============== -->\n"
                + "<root/>";
        Document doc = parse(xml);

        xml = XmlUtils.toXml(doc);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- ============== --><!-- Generic styles --><!-- ============== --><root/>",
                xml);
    }

    public void testToXml5() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<root>\n"
                + "    <!-- <&'>\" -->\n"
                + "</root>";
        Document doc = parse(xml);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(xml, formatted);
    }

    public void testToXml6() throws Exception {
        // Check CDATA
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string \n"
                + "        name=\"description_search\">Search</string>\n"
                + "    <string name=\"map_at\">At %1$s:<![CDATA[<br><b>%2$s</b>]]></string>\n"
                + "    <string name=\"map_now_playing\">Now playing:\n"
                + "<![CDATA[\n"
                + "<br><b>%1$s</b>\n"
                + "]]></string>\n"
                + "</resources>";

        Document doc = parse(xml);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <string name=\"description_search\">Search</string>\n"
                + "    <string name=\"map_at\">At %1$s:<![CDATA[<br><b>%2$s</b>]]></string>\n"
                + "    <string name=\"map_now_playing\">Now playing:\n"
                + "<![CDATA[\n"
                + "<br><b>%1$s</b>\n"
                + "]]></string>\n"
                + "</resources>",
                formatted);
    }



    @Nullable
    private static Document createEmptyPlainDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    @Nullable
    private static Document parse(String xml) throws Exception {
        if (true) {
            return XmlUtils.parseDocumentSilently(xml, true);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setExpandEntityReferences(false);
        factory.setXIncludeAware(false);
        factory.setIgnoringComments(false);
        factory.setCoalescing(false);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    public void testFormatFloatValue() throws Exception {
        assertEquals("1", XmlUtils.formatFloatAttribute(1.0f));
        assertEquals("2", XmlUtils.formatFloatAttribute(2.0f));
        assertEquals("1.50", XmlUtils.formatFloatAttribute(1.5f));
        assertEquals("1.50", XmlUtils.formatFloatAttribute(1.50f));
        assertEquals("1.51", XmlUtils.formatFloatAttribute(1.51f));
        assertEquals("1.51", XmlUtils.formatFloatAttribute(1.514542f));
        assertEquals("1.52", XmlUtils.formatFloatAttribute(1.516542f));
        assertEquals("-1.51", XmlUtils.formatFloatAttribute(-1.51f));
        assertEquals("-1", XmlUtils.formatFloatAttribute(-1f));
    }

    public void testFormatFloatValueLocale() throws Exception {
        // Ensure that the layout float values aren't affected by
        // locale settings, like using commas instead of of periods
        Locale originalDefaultLocale = Locale.getDefault();

        try {
            Locale.setDefault(Locale.FRENCH);

            // Ensure that this is a locale which uses a comma instead of a period:
            assertEquals("5,24", String.format("%.2f", 5.236f));

            // Ensure that the formatFloatAttribute is immune
            assertEquals("1.50", XmlUtils.formatFloatAttribute(1.5f));
        } finally {
            Locale.setDefault(originalDefaultLocale);
        }
    }

    public void testGetUtfReader() throws IOException {
        File file = File.createTempFile(getName(), SdkConstants.DOT_XML);

        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter writer = new OutputStreamWriter(stream, Charsets.UTF_8);
        try {
            stream.write(0xef);
            stream.write(0xbb);
            stream.write(0xbf);
            writer.write("OK");
        } finally {
            writer.close();
        }

        Reader reader = XmlUtils.getUtfReader(file);
        assertEquals('O', reader.read());
        assertEquals('K', reader.read());
        assertEquals(-1, reader.read());

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public void testStripBom() {
        assertEquals("", XmlUtils.stripBom(""));
        assertEquals("Hello", XmlUtils.stripBom("Hello"));
        assertEquals("Hello", XmlUtils.stripBom("\uFEFFHello"));
    }

    public void testParseDocument() throws Exception {
        String xml = "" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"wrap_content\"\n" +
                "    android:orientation=\"vertical\" >\n" +
                "\n" +
                "    <Button\n" +
                "        android:id=\"@+id/button1\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Button\" />\n" +
                "          some text\n" +
                "\n" +
                "</LinearLayout>\n";

        Document document = XmlUtils.parseDocument(xml, true);
        assertNotNull(document);
        assertNotNull(document.getDocumentElement());
        assertEquals("LinearLayout", document.getDocumentElement().getTagName());

        // Add BOM
        xml = '\uFEFF' + xml;
        document = XmlUtils.parseDocument(xml, true);
        assertNotNull(document);
        assertNotNull(document.getDocumentElement());
        assertEquals("LinearLayout", document.getDocumentElement().getTagName());
    }

    public void testParseUtfXmlFile() throws Exception {
        File file = File.createTempFile(getName(), SdkConstants.DOT_XML);
        String xml = "" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"wrap_content\"\n" +
                "    android:orientation=\"vertical\" >\n" +
                "\n" +
                "    <Button\n" +
                "        android:id=\"@+id/button1\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Button\" />\n" +
                "          some text\n" +
                "\n" +
                "</LinearLayout>\n";

        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter writer = new OutputStreamWriter(stream, Charsets.UTF_8);
        try {
            stream.write(0xef);
            stream.write(0xbb);
            stream.write(0xbf);
            writer.write(xml);
        } finally {
            writer.close();
        }

        Document document = XmlUtils.parseUtfXmlFile(file, true);
        assertNotNull(document);
        assertNotNull(document.getDocumentElement());
        assertEquals("LinearLayout", document.getDocumentElement().getTagName());

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
