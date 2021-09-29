package com.android.build.gradle.tasks;

import android.support.annotation.NonNull;

import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.dependency.ManifestDependency;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ProductFlavor;
import com.android.manifmerger.ManifestMerger2;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * a Task that only merge a single manifest with its overlays.
 */
public class ProcessManifest extends ManifestProcessorTask {
    private String minSdkVersion;
    private String targetSdkVersion;
    private Integer maxSdkVersion;
    private VariantConfiguration variantConfiguration;
    private File reportFile;

    public File getMainManifest() {
        return variantConfiguration.getMainManifest();
    }

    public String getPackageOverride() {
        return variantConfiguration.getApplicationId();
    }

    public int getVersionCode() {
        return variantConfiguration.getVersionCode();
    }

    public String getVersionName() {
        return variantConfiguration.getVersionName();
    }

    public List<File> getManifestOverlays() {
        return variantConfiguration.getManifestOverlays();
    }

    /**
     * Return a serializable version of our map of key value pairs for placeholder substitution.
     * This serialized form is only used by gradle to compare past and present tasks to determine
     * whether a task need to be re-run or not.
     */
    public String getManifestPlaceholders() {
        return serializeMap((Map<String, String>) variantConfiguration.getManifestPlaceholders());
    }

    @Override
    protected void doFullTaskAction() {

        final File file = getAaptFriendlyManifestOutputFile();
        getBuilder().mergeManifests(
                getMainManifest(),
                getManifestOverlays(),
                Collections.<ManifestDependency>emptyList(),
                getPackageOverride(),
                getVersionCode(),
                getVersionName(),
                getMinSdkVersion(),
                getTargetSdkVersion(),
                getMaxSdkVersion(),
                getManifestOutputFile().getAbsolutePath(),
                (file == null ? null : file.getAbsolutePath()),
                ManifestMerger2.MergeType.LIBRARY,
                (Map<String, String>) variantConfiguration.getManifestPlaceholders(),
                getReportFile());
    }

    public String getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public Integer getMaxSdkVersion() {
        return maxSdkVersion;
    }

    public void setMaxSdkVersion(Integer maxSdkVersion) {
        this.maxSdkVersion = maxSdkVersion;
    }

    public VariantConfiguration getVariantConfiguration() {
        return variantConfiguration;
    }

    public void setVariantConfiguration(VariantConfiguration variantConfiguration) {
        this.variantConfiguration = variantConfiguration;
    }

    public File getReportFile() {
        return reportFile;
    }

    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }

    public static class ConfigAction implements TaskConfigAction<ProcessManifest> {
        private VariantScope scope;

        public ConfigAction(VariantScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("process", "Manifest");
        }

        @Override
        public Class<ProcessManifest> getType() {
            return ProcessManifest.class;
        }

        @Override
        public void execute(@NonNull ProcessManifest processManifest) {
            final VariantConfiguration config = scope.getVariantConfiguration();
            final AndroidBuilder androidBuilder = scope.getGlobalScope().getAndroidBuilder();

            // get single output for now.
            final BaseVariantOutputData variantOutputData = scope.getVariantData().getOutputs().get(0);

            variantOutputData.manifestProcessorTask = processManifest;
            processManifest.setAndroidBuilder(androidBuilder);
            processManifest.setVariantName(config.getFullName());

            processManifest.setVariantConfiguration(config);

            final ProductFlavor mergedFlavor = config.getMergedFlavor();
            processManifest.setMinSdkVersion(getMinSdkVersion(androidBuilder, mergedFlavor, processManifest));
            processManifest.setTargetSdkVersion(getTargetSdkVersion(androidBuilder, mergedFlavor, processManifest));
            processManifest.setMaxSdkVersion(getMaxSdkVersion(androidBuilder, mergedFlavor));
            processManifest.setManifestOutputFile(variantOutputData.getScope().getManifestOutputFile());

            processManifest.setAaptFriendlyManifestOutputFile(
                    new File(scope.getGlobalScope().getIntermediatesDir(),
                            TaskManager.DIR_BUNDLES + "/" + config.getDirName() + "/aapt/AndroidManifest.xml"));
        }

        private Integer getMaxSdkVersion(AndroidBuilder androidBuilder, ProductFlavor mergedFlavor) {
            if (androidBuilder.isPreviewTarget()) {
                return null;
            }

            return mergedFlavor.getMaxSdkVersion();
        }

        private String getTargetSdkVersion(AndroidBuilder androidBuilder, ProductFlavor mergedFlavor, ProcessManifest processManifest) {
            if (androidBuilder.isPreviewTarget()) {
                return androidBuilder.getTargetCodename();
            }

            final ApiVersion version = mergedFlavor.getTargetSdkVersion();
            return (version == null ? null : version.getApiString());
        }

        private String getMinSdkVersion(AndroidBuilder androidBuilder, ProductFlavor mergedFlavor, ProcessManifest processManifest) {

            if (androidBuilder.isPreviewTarget()) {
                return androidBuilder.getTargetCodename();
            }

            final ApiVersion version = mergedFlavor.getMinSdkVersion();
            return (version == null ? null : version.getApiString());
        }

        public VariantScope getScope() {
            return scope;
        }

        public void setScope(VariantScope scope) {
            this.scope = scope;
        }
    }
}
