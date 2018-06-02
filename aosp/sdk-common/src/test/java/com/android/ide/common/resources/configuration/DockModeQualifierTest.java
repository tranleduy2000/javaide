/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.ide.common.resources.configuration;

import com.android.resources.UiMode;

import junit.framework.TestCase;

public class DockModeQualifierTest extends TestCase {

    private UiModeQualifier mCarQualifier;
    private UiModeQualifier mDeskQualifier;
    private UiModeQualifier mTVQualifier;
    private UiModeQualifier mNoneQualifier;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCarQualifier = new UiModeQualifier(UiMode.CAR);
        mDeskQualifier = new UiModeQualifier(UiMode.DESK);
        mTVQualifier = new UiModeQualifier(UiMode.TELEVISION);
        mNoneQualifier = new UiModeQualifier(UiMode.NORMAL);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mCarQualifier = null;
        mDeskQualifier = null;
        mTVQualifier = null;
        mNoneQualifier = null;
    }

    public void testIsBetterMatchThan() {
        assertTrue(mNoneQualifier.isBetterMatchThan(mCarQualifier, mDeskQualifier));
        assertTrue(mNoneQualifier.isBetterMatchThan(mCarQualifier, mDeskQualifier));
        assertFalse(mNoneQualifier.isBetterMatchThan(mDeskQualifier, mDeskQualifier));
        assertTrue(mNoneQualifier.isBetterMatchThan(mDeskQualifier, mCarQualifier));
        assertFalse(mNoneQualifier.isBetterMatchThan(mCarQualifier, mCarQualifier));

        assertTrue(mDeskQualifier.isBetterMatchThan(mCarQualifier, mDeskQualifier));
        assertFalse(mDeskQualifier.isBetterMatchThan(mCarQualifier, mCarQualifier));

        assertTrue(mCarQualifier.isBetterMatchThan(mDeskQualifier, mCarQualifier));
        assertFalse(mCarQualifier.isBetterMatchThan(mDeskQualifier, mDeskQualifier));

        assertTrue(mTVQualifier.isBetterMatchThan(mCarQualifier, mTVQualifier));
        assertFalse(mTVQualifier.isBetterMatchThan(mDeskQualifier, mDeskQualifier));

    }

    public void testIsMatchFor() {
        assertTrue(mNoneQualifier.isMatchFor(mCarQualifier));
        assertTrue(mNoneQualifier.isMatchFor(mDeskQualifier));
        assertTrue(mNoneQualifier.isMatchFor(mTVQualifier));
        assertTrue(mCarQualifier.isMatchFor(mCarQualifier));
        assertTrue(mDeskQualifier.isMatchFor(mDeskQualifier));
        assertTrue(mTVQualifier.isMatchFor(mTVQualifier));

        assertFalse(mCarQualifier.isMatchFor(mNoneQualifier));
        assertFalse(mCarQualifier.isMatchFor(mDeskQualifier));

        assertFalse(mDeskQualifier.isMatchFor(mCarQualifier));
        assertFalse(mDeskQualifier.isMatchFor(mNoneQualifier));

        assertFalse(mTVQualifier.isMatchFor(mCarQualifier));
        assertFalse(mTVQualifier.isMatchFor(mNoneQualifier));
    }
}
