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

package com.android.build.gradle.internal.api;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.gradle.api.AndroidSourceDirectorySet;
import com.android.build.gradle.api.AndroidSourceFile;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.builder.model.SourceProvider;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import groovy.lang.Closure;

/**
 */
public class DefaultAndroidSourceSet implements AndroidSourceSet, SourceProvider {
    @NonNull
    private final String name;
    private final boolean isLibrary;

    private final AndroidSourceDirectorySet javaSource;
    private final AndroidSourceDirectorySet javaResources;
    private final AndroidSourceFile manifest;
    private final AndroidSourceDirectorySet assets;
    private final AndroidSourceDirectorySet res;
    private final AndroidSourceDirectorySet aidl;
    private final AndroidSourceDirectorySet renderscript;
    private final AndroidSourceDirectorySet jni;
    private final AndroidSourceDirectorySet jniLibs;
    private final String displayName;

    public DefaultAndroidSourceSet(@NonNull String name,
            Project project, boolean isLibrary) {
        this.name = name;
        this.isLibrary = isLibrary;
        displayName = GUtil.toWords(this.name);

        String javaSrcDisplayName = String.format("%s Java source", displayName);

        javaSource = new DefaultAndroidSourceDirectorySet(javaSrcDisplayName, project);
        javaSource.getFilter().include("**/*.java");

        String javaResourcesDisplayName = String.format("%s Java resources", displayName);
        javaResources = new DefaultAndroidSourceDirectorySet(javaResourcesDisplayName, project);
        javaResources.getFilter().exclude("**/*.java");

        String manifestDisplayName = String.format("%s manifest", displayName);
        manifest = new DefaultAndroidSourceFile(manifestDisplayName, project);

        String assetsDisplayName = String.format("%s assets", displayName);
        assets = new DefaultAndroidSourceDirectorySet(assetsDisplayName, project);

        String resourcesDisplayName = String.format("%s resources", displayName);
        res = new DefaultAndroidSourceDirectorySet(resourcesDisplayName, project);

        String aidlDisplayName = String.format("%s aidl", displayName);
        aidl = new DefaultAndroidSourceDirectorySet(aidlDisplayName, project);

        String renderscriptDisplayName = String.format("%s renderscript", displayName);
        renderscript = new DefaultAndroidSourceDirectorySet(renderscriptDisplayName, project);

        String jniDisplayName = String.format("%s jni", displayName);
        jni = new DefaultAndroidSourceDirectorySet(jniDisplayName, project);

        String libsDisplayName = String.format("%s jniLibs", displayName);
        jniLibs = new DefaultAndroidSourceDirectorySet(libsDisplayName, project);
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("source set %s", getDisplayName());
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    @NonNull
    public String getCompileConfigurationName() {
        if (name.equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            return "compile";
        } else {
            return String.format("%sCompile", name);
        }
    }

    @Override
    @NonNull
    public String getPackageConfigurationName() {
        if (isLibrary) {
            if (name.equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
                return "publish";
            } else {
                return String.format("%sPublish", name);
            }
        }

        if (name.equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            return "apk";
        } else {
            return String.format("%sApk", name);
        }
    }

    @Override
    @NonNull
    public String getProvidedConfigurationName() {
        if (name.equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            return "provided";
        } else {
            return String.format("%sProvided", name);
        }
    }

    @NonNull
    @Override
    public String getWearAppConfigurationName() {
        if (name.equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            return "wearApp";
        } else {
            return String.format("%sWearApp", name);
        }
    }

    @Override
    @NonNull
    public AndroidSourceFile getManifest() {
        return manifest;
    }

    @Override
    @NonNull
    public AndroidSourceSet manifest(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getManifest());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getRes() {
        return res;
    }

    @Override
    @NonNull
    public AndroidSourceSet res(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getRes());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getAssets() {
        return assets;
    }

    @Override
    @NonNull
    public AndroidSourceSet assets(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getAssets());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getAidl() {
        return aidl;
    }

    @Override
    @NonNull
    public AndroidSourceSet aidl(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getAidl());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getRenderscript() {
        return renderscript;
    }

    @Override
    @NonNull
    public AndroidSourceSet renderscript(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getRenderscript());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getJni() {
        return jni;
    }

    @Override
    @NonNull
    public AndroidSourceSet jni(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getJni());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getJniLibs() {
        return jniLibs;
    }

    @Override
    @NonNull
    public AndroidSourceSet jniLibs(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getJniLibs());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceDirectorySet getJava() {
        return javaSource;
    }

    @Override
    @NonNull
    public AndroidSourceSet java(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getJava());
        return this;
    }


    @Override
    @NonNull
    public AndroidSourceDirectorySet getResources() {
        return javaResources;
    }

    @Override
    @NonNull
    public AndroidSourceSet resources(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getResources());
        return this;
    }

    @Override
    @NonNull
    public AndroidSourceSet setRoot(String path) {
        javaSource.setSrcDirs(Collections.singletonList(path + "/java"));
        javaResources.setSrcDirs(Collections.singletonList(path + "/resources"));
        res.setSrcDirs(Collections.singletonList(path + "/" + SdkConstants.FD_RES));
        assets.setSrcDirs(Collections.singletonList(path + "/" + SdkConstants.FD_ASSETS));
        manifest.srcFile(path + "/" + SdkConstants.FN_ANDROID_MANIFEST_XML);
        aidl.setSrcDirs(Collections.singletonList(path + "/aidl"));
        renderscript.setSrcDirs(Collections.singletonList(path + "/rs"));
        jni.setSrcDirs(Collections.singletonList(path + "/jni"));
        jniLibs.setSrcDirs(Collections.singletonList(path + "/jniLibs"));
        return this;
    }

    // --- SourceProvider

    @NonNull
    @Override
    public Set<File> getJavaDirectories() {
        return getJava().getSrcDirs();
    }

    @NonNull
    @Override
    public Set<File> getResourcesDirectories() {
        return getResources().getSrcDirs();
    }

    @Override
    @NonNull
    public File getManifestFile() {
        return getManifest().getSrcFile();
    }

    @Override
    @NonNull
    public Set<File> getAidlDirectories() {
        return getAidl().getSrcDirs();
    }

    @Override
    @NonNull
    public Set<File> getRenderscriptDirectories() {
        return getRenderscript().getSrcDirs();
    }

    @Override
    @NonNull
    public Set<File> getCDirectories() {
        return getJni().getSrcDirs();
    }

    @Override
    @NonNull
    public Set<File> getCppDirectories() {
        // The C and C++ directories are currently the same.  This may change in the future when
        // we use Gradle's native source sets.
        return getJni().getSrcDirs();
    }

    @Override
    @NonNull
    public Set<File> getResDirectories() {
        return getRes().getSrcDirs();
    }

    @Override
    @NonNull
    public Set<File> getAssetsDirectories() {
        return getAssets().getSrcDirs();
    }

    @NonNull
    @Override
    public Collection<File> getJniLibsDirectories() {
        return getJniLibs().getSrcDirs();
    }
}
