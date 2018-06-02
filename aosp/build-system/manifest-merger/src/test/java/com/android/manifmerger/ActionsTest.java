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

import static com.android.manifmerger.Actions.DecisionTreeRecord;
import static com.android.manifmerger.XmlNode.NodeKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.SdkConstants;
import com.android.ide.common.blame.SourceFile;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.android.sdklib.mock.MockLog;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for {@link Actions} class
 */
public class ActionsTest extends TestCase {

    public void testGetNodeKeys() {
        Element xmlElement = mock(Element.class);
        when(xmlElement.getNodeName()).thenReturn("activity");
        when(xmlElement.getAttributeNS(SdkConstants.ANDROID_URI, "name")).thenReturn("keyOne");
        NodeKey nodeKey = NodeKey.fromXml(xmlElement);
        assertNotNull(nodeKey);

        ImmutableMap.Builder<NodeKey, DecisionTreeRecord> records = ImmutableMap.builder();
        DecisionTreeRecord activityDecisionTree = new DecisionTreeRecord();
        records.put(nodeKey, activityDecisionTree);
        Actions actions = new Actions(records.build());

        assertEquals(1, actions.getNodeKeys().size());
        assertTrue(actions.getNodeKeys().contains(nodeKey));
    }

    public void testGetNodeRecords() {
        Element xmlElement = mock(Element.class);
        when(xmlElement.getNodeName()).thenReturn("activity");
        when(xmlElement.getAttributeNS(SdkConstants.ANDROID_URI, "name")).thenReturn("keyOne");
        NodeKey nodeKey = NodeKey.fromXml(xmlElement);
        assertNotNull(nodeKey);

        ImmutableMap.Builder<NodeKey, DecisionTreeRecord> records = ImmutableMap.builder();
        DecisionTreeRecord activityDecisionTree = new DecisionTreeRecord();
        activityDecisionTree.addNodeRecord(new Actions.NodeRecord(
                Actions.ActionType.ADDED,
                new SourceFilePosition(new SourceFile("file"), new SourcePosition(1, 2, -1)),
                new NodeKey("nodeKey"),
                null, /* reason */
                NodeOperationType.MERGE));
        records.put(nodeKey, activityDecisionTree);
        Actions actions = new Actions(records.build());

        // lookup using key
        ImmutableList<Actions.NodeRecord>  nodeRecords = actions.getNodeRecords(nodeKey);
        assertNotNull(nodeRecords);
        assertEquals(1, nodeRecords.size());
    }

    public void testGetAttributesRecords() {
        Element xmlElement = mock(Element.class);
        when(xmlElement.getNodeName()).thenReturn("activity");
        when(xmlElement.getAttributeNS(SdkConstants.ANDROID_URI, "name")).thenReturn("keyOne");
        NodeKey nodeKey = NodeKey.fromXml(xmlElement);
        assertNotNull(nodeKey);

        ImmutableMap.Builder<NodeKey, DecisionTreeRecord> records = ImmutableMap.builder();
        DecisionTreeRecord activityDecisionTree = new DecisionTreeRecord();
        activityDecisionTree.addNodeRecord(new Actions.NodeRecord(
                Actions.ActionType.ADDED,
                new SourceFilePosition(new SourceFile("file"), new SourcePosition(1, 2, -1)),
                new NodeKey("nodeKey"),
                null, /* reason */
                NodeOperationType.MERGE));
        XmlNode.NodeName attributeName = XmlNode.fromXmlName("android:name");
        activityDecisionTree.mAttributeRecords.put(attributeName,
                ImmutableList.of(
                    new Actions.AttributeRecord(Actions.ActionType.INJECTED,
                            new SourceFilePosition(
                                    new SourceFile("file"), new SourcePosition(1, 2, -1)),
                            new NodeKey("nodeKey"),
                            null, /* reason */
                            AttributeOperationType.STRICT)));
            records.put(nodeKey, activityDecisionTree);
        Actions actions = new Actions(records.build());

        ImmutableList<XmlNode.NodeName> recordedAttributeNames = actions
                .getRecordedAttributeNames(nodeKey);
        assertTrue(recordedAttributeNames.contains(attributeName));

        ImmutableList<Actions.AttributeRecord> attributeRecords = actions
                .getAttributeRecords(nodeKey, attributeName);
        assertEquals(1, attributeRecords.size());
    }


    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testActionsPersistenceAndLoading()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"                                                             // 1
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"      // 2
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"                  // 3
                + "    package=\"com.example.lib3\">\n"                                     // 4
                + "\n"                                                                      // 5
                + "    <permission\n"                                                       // 6
                + "          android:name=\"permissionOne\"\n"                              // 7
                + "          tools:node=\"remove\">\n"                                       // 8
                + "    </permission>\n"                                                     // 10
                + "    <permission \n"                                                      // 11
                + "          tools:node=\"removeAll\"\n"                                    // 12
                + "          tools:selector=\"com.example.lib3\">\n"                        // 13
                + "    </permission>\n"                                                     // 14
                + "    <permission\n"                                                       // 15
                + "             android:name=\"permissionThree\"\n"                         // 16
                + "             android:protectionLevel=\"signature\"\n"                    // 17
                + "             tools:node=\"replace\">\n"                                  // 18
                + "    </permission>\n"                                                     // 19
                + "\n"                                                                      // 20
                + "</manifest>";                                                            // 21

        String lowerPriorityOne = ""
                + "<manifest\n"                                                             // 1
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"      // 2
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"                  // 3
                + "    package=\"com.example.lib1\">\n"                                     // 4
                + "\n"                                                                      // 5
                + "    <permission android:name=\"permissionOne\"\n"                        // 6
                + "             android:protectionLevel=\"signature\">\n"                   // 7
                + "    </permission>\n"                                                     // 8
                + "    <permission android:name=\"permissionTwo\"\n"                        // 9
                + "             android:protectionLevel=\"signature\">\n"                   // 10
                + "    </permission>\n"                                                     // 11
                + "\n"                                                                      // 12
                + "</manifest>";                                                            // 13

        String lowerPriorityTwo = ""
                + "<manifest\n"                                                             // 1
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"      // 2
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"                  // 3
                + "    package=\"com.example.lib2\">\n"                                     // 4
                + "\n"                                                                      // 5
                + "    <permission android:name=\"permissionThree\"\n"                      // 6
                + "             android:protectionLevel=\"normal\">\n"                      // 7
                + "    </permission>\n"                                                     // 8
                + "    <permission android:name=\"permissionFour\"\n"                       // 9
                + "             android:protectionLevel=\"normal\">\n"                      // 10
                + "    </permission>\n"                                                     // 11
                + "\n"                                                                      // 12
                + "</manifest>";                                                            // 13

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument firstLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);
        XmlDocument secondLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityTwo"), lowerPriorityTwo);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());
        result = result.get().merge(secondLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());

        ILogger logger = new MockLog();
        XmlDocument cleanedDocument =
                ToolsInstructionsCleaner.cleanToolsReferences(result.get(), logger);
        assertNotNull(cleanedDocument);

        Actions actions = mergingReportBuilder.getActionRecorder().build();
        String expectedMappings = "1<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "2<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "3    package=\"com.example.lib3\" >\n"
                + "4\n"
                + "5    <permission\n"
                + "5-->ActionsTest#higherPriority:14:5-18:18\n"
                + "6        android:name=\"permissionThree\"\n"
                + "6-->ActionsTest#higherPriority:15:14-44\n"
                + "7        android:protectionLevel=\"signature\" >\n"
                + "7-->ActionsTest#higherPriority:16:14-49\n"
                + "8    </permission>\n"
                + "9    <permission\n"
                + "9-->ActionsTest#lowerPriorityOne:9:5-11:18\n"
                + "10        android:name=\"permissionTwo\"\n"
                + "10-->ActionsTest#lowerPriorityOne:9:17-45\n"
                + "11        android:protectionLevel=\"signature\" >\n"
                + "11-->ActionsTest#lowerPriorityOne:10:14-49\n"
                + "12    </permission>\n"
                + "13    <permission\n"
                + "13-->ActionsTest#lowerPriorityTwo:9:5-11:18\n"
                + "14        android:name=\"permissionFour\"\n"
                + "14-->ActionsTest#lowerPriorityTwo:9:17-46\n"
                + "15        android:protectionLevel=\"normal\" >\n"
                + "15-->ActionsTest#lowerPriorityTwo:10:14-46\n"
                + "16    </permission>\n"
                + "17\n"
                + "18</manifest>\n";
        assertEquals(expectedMappings, actions.blame(cleanedDocument));

        // persist the records
        String persistedMappings = actions.persist();
        System.out.println(persistedMappings);

        // and reload them from the persisted media.
        Actions newActions = Actions.load(persistedMappings);
        assertNotNull(newActions);

        // check equality.
        for (NodeKey nodeKey : actions.getNodeKeys()) {

            ImmutableList<Actions.NodeRecord> expectedNodeRecords = actions.getNodeRecords(nodeKey);
            assertNotNull(expectedNodeRecords);
            assertEquals(expectedNodeRecords.size(), newActions.getNodeRecords(nodeKey).size());

            for (Actions.NodeRecord nodeRecord : newActions.getNodeRecords(nodeKey)) {
                assertTrue("Cannot find node=" + nodeKey + "record=" + nodeRecord,
                        findNodeRecordInList(nodeRecord, expectedNodeRecords));
            }

            for (XmlNode.NodeName nodeName : actions
                    .getRecordedAttributeNames(nodeKey)) {
                ImmutableList<Actions.AttributeRecord> expectedAttributeRecords =
                        actions.getAttributeRecords(nodeKey, nodeName);
                ImmutableList<Actions.AttributeRecord> actualAttributeRecords =
                        newActions.getAttributeRecords(nodeKey, nodeName);
                assertEquals(expectedAttributeRecords.size(), actualAttributeRecords.size());
                for (Actions.AttributeRecord expectedAttributeRecord : expectedAttributeRecords) {
                    assertTrue("Cannot find attribute=" + nodeName
                                    + " record=" + expectedAttributeRecord,
                            findAttributeRecordInList(
                                    expectedAttributeRecord, actualAttributeRecords)
                    );
                }
            }

        }
    }

    public static boolean findNodeRecordInList(
            Actions.NodeRecord nodeRecord,
            List<Actions.NodeRecord> nodeRecordList) {
        for (Actions.NodeRecord record : nodeRecordList) {
            if (record.getActionLocation().equals(nodeRecord.getActionLocation())
                    && record.getActionType() == nodeRecord.getActionType()) {
                return true;
            }
        }
        return false;
    }

    private static boolean findAttributeRecordInList(
            Actions.AttributeRecord attributeRecord,
            List<Actions.AttributeRecord> attributeRecordList) {

        for (Actions.AttributeRecord record : attributeRecordList) {
            if (record.getOperationType() == attributeRecord.getOperationType()
                    && record.getActionType() == attributeRecord.getActionType()
                    && record.getActionLocation().equals(attributeRecord.getActionLocation())) {
                return true;
            }
        }
        return false;
    }
}
