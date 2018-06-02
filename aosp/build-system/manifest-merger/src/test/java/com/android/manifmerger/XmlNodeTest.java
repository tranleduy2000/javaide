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

import static org.mockito.Mockito.when;

import com.android.SdkConstants;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Tests for the {@link XmlNode} class
 */
public class XmlNodeTest extends TestCase {

    @Mock Node mNodeOne;
    @Mock Node mNodeTwo;
    @Mock Element mElement;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testNoNamespace() {
        when(mNodeOne.getNodeName()).thenReturn("my-name");

        XmlNode.NodeName nodeName = XmlNode.unwrapName(mNodeOne);
        assertFalse(nodeName.isInNamespace(SdkConstants.ANDROID_URI));
        assertEquals("my-name", nodeName.toString());
    }

    public void testNoNamespaceEqualityAndHashCoding() {
        when(mNodeOne.getNodeName()).thenReturn("my-name");
        when(mNodeTwo.getNodeName()).thenReturn("my-name");

        assertEquals(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertEquals(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());
    }

    public void testNoNamespaceInequalityAndHashCoding() {
        when(mNodeOne.getNodeName()).thenReturn("my-name");
        when(mNodeTwo.getNodeName()).thenReturn("my-other-name");

        assertNotSame(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertNotSame(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());
    }

    public void testNamespace() {
        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        XmlNode.NodeName nodeName = XmlNode.unwrapName(mNodeOne);
        assertTrue(nodeName.isInNamespace(SdkConstants.ANDROID_URI));
        assertEquals("android:my-name", nodeName.toString());
    }

    public void testNamespaceEqualityAndHashCoding() {
        // different local name.
        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        when(mNodeTwo.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeTwo.getLocalName()).thenReturn("my-name");
        when(mNodeTwo.getPrefix()).thenReturn("android");

        assertEquals(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertEquals(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());

        // different namespace.
        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        when(mNodeTwo.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeTwo.getLocalName()).thenReturn("my-name");
        // another prefix should not matter, they are still the same xml names.
        when(mNodeTwo.getPrefix()).thenReturn("y");

        assertEquals(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertEquals(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());
    }

    public void testNamespaceInequality() {
        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        when(mNodeTwo.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeTwo.getLocalName()).thenReturn("my-other-name");
        when(mNodeTwo.getPrefix()).thenReturn("android");

        assertNotSame(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertNotSame(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());


        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        when(mNodeTwo.getNamespaceURI()).thenReturn(SdkConstants.TOOLS_URI);
        when(mNodeTwo.getLocalName()).thenReturn("my-name");
        when(mNodeTwo.getPrefix()).thenReturn("android");

        assertNotSame(XmlNode.unwrapName(mNodeOne), XmlNode.unwrapName(mNodeTwo));
        assertNotSame(XmlNode.unwrapName(mNodeOne).hashCode(),
                XmlNode.unwrapName(mNodeTwo).hashCode());
    }

    public void testAddAttributeToNode() {
        when(mNodeOne.getNodeName()).thenReturn("my-name");

        XmlNode.NodeName nodeName = XmlNode.unwrapName(mNodeOne);
        nodeName.addToNode(mElement, "my-value");
        Mockito.verify(mElement).setAttribute("my-name", "my-value");
        Mockito.verifyNoMoreInteractions(mElement);
    }

    public void testAddNamespaceAwareAttributeToNode() {
        when(mNodeOne.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mNodeOne.getLocalName()).thenReturn("my-name");
        when(mNodeOne.getPrefix()).thenReturn("android");

        XmlNode.NodeName nodeName = XmlNode.unwrapName(mNodeOne);
        nodeName.addToNode(mElement, "my-value");
        Mockito.verify(mElement).setAttributeNS(
                SdkConstants.ANDROID_URI, "android:my-name", "my-value");
        Mockito.verifyNoMoreInteractions(mElement);
    }
}
