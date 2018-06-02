/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.ide.common.blame.parser;

import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.PLATFORM_WINDOWS;
import static com.android.SdkConstants.currentPlatform;
import static com.android.utils.SdkUtils.createPathComment;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.android.ide.common.blame.parser.aapt.AaptOutputParser;
import com.android.ide.common.blame.parser.aapt.AbstractAaptOutputParser;
import com.android.utils.StdLogger;
import com.android.utils.StringHelper;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Tests for {@link ToolOutputParser}.
 */
public class AaptOutputParserTest extends TestCase {
    private File sourceFile;

    private String sourceFilePath;

    private ToolOutputParser parser;

    @Nullable
    private static String getSystemIndependentSourcePath(@NonNull Message message) {
        String sourcePath = message.getSourcePath();
        return sourcePath == null ? null : sourcePath.replace('\\', '/');
    }

    private static boolean setupSdkHome() {
        AbstractAaptOutputParser.ourRootDir = new File(".");
        return true;
    }

    @NonNull
    private static String toString(@NonNull List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = messages.size(); i < n; i++) {
            Message message = messages.get(i);
            sb.append(Integer.toString(i)).append(':').append(' ');
            sb.append(
                    StringHelper.capitalize(message.getKind().toString().toLowerCase(Locale.US)))
                    .append(':'); // INFO => Info
            sb.append(message.getText());
            if (!message.getSourceFilePositions().isEmpty() &&
                    !message.getSourceFilePositions().get(0).getPosition().equals(
                            SourcePosition.UNKNOWN)) {
                sb.append('\n');
                sb.append('\t');
                sb.append(message.getSourceFilePositions().get(0).toString());
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        parser = new ToolOutputParser(new AaptOutputParser(),
                new StdLogger(StdLogger.Level.VERBOSE));
    }

    @Override
    public void tearDown() throws Exception {
        if (sourceFile != null) {
            sourceFile.delete();
        }
        super.tearDown();
    }

    public void testParseDisplayingUnhandledMessages() {
        String output = " **--- HELLO WORLD ---**";
        List<Message> gradleMessages = parser.parseToolOutput(output);
        assertEquals(1, gradleMessages.size());
        Message message = gradleMessages.get(0);
        assertEquals(output, message.getText());
        assertEquals(Message.Kind.SIMPLE, message.getKind());
    }

    public void testParseAaptOutputWithRange1() throws IOException {
        createTempXmlFile();
        writeToFile("<manifest xmlns:android='http://schemas.android.com/apk/res/android'",
                "    android:versionCode='12' android:versionName='2.0' package='com.android.tests.basic'>",
                "  <uses-sdk android:minSdkVersion='16' android:targetSdkVersion='16'/>",
                "  <application android:icon='@drawable/icon' android:label='@string/app_name2'>");
        String messageText = "No resource found that matches the given name (at 'label' with value "
                + "'@string/app_name2').";
        String err = sourceFilePath + ":4: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 4, 61);
    }

    public void testParseAaptOutputWithRange2() throws IOException {
        // Check that when the actual aapt error occurs on a line later than the original error line,
        // the forward search which looks for a value match does not stop on an earlier line that
        // happens to have the same value prefix
        createTempXmlFile();
        writeToFile("<manifest xmlns:android='http://schemas.android.com/apk/res/android'",
                "    android:versionCode='12' android:versionName='2.0' package='com.android.tests.basic'>",
                "  <uses-sdk android:minSdkVersion='16' android:targetSdkVersion='16'/>",
                "  <application android:icon='@drawable/icon' android:label=",
                "      '@string/app_name2'>");
        String messageText = "No resource found that matches the given name (at 'label' with value "
                + "'@string/app_name2').";
        String err = sourceFilePath + ":4: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 5, 8);
    }

    public void testParseAaptOutputWithRange3() throws IOException {
        // Check that when we have a duplicate resource error, we highlight both the original property
        // and the original definition.
        // This tests the second, duplicate declaration ration.
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <style name='repeatedStyle1'>",
                "    <item name='android:gravity'>left</item>",
                "  </style>",
                "  <style name='repeatedStyle1'>",
                "    <item name='android:gravity'>left</item>");
        String messageText = "Resource entry repeatedStyle1 already has bag item android:gravity.";
        String err = sourceFilePath + ":6: error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 6, 17);
    }

    public void testParseAaptOutputWithRange4() throws IOException {
        // Check that when we have a duplicate resource error, we highlight both the original property
        // and the original definition.
        // This tests the original definition. Note that we don't have enough position info so we simply
        // highlight the whitespace portion of the line.
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <style name='repeatedStyle1'>",
                "    <item name='android:gravity'>left</item>");
        String messageText = "Originally defined here.";
        String err = sourceFilePath + ":3: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 3, 5);
    }

    public void testParseAaptOutputWithRange5() throws IOException {
        // Check for aapt error which occurs when the attribute name in an item style declaration is
        // non-existent.
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <style name='wrongAttribute'>",
                "    <item name='nonexistent'>left</item>");
        String messageText = "No resource found that matches the given name: attr 'nonexistent'.";
        String err = sourceFilePath + ":3: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 3, 17);
    }

    public void testParseAaptOutputWithRange6() throws IOException {
        // Test missing resource name.
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <style>");
        String messageText = "A 'name' attribute is required for <style>";
        String err = sourceFilePath + ":2: error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 2, 3);
    }

    public void testParseAaptOutputWithRange7() throws IOException {
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <item>");
        String messageText = "A 'type' attribute is required for <item>";
        String err = sourceFilePath + ":2: error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 2, 3);
    }

    public void testParseAaptOutputWithRange8() throws IOException {
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <item>");
        String messageText = "A 'name' attribute is required for <item>";
        String err = sourceFilePath + ":2: error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 2, 3);
    }

    public void testParseAaptOutputWithRange9() throws IOException {
        createTempXmlFile();
        writeToFile("<resources xmlns:android='http://schemas.android.com/apk/res/android'>",
                "  <style name='test'>",
                "        <item name='android:layout_width'></item>");
        String messageText = "String types not allowed (at 'android:layout_width' with value '').";
        String err = sourceFilePath + ":3: error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 3, 21);
    }

    public void testParseAaptOutputWithRange10() throws IOException {
        createTempXmlFile();
        writeToFile("<FrameLayout",
                "    xmlns:android='http://schemas.android.com/apk/res/android'",
                "    android:layout_width='wrap_content'",
                "    android:layout_height='match_parent'>",
                "    <TextView",
                "        android:layout_width='fill_parent'",
                "        android:layout_height='wrap_content'",
                "        android:layout_marginTop=''",
                "        android:layout_marginLeft=''");
        String messageText = "String types not allowed (at 'layout_marginTop' with value '').";
        String err = sourceFilePath + ":5: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 8, 34);
    }

    public void testParseAaptOutputWithRange11() throws IOException {
        createTempXmlFile();
        writeToFile("<FrameLayout",
                "    xmlns:android='http://schemas.android.com/apk/res/android'",
                "    android:layout_width='wrap_content'",
                "    android:layout_height='match_parent'>",
                "    <TextView",
                "        android:layout_width='fill_parent'",
                "        android:layout_height='wrap_content'",
                "        android:layout_marginTop=''",
                "        android:layout_marginLeft=''");
        String messageText = "String types not allowed (at 'layout_marginLeft' with value '').";
        String err = sourceFilePath + ":5: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 9, 35);
    }

    public void testParseAaptOutputWithRange12() throws IOException {
        createTempXmlFile();
        writeToFile("<FrameLayout",
                "    xmlns:android='http://schemas.android.com/apk/res/android'",
                "    android:layout_width='wrap_content'",
                "    android:layout_height='match_parent'>",
                "    <TextView",
                "        android:id=''");
        String messageText = "String types not allowed (at 'id' with value '').";
        String err = sourceFilePath + ":5: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertHasCorrectErrorMessage(messages, messageText, 6, 20);
    }

    private void createTempXmlFile() throws IOException {
        createTempFile(DOT_XML);
    }

    private void createTempFile(@NonNull String fileExtension) throws IOException {
        sourceFile = File.createTempFile(AaptOutputParserTest.class.getName(), fileExtension);
        sourceFilePath = sourceFile.getAbsolutePath();
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private void writeToFile(@NonNull String... lines) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(sourceFile));
            for (String line : lines) {
                out.write(line);
                out.newLine();
            }
        } finally {
            Closeables.close(out, true /* swallowIOException */);
        }
    }

    /**
     * Assert the error message is correct.
     *
     * @param messages       a collection of Messages
     * @param expectedText   the text the single gradle message should have.
     * @param expectedLine   the 1-based line
     * @param expectedColumn the 1-based column.
     */
    private void assertHasCorrectErrorMessage(@NonNull Collection<Message> messages,
            @NonNull String expectedText,
            int expectedLine,
            int expectedColumn) {
        assertEquals("[message count]", 1, messages.size());
        Message message = messages.iterator().next();
        assertEquals("[source file position count]", 1, message.getSourceFilePositions().size());
        SourceFilePosition position = message.getSourceFilePositions().get(0);
        assertEquals("[file path]", sourceFilePath, position.getFile().toString());
        assertEquals("[message severity]", Message.Kind.ERROR, message.getKind());
        assertEquals("[message text]", expectedText, message.getText());
        assertEquals("[position line]", expectedLine, position.getPosition().getStartLine() + 1);
        assertEquals("[position column]", expectedColumn, position.getPosition().getStartColumn() + 1);
    }

    public void testRedirectValueLinksOutput() throws Exception {
        if (!setupSdkHome()) {
            System.out.println(
                    "Skipping testRedirectValueLinksOutput because sdk-common was not found");
            return;
        }

        // Need file to be named (exactly) values.xml
        File tempDir = Files.createTempDir();
        File valueDir = new File(tempDir, "values-en");
        valueDir.mkdirs();
        sourceFile = new File(valueDir,
                "values.xml"); // Keep in sync with MergedResourceWriter.FN_VALUES_XML
        sourceFilePath = sourceFile.getAbsolutePath();

        writeToFile(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<resources xmlns:ns1=\"urn:oasis:names:tc:xliff:document:1.2\">\n" +
                        "\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseSet/values/values.xml -->\n"
                        +
                        "    <string-array name=\"string_array\" translatable=\"false\">\n" +
                        "        <item/> <!-- 0 -->\n" +
                        "        <item/> <!-- 1 -->\n" +
                        "        <item>ABC</item> <!-- 2 -->\n" +
                        "        <item>DEF</item> <!-- 3 -->\n" +
                        "        <item>GHI</item> <!-- 4 -->\n" +
                        "        <item>JKL</item> <!-- 5 -->\n" +
                        "        <item>MNO</item> <!-- 6 -->\n" +
                        "        <item>PQRS</item> <!-- 7 -->\n" +
                        "        <item>TUV</item> <!-- 8 -->\n" +
                        "        <item>WXYZ</item> <!-- 9 -->\n" +
                        "    </string-array>\n" +
                        "\n" +
                        "    <attr name=\"dimen_attr\" format=\"dimension\" />\n" +
                        "    <attr name=\"enum_attr\">\n" +
                        "        <enum name=\"normal\" value=\"0\" />\n" +
                        "        <enum name=\"sans\" value=\"1\" />\n" +
                        "        <enum name=\"serif\" value=\"2\" />\n" +
                        "        <enum name=\"monospace\" value=\"3\" />\n" +
                        "    </attr>\n" +
                        "    <attr name=\"flag_attr\">\n" +
                        "        <flag name=\"normal\" value=\"0\" />\n" +
                        "        <flag name=\"bold\" value=\"1\" />\n" +
                        "        <flag name=\"italic\" value=\"2\" />\n" +
                        "    </attr>\n" +
                        "    <attr name=\"string_attr\" format=\"string\" />\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseMerge/overlay/values/values.xml -->\n"
                        +
                        "    <color name=\"color\">#FFFFFFFF</color>\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseSet/values/values.xml -->\n"
                        +
                        "    <declare-styleable name=\"declare_styleable\">\n" +
                        "\n" +
                        "        <!-- ============== -->\n" +
                        "        <!-- Generic styles -->\n" +
                        "        <!-- ============== -->\n" +
                        "        <eat-comment />\n" +
                        "\n" +
                        "        <!-- Default color of foreground imagery. -->\n" +
                        "        <attr name=\"blah\" format=\"color\" />\n" +
                        "        <!-- Default color of foreground imagery on an inverted background. -->\n"
                        +
                        "        <attr name=\"android:colorForegroundInverse\" />\n" +
                        "    </declare-styleable>\n" +
                        "\n" +
                        "    <dimen name=\"dimen\">164dp</dimen>\n" +
                        "\n" +
                        "    <drawable name=\"color_drawable\">#ffffffff</drawable>\n" +
                        "    <drawable name=\"drawable_ref\">@drawable/stat_notify_sync_anim0</drawable>\n"
                        +
                        "\n" +
                        "    <item name=\"item_id\" type=\"id\"/>\n" +
                        "\n" +
                        "    <integer name=\"integer\">75</integer>\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseMerge/overlay/values/values.xml -->\n"
                        +
                        "    <item name=\"file_replaced_by_alias\" type=\"layout\">@layout/ref</item>\n"
                        +
                        "    <!-- From: src/test/resources/testData/resources/baseSet/values/values.xml -->\n"
                        +
                        "    <item name=\"layout_ref\" type=\"layout\">@layout/ref</item>\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseMerge/overlay/values/values.xml -->\n"
                        +
                        "    <string name=\"basic_string\">overlay_string</string>\n" +
                        "    <!-- From: src/test/resources/testData/resources/baseSet/values/values.xml -->\n"
                        +
                        "    <string name=\"styled_string\">Forgot your username or password\\?\\nVisit <b>google.com/accounts/recovery</b>.</string>\n"
                        +
                        "    <string name=\"xliff_string\"><ns1:g id=\"number\" example=\"123\">%1$s</ns1:g><ns1:g id=\"unit\" example=\"KB\">%2$s</ns1:g></string>\n"
                        +
                        "\n" +
                        "    <style name=\"style\" parent=\"@android:style/Holo.Light\">\n" +
                        "        <item name=\"android:singleLine\">true</item>\n" +
                        "        <item name=\"android:textAppearance\">@style/TextAppearance.WindowTitle</item>\n"
                        +
                        "        <item name=\"android:shadowColor\">#BB000000</item>\n" +
                        "        <item name=\"android:shadowRadius\">2.75</item>\n" +
                        "        <item name=\"foo\">foo</item>\n" +
                        "    </style>\n" +
                        "\n" +
                        "</resources>\n");

        String messageText
                = "String types not allowed (at 'drawable_ref' with value '@drawable/stat_notify_sync_anim0').";
        String err = sourceFilePath + ":46: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertEquals(1, messages.size());

        assertEquals("[message count]", 1, messages.size());
        Message message = messages.iterator().next();

        assertNotNull(message);

        // NOT sourceFilePath; should be translated back from source comment
        assertEquals(new File ("src/test/resources/testData/resources/baseSet/values/values.xml").getAbsolutePath(),
                     getSystemIndependentSourcePath(message));

        assertEquals("[message severity]", Message.Kind.ERROR, message.getKind());
        assertEquals("[message text]", messageText, message.getText());
        assertEquals(1, message.getSourceFilePositions().size());
        SourcePosition pos = message.getSourceFilePositions().get(0).getPosition();
        assertEquals("[position line]", 9, pos.getStartLine() + 1);
        assertEquals("[position column]", 35, pos.getStartColumn() + 1);
    }

    public void testRedirectFileLinksOutput() throws Exception {
        if (!setupSdkHome()) {
            System.out.println(
                    "Skipping testRedirectFileLinksOutput because sdk-common was not found");
            return;
        }

        // Need file to be named (exactly) values.xml
        File tempDir = Files.createTempDir();
        File layoutDir = new File(tempDir, "layout-land");
        layoutDir.mkdirs();
        sourceFile = new File(layoutDir, "main.xml");
        sourceFilePath = sourceFile.getAbsolutePath();

        writeToFile(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        +
                        "    android:orientation=\"vertical\"\n" +
                        "    android:layout_width=\"fill_parent\"\n" +
                        "    android:layout_height=\"fill_parent\"\n" +
                        "    >\n" +
                        "<TextView\n" +
                        "    android:layout_width=\"fill_parent\"\n" +
                        "    android:layout_height=\"wrap_content\"\n" +
                        "    android:text=\"Test App - Basic\"\n" +
                        "    android:id=\"@+id/text\"\n" +
                        "    />\n" +
                        "</LinearLayout>\n" +
                        "\n" +
                        "<!-- From: file:src/test/resources/testData/resources/incMergeData/filesVsValues/main/layout/main.xml -->");

        String messageText = "Random error message here";
        String err = sourceFilePath + ":4: error: Error: " + messageText;
        Collection<Message> messages = parser.parseToolOutput(err);
        assertEquals("[message count]", 1, messages.size());
        Message message = messages.iterator().next();
        assertNotNull(message);

        // NOT sourceFilePath; should be translated back from source comment
        String expected = new File("src/test/resources/testData/resources/incMergeData/filesVsValues/main/layout/main.xml")
          .getAbsolutePath();
        assertEquals("[file path]", expected, getSystemIndependentSourcePath(message));
        assertEquals("[message severity]", Message.Kind.ERROR, message.getKind());
        assertEquals("[message text]", messageText, message.getText());
        assertEquals("[position line]", 4, message.getSourceFilePositions().get(0).getPosition().getStartLine() + 1);
        //assertEquals("[position column]", 35, message.getColumn());

        // TODO: Test encoding issues (e.g. & in path where the XML source comment would be &amp; instead)
    }

    public void test() throws Exception {
        File tempDir = Files.createTempDir();
        sourceFile = new File(tempDir, "values.xml"); // Name matters for position search
        sourceFilePath = sourceFile.getAbsolutePath();
        File source = new File(tempDir, "dimens.xml");
        Files.write("<resources>\n" +
                "    <!-- Default screen margins, per the Android Design guidelines. -->\n" +
                "    <dimen name=\"activity_horizontal_margin\">16dp</dimen>\n" +
                "    <dimen name=\"activity_vertical_margin\">16dp</dimen>\n" +
                "    <dimen name=\"new_name\">50</dimen>\n" +
                "</resources>", source, Charsets.UTF_8);
        source.deleteOnExit();
        Files.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <!-- From: file:/Users/unittest/AndroidStudioProjects/BlankProject1Project/BlankProject1/build/exploded-bundles/ComAndroidSupportAppcompatV71800.aar/res/values/values.xml -->\n"
                +
                "    <dimen name=\"abc_action_bar_default_height\">48dip</dimen>\n" +
                "    <dimen name=\"abc_action_bar_icon_vertical_padding\">8dip</dimen>\n" +
                "    <!-- From: file:" + source.getPath() + " -->\n" +
                "    <dimen name=\"activity_horizontal_margin\">16dp</dimen>\n" +
                "    <dimen name=\"activity_vertical_margin\">16dp</dimen>\n" +
                "    <dimen name=\"ok\">50dp</dimen>\n" +
                "    <dimen name=\"new_name\">50</dimen>\n" +
                "    <!-- From: file:/Users/unittest/AndroidStudioProjects/BlankProject1Project/BlankProject1/build/exploded-bundles/ComAndroidSupportAppcompatV71800.aar/res/values/values.xml -->\n"
                +
                "    <item name=\"action_bar_activity_content\" type=\"id\"/>\n" +
                "    <item name=\"action_menu_divider\" type=\"id\"/>\n" +
                "    <item name=\"action_menu_presenter\" type=\"id\"/>\n" +
                "    <item name=\"home\" type=\"id\"/>\n" +
                "</resources>\n", sourceFile, Charsets.UTF_8);

        String output =
                ":BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        ":BlankProject1:prepareDebugDependencies\n" +
                        ":BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        ":BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        ":BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        ":BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        ":BlankProject1:processDebugResources\n" +
                        sourceFilePath
                        + ":10: error: Error: Integer types not allowed (at 'new_name' with value '50').\n"
                        +
                        ":BlankProject1:processDebugResources FAILED\n" +
                        "\n"; /* +
                        "FAILURE: Build failed with an exception.\n" +
                        "\n" +
                        "* What went wrong:\n" +
                        "Execution failed for task ':BlankProject1:processDebugResources'.\n" +
                        "> Failed to run command:\n" +
                        "  \t/Users/tnorbye/dev/sdks/build-tools/18.0.1/aapt package -f --no-crunch -I ...\n"
                        +
                        "  Error Code:\n" +
                        "  \t1\n" +
                        "  Output:\n" +
                        "  \t" + sourceFilePath
                        + ":10: error: Error: Integer types not allowed (at 'new_name' with value '50').\n"
                        +
                        "\n" +
                        "\n" +
                        "* Try:\n" +
                        "Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output.\n"
                        +
                        "\n" +
                        "BUILD FAILED\n" +
                        "\n" +
                        "Total time: 5.435 secs"; */

        String expected =
                "0: Simple::BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        "1: Simple::BlankProject1:prepareDebugDependencies\n" +
                        "2: Simple::BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        "3: Simple::BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        "4: Simple::BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        "5: Simple::BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        "6: Simple::BlankProject1:processDebugResources\n" +
                        "7: Error:Integer types not allowed (at 'new_name' with value '50').\n" +
                        "\t" + source.getPath() + ":5:28-30\n" +
                        "8: Simple::BlankProject1:processDebugResources FAILED\n"; /* +
                        "9: Error:Error while executing aapt command\n" +
                        "10: Error:Integer types not allowed (at 'new_name' with value '50').\n" +
                        "\t" + source.getPath() + ":5:28\n" +
                        "11: Error:Execution failed for task ':BlankProject1:processDebugResources'.\n"
                        +
                        "12: Info:BUILD FAILED\n" +
                        "13: Info:Total time: 5.435 secs\n";*/
        String actual =
                toString(parser.parseToolOutput(output));
        assertEquals(expected, actual);

        sourceFile.delete();
        source.delete();
        tempDir.delete();
    }

    public void testDashes() throws Exception {
        File tempDir = Files.createTempDir();
        String dirName = currentPlatform() == PLATFORM_WINDOWS ? "My -- Q&A Dir" : "My -- Q&A< Dir";
        File dir = new File(tempDir,
                dirName); // path which should force encoding of path chars, see for example issue 60050
        dir.mkdirs();
        sourceFile = new File(dir, "values.xml"); // Name matters for position search
        sourceFilePath = sourceFile.getAbsolutePath();
        File source = new File(dir, "dimens.xml");
        Files.write("<resources>\n" +
                "    <!-- Default screen margins, per the Android Design guidelines. -->\n" +
                "    <dimen name=\"activity_horizontal_margin\">16dp</dimen>\n" +
                "    <dimen name=\"activity_vertical_margin\">16dp</dimen>\n" +
                "    <dimen name=\"new_name\">50</dimen>\n" +
                "</resources>", source, Charsets.UTF_8);
        source.deleteOnExit();
        Files.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <!-- From: file:/Users/unittest/AndroidStudioProjects/BlankProject1Project/BlankProject1/build/exploded-bundles/ComAndroidSupportAppcompatV71800.aar/res/values/values.xml -->\n"
                +
                "    <dimen name=\"abc_action_bar_default_height\">48dip</dimen>\n" +
                "    <dimen name=\"abc_action_bar_icon_vertical_padding\">8dip</dimen>\n" +
                "    <!-- " + createPathComment(source, false) + " -->\n" +
                "    <dimen name=\"activity_horizontal_margin\">16dp</dimen>\n" +
                "    <dimen name=\"activity_vertical_margin\">16dp</dimen>\n" +
                "    <dimen name=\"ok\">50dp</dimen>\n" +
                "    <dimen name=\"new_name\">50</dimen>\n" +
                "    <!-- From: file:/Users/unittest/AndroidStudioProjects/BlankProject1Project/BlankProject1/build/exploded-bundles/ComAndroidSupportAppcompatV71800.aar/res/values/values.xml -->\n"
                +
                "    <item name=\"action_bar_activity_content\" type=\"id\"/>\n" +
                "    <item name=\"action_menu_divider\" type=\"id\"/>\n" +
                "    <item name=\"action_menu_presenter\" type=\"id\"/>\n" +
                "    <item name=\"home\" type=\"id\"/>\n" +
                "</resources>\n", sourceFile, Charsets.UTF_8);

        // TODO: Test layout too

        String output =
                ":BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        ":BlankProject1:prepareDebugDependencies\n" +
                        ":BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        ":BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        ":BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        ":BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        ":BlankProject1:processDebugResources\n" +
                        sourceFilePath
                        + ":10: error: Error: Integer types not allowed (at 'new_name' with value '50').\n"
                        +
                        ":BlankProject1:processDebugResources FAILED\n";

        String expected =
                "0: Simple::BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        "1: Simple::BlankProject1:prepareDebugDependencies\n" +
                        "2: Simple::BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        "3: Simple::BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        "4: Simple::BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        "5: Simple::BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        "6: Simple::BlankProject1:processDebugResources\n" +
                        "7: Error:Integer types not allowed (at 'new_name' with value '50').\n" +
                        "\t" + source.getPath() + ":5:28-30\n" +
                        "8: Simple::BlankProject1:processDebugResources FAILED\n";
        String actual = toString(parser.parseToolOutput(output));

        assertEquals(expected, actual);
        sourceFile.delete();
        source.delete();
        dir.delete();
        tempDir.delete();
    }

    public void testLayoutFileSuffix() throws Exception {
        File tempDir = Files.createTempDir();
        sourceFile = new File(tempDir, "layout.xml");
        sourceFilePath = sourceFile.getAbsolutePath();
        File source = new File(tempDir, "real-layout.xml");
        Files.write(
                "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\"\n" +
                        "    android:paddingLeft=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingRight=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingTop=\"@dimen/activity_vertical_margin\"\n" +
                        "    android:paddingBottom=\"@dimen/activity_vertical_margin\"\n" +
                        "    tools:context=\".MainActivity\">\n" +
                        "\n" +
                        "\n" +
                        "    <Button\n" +
                        "        android:layout_width=\"wrap_content\"\n" +
                        "        android:layout_height=\"wrap_content\"\n" +
                        "        android:hint=\"fy faen\"\n" +
                        "        android:text=\"@string/hello_world\"\n" +
                        "        android:slayout_alignParentTop=\"true\"\n" +
                        "        android:layout_alignParentLeft=\"true\" />\n" +
                        "\n" +
                        "</RelativeLayout>\n", source, Charsets.UTF_8);
        source.deleteOnExit();
        Files.write(
                "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\"\n" +
                        "    android:paddingLeft=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingRight=\"@dimen/activity_horizontal_margin\"\n" +
                        "    android:paddingTop=\"@dimen/activity_vertical_margin\"\n" +
                        "    android:paddingBottom=\"@dimen/activity_vertical_margin\"\n" +
                        "    tools:context=\".MainActivity\">\n" +
                        "\n" +
                        "    <!--style=\"@style/Buttons\"-->\n" +
                        "    <Button\n" +
                        "        android:layout_width=\"wrap_content\"\n" +
                        "        android:layout_height=\"wrap_content\"\n" +
                        "        android:hint=\"fy faen\"\n" +
                        "        android:text=\"@string/hello_world\"\n" +
                        "        android:slayout_alignParentTop=\"true\"\n" +
                        "        android:layout_alignParentLeft=\"true\" />\n" +
                        "\n" +
                        "</RelativeLayout>\n" +
                        "<!-- " + createPathComment(source, false) + " -->", sourceFile,
                Charsets.UTF_8);

        String output =
                ":BlankProject1:preBuild UP-TO-DATE\n" +
                        ":BlankProject1:preDebugBuild UP-TO-DATE\n" +
                        ":BlankProject1:preReleaseBuild UP-TO-DATE\n" +
                        ":BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        ":BlankProject1:prepareDebugDependencies\n" +
                        ":BlankProject1:compileDebugAidl UP-TO-DATE\n" +
                        ":BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        ":BlankProject1:generateDebugBuildConfig UP-TO-DATE\n" +
                        ":BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        ":BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        ":BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        ":BlankProject1:processDebugResources\n" +
                        sourceFilePath
                        + ":12: error: No resource identifier found for attribute 'slayout_alignParentTop' in package 'android'\n"
                        +
                        ":BlankProject1:processDebugResources FAILED\n";

        assertEquals("0: Simple::BlankProject1:preBuild UP-TO-DATE\n" +
                        "1: Simple::BlankProject1:preDebugBuild UP-TO-DATE\n" +
                        "2: Simple::BlankProject1:preReleaseBuild UP-TO-DATE\n" +
                        "3: Simple::BlankProject1:prepareComAndroidSupportAppcompatV71800Library UP-TO-DATE\n"
                        +
                        "4: Simple::BlankProject1:prepareDebugDependencies\n" +
                        "5: Simple::BlankProject1:compileDebugAidl UP-TO-DATE\n" +
                        "6: Simple::BlankProject1:compileDebugRenderscript UP-TO-DATE\n" +
                        "7: Simple::BlankProject1:generateDebugBuildConfig UP-TO-DATE\n" +
                        "8: Simple::BlankProject1:mergeDebugAssets UP-TO-DATE\n" +
                        "9: Simple::BlankProject1:mergeDebugResources UP-TO-DATE\n" +
                        "10: Simple::BlankProject1:processDebugManifest UP-TO-DATE\n" +
                        "11: Simple::BlankProject1:processDebugResources\n" +
                        "12: Error:No resource identifier found for attribute 'slayout_alignParentTop' in package 'android'\n"
                        +
                        "\t" + source.getPath() + ":12\n" +
                        "13: Simple::BlankProject1:processDebugResources FAILED\n",
                toString(parser.parseToolOutput(output)));

        sourceFile.delete();
        source.delete();
        tempDir.delete();
    }

    public void testMismatchedTag() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=59824
        createTempXmlFile();
        String output =
                ":AudioPlayer:prepareDebugDependencies\n" +
                        ":AudioPlayer:compileDebugAidl UP-TO-DATE\n" +
                        ":AudioPlayer:generateDebugBuildConfig UP-TO-DATE\n" +
                        ":AudioPlayer:mergeDebugAssets UP-TO-DATE\n" +
                        ":AudioPlayer:compileDebugRenderscript UP-TO-DATE\n" +
                        ":AudioPlayer:mergeDebugResources UP-TO-DATE\n" +
                        ":AudioPlayer:processDebugManifest UP-TO-DATE\n" +
                        ":AudioPlayer:processDebugResources\n" +
                        sourceFilePath + ":101: error: Error parsing XML: mismatched tag\n" +
                        ":AudioPlayer:processDebugResources FAILED\n" +
                        "\n";
        assertEquals("0: Simple::AudioPlayer:prepareDebugDependencies\n" +
                        "1: Simple::AudioPlayer:compileDebugAidl UP-TO-DATE\n" +
                        "2: Simple::AudioPlayer:generateDebugBuildConfig UP-TO-DATE\n" +
                        "3: Simple::AudioPlayer:mergeDebugAssets UP-TO-DATE\n" +
                        "4: Simple::AudioPlayer:compileDebugRenderscript UP-TO-DATE\n" +
                        "5: Simple::AudioPlayer:mergeDebugResources UP-TO-DATE\n" +
                        "6: Simple::AudioPlayer:processDebugManifest UP-TO-DATE\n" +
                        "7: Simple::AudioPlayer:processDebugResources\n" +
                        "8: Error:Error parsing XML: mismatched tag\n" +
                        "\t" + sourceFilePath + ":101\n" +
                        "9: Simple::AudioPlayer:processDebugResources FAILED\n",
                toString(parser.parseToolOutput(output)));
        sourceFile.delete();
    }
}
