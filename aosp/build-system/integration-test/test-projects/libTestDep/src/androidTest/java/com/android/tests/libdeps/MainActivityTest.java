/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.tests.libdeps;

import com.android.tests.libdeps.MainActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

/**
 *
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TextView mLib1TextView1;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final MainActivity a = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(a);
        
        mLib1TextView1 = (TextView) a.findViewById(R.id.lib1_text1);
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mLib1TextView1);

        // use some of Guava's class as they should be accessible through the
        // classpath from the library
        Iterable<String> segments = MainActivity.split("-", mLib1TextView1.getText());
        assertEquals("SUCCESS", segments.iterator().next());
    }
}
