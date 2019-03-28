/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.builder.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.dependency.SymbolFileProvider;
import com.android.builder.model.AaptOptions;
import com.android.ide.common.process.ProcessEnvBuilder;
import com.android.ide.common.process.ProcessInfo;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.resources.Density;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.IAndroidTarget;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds the ProcessInfo necessary for an aapt package invocation
 */
public class AaptPackageProcessBuilder extends ProcessEnvBuilder<AaptPackageProcessBuilder> {

    @NonNull private final File mManifestFile;
    @NonNull private final AaptOptions mOptions;
    @Nullable private File mResFolder;
    @Nullable private File mAssetsFolder;
    private boolean mVerboseExec = false;
    @Nullable private String mSourceOutputDir;
    @Nullable private String mSymbolOutputDir;
    @Nullable private List<? extends SymbolFileProvider> mLibraries;
    @Nullable private String mResPackageOutput;
    @Nullable private String mProguardOutput;
    @Nullable private VariantType mType;
    private boolean mDebuggable = false;
    private boolean mPseudoLocalesEnabled = false;
    @Nullable private Collection<String> mResourceConfigs;
    @Nullable Collection<String> mSplits;
    @Nullable String mPackageForR;
    @Nullable String mPreferredDensity;

    /**
     *
     * @param manifestFile the location of the manifest file
     * @param options the {@link com.android.builder.model.AaptOptions}
     */
    public AaptPackageProcessBuilder(
            @NonNull File manifestFile,
            @NonNull AaptOptions options) {
        checkNotNull(manifestFile, "manifestFile cannot be null.");
        checkNotNull(options, "options cannot be null.");
        mManifestFile = manifestFile;
        mOptions = options;
    }

    @NonNull
    public File getManifestFile() {
        return mManifestFile;
    }

    /**
     * @param resFolder the merged res folder
     * @return itself
     */
    public AaptPackageProcessBuilder setResFolder(@NonNull File resFolder) {
        if (!resFolder.isDirectory()) {
            throw new RuntimeException("resFolder parameter is not a directory");
        }
        mResFolder = resFolder;
        return this;
    }

    /**
     * @param assetsFolder the merged asset folder
     * @return itself
     */
    public AaptPackageProcessBuilder setAssetsFolder(@NonNull File assetsFolder) {
        if (!assetsFolder.isDirectory()) {
            throw new RuntimeException("assetsFolder parameter is not a directory");
        }
        mAssetsFolder = assetsFolder;
        return this;
    }

    /**
     * @param sourceOutputDir optional source folder to generate R.java
     * @return itself
     */
    public AaptPackageProcessBuilder setSourceOutputDir(@Nullable String sourceOutputDir) {
        mSourceOutputDir = sourceOutputDir;
        return this;
    }

    @Nullable
    public String getSourceOutputDir() {
        return mSourceOutputDir;
    }

    /**
     * @param symbolOutputDir the folder to write symbols into
     * @ itself
     */
    public AaptPackageProcessBuilder setSymbolOutputDir(@Nullable String symbolOutputDir) {
        mSymbolOutputDir = symbolOutputDir;
        return this;
    }

    @Nullable
    public String getSymbolOutputDir() {
        return mSymbolOutputDir;
    }

    /**
     * @param libraries the flat list of libraries
     * @return itself
     */
    public AaptPackageProcessBuilder setLibraries(
            @NonNull List<? extends SymbolFileProvider> libraries) {
        mLibraries = libraries;
        return this;
    }

    @NonNull
    public List<? extends SymbolFileProvider> getLibraries() {
        return mLibraries == null ? ImmutableList.<SymbolFileProvider>of() : mLibraries;
    }

    /**
     * @param resPackageOutput optional filepath for packaged resources
     * @return itself
     */
    public AaptPackageProcessBuilder setResPackageOutput(@Nullable String resPackageOutput) {
        mResPackageOutput = resPackageOutput;
        return this;
    }

    /**
     * @param proguardOutput optional filepath for proguard file to generate
     * @return itself
     */
    public AaptPackageProcessBuilder setProguardOutput(@Nullable String proguardOutput) {
        mProguardOutput = proguardOutput;
        return this;
    }

    /**
     * @param type the type of the variant being built
     * @return itself
     */
    public AaptPackageProcessBuilder setType(@NonNull VariantType type) {
        this.mType = type;
        return this;
    }

    @Nullable
    public VariantType getType() {
        return mType;
    }

    /**
     * @param debuggable whether the app is debuggable
     * @return itself
     */
    public AaptPackageProcessBuilder setDebuggable(boolean debuggable) {
        this.mDebuggable = debuggable;
        return this;
    }

    /**
     * @param resourceConfigs a list of resource config filters to pass to aapt.
     * @return itself
     */
    public AaptPackageProcessBuilder setResourceConfigs(@NonNull Collection<String> resourceConfigs) {
        this.mResourceConfigs = resourceConfigs;
        return this;
    }

    /**
     * @param splits optional list of split dimensions values (like a density or an abi). This
     *               will be used by aapt to generate the corresponding pure split apks.
     * @return itself
     */
    public AaptPackageProcessBuilder setSplits(@NonNull Collection<String> splits) {
        this.mSplits = splits;
        return this;
    }


    public AaptPackageProcessBuilder setVerbose() {
        mVerboseExec = true;
        return this;
    }

    /**
     * @param packageForR Package override to generate the R class in a different package.
     * @return itself
     */
    public AaptPackageProcessBuilder setPackageForR(@NonNull String packageForR) {
        this.mPackageForR = packageForR;
        return this;
    }

    public AaptPackageProcessBuilder setPseudoLocalesEnabled(boolean pseudoLocalesEnabled) {
        mPseudoLocalesEnabled = pseudoLocalesEnabled;
        return this;
    }

    /**
     * Specifies a preference for a particular density. Resources that do not match this density
     * and have variants that are a closer match are removed.
     * @param density the preferred density
     * @return itself
     */
    public AaptPackageProcessBuilder setPreferredDensity(String density) {
        mPreferredDensity = density;
        return this;
    }

    @Nullable
    String getPackageForR() {
        return mPackageForR;
    }

    public ProcessInfo build(
            @NonNull BuildToolInfo buildToolInfo,
            @NonNull IAndroidTarget target,
            @NonNull ILogger logger) {

        // if both output types are empty, then there's nothing to do and this is an error
        checkArgument(mSourceOutputDir != null || mResPackageOutput != null,
                "No output provided for aapt task");
        if (mSymbolOutputDir != null || mSourceOutputDir != null) {
            checkNotNull(mLibraries,
                    "libraries cannot be null if symbolOutputDir or sourceOutputDir is non-null");
        }

        // check resConfigs and split settings coherence.
        checkResConfigsVersusSplitSettings(logger);

        ProcessInfoBuilder builder = new ProcessInfoBuilder();
        builder.addEnvironments(mEnvironment);

        String aapt = buildToolInfo.getPath(BuildToolInfo.PathId.AAPT);
        if (aapt == null || !new File(aapt).isFile()) {
            throw new IllegalStateException("aapt is missing");
        }

        builder.setExecutable(aapt);
        builder.addArgs("package");

        if (mVerboseExec) {
            builder.addArgs("-v");
        }

        builder.addArgs("-f");
        builder.addArgs("--no-crunch");

        // inputs
        builder.addArgs("-I", target.getPath(IAndroidTarget.ANDROID_JAR));

        builder.addArgs("-M", mManifestFile.getAbsolutePath());

        if (mResFolder != null) {
            builder.addArgs("-S", mResFolder.getAbsolutePath());
        }

        if (mAssetsFolder != null) {
            builder.addArgs("-A", mAssetsFolder.getAbsolutePath());
        }

        // outputs
        if (mSourceOutputDir != null) {
            builder.addArgs("-m");
            builder.addArgs("-J", mSourceOutputDir);
        }

        if (mResPackageOutput != null) {
            builder.addArgs("-F", mResPackageOutput);
        }

        if (mProguardOutput != null) {
            builder.addArgs("-G", mProguardOutput);
        }

        if (mSplits != null) {
            for (String split : mSplits) {

                builder.addArgs("--split", split);
            }
        }

        // options controlled by build variants

        if (mDebuggable) {
            builder.addArgs("--debug-mode");
        }

        if (mType != VariantType.ANDROID_TEST) {
            if (mPackageForR != null) {
                builder.addArgs("--custom-package", mPackageForR);
                logger.verbose("Custom package for R class: '%s'", mPackageForR);
            }
        }

        if (mPseudoLocalesEnabled) {
            if (buildToolInfo.getRevision().getMajor() >= 21) {
                builder.addArgs("--pseudo-localize");
            } else {
                throw new RuntimeException(
                        "Pseudolocalization is only available since Build Tools version 21.0.0,"
                                + " please upgrade or turn it off.");
            }
        }

        // library specific options
        if (mType == VariantType.LIBRARY) {
            builder.addArgs("--non-constant-id");
        }

        // AAPT options
        String ignoreAssets = mOptions.getIgnoreAssets();
        if (ignoreAssets != null) {
            builder.addArgs("--ignore-assets", ignoreAssets);
        }

        if (mOptions.getFailOnMissingConfigEntry()) {
            if (buildToolInfo.getRevision().getMajor() > 20) {
                builder.addArgs("--error-on-missing-config-entry");
            } else {
                throw new IllegalStateException("aaptOptions:failOnMissingConfigEntry cannot be used"
                        + " with SDK Build Tools revision earlier than 21.0.0");
            }
        }

        // never compress apks.
        builder.addArgs("-0", "apk");

        // add custom no-compress extensions
        Collection<String> noCompressList = mOptions.getNoCompress();
        if (noCompressList != null) {
            for (String noCompress : noCompressList) {
                builder.addArgs("-0", noCompress);
            }
        }
        List<String> additionalParameters = mOptions.getAdditionalParameters();
        if (!isNullOrEmpty(additionalParameters)) {
            builder.addArgs(additionalParameters);
        }

        List<String> resourceConfigs = new ArrayList<String>();
        if (!isNullOrEmpty(mResourceConfigs)) {
            resourceConfigs.addAll(mResourceConfigs);
        }
        if (buildToolInfo.getRevision().getMajor() < 21 && mPreferredDensity != null) {
            resourceConfigs.add(mPreferredDensity);
            // when adding a density filter, also always add the nodpi option.
            resourceConfigs.add(Density.NODPI.getResourceValue());
        }


        // separate the density and language resource configs, since starting in 21, the
        // density resource configs should be passed with --preferred-density to ensure packaging
        // of scalable resources when no resource for the preferred density is present.
        List<String> otherResourceConfigs = new ArrayList<String>();
        List<String> densityResourceConfigs = new ArrayList<String>();
        if (!resourceConfigs.isEmpty()) {
            if (buildToolInfo.getRevision().getMajor() >= 21) {
                for (String resourceConfig : resourceConfigs) {
                    if (Density.getEnum(resourceConfig) != null) {
                        densityResourceConfigs.add(resourceConfig);
                    } else {
                        otherResourceConfigs.add(resourceConfig);
                    }
                }
            } else {
                // before 21, everything is passed with -c option.
                otherResourceConfigs = resourceConfigs;
            }
        }
        if (!otherResourceConfigs.isEmpty()) {
            Joiner joiner = Joiner.on(',');
            builder.addArgs("-c", joiner.join(otherResourceConfigs));
        }
        for (String densityResourceConfig : densityResourceConfigs) {
            builder.addArgs("--preferred-density", densityResourceConfig);
        }

        if (buildToolInfo.getRevision().getMajor() >= 21 && mPreferredDensity != null) {
            if (!isNullOrEmpty(mResourceConfigs)) {
                Collection<String> densityResConfig = getDensityResConfigs(mResourceConfigs);
                if (!densityResConfig.isEmpty()) {
                    throw new RuntimeException(String.format(
                            "When using splits in tools 21 and above, resConfigs should not contain "
                                    + "any densities. Right now, it contains \"%1$s\"\n"
                                    + "Suggestion: remove these from resConfigs from build.gradle",
                            Joiner.on("\",\"").join(densityResConfig)));
                }
            }
            builder.addArgs("--preferred-density", mPreferredDensity);
        }

        if (buildToolInfo.getRevision().getMajor() < 21 && mPreferredDensity != null) {
            logger.warning(String.format("Warning : Project is building density based multiple APKs"
                            + " but using tools version %1$s, you should upgrade to build-tools 21 or above"
                            + " to ensure proper packaging of resources.",
                    buildToolInfo.getRevision().getMajor()));
        }

        if (mSymbolOutputDir != null &&
                (mType == VariantType.LIBRARY || !mLibraries.isEmpty())) {
            builder.addArgs("--output-text-symbols", mSymbolOutputDir);
        }

        return builder.createProcess();
    }

    private void checkResConfigsVersusSplitSettings(ILogger logger) {
        if (isNullOrEmpty(mResourceConfigs) || isNullOrEmpty(mSplits)) {
            return;
        }

        // only consider the Density related resConfig settings.
        Collection<String> resConfigs = getDensityResConfigs(mResourceConfigs);
        List<String> splits = new ArrayList<String>(mSplits);
        splits.removeAll(resConfigs);
        if (!splits.isEmpty()) {
            // some splits are required, yet the resConfigs do not contain the split density value
            // which mean that the resulting split file would be empty, flag this as an error.
            throw new RuntimeException(String.format(
                    "Splits for densities \"%1$s\" were configured, yet the resConfigs settings does"
                            + " not include such splits. The resulting split APKs would be empty.\n"
                            + "Suggestion : exclude those splits in your build.gradle : \n"
                            + "splits {\n"
                            + "     density {\n"
                            + "         enable true\n"
                            + "         exclude \"%2$s\"\n"
                            + "     }\n"
                            + "}\n"
                            + "OR add them to the resConfigs list.",
                    Joiner.on(",").join(splits),
                    Joiner.on("\",\"").join(splits)));
        }
        resConfigs.removeAll(mSplits);
        if (!resConfigs.isEmpty()) {
            // there are densities present in the resConfig but not in splits, which mean that those
            // densities will be packaged in the main APK
            throw new RuntimeException(String.format(
                    "Inconsistent density configuration, with \"%1$s\" present on "
                            + "resConfig settings, while only \"%2$s\" densities are requested "
                            + "in splits APK density settings.\n"
                            + "Suggestion : remove extra densities from the resConfig : \n"
                            + "defaultConfig {\n"
                            + "     resConfigs \"%2$s\"\n"
                            + "}\n"
                            + "OR remove such densities from the split's exclude list.\n",
                    Joiner.on(",").join(resConfigs),
                    Joiner.on("\",\"").join(mSplits)));
        }
    }

    private static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private static Collection<String> getDensityResConfigs(Collection<String> resourceConfigs) {
        return Collections2.filter(new ArrayList<String>(resourceConfigs),
                new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return Density.getEnum(input) != null;
                    }
                });
    }
}
