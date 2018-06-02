package com.example.android.multiproject.library.base.test;

import com.sample.android.multiproject.library.PersonView;

import android.test.ActivityInstrumentationTestCase2;

public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    public TestActivityTest() {
        super(TestActivity.class);
    }

    public void testPreconditions() {
        TestActivity activity = getActivity();
        PersonView view = (PersonView) activity.findViewById(R.id.view);

        assertNotNull(view);
        assertEquals(20.0f, view.getTextSize());
    }
}

