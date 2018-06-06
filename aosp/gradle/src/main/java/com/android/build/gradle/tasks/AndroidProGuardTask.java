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

package com.android.build.gradle.tasks;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.DependencyManager;
import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.tasks.Job;
import com.android.builder.tasks.JobContext;
import com.android.ide.common.packaging.PackagingUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import groovy.lang.Closure;
import proguard.ParseException;
import proguard.gradle.ProGuardTask;

import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

/**
 * Decoration for the {@link ProGuardTask} so it implements shared interfaces with our custom
 * tasks.
 */
public class AndroidProGuardTask extends ProGuardTask implements FileSupplier, JavaResourcesProvider {

    /**
     * resulting obfuscation mapping file.
     */
    @Nullable
    @InputFile
    @Optional
    File mappingFile;

    File obfuscatedClassesJar;

    @Override
    public void printmapping(Object printMapping) throws ParseException {
        mappingFile = (File) printMapping;
        super.printmapping(printMapping);
    }

    @Override
    public void applymapping(Object applyMapping) {
    }

    @Override
    public File get() {
        return mappingFile;
    }

    @NonNull
    @Override
    public Task getTask() {
        return this;
    }

    @Override
    @TaskAction
    public void proguard() {
        final Job<Void> job = new Job<Void>(getName(),
                new com.android.builder.tasks.Task<Void>() {
                    @Override
                    public void run(@NonNull Job<Void> job,
                                    @NonNull JobContext<Void> context) throws IOException {
                        try {
                            AndroidProGuardTask.this.doMinification();
                        } catch (ParseException e) {
                            throw new IOException(e);
                        }
                    }
                });
        try {
            SimpleWorkQueue.push(job);

            // wait for the task completion.
            job.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

    }

    public void doMinification() throws ParseException, IOException {
        // only set the tested application mapping file if it exists (it must at this point or that
        // means the tested application did not request obfuscation).
        super.proguard();
    }

    @NonNull
    @Override
    public ImmutableList<JavaResourcesLocation> getJavaResourcesLocations() {
        return ImmutableList.of(new JavaResourcesLocation(Type.JAR, obfuscatedClassesJar));
    }

    @SuppressWarnings("unchecked")
    public static class ConfigAction implements TaskConfigAction<AndroidProGuardTask> {

        private VariantScope scope;

        private Callable<File> inputDir;
        private Callable<File> javaResourcesInputDir;

        private Callable<List<File>> inputLibraries;

        public ConfigAction(VariantScope scope, final PostCompilationData pcData) {
            this.scope = scope;
            inputDir = pcData.getInputDirCallable();
            javaResourcesInputDir = pcData.getJavaResourcesInputDirCallable();
            inputLibraries = pcData.getInputLibrariesCallable();
        }

        @Override
        public String getName() {
            return scope.getTaskName("proguard");
        }

        @Override
        public Class<AndroidProGuardTask> getType() {
            return AndroidProGuardTask.class;
        }

        @Override
        public void execute(final AndroidProGuardTask proguardTask) {
            final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
            final VariantConfiguration variantConfig = scope.getVariantData().getVariantConfiguration();

            // use single output for now.
            final BaseVariantOutputData variantOutputData = scope.getVariantData().getOutputs().get(0);

            variantData.obfuscationTask = proguardTask;
            variantData.mappingFileProviderTask = proguardTask;

            // --- Output File ---

            proguardTask.obfuscatedClassesJar = scope.getProguardOutputFile();

            // --- Proguard Config ---

            try {

                proguardTask.configuration(new Callable<Collection<File>>() {
                    @Override
                    public Collection<File> call() {
                        List<File> proguardFiles = variantConfig.getProguardFiles(true,
                                Collections.singletonList(getDefaultProguardFile(
                                        TaskManager.DEFAULT_PROGUARD_CONFIG_FILE)));
                        proguardFiles.add(
                                variantOutputData.processResourcesTask.getProguardOutputFile());
                        return proguardFiles;
                    }
                });

                // --- InJars / LibraryJars ---

                if (variantData instanceof LibraryVariantData) {
                    String packageName = variantConfig.getPackageFromManifest();
                    if (packageName == null) {
                        throw new BuildException("Failed to read manifest", null);
                    }

                    packageName = packageName.replace(".", "/");

                    // injar: the compilation output
                    // exclude R files and such from output
                    String exclude = "!" + packageName + "/R.class";
                    exclude += (", !" + packageName + "/R$*.class");
                    if (!scope.getGlobalScope().getExtension().getPackageBuildConfig()) {
                        exclude += (", !" + packageName + "/Manifest.class");
                        exclude += (", !" + packageName + "/Manifest$*.class");
                        exclude += (", !" + packageName + "/BuildConfig.class");
                    }

                    proguardTask.injars(ImmutableMap.of("filter", exclude), inputDir);

                    // include R files and such for compilation
                    String include = exclude.replace("!", "");
                    LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>(1);
                    map1.put("filter", include);
                    proguardTask.libraryjars(map1, inputDir);

                    // injar: the local dependencies
                    Callable inJars = new Callable<List<File>>() {
                        @Override
                        public List<File> call() {
                            return DependencyManager
                                    .getPackagedLocalJarFileList(variantData.getVariantDependency());
                        }
                    };

                    proguardTask.injars(ImmutableMap.of("filter", "!META-INF/MANIFEST.MF"), inJars);

                    // libjar: the library dependencies. In this case we take all the compile-scope
                    // dependencies
                    Callable libJars = new Callable<Iterable<File>>() {
                        @Override
                        public Iterable<File> call() {
                            // get all the compiled jar.
                            Set<File> compiledJars = scope.getGlobalScope().getAndroidBuilder()
                                    .getCompileClasspath(variantConfig);
                            // and remove local jar that are also packaged
                            final List<File> localJars = DependencyManager
                                    .getPackagedLocalJarFileList(variantData.getVariantDependency());

                            return Iterables.filter(compiledJars, new Predicate<File>() {
                                @Override
                                public boolean apply(File file) {
                                    return !localJars.contains(file);
                                }
                            });
                        }
                    };

                    proguardTask.libraryjars(ImmutableMap.of("filter", "!META-INF/MANIFEST.MF"), libJars);

                    // ensure local jars keep their package names
                    proguardTask.keeppackagenames();
                } else {
                    // injar: the compilation output
                    proguardTask.injars(inputDir);
                    if (javaResourcesInputDir != null) {
                        proguardTask.injars(javaResourcesInputDir);
                    }

                    // injar: the packaged dependencies
                    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);

                    // add a filter to explicitly add files of the following extensions to the
                    // resulting proguarded classes.jar by only including the extensions that were
                    // not filtered by the libraries resource extraction task.
                    String filter = Joiner.on(", **/*.").join(
                            PackagingUtils.NON_RESOURCES_EXTENSIONS);

                    map.put("filter", "!META-INF/MANIFEST.MF" + (Strings.isNullOrEmpty(filter)
                            ? ""
                            : ", **/*." + filter));
                    proguardTask.injars(map, inputLibraries);

                    // the provided-only jars as libraries.
                    Callable<List<File>> libJars = new Callable<List<File>>() {
                        @Override
                        public List<File> call() {
                            return variantData.getVariantConfiguration().getProvidedOnlyJars();
                        }
                    };

                    proguardTask.libraryjars(libJars);
                }

                // libraryJars: the runtime jars. Do this in doFirst since the boot classpath isn't
                // available until the SDK is loaded in the prebuild task
                proguardTask.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task proguardTask) {
                        for (String runtimeJar : scope.getGlobalScope().getAndroidBuilder()
                                .getBootClasspathAsStrings()) {
                            try {
                                ((AndroidProGuardTask) proguardTask).libraryjars(runtimeJar);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

                // --- Out files ---

                proguardTask.outjars(scope.getProguardOutputFile());

                final File proguardOut = new File(
                        String.valueOf(scope.getGlobalScope().getBuildDir()) + "/" + FD_OUTPUTS
                                + "/mapping/" + variantData.getVariantConfiguration().getDirName());

                proguardTask.dump(new File(proguardOut, "dump.txt"));
                proguardTask.printseeds(new File(proguardOut, "seeds.txt"));
                proguardTask.printusage(new File(proguardOut, "usage.txt"));
                proguardTask.printmapping(new File(proguardOut, "mapping.txt"));

                // proguard doesn't verify that the seed/mapping/usage folders exist and will fail
                // if they don't so create them.
                proguardTask.doFirst(new Closure<Boolean>(this, this) {
                    public Boolean doCall(Task it) {
                        return proguardOut.mkdirs();
                    }

                    public Boolean doCall() {
                        return doCall(null);
                    }

                });
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File getDefaultProguardFile(String name) {
            File sdkDir = scope.getGlobalScope().getSdkHandler().getAndCheckSdkFolder();
            return new File(sdkDir,
                    SdkConstants.FD_TOOLS + File.separatorChar
                            + SdkConstants.FD_PROGUARD + File.separatorChar
                            + name);
        }
    }
}
