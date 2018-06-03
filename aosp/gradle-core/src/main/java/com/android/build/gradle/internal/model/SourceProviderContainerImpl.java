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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.builder.model.SourceProvider;
import com.android.builder.model.SourceProviderContainer;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of SourceProviderContainer that is serializable and is meant to be used
 * in the model sent to the tooling API.
 *
 * It also provides convenient methods to create an instance, cloning the original
 * SourceProvider.
 *
 * When the source Provider is cloned, its values are queried and then statically stored.
 * Any further change through the DSL will not be impact. Therefore instances of this class
 * should only be used when the model is built.
 *
 * To create more dynamic isntances of SourceProviderContainer, use
 * {@link com.android.build.gradle.internal.variant.DefaultSourceProviderContainer}
 */
class SourceProviderContainerImpl implements SourceProviderContainer, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final String name;
    @NonNull
    private final SourceProvider sourceProvider;

    /**
     * Create a {@link SourceProviderContainer} that is serializable to
     * use in the model sent through the tooling API.
     *
     * @param sourceProviderContainer the source provider
     *
     * @return a non-null SourceProviderContainer
     */
    @NonNull
    static SourceProviderContainer clone(
            @NonNull SourceProviderContainer sourceProviderContainer) {
        return create(
                sourceProviderContainer.getArtifactName(),
                sourceProviderContainer.getSourceProvider());
    }

    @NonNull
    static List<SourceProviderContainer> cloneCollection(@NonNull Collection<SourceProviderContainer> containers) {
        List<SourceProviderContainer> clones = Lists.newArrayListWithCapacity(containers.size());

        for (SourceProviderContainer container : containers) {
            clones.add(clone(container));
        }

        return clones;
    }

    @NonNull
    static SourceProviderContainer create(
            @NonNull String name,
            @NonNull SourceProvider sourceProvider) {
        return new SourceProviderContainerImpl(name,
                SourceProviderImpl.cloneProvider(sourceProvider));
    }

    private SourceProviderContainerImpl(@NonNull String name,
                                        @NonNull SourceProvider sourceProvider) {
        this.name = name;
        this.sourceProvider = sourceProvider;
    }

    @NonNull
    @Override
    public String getArtifactName() {
        return name;
    }

    @NonNull
    @Override
    public SourceProvider getSourceProvider() {
        return sourceProvider;
    }
}
