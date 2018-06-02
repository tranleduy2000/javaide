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
import com.android.sdklib.repository.NoPreviewRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.IPkgDescExtra;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Properties;

public class LocalExtraPkgInfo extends LocalPkgInfo {

    @NonNull
    private final IPkgDescExtra mDesc;

    public LocalExtraPkgInfo(@NonNull  LocalSdk localSdk,
                             @NonNull  File localDir,
                             @NonNull  Properties sourceProps,
                             @NonNull  IdDisplay vendor,
                             @NonNull  String path,
                             @Nullable String displayName,
                             @NonNull  String[] oldPaths,
                             @NonNull  NoPreviewRevision revision) {
        super(localSdk, localDir, sourceProps);
        mDesc = (IPkgDescExtra) PkgDesc.Builder.newExtra(
                vendor,
                path,
                displayName,
                oldPaths,
                revision).create();
    }

    @NonNull
    @Override
    public IPkgDesc getDesc() {
        return mDesc;
    }

    @NonNull
    public String[] getOldPaths() {
        return mDesc.getOldPaths();
    }

    // --- helpers ---

    /**
     * Used to produce a suitable name-display based on the extra's path
     * and vendor display string in addon-3 schemas.
     *
     * @param vendor The vendor id of the extra.
     * @param extraPath The non-null path of the extra.
     * @return A non-null display name based on the extra's path id.
     */
    public static String getPrettyName(@Nullable IdDisplay vendor, @NonNull String extraPath) {
        String name = extraPath;

        // In the past, we used to save the extras in a folder vendor-path,
        // and that "vendor" would end up in the path when we reload the extra from
        // disk. Detect this and compensate.
        String disp = vendor == null ? null : vendor.getDisplay();
        if (disp != null && !disp.isEmpty()) {
            if (name.startsWith(disp + "-")) {  //$NON-NLS-1$
                name = name.substring(disp.length() + 1);
            }
        }

        // Uniformize all spaces in the name
        if (name != null) {
            name = name.replaceAll("[ _\t\f-]+", " ").trim();   //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (name == null || name.isEmpty()) {
            name = "Unknown Extra";
        }

        if (disp != null && !disp.isEmpty()) {
            name = disp + " " + name;  //$NON-NLS-1$
            name = name.replaceAll("[ _\t\f-]+", " ").trim();   //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Look at all lower case characters in range [1..n-1] and replace them by an upper
        // case if they are preceded by a space. Also upper cases the first character of the
        // string.
        boolean changed = false;
        char[] chars = name.toCharArray();
        for (int n = chars.length - 1, i = 0; i < n; i++) {
            if (Character.isLowerCase(chars[i]) && (i == 0 || chars[i - 1] == ' ')) {
                chars[i] = Character.toUpperCase(chars[i]);
                changed = true;
            }
        }
        if (changed) {
            name = new String(chars);
        }

        // Special case: reformat a few typical acronyms.
        name = name.replaceAll(" Usb ", " USB ");   //$NON-NLS-1$
        name = name.replaceAll(" Api ", " API ");   //$NON-NLS-1$

        return name;
    }
}

