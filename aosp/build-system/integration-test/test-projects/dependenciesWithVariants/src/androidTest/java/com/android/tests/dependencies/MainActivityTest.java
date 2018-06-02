package com.android.tests.dependencies;

import com.android.tests.dependencies.jar.StringHelper;
import com.android.tests.dependencies.jar.StringHelper2;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.TextView;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TextView mTextView;

    /**
     * Creates an {@link ActivityInstrumentationTestCase2} that tests the {@link MainActivity} activity.
     */
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final MainActivity a = getActivity();
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
    @SmallTest
    public void testPreconditions() {
        assertNotNull(mTextView);
    }

    @SmallTest
    public void testMainActivity() {
        assertEquals("Foo-helper", mTextView.getText().toString());
    }

    @SmallTest
    public void testIndirectDependencies() {
        assertEquals("Foo-helper", StringHelper.getString("Foo"));
    }

    @SmallTest
    public void testDirectDependencies() {
        assertEquals("Foo-helper", StringHelper2.getString2("Foo"));
    }
}

