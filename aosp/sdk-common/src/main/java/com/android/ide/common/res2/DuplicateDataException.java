/*
 * Copyright (C) 2012 The Android Open Source Project
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
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFile;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;
import java.util.List;


/**
 * Exception when a {@link DataItem} is declared more than once in a {@link DataSet}
 */
public class DuplicateDataException extends MergingException {

    private static final String DUPLICATE_RESOURCES = "Duplicate resources";

    DuplicateDataException(Message[] messages) {
        super(null, messages);
    }

    static <I extends DataItem> Message[] createMessages(
            @NonNull Collection<Collection<I>> duplicateDataItemSets) {
        List<Message> messages = Lists.newArrayListWithCapacity(duplicateDataItemSets.size());
        for (Collection<I> duplicateItems : duplicateDataItemSets) {
            ImmutableList.Builder<SourceFilePosition> positions = ImmutableList.builder();
            for (I item : duplicateItems) {
                if (!item.isRemoved()) {
                    positions.add(getPosition(item));
                }
            }
            messages.add(new Message(
                    Message.Kind.ERROR,
                    DUPLICATE_RESOURCES,
                    DUPLICATE_RESOURCES,
                    positions.build()));
        }
        return Iterables.toArray(messages, Message.class);
    }

    private static SourceFilePosition getPosition(DataItem item) {
        DataFile dataFile = item.getSource();
        if (dataFile == null) {
            return new SourceFilePosition(new SourceFile(item.getKey()), SourcePosition.UNKNOWN);
        }
        File f = dataFile.getFile();
        SourcePosition sourcePosition = SourcePosition.UNKNOWN;  // TODO: find position in file.
        return new SourceFilePosition(new SourceFile(f, item.getKey()), sourcePosition);
    }
}
