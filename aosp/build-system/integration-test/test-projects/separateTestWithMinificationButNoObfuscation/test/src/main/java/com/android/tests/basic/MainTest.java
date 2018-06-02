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

package com.android.tests.basic;

import com.android.tests.utils.Utility;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

public class MainTest extends ActivityInstrumentationTestCase2<Main> {

    private TextView mTextView;
    private Main mMain;

    /**
     * Creates an {@link ActivityInstrumentationTestCase2} that tests the {@link Main} activity.
     */
    public MainTest() {
        super(Main.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMain = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(mMain);
        mTextView = (TextView) mMain.findViewById(R.id.text);

    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mTextView);
        assertEquals(mMain.getUtility().getValue(), mTextView.getText());
    }
}

