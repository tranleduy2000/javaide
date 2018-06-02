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
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFile;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A consumer of merges. Used with {@link DataMerger#mergeData(MergeConsumer, boolean)}.
 */
public interface MergeConsumer<I extends DataItem> {

    /**
     * An exception thrown during by the consumer. It always contains the original exception as its
     * cause.
     */
    class ConsumerException extends MergingException {

        public ConsumerException(@NonNull Throwable cause) {
            this(cause, SourceFile.UNKNOWN);
        }

        public ConsumerException(@NonNull Throwable cause, @NonNull File file) {
            this(cause, new SourceFile(file));
        }

        private ConsumerException(@NonNull Throwable cause, @NonNull SourceFile file) {
            super(cause, new Message(
                    Message.Kind.ERROR,
                    Objects.firstNonNull(
                            cause.getLocalizedMessage(),
                            cause.getClass().getCanonicalName()),
                    Throwables.getStackTraceAsString(cause),
                    new SourceFilePosition(file, SourcePosition.UNKNOWN)));
        }
    }

    /**
     * Called before the merge starts.
     */
    void start(@NonNull DocumentBuilderFactory factory) throws ConsumerException;

    /**
     * Called after the merge ends.
     */
    void end() throws ConsumerException;

    /**
     * Adds an item. The item may already be existing. Calling {@link DataItem#isTouched()} will
     * indicate whether the item actually changed.
     *
     * @param item the new item.
     */
    void addItem(@NonNull I item) throws ConsumerException;

    /**
     * Removes an item. Optionally pass the item that will replace this one. This methods does not
     * do the replacement. The replaced item is just there in case the removal can be optimized when
     * it's a replacement vs. a removal.
     *
     * @param removedItem the removed item.
     * @param replacedBy  the optional item that replaces the removed item.
     */
    void removeItem(@NonNull I removedItem, @Nullable I replacedBy) throws ConsumerException;

    boolean ignoreItemInMerge(I item);

}
