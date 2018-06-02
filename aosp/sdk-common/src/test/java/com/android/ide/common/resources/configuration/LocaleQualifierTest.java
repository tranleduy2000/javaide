/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.common.resources.configuration;

import static com.android.ide.common.resources.configuration.LocaleQualifier.FAKE_VALUE;
import static com.android.ide.common.resources.configuration.LocaleQualifier.getQualifier;
import static com.android.ide.common.resources.configuration.LocaleQualifier.isNormalizedCase;
import static com.android.ide.common.resources.configuration.LocaleQualifier.normalizeCase;
import static com.android.ide.common.resources.configuration.LocaleQualifier.parseBcp47;

import junit.framework.TestCase;

import java.util.Locale;

public class LocaleQualifierTest extends TestCase {

    private FolderConfiguration config;
    private LocaleQualifier lq;

    @Override
    public void setUp()  throws Exception {
        super.setUp();
        config = new FolderConfiguration();
        lq = new LocaleQualifier();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
        lq = null;
    }

    public void testCheckAndSet() {
        assertEquals(true, lq.checkAndSet("b+kok", config)); //$NON-NLS-1$
        assertTrue(config.getLocaleQualifier() != null);
        assertEquals("kok", config.getLocaleQualifier().toString()); //$NON-NLS-1$
    }

    public void testCheckAndSetCaseInsensitive() {
        assertEquals(true, lq.checkAndSet("b+KOK", config)); //$NON-NLS-1$
        assertTrue(config.getLocaleQualifier() != null);
        assertEquals("kok", config.getLocaleQualifier().toString()); //$NON-NLS-1$
    }

    public void testFailures() {
        assertEquals(false, lq.checkAndSet("", config)); //$NON-NLS-1$
        assertEquals(false, lq.checkAndSet("abcd", config)); //$NON-NLS-1$
        assertEquals(false, lq.checkAndSet("en-USofA", config)); //$NON-NLS-1$
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetQualifier() {
        assertNull(getQualifier("v4")); // version qualifier shouldn't match

        assertNull(getQualifier(""));
        assertEquals("en", getQualifier("en").getLanguage());
        assertNull(getQualifier("en").getRegion());
        assertNull(getQualifier("en").getScript());
        assertEquals("en", getQualifier("EN").getLanguage());
        assertEquals("en", getQualifier("EN").getFull());

        assertEquals("en", getQualifier("en-rUS").getLanguage());
        assertEquals("US", getQualifier("en-rUS").getRegion());
        assertNull(getQualifier("en-rUS").getScript());
        assertEquals("en", getQualifier("EN-RUS").getLanguage());
        assertEquals("en", getQualifier("EN-RUS").getLanguage());
        assertEquals("US", getQualifier("EN-RUS").getRegion());
        assertNull(getQualifier("EN-RUS").getScript());
        assertEquals("en-rUS", getQualifier("EN-RUS").getFull());

        assertEquals("eng", getQualifier("eng").getLanguage());
        assertNull(getQualifier("eng").getRegion());
        assertNull(getQualifier("eng").getScript());
        assertEquals("eng", getQualifier("ENG").getLanguage());
        assertEquals("eng", getQualifier("ENG").getFull());

        assertEquals("foo", getQualifier("foo").getLanguage());
        assertNull(getQualifier("car")); // car mode: not recognized as language

        assertEquals("eng", getQualifier("eng-rUS").getLanguage());
        assertEquals("US", getQualifier("eng-rUS").getRegion());
        assertNull(getQualifier("eng-rUS").getScript());
        assertEquals("eng", getQualifier("ENG-RUS").getLanguage());
        assertEquals("eng", getQualifier("ENG-RUS").getLanguage());
        assertEquals("US", getQualifier("ENG-RUS").getRegion());
        assertNull(getQualifier("ENG-RUS").getScript());
        assertEquals("eng-rUS", getQualifier("ENG-RUS").getFull());
        assertNull(getQualifier("eng-rUSA"));

        assertNull(getQualifier("kok-rIND"));
        assertEquals("kok", getQualifier("b+kok").getLanguage());
        assertNull(getQualifier("b+kok").getRegion());
        assertEquals("kok", getQualifier("b+kok+VARIANT").getLanguage());
        assertNull(getQualifier("b+kok+VARIANT").getRegion());
        assertEquals("kok", getQualifier("b+kok+Knda+419+VARIANT").getLanguage());
        assertEquals("419", getQualifier("b+kok+Knda+419+VARIANT").getRegion());
        assertEquals("Knda", getQualifier("b+kok+Knda+419+VARIANT").getScript());
        assertEquals("kok", getQualifier("b+kok+VARIANT").getLanguage());
        assertNull(getQualifier("b+kok+VARIANT").getRegion());
        assertEquals("kok", getQualifier("b+kok+IN").getLanguage());
        assertEquals("IN", getQualifier("b+kok+IN").getRegion());
        assertEquals("kok", getQualifier("b+kok+Knda").getLanguage());
        assertNull(getQualifier("b+kok+Knda").getRegion());
        assertEquals("kok", getQualifier("b+kok+Knda+419").getLanguage());
        assertEquals("419", getQualifier("b+kok+Knda+419").getRegion());
        assertEquals("b+kok+Knda+419", getQualifier("b+KOK+knda+419").getFull());
    }

    public void testSetRegion() {
        LocaleQualifier qualifier = getQualifier("en");
        assertNotNull(qualifier);
        qualifier.setRegionSegment("rUS");
        assertEquals("en", qualifier.getLanguage());
        assertEquals("US", qualifier.getRegion());
        assertEquals("en-rUS", qualifier.getFull());

        // Case check
        qualifier = getQualifier("EN");
        assertNotNull(qualifier);
        qualifier.setRegionSegment("Rus");
        assertEquals("en", qualifier.getLanguage());
        assertEquals("US", qualifier.getRegion());
        assertEquals("en-rUS", qualifier.getFull());

        // 3 letter language
        qualifier = getQualifier("eng");
        assertNotNull(qualifier);
        qualifier.setRegionSegment("rUS");
        assertEquals("eng", qualifier.getLanguage());
        assertEquals("US", qualifier.getRegion());
        assertEquals("eng-rUS", qualifier.getFull());
    }

    public void testEquals() {
        LocaleQualifier qualifier1 = getQualifier("b+KOK+knda+419");
        LocaleQualifier qualifier2 = getQualifier("b+kok+knda+419");
        assertNotNull(qualifier1);
        assertNotNull(qualifier2);
        assertEquals(qualifier1, qualifier2);
        assertEquals(qualifier2, qualifier1);

        qualifier2 = getQualifier("b+kok+knda");
        assertNotNull(qualifier2);
        assertFalse(qualifier1.equals(qualifier2));
        assertFalse(qualifier2.equals(qualifier1));

        // Equivalent, with different syntax
        qualifier1 = getQualifier("b+en+US");
        qualifier2 = getQualifier("en-rUS");
        assertNotNull(qualifier1);
        assertNotNull(qualifier1);
        assertNotNull(qualifier2);
        assertEquals(qualifier1, qualifier2);
        assertEquals(qualifier2, qualifier1);

        qualifier1 = getQualifier("b+eng+US");
        qualifier2 = getQualifier("eng-rUS");
        assertNotNull(qualifier1);
        assertNotNull(qualifier1);
        assertNotNull(qualifier2);
        assertEquals(qualifier1, qualifier2);
        assertEquals(qualifier2, qualifier1);
    }

    @SuppressWarnings("ConstantConditions")
    public void testParseBcp47() {

        assertNull(parseBcp47("kok-rIN"));
        assertEquals("kok", parseBcp47("b+kok").getLanguage());
        assertNull(parseBcp47("b+kok").getRegion());

        assertEquals("kok", parseBcp47("b+kok+VARIANT").getLanguage());
        assertNull(parseBcp47("b+kok+VARIANT").getRegion());

        assertEquals("kok", parseBcp47("b+kok+Knda+419+VARIANT").getLanguage());
        assertEquals("419", parseBcp47("b+kok+Knda+419+VARIANT").getRegion());
        assertEquals("Knda", parseBcp47("b+kok+Knda+419+VARIANT").getScript());

        assertEquals("kok", parseBcp47("b+kok+VARIANT").getLanguage());
        assertNull(parseBcp47("b+kok+VARIANT").getRegion());

        assertEquals("kok", parseBcp47("b+kok+IN").getLanguage());
        assertEquals("IN", parseBcp47("b+kok+IN").getRegion());

        assertEquals("kok", parseBcp47("b+kok+Knda").getLanguage());
        assertEquals("Knda", parseBcp47("b+kok+Knda").getScript());
        assertNull(parseBcp47("b+kok+Knda").getRegion());

        assertEquals("kok", parseBcp47("b+kok+Knda+419").getLanguage());
        assertEquals("419", parseBcp47("b+kok+Knda+419").getRegion());
        assertEquals("Knda", parseBcp47("b+kok+Knda+419").getScript());
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetLanguageAndGetRegion() {
        assertEquals(true, lq.checkAndSet("b+kok", config)); //$NON-NLS-1$
        assertEquals("kok", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertNull("kok", config.getLocaleQualifier().getRegion());

        assertEquals(true, lq.checkAndSet("b+kok+VARIANT", config)); //$NON-NLS-1$
        assertEquals("b+kok+variant", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertNull("kok", config.getLocaleQualifier().getRegion());

        assertEquals(true, lq.checkAndSet("b+kok+Knda+419+VARIANT", config)); //$NON-NLS-1$
        assertEquals("b+kok+Knda+419+variant", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertEquals("419", config.getLocaleQualifier().getRegion());

        assertEquals(true, lq.checkAndSet("b+kok+IN", config)); //$NON-NLS-1$
        assertEquals("kok-rIN", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertEquals("IN", config.getLocaleQualifier().getRegion());

        assertEquals(true, lq.checkAndSet("b+kok+Knda", config)); //$NON-NLS-1$
        assertEquals("b+kok+Knda", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertNull(config.getLocaleQualifier().getRegion());

        assertEquals(true, lq.checkAndSet("b+kok+Knda+419", config)); //$NON-NLS-1$
        assertEquals("b+kok+Knda+419", config.getLocaleQualifier().getValue());
        assertEquals("kok", config.getLocaleQualifier().getLanguage());
        assertEquals("419", config.getLocaleQualifier().getRegion());
    }

    public void testIsNormalCase() {
        // Language
        assertFalse(isNormalizedCase("LL"));
        assertFalse(isNormalizedCase("Ll"));
        assertFalse(isNormalizedCase("lL"));
        assertFalse(isNormalizedCase("LLL"));
        assertTrue(isNormalizedCase("ll"));
        assertTrue(isNormalizedCase("lll"));

        // Language + Region
        assertFalse(isNormalizedCase("LL-rRR"));
        assertFalse(isNormalizedCase("ll-rrr"));
        assertFalse(isNormalizedCase("LL-rrr"));
        assertFalse(isNormalizedCase("ll-RRR"));
        assertFalse(isNormalizedCase("lL-frR"));
        assertFalse(isNormalizedCase("Ll-fRr"));
        assertFalse(isNormalizedCase("llL-frr"));
        assertTrue(isNormalizedCase("ll-rRR"));
        assertTrue(isNormalizedCase("lll-rRR"));

        // BCP 47
        assertFalse(isNormalizedCase("b+en+CA+x+ca".toLowerCase(Locale.US)));
        assertTrue(isNormalizedCase("b+en+CA+x+ca"));
        assertFalse(isNormalizedCase("b+sgn+BE+FR".toLowerCase(Locale.US)));
        assertTrue(isNormalizedCase("b+sgn+BE+FR"));
        assertFalse(isNormalizedCase("b+az+Latn+x+latn".toLowerCase(Locale.US)));
        assertTrue(isNormalizedCase("b+az+Latn+x+latn"));
        assertFalse(isNormalizedCase("b+MN+cYRL+mn".toLowerCase(Locale.US)));
        assertTrue(isNormalizedCase("b+mn+Cyrl+MN"));
        assertFalse(isNormalizedCase("b+zh+CN+a+myext+x+private".toLowerCase(Locale.US)));
        assertTrue(isNormalizedCase("b+zh+CN+a+myext+x+private"));
    }

    public void testNormalizeCase() {
        assertEquals("bb", normalizeCase("BB"));
        assertEquals("ll-rRR", normalizeCase("LL-Rrr"));
        assertEquals("lll-rRR", normalizeCase("LLL-Rrr"));

        assertEquals("b+en+CA+x+ca", normalizeCase("b+en+CA+x+ca".toLowerCase(Locale.US)));
        assertEquals("b+sgn+BE+FR", normalizeCase("b+sgn+BE+FR".toLowerCase(Locale.US)));
        assertEquals("b+az+Latn+x+latn", normalizeCase("b+az+Latn+x+latn".toLowerCase(Locale.US)));
        assertEquals("b+mn+Cyrl+MN", normalizeCase("b+MN+cYRL+mn".toLowerCase(Locale.US)));
        assertEquals("b+zh+CN+a+myext+x+private", normalizeCase(
                "b+zh+CN+a+myext+x+private".toLowerCase(Locale.US)));
    }

    @SuppressWarnings("ConstantConditions")
    public void testIsMatchFor() {
        assertTrue(getQualifier("en").isMatchFor(getQualifier("en")));
        assertFalse(getQualifier("en").isMatchFor(getQualifier("fr")));
        assertFalse(getQualifier("fr").isMatchFor(getQualifier("en")));

        assertTrue(getQualifier("en-rUS").isMatchFor(getQualifier("en-rUS")));
        assertFalse(getQualifier("en-rUS").isMatchFor(getQualifier("en-rGB")));
        assertFalse(getQualifier("en-rGB").isMatchFor(getQualifier("en-rUS")));
        assertFalse(getQualifier("fr-rGB").isMatchFor(getQualifier("en-rGB")));

        assertTrue(getQualifier("en-rUS").isMatchFor(getQualifier("en-rUS")));
        assertTrue(getQualifier("en-rUS").isMatchFor(getQualifier("en")));
        assertTrue(getQualifier("b+en+US").isMatchFor(getQualifier("b+en+US")));
        assertTrue(getQualifier("b+en+US").isMatchFor(getQualifier("b+en")));
        assertTrue(getQualifier("b+en+US").isMatchFor(getQualifier("en")));

        assertTrue(getQualifier("b+en+US").isMatchFor(getQualifier("b+en+US")));
        assertTrue(getQualifier("b+en+Knda+US").isMatchFor(getQualifier("b+en+Knda+US")));
        assertTrue(getQualifier("b+en+Knda+US").isMatchFor(getQualifier("b+en")));

        // Apparently isMatchFor is a bit more general than you would think; it
        // can't restrict as shown in these two conditions because then other
        // configuration matching code will fail
        //assertFalse(getQualifier("en").isMatchFor(getQualifier("en-rUS")));
        //assertFalse(getQualifier("b+en").isMatchFor(getQualifier("b+en+Knda+US")));
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetTag() {
        assertEquals("en-CA", getQualifier("b+en+CA".toLowerCase(Locale.US)).getTag());
        assertEquals("eng-CA", getQualifier("b+eng+CA".toLowerCase(Locale.US)).getTag());
        assertEquals("en-CA-x-ca", getQualifier("b+en+CA+x+ca".toLowerCase(Locale.US)).getTag());
        assertEquals("en", getQualifier("EN").getTag());
        assertEquals("en-US", getQualifier("EN-rUS").getTag());
    }

    public void testHasLanguage() {
        //noinspection ConstantConditions
        assertTrue(LocaleQualifier.getQualifier("b+en+CA+x+ca").hasLanguage());
        assertTrue(new LocaleQualifier("en").hasLanguage());
        assertFalse(new LocaleQualifier(FAKE_VALUE).hasLanguage());
    }

    public void testHasRegion() {
        //noinspection ConstantConditions
        assertTrue(LocaleQualifier.getQualifier("b+en+CA+x+ca").hasRegion());
        //noinspection ConstantConditions
        assertFalse(LocaleQualifier.getQualifier("b+en").hasRegion());
        assertFalse(new LocaleQualifier(FAKE_VALUE).hasRegion());
        assertFalse(new LocaleQualifier("", FAKE_VALUE, FAKE_VALUE, null).hasRegion());
    }
}