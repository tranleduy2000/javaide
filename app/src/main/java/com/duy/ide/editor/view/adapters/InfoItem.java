/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.editor.view.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.ide.editor.view.CodeSuggestsEditText;

/**
 * item for suggest adapter of {@link CodeSuggestsEditText}
 */
public class InfoItem implements Comparable<String> {
    private int type;
    @NonNull
    private String name = "";
    @Nullable
    private String description = null;
    @Nullable
    private CharSequence show = null;
    private String compare = "";

    public InfoItem(int type, @NonNull String name, @Nullable String description, @Nullable String show) {
        this(type, name, description);
        this.show = show;
    }

    public InfoItem(int type, @NonNull String name, @Nullable String description) {
        this(type, name);
        this.description = description;
        this.compare = name.toLowerCase();
    }

    public InfoItem(int type, @NonNull String name) {
        this.name = name;
        this.type = type;
        this.compare = name.toLowerCase();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public CharSequence getShow() {
        return show;
    }

    public void setShow(@Nullable CharSequence show) {
        this.show = show;
    }

    @Override
    public int compareTo(@NonNull String o) {
        String s = o.toLowerCase();
        return compare.startsWith(s) ? 0 : -1;
    }

    @Override
    public String toString() {
        return name + " - " + description + " - " + show;
    }
}
