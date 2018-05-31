/*
 * Copyright (C) 2014 The Android Open Source Project
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

/**
 * This class describes the properties that can appear in an add-on's manifest.ini file.
 * <p/>
 * These constants are public and part of the SDK Manager public API.
 * Once published we can't change them arbitrarily since various parts
 * of our build process depend on them.
 */
public class AddonManifestIniProps {

    /**
     * The <em>display</em> name of the add-on. Always present. <br/>
     * In source.properties, this matches {@link PkgProps#ADDON_NAME_DISPLAY}.
     */
    public static final String ADDON_NAME         = "name";                 //$NON-NLS-1$

    /**
     * The optional "name id" of the add-on. <br/>
     * In source.properties, this matches {@link PkgProps#ADDON_NAME_ID}.
     * <p/>
     * Historically the manifest used to have only a 'name' property for both internal unique id
     * and display, in which case the internal id was synthesized using the display name and
     * matching a {@code [a-zA-Z0-9_-]+} pattern (see {@code Addonpackage#sanitizeDisplayToNameId}
     * for details.)
     */
    public static final String ADDON_NAME_ID      = "name-id";              //$NON-NLS-1$

    /**
     * The <em>display</em> vendor of the add-on. Always present. <br/>
     * In source.properties, this matches {@link PkgProps#ADDON_VENDOR_DISPLAY}.
     */
    public static final String ADDON_VENDOR       = "vendor";               //$NON-NLS-1$

    /**
     * The optional vendor id of the add-on. <br/>
     * In source.properties, this matches {@link PkgProps#ADDON_VENDOR_ID}.
     * <p/>
     * Historically the manifest used to have only a 'vendor' property for both internal unique id
     * and display, in which case the internal id was synthesized using the display name and
     * matching a {@code [a-zA-Z0-9_-]+} pattern (see {@code Addonpackage#sanitizeDisplayToNameId}
     * for details.)
     */
    public static final String ADDON_VENDOR_ID    = "vendor-id";            //$NON-NLS-1$

    /**
     * The free description string of the add-on. <br/>
     * Not saved in source.properties.
     */
    public static final String ADDON_DESCRIPTION  = "description";          //$NON-NLS-1$

    /**
     * The revision of the add-on. <br/>
     * In source.properties, this matches {@link PkgProps#PKG_REVISION}.
     */
    public static final String ADDON_REVISION     = "revision";             //$NON-NLS-1$

    /**
     * An older/obsolete attribute for the revision of the add-on. <br/>
     * The name was changed as it is ambiguous (platform version vs platform revision.)
     */
    public static final String ADDON_REVISION_OLD = "version";              //$NON-NLS-1$

    /**
     * The API level of the add-on, always an integer. <br/>
     * <em>Note: add-ons do not currently support API codenames. </em> <br/>
     * In source.properties, this matches {@link PkgProps#VERSION_API_LEVEL}.
     */
    public static final String ADDON_API          = "api";                  //$NON-NLS-1$

    /**
     * The list of libraries of the add-on. <br/>
     * This is a string in the format "java.package1;java.package2;...java.packageN".
     * For each library's java package name, the manifest.ini contains a key with
     * value "library.jar;Jar Description String". Example:
     * <pre>
     * libraries=com.example.foo;com.example.bar
     * com.example.foo=foo.jar;Foo Library
     * com.example.bar=bar.jar;Bar Library
     * </pre>
     * Not saved in source.properties.
     */
    public static final String ADDON_LIBRARIES    = "libraries";            //$NON-NLS-1$

    /**
     * An optional default skin string of the add-on. <br/>
     * Not saved in source.properties.
     */
    public static final String ADDON_DEFAULT_SKIN = "skin";                 //$NON-NLS-1$

    /**
     * An optional USB vendor string for the add-on. <br/>
     * Not saved in source.properties.
     */
    public static final String ADDON_USB_VENDOR   = "usb-vendor";           //$NON-NLS-1$

}
