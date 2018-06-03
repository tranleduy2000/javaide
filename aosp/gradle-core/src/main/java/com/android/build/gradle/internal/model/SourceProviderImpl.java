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
import com.google.common.collect.Lists;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

/**
 * Implementation of SourceProvider that is serializable. Objects used in the DSL cannot be
 * serialized.
 */
class SourceProviderImpl implements SourceProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private File manifestFile;
    private Collection<File> javaDirs;
    private Collection<File> resourcesDirs;
    private Collection<File> aidlDirs;
    private Collection<File> rsDirs;
    private Collection<File> cDirs;
    private Collection<File> cppDirs;
    private Collection<File> resDirs;
    private Collection<File> assetsDirs;
    private Collection<File> libsDirs;

    @NonNull
    static SourceProviderImpl cloneProvider(@NonNull SourceProvider sourceProvider) {
        SourceProviderImpl sourceProviderClone = new SourceProviderImpl();

        sourceProviderClone.name = sourceProvider.getName();
        sourceProviderClone.manifestFile = sourceProvider.getManifestFile();
        sourceProviderClone.javaDirs = sourceProvider.getJavaDirectories();
        sourceProviderClone.resourcesDirs = sourceProvider.getResourcesDirectories();
        sourceProviderClone.aidlDirs = sourceProvider.getAidlDirectories();
        sourceProviderClone.rsDirs = sourceProvider.getRenderscriptDirectories();
        sourceProviderClone.cDirs = sourceProvider.getCDirectories();
        sourceProviderClone.cppDirs = sourceProvider.getCDirectories();
        sourceProviderClone.resDirs = sourceProvider.getResDirectories();
        sourceProviderClone.assetsDirs = sourceProvider.getAssetsDirectories();
        sourceProviderClone.libsDirs = sourceProvider.getJniLibsDirectories();

        return sourceProviderClone;
    }

    @NonNull
    static Collection<SourceProvider> cloneCollection(
            @NonNull Collection<SourceProvider> sourceProviders) {
        Collection<SourceProvider> results = Lists.newArrayListWithCapacity(sourceProviders.size());
        for (SourceProvider sourceProvider : sourceProviders) {
            results.add(SourceProviderImpl.cloneProvider(sourceProvider));
        }

        return results;
    }

    private SourceProviderImpl() {
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public File getManifestFile() {
        return manifestFile;
    }

    @NonNull
    @Override
    public Collection<File> getJavaDirectories() {
        return javaDirs;
    }

    @NonNull
    @Override
    public Collection<File> getResourcesDirectories() {
        return resourcesDirs;
    }

    @NonNull
    @Override
    public Collection<File> getAidlDirectories() {
        return aidlDirs;
    }

    @NonNull
    @Override
    public Collection<File> getRenderscriptDirectories() {
        return rsDirs;
    }

    @NonNull
    @Override
    public Collection<File> getCDirectories() {
        return cDirs;
    }

    @NonNull
    @Override
    public Collection<File> getCppDirectories() {
        return cppDirs;
    }

    @NonNull
    @Override
    public Collection<File> getResDirectories() {
        return resDirs;
    }

    @NonNull
    @Override
    public Collection<File> getAssetsDirectories() {
        return assetsDirs;
    }

    @NonNull
    @Override
    public Collection<File> getJniLibsDirectories() {
        return libsDirs;
    }

    @Override
    public String toString() {
        return "SourceProviderImpl{" +
                "manifestFile=" + manifestFile +
                ", javaDirs=" + javaDirs +
                ", resourcesDirs=" + resourcesDirs +
                ", aidlDirs=" + aidlDirs +
                ", rsDirs=" + rsDirs +
                ", cDirs=" + cDirs +
                ", cppDirs=" + cppDirs +
                ", resDirs=" + resDirs +
                ", assetsDirs=" + assetsDirs +
                ", libsDirs=" + libsDirs +
                '}';
    }
}
