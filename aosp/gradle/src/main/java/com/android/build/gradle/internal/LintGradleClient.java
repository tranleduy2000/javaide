/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.build.gradle.internal;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;
import static java.io.File.separator;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Warning;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LintGradleClient extends LintCliClient {
    private final AndroidProject mModelProject;
    private final String mVariantName;
    private final org.gradle.api.Project mGradleProject;
    private List<File> mCustomRules = Lists.newArrayList();
    private File mSdkHome;

    public LintGradleClient(
            @NonNull IssueRegistry registry,
            @NonNull LintCliFlags flags,
            @NonNull org.gradle.api.Project gradleProject,
            @NonNull AndroidProject modelProject,
            @Nullable File sdkHome,
            @Nullable String variantName) {
        super(flags);
        mGradleProject = gradleProject;
        mModelProject = modelProject;
        mVariantName = variantName;
        mSdkHome = sdkHome;
        mRegistry = registry;
    }

    public void setCustomRules(List<File> customRules) {
        mCustomRules = customRules;
    }

    @NonNull
    @Override
    public List<File> findRuleJars(@NonNull Project project) {
        return mCustomRules;
    }

    @NonNull
    @Override
    protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
        // Should not be called by lint since we supply an explicit set of projects
        // to the LintRequest
        throw new IllegalStateException();
    }

    @Override
    public File getSdkHome() {
        if (mSdkHome != null) {
            return mSdkHome;
        }
        return super.getSdkHome();
    }

    @Override
    @Nullable
    public File getCacheDir(boolean create) {
        File dir = new File(mGradleProject.getRootProject().getBuildDir(),
                FD_INTERMEDIATES + separator + "lint-cache"); //$NON-NLS-1$
        if (dir.exists() || create && dir.mkdirs()) {
            return dir;
        }

        return super.getCacheDir(create);
    }

    @Override
    @NonNull
    protected LintRequest createLintRequest(@NonNull List<File> files) {
        return new LintGradleRequest(this, mModelProject, mGradleProject, mVariantName, files);
    }

    /** Run lint with the given registry and return the resulting warnings */
    @NonNull
    public List<Warning> run(@NonNull IssueRegistry registry) throws IOException {
        run(registry, Collections.<File>emptyList());
        return mWarnings;
    }

    /**
     * Given a list of results from separate variants, merge them into a single
     * list of warnings, and mark their
     * @param warningMap a map from variant to corresponding warnings
     * @param project the project model
     * @return a merged list of issues
     */
    @NonNull
    public static List<Warning> merge(
            @NonNull Map<Variant,List<Warning>> warningMap,
            @NonNull AndroidProject project) {
        // Easy merge?
        if (warningMap.size() == 1) {
            return warningMap.values().iterator().next();
        }
        int maxCount = 0;
        for (List<Warning> warnings : warningMap.values()) {
            int size = warnings.size();
            maxCount = Math.max(size, maxCount);
        }
        if (maxCount == 0) {
            return Collections.emptyList();
        }

        int totalVariantCount = project.getVariants().size();

        List<Warning> merged = Lists.newArrayListWithExpectedSize(2 * maxCount);

        // Map fro issue to message to line number to file name to canonical warning
        Map<Issue,Map<String, Map<Integer, Map<String, Warning>>>> map =
                Maps.newHashMapWithExpectedSize(2 * maxCount);

        for (Map.Entry<Variant,List<Warning>> entry : warningMap.entrySet()) {
            Variant variant = entry.getKey();
            List<Warning> warnings = entry.getValue();
            for (Warning warning : warnings) {
                Map<String,Map<Integer,Map<String,Warning>>> messageMap = map.get(warning.issue);
                if (messageMap == null) {
                    messageMap = Maps.newHashMap();
                    map.put(warning.issue, messageMap);
                }
                Map<Integer, Map<String, Warning>> lineMap = messageMap.get(warning.message);
                if (lineMap == null) {
                    lineMap = Maps.newHashMap();
                    messageMap.put(warning.message, lineMap);
                }
                Map<String, Warning> fileMap = lineMap.get(warning.line);
                if (fileMap == null) {
                    fileMap = Maps.newHashMap();
                    lineMap.put(warning.line, fileMap);
                }
                String fileName = warning.file != null ? warning.file.getName() : "<unknown>";
                Warning canonical = fileMap.get(fileName);
                if (canonical == null) {
                    canonical = warning;
                    fileMap.put(fileName, canonical);
                    canonical.variants = Sets.newHashSet();
                    canonical.gradleProject = project;
                    merged.add(canonical);
                }
                canonical.variants.add(variant);
            }
        }

        // Clear out variants on any nodes that define all
        for (Warning warning : merged) {
            if (warning.variants != null && warning.variants.size() == totalVariantCount) {
                // If this error is present in all variants, just clear it out
                warning.variants = null;
            }

        }

        Collections.sort(merged);
        return merged;
    }

    @Override
    protected void addProgressPrinter() {
        // No progress printing from the Gradle lint task; gradle tasks
        // do not really do that, even for long-running jobs.
    }
}
