package com.example.combinedSplits;

import android.test.ActivityInstrumentationTestCase;

public class DemoActivityTest extends ActivityInstrumentationTestCase<DemoActivity> {

    public DemoActivityTest() {
        super("com.example.SanAngeles", DemoActivity.class);
    }


    public void testJniName() {
        final DemoActivity a = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(a);

        assertFalse("unknown".equals(a.jniNameFromJNI()));
    }
}
