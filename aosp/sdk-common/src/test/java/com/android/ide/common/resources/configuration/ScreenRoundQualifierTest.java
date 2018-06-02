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

package com.android.ide.common.resources.configuration;

import com.android.resources.ScreenRound;

import junit.framework.TestCase;

public class ScreenRoundQualifierTest extends TestCase {

    private ScreenRoundQualifier soq;
    private FolderConfiguration config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        soq = new ScreenRoundQualifier();
        config = new FolderConfiguration();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        soq = null;
        config = null;
    }

    public void testRound() {
        assertEquals(true, soq.checkAndSet("round", config));
        assertTrue(config.getScreenRoundQualifier() != null);
        assertEquals(ScreenRound.ROUND, config.getScreenRoundQualifier().getValue());
        assertEquals("round", config.getScreenRoundQualifier().toString());
    }

    public void testNotRound() {
        assertEquals(true, soq.checkAndSet("notround", config));
        assertTrue(config.getScreenRoundQualifier() != null);
        assertEquals(ScreenRound.NOTROUND,
                config.getScreenRoundQualifier().getValue());
        assertEquals("notround", config.getScreenRoundQualifier().toString());
    }

    public void testFailures() {
        assertEquals(false, soq.checkAndSet("", config));
        assertEquals(false, soq.checkAndSet("ROUND", config));
        assertEquals(false, soq.checkAndSet("not-round", config));
    }
}
