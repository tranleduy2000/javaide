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

package com.android.tools.lint.client.api;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class DefaultSdkInfoTest extends TestCase {
    public void testGetParentClass() {
        DefaultSdkInfo info = new DefaultSdkInfo();
        assertNull(info.getParentViewClass("android.view.View"));
        assertEquals("android.view.View", info.getParentViewClass("android.view.ViewGroup"));
        assertEquals("android.view.ViewGroup",
                info.getParentViewClass("android.widget.LinearLayout"));
        assertEquals("android.widget.LinearLayout",
                info.getParentViewClass("android.widget.TableLayout"));
    }

    public void testGetParentName() {
        DefaultSdkInfo info = new DefaultSdkInfo();
        assertNull(info.getParentViewName("View"));
        assertEquals("View", info.getParentViewName("ViewGroup"));
        assertEquals("ViewGroup", info.getParentViewName("LinearLayout"));
        assertEquals("LinearLayout", info.getParentViewName("TableLayout"));
    }

    public void testIsSubViewOf() {
        DefaultSdkInfo info = new DefaultSdkInfo();
        assertTrue(info.isSubViewOf("Button", "Button"));
        assertTrue(info.isSubViewOf("TextView", "Button"));
        assertTrue(info.isSubViewOf("TextView", "RadioButton"));
        assertTrue(info.isSubViewOf("AdapterView", "Spinner"));
        assertTrue(info.isSubViewOf("AdapterView<?>", "Spinner"));
        assertFalse(info.isSubViewOf("Button", "TextView"));
        assertFalse(info.isSubViewOf("CheckBox", "ToggleButton"));
        assertFalse(info.isSubViewOf("ToggleButton", "CheckBox"));
        assertTrue(info.isSubViewOf("LinearLayout", "LinearLayout"));
        assertTrue(info.isSubViewOf("LinearLayout", "TableLayout"));
        assertFalse(info.isSubViewOf("TableLayout", "LinearLayout"));
        assertTrue(info.isSubViewOf("TextView", "EditText"));
        assertFalse(info.isSubViewOf("EditText", "TextView"));
        assertTrue(info.isSubViewOf("View", "TextView"));
        assertFalse(info.isSubViewOf("TextView", "View"));
        assertFalse(info.isSubViewOf("Spinner", "AdapterView<?>"));
    }
}
