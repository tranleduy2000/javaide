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

package com.android.sdklib.repository.local;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Properties;

/**
 * Local add-on system-image package, for a given addon's {@link AndroidVersion} and given ABI.
 * The system-image tag is the add-on name.
 * The package itself has a major revision.
 * There should be only one for a given android platform version & ABI.
 */
public class LocalAddonSysImgPkgInfo extends LocalPkgInfo {


    @NonNull
    private final IPkgDesc mDesc;

    public LocalAddonSysImgPkgInfo(@NonNull LocalSdk localSdk,
                              @NonNull File localDir,
                              @NonNull Properties sourceProps,
                              @NonNull AndroidVersion version,
                              @Nullable IdDisplay addonVendor,
                              @Nullable IdDisplay addonName,
                              @NonNull String abi,
                              @NonNull MajorRevision revision) {
        super(localSdk, localDir, sourceProps);
        mDesc = PkgDesc.Builder.newAddonSysImg(version, addonVendor, addonName, abi, revision)
                               .create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mDesc;
    }

    public ISystemImage getSystemImage() {
        return LocalSysImgPkgInfo.getSystemImage(mDesc, getLocalDir(), getLocalSdk().getFileOp());
    }
}
