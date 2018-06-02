/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.ide.common.xml;

import com.android.ide.common.xml.ManifestData.UsesFeature;
import com.android.ide.common.xml.ManifestData.UsesLibrary;
import com.android.resources.Keyboard;
import com.android.resources.Navigation;
import com.android.resources.TouchScreen;

import junit.framework.TestCase;

import java.io.InputStream;

/**
 * Tests for {@link AndroidManifestParser}
 */
public class AndroidManifestParserTest extends TestCase {
    private ManifestData mManifestTestApp;
    private ManifestData mManifestInstrumentation;

    private static final String TESTDATA_PATH =
        "/com/android/sdklib/testdata/";  //$NON-NLS-1$
    private static final String INSTRUMENTATION_XML = TESTDATA_PATH +
        "AndroidManifest-instrumentation.xml";  //$NON-NLS-1$
    private static final String TESTAPP_XML = TESTDATA_PATH +
        "AndroidManifest-testapp.xml";  //$NON-NLS-1$
    private static final String ACTIVITY_ALIAS_XML = TESTDATA_PATH +
            "AndroidManifest-activityalias.xml";  //$NON-NLS-1$
    private static final String PACKAGE_NAME =  "com.android.testapp"; //$NON-NLS-1$
    private static final Integer VERSION_CODE = 42;
    private static final String ACTIVITY_NAME = "com.android.testapp.MainActivity"; //$NON-NLS-1$
    private static final String LIBRARY_NAME = "android.test.runner"; //$NON-NLS-1$
    private static final String LIBRARY_NAME2 = "android.test.runner2"; //$NON-NLS-1$
    private static final String FEATURE_NAME = "com.foo.feature"; //$NON-NLS-1$
    private static final String INSTRUMENTATION_NAME = "android.test.InstrumentationTestRunner"; //$NON-NLS-1$
    private static final String INSTRUMENTATION_TARGET = "com.android.AndroidProject"; //$NON-NLS-1$

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        InputStream manifestStream = this.getClass().getResourceAsStream(TESTAPP_XML);

        mManifestTestApp = AndroidManifestParser.parse(manifestStream);
        assertNotNull(mManifestTestApp);

        manifestStream = this.getClass().getResourceAsStream(INSTRUMENTATION_XML);
        mManifestInstrumentation = AndroidManifestParser.parse(manifestStream);
        assertNotNull(mManifestInstrumentation);
    }

    public void testGetInstrumentationInformation() {
        assertEquals(1, mManifestInstrumentation.getInstrumentations().length);
        assertEquals(INSTRUMENTATION_NAME,
                mManifestInstrumentation.getInstrumentations()[0].getName());
        assertEquals(INSTRUMENTATION_TARGET,
                mManifestInstrumentation.getInstrumentations()[0].getTargetPackage());
    }

    public void testGetPackage() {
        assertEquals(PACKAGE_NAME, mManifestTestApp.getPackage());
    }

    public void testGetVersionCode() {
        assertEquals(VERSION_CODE, mManifestTestApp.getVersionCode());
        assertEquals(null, mManifestInstrumentation.getVersionCode());
    }

    public void testMinSdkVersion() {
        assertEquals(7, mManifestTestApp.getMinSdkVersion());
        assertEquals(8, mManifestTestApp.getTargetSdkVersion());

        assertEquals("foo", mManifestInstrumentation.getMinSdkVersionString());
        assertEquals(ManifestData.MIN_SDK_CODENAME, mManifestInstrumentation.getMinSdkVersion());
    }

    public void testGetActivities() {
        assertEquals(1, mManifestTestApp.getActivities().length);
        ManifestData.Activity activity = mManifestTestApp.getActivities()[0];
        assertEquals(ACTIVITY_NAME, activity.getName());
        assertTrue(activity.hasAction());
        assertTrue(activity.isHomeActivity());
        assertTrue(activity.hasAction());
        assertEquals(activity, mManifestTestApp.getActivities()[0]);
    }

    public void testGetLauncherActivity() {
        ManifestData.Activity activity = mManifestTestApp.getLauncherActivity();
        assertEquals(ACTIVITY_NAME, activity.getName());
        assertTrue(activity.hasAction());
        assertTrue(activity.isHomeActivity());
    }

    public void testSupportsScreen() {
        ManifestData.SupportsScreens supportsScreens =
            mManifestTestApp.getSupportsScreensFromManifest();

        assertNotNull(supportsScreens);
        assertEquals(Boolean.TRUE, supportsScreens.getAnyDensity());
        assertEquals(Boolean.TRUE, supportsScreens.getResizeable());
        assertEquals(Boolean.TRUE, supportsScreens.getSmallScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getNormalScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getLargeScreens());
    }

    public void testUsesConfiguration() {
        ManifestData.UsesConfiguration usesConfig = mManifestTestApp.getUsesConfiguration();

        assertNotNull(usesConfig);
        assertEquals(Boolean.TRUE, usesConfig.getReqFiveWayNav());
        assertEquals(Navigation.NONAV, usesConfig.getReqNavigation());
        assertEquals(Boolean.TRUE, usesConfig.getReqHardKeyboard());
        assertEquals(Keyboard.TWELVEKEY, usesConfig.getReqKeyboardType());
        assertEquals(TouchScreen.FINGER, usesConfig.getReqTouchScreen());
    }

    private void assertEquals(ManifestData.Activity lhs, ManifestData.Activity rhs) {
        assertTrue(lhs == rhs || (lhs != null && rhs != null));
        if (lhs != null && rhs != null) {
            assertEquals(lhs.getName(),        rhs.getName());
            assertEquals(lhs.isExported(),     rhs.isExported());
            assertEquals(lhs.hasAction(),      rhs.hasAction());
            assertEquals(lhs.isHomeActivity(), rhs.isHomeActivity());
        }
    }

    public void testGetUsesLibraries() {
        UsesLibrary[] libraries = mManifestTestApp.getUsesLibraries();

        assertEquals(2,             libraries.length);
        assertEquals(LIBRARY_NAME,  libraries[0].getName());
        assertEquals(Boolean.FALSE, libraries[0].getRequired());
        assertEquals(LIBRARY_NAME2, libraries[1].getName());
        assertEquals(Boolean.TRUE,  libraries[1].getRequired());
    }

    public void testGetUsesFeatures() {
        UsesFeature[] features = mManifestTestApp.getUsesFeatures();

        assertEquals(2,            features.length);
        assertEquals(0x00020001,   features[0].mGlEsVersion);
        assertEquals(Boolean.TRUE, features[0].getRequired());
        assertEquals(FEATURE_NAME, features[1].getName());
        assertEquals(Boolean.TRUE, features[1].getRequired());
    }

    public void testGetPackageName() {
        assertEquals(PACKAGE_NAME, mManifestTestApp.getPackage());
    }

    public void testActivityAlias() throws Exception {
        InputStream manifestStream = this.getClass().getResourceAsStream(ACTIVITY_ALIAS_XML);

        ManifestData manifest = AndroidManifestParser.parse(manifestStream);
        assertNotNull(manifest);

        assertEquals(manifest.getLauncherActivity().getName(),
                "com.android.testapp.AliasActivity"); //$NON-NLS-1$
    }
}
