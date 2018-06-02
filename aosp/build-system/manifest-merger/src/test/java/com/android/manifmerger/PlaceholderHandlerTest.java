/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.manifmerger;

import static com.android.manifmerger.PlaceholderHandler.KeyBasedValueResolver;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.android.sdklib.mock.MockLog;
import com.google.common.base.Optional;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.PlaceholderHandler}
 */
public class PlaceholderHandlerTest extends TestCase {

    @Mock
    ActionRecorder mActionRecorder;

    @Mock
    MergingReport.Builder mBuilder;

    MockLog mMockLog = new MockLog();

    KeyBasedValueResolver<String> nullResolver = new KeyBasedValueResolver<String>() {
        @Override
        public String getValue(@NonNull String key) {
            // not provided a placeholder value should generate an error.
            return null;
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(mBuilder.getLogger()).thenReturn(mMockLog);
        when(mBuilder.getActionRecorder()).thenReturn(mActionRecorder);
    }

    public void testPlaceholders() throws ParserConfigurationException, SAXException, IOException {

        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:attr1=\"${landscapePH}\"\n"
                + "         android:attr2=\"prefix.${landscapePH}\"\n"
                + "         android:attr3=\"${landscapePH}.suffix\"\n"
                + "         android:attr4=\"prefix${landscapePH}suffix\">\n"
                + "    </activity>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPlaceholders#xml"), xml);

        PlaceholderHandler handler = new PlaceholderHandler();
        handler.visit(
                ManifestMerger2.MergeType.APPLICATION,
                refDocument, new KeyBasedValueResolver<String>() {
                    @Override
                    public String getValue(@NonNull String key) {
                        return "newValue";
                    }
                }, mBuilder);

        Optional<XmlElement> activityOne = refDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY, ".activityOne");
        assertTrue(activityOne.isPresent());
        assertEquals(5, activityOne.get().getAttributes().size());
        // check substitution.
        assertEquals("newValue",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr1")).get().getValue());
        assertEquals("prefix.newValue",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr2")).get().getValue());
        assertEquals("newValue.suffix",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr3")).get().getValue());
        assertEquals("prefixnewValuesuffix",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr4")).get().getValue());

        for (XmlAttribute xmlAttribute : activityOne.get().getAttributes()) {
            // any attribute other than android:name should have been injected.
            if (!xmlAttribute.getName().toString().contains("name")) {
                verify(mActionRecorder).recordAttributeAction(
                        xmlAttribute,
                        SourcePosition.UNKNOWN,
                        Actions.ActionType.INJECTED,
                        null);
            }
        }
    }

    public void testSeveralPlaceholders() throws ParserConfigurationException, SAXException, IOException {

        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:attr1=\"prefix${first}${second}\"\n"
                + "         android:attr2=\"${first}${second}suffix\"\n"
                + "         android:attr3=\"prefix${first}.${second}suffix\"\n"
                + "         android:attr4=\"${first}.${second}\"/>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPlaceholders#xml"), xml);

        PlaceholderHandler handler = new PlaceholderHandler();
        handler.visit(
                ManifestMerger2.MergeType.APPLICATION,
                refDocument, new KeyBasedValueResolver<String>() {
                    @Override
                    public String getValue(@NonNull String key) {
                        if (key.equals("first")) {
                            return "firstValue";
                        } else {
                            return "secondValue";
                        }
                    }
                }, mBuilder);

        Optional<XmlElement> activityOne = refDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY, ".activityOne");
        assertTrue(activityOne.isPresent());
        assertEquals(5, activityOne.get().getAttributes().size());
        // check substitution.

        assertEquals("prefixfirstValuesecondValue",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr1")).get().getValue());

        assertEquals("firstValuesecondValuesuffix",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr2")).get().getValue());

        assertEquals("prefixfirstValue.secondValuesuffix",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr3")).get().getValue());

        assertEquals("firstValue.secondValue",
                activityOne.get().getAttribute(
                        XmlNode.fromXmlName("android:attr4")).get().getValue());

        for (XmlAttribute xmlAttribute : activityOne.get().getAttributes()) {
            // any attribute other than android:name should have been injected.
            if (!xmlAttribute.getName().toString().contains("name")) {
                verify(mActionRecorder, times(2)).recordAttributeAction(
                        xmlAttribute,
                        SourcePosition.UNKNOWN,
                        Actions.ActionType.INJECTED,
                        null);

            }
        }
    }

    public void testPlaceHolder_notProvided()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:attr1=\"${landscapePH}\"/>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPlaceholders#xml"), xml);

        PlaceholderHandler handler = new PlaceholderHandler();
        handler.visit(ManifestMerger2.MergeType.APPLICATION, refDocument, nullResolver, mBuilder);
        // verify the error was recorded.
        verify(mBuilder).addMessage(
                any(SourceFilePosition.class),
                eq(MergingReport.Record.Severity.ERROR), anyString());
    }

    public void testPlaceHolder_notProvided_inLibrary()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:attr1=\"${landscapePH}\"/>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPlaceholders#xml"), xml);

        PlaceholderHandler handler = new PlaceholderHandler();
        handler.visit(ManifestMerger2.MergeType.LIBRARY, refDocument, nullResolver, mBuilder);
        // verify the error was recorded.
        verify(mBuilder).addMessage(
                any(SourceFilePosition.class),
                eq(MergingReport.Record.Severity.INFO), anyString());
    }
}
