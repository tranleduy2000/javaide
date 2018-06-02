package com.android.tests.basic;

import com.google.common.collect.ImmutableList;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

public class MainTest extends ActivityInstrumentationTestCase2<Main> {

    private ImmutableList<TextView> mTextView;

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
        // Wrapped in an immutable list from guava, to check the dependency worked.
        mTextView = ImmutableList.of((TextView) a.findViewById(R.id.text))
    }

    @Override
    public void tearDown() throws Exception {
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
        assertEquals(1, mTextView.size());
        assertNotNull(mTextView.get(0));
    }
}

