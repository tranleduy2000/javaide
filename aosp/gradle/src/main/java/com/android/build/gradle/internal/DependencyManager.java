/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.internal;

import static com.android.SdkConstants.DOT_JAR;
import static com.android.SdkConstants.EXT_ANDROID_PACKAGE;
import static com.android.SdkConstants.EXT_JAR;
import static com.android.builder.core.BuilderConstants.EXT_LIB_ARCHIVE;
import static com.android.builder.core.ErrorReporter.EvaluationMode.STANDARD;
import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.dependency.JarInfo;
import com.android.build.gradle.internal.dependency.LibInfo;
import com.android.build.gradle.internal.dependency.LibraryDependencyImpl;
import com.android.build.gradle.internal.dependency.ManifestDependencyImpl;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.model.MavenCoordinatesImpl;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.internal.tasks.PrepareLibraryTask;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.dependency.DependencyContainer;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.model.MavenCoordinates;
import com.android.builder.model.SyncIssue;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.gradle.api.CircularReferenceException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownProjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.specs.Specs;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A manager to resolve configuration dependencies.
 */
public class DependencyManager {
    protected static final boolean DEBUG_DEPENDENCY = false;

    private Project project;
    private ExtraModelInfo extraModelInfo;
    private ILogger logger;

    final Map<LibraryDependencyImpl, PrepareLibraryTask> prepareTaskMap = Maps.newHashMap();

    public DependencyManager(Project project, ExtraModelInfo extraModelInfo) {
        this.project = project;
        this.extraModelInfo = extraModelInfo;
        logger = new LoggerWrapper(Logging.getLogger(DependencyManager.class));
    }

    /**
     * Returns the list of packaged local jars.
     */
    public static List<File> getPackagedLocalJarFileList(DependencyContainer dependencyContainer) {
        List<JarDependency> jarDependencyList = dependencyContainer.getLocalDependencies();
        Set<File> files = Sets.newHashSetWithExpectedSize(jarDependencyList.size());
        for (JarDependency jarDependency : jarDependencyList) {
            if (jarDependency.isPackaged()) {
                files.add(jarDependency.getJarFile());
            }
        }

        return Lists.newArrayList(files);
    }

    public void addDependencyToPrepareTask(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData,
            @NonNull PrepareDependenciesTask prepareDependenciesTask,
            @NonNull LibraryDependencyImpl lib) {
        PrepareLibraryTask prepareLibTask = prepareTaskMap.get(lib.getNonTransitiveRepresentation());
        if (prepareLibTask != null) {
            prepareDependenciesTask.dependsOn(prepareLibTask);
            prepareLibTask.dependsOn(variantData.preBuildTask);
        }

        for (LibraryDependency childLib : lib.getDependencies()) {
            addDependencyToPrepareTask(
                    variantData,
                    prepareDependenciesTask,
                    (LibraryDependencyImpl) childLib);
        }
    }

    public void resolveDependencies(
            @NonNull VariantDependencies variantDeps,
            @Nullable VariantDependencies testedVariantDeps,
            @Nullable String testedProjectPath) {
        Multimap<LibraryDependency, VariantDependencies> reverseMap = ArrayListMultimap.create();

        resolveDependencyForConfig(variantDeps, testedVariantDeps, testedProjectPath, reverseMap);
        processLibraries(variantDeps.getLibraries(), reverseMap);
    }

    private void processLibraries(
            @NonNull Collection<LibraryDependencyImpl> libraries,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap) {
        for (LibraryDependencyImpl lib : libraries) {
            setupPrepareLibraryTask(lib, reverseMap);
            //noinspection unchecked
            processLibraries(
                    (Collection<LibraryDependencyImpl>) (List<?>) lib.getDependencies(),
                    reverseMap);
        }
    }

    private void setupPrepareLibraryTask(
            @NonNull LibraryDependencyImpl libDependency,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap) {
        Task task = maybeCreatePrepareLibraryTask(libDependency, project);

        // Use the reverse map to find all the configurations that included this android
        // library so that we can make sure they are built.
        // TODO fix, this is not optimum as we bring in more dependencies than we should.
        Collection<VariantDependencies> configDepList = reverseMap.get(libDependency);
        if (configDepList != null && !configDepList.isEmpty()) {
            for (VariantDependencies configDependencies: configDepList) {
                task.dependsOn(configDependencies.getCompileConfiguration().getBuildDependencies());
            }
        }

        // check if this library is created by a parent (this is based on the
        // output file.
        // TODO Fix this as it's fragile
            /*
            This is a somewhat better way but it doesn't work in some project with
            weird setups...
            Project parentProject = DependenciesImpl.getProject(library.getBundle(), projects)
            if (parentProject != null) {
                String configName = library.getProjectVariant()
                if (configName == null) {
                    configName = "default"
                }

                prepareLibraryTask.dependsOn parentProject.getPath() + ":assemble${configName.capitalize()}"
            }
*/

    }

    /**
     * Handles the library and returns a task to "prepare" the library (ie unarchive it). The task
     * will be reused for all projects using the same library.
     *
     * @param library the library.
     * @param project the project
     * @return the prepare task.
     */
    private PrepareLibraryTask maybeCreatePrepareLibraryTask(
            @NonNull LibraryDependencyImpl library,
            @NonNull Project project) {

        // create proper key for the map. library here contains all the dependencies which
        // are not relevant for the task (since the task only extract the aar which does not
        // include the dependencies.
        // However there is a possible case of a rewritten dependencies (with resolution strategy)
        // where the aar here could have different dependencies, in which case we would still
        // need the same task.
        // So we extract a LibraryBundle (no dependencies) from the LibraryDependencyImpl to
        // make the map key that doesn't take into account the dependencies.
        LibraryDependencyImpl key = library.getNonTransitiveRepresentation();

        PrepareLibraryTask prepareLibraryTask = prepareTaskMap.get(key);

        if (prepareLibraryTask == null) {
            String bundleName = GUtil.toCamelCase(library.getName().replaceAll("\\:", " "));

            prepareLibraryTask = project.getTasks().create(
                    "prepare" + bundleName + "Library", PrepareLibraryTask.class);

            prepareLibraryTask.setDescription("Prepare " + library.getName());
            prepareLibraryTask.setBundle(library.getBundle());
            prepareLibraryTask.setExplodedDir(library.getBundleFolder());
            prepareLibraryTask.setVariantName("");

            prepareTaskMap.put(key, prepareLibraryTask);
        }

        return prepareLibraryTask;
    }

    private void resolveDependencyForConfig(
            @NonNull VariantDependencies variantDeps,
            @Nullable VariantDependencies testedVariantDeps,
            @Nullable String testedProjectPath,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap) {

        Configuration compileClasspath = variantDeps.getCompileConfiguration();
        Configuration packageClasspath = variantDeps.getPackageConfiguration();

        // TODO - shouldn't need to do this - fix this in Gradle
        ensureConfigured(compileClasspath);
        ensureConfigured(packageClasspath);

        if (DEBUG_DEPENDENCY) {
            System.out.println(">>>>>>>>>>");
            System.out.println(
                    project.getName() + ":" +
                            compileClasspath.getName() + "/" +
                            packageClasspath.getName());
        }

        Set<String> currentUnresolvedDependencies = Sets.newHashSet();

        // TODO - defer downloading until required -- This is hard to do as we need the info to build the variant config.
        Map<ModuleVersionIdentifier, List<ResolvedArtifact>> artifacts = Maps.newHashMap();
        collectArtifacts(compileClasspath, artifacts);
        collectArtifacts(packageClasspath, artifacts);

        // --- Handle the external/module dependencies ---
        // keep a map of modules already processed so that we don't go through sections of the
        // graph that have been seen elsewhere.
        Map<ModuleVersionIdentifier, List<LibInfo>> foundLibraries = Maps.newHashMap();
        Map<ModuleVersionIdentifier, List<JarInfo>> foundJars = Maps.newHashMap();

        // first get the compile dependencies. Note that in both case the libraries and the
        // jars are a graph. The list only contains the first level of dependencies, and
        // they themselves contain transitive dependencies (libraries can contain both, jars only
        // contains jars)
        List<LibInfo> compiledAndroidLibraries = Lists.newArrayList();
        List<JarInfo> compiledJars = Lists.newArrayList();

        Set<? extends DependencyResult> dependencyResultSet = compileClasspath.getIncoming()
                .getResolutionResult().getRoot().getDependencies();

        for (DependencyResult dependencyResult : dependencyResultSet) {
            if (dependencyResult instanceof ResolvedDependencyResult) {
                addDependency(
                        ((ResolvedDependencyResult) dependencyResult).getSelected(),
                        variantDeps,
                        compiledAndroidLibraries,
                        compiledJars,
                        foundLibraries,
                        foundJars,
                        artifacts,
                        reverseMap,
                        currentUnresolvedDependencies,
                        testedProjectPath,
                        Collections.<String>emptyList(),
                        0);
            } else if (dependencyResult instanceof UnresolvedDependencyResult) {
                ComponentSelector attempted = ((UnresolvedDependencyResult) dependencyResult).getAttempted();
                if (attempted != null) {
                    currentUnresolvedDependencies.add(attempted.toString());
                }
            }
        }

        // then the packaged ones.
        List<LibInfo> packagedAndroidLibraries = Lists.newArrayList();
        List<JarInfo> packagedJars = Lists.newArrayList();

        dependencyResultSet = packageClasspath.getIncoming()
                .getResolutionResult().getRoot().getDependencies();

        for (DependencyResult dependencyResult : dependencyResultSet) {
            if (dependencyResult instanceof ResolvedDependencyResult) {
                addDependency(
                        ((ResolvedDependencyResult) dependencyResult).getSelected(),
                        variantDeps,
                        packagedAndroidLibraries,
                        packagedJars,
                        foundLibraries,
                        foundJars,
                        artifacts,
                        reverseMap,
                        currentUnresolvedDependencies,
                        testedProjectPath,
                        Collections.<String>emptyList(),
                        0);
            } else if (dependencyResult instanceof UnresolvedDependencyResult) {
                ComponentSelector attempted = ((UnresolvedDependencyResult) dependencyResult)
                        .getAttempted();
                if (attempted != null) {
                    currentUnresolvedDependencies.add(attempted.toString());
                }
            }
        }

        // now look through both results.
        // 1. Handle the compile and package list of Libraries.
        // For Libraries:
        // Only library projects can support provided aar.
        // However, package(publish)-only are still not supported (they don't make sense).
        // For now, provided only dependencies will be kept normally in the compile-graph.
        // However we'll want to not include them in the resource merging.
        // For Applications:
        // All Android libraries must be in both lists.
        // ---
        // Since we reuse the same instance of LibInfo for identical modules
        // we can simply run through each list and look for libs that are in only one.
        // While the list of library is actually a graph, it's fine to look only at the
        // top level ones since the transitive ones are in the same scope as the direct libraries.
        List<LibInfo> copyOfPackagedLibs = Lists.newArrayList(packagedAndroidLibraries);
        boolean isLibrary = extraModelInfo.isLibrary();

        for (LibInfo lib : compiledAndroidLibraries) {
            if (!copyOfPackagedLibs.contains(lib)) {
                if (isLibrary || lib.isOptional()) {
                    lib.setIsOptional(true);
                } else {
                    //noinspection ConstantConditions
                    variantDeps.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                            lib.getResolvedCoordinates().toString(),
                            SyncIssue.TYPE_NON_JAR_PROVIDED_DEP,
                            String.format(
                                    "Project %s: provided dependencies can only be jars. %s is an Android Library.",
                                    project.getName(), lib.getResolvedCoordinates())));
                }
            } else {
                copyOfPackagedLibs.remove(lib);
            }
        }
        // at this stage copyOfPackagedLibs should be empty, if not, error.
        for (LibInfo lib : copyOfPackagedLibs) {
            //noinspection ConstantConditions
            variantDeps.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                    lib.getResolvedCoordinates().toString(),
                    SyncIssue.TYPE_NON_JAR_PACKAGE_DEP,
                    String.format(
                            "Project %s: apk dependencies can only be jars. %s is an Android Library.",
                            project.getName(), lib.getResolvedCoordinates())));
        }

        // 2. merge jar dependencies with a single list where items have packaged/compiled properties.
        // since we reuse the same instance of a JarInfo for identical modules, we can use an
        // Identity set (ie both compiledJars and packagedJars will contain the same instance
        // if it's both compiled and packaged)
        Set<JarInfo> jarInfoSet = Sets.newIdentityHashSet();

        // go through the graphs of dependencies (jars and libs) and gather all the transitive
        // jar dependencies.
        // At the same this we set the compiled/packaged properties.
        gatherJarDependencies(jarInfoSet, compiledJars, true /*compiled*/, false /*packaged*/);
        gatherJarDependencies(jarInfoSet, packagedJars, false /*compiled*/, true /*packaged*/);
        // at this step, we know that libraries have been checked and libraries can only
        // be in both compiled and packaged scope.
        gatherJarDependenciesFromLibraries(jarInfoSet, compiledAndroidLibraries);

        // the final list of JarDependency, created from the list of JarInfo.
        List<JarDependency> jars = Lists.newArrayListWithCapacity(jarInfoSet.size());

        // if this is a test dependencies (ie tested dependencies is non null), override
        // packaged attributes for jars that are already in the tested dependencies in order to
        // not package them twice (since the VM loads the classes of both APKs in the same
        // classpath and refuses to load the same class twice)
        if (testedVariantDeps != null) {
            List<JarDependency> jarDependencies = testedVariantDeps.getJarDependencies();

            // gather the tested dependencies
            Map<String, String> testedDeps = Maps.newHashMapWithExpectedSize(jarDependencies.size());

            for (JarDependency jar : jarDependencies) {
                if (jar.isPackaged()) {
                    MavenCoordinates coordinates = jar.getResolvedCoordinates();
                    //noinspection ConstantConditions
                    testedDeps.put(
                            computeVersionLessCoordinateKey(coordinates),
                            coordinates.getVersion());
                }
            }

            // now go through all the test dependencies and check we don't have the same thing.
            // Skip the ones that are already in the tested variant, and convert the rest
            // to the final immutable instance
            for (JarInfo jar : jarInfoSet) {
                if (jar.isPackaged()) {
                    MavenCoordinates coordinates = jar.getResolvedCoordinates();

                    String testedVersion = testedDeps.get(
                            computeVersionLessCoordinateKey(coordinates));
                    if (testedVersion != null) {
                        // same artifact, skip packaging of the dependency in the test app,
                        // whether the version is a match or not.

                        // if the dependency is present in both tested and test artifact,
                        // verify that they are the same version
                        if (!testedVersion.equals(coordinates.getVersion())) {
                            String artifactInfo =  coordinates.getGroupId() + ":" + coordinates.getArtifactId();
                            variantDeps.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                                    artifactInfo,
                                    SyncIssue.TYPE_MISMATCH_DEP,
                                    String.format(
                                            "Conflict with dependency '%s'. Resolved versions for app (%s) and test app (%s) differ.",
                                            artifactInfo,
                                            testedVersion,
                                            coordinates.getVersion())));

                        } else {
                            logger.info(String.format(
                                    "Removed '%s' from packaging of %s: Already in tested package.",
                                    coordinates,
                                    variantDeps.getName()));
                        }
                    } else {
                        // new artifact, convert it.
                        jars.add(jar.createJarDependency());
                    }
                }
            }
        } else {
            // just convert all of them to JarDependency
            for (JarInfo jarInfo : jarInfoSet) {
                jars.add(jarInfo.createJarDependency());
            }
        }

        // --- Handle the local jar dependencies ---

        // also need to process local jar files, as they are not processed by the
        // resolvedConfiguration result. This only includes the local jar files for this project.
        Set<File> localCompiledJars = Sets.newHashSet();
        for (Dependency dependency : compileClasspath.getAllDependencies()) {
            if (dependency instanceof SelfResolvingDependency &&
                    !(dependency instanceof ProjectDependency)) {
                Set<File> files = ((SelfResolvingDependency) dependency).resolve();
                for (File f : files) {
                    if (DEBUG_DEPENDENCY) {
                        System.out.println("LOCAL compile: " + f.getName());
                    }
                    // only accept local jar, no other types.
                    if (!f.getName().toLowerCase(Locale.getDefault()).endsWith(DOT_JAR)) {
                        variantDeps.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                                f.getAbsolutePath(),
                                SyncIssue.TYPE_NON_JAR_LOCAL_DEP,
                                String.format(
                                        "Project %s: Only Jar-type local dependencies are supported. Cannot handle: %s",
                                        project.getName(), f.getAbsolutePath())));
                    } else {
                        localCompiledJars.add(f);
                    }
                }
            }
        }

        Set<File> localPackagedJars = Sets.newHashSet();
        for (Dependency dependency : packageClasspath.getAllDependencies()) {
            if (dependency instanceof SelfResolvingDependency &&
                    !(dependency instanceof ProjectDependency)) {
                Set<File> files = ((SelfResolvingDependency) dependency).resolve();
                for (File f : files) {
                    if (DEBUG_DEPENDENCY) {
                        System.out.println("LOCAL package: " + f.getName());
                    }
                    // only accept local jar, no other types.
                    if (!f.getName().toLowerCase(Locale.getDefault()).endsWith(DOT_JAR)) {
                        variantDeps.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                                f.getAbsolutePath(),
                                SyncIssue.TYPE_NON_JAR_LOCAL_DEP,
                                String.format(
                                        "Project %s: Only Jar-type local dependencies are supported. Cannot handle: %s",
                                        project.getName(), f.getAbsolutePath())));
                    } else {
                        localPackagedJars.add(f);
                    }
                }
            }
        }

        // loop through both the compiled and packaged jar to compute the list
        // of jars that are: compile-only, package-only, or both.
        Map<File, JarDependency> localJars = Maps.newHashMap();
        for (File file : localCompiledJars) {
            localJars.put(file, new JarDependency(
                    file,
                    true /*compiled*/,
                    localPackagedJars.contains(file) /*packaged*/,
                    null /*resolvedCoordinates*/,
                    null /*projectPath*/));
        }

        for (File file : localPackagedJars) {
            if (!localCompiledJars.contains(file)) {
                localJars.put(file, new JarDependency(
                        file,
                        false /*compiled*/,
                        true /*packaged*/,
                        null /*resolvedCoordinates*/,
                        null /*projectPath*/));
            }
        }

        if (extraModelInfo.getMode() != STANDARD &&
                compileClasspath.getResolvedConfiguration().hasError()) {
            for (String dependency : currentUnresolvedDependencies) {
                extraModelInfo.handleSyncError(
                        dependency,
                        SyncIssue.TYPE_UNRESOLVED_DEPENDENCY,
                        String.format(
                                "Unable to resolve dependency '%s'",
                                dependency));
            }
        }

        // convert the LibInfo in LibraryDependencyImpl and update the reverseMap
        // with the converted keys
        List<LibraryDependencyImpl> libList = convertLibraryInfoIntoDependency(
                compiledAndroidLibraries, reverseMap);

        if (DEBUG_DEPENDENCY) {
            for (LibraryDependency lib : libList) {
                System.out.println("LIB: " + lib);
            }
            for (JarDependency jar : jars) {
                System.out.println("JAR: " + jar);
            }
            for (JarDependency jar : localJars.values()) {
                System.out.println("LOCAL-JAR: " + jar);
            }
        }

        variantDeps.addLibraries(libList);
        variantDeps.addJars(jars);
        variantDeps.addLocalJars(localJars.values());

        configureBuild(variantDeps);

        if (DEBUG_DEPENDENCY) {
            System.out.println(project.getName() + ":" + compileClasspath.getName() + "/" +packageClasspath.getName());
            System.out.println("<<<<<<<<<<");
        }

    }

    private static List<LibraryDependencyImpl> convertLibraryInfoIntoDependency(
            @NonNull List<LibInfo> libInfos,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap) {
        List<LibraryDependencyImpl> list = Lists.newArrayListWithCapacity(libInfos.size());

        // since the LibInfos is a graph and the previous "foundLibraries" map ensure we reuse
        // instance where applicable, we'll create a map to keep track of what we have already
        // converted.
        Map<LibInfo, LibraryDependencyImpl> convertedMap = Maps.newIdentityHashMap();

        for (LibInfo libInfo : libInfos) {
            list.add(convertLibInfo(libInfo, reverseMap, convertedMap));
        }

        return list;
    }

    private static LibraryDependencyImpl convertLibInfo(
            @NonNull LibInfo libInfo,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap,
            @NonNull Map<LibInfo, LibraryDependencyImpl> convertedMap) {
        LibraryDependencyImpl convertedLib = convertedMap.get(libInfo);
        if (convertedLib == null) {
            // first, convert the children.
            @SuppressWarnings("unchecked")
            List<LibInfo> children = (List<LibInfo>) (List<?>) libInfo.getDependencies();
            List<LibraryDependency> convertedChildren = Lists.newArrayListWithCapacity(children.size());

            for (LibInfo child : children) {
                convertedChildren.add(convertLibInfo(child, reverseMap, convertedMap));
            }

            // now convert the libInfo
            convertedLib = new LibraryDependencyImpl(
                    libInfo.getBundle(),
                    libInfo.getFolder(),
                    convertedChildren,
                    libInfo.getName(),
                    libInfo.getProjectVariant(),
                    libInfo.getProject(),
                    libInfo.getRequestedCoordinates(),
                    libInfo.getResolvedCoordinates(),
                    libInfo.isOptional());

            // add it to the map
            convertedMap.put(libInfo, convertedLib);

            // and update the reversemap
            // get the items associated with the libInfo. Put in a fresh list as the returned
            // collection is backed by the content of the map.
            Collection<VariantDependencies> values = Lists.newArrayList(reverseMap.get(libInfo));
            reverseMap.removeAll(libInfo);
            reverseMap.putAll(convertedLib, values);
        }

        return convertedLib;
    }

    private static void gatherJarDependencies(
            Set<JarInfo> outJarInfos,
            Collection<JarInfo> inJarInfos,
            boolean compiled,
            boolean packaged) {
        for (JarInfo jarInfo : inJarInfos) {
            if (!outJarInfos.contains(jarInfo)) {
                outJarInfos.add(jarInfo);
            }

            if (compiled) {
                jarInfo.setCompiled(true);
            }
            if (packaged) {
                jarInfo.setPackaged(true);
            }

            gatherJarDependencies(outJarInfos, jarInfo.getDependencies(), compiled, packaged);
        }
    }

    private static void gatherJarDependenciesFromLibraries(
            Set<JarInfo> outJarInfos,
            Collection<LibInfo> inLibraryDependencies) {
        for (LibInfo libInfo : inLibraryDependencies) {
            gatherJarDependencies(outJarInfos, libInfo.getJarDependencies(),
                    true, !libInfo.isOptional());

            gatherJarDependenciesFromLibraries(
                    outJarInfos,
                    libInfo.getLibInfoDependencies());
        }
    }

    private void ensureConfigured(Configuration config) {
        for (Dependency dependency : config.getAllDependencies()) {
            if (dependency instanceof ProjectDependency) {
                ProjectDependency projectDependency = (ProjectDependency) dependency;
                project.evaluationDependsOn(projectDependency.getDependencyProject().getPath());
                try {
                    ensureConfigured(projectDependency.getProjectConfiguration());
                } catch (Throwable e) {
                    throw new UnknownProjectException(String.format(
                            "Cannot evaluate module %s : %s",
                            projectDependency.getName(), e.getMessage()),
                            e);
                }
            }
        }
    }

    private void collectArtifacts(
            Configuration configuration,
            Map<ModuleVersionIdentifier,
                    List<ResolvedArtifact>> artifacts) {

        Set<ResolvedArtifact> allArtifacts;
        if (extraModelInfo.getMode() != STANDARD) {
            allArtifacts = configuration.getResolvedConfiguration().getLenientConfiguration().getArtifacts(
                    Specs.satisfyAll());
        } else {
            allArtifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        }

        for (ResolvedArtifact artifact : allArtifacts) {
            ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
            List<ResolvedArtifact> moduleArtifacts = artifacts.get(id);

            if (moduleArtifacts == null) {
                moduleArtifacts = Lists.newArrayList();
                artifacts.put(id, moduleArtifacts);
            }

            if (!moduleArtifacts.contains(artifact)) {
                moduleArtifacts.add(artifact);
            }
        }
    }

    private static void printIndent(int indent, @NonNull String message) {
        for (int i = 0 ; i < indent ; i++) {
            System.out.print("\t");
        }

        System.out.println(message);
    }

    private void addDependency(
            @NonNull ResolvedComponentResult resolvedComponentResult,
            @NonNull VariantDependencies configDependencies,
            @NonNull Collection<LibInfo> outLibraries,
            @NonNull List<JarInfo> outJars,
            @NonNull Map<ModuleVersionIdentifier, List<LibInfo>> alreadyFoundLibraries,
            @NonNull Map<ModuleVersionIdentifier, List<JarInfo>> alreadyFoundJars,
            @NonNull Map<ModuleVersionIdentifier, List<ResolvedArtifact>> artifacts,
            @NonNull Multimap<LibraryDependency, VariantDependencies> reverseMap,
            @NonNull Set<String> currentUnresolvedDependencies,
            @Nullable String testedProjectPath,
            @NonNull List<String> projectChain,
            int indent) {

        ModuleVersionIdentifier moduleVersion = resolvedComponentResult.getModuleVersion();
        if (configDependencies.getChecker().excluded(moduleVersion)) {
            return;
        }

        if (moduleVersion.getName().equals("support-annotations") &&
                moduleVersion.getGroup().equals("com.android.support")) {
            configDependencies.setAnnotationsPresent(true);
        }

        List<LibInfo> libsForThisModule = alreadyFoundLibraries.get(moduleVersion);
        List<JarInfo> jarsForThisModule = alreadyFoundJars.get(moduleVersion);

        if (libsForThisModule != null) {
            if (DEBUG_DEPENDENCY) {
                printIndent(indent, "FOUND LIB: " + moduleVersion.getName());
            }
            outLibraries.addAll(libsForThisModule);

            for (LibInfo lib : libsForThisModule) {
                reverseMap.put(lib, configDependencies);
            }

        } else if (jarsForThisModule != null) {
            if (DEBUG_DEPENDENCY) {
                printIndent(indent, "FOUND JAR: " + moduleVersion.getName());
            }
            outJars.addAll(jarsForThisModule);
        }
        else {
            if (DEBUG_DEPENDENCY) {
                printIndent(indent, "NOT FOUND: " + moduleVersion.getName());
            }
            // new module! Might be a jar or a library

            // get the nested components first.
            List<LibInfo> nestedLibraries = Lists.newArrayList();
            List<JarInfo> nestedJars = Lists.newArrayList();

            Set<? extends DependencyResult> dependencies = resolvedComponentResult.getDependencies();
            for (DependencyResult dependencyResult : dependencies) {
                if (dependencyResult instanceof ResolvedDependencyResult) {
                    ResolvedComponentResult selected =
                            ((ResolvedDependencyResult) dependencyResult).getSelected();

                    List<String> newProjectChain = projectChain;

                    ComponentIdentifier identifier = selected.getId();
                    if (identifier instanceof ProjectComponentIdentifier) {
                        String projectPath =
                                ((ProjectComponentIdentifier) identifier).getProjectPath();

                        int index = projectChain.indexOf(projectPath);
                        if (index != -1) {
                            projectChain.add(projectPath);
                            String path = Joiner
                                    .on(" -> ")
                                    .join(projectChain.subList(index, projectChain.size()));

                            throw new CircularReferenceException(
                                    "Circular reference between projects: " + path);
                        }

                        newProjectChain = Lists.newArrayList();
                        newProjectChain.addAll(projectChain);
                        newProjectChain.add(projectPath);
                    }

                    addDependency(
                            selected,
                            configDependencies,
                            nestedLibraries,
                            nestedJars,
                            alreadyFoundLibraries,
                            alreadyFoundJars,
                            artifacts,
                            reverseMap,
                            currentUnresolvedDependencies,
                            testedProjectPath,
                            newProjectChain,
                            indent + 1);
                } else if (dependencyResult instanceof UnresolvedDependencyResult) {
                    ComponentSelector attempted = ((UnresolvedDependencyResult) dependencyResult).getAttempted();
                    if (attempted != null) {
                        currentUnresolvedDependencies.add(attempted.toString());
                    }
                }
            }

            if (DEBUG_DEPENDENCY) {
                printIndent(indent, "BACK2: " + moduleVersion.getName());
                printIndent(indent, "NESTED LIBS: " + nestedLibraries.size());
                printIndent(indent, "NESTED JARS: " + nestedJars.size());
            }

            // now loop on all the artifact for this modules.
            List<ResolvedArtifact> moduleArtifacts = artifacts.get(moduleVersion);

            ComponentIdentifier id = resolvedComponentResult.getId();
            String gradlePath = (id instanceof ProjectComponentIdentifier) ?
                    ((ProjectComponentIdentifier) id).getProjectPath() : null;

            if (moduleArtifacts != null) {
                for (ResolvedArtifact artifact : moduleArtifacts) {
                    if (EXT_LIB_ARCHIVE.equals(artifact.getExtension())) {
                        if (DEBUG_DEPENDENCY) {
                            printIndent(indent, "TYPE: AAR");
                        }
                        if (libsForThisModule == null) {
                            libsForThisModule = Lists.newArrayList();
                            alreadyFoundLibraries.put(moduleVersion, libsForThisModule);
                        }

                        String path = computeArtifactPath(moduleVersion, artifact);
                        String name = computeArtifactName(moduleVersion, artifact);

                        if (DEBUG_DEPENDENCY) {
                            printIndent(indent, "NAME: " + name);
                            printIndent(indent, "PATH: " + path);
                        }

                        //def explodedDir = project.file("$project.rootProject.buildDir/${FD_INTERMEDIATES}/exploded-aar/$path")
                        File explodedDir = project.file(project.getBuildDir() + "/" + FD_INTERMEDIATES + "/exploded-aar/" + path);
                        @SuppressWarnings("unchecked")
                        LibInfo libInfo = new LibInfo(
                                artifact.getFile(),
                                explodedDir,
                                (List<LibraryDependency>) (List<?>) nestedLibraries,
                                nestedJars,
                                name,
                                artifact.getClassifier(),
                                gradlePath,
                                null /*requestedCoordinates*/,
                                new MavenCoordinatesImpl(artifact));

                        libsForThisModule.add(libInfo);
                        outLibraries.add(libInfo);
                        reverseMap.put(libInfo, configDependencies);

                    } else if (EXT_JAR.equals(artifact.getExtension())) {
                        if (DEBUG_DEPENDENCY) {
                            printIndent(indent, "TYPE: JAR");
                        }
                        // check this jar does not have a dependency on an library, as this would not work.
                        if (!nestedLibraries.isEmpty()) {
                            if (testedProjectPath != null && testedProjectPath.equals(gradlePath)) {
                                // TODO: make sure this is a direct dependency and not a transitive one.
                                // add nested libs as optional somehow...
                                for (LibInfo lib : nestedLibraries) {
                                    lib.setIsOptional(true);
                                }
                                outLibraries.addAll(nestedLibraries);

                            } else {
                                configDependencies.getChecker()
                                        .addSyncIssue(extraModelInfo.handleSyncError(
                                                new MavenCoordinatesImpl(artifact).toString(),
                                                SyncIssue.TYPE_JAR_DEPEND_ON_AAR,
                                                String.format(
                                                        "Module '%s' depends on one or more Android Libraries but is a jar",
                                                        moduleVersion)));
                            }
                        }

                        if (jarsForThisModule == null) {
                            jarsForThisModule = Lists.newArrayList();
                            alreadyFoundJars.put(moduleVersion, jarsForThisModule);
                        }

                        JarInfo jarInfo = new JarInfo(
                                artifact.getFile(),
                                new MavenCoordinatesImpl(artifact),
                                gradlePath,
                                nestedJars);
                        if (DEBUG_DEPENDENCY) {
                            printIndent(indent, "JAR-INFO: " + jarInfo.toString());
                        }

                        jarsForThisModule.add(jarInfo);
                        outJars.add(jarInfo);

                    } else if (EXT_ANDROID_PACKAGE.equals(artifact.getExtension())) {
                        String name = computeArtifactName(moduleVersion, artifact);

                        configDependencies.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                                name,
                                SyncIssue.TYPE_DEPENDENCY_IS_APK,
                                String.format(
                                        "Dependency %s on project %s resolves to an APK archive " +
                                        "which is not supported as a compilation dependency. File: %s",
                                        name, project.getName(), artifact.getFile())));
                    } else if ("apklib".equals(artifact.getExtension())) {
                        String name = computeArtifactName(moduleVersion, artifact);

                        configDependencies.getChecker().addSyncIssue(extraModelInfo.handleSyncError(
                                name,
                                SyncIssue.TYPE_DEPENDENCY_IS_APKLIB,
                                String.format(
                                        "Packaging for dependency %s is 'apklib' and is not supported. " +
                                        "Only 'aar' libraries are supported.", name)));
                    } else {
                        String name = computeArtifactName(moduleVersion, artifact);

                        logger.warning(String.format(
                                        "Unrecognized dependency: '%s' (type: '%s', extension: '%s')",
                                        name, artifact.getType(), artifact.getExtension()));
                    }
                }
            }

            if (DEBUG_DEPENDENCY) {
                printIndent(indent, "DONE: " + moduleVersion.getName());
            }
        }
    }

    @NonNull
    private String computeArtifactPath(
            @NonNull ModuleVersionIdentifier moduleVersion,
            @NonNull ResolvedArtifact artifact) {
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder.append(normalize(logger, moduleVersion, moduleVersion.getGroup()))
                .append('/')
                .append(normalize(logger, moduleVersion, moduleVersion.getName()))
                .append('/')
                .append(normalize(logger, moduleVersion,
                        moduleVersion.getVersion()));

        if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
            pathBuilder.append('/').append(normalize(logger, moduleVersion,
                    artifact.getClassifier()));
        }

        return pathBuilder.toString();
    }

    @NonNull
    private static String computeArtifactName(
            @NonNull ModuleVersionIdentifier moduleVersion,
            @NonNull ResolvedArtifact artifact) {
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(moduleVersion.getGroup())
                .append(':')
                .append(moduleVersion.getName())
                .append(':')
                .append(moduleVersion.getVersion());

        if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
            nameBuilder.append(':').append(artifact.getClassifier());
        }

        return nameBuilder.toString();
    }

    /**
     * Normalize a path to remove all illegal characters for all supported operating systems.
     * {@see http://en.wikipedia.org/wiki/Filename#Comparison%5Fof%5Ffile%5Fname%5Flimitations}
     *
     * @param id the module coordinates that generated this path
     * @param path the proposed path name
     * @return the normalized path name
     */
    static String normalize(ILogger logger, ModuleVersionIdentifier id, String path) {
        if (path == null || path.isEmpty()) {
            logger.info(String.format(
                    "When unzipping library '%s:%s:%s, either group, name or version is empty",
                    id.getGroup(), id.getName(), id.getVersion()));
            return path;
        }
        // list of illegal characters
        String normalizedPath = path.replaceAll("[%<>:\"/?*\\\\]", "@");
        if (normalizedPath == null || normalizedPath.isEmpty()) {
            // if the path normalization failed, return the original path.
            logger.info(String.format(
                    "When unzipping library '%s:%s:%s, the normalized '%s' is empty",
                    id.getGroup(), id.getName(), id.getVersion(), path));
            return path;
        }
        try {
            int pathPointer = normalizedPath.length() - 1;
            // do not end your path with either a dot or a space.
            String suffix = "";
            while (pathPointer >= 0 && (normalizedPath.charAt(pathPointer) == '.'
                    || normalizedPath.charAt(pathPointer) == ' ')) {
                pathPointer--;
                suffix += "@";
            }
            if (pathPointer < 0) {
                throw new RuntimeException(String.format(
                        "When unzipping library '%s:%s:%s, " +
                        "the path '%s' cannot be transformed into a valid directory name",
                        id.getGroup(), id.getName(), id.getVersion(), path));
            }
            return normalizedPath.substring(0, pathPointer + 1) + suffix;
        } catch (Exception e) {
            logger.error(e, String.format(
                    "When unzipping library '%s:%s:%s', " +
                    "Path normalization failed for input %s",
                    id.getGroup(), id.getName(), id.getVersion(), path));
            return path;
        }
    }

    private void configureBuild(VariantDependencies configurationDependencies) {
        addDependsOnTaskInOtherProjects(
                project.getTasks().getByName(JavaBasePlugin.BUILD_NEEDED_TASK_NAME), true,
                JavaBasePlugin.BUILD_NEEDED_TASK_NAME, "compile");
        addDependsOnTaskInOtherProjects(
                project.getTasks().getByName(JavaBasePlugin.BUILD_DEPENDENTS_TASK_NAME), false,
                JavaBasePlugin.BUILD_DEPENDENTS_TASK_NAME, "compile");
    }

    @NonNull
    public static List<ManifestDependencyImpl> getManifestDependencies(
            List<LibraryDependency> libraries) {

        List<ManifestDependencyImpl> list = Lists.newArrayListWithCapacity(libraries.size());

        for (LibraryDependency lib : libraries) {
            // get the dependencies
            List<ManifestDependencyImpl> children = getManifestDependencies(lib.getDependencies());
            list.add(new ManifestDependencyImpl(lib.getName(), lib.getManifest(), children));
        }

        return list;
    }

    /**
     * Adds a dependency on tasks with the specified name in other projects.  The other projects
     * are determined from project lib dependencies using the specified configuration name.
     * These may be projects this project depends on or projects that depend on this project
     * based on the useDependOn argument.
     *
     * @param task Task to add dependencies to
     * @param useDependedOn if true, add tasks from projects this project depends on, otherwise
     * use projects that depend on this one.
     * @param otherProjectTaskName name of task in other projects
     * @param configurationName name of configuration to use to find the other projects
     */
    private static void addDependsOnTaskInOtherProjects(final Task task, boolean useDependedOn,
            String otherProjectTaskName,
            String configurationName) {
        Project project = task.getProject();
        final Configuration configuration = project.getConfigurations().getByName(
                configurationName);
        task.dependsOn(configuration.getTaskDependencyFromProjectDependency(
                useDependedOn, otherProjectTaskName));
    }

    /**
     * Compute a version-less key representing the given coordinates.
     * @param coordinates the coordinate
     * @return the key.
     */
    @NonNull
    private static String computeVersionLessCoordinateKey(@NonNull MavenCoordinates coordinates) {
        StringBuilder sb = new StringBuilder(coordinates.getGroupId());
        sb.append(':').append(coordinates.getArtifactId());
        if (coordinates.getClassifier() != null) {
            sb.append(':').append(coordinates.getClassifier());
        }
        return sb.toString();
    }
}
