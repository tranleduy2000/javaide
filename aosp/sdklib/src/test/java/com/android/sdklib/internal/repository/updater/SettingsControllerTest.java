/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sdklib.internal.repository.updater;

import com.android.sdklib.AndroidLocationTestCase;
import com.android.sdklib.internal.repository.updater.ISettingsPage.SettingsChangedCallback;
import com.android.sdklib.internal.repository.updater.SettingsController.OnChangedListener;
import com.android.sdklib.internal.repository.updater.SettingsController.Settings;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.mock.MockLog;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsControllerTest extends AndroidLocationTestCase {

    private IFileOp mFileOp;
    private MockLog mMockLog;
    private SettingsController m;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mFileOp = new FileOp();
        mMockLog = new MockLog();
        m = new SettingsController(mFileOp, mMockLog);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testDefaultSettings() {
        m.loadSettings();
        Settings s = m.getSettings();
        assertFalse(s.getAskBeforeAdbRestart());
        assertTrue(s.getEnablePreviews());
        assertFalse(s.getForceHttp());
        assertTrue (s.getShowUpdateOnly());
        assertTrue (s.getUseDownloadCache());
        assertEquals(-1, s.getMonitorDensity());
    }

    public final void testSetSettings() {
        m.loadSettings();

        Settings s1 = m.getSettings();
        assertFalse(s1.getAskBeforeAdbRestart());
        assertTrue (s1.getEnablePreviews());
        assertFalse(s1.getForceHttp());
        assertTrue (s1.getShowUpdateOnly());
        assertTrue (s1.getUseDownloadCache());
        assertEquals(-1, s1.getMonitorDensity());

        m.setSetting(ISettingsPage.KEY_ASK_ADB_RESTART, true);
        m.setSetting(ISettingsPage.KEY_ENABLE_PREVIEWS, false);
        m.setSetting(ISettingsPage.KEY_FORCE_HTTP, true);
        m.setShowUpdateOnly(false);
        m.setSetting(ISettingsPage.KEY_USE_DOWNLOAD_CACHE, false);
        m.setMonitorDensity(320);

        Settings s2 = m.getSettings();
        assertSame(s2, s1);
        assertTrue (s2.getAskBeforeAdbRestart());
        assertFalse(s2.getEnablePreviews());
        assertTrue (s2.getForceHttp());
        assertFalse(s2.getShowUpdateOnly());
        assertFalse(s2.getUseDownloadCache());
        assertEquals(320, s2.getMonitorDensity());

        m.saveSettings();

        // create a new instance
        SettingsController m3 = new SettingsController(mFileOp, mMockLog);
        m3.loadSettings();

        Settings s3 = m3.getSettings();
        assertNotSame(s3, s1);
        assertTrue (s3.getAskBeforeAdbRestart());
        assertFalse(s3.getEnablePreviews());
        assertTrue (s3.getForceHttp());
        assertFalse(s3.getShowUpdateOnly());
        assertFalse(s3.getUseDownloadCache());
        assertEquals(320, s3.getMonitorDensity());
    }

    public final void testSettingsPage() {
        final AtomicBoolean pageLoadSettingsCalled = new AtomicBoolean(false);
        final AtomicBoolean pageRetrieveSettingsCalled = new AtomicBoolean(false);
        final AtomicBoolean pageSetOnSettingsChangedCalled = new AtomicBoolean(false);
        final AtomicReference<SettingsChangedCallback> pageSettingsChangedCallback =
            new AtomicReference<ISettingsPage.SettingsChangedCallback>();

        ISettingsPage mockPage = new ISettingsPage() {
            @Override
            public void loadSettings(Properties inSettings) {
                pageLoadSettingsCalled.set(true);
            }

            @Override
            public void setOnSettingsChanged(SettingsChangedCallback settingsChangedCallback) {
                pageSetOnSettingsChangedCalled.set(true);
                pageSettingsChangedCallback.set(settingsChangedCallback);
            }

            @Override
            public void retrieveSettings(Properties outSettings) {
                pageRetrieveSettingsCalled.set(true);
            }
        };

        // Setting the page loads settings into it and then registers a changed-callback
        // that will call m.onSettingsChanged.
        m.setSettingsPage(mockPage);
        assertTrue(pageLoadSettingsCalled.get());
        assertTrue(pageSetOnSettingsChangedCalled.get());
        assertFalse(pageRetrieveSettingsCalled.get());

        final AtomicBoolean listener1Called = new AtomicBoolean(false);

        OnChangedListener listener1 = new OnChangedListener() {
            @Override
            public void onSettingsChanged(SettingsController controller, Settings oldSettings) {
                listener1Called.set(true);
            }
        };

        final AtomicBoolean listener2Called = new AtomicBoolean(false);

        OnChangedListener listener2 = new OnChangedListener() {
            @Override
            public void onSettingsChanged(SettingsController controller, Settings oldSettings) {
                listener1Called.set(true);
            }
        };

        m.registerOnChangedListener(listener1);
        m.registerOnChangedListener(listener2);
        m.unregisterOnChangedListener(listener2);
        m.unregisterOnChangedListener(listener2);
        assertFalse(listener1Called.get());
        assertFalse(listener2Called.get());

        // When the settings page changes, it calls the callback that it was given
        // (which we captured earlier)
        assertNotNull(pageSettingsChangedCallback.get());
        pageSettingsChangedCallback.get().onSettingsChanged(mockPage);
        // That triggers SettingsController.onSettingsChanged which calls retrieve() on the
        // page to get the settings and save them and then call all the registered listeners
        // with the settings.
        assertTrue(pageRetrieveSettingsCalled.get());
        assertTrue(listener1Called.get());
        assertFalse(listener2Called.get());

    }

}
