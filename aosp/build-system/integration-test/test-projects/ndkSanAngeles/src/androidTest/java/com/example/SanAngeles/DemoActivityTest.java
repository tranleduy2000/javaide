package com.example.SanAngeles;

import android.test.ActivityInstrumentationTestCase2;

public class DemoActivityTest extends ActivityInstrumentationTestCase2<DemoActivity> {

    public DemoActivityTest() {
        super(DemoActivity.class);
    }

    public void testGlView() {
        assertNotNull(getActivity().mGLView);
    }
}
