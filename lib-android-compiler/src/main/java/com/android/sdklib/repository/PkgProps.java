/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * Public constants used by the repository when saving {@code source.properties}
 * files in local packages.
 * <p/>
 * These constants are public and part of the SDK Manager public API.
 * Once published we can't change them arbitrarily since various parts
 * of our build process depend on them.
 */
public class PkgProps {

    // Base Package
    public static final String PKG_REVISION             = "Pkg.Revision";           //$NON-NLS-1$
    public static final String PKG_LICENSE              = "Pkg.License";            //$NON-NLS-1$
    public static final String PKG_LICENSE_REF          = "Pkg.LicenseRef";         //$NON-NLS-1$
    public static final String PKG_DESC                 = "Pkg.Desc";               //$NON-NLS-1$
    public static final String PKG_DESC_URL             = "Pkg.DescUrl";            //$NON-NLS-1$
    public static final String PKG_RELEASE_NOTE         = "Pkg.RelNote";            //$NON-NLS-1$
    public static final String PKG_RELEASE_URL          = "Pkg.RelNoteUrl";         //$NON-NLS-1$
    public static final String PKG_SOURCE_URL           = "Pkg.SourceUrl";          //$NON-NLS-1$
    public static final String PKG_OBSOLETE             = "Pkg.Obsolete";           //$NON-NLS-1$
    public static final String PKG_LIST_DISPLAY         = "Pkg.ListDisplay";        //$NON-NLS-1$

    // AndroidVersion

    public static final String VERSION_API_LEVEL        = "AndroidVersion.ApiLevel";//$NON-NLS-1$
    /** Code name of the platform if the platform is not final */
    public static final String VERSION_CODENAME         = "AndroidVersion.CodeName";//$NON-NLS-1$


    // AddonPackage

    public static final String ADDON_NAME               = "Addon.Name";             //$NON-NLS-1$
    public static final String ADDON_NAME_ID            = "Addon.NameId";           //$NON-NLS-1$
    public static final String ADDON_NAME_DISPLAY       = "Addon.NameDisplay";      //$NON-NLS-1$

    public static final String ADDON_VENDOR             = "Addon.Vendor";           //$NON-NLS-1$
    public static final String ADDON_VENDOR_ID          = "Addon.VendorId";         //$NON-NLS-1$
    public static final String ADDON_VENDOR_DISPLAY     = "Addon.VendorDisplay";    //$NON-NLS-1$

    // DocPackage

    // ExtraPackage

    public static final String EXTRA_PATH               = "Extra.Path";             //$NON-NLS-1$
    public static final String EXTRA_OLD_PATHS          = "Extra.OldPaths";         //$NON-NLS-1$
    public static final String EXTRA_MIN_API_LEVEL      = "Extra.MinApiLevel";      //$NON-NLS-1$
    public static final String EXTRA_PROJECT_FILES      = "Extra.ProjectFiles";     //$NON-NLS-1$
    public static final String EXTRA_VENDOR             = "Extra.Vendor";           //$NON-NLS-1$
    public static final String EXTRA_VENDOR_ID          = "Extra.VendorId";         //$NON-NLS-1$
    public static final String EXTRA_VENDOR_DISPLAY     = "Extra.VendorDisplay";    //$NON-NLS-1$
    public static final String EXTRA_NAME_DISPLAY       = "Extra.NameDisplay";      //$NON-NLS-1$

    // ILayoutlibVersion

    public static final String LAYOUTLIB_API            = "Layoutlib.Api";          //$NON-NLS-1$
    public static final String LAYOUTLIB_REV            = "Layoutlib.Revision";     //$NON-NLS-1$

    // MinToolsPackage

    public static final String MIN_TOOLS_REV            = "Platform.MinToolsRev";   //$NON-NLS-1$

    // PlatformPackage

    public static final String PLATFORM_VERSION         = "Platform.Version";       //$NON-NLS-1$
    /** Code name of the platform. This has no bearing on the package being a preview or not. */
    public static final String PLATFORM_CODENAME        = "Platform.CodeName";      //$NON-NLS-1$
    public static final String PLATFORM_INCLUDED_ABI    = "Platform.Included.Abi";  //$NON-NLS-1$

    // ToolPackage

    public static final String MIN_PLATFORM_TOOLS_REV = "Platform.MinPlatformToolsRev";//$NON-NLS-1$
    public static final String MIN_BUILD_TOOLS_REV      = "Platform.MinBuildToolsRev"; //$NON-NLS-1$


    // SamplePackage

    public static final String SAMPLE_MIN_API_LEVEL     = "Sample.MinApiLevel";     //$NON-NLS-1$

    // SystemImagePackage

    public static final String SYS_IMG_ABI              = "SystemImage.Abi";        //$NON-NLS-1$
    public static final String SYS_IMG_TAG_ID           = "SystemImage.TagId";      //$NON-NLS-1$
    public static final String SYS_IMG_TAG_DISPLAY      = "SystemImage.TagDisplay"; //$NON-NLS-1$
}
