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
import com.android.annotations.Nullable;
import com.android.builder.model.AaptOptions;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of AaptOptions that is Serializable.
 */
public class AaptOptionsImpl implements AaptOptions, Serializable {

    private static final long serialVersionUID = 1L;

    @Nullable
    private final String ignoreAssets;

    @Nullable
    private final Collection<String> noCompress;

    private final boolean failOnMissingConfigEntry;

    @NonNull
    private final List<String> additionalParameters;


    static AaptOptions create(@NonNull AaptOptions aaptOptions) {
        return new AaptOptionsImpl(
                aaptOptions.getIgnoreAssets(),
                aaptOptions.getNoCompress(),
                aaptOptions.getFailOnMissingConfigEntry(),
                aaptOptions.getAdditionalParameters()
        );
    }

    private AaptOptionsImpl(String ignoreAssets, Collection<String> noCompress,
            boolean failOnMissingConfigEntry, List<String> additionalParameters) {
        this.ignoreAssets = ignoreAssets;
        if (noCompress == null) {
            this.noCompress = null;
        } else {
            this.noCompress = ImmutableList.copyOf(noCompress);
        }
        this.failOnMissingConfigEntry = failOnMissingConfigEntry;
        this.additionalParameters = additionalParameters;
    }

    @Override

    public String getIgnoreAssets() {
        return ignoreAssets;
    }

    @Override
    public Collection<String> getNoCompress() {
        return noCompress;
    }

    @Override
    public boolean getFailOnMissingConfigEntry() {
        return failOnMissingConfigEntry;
    }

    @Override
    public List<String> getAdditionalParameters() {
        return additionalParameters;
    }


    public String toString() {
        return "AaptOptions{" +
                ", ignoreAssets=" + ignoreAssets +
                ", noCompress=" + noCompress +
                ", failOnMissingConfigEntry=" + failOnMissingConfigEntry +
                ", additionalParameters=" + additionalParameters +
                "}";
    }
}
