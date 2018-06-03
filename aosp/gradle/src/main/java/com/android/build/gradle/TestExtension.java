package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.ExtraModelInfo;
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
 * 'android' extension for 'com.android.test' project.
 */
public class TestExtension extends BaseExtension implements TestAndroidConfig {

    private final DefaultDomainObjectSet<ApplicationVariant> applicationVariantList
            = new DefaultDomainObjectSet<ApplicationVariant>(ApplicationVariant.class);

    private String targetProjectPath = null;

    private String targetVariant = "debug";

    public TestExtension(@NonNull ProjectInternal project, @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder, @NonNull SdkHandler sdkHandler,
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs,
            @NonNull ExtraModelInfo extraModelInfo, boolean isLibrary) {
        super(project, instantiator, androidBuilder, sdkHandler, buildTypes, productFlavors,
                signingConfigs, extraModelInfo, isLibrary);
    }

    /**
     * Returns the list of Application variants. Since the collections is built after evaluation, it
     * should be used with Gradle's <code>all</code> iterator to process future items.
     */
    public DefaultDomainObjectSet<ApplicationVariant> getApplicationVariants() {
        return applicationVariantList;
    }

    @Override
    public void addVariant(BaseVariant variant) {
        applicationVariantList.add((ApplicationVariant) variant);
    }

    /**
     * Returns the Gradle path of the project that this test project tests.
     */
    @Override
    public String getTargetProjectPath() {
        return targetProjectPath;
    }

    public void setTargetProjectPath(String targetProjectPath) {
        checkWritability();
        this.targetProjectPath = targetProjectPath;
    }

    public void targetProjectPath(String targetProjectPath) {
        setTargetProjectPath(targetProjectPath);
    }

    /**
     * Returns the variant of the tested project.
     *
     * Default is 'debug'
     */
    @Override
    public String getTargetVariant() {
        return targetVariant;
    }

    public void setTargetVariant(String targetVariant) {
        checkWritability();
        this.targetVariant = targetVariant;
    }

    public void targetVariant(String targetVariant) {
        setTargetVariant(targetVariant);
    }
}
