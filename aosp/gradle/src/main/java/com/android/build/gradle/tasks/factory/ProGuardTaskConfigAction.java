package com.android.build.gradle.tasks.factory;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import proguard.ParseException;
import proguard.gradle.ProGuardTask;

/**
 * Configuration Action for a ProGuardTask task.
 */
public class ProGuardTaskConfigAction implements TaskConfigAction<ProGuardTask> {

    private VariantScope scope;

    private Callable<List<File>> inputFiles;

    public ProGuardTaskConfigAction(VariantScope scope, PostCompilationData pcData) {
        this.scope = scope;
        this.inputFiles = pcData.getInputFilesCallable();
    }

    @Override
    public String getName() {
        return scope.getTaskName("shrink", "MultiDexComponents");
    }

    @Override
    public Class<ProGuardTask> getType() {
        return ProGuardTask.class;
    }

    @Override
    public void execute(ProGuardTask proguardComponentsTask) {
        proguardComponentsTask.dontobfuscate();
        proguardComponentsTask.dontoptimize();
        proguardComponentsTask.dontpreverify();
        proguardComponentsTask.dontwarn();
        proguardComponentsTask.forceprocessing();

        try {
            proguardComponentsTask.configuration(scope.getManifestKeepListFile());

            proguardComponentsTask.libraryjars(new Callable<File>() {
                @Override
                public File call() throws Exception {
                    Preconditions.checkNotNull(
                            scope.getGlobalScope().getAndroidBuilder().getTargetInfo());
                    File shrinkedAndroid = new File(
                            scope.getGlobalScope().getAndroidBuilder().getTargetInfo()
                                    .getBuildTools()
                                    .getLocation(),
                            "lib" + File.separatorChar + "shrinkedAndroid.jar");

                    // TODO remove in 1.0
                    // STOPSHIP
                    if (!shrinkedAndroid.isFile()) {
                        shrinkedAndroid = new File(
                                scope.getGlobalScope().getAndroidBuilder().getTargetInfo()
                                        .getBuildTools().getLocation(),
                                "multidex" + File.separatorChar + "shrinkedAndroid.jar");
                    }

                    return shrinkedAndroid;
                }
            });

            proguardComponentsTask.injars(new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return inputFiles.call().iterator().next();
                }
            });

            proguardComponentsTask.outjars(scope.getProguardComponentsJarFile());

            proguardComponentsTask.printconfiguration(
                    scope.getGlobalScope().getBuildDir() + "/" + FD_INTERMEDIATES
                            + "/multi-dex/" + scope.getVariantConfiguration().getDirName()
                            + "/components.flags");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
