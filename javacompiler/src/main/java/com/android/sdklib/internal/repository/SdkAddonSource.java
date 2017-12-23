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

package com.android.sdklib.internal.repository;

import com.android.annotations.Nullable;
import com.android.sdklib.repository.SdkAddonConstants;

import org.w3c.dom.Document;

import java.io.InputStream;


/**
 * An sdk-addon source, i.e. a download site for addons and extra packages.
 * A repository describes one or more {@link Package}s available for download.
 */
public class SdkAddonSource extends SdkSource {

    /**
     * Constructs a new source for the given repository URL.
     * @param url The source URL. Cannot be null. If the URL ends with a /, the default
     *            repository.xml filename will be appended automatically.
     * @param uiName The UI-visible name of the source. Can be null.
     */
    public SdkAddonSource(String url, String uiName) {
        super(url, uiName);
    }

    /**
     * Returns true if this is an addon source.
     * We only load addons and extras from these sources.
     */
    @Override
    public boolean isAddonSource() {
        return true;
    }

    @Override
    protected String[] getDefaultXmlFileUrls() {
        return new String[] { SdkAddonConstants.URL_DEFAULT_FILENAME };
    }

    @Override
    protected int getNsLatestVersion() {
        return SdkAddonConstants.NS_LATEST_VERSION;
    }

    @Override
    protected String getNsUri() {
        return SdkAddonConstants.NS_URI;
    }

    @Override
    protected String getNsPattern() {
        return SdkAddonConstants.NS_PATTERN;
    }

    @Override
    protected String getSchemaUri(int version) {
        return SdkAddonConstants.getSchemaUri(version);
    }

    @Override
    protected String getRootElementName() {
        return SdkAddonConstants.NODE_SDK_ADDON;
    }

    @Override
    protected InputStream getXsdStream(int version) {
        return SdkAddonConstants.getXsdStream(version);
    }

    /**
     * There is no support forward evolution of the sdk-addon schema yet since we
     * currently have only one version.
     *
     * @param xml The input XML stream. Can be null.
     * @return Always null.
     * @null This implementation always return null.
     */
    @Override
    protected Document findAlternateToolsXml(@Nullable InputStream xml) {
        return null;
    }
}
