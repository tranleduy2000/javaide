package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Project;
import com.android.utils.Pair;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class LintGradleRequest extends LintRequest {
    @NonNull private final LintGradleClient mLintClient;
    @NonNull private final org.gradle.api.Project mGradleProject;
    @Nullable private final String mVariantName;
    @NonNull private final AndroidProject mModelProject;

    public LintGradleRequest(
            @NonNull LintGradleClient client,
            @NonNull AndroidProject modelProject,
            @NonNull org.gradle.api.Project gradleProject,
            @Nullable String variantName,
            @NonNull List<File> files) {
        super(client, files);
        mLintClient = client;
        mModelProject = modelProject;
        mGradleProject = gradleProject;
        mVariantName = variantName;
    }

    @Nullable
    @Override
    public Collection<Project> getProjects() {
        if (mProjects == null) {
            Variant variant = findVariant(mModelProject, mVariantName);
            if (variant == null) {
                mProjects = Collections.emptyList();
                return mProjects;
            }
            Pair<LintGradleProject,List<File>> result = LintGradleProject.create(
                    mLintClient, mModelProject, variant, mGradleProject);
            mProjects = Collections.<Project>singletonList(result.getFirst());
            mLintClient.setCustomRules(result.getSecond());
        }

        return mProjects;
    }

    private static Variant findVariant(@NonNull AndroidProject project,
            @Nullable String variantName) {
        if (variantName != null) {
            for (Variant variant : project.getVariants()) {
                if (variantName.equals(variant.getName())) {
                    return variant;
                }
            }
        }

        if (!project.getVariants().isEmpty()) {
            return project.getVariants().iterator().next();
        }

        return null;
    }
}
