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

import static com.android.SdkConstants.DOT_JAR;
import static com.android.SdkConstants.FD_JARS;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dependency.LibraryDependencyImpl;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaLibrary;
import com.android.ide.common.caching.CreatingCache;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 */
public class DependenciesImpl implements Dependencies, Serializable {
    private static final long serialVersionUID = 1L;

    private static final CreatingCache<LibraryDependency, AndroidLibrary> sCache
            = new CreatingCache<LibraryDependency, AndroidLibrary>(
            new CreatingCache.ValueFactory<LibraryDependency, AndroidLibrary>() {
                @Override
                @NonNull
                public AndroidLibrary create(@NonNull LibraryDependency key) {
                    return convertAndroidLibrary(key);
                }
            });

    @NonNull
    private final List<AndroidLibrary> libraries;
    @NonNull
    private final List<JavaLibrary> javaLibraries;
    @NonNull
    private final List<String> projects;

    public static void clearCaches() {
        sCache.clear();
    }

    @NonNull
    static DependenciesImpl cloneDependenciesForJavaArtifacts(@NonNull Dependencies dependencies) {
        List<AndroidLibrary> libraries = Collections.emptyList();
        List<JavaLibrary> javaLibraries = Lists.newArrayList(dependencies.getJavaLibraries());
        List<String> projects = Collections.emptyList();

        return new DependenciesImpl(libraries, javaLibraries, projects);
    }

    @NonNull
    static DependenciesImpl cloneDependencies(
            @NonNull BaseVariantData variantData,
            @NonNull AndroidBuilder androidBuilder) {
        VariantDependencies variantDependencies = variantData.getVariantDependency();

        List<AndroidLibrary> libraries;
        List<JavaLibrary> javaLibraries;
        List<String> projects;

        List<LibraryDependencyImpl> libs = variantDependencies.getLibraries();
        libraries = Lists.newArrayListWithCapacity(libs.size());
        for (LibraryDependencyImpl libImpl : libs) {
            AndroidLibrary clonedLib = sCache.get(libImpl);
            if (clonedLib != null) {
                libraries.add(clonedLib);
            }
        }

        List<JarDependency> jarDeps = variantDependencies.getJarDependencies();
        List<JarDependency> localDeps = variantDependencies.getLocalDependencies();

        javaLibraries = Lists.newArrayListWithExpectedSize(jarDeps.size() + localDeps.size());
        projects = Lists.newArrayList();

        for (JarDependency jarDep : jarDeps) {
            // don't include package-only dependencies
            if (jarDep.isCompiled()) {
                boolean customArtifact = jarDep.getResolvedCoordinates() != null &&
                        jarDep.getResolvedCoordinates().getClassifier() != null;

                File jarFile = jarDep.getJarFile();
                if (!customArtifact && jarDep.getProjectPath() != null) {
                    projects.add(jarDep.getProjectPath());
                } else {
                    javaLibraries.add(
                            new JavaLibraryImpl(jarFile, null, jarDep.getResolvedCoordinates()));
                }
            }
        }

        for (JarDependency jarDep : localDeps) {
            // don't include package-only dependencies
            if (jarDep.isCompiled()) {
                javaLibraries.add(
                        new JavaLibraryImpl(
                                jarDep.getJarFile(),
                                null,
                                jarDep.getResolvedCoordinates()));
            }
        }

        GradleVariantConfiguration variantConfig = variantData.getVariantConfiguration();

        if (variantConfig.getRenderscriptSupportModeEnabled()) {
            File supportJar = androidBuilder.getRenderScriptSupportJar();
            if (supportJar != null) {
                javaLibraries.add(new JavaLibraryImpl(supportJar, null, null));
            }
        }

        return new DependenciesImpl(libraries, javaLibraries, projects);
    }

    public DependenciesImpl(@NonNull Set<JavaLibrary> javaLibraries) {
        this.javaLibraries = Lists.newArrayList(javaLibraries);
        this.libraries = Collections.emptyList();
        this.projects = Collections.emptyList();
    }

    private DependenciesImpl(@NonNull List<AndroidLibrary> libraries,
                             @NonNull List<JavaLibrary> javaLibraries,
                             @NonNull List<String> projects) {
        this.libraries = libraries;
        this.javaLibraries = javaLibraries;
        this.projects = projects;
    }

    @NonNull
    @Override
    public Collection<AndroidLibrary> getLibraries() {
        return libraries;
    }

    @NonNull
    @Override
    public Collection<JavaLibrary> getJavaLibraries() {
        return javaLibraries;
    }

    @NonNull
    @Override
    public List<String> getProjects() {
        return projects;
    }

    @NonNull
    private static AndroidLibrary convertAndroidLibrary(
            @NonNull LibraryDependency libraryDependency) {
        List<LibraryDependency> deps = libraryDependency.getDependencies();
        List<AndroidLibrary> clonedDeps = Lists.newArrayListWithCapacity(deps.size());
        for (LibraryDependency child : deps) {
            AndroidLibrary clonedLib = sCache.get(child);
            if (clonedLib != null) {
                clonedDeps.add(clonedLib);
            }
        }

        // compute local jar even if the bundle isn't exploded.
        Collection<File> localJarOverride = findLocalJar(libraryDependency);

        return new AndroidLibraryImpl(
                libraryDependency,
                clonedDeps,
                localJarOverride,
                libraryDependency.getProject(),
                libraryDependency.getProjectVariant(),
                libraryDependency.getRequestedCoordinates(),
                libraryDependency.getResolvedCoordinates());
    }

    /**
     * Finds the local jar for an aar.
     *
     * Since the model can be queried before the aar are exploded, we attempt to get them
     * from inside the aar.
     *
     * @param library the library.
     * @return its local jars.
     */
    @NonNull
    private static Collection<File> findLocalJar(LibraryDependency library) {
        // if the library is exploded, just use the normal method.
        File explodedFolder = library.getFolder();
        if (explodedFolder.isDirectory()) {
            return library.getLocalJars();
        }

        // if the aar file is present, search inside it for jar files under libs/
        File aarFile = library.getBundle();
        if (aarFile.isFile()) {
            List<File> jarList = Lists.newArrayList();

            File jarsFolder = new File(explodedFolder, FD_JARS);

            ZipFile zipFile = null;
            try {
                //noinspection IOResourceOpenedButNotSafelyClosed
                zipFile = new ZipFile(aarFile);

                for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
                    ZipEntry zipEntry = e.nextElement();
                    String name = zipEntry.getName();
                    if (name.startsWith("libs/") && name.endsWith(DOT_JAR)) {
                        jarList.add(new File(jarsFolder, name.replace('/', File.separatorChar)));
                    }
                }

                return jarList;
            } catch (FileNotFoundException ignored) {
                // should not happen since we check ahead of time
            } catch (IOException e) {
                // we'll return an empty list below
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return Collections.emptyList();
    }
}
