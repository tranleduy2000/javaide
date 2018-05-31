/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.repository;


import java.io.InputStream;

/**
 * Public constants for the sdk-stats XML Schema.
 */
public class SdkStatsConstants {

    /** The canonical URL filename for addons-list XML files. */
    public static final String URL_DEFAULT_FILENAME = "stats-1.xml";                //$NON-NLS-1$

    /** The URL where to find the official addons list fle. */
    public static final String URL_STATS =
        SdkRepoConstants.URL_GOOGLE_SDK_SITE + URL_DEFAULT_FILENAME;

    /** The base of our sdk-addons-list XML namespace. */
    private static final String NS_BASE =
        "http://schemas.android.com/sdk/android/stats/";                            //$NON-NLS-1$

    /**
     * The pattern of our sdk-stats XML namespace.
     * Matcher's group(1) is the schema version (integer).
     */
    public static final String NS_PATTERN = NS_BASE + "([1-9][0-9]*)";              //$NON-NLS-1$

    /** The latest version of the sdk-stats XML Schema.
     *  Valid version numbers are between 1 and this number, included. */
    public static final int NS_LATEST_VERSION = 1;

    /** The XML namespace of the latest sdk-stats XML. */
    public static final String NS_URI = getSchemaUri(NS_LATEST_VERSION);

    /** The root sdk-stats element */
    public static final String NODE_SDK_STATS = "sdk-stats";                        //$NON-NLS-1$

    /** A platform stat. */
    public static final String NODE_PLATFORM = "platform";                          //$NON-NLS-1$

    /** The Android API Level for the platform. An int > 0. */
    public static final String NODE_API_LEVEL = "api-level";                        //$NON-NLS-1$

    /** The official codename for this platform, for example "Cupcake". */
    public static final String NODE_CODENAME = "codename";                          //$NON-NLS-1$

    /** The official version name of this platform, for example "Android 1.5". */
    public static final String NODE_VERSION = "version";                            //$NON-NLS-1$

    /**
     * The <em>approximate</em> share percentage of that platform.
     * See the caveat in sdk-stats-1.xsd about value freshness and accuracy.
     */
    public static final String NODE_SHARE = "share";                                //$NON-NLS-1$

    /**
     * Returns a stream to the requested sdk-stats XML Schema.
     *
     * @param version Between 1 and {@link #NS_LATEST_VERSION}, included.
     * @return An {@link InputStream} object for the local XSD file or
     *         null if there is no schema for the requested version.
     */
    public static InputStream getXsdStream(int version) {
        String filename = String.format("sdk-stats-%d.xsd", version);       //$NON-NLS-1$
        return SdkStatsConstants.class.getResourceAsStream(filename);
    }

    /**
     * Returns the URI of the sdk-stats schema for the given version number.
     * @param version Between 1 and {@link #NS_LATEST_VERSION} included.
     */
    public static String getSchemaUri(int version) {
        return String.format(NS_BASE + "%d", version);                      //$NON-NLS-1$
    }
}
