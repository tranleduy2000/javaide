package com.android.tests.basic;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.List;

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

    @MediumTest
    public void testTextView() {
        List<String> list = Lists.newArrayList("foo", "bar");

        assertEquals(mTextView.getText(), list.toString());
    }

    @MediumTest
    public void testPreconditions() {
        assertNotNull(mTextView);
    }

}

