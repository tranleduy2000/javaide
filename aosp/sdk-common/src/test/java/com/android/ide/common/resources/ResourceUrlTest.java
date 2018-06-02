/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.ide.common.resources;

import static com.android.resources.ResourceType.ATTR;
import static com.android.resources.ResourceType.DIMEN;
import static com.android.resources.ResourceType.ID;
import static com.android.resources.ResourceType.LAYOUT;

import com.android.ide.common.rendering.api.ResourceValue;
import com.android.resources.ResourceType;

import junit.framework.TestCase;

public class ResourceUrlTest extends TestCase {
    @SuppressWarnings("ConstantConditions")
    public void testParseResource() {
        assertNull(ResourceUrl.parse(""));
        assertNull(ResourceUrl.parse("not_a_resource"));
        assertNull(ResourceUrl.parse("@null"));
        assertNull(ResourceUrl.parse("@empty"));
        assertNull(ResourceUrl.parse("@undefined"));
        assertNull(ResourceUrl.parse("@?"));
        assertNull(ResourceUrl.parse("@android:layout"));
        assertNull(ResourceUrl.parse("@layout"));

        assertEquals("foo", ResourceUrl.parse("@id/foo").name);
        assertEquals(ID, ResourceUrl.parse("@id/foo").type);
        assertFalse(ResourceUrl.parse("@id/foo").framework);
        assertFalse(ResourceUrl.parse("@id/foo").create);
        assertFalse(ResourceUrl.parse("@id/foo").theme);

        assertEquals("foo", ResourceUrl.parse("@+id/foo").name);
        assertEquals(ID, ResourceUrl.parse("@+id/foo").type);
        assertFalse(ResourceUrl.parse("@+id/foo").framework);
        assertTrue(ResourceUrl.parse("@+id/foo").create);

        assertEquals(LAYOUT, ResourceUrl.parse("@layout/foo").type);
        assertEquals(DIMEN, ResourceUrl.parse("@dimen/foo").type);
        assertFalse(ResourceUrl.parse("@dimen/foo").framework);
        assertEquals("foo", ResourceUrl.parse("@android:dimen/foo").name);
        assertEquals(DIMEN, ResourceUrl.parse("@android:dimen/foo").type);
        assertTrue(ResourceUrl.parse("@android:dimen/foo").framework);
        assertEquals("foo", ResourceUrl.parse("@layout/foo").name);
        assertEquals("foo", ResourceUrl.parse("@dimen/foo").name);
        assertEquals(ATTR, ResourceUrl.parse("?attr/foo").type);
        assertTrue(ResourceUrl.parse("?attr/foo").theme);
        assertEquals("foo", ResourceUrl.parse("?attr/foo").name);
        assertFalse(ResourceUrl.parse("?attr/foo").framework);
        assertEquals(ATTR, ResourceUrl.parse("?foo").type);
        assertEquals("foo", ResourceUrl.parse("?foo").name);
        assertFalse(ResourceUrl.parse("?foo").framework);
        assertEquals(ATTR, ResourceUrl.parse("?android:foo").type);
        assertEquals("foo", ResourceUrl.parse("?android:foo").name);
        assertTrue(ResourceUrl.parse("?android:foo").framework);
        assertTrue(ResourceUrl.parse("?android:foo").theme);
        assertFalse(ResourceUrl.parse("?foo", false).framework);
        assertTrue(ResourceUrl.parse("?android:foo", false).framework);
        assertTrue(ResourceUrl.parse("?foo", true).framework);
        assertTrue(ResourceUrl.parse("?attr/foo", true).framework);

        assertEquals("@+id/foo", ResourceUrl.parse("@+id/foo").toString());
        assertEquals("@layout/foo", ResourceUrl.parse("@layout/foo").toString());
        assertEquals("@android:layout/foo", ResourceUrl.parse("@android:layout/foo").toString());
        assertEquals("?android:attr/foo", ResourceUrl.parse("?android:foo").toString());

        assertTrue(ResourceUrl.parse("@id/foo").hasValidName());
        assertFalse(ResourceUrl.parse("@id/foo bar").hasValidName());
        assertFalse(ResourceUrl.parse("@id/").hasValidName());
        assertFalse(ResourceUrl.parse("@id/?").hasValidName());
        assertFalse(ResourceUrl.parse("@id/123").hasValidName());
        assertFalse(ResourceUrl.parse("@id/ab+").hasValidName());
    }

    public void testCreateFromResourceValue() {
        assertEquals("@android:string/foo",
            ResourceUrl.create(new ResourceValue(ResourceType.STRING, "foo", true)).toString());
        assertEquals("@string/foo",
            ResourceUrl.create(new ResourceValue(ResourceType.STRING, "foo", false)).toString());
    }
}
