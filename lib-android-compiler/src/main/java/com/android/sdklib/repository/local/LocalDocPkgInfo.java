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

package com.android.sdklib.repository.local;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.internal.repository.packages.DocPackage;
import com.android.sdklib.internal.repository.packages.Package;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Properties;

public class LocalDocPkgInfo extends LocalPkgInfo {

    private final @NonNull IPkgDesc mDesc;

    public LocalDocPkgInfo(@NonNull LocalSdk localSdk,
                           @NonNull File localDir,
                           @NonNull Properties sourceProps,
                           @NonNull AndroidVersion version,
                           @NonNull MajorRevision revision) {
        super(localSdk, localDir, sourceProps);
        mDesc = PkgDesc.Builder.newDoc(version, revision).create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mDesc;
    }

    @Nullable
    @Override
    public Package getPackage() {
        Package pkg = super.getPackage();
        if (pkg == null) {
            try {
                pkg = DocPackage.create(
                        null,                       //source
                        getSourceProperties(),      //properties
                        0,                          //apiLevel
                        null,                       //codename
                        0,                          //revision
                        null,                       //license
                        null,                       //description
                        null,                       //descUrl
                        getLocalDir().getPath()     //archiveOsPath
                        );
                setPackage(pkg);
            } catch (Exception e) {
                appendLoadError("Failed to parse package: %1$s", e.toString());
            }
        }
        return pkg;
    }
}
