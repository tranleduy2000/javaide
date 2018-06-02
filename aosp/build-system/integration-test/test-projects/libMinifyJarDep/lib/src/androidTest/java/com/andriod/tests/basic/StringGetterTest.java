package com.andriod.tests.basic;

import com.android.tests.basic.StringGetter;

import junit.framework.TestCase;

public class StringGetterTest extends TestCase {

    public void testGetString() {
        assertEquals("FredBarney", StringGetter.getString());
    }
}
