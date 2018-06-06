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

package com.android.tools.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Location.Handle;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.detector.api.XmlContext;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

@SuppressWarnings("javadoc")
public class LintCliXmlParserTest extends TestCase {
    public void test() throws Exception {
        String xml =
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
                "\n" +
                "    <Button\n" +
                "        android:id=\"@+id/button2\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Button\" />\n" +
                "\n" +
                "</LinearLayout>\n";
        LintCliXmlParser parser = new LintCliXmlParser();
        File file = File.createTempFile("parsertest", ".xml");
        //noinspection IOResourceOpenedButNotSafelyClosed
        Writer fw = new BufferedWriter(new FileWriter(file));
        fw.write(xml);
        fw.close();
        LintClient client = new TestClient();
        LintDriver driver = new LintDriver(new BuiltinIssueRegistry(), client);
        Project project = Project.create(client, file.getParentFile(), file.getParentFile());
        XmlContext context = new XmlContext(driver, project, null, file, null, parser);
        Document document = parser.parseXml(context);
        assertNotNull(document);

        // Basic parsing heart beat tests
        Element linearLayout = (Element) document.getElementsByTagName("LinearLayout").item(0);
        assertNotNull(linearLayout);
        NodeList buttons = document.getElementsByTagName("Button");
        assertEquals(2, buttons.getLength());
        final String ANDROID_URI = "http://schemas.android.com/apk/res/android";
        assertEquals("wrap_content",
                linearLayout.getAttributeNS(ANDROID_URI, "layout_height"));

        // Check attribute positions
        Attr attr = linearLayout.getAttributeNodeNS(ANDROID_URI, "layout_width");
        assertNotNull(attr);
        Location location = parser.getLocation(context, attr);
        Position start = location.getStart();
        Position end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        assertEquals(2, start.getLine());
        assertEquals(xml.indexOf("android:layout_width"), start.getOffset());
        assertEquals(2, end.getLine());
        String target = "android:layout_width=\"match_parent\"";
        assertEquals(xml.indexOf(target) + target.length(), end.getOffset());

        // Check attribute name positions
        location = parser.getNameLocation(context, attr);
        start = location.getStart();
        end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        target = "android:layout_width";
        assertEquals(target, xml.substring(start.getOffset(), end.getOffset()));
        assertEquals(xml.indexOf(target) + target.length(), end.getOffset());

        // Check attribute value positions
        location = parser.getValueLocation(context, attr);
        start = location.getStart();
        end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        target = "match_parent";
        assertEquals(target, xml.substring(start.getOffset(), end.getOffset()));
        assertEquals(xml.indexOf(target) + target.length(), end.getOffset());

        // Check element positions
        Element button = (Element) buttons.item(0);
        location = parser.getLocation(context, button);
        start = location.getStart();
        end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        assertEquals(6, start.getLine());
        assertEquals(xml.indexOf("<Button"), start.getOffset());
        assertEquals(xml.indexOf("/>") + 2, end.getOffset());
        assertEquals(10, end.getLine());
        int button1End = end.getOffset();

        // Check element name positions
        location = parser.getNameLocation(context, button);
        start = location.getStart();
        end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        target = "Button";
        assertEquals(target, xml.substring(start.getOffset(), end.getOffset()));
        assertEquals(xml.indexOf(target) + target.length(), end.getOffset());

        Handle handle = parser.createLocationHandle(context, button);
        Location location2 = handle.resolve();
        assertSame(location.getFile(), location.getFile());
        assertNotNull(location2.getStart());
        assertNotNull(location2.getEnd());
        assertEquals(6, location2.getStart().getLine());
        assertEquals(10, location2.getEnd().getLine());

        Element button2 = (Element) buttons.item(1);
        location = parser.getLocation(context, button2);
        start = location.getStart();
        end = location.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        assertEquals(12, start.getLine());
        assertEquals(xml.indexOf("<Button", button1End), start.getOffset());
        assertEquals(xml.indexOf("/>", start.getOffset()) + 2, end.getOffset());
        assertEquals(16, end.getLine());

        parser.dispose(context, document);

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public void testLineEndings() throws Exception {
        // Test for http://code.google.com/p/android/issues/detail?id=22925
        String xml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                "<LinearLayout>\r\n" +
                "\r" +
                "<LinearLayout></LinearLayout>\r\n" +
                "</LinearLayout>\r\n";
        LintCliXmlParser parser = new LintCliXmlParser();
        File file = File.createTempFile("parsertest2", ".xml");
        //noinspection IOResourceOpenedButNotSafelyClosed
        Writer fw = new BufferedWriter(new FileWriter(file));
        fw.write(xml);
        fw.close();
        LintClient client = new TestClient();
        LintDriver driver = new LintDriver(new BuiltinIssueRegistry(), client);
        Project project = Project.create(client, file.getParentFile(), file.getParentFile());
        XmlContext context = new XmlContext(driver, project, null, file, null, parser);
        Document document = parser.parseXml(context);
        assertNotNull(document);

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    private static class TestClient extends LintCliClient {
        @Override
        public void report(
                @NonNull Context context,
                @NonNull Issue issue,
                @NonNull Severity severity,
                @Nullable Location location,
                @NonNull String message,
                @NonNull TextFormat format) {
            System.out.println(location + ":" + message);
        }
    }
}
