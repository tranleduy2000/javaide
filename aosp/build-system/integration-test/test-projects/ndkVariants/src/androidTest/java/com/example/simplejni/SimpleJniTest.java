package com.example.simplejni;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.example.hellojni.HelloJniTest \
 * com.example.hellojni.tests/android.test.InstrumentationTestRunner
 */
public class SimpleJniTest extends ActivityInstrumentationTestCase<SimpleJni> {

    public SimpleJniTest() {
        super("com.example.simple", SimpleJni.class);
    }


    public void testJniName() {
        final SimpleJni a = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(a);

        assertTrue(TestConstants.PRODUCT_FLAVOR.equals(a.productFlavorFromJni()));

        // This test is expected to be run in debug build.
        assertTrue("debug".equals(a.buildTypeFromJni()));
    }
}
