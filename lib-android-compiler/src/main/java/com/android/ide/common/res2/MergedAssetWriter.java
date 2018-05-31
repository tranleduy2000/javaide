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

package com.android.ide.common.res2;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.io.Files;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * A {@link MergeWriter} for assets, using {@link AssetItem}.
 */
public class MergedAssetWriter extends MergeWriter<AssetItem> {

    public MergedAssetWriter(@NonNull File rootFolder) {
        super(rootFolder);
    }

    @Override
    public void addItem(@NonNull final AssetItem item) throws ConsumerException {
        // Only write it if the state is TOUCHED.
        if (item.isTouched()) {
            getExecutor().execute(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    AssetFile assetFile = item.getSource();

                    File fromFile = assetFile.getFile();

                    // the out file is computed from the item key since that includes the
                    // relative folder.
                    File toFile = new File(getRootFolder(),
                            item.getKey().replace('/', File.separatorChar));

                    // make sure the folders are created
                    toFile.getParentFile().mkdirs();

                    Files.copy(fromFile, toFile);

                    return null;
                }
            });
        }
    }

    @Override
    public void removeItem(@NonNull AssetItem removedItem, @Nullable AssetItem replacedBy)
            throws ConsumerException {
        if (replacedBy == null) {
            File removedFile = new File(getRootFolder(), removedItem.getName());
            removedFile.delete();
        }
    }

    @Override
    public boolean ignoreItemInMerge(AssetItem item) {
        // never ignore any item
        return false;
    }
}
