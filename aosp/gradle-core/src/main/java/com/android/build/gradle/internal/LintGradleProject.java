package com.android.build.gradle.internal;

import static com.android.SdkConstants.APPCOMPAT_LIB_ARTIFACT;
import static com.android.SdkConstants.SUPPORT_LIB_ARTIFACT;
import static java.io.File.separatorChar;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProvider;
import com.android.builder.model.SourceProviderContainer;
import com.android.builder.model.Variant;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Project;
import com.android.utils.Pair;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An implementation of Lint's {@link Project} class wrapping a Gradle model (project or
 * library)
 */
public class LintGradleProject extends Project {
    protected AndroidVersion mMinSdkVersion;
    protected AndroidVersion mTargetSdkVersion;

    private LintGradleProject(
            @NonNull LintGradleClient client,
            @NonNull File dir,
            @NonNull File referenceDir,
            @NonNull File manifest) {
        super(client, dir, referenceDir);
        mGradleProject = true;
        mMergeManifests = true;
        mDirectLibraries = Lists.newArrayList();
        readManifest(manifest);
    }

    /**
     * Creates a {@link com.android.build.gradle.internal.LintGradleProject} from
     * the given {@link com.android.builder.model.AndroidProject} definition for
     * a given {@link com.android.builder.model.Variant}, and returns it along with
     * a set of lint custom rule jars applicable for the given model project.
     *
     * @param client the client
     * @param project the model project
     * @param variant the variant
     * @param gradleProject the gradle project
     * @return a pair of new project and list of custom rule jars
     */
    @NonNull
    public static Pair<LintGradleProject, List<File>> create(
            @NonNull LintGradleClient client,
            @NonNull AndroidProject project,
            @NonNull Variant variant,
            @NonNull org.gradle.api.Project gradleProject) {
        File dir = gradleProject.getProjectDir();
        AppGradleProject lintProject = new AppGradleProject(client, dir,
                dir, project, variant);

        List<File> customRules = Lists.newArrayList();
        File appLintJar = new File(gradleProject.getBuildDir(),
                "lint" + separatorChar + "lint.jar");
        if (appLintJar.exists()) {
            customRules.add(appLintJar);
        }

        Set<AndroidLibrary> libraries = Sets.newHashSet();
        Dependencies dependencies = variant.getMainArtifact().getDependencies();
        for (AndroidLibrary library : dependencies.getLibraries()) {
            lintProject.addDirectLibrary(createLibrary(client, library, libraries, customRules));
        }

        return Pair.<LintGradleProject,List<File>>of(lintProject, customRules);
    }

    @Override
    protected void initialize() {
        // Deliberately not calling super; that code is for ADT compatibility
    }

    protected void readManifest(File manifest) {
        if (manifest.exists()) {
            try {
                String xml = Files.toString(manifest, Charsets.UTF_8);
                Document document = XmlUtils.parseDocumentSilently(xml, true);
                if (document != null) {
                    readManifest(document);
                }
            } catch (IOException e) {
                mClient.log(e, "Could not read manifest %1$s", manifest);
            }
        }
    }

    @Override
    public boolean isGradleProject() {
        return true;
    }

    protected static boolean dependsOn(@NonNull Dependencies dependencies,
            @NonNull String artifact) {
        for (AndroidLibrary library : dependencies.getLibraries()) {
            if (dependsOn(library, artifact)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean dependsOn(@NonNull AndroidLibrary library, @NonNull String artifact) {
        if (SUPPORT_LIB_ARTIFACT.equals(artifact)) {
            if (library.getJarFile().getName().startsWith("support-v4-")) {
                return true;
            }

        } else if (APPCOMPAT_LIB_ARTIFACT.equals(artifact)) {
            File bundle = library.getBundle();
            if (bundle.getName().startsWith("appcompat-v7-")) {
                return true;
            }
        }

        for (AndroidLibrary dependency : library.getLibraryDependencies()) {
            if (dependsOn(dependency, artifact)) {
                return true;
            }
        }

        return false;
    }

    void addDirectLibrary(@NonNull Project project) {
        mDirectLibraries.add(project);
    }

    @NonNull
    private static LibraryProject createLibrary(@NonNull LintGradleClient client,
            @NonNull AndroidLibrary library,
            @NonNull Set<AndroidLibrary> seen, List<File> customRules) {
        seen.add(library);
        File dir = library.getFolder();
        LibraryProject project = new LibraryProject(client, dir, dir, library);

        File ruleJar = library.getLintJar();
        if (ruleJar.exists()) {
            customRules.add(ruleJar);
        }

        for (AndroidLibrary dependent : library.getLibraryDependencies()) {
            if (!seen.contains(dependent)) {
                project.addDirectLibrary(createLibrary(client, dependent, seen, customRules));
            }
        }

        return project;
    }

    private static class AppGradleProject extends LintGradleProject {
        private AndroidProject mProject;
        private Variant mVariant;
        private List<SourceProvider> mProviders;
        private List<SourceProvider> mTestProviders;

        private AppGradleProject(
                @NonNull LintGradleClient client,
                @NonNull File dir,
                @NonNull File referenceDir,
                @NonNull AndroidProject project,
                @NonNull Variant variant) {
            //TODO FIXME: handle multi-apk
            super(client, dir, referenceDir,
                    variant.getMainArtifact().getOutputs().iterator().next().getGeneratedManifest());

            mProject = project;
            mVariant = variant;
        }

        @Override
        public boolean isLibrary() {
            return mProject.isLibrary();
        }

        @Override
        public AndroidProject getGradleProjectModel() {
            return mProject;
        }

        @Override
        public Variant getCurrentVariant() {
            return mVariant;
        }

        private List<SourceProvider> getSourceProviders() {
            if (mProviders == null) {
                List<SourceProvider> providers = Lists.newArrayList();
                AndroidArtifact mainArtifact = mVariant.getMainArtifact();

                providers.add(mProject.getDefaultConfig().getSourceProvider());

                for (String flavorName : mVariant.getProductFlavors()) {
                    for (ProductFlavorContainer flavor : mProject.getProductFlavors()) {
                        if (flavorName.equals(flavor.getProductFlavor().getName())) {
                            providers.add(flavor.getSourceProvider());
                            break;
                        }
                    }
                }

                SourceProvider multiProvider = mainArtifact.getMultiFlavorSourceProvider();
                if (multiProvider != null) {
                    providers.add(multiProvider);
                }

                String buildTypeName = mVariant.getBuildType();
                for (BuildTypeContainer buildType : mProject.getBuildTypes()) {
                    if (buildTypeName.equals(buildType.getBuildType().getName())) {
                        providers.add(buildType.getSourceProvider());
                        break;
                    }
                }

                SourceProvider variantProvider =  mainArtifact.getVariantSourceProvider();
                if (variantProvider != null) {
                    providers.add(variantProvider);
                }

                mProviders = providers;
            }

            return mProviders;
        }

        private List<SourceProvider> getTestSourceProviders() {
            if (mTestProviders == null) {
                List<SourceProvider> providers = Lists.newArrayList();

                ProductFlavorContainer defaultConfig = mProject.getDefaultConfig();
                for (SourceProviderContainer extra : defaultConfig.getExtraSourceProviders()) {
                    String artifactName = extra.getArtifactName();
                    if (AndroidProject.ARTIFACT_ANDROID_TEST.equals(artifactName)) {
                        providers.add(extra.getSourceProvider());
                    }
                }

                for (String flavorName : mVariant.getProductFlavors()) {
                    for (ProductFlavorContainer flavor : mProject.getProductFlavors()) {
                        if (flavorName.equals(flavor.getProductFlavor().getName())) {
                            for (SourceProviderContainer extra : flavor.getExtraSourceProviders()) {
                                String artifactName = extra.getArtifactName();
                                if (AndroidProject.ARTIFACT_ANDROID_TEST.equals(artifactName)) {
                                    providers.add(extra.getSourceProvider());
                                }
                            }
                        }
                    }
                }

                String buildTypeName = mVariant.getBuildType();
                for (BuildTypeContainer buildType : mProject.getBuildTypes()) {
                    if (buildTypeName.equals(buildType.getBuildType().getName())) {
                        for (SourceProviderContainer extra : buildType.getExtraSourceProviders()) {
                            String artifactName = extra.getArtifactName();
                            if (AndroidProject.ARTIFACT_ANDROID_TEST.equals(artifactName)) {
                                providers.add(extra.getSourceProvider());
                            }
                        }
                    }
                }

                mTestProviders = providers;
            }

            return mTestProviders;
        }

        @NonNull
        @Override
        public List<File> getManifestFiles() {
            if (mManifestFiles == null) {
                mManifestFiles = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    File manifestFile = provider.getManifestFile();
                    if (manifestFile.exists()) { // model returns path whether or not it exists
                        mManifestFiles.add(manifestFile);
                    }
                }
            }

            return mManifestFiles;
        }

        @NonNull
        @Override
        public List<File> getProguardFiles() {
            if (mProguardFiles == null) {
                ProductFlavor flavor = mProject.getDefaultConfig().getProductFlavor();
                mProguardFiles = Lists.newArrayList();
                for (File file : flavor.getProguardFiles()) {
                    if (file.exists()) {
                        mProguardFiles.add(file);
                    }
                }
                try {
                    for (File file : flavor.getConsumerProguardFiles()) {
                        if (file.exists()) {
                            mProguardFiles.add(file);
                        }
                    }
                } catch (Throwable t) {
                    // On some models, this threw
                    //   org.gradle.tooling.model.UnsupportedMethodException:
                    //    Unsupported method: BaseConfig.getConsumerProguardFiles().
                    // Playing it safe for a while.
                }
            }

            return mProguardFiles;
        }

        @NonNull
        @Override
        public List<File> getResourceFolders() {
            if (mResourceFolders == null) {
                mResourceFolders = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    Collection<File> resDirs = provider.getResDirectories();
                    for (File res : resDirs) {
                        if (res.exists()) { // model returns path whether or not it exists
                            mResourceFolders.add(res);
                        }
                    }
                }

                for (File file : mVariant.getMainArtifact().getGeneratedResourceFolders()) {
                    if (file.exists()) {
                        mResourceFolders.add(file);
                    }
                }

            }

            return mResourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaSourceFolders() {
            if (mJavaSourceFolders == null) {
                mJavaSourceFolders = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    Collection<File> srcDirs = provider.getJavaDirectories();
                    for (File srcDir : srcDirs) {
                        if (srcDir.exists()) { // model returns path whether or not it exists
                            mJavaSourceFolders.add(srcDir);
                        }
                    }
                }

                for (File file : mVariant.getMainArtifact().getGeneratedSourceFolders()) {
                    if (file.exists()) {
                        mJavaSourceFolders.add(file);
                    }
                }
            }

            return mJavaSourceFolders;
        }

        @NonNull
        @Override
        public List<File> getTestSourceFolders() {
            if (mTestSourceFolders == null) {
                mTestSourceFolders = Lists.newArrayList();
                for (SourceProvider provider : getTestSourceProviders()) {
                    Collection<File> srcDirs = provider.getJavaDirectories();
                    for (File srcDir : srcDirs) {
                        if (srcDir.exists()) { // model returns path whether or not it exists
                            mTestSourceFolders.add(srcDir);
                        }
                    }
                }
            }

            return mTestSourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaClassFolders() {
            if (mJavaClassFolders == null) {
                mJavaClassFolders = new ArrayList<File>(1);
                File outputClassFolder = mVariant.getMainArtifact().getClassesFolder();
                if (outputClassFolder.exists()) {
                    mJavaClassFolders.add(outputClassFolder);
                }
            }

            return mJavaClassFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaLibraries() {
            if (mJavaLibraries == null) {
                Collection<JavaLibrary> libs = mVariant.getMainArtifact().getDependencies().getJavaLibraries();
                mJavaLibraries = Lists.newArrayListWithExpectedSize(libs.size());
                for (JavaLibrary lib : libs) {
                    File jar = lib.getJarFile();
                    if (jar.exists()) {
                        mJavaLibraries.add(jar);
                    }
                }
            }
            return mJavaLibraries;
        }

        @Nullable
        @Override
        public String getPackage() {
            // For now, lint only needs the manifest package; not the potentially variant specific
            // package. As part of the Gradle work on the Lint API we should make two separate
            // package lookup methods -- one for the manifest package, one for the build package
            if (mPackage == null) { // only used as a fallback in case manifest somehow is null
                String packageName = mProject.getDefaultConfig().getProductFlavor().getApplicationId();
                if (packageName != null) {
                    return packageName;
                }
            }

            return mPackage; // from manifest
        }

        @Override
        @NonNull
        public AndroidVersion getMinSdkVersion() {
            if (mMinSdkVersion == null) {
                ApiVersion minSdk = mVariant.getMergedFlavor().getMinSdkVersion();
                if (minSdk == null) {
                    ProductFlavor flavor = mProject.getDefaultConfig().getProductFlavor();
                    minSdk = flavor.getMinSdkVersion();
                }
                if (minSdk != null) {
                    mMinSdkVersion = LintUtils.convertVersion(minSdk, mClient.getTargets());
                } else {
                    mMinSdkVersion = super.getMinSdkVersion(); // from manifest
                }
            }

            return mMinSdkVersion;
        }

        @Override
        @NonNull
        public AndroidVersion getTargetSdkVersion() {
            if (mTargetSdkVersion == null) {
                ApiVersion targetSdk = mVariant.getMergedFlavor().getTargetSdkVersion();
                if (targetSdk == null) {
                    ProductFlavor flavor = mProject.getDefaultConfig().getProductFlavor();
                    targetSdk = flavor.getTargetSdkVersion();
                }
                if (targetSdk != null) {
                    mTargetSdkVersion = LintUtils.convertVersion(targetSdk, mClient.getTargets());
                } else {
                    mTargetSdkVersion = super.getTargetSdkVersion(); // from manifest
                }
            }

            return mTargetSdkVersion;
        }

        @Override
        public int getBuildSdk() {
            String compileTarget = mProject.getCompileTarget();
            AndroidVersion version = AndroidTargetHash.getPlatformVersion(compileTarget);
            if (version != null) {
                return version.getApiLevel();
            }

            return super.getBuildSdk();
        }

        @Nullable
        @Override
        public Boolean dependsOn(@NonNull String artifact) {
            if (SUPPORT_LIB_ARTIFACT.equals(artifact)) {
                if (mSupportLib == null) {
                    Dependencies dependencies = mVariant.getMainArtifact().getDependencies();
                    mSupportLib = dependsOn(dependencies, artifact);
                }
                return mSupportLib;
            } else if (APPCOMPAT_LIB_ARTIFACT.equals(artifact)) {
                if (mAppCompat == null) {
                    Dependencies dependencies = mVariant.getMainArtifact().getDependencies();
                    mAppCompat = dependsOn(dependencies, artifact);
                }
                return mAppCompat;
            } else {
                return super.dependsOn(artifact);
            }
        }
    }

    private static class LibraryProject extends LintGradleProject {
        private AndroidLibrary mLibrary;

        private LibraryProject(
                @NonNull LintGradleClient client,
                @NonNull File dir,
                @NonNull File referenceDir,
                @NonNull AndroidLibrary library) {
            super(client, dir, referenceDir, library.getManifest());
            mLibrary = library;

            // TODO: Make sure we don't use this project for any source library projects!
            mReportIssues = false;
        }

        @Override
        public boolean isLibrary() {
            return true;
        }

        @Override
        public AndroidLibrary getGradleLibraryModel() {
            return mLibrary;
        }

        @Override
        public Variant getCurrentVariant() {
            return null;
        }

        @NonNull
        @Override
        public List<File> getManifestFiles() {
            if (mManifestFiles == null) {
                File manifest = mLibrary.getManifest();
                if (manifest.exists()) {
                    mManifestFiles = Collections.singletonList(manifest);
                } else {
                    mManifestFiles = Collections.emptyList();
                }
            }

            return mManifestFiles;
        }

        @NonNull
        @Override
        public List<File> getProguardFiles() {
            if (mProguardFiles == null) {
                File proguardRules = mLibrary.getProguardRules();
                if (proguardRules.exists()) {
                    mProguardFiles = Collections.singletonList(proguardRules);
                } else {
                    mProguardFiles = Collections.emptyList();
                }
            }

            return mProguardFiles;
        }

        @NonNull
        @Override
        public List<File> getResourceFolders() {
            if (mResourceFolders == null) {
                File folder = mLibrary.getResFolder();
                if (folder.exists()) {
                    mResourceFolders = Collections.singletonList(folder);
                } else {
                    mResourceFolders = Collections.emptyList();
                }
            }

            return mResourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaSourceFolders() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<File> getTestSourceFolders() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<File> getJavaClassFolders() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<File> getJavaLibraries() {
            if (mJavaLibraries == null) {
                mJavaLibraries = Lists.newArrayList();
                File jarFile = mLibrary.getJarFile();
                if (jarFile.exists()) {
                    mJavaLibraries.add(jarFile);
                }

                for (File local : mLibrary.getLocalJars()) {
                    if (local.exists()) {
                        mJavaLibraries.add(local);
                    }
                }
            }

            return mJavaLibraries;
        }

        @Nullable
        @Override
        public Boolean dependsOn(@NonNull String artifact) {
            if (SUPPORT_LIB_ARTIFACT.equals(artifact)) {
                if (mSupportLib == null) {
                    mSupportLib = dependsOn(mLibrary, artifact);
                }
                return mSupportLib;
            } else if (APPCOMPAT_LIB_ARTIFACT.equals(artifact)) {
                if (mAppCompat == null) {
                    mAppCompat = dependsOn(mLibrary, artifact);
                }
                return mAppCompat;
            } else {
                return super.dependsOn(artifact);
            }
        }
    }
}
