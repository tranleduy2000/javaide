/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.sdklib.internal.repository.DownloadCache;

import java.net.URL;
import java.util.Properties;

/**
 * Interface that a settings page must implement.
 *
 * @deprecated
 * com.android.sdklib.internal.repository has moved into Studio as
 * com.android.tools.idea.sdk.remote.internal.
 */
@Deprecated
public interface ISettingsPage {

    /**
     * Java system setting picked up by {@link URL} for http proxy port.
     * Type: String.
     */
    String KEY_HTTP_PROXY_PORT = "http.proxyPort";           //$NON-NLS-1$

    /**
     * Java system setting picked up by {@link URL} for http proxy host.
     * Type: String.
     */
    String KEY_HTTP_PROXY_HOST = "http.proxyHost";           //$NON-NLS-1$

    /**
     * Setting to force using http:// instead of https:// connections.
     * Type: Boolean.
     * Default: False.
     */
    String KEY_FORCE_HTTP = "sdkman.force.http";             //$NON-NLS-1$

    /**
     * Setting to display only packages that are new or updates.
     * Type: Boolean.
     * Default: True.
     */
    String KEY_SHOW_UPDATE_ONLY = "sdkman.show.update.only"; //$NON-NLS-1$

    /**
     * Setting to ask for permission before restarting ADB.
     * Type: Boolean.
     * Default: False.
     */
    String KEY_ASK_ADB_RESTART = "sdkman.ask.adb.restart";   //$NON-NLS-1$

    /**
     * Setting to use the {@link DownloadCache}, for small manifest XML files.
     * Type: Boolean.
     * Default: True.
     */
    String KEY_USE_DOWNLOAD_CACHE = "sdkman.use.dl.cache";   //$NON-NLS-1$

    /**
     * Setting to enabling previews in the package list
     * Type: Boolean.
     * Default: True.
     */
    String KEY_ENABLE_PREVIEWS = "sdkman.enable.previews2";   //$NON-NLS-1$

    /**
     * Setting to set the density of the monitor.
     * Type: Integer.
     * Default: -1
     */
    String KEY_MONITOR_DENSITY = "sdkman.monitor.density"; //$NON-NLS-1$

    /** Loads settings from the given {@link Properties} container and update the page UI. */
    void loadSettings(Properties inSettings);

    /** Called by the application to retrieve settings from the UI and store them in
     * the given {@link Properties} container. */
    void retrieveSettings(Properties outSettings);

    /**
     * Called by the application to give a callback that the page should invoke when
     * settings have changed.
     */
    void setOnSettingsChanged(SettingsChangedCallback settingsChangedCallback);

    /**
     * Callback used to notify the application that settings have changed and need to be
     * applied.
     */
    interface SettingsChangedCallback {
        /**
         * Invoked by the settings page when settings have changed and need to be
         * applied. The application will call {@link ISettingsPage#retrieveSettings(Properties)}
         * and apply the new settings.
         */
        void onSettingsChanged(ISettingsPage page);
    }
}
