/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.repository.sources;


import junit.framework.TestCase;

public class SdkSourcePropertiesTest extends TestCase {

    private static class MockSdkSourceProperties extends SdkSourceProperties {
        private int mLoadCount;
        private int mSaveCount;

        public MockSdkSourceProperties() {
            clear();
        }

        public int getLoadCount() {
            return mLoadCount;
        }

        public int getSaveCount() {
            return mSaveCount;
        }

        @Override
        protected boolean loadProperties() {
            // Don't actually load anthing.
            mLoadCount++;
            return false;
        }

        @Override
        protected void saveLocked() {
            // Don't actually save anything.
            mSaveCount++;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void testSdkSourceProperties() {
        MockSdkSourceProperties m = new MockSdkSourceProperties();

        assertEquals(0, m.getLoadCount());
        assertEquals(0, m.getSaveCount());
        assertEquals(
                "<SdkSourceProperties>",
                m.toString());

        assertNull(m.getProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/1", null));
        assertEquals("None",
                     m.getProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", "None"));
        assertEquals(1, m.getLoadCount());
        assertEquals(0, m.getSaveCount());
        assertEquals(
                "<SdkSourceProperties\n" +
                "@version@ = 1>",
                m.toString());

        m.setProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/1", "disabled");
        assertEquals("disabled",
                m.getProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/1", "None"));
        assertNull(m.getProperty(SdkSourceProperties.KEY_NAME, "http://example.com/1", null));
        assertEquals(
                "<SdkSourceProperties\n" +
                "@disabled@http://example.com/1 = disabled\n" +
                "@version@ = 1>",
                m.toString());

        m.setProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", "Site Name");
        assertEquals("Site Name",
                m.getProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", null));
        assertNull(m.getProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/2", null));
        assertEquals(1, m.getLoadCount());
        assertEquals(0, m.getSaveCount());
        assertEquals(
                "<SdkSourceProperties\n" +
                "@disabled@http://example.com/1 = disabled\n" +
                "@name@http://example.com/2 = Site Name\n" +
                "@version@ = 1>",
                m.toString());

        m.save();
        assertEquals(1, m.getSaveCount());

        // saving a 2nd time doesn't do anything if no property has been modified
        m.save();
        assertEquals(1, m.getSaveCount());

        // setting things to the same value doesn't actually mark the properties as modified
        m.setProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/1", "disabled");
        m.setProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", "Site Name");
        m.save();
        assertEquals(1, m.getSaveCount());

        m.setProperty(SdkSourceProperties.KEY_DISABLED, "http://example.com/1", "not disabled");
        m.setProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", "New Name");
        assertEquals(
                "<SdkSourceProperties\n" +
                "@disabled@http://example.com/1 = not disabled\n" +
                "@name@http://example.com/2 = New Name\n" +
                "@version@ = 1>",
                m.toString());
        m.save();
        assertEquals(2, m.getSaveCount());

        // setting a value to null deletes it
        m.setProperty(SdkSourceProperties.KEY_NAME, "http://example.com/2", null);
        assertEquals(
                "<SdkSourceProperties\n" +
                "@disabled@http://example.com/1 = not disabled\n" +
                "@version@ = 1>",
                m.toString());

        m.save();
        assertEquals(1, m.getLoadCount());
        assertEquals(3, m.getSaveCount());
    }

}
