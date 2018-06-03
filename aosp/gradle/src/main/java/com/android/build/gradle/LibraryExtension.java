package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.LoggingUtil;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.builder.core.AndroidBuilder;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.reflect.Instantiator;

/**
 * 'android' extension for 'com.android.library' project.
 */
public class LibraryExtension extends TestedExtension {

    private final DefaultDomainObjectSet<LibraryVariant> libraryVariantList
            = new DefaultDomainObjectSet<LibraryVariant>(LibraryVariant.class);

    private boolean packageBuildConfig = true;

    public LibraryExtension(@NonNull ProjectInternal project, @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder, @NonNull SdkHandler sdkHandler,
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs,
            @NonNull ExtraModelInfo extraModelInfo, boolean isLibrary) {
        super(project, instantiator, androidBuilder, sdkHandler, buildTypes, productFlavors,
                signingConfigs, extraModelInfo, isLibrary);
    }

    /**
     * Returns the list of library variants. Since the collections is built after evaluation, it
     * should be used with Gradle's <code>all</code> iterator to process future items.
     */
    public DefaultDomainObjectSet<LibraryVariant> getLibraryVariants() {
        return libraryVariantList;
    }

    @Override
    public void addVariant(BaseVariant variant) {
        libraryVariantList.add((LibraryVariant) variant);
    }

    // ---------------
    // TEMP for compatibility
    // STOPSHIP Remove in 1.0

    public void packageBuildConfig(boolean value) {
        if (!value) {
            LoggingUtil.displayDeprecationWarning(logger, project,
                    "Support for not packaging BuildConfig is deprecated and will be removed in 1.0");
        }

        packageBuildConfig = value;
    }

    public void setPackageBuildConfig(boolean value) {
        packageBuildConfig(value);
    }

    @Override
    public Boolean getPackageBuildConfig() {
        return packageBuildConfig;
    }
}
