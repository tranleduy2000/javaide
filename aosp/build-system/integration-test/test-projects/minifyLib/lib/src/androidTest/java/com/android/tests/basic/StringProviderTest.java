package com.android.tests.basic;

import junit.framework.TestCase;

public class StringProviderTest extends TestCase {

    public void testGetString() {
        assertEquals("123", StringProvider.getString(123));
    }
}
