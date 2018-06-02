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

package com.android.ide.common.xml;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.security.Permission;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@SuppressWarnings("javadoc")
public class XmlPrettyPrinterTest extends TestCase {
    private void checkFormat(XmlFormatPreferences prefs,
            String xml,
            String expected, String delimiter, String startNodeName,
            String endNodeName) throws Exception {

        Document document = XmlUtils.parseDocumentSilently(xml, true);
        assertNotNull(document);
        XmlFormatStyle style = XmlFormatStyle.get(document);

        XmlPrettyPrinter printer = new XmlPrettyPrinter(prefs, style, delimiter);

        StringBuilder sb = new StringBuilder(1000);
        Node startNode = document;
        Node endNode = document;
        if (startNodeName != null) {
            startNode = findNode(document.getDocumentElement(), startNodeName);
        }
        if (endNodeName != null) {
            endNode = findNode(document.getDocumentElement(), endNodeName);
        }

        printer.prettyPrint(-1, document, startNode, endNode, sb, false/*openTagOnly*/);
        String formatted = sb.toString();
        if (!expected.equals(formatted)) {
            System.out.println(formatted);
        }
        assertEquals(expected, formatted);
    }

    private static Node findNode(Node node, String nodeName) {
        if (node.getNodeName().equals(nodeName)) {
            return node;
        }

        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            Node result = findNode(child, nodeName);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private void checkFormat(XmlFormatPreferences prefs, String xml,
            String expected, String delimiter) throws Exception {
        checkFormat(prefs, xml, expected, delimiter, null, null);
    }

    private void checkFormat(XmlFormatPreferences prefs, String xml,
            String expected) throws Exception {
        checkFormat(prefs, xml, expected, "\n"); //$NON-NLS-1$
    }
    private void checkFormat(String xml, String expected)
            throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        checkFormat(prefs, xml, expected);
    }

    public void testLayout1() throws Exception {
        checkFormat(
                "<LinearLayout><Button></Button></LinearLayout>",

                "<LinearLayout>\n" +
                "\n" +
                "    <Button />\n" +
                "\n" +
                "</LinearLayout>");
    }

    public void testLayout2() throws Exception {
        checkFormat(
                "<LinearLayout><Button foo=\"bar\"></Button></LinearLayout>",

                "<LinearLayout>\n" +
                "\n" +
                "    <Button foo=\"bar\" />\n" +
                "\n" +
                "</LinearLayout>");
    }

    public void testLayout3() throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        prefs.oneAttributeOnFirstLine = true;
        checkFormat(
                prefs,
                "<LinearLayout><Button foo=\"bar\"></Button></LinearLayout>",

                "<LinearLayout>\n" +
                "\n" +
                "    <Button foo=\"bar\" />\n" +
                "\n" +
                "</LinearLayout>");
    }

    public void testClosedElements() throws Exception {
        checkFormat(
                "<resources>\n" +
                "<item   name=\"title_container\"  type=\"id\"   />\n" +
                "<item name=\"title_logo\" type=\"id\"/>\n" +
                "</resources>\n",

                "<resources>\n" +
                "\n" +
                "    <item name=\"title_container\" type=\"id\"/>\n" +
                "    <item name=\"title_logo\" type=\"id\"/>\n" +
                "\n" +
                "</resources>");
    }

    public void testResources() throws Exception {
        checkFormat(
                "<resources><item name=\"foo\">Text value here </item></resources>",
                "<resources>\n\n" +
                "    <item name=\"foo\">Text value here </item>\n" +
                "\n</resources>");
    }

    public void testNodeTypes() throws Exception {
        // Ensures that a document with all kinds of node types is serialized correctly
        checkFormat(

                "<!--\n" +
                "/**\n" +
                " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                " */\n" +
                "-->\n" +
                "<!DOCTYPE metadata [\n" +
                "<!ELEMENT metadata (category)*>\n" +
                "<!ENTITY % ISOLat2\n" +
                "         SYSTEM \"http://www.xml.com/iso/isolat2-xml.entities\" >\n" +
                "]>\n" +
                "<LinearLayout\n" +
                "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:orientation=\"vertical\">\n" +
                "<![CDATA[\n" +
                "This is character data!\n" +
                "<!-- This is not a comment! -->\n" +
                "and <this is not an element>\n" +
                "]]>         \n" +
                "This is text: &lt; and &amp;\n" +
                "<!-- comment 1 \"test\"... -->\n" +
                "<!-- ... comment2 -->\n" +
                "%ISOLat2;        \n" +
                "<!-- \n" +
                "Type <key>less-than</key> (&#x3C;)\n" +
                "-->        \n" +
                "</LinearLayout>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!--\n" +
                "/**\n" +
                " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                " */\n" +
                "-->\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:orientation=\"vertical\" >\n" +
                "<![CDATA[\n" +
                "This is character data!\n" +
                "<!-- This is not a comment! -->\n" +
                "and <this is not an element>\n" +
                "]]>\n" +
                "This is text: &lt; and &amp;\n" +
                "    <!-- comment 1 \"test\"... -->\n" +
                "    <!-- ... comment2 -->\n" +
                "%ISOLat2;        \n" +
                "<!-- Type <key>less-than</key> (&#x3C;) -->\n" +
                "\n" +
                "</LinearLayout>");
    }

    public void testWindowsDelimiters() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<LinearLayout><Button foo=\"bar\"></Button></LinearLayout>",

                "<LinearLayout>\r\n" +
                "\r\n" +
                "    <Button foo=\"bar\" />\r\n" +
                "\r\n" +
                "</LinearLayout>",
                "\r\n");
    }

    public void testRemoveBlanklines() throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        prefs.removeEmptyLines = true;
        checkFormat(
                prefs,
                "<foo><bar><baz1></baz1><baz2></baz2></bar><bar2></bar2><bar3><baz12></baz12></bar3></foo>",

                ""
                + "<foo>\n"
                + "    <bar>\n"
                + "        <baz1 />\n"
                + "        <baz2 />\n"
                + "    </bar>\n"
                + "    <bar2 />\n"
                + "    <bar3>\n"
                + "        <baz12 />\n"
                + "    </bar3>\n"
                + "</foo>");
    }

    public void testRange() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<LinearLayout><Button foo=\"bar\"></Button><CheckBox/></LinearLayout>",
                "\n" +
                "    <Button foo=\"bar\" />\n" +
                "\n" +
                "    <CheckBox />\n",
                "\n",
                "Button", "CheckBox");
    }

    public void testOpenTagOnly() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<LinearLayout><Button foo=\"bar\"><Foo/></Button><CheckBox/></LinearLayout>",
                "\n" +
                "    <Button foo=\"bar\" >\n" +
                "\n" +
                "        <Foo />\n" +
                "    </Button>\n",
                "\n",

                "Button", "Button");
    }

    public void testRange2() throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        prefs.removeEmptyLines = true;
        checkFormat(
                prefs,
                "<foo><bar><baz1></baz1><baz2></baz2></bar><bar2></bar2><bar3><baz12></baz12></bar3></foo>",

                "        <baz1 />\n" +
                "        <baz2 />\n" +
                "    </bar>\n" +
                "    <bar2 />\n" +
                "    <bar3>\n" +
                "        <baz12 />\n",

                "\n",
                "baz1", "baz12");
    }

    public void testEOLcomments() throws Exception {
        checkFormat(
                "<selector xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                "    <item android:state_pressed=\"true\"\n" +
                "     android:color=\"#ffff0000\"/> <!-- pressed -->\n" +
                "    <item android:state_focused=\"true\"\n" +
                "     android:color=\"#ff0000ff\"/> <!-- focused -->\n" +
                "    <item android:color=\"#ff000000\"/> <!-- default -->\n" +
                "</selector>",

                ""
                + "<selector xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n"
                + "\n"
                + "    <item\n"
                + "        android:state_pressed=\"true\"\n"
                + "        android:color=\"#ffff0000\"/> <!-- pressed -->\n"
                + "    <item\n"
                + "        android:state_focused=\"true\"\n"
                + "        android:color=\"#ff0000ff\"/> <!-- focused -->\n"
                + "    <item android:color=\"#ff000000\"/> <!-- default -->\n"
                + "\n"
                + "</selector>");
    }

    public void testFormatColorList() throws Exception {
        checkFormat(
                "<selector xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                "<item android:state_activated=\"true\" android:color=\"#FFFFFF\"/>\n" +
                "<item android:color=\"#777777\" /> <!-- not selected -->\n" +
                "</selector>",

                ""
                + "<selector xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n"
                + "\n"
                + "    <item\n"
                + "        android:state_activated=\"true\"\n"
                + "        android:color=\"#FFFFFF\"/>\n"
                + "    <item android:color=\"#777777\"/> <!-- not selected -->\n"
                + "\n"
                + "</selector>");
    }

    public void testPreserveNewlineAfterComment() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources><dimen name=\"colorstrip_height\">6dip</dimen>\n" +
                "    <!-- comment1 --><dimen name=\"title_height\">45dip</dimen>\n" +
                "\n" +
                "    <!-- comment2: newline above --><dimen name=\"now_playing_height\">90dip</dimen>\n" +
                "    <dimen name=\"text_size_small\">14sp</dimen>\n" +
                "\n" +
                "\n" +
                "    <!-- comment3: newline above and below -->\n" +
                "\n" +
                "\n" +
                "\n" +
                "    <dimen name=\"text_size_medium\">18sp</dimen><dimen name=\"text_size_large\">22sp</dimen>\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <dimen name=\"colorstrip_height\">6dip</dimen>\n" +
                "    <!-- comment1 -->\n" +
                "    <dimen name=\"title_height\">45dip</dimen>\n" +
                "\n" +
                "    <!-- comment2: newline above -->\n" +
                "    <dimen name=\"now_playing_height\">90dip</dimen>\n" +
                "    <dimen name=\"text_size_small\">14sp</dimen>\n" +
                "\n" +
                "    <!-- comment3: newline above and below -->\n" +
                "\n" +
                "    <dimen name=\"text_size_medium\">18sp</dimen>\n" +
                "    <dimen name=\"text_size_large\">22sp</dimen>\n" +
                "\n" +
                "</resources>");
    }

    public void testPlurals() throws Exception {
        checkFormat(
                "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n" +
                "<string name=\"toast_sync_error\">Sync error: <xliff:g id=\"error\">%1$s</xliff:g></string>\n" +
                "<string name=\"session_subtitle\"><xliff:g id=\"time\">%1$s</xliff:g> in <xliff:g id=\"room\">%2$s</xliff:g></string>\n" +
                "<plurals name=\"now_playing_countdown\">\n" +
                "<item quantity=\"zero\"><xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "<item quantity=\"one\"><xliff:g id=\"number_of_days\">%1$s</xliff:g> day, <xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "<item quantity=\"other\"><xliff:g id=\"number_of_days\">%1$s</xliff:g> days, <xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "</plurals>\n" +
                "</resources>",

                "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n" +
                "\n" +
                "    <string name=\"toast_sync_error\">Sync error: <xliff:g id=\"error\">%1$s</xliff:g></string>\n" +
                "    <string name=\"session_subtitle\"><xliff:g id=\"time\">%1$s</xliff:g> in <xliff:g id=\"room\">%2$s</xliff:g></string>\n" +
                "\n" +
                "    <plurals name=\"now_playing_countdown\">\n" +
                "        <item quantity=\"zero\"><xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "        <item quantity=\"one\"><xliff:g id=\"number_of_days\">%1$s</xliff:g> day, <xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "        <item quantity=\"other\"><xliff:g id=\"number_of_days\">%1$s</xliff:g> days, <xliff:g id=\"remaining_time\">%2$s</xliff:g></item>\n" +
                "    </plurals>\n" +
                "\n" +
                "</resources>");
    }

    public void testMultiAttributeResource() throws Exception {
        checkFormat(
                "<resources><string name=\"debug_enable_debug_logging_label\" translatable=\"false\">Enable extra debug logging?</string></resources>",

                "<resources>\n" +
                "\n" +
                "    <string name=\"debug_enable_debug_logging_label\" translatable=\"false\">Enable extra debug logging?</string>\n" +
                "\n" +
                "</resources>");
    }

    public void testMultilineCommentAlignment() throws Exception {
        checkFormat(
                "<resources>" +
                "    <!-- Deprecated strings - Move the identifiers to this section, mark as DO NOT TRANSLATE,\n" +
                "         and remove the actual text.  These will be removed in a bulk operation. -->\n" +
                "    <!-- Do Not Translate.  Unused string. -->\n" +
                "    <string name=\"meeting_invitation\"></string>\n" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "    <!--\n" +
                "         Deprecated strings - Move the identifiers to this section, mark as DO NOT TRANSLATE,\n" +
                "         and remove the actual text.  These will be removed in a bulk operation.\n" +
                "    -->\n" +
                "    <!-- Do Not Translate.  Unused string. -->\n" +
                "    <string name=\"meeting_invitation\"></string>\n" +
                "\n" +
                "</resources>");
    }

    public void testLineCommentSpacing() throws Exception {
        checkFormat(
                "<resources>\n" +
                "\n" +
                "    <dimen name=\"colorstrip_height\">6dip</dimen>\n" +
                "    <!-- comment1 -->\n" +
                "    <dimen name=\"title_height\">45dip</dimen>\n" +
                "    <!-- comment2: no newlines -->\n" +
                "    <dimen name=\"now_playing_height\">90dip</dimen>\n" +
                "    <dimen name=\"text_size_small\">14sp</dimen>\n" +
                "\n" +
                "    <!-- comment3: newline above and below -->\n" +
                "\n" +
                "    <dimen name=\"text_size_medium\">18sp</dimen>\n" +
                "    <dimen name=\"text_size_large\">22sp</dimen>\n" +
                "\n" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "    <dimen name=\"colorstrip_height\">6dip</dimen>\n" +
                "    <!-- comment1 -->\n" +
                "    <dimen name=\"title_height\">45dip</dimen>\n" +
                "    <!-- comment2: no newlines -->\n" +
                "    <dimen name=\"now_playing_height\">90dip</dimen>\n" +
                "    <dimen name=\"text_size_small\">14sp</dimen>\n" +
                "\n" +
                "    <!-- comment3: newline above and below -->\n" +
                "\n" +
                "    <dimen name=\"text_size_medium\">18sp</dimen>\n" +
                "    <dimen name=\"text_size_large\">22sp</dimen>\n" +
                "\n" +
                "</resources>");
    }

    public void testCommentHandling() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<foo >\n" +
                "\n" +
                "    <!-- abc\n" +
                "         def\n" +
                "         ghi -->\n" +
                "\n" +
                "    <!-- abc\n" +
                "    def\n" +
                "    ghi -->\n" +
                "    \n" +
                "<!-- abc\n" +
                "def\n" +
                "ghi -->\n" +
                "\n" +
                "</foo>",

                "<foo>\n" +
                "\n" +
                "    <!--\n" +
                "         abc\n" +
                "         def\n" +
                "         ghi\n" +
                "    -->\n" +
                "\n" +
                "\n" +
                "    <!--\n" +
                "    abc\n" +
                "    def\n" +
                "    ghi\n" +
                "    -->\n" +
                "\n" +
                "\n" +
                "    <!--\n" +
                "abc\n" +
                "def\n" +
                "ghi\n" +
                "    -->\n" +
                "\n" +
                "</foo>");
    }

    public void testCommentHandling2() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<foo >\n" +
                "    <!-- multi -->\n" +
                "\n" +
                "    <bar />\n" +
                "</foo>",

                "<foo>\n" +
                "\n" +
                "    <!-- multi -->\n" +
                "\n" +
                "    <bar />\n" +
                "\n" +
                "</foo>");
    }

    public void testMenus1() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                // http://code.google.com/p/android/issues/detail?id=21383
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<menu xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n" +
                "\n" +
                "    <item\n" +
                "        android:id=\"@+id/menu_debug\"\n" +
                "        android:icon=\"@android:drawable/ic_menu_more\"\n" +
                "        android:showAsAction=\"ifRoom|withText\"\n" +
                "        android:title=\"@string/menu_debug\">\n" +
                "    \n" +
                "        <menu>\n" +
                "                <item\n" +
                "                    android:id=\"@+id/menu_debug_clearCache_memory\"\n" +
                "                    android:icon=\"@android:drawable/ic_menu_delete\"\n" +
                "                    android:showAsAction=\"ifRoom|withText\"\n" +
                "                    android:title=\"@string/menu_debug_clearCache_memory\"/>\n" +
                "    \n" +
                "                <item\n" +
                "                    android:id=\"@+id/menu_debug_clearCache_file\"\n" +
                "                    android:icon=\"@android:drawable/ic_menu_delete\"\n" +
                "                    android:showAsAction=\"ifRoom|withText\"\n" +
                "                    android:title=\"@string/menu_debug_clearCache_file\"/>\n" +
                "        </menu>\n" +
                "    </item>\n" +
                "</menu>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<menu xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n" +
                "\n" +
                "    <item\n" +
                "        android:id=\"@+id/menu_debug\"\n" +
                "        android:icon=\"@android:drawable/ic_menu_more\"\n" +
                "        android:showAsAction=\"ifRoom|withText\"\n" +
                "        android:title=\"@string/menu_debug\">\n" +
                "        <menu>\n" +
                "            <item\n" +
                "                android:id=\"@+id/menu_debug_clearCache_memory\"\n" +
                "                android:icon=\"@android:drawable/ic_menu_delete\"\n" +
                "                android:showAsAction=\"ifRoom|withText\"\n" +
                "                android:title=\"@string/menu_debug_clearCache_memory\"/>\n" +
                "            <item\n" +
                "                android:id=\"@+id/menu_debug_clearCache_file\"\n" +
                "                android:icon=\"@android:drawable/ic_menu_delete\"\n" +
                "                android:showAsAction=\"ifRoom|withText\"\n" +
                "                android:title=\"@string/menu_debug_clearCache_file\"/>\n" +
                "        </menu>\n" +
                "    </item>\n" +
                "\n" +
                "</menu>");
    }

    public void testMenus2() throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        prefs.removeEmptyLines = true;
        checkFormat(
                prefs,
                // http://code.google.com/p/android/issues/detail?id=21346
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<layer-list xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                "  <item>\n" +
                "    <shape android:shape=\"rectangle\">\n" +
                "      <stroke\n" +
                "        android:width=\"1dip\"\n" +
                "        android:color=\"@color/line_separator\"/>\n" +
                "      <solid android:color=\"@color/event_header_background\"/>\n" +
                "    </shape>\n" +
                "  </item>\n" +
                "  <item\n" +
                "    android:bottom=\"1dip\"\n" +
                "    android:top=\"1dip\">\n" +
                "    <shape android:shape=\"rectangle\">\n" +
                "      <stroke\n" +
                "        android:width=\"1dip\"\n" +
                "        android:color=\"@color/event_header_background\"/>\n" +
                "      <solid android:color=\"@color/transparent\"/>\n" +
                "    </shape>\n" +
                "  </item>\n" +
                "</layer-list>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<layer-list xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n" +
                "    <item>\n" +
                "        <shape android:shape=\"rectangle\" >\n" +
                "            <stroke\n" +
                "                android:width=\"1dip\"\n" +
                "                android:color=\"@color/line_separator\" />\n" +
                "            <solid android:color=\"@color/event_header_background\" />\n" +
                "        </shape>\n" +
                "    </item>\n" +
                "    <item\n" +
                "        android:bottom=\"1dip\"\n" +
                "        android:top=\"1dip\">\n" +
                "        <shape android:shape=\"rectangle\" >\n" +
                "            <stroke\n" +
                "                android:width=\"1dip\"\n" +
                "                android:color=\"@color/event_header_background\" />\n" +
                "            <solid android:color=\"@color/transparent\" />\n" +
                "        </shape>\n" +
                "    </item>\n" +
                "</layer-list>");
    }

    public void testMenus3() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                // http://code.google.com/p/android/issues/detail?id=21227
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<menu xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n" +
                "\n" +
                "    <item\n" +
                "        android:icon=\"@android:drawable/ic_menu_more\"\n" +
                "        android:title=\"@string/account_list_menu_more\">\n" +
                "        <menu>\n" +
                "            <item\n" +
                "                android:id=\"@+id/account_list_menu_backup_restore\"\n" +
                "                android:icon=\"@android:drawable/ic_menu_save\"\n" +
                "                android:title=\"@string/account_list_menu_backup_restore\"/>\n" +
                "        </menu>\n" +
                "    </item>\n" +
                "\n" +
                "</menu>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<menu xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n" +
                "\n" +
                "    <item\n" +
                "        android:icon=\"@android:drawable/ic_menu_more\"\n" +
                "        android:title=\"@string/account_list_menu_more\">\n" +
                "        <menu>\n" +
                "            <item\n" +
                "                android:id=\"@+id/account_list_menu_backup_restore\"\n" +
                "                android:icon=\"@android:drawable/ic_menu_save\"\n" +
                "                android:title=\"@string/account_list_menu_backup_restore\"/>\n" +
                "        </menu>\n" +
                "    </item>\n" +
                "\n" +
                "</menu>");

    }

    public void testColors1() throws Exception {
        checkFormat(
                XmlFormatPreferences.defaults(),
                "<resources>\n" +
                "  <color name=\"enrollment_error\">#99e21f14</color>\n" +
                "\n" +
                "  <color name=\"service_starting_up\">#99000000</color>\n" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "    <color name=\"enrollment_error\">#99e21f14</color>\n" +
                "    <color name=\"service_starting_up\">#99000000</color>\n" +
                "\n" +
                "</resources>");
    }

    public void testEclipseFormatStyle1() throws Exception {
        XmlFormatPreferences prefs = new XmlFormatPreferences() {
            @Override
            public String getOneIndentUnit() {
                return "\t";
            }

            @Override
            public int getTabWidth() {
                return 8;
            }
        };
        checkFormat(
                prefs,
                "<resources>\n" +
                "  <color name=\"enrollment_error\">#99e21f14</color>\n" +
                "\n" +
                "  <color name=\"service_starting_up\">#99000000</color>\n" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "\t<color name=\"enrollment_error\">#99e21f14</color>\n" +
                "\t<color name=\"service_starting_up\">#99000000</color>\n" +
                "\n" +
                "</resources>");
    }

    public void testEclipseFormatStyle2() throws Exception {
        XmlFormatPreferences prefs = new XmlFormatPreferences() {
            @Override
            public String getOneIndentUnit() {
                return "  ";
            }

            @Override
            public int getTabWidth() {
                return 2;
            }
        };
        prefs.useEclipseIndent = true;
        checkFormat(
                prefs,
                "<resources>\n" +
                "  <color name=\"enrollment_error\">#99e21f14</color>\n" +
                "\n" +
                "  <color name=\"service_starting_up\">#99000000</color>\n" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "  <color name=\"enrollment_error\">#99e21f14</color>\n" +
                "  <color name=\"service_starting_up\">#99000000</color>\n" +
                "\n" +
                "</resources>");
    }

    public void testNameSorting() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <attr format=\"integer\" name=\"no\" />\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <attr name=\"no\" format=\"integer\" />\n" +
                "\n" +
                "</resources>");
    }

    public void testStableText() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:orientation=\"vertical\">\n" +
                "    Hello World\n" +
                "\n" +
                "</LinearLayout>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:orientation=\"vertical\" >\n" +
                "    Hello World\n" +
                "\n" +
                "</LinearLayout>");
    }

    public void testResources1() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "        <string name=\"test_string\">a\n" +
                "                </string>\n" +
                "\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <string name=\"test_string\">a</string>\n" +
                "\n" +
                "</resources>");
    }

    public void testMarkup() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "<string name=\"welcome\">Welcome to <b>Android</b>!</string>" +
                "<string name=\"glob_settings_top_text\"><b>To install a 24 Clock Widget, " +
                "please <i>long press</i> in Home Screen.</b> Configure the Global Settings " +
                "here.</string>" +
                "" +
                "\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <string name=\"welcome\">Welcome to <b>Android</b>!</string>\n" +
                "    <string name=\"glob_settings_top_text\"><b>To install a 24 Clock Widget, " +
                "please <i>long press</i> in Home Screen.</b> Configure the Global Settings " +
                "here.</string>\n" +
                "\n" +
                "</resources>");
    }

    /* This test fails when run on a plain DOM; when used with the Eclipse DOM for example
       where we can get access to the original DOM, as in EclipseXmlPrettyPrinter, it works
       public void testPreserveEntities() throws Exception {
        // Ensure that entities such as &gt; in the input string are preserved in the output
        // format
        checkFormat(
                "res/values/strings.xml",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources><string name=\"untitled\">&lt;untitled2></string>\n" +
                "<string name=\"untitled2\">&lt;untitled2&gt;</string>\n" +
                "<string name=\"untitled3\">&apos;untitled3&quot;</string></resources>\n",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <string name=\"untitled\">&lt;untitled2></string>\n" +
                "    <string name=\"untitled2\">&lt;untitled2&gt;</string>\n" +
                "    <string name=\"untitled3\">&apos;untitled3&quot;</string>\n" +
                "\n" +
                "</resources>");
    }
    */

    public void testCData1() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <string name=\"foo\"><![CDATA[bar]]></string>\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <string name=\"foo\"><![CDATA[bar]]></string>\n" +
                "\n" +
                "</resources>");
    }

    public void testCData2() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <string name=\"foo1\"><![CDATA[bar1\n" +
                "bar2\n" +
                "bar3]]></string>\n" +
                "    <string name=\"foo2\"><![CDATA[bar]]></string>\n" +
                "</resources>",

                //"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <string name=\"foo1\">\n" +
                "<![CDATA[bar1\n" +
                "bar2\n" +
                "bar3]]>\n" +
                "    </string>\n" +
                "    <string name=\"foo2\"><![CDATA[bar]]></string>\n" +
                "\n" +
                "</resources>");
    }

    public void testComplexString() throws Exception {
        checkFormat(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "<string name=\"progress_completed_export_all\">The database has " +
                "<b>successfully</b> been exported into: <br /><br /><font size=\"14\">" +
                "\\\"<i>%s</i>\\\"</font></string>" +
                "</resources>",

                "<resources>\n" +
                "\n" +
                "    <string name=\"progress_completed_export_all\">The database has " +
                "<b>successfully</b> been exported into: <br /><br /><font size=\"14\">" +
                "\\\"<i>%s</i>\\\"</font></string>\n" +
                "\n" +
                "</resources>");
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

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<myroot\n"
                + "    baz=\"baz\"\n"
                + "    foo=\"bar\" >\n"
                + "\n"
                + "    <mychild />\n"
                + "\n"
                + "    <hasComment> <!-- This is my comment -->\n"
                + "    </hasComment>\n"
                + "\n"
                + "    <hasText>\n"
                + "  This is my text  \n"
                + "    </hasText>\n"
                + "\n"
                + "</myroot>",
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
        assertNotNull(doc);

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

        formatted = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "\n"
                + "    <string name=\"description_search\">Search</string>\n"
                + "    <string name=\"description_map\">Map</string>\n"
                + "    <string name=\"description_refresh\">Refresh</string>\n"
                + "    <string name=\"description_share\">Share</string>\n"
                + "\n"
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
        assertNotNull(doc);

        String formatted = XmlUtils.toXml(doc);
        assertEquals(xml, formatted);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<root>\n"
                + "\n"
                + "    <!-- ============== -->\n"
                + "    <!-- Generic styles -->\n"
                + "    <!-- ============== -->\n"
                + "\n"
                + "</root>",
                xml);
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
        assertNotNull(doc);

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

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "\n"
                + "    <!-- ============== -->\n"
                + "    <!-- Generic styles -->\n"
                + "    <!-- ============== -->\n"
                + "    <string name=\"test\">test</string>\n"
                + "\n"
                + "</resources>",
                xml);
    }


    public void testToXml4() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- ============== -->\n"
                + "<!-- Generic styles -->\n"
                + "<!-- ============== -->\n"
                + "<root/>";
        Document doc = parse(xml);
        assertNotNull(doc);

        xml = XmlUtils.toXml(doc);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- ============== --><!-- Generic styles --><!-- ============== --><root/>",
                xml);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- ============== -->\n"
                + "<!-- Generic styles -->\n"
                + "<!-- ============== -->\n"
                + "<root />\n",
                xml);
    }

    public void testToXml5() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources><attr name=\"flag_attr\">"
                + "<flag name=\"normal\" value=\"0\"/>"
                + "<flag name=\"bold\" value=\"1\"/>"
                + "<flag name=\"italic\" "
                + "value=\"2\"/></attr></resources>";

        Document doc = parse(xml);
        assertNotNull(doc);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "\n"
                + "    <attr name=\"flag_attr\">\n"
                + "        <flag name=\"normal\" value=\"0\" />\n"
                + "        <flag name=\"bold\" value=\"1\" />\n"
                + "        <flag name=\"italic\" value=\"2\" />\n"
                + "    </attr>\n"
                + "\n"
                + "</resources>",
                xml);
    }

    public void testMarkupSpacing() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
                + "\n"
                + "    <string name=\"basic_string\">basic_string</string>\n"
                + "    <string name=\"markup\">this is a <b>bold</b>  <b>string</b> </string>\n"
                + "    <string name=\"xliff_string\"><xliff:g id=\"firstName\">%1$s</xliff:g> <xliff:g id=\"lastName\">%2$s</xliff:g></string>\n"
                + "    <string name=\"styled_string\">Forgot your username or password\\?\\nVisit <b>google.com/accounts/recovery</b>.</string>\n"
                + "\n"
                + "    <plurals name=\"plurals\">\n"
                + "        <item quantity=\"one\">test2 <xliff:g xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" id=\"test3\">%s</xliff:g> test4</item>\n"
                + "    </plurals>\n"
                + "</resources>\n";

        Document doc = parse(xml);
        assertNotNull(doc);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
                + "\n"
                + "    <string name=\"basic_string\">basic_string</string>\n"
                + "    <string name=\"markup\">this is a <b>bold</b> <b>string</b></string>\n"
                + "    <string name=\"xliff_string\"><xliff:g id=\"firstName\">%1$s</xliff:g> <xliff:g id=\"lastName\">%2$s</xliff:g></string>\n"
                + "    <string name=\"styled_string\">Forgot your username or password\\?\\nVisit <b>google.com/accounts/recovery</b>.</string>\n"
                + "\n"
                + "    <plurals name=\"plurals\">\n"
                + "        <item quantity=\"one\">test2 <xliff:g id=\"test3\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\">%s</xliff:g> test4</item>\n"
                + "    </plurals>\n"
                + "\n"
                + "</resources>",

                xml);
    }

    public void testXliff() throws Exception {
        String xml = ""
                + "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
                + "    <string name=\"xliff\">Text:\n"
                + "        <xliff:g id=\"firstName\">%1$s</xliff:g></string>\n"
                + "    <string name=\"xliff2\">Name:<xliff:g id=\"firstName\"> %1$s</xliff:g></string>\n"
                + "</resources>";

        Document doc = parse(xml);
        assertNotNull(doc);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
                + "\n"
                + "    <string name=\"xliff\">Text: <xliff:g id=\"firstName\">%1$s</xliff:g></string>\n"
                + "    <string name=\"xliff2\">Name:<xliff:g id=\"firstName\"> %1$s</xliff:g></string>\n"
                + "\n"
                + "</resources>",
            xml);
    }

    public void test52887() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=52887
        String xml = ""
                + "<!--Comment-->\n"
                + "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "\t\t    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"/>\n";

        Document doc = parse(xml);
        assertNotNull(doc);

        xml = XmlPrettyPrinter.prettyPrint(doc, false);
        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- Comment -->\n"
                + "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\" />\n",
                xml);
    }

    public void testPreserveNewlines() throws Exception {
        XmlFormatPreferences prefs = XmlFormatPreferences.defaults();
        XmlFormatStyle style = XmlFormatStyle.LAYOUT;

        String before = "<LinearLayout><Button></Button></LinearLayout>\n";

        String expected =
                "<LinearLayout>\n" +
                "\n" +
                "    <Button />\n" +
                "\n" +
                "</LinearLayout>\n";

        String after = XmlPrettyPrinter.prettyPrint(before, prefs, style, "\n");
        assertEquals(expected, after);
    }

    public void testDriver1() throws Exception {
        checkDriver(""
                + "Usage: XmlPrettyPrinter <options>... <files or directories...>\n"
                + "OPTIONS:\n"
                + "--stdout\n"
                + "--removeEmptyLines\n"
                + "--noAttributeOnFirstLine\n"
                + "--noSpaceBeforeClose\n",
                "Unknown flag --nonexistentFlag\n",
                1,
                new String[]{"--nonexistentFlag"},
                null
        );
    }

    public void testDriver2() throws Exception {
        String brokenXml = "<view>\n"
                + "<notclosed>\n"
                + "</view>";
        File temp = File.createTempFile("mylayout", ".xml");
        Files.write(brokenXml, temp, Charsets.UTF_8);
        checkDriver(
                "",
                "[Fatal Error] :3:3: The element type \"notclosed\" must be terminated by "
                        + "the matching end-tag \"</notclosed>\".\n"
                        + "Could not parse $TESTFILE\n",
                1,
                new String[]{"--stdout", temp.getPath()},
                temp
        );
        //noinspection ResultOfMethodCallIgnored
        temp.delete();
    }

    public void testDriver3() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\""
                + " xmlns:tools=\"http://schemas.android.com/tools\""
                + " android:layout_width=\"match_parent\" android:layout_height=\"match_parent\""
                + " android:orientation=\"vertical\"  tools:ignore=\"HardcodedText\">"
                + "\n"
                + "        <!-- Comment -->\n"
                + "</LinearLayout>";
        File temp = File.createTempFile("mylayout", ".xml");
        Files.write(xml, temp, Charsets.UTF_8);
        checkDriver(
                "",
                "",
                0,
                new String[]{temp.getPath()},
                temp
        );

        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    android:orientation=\"vertical\"\n"
                + "    tools:ignore=\"HardcodedText\" >\n"
                + "\n"
                + "    <!-- Comment -->\n"
                + "\n"
                + "</LinearLayout>",
                Files.toString(temp, Charsets.UTF_8));
        //noinspection ResultOfMethodCallIgnored
        temp.delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testDriver4() throws Exception {
        File root = Files.createTempDir();
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\""
                + " xmlns:tools=\"http://schemas.android.com/tools\""
                + " android:layout_width=\"match_parent\" android:layout_height=\"match_parent\""
                + " android:orientation=\"vertical\"  tools:ignore=\"HardcodedText\">"
                + "\n"
                + "        <!-- Comment -->\n"
                + "</LinearLayout>";
        File file1 = new File(root, "layout1.xml");
        Files.write(xml, file1, Charsets.UTF_8);
        File dir1 = new File(root, "layout");
        dir1.mkdirs();
        File file2 = new File(dir1, "layout2.xml");
        Files.write(xml, file2, Charsets.UTF_8);

        checkDriver(
                "",
                "",
                0,
                new String[]{file1.getPath(), dir1.getPath()},
                root
        );

        String formatted = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    android:orientation=\"vertical\"\n"
                + "    tools:ignore=\"HardcodedText\" >\n"
                + "\n"
                + "    <!-- Comment -->\n"
                + "\n"
                + "</LinearLayout>";

        assertEquals(formatted, Files.toString(file1, Charsets.UTF_8));
        assertEquals(formatted, Files.toString(file2, Charsets.UTF_8));

        file1.delete();
        file2.delete();
        dir1.delete();
        root.delete();
    }

    public void testDriver5() throws Exception {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "<Button></Button>"
                + "<Button/>"
                + "</LinearLayout>";
        File temp = File.createTempFile("mylayout", ".xml");
        Files.write(xml, temp, Charsets.UTF_8);
        checkDriver(
                "",
                "",
                0,
                new String[]{temp.getPath()},
                temp
        );

        // Note limitation of command line XML parser: Can't distinguish
        // between <element></element> and <element/>

        assertEquals(""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n"
                + "\n"
                + "    <Button />\n"
                + "\n"
                + "    <Button />\n"
                + "\n"
                + "</LinearLayout>",
                Files.toString(temp, Charsets.UTF_8));
        //noinspection ResultOfMethodCallIgnored
        temp.delete();
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

    @SuppressWarnings("null")
    @NonNull
    private static Document parse(String xml) throws Exception {
        Document doc = XmlUtils.parseDocumentSilently(xml, true);
        assertNotNull(doc);
        return doc;
    }

    private static void checkDriver(String expectedOutput, String expectedError,
            int expectedExitCode, String[] args, File testFile)
            throws Exception {
        PrintStream previousOut = System.out;
        PrintStream previousErr = System.err;
        try {
            // Trap System.exit calls:
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm)
                {
                    // allow anything.
                }
                @Override
                public void checkPermission(Permission perm, Object context)
                {
                    // allow anything.
                }
                @Override
                public void checkExit(int status) {
                    throw new ExitException(status);
                }
            });

            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));
            final ByteArrayOutputStream error = new ByteArrayOutputStream();
            System.setErr(new PrintStream(error));

            int exitCode = 0xCAFEBABE; // not set
            try {
                XmlPrettyPrinter.main(args);
            } catch (ExitException e) {
                // Allow
                exitCode = e.getStatus();
            }

            String testPath = testFile == null
                    ? XmlPrettyPrinterTest.class.getName() : testFile.getPath();
            String pathName = "$TESTFILE";
            assertEquals(expectedError, error.toString().replace(testPath, pathName));
            assertEquals(expectedOutput, output.toString().replace(testPath, pathName));
            assertEquals(expectedExitCode, exitCode);
        } finally {
            // Re-enable system exit for unit test
            System.setSecurityManager(null);

            System.setOut(previousOut);
            System.setErr(previousErr);
        }
    }

    private static class ExitException extends SecurityException {
        private static final long serialVersionUID = 1L;

        private final int mStatus;

        public ExitException(int status) {
            super("Unit test");
            mStatus = status;
        }

        public int getStatus() {
            return mStatus;
        }
    }
}
