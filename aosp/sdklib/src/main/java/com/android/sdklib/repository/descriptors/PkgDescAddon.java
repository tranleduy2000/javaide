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
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.repository.License;
import com.android.sdklib.repository.MajorRevision;

/**
 * Implementation detail of {@link PkgDesc} for add-ons.
 * Do not use this class directly.
 * To create an instance use {@link PkgDesc.Builder#newAddon} instead.
 */
final class PkgDescAddon extends PkgDesc implements IPkgDescAddon {

    private final IdDisplay mAddonName;

    /**
     * Add-on descriptor.
     * The following attributes are mandatory:
     */
    PkgDescAddon(@NonNull  PkgType type,
                 @Nullable License license,
                 @Nullable String listDisplay,
                 @Nullable String descriptionShort,
                 @Nullable String descriptionUrl,
                 boolean isObsolete,
                 @NonNull  MajorRevision majorRevision,
                 @NonNull  AndroidVersion androidVersion,
                 @NonNull  IdDisplay addonVendor,
                 @NonNull  IdDisplay addonName) {
        super(type,
              license,
              listDisplay,
              descriptionShort,
              descriptionUrl,
              isObsolete,
              null,     //fullRevision
              majorRevision,
              androidVersion,
              AndroidTargetHash.getAddonHashString(addonVendor.getDisplay(),
                                                   addonName.getDisplay(),
                                                   androidVersion),
              null,     //tag
              addonVendor,
              null,     //minToolsRev
              null,     //minPlatformToolsRev
              null,     //customIsUpdateFor
              null);    //customPath

        mAddonName = addonName;
    }

    @NonNull
    @Override
    public IdDisplay getName() {
        return mAddonName;
    }
}
