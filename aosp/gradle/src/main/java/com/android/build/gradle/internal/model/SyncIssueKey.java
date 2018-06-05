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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.builder.model.SyncIssue;

/**
 * Creates a key from a SyncIssue to use in a map.
 */
public class SyncIssueKey {

    private final int type;
    @NonNull
    private final String data;

    public static SyncIssueKey from(@NonNull SyncIssue syncIssue) {
        return new SyncIssueKey(syncIssue.getType(), syncIssue.getData());
    }

    private SyncIssueKey(int type, @NonNull String data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SyncIssueKey that = (SyncIssueKey) o;

        return type == that.type && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + data.hashCode();
        return result;
    }
}
