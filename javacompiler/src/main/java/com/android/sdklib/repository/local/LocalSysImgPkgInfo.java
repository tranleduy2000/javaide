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
import com.android.sdklib.SystemImage;
import com.android.sdklib.repository.MajorRevision;
import com.android.sdklib.repository.PkgProps;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

/**
 * Local system-image package, for a given platform's {@link AndroidVersion}
 * and given ABI.
 * The package itself has a major revision.
 * There should be only one for a given android platform version & ABI.
 */
public class LocalSysImgPkgInfo extends LocalPkgInfo {


    private final @NonNull IPkgDesc mDesc;

    public LocalSysImgPkgInfo(@NonNull  LocalSdk localSdk,
                              @NonNull  File localDir,
                              @NonNull  Properties sourceProps,
                              @NonNull  AndroidVersion version,
                              @Nullable IdDisplay tag,
                              @NonNull  String abi,
                              @NonNull  MajorRevision revision) {
        super(localSdk, localDir, sourceProps);
        mDesc = PkgDesc.Builder.newSysImg(version, tag, abi, revision).create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mDesc;
    }

    /**
     * Extracts the tag id & display from the properties.
     * If missing, uses the "default" tag id.
     */
    @NonNull
    public static IdDisplay extractTagFromProps(Properties props) {
        if (props != null) {
            String tagId   = props.getProperty(PkgProps.SYS_IMG_TAG_ID,
                                               SystemImage.DEFAULT_TAG.getId());
            String tagDisp = props.getProperty(PkgProps.SYS_IMG_TAG_DISPLAY, "");      //$NON-NLS-1$
            if (tagDisp == null || tagDisp.isEmpty()) {
                tagDisp = tagIdToDisplay(tagId);
            }
            assert tagId   != null;
            assert tagDisp != null;
            return new IdDisplay(tagId, tagDisp);
        }
        return SystemImage.DEFAULT_TAG;
    }

    /**
     * Computes a display-friendly tag string based on the tag id.
     * This is typically used when there's no tag-display attribute.
     *
     * @param tagId A non-null tag id to sanitize for display.
     * @return The tag id with all non-alphanum symbols replaced by spaces and trimmed.
     */
    @NonNull
    public static String tagIdToDisplay(@NonNull String tagId) {
        String name;
        name = tagId.replaceAll("[^A-Za-z0-9]+", " ");      //$NON-NLS-1$ //$NON-NLS-2$
        name = name.replaceAll(" +", " ");                  //$NON-NLS-1$ //$NON-NLS-2$
        name = name.trim();

        if (name.length() > 0) {
            char c = name.charAt(0);
            if (!Character.isUpperCase(c)) {
                StringBuilder sb = new StringBuilder(name);
                sb.replace(0, 1, String.valueOf(c).toUpperCase(Locale.US));
                name = sb.toString();
            }
        }
        return name;
    }

    // TODO create package on demand if needed. This might not be needed
    // since typically system-images are retrieved via IAndroidTarget.
}
