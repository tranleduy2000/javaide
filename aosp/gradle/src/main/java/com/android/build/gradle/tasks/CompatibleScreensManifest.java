package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantOutputScope;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.android.resources.Density;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import groovy.lang.Closure;

/**
 * Task to generate a manifest snippet that just contains a compatible-screens
 * node with the given density and the given list of screen sizes.
 */
public class CompatibleScreensManifest extends DefaultAndroidTask {

    private String screenDensity;
    private Set<String> screenSizes;
    private File manifestFile;

    private static String convert(@NonNull String density, @NonNull Density... densitiesToConvert) {
        for (Density densityToConvert : densitiesToConvert) {
            if (densityToConvert.getResourceValue().equals(density)) {
                return Integer.toString(densityToConvert.getDpiValue());
            }

        }


        return density;
    }

    @TaskAction
    public void generate() throws IOException {
        StringBuilder content = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    package=\"\">\n" + "\n" +
                        "    <compatible-screens>\n");

        String density = getScreenDensity();

        // convert unsupported values to numbers.
        density = convert(density, Density.XXHIGH, Density.XXXHIGH);

        for (String size : getScreenSizes()) {
            content.append(
                    "        <screen android:screenSize=\"" + size +
                            "\" android:screenDensity=\"" + density +
                            "\" />\n");
        }

        content.append("    </compatible-screens>\n" +
                "</manifest>");

        Files.write(content.toString(), getManifestFile(), Charsets.UTF_8);
    }

    public String getScreenDensity() {
        return screenDensity;
    }

    public void setScreenDensity(String screenDensity) {
        this.screenDensity = screenDensity;
    }

    public Set<String> getScreenSizes() {
        return screenSizes;
    }

    public void setScreenSizes(Set<String> screenSizes) {
        this.screenSizes = screenSizes;
    }

    public File getManifestFile() {
        return manifestFile;
    }

    public void setManifestFile(File manifestFile) {
        this.manifestFile = manifestFile;
    }

    public static class ConfigAction implements TaskConfigAction<CompatibleScreensManifest> {
        @NonNull
        private VariantOutputScope scope;
        @NonNull
        private Set<String> screenSizes;

        public ConfigAction(@NonNull VariantOutputScope scope, @NonNull Set<String> screenSizes) {
            this.scope = scope;
            this.screenSizes = screenSizes;
        }

        @Override
        public String getName() {
            return scope.getTaskName("create", "CompatibleScreenManifest");
        }

        @Override
        public Class<CompatibleScreensManifest> getType() {
            return ((Class<CompatibleScreensManifest>) (CompatibleScreensManifest.class));
        }

        @Override
        public void execute(CompatibleScreensManifest csmTask) {
            csmTask.setVariantName(scope.getVariantScope().getVariantConfiguration().getFullName());

            csmTask.setScreenDensity(scope.getVariantOutputData().getMainOutputFile().getFilter(com.android.build.OutputFile.DENSITY));
            csmTask.setScreenSizes(screenSizes);

            ConventionMappingHelper.map(csmTask, "manifestFile", new Closure<File>(this, this) {
                public File doCall(Object it) {
                    return scope.getCompatibleScreensManifestFile();
                }

                public File doCall() {
                    return doCall(null);
                }

            });
        }

        public VariantOutputScope getScope() {
            return scope;
        }

        public void setScope(VariantOutputScope scope) {
            this.scope = scope;
        }

        public Set<String> getScreenSizes() {
            return screenSizes;
        }

        public void setScreenSizes(Set<String> screenSizes) {
            this.screenSizes = screenSizes;
        }
    }
}
