package com.android.build.gradle;

import com.android.build.gradle.internal.DependencyManager;
import com.android.build.gradle.internal.LibraryTaskManager;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.variant.LibraryVariantFactory;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.builder.core.AndroidBuilder;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * Gradle plugin class for 'library' projects.
 */
public class LibraryPlugin extends BasePlugin implements Plugin<Project> {
    /**
     * Default assemble task for the default-published artifact. this is needed for
     * the prepare task on the consuming project.
     */
    private Task assembleDefault;

    public LibraryPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        super(instantiator, registry);
    }

    @Override
    public Class<? extends BaseExtension> getExtensionClass() {
        return LibraryExtension.class;
    }

    @Override
    protected VariantFactory createVariantFactory() {
        return new LibraryVariantFactory(instantiator, androidBuilder, extension);
    }

    @Override
    protected boolean isLibrary() {
        return true;
    }

    @Override
    protected TaskManager createTaskManager(Project project, AndroidBuilder androidBuilder, AndroidConfig extension, SdkHandler sdkHandler, DependencyManager dependencyManager, ToolingModelBuilderRegistry toolingRegistry) {
        return new LibraryTaskManager(project, androidBuilder, extension, sdkHandler, dependencyManager, toolingRegistry);
    }

    @Override
    public void apply(Project project) {
        super.apply(project);

        assembleDefault = project.getTasks().create("assembleDefault");
    }

    public Task getAssembleDefault() {
        return assembleDefault;
    }

    public void setAssembleDefault(Task assembleDefault) {
        this.assembleDefault = assembleDefault;
    }
}
