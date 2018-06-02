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

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;

/**
 * {@link IPkgCapabilities} describe which attributes are available for each kind of
 * SDK Manager package type.
 * <p/>
 * To query packages capabilities, rely on {@code PkgType.hasXxx()} or {@code PkgDesc.hasXxx()}.
 *
 * @see PkgType
 * @see PkgDesc
 */
public interface IPkgCapabilities {

    /**
     * Indicates whether this package type has a {@link FullRevision}.
     * @return True if this package type has a {@link FullRevision}.
     */
    boolean hasFullRevision();

    /**
     * Indicates whether this package type has a {@link MajorRevision}.
     * @return True if this package type has a {@link MajorRevision}.
     */
    boolean hasMajorRevision();

    /**
     * Indicates whether this package type has a {@link AndroidVersion}.
     * @return True if this package type has a {@link AndroidVersion}.
     */
    boolean hasAndroidVersion();

    /**
     * Indicates whether this package type has a path.
     * @return True if this package type has a path.
     */
    boolean hasPath();

    /**
     * Indicates whether this package type has a tag.
     * @return True if this package type has a tag id-display tuple.
     */
    boolean hasTag();

    /**
     * Indicates whether this package type has a vendor id.
     * @return True if this package type has a vendor id.
     */
    boolean hasVendor();

    /**
     * Indicates whether this package type has a {@code min-tools-rev} attribute.
     * @return True if this package type has a {@code min-tools-rev} attribute.
     */
    boolean hasMinToolsRev();

    /**
     * Indicates whether this package type has a {@code min-platform-tools-rev} attribute.
     * @return True if this package type has a {@code min-platform-tools-rev} attribute.
     */
    boolean hasMinPlatformToolsRev();
}

