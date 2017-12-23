/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sdklib.repository.descriptors;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.internal.repository.IListDescription;
import com.android.sdklib.internal.repository.packages.License;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;

import java.io.File;

/**
 * {@link IPkgDesc} keeps information on individual SDK packages
 * (both local or remote packages definitions.)
 * <br/>
 * Packages have different attributes depending on their type.
 * <p/>
 * To create a new {@link IPkgDesc}, use one of the package-specific constructors
 * provided by {@code PkgDesc.Builder.newXxx()}.
 * <p/>
 * To query packages capabilities, rely on {@link #getType()} and the {@code IPkgDesc.hasXxx()}
 * methods provided by {@link IPkgDesc}.
 */
public interface IPkgDesc extends Comparable<IPkgDesc>, IPkgCapabilities, IListDescription {

    /**
     * Returns the type of the package.
     * @return Returns one of the {@link PkgType} constants.
     */
    @NonNull
    public abstract PkgType getType();

    /**
     * Returns the list-display meta data of this package.
     * @return The list-display data, if available, or null.
     */
    @Nullable
    public String getListDisplay();

    @Nullable
    public String getDescriptionShort();

    @Nullable
    public String getDescriptionUrl();

    @Nullable
    public License getLicense();

    public boolean isObsolete();

    /**
     * Returns the package's {@link FullRevision} or null.
     * @return A non-null value if {@link #hasFullRevision()} is true; otherwise a null value.
     */
    @Nullable
    public FullRevision getFullRevision();

    /**
     * Returns the package's {@link MajorRevision} or null.
     * @return A non-null value if {@link #hasMajorRevision()} is true; otherwise a null value.
     */
    @Nullable
    public MajorRevision getMajorRevision();

    /**
     * Returns the package's {@link AndroidVersion} or null.
     * @return A non-null value if {@link #hasAndroidVersion()} is true; otherwise a null value.
     */
    @Nullable
    public AndroidVersion getAndroidVersion();

    /**
     * Returns the package's path string or null.
     * <p/>
     * For {@link PkgType#PKG_SYS_IMAGE}, the path is the system-image ABI. <br/>
     * For {@link PkgType#PKG_PLATFORM}, the path is the platform hash string. <br/>
     * For {@link PkgType#PKG_ADDON}, the path is the platform hash string. <br/>
     * For {@link PkgType#PKG_EXTRA}, the path is the extra-path string. <br/>
     *
     * @return A non-null value if {@link #hasPath()} is true; otherwise a null value.
     */
    @Nullable
    public String getPath();

    /**
     * Returns the package's tag id-display tuple or null.
     *
     * @return A non-null tag if {@link #hasTag()} is true; otherwise a null value.
     */
    @Nullable
    public IdDisplay getTag();

    /**
     * Returns the package's vendor-id string or null.
     * @return A non-null value if {@link #hasVendor()} is true; otherwise a null value.
     */
    @Nullable
    public IdDisplay getVendor();

    /**
     * Returns the package's {@code min-tools-rev} or null.
     * @return A non-null value if {@link #hasMinToolsRev()} is true; otherwise a null value.
     */
    @Nullable
    public FullRevision getMinToolsRev();

    /**
     * Returns the package's {@code min-platform-tools-rev} or null.
     * @return A non-null value if {@link #hasMinPlatformToolsRev()} is true; otherwise null.
     */
    @Nullable
    public FullRevision getMinPlatformToolsRev();

    /**
     * Indicates whether <em>this</em> package descriptor is an update for the given
     * existing descriptor.
     *
     * @param existingDesc A non-null existing descriptor.
     * @return True if this package is an update for the given one.
     */
    public boolean isUpdateFor(@NonNull IPkgDesc existingDesc);

    /**
     * Returns a stable string id that can be used to reference this package.
     * @return A stable string id that can be used to reference this package.
     */
    @NonNull
    public String getInstallId();

    /**
     * Returns the canonical location where such a package would be installed.
     * @param sdkLocation The root of the SDK.
     * @return the canonical location where such a package would be installed.
     */
    @NonNull
    public File getCanonicalInstallFolder(@NonNull File sdkLocation);
}

