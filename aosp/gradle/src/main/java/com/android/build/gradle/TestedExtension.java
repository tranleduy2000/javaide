package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.api.UnitTestVariant;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.builder.core.AndroidBuilder;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.reflect.Instantiator;

import static com.android.builder.core.VariantType.ANDROID_TEST;
import static com.android.builder.core.VariantType.UNIT_TEST;

/**
 * base 'android' extension for plugins that have a test component.
 */
public abstract class TestedExtension extends BaseExtension implements TestedAndroidConfig {

    private final DomainObjectSet<TestVariant> testVariantList =
            new DefaultDomainObjectSet<TestVariant>(TestVariant.class);

    private final DomainObjectSet<UnitTestVariant> unitTestVariantList =
            new DefaultDomainObjectSet<UnitTestVariant>(UnitTestVariant.class);

    private String testBuildType = "debug";

    public TestedExtension(@NonNull ProjectInternal project, @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder, @NonNull SdkHandler sdkHandler,
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs,
            @NonNull ExtraModelInfo extraModelInfo, boolean isLibrary) {
        super(project, instantiator, androidBuilder, sdkHandler, buildTypes, productFlavors,
                signingConfigs, extraModelInfo, isLibrary);

        getSourceSets().create(ANDROID_TEST.getPrefix());
        getSourceSets().create(UNIT_TEST.getPrefix());
    }

    /**
     * Returns the list of (Android) test variants. Since the collections is built after evaluation,
     * it should be used with Gradle's <code>all</code> iterator to process future items.
     */
    @Override
    @NonNull
    public DomainObjectSet<TestVariant> getTestVariants() {
        return testVariantList;
    }

    public void addTestVariant(TestVariant testVariant) {
        testVariantList.add(testVariant);
    }

    /**
     * Returns the list of (Android) test variants. Since the collections is built after evaluation,
     * it should be used with Gradle's <code>all</code> iterator to process future items.
     */
    @Override
    @NonNull
    public DomainObjectSet<UnitTestVariant> getUnitTestVariants() {
        return unitTestVariantList;
    }

    public void addUnitTestVariant(UnitTestVariant testVariant) {
        unitTestVariantList.add(testVariant);
    }

    /**
     * Name of the build type that will be used when running Android (on-device) tests.
     *
     * <p>Defaults to "debug".
     */
    @Override
    @NonNull
    public String getTestBuildType() {
        return testBuildType;
    }

    public void setTestBuildType(String testBuildType) {
        this.testBuildType = testBuildType;
    }
}
