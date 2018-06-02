package com.android.tests.basic;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.android.tests.basic.Main;

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
        Gson gson = new Gson();
        String jsonString = gson.toJson(Main.STRINGS);
        // assert that our gson strings is correct.
        assertEquals(jsonString, mTextView.getText());
    }
}

