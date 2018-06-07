package com.android.build.gradle.tasks.factory;

import android.support.annotation.NonNull;

import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.builder.model.SourceProvider;

import org.gradle.api.tasks.Sync;

import java.io.File;

/**
 * Configuration Action for a ProcessJavaRes task.
 */
public class ProcessJavaResConfigAction implements TaskConfigAction<Sync> {
    private VariantScope scope;

    public ProcessJavaResConfigAction(VariantScope scope) {
        this.scope = scope;
    }

    @Override
    public String getName() {
        return scope.getTaskName("process", "JavaRes");
    }

    @Override
    public Class<Sync> getType() {
        return Sync.class;
    }

    @Override
    public void execute(@NonNull Sync processResources) {
        scope.getVariantData().processJavaResourcesTask = processResources;

        // set the input
        processResources.from(((AndroidSourceSet) scope.getVariantConfiguration().getDefaultSourceSet()).getResources().getSourceFiles());
        processResources.from(((AndroidSourceSet) scope.getVariantConfiguration().getBuildTypeSourceSet()).getResources().getSourceFiles());
        if (scope.getVariantConfiguration().hasFlavors()) {
            for (SourceProvider flavorSourceSet : scope.getVariantConfiguration().getFlavorSourceProviders()) {
                processResources.from(((AndroidSourceSet) flavorSourceSet).getResources().getSourceFiles());
            }

        }
        processResources.setDestinationDir(new File(scope.getSourceFoldersJavaResDestinationDir(), "src"));
    }

    public VariantScope getScope() {
        return scope;
    }

    public void setScope(VariantScope scope) {
        this.scope = scope;
    }
}
