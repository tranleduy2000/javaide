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
import com.android.ide.common.internal.WaitableExecutor;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A {@link MergeConsumer} that writes the result on the disk.
 */
public abstract class MergeWriter<I extends DataItem> implements MergeConsumer<I> {

    @NonNull
    private final File mRootFolder;
    @NonNull
    private final WaitableExecutor<Void> mExecutor;

    public MergeWriter(@NonNull File rootFolder) {
        mRootFolder = rootFolder;
        mExecutor = new WaitableExecutor<Void>();
    }

    @Override
    public void start(@NonNull DocumentBuilderFactory factory) throws ConsumerException {
    }

    @Override
    public void end() throws ConsumerException {
        try {
            postWriteAction();

            getExecutor().waitForTasksWithQuickFail(true);
        } catch (ConsumerException e) {
            throw e;
        } catch (Exception e) {
            throw new ConsumerException(e);
        }
    }

    /**
     * Called after all the items have been added/removed. This is called by {@link #end()}.
     * @throws ConsumerException
     */
    protected void postWriteAction() throws ConsumerException {
    }

    @NonNull
    protected WaitableExecutor<Void> getExecutor() {
        return mExecutor;
    }

    @NonNull
    protected File getRootFolder() {
        return mRootFolder;
    }
}
