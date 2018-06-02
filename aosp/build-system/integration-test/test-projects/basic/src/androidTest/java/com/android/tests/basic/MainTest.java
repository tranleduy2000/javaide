package com.android.tests.basic;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.TextView;

import java.lang.RuntimeException;

/**
 * NB: All tests not annotated with @MediumTest will be ignored as the InstrumentationTestRunner
 * is passed the custom argument of "size medium".
 */
public class MainTest extends ActivityInstrumentationTestCase2<Main> {

    private TextView mTextView;

    /**
     * Creates an {@link ActivityInstrumentationTestCase2} that tests the {@link Main} activity.
     */
    public MainTest() {
        super(Main.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Main a = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(a);
        mTextView = (TextView) a.findViewById(R.id.text);

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
    }

    @MediumTest
    public void testBuildConfig() {
        assertEquals("bar", BuildConfig.FOO);
    }

    @SmallTest
    public void testSmallTestsShouldNotBeRun() {
        throw new RuntimeException("Should have been excluded by custom test instrumentation "
                + "runner argument.");
    }
}

