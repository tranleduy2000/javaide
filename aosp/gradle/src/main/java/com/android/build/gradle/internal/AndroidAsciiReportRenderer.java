/*
 * Copyright (C) 2012 The Android Open Source Project
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

import static org.gradle.logging.StyledTextOutput.Style.Description;
import static org.gradle.logging.StyledTextOutput.Style.Identifier;
import static org.gradle.logging.StyledTextOutput.Style.Info;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryBundle;
import com.android.builder.dependency.LibraryDependency;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;
import org.gradle.internal.graph.GraphRenderer;
import org.gradle.logging.StyledTextOutput;
import org.gradle.util.GUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * android version of the AsciiReportRenderer that outputs Android Library dependencies.
 */
public class AndroidAsciiReportRenderer extends TextReportRenderer {
    private boolean hasConfigs;
    private boolean hasCyclicDependencies;
    private GraphRenderer renderer;

    @Override
    public void startProject(Project project) {
        super.startProject(project);
        hasConfigs = false;
        hasCyclicDependencies = false;
    }

    @Override
    public void completeProject(Project project) {
        if (!hasConfigs) {
            getTextOutput().withStyle(Info).println("No dependencies");
        }
        super.completeProject(project);
    }

    public void startVariant(final BaseVariantData variantData) {
        if (hasConfigs) {
            getTextOutput().println();
        }
        hasConfigs = true;
        renderer = new GraphRenderer(getTextOutput());
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().withStyle(Identifier).text(
                        variantData.getVariantConfiguration().getFullName());
                getTextOutput().withStyle(Description).text("");
            }
        }, true);
    }

    private String getDescription(Configuration configuration) {
        return GUtil.isTrue(
                configuration.getDescription()) ? " - " + configuration.getDescription() : "";
    }

    public void completeConfiguration(BaseVariantData variantData) {}

    public void render(BaseVariantData variantData) throws IOException {
        List<LibraryDependency> libraries =
                variantData.getVariantConfiguration().getDirectLibraries();

        renderNow(libraries, variantData.getVariantDependency().getLocalDependencies());
    }

    void renderNow(@NonNull List<LibraryDependency> libraries,
                   @Nullable List<JarDependency> localJars) {
        if (libraries.isEmpty() && (localJars == null || localJars.isEmpty())) {
            getTextOutput().withStyle(Info).text("No dependencies");
            getTextOutput().println();
            return;
        }

        renderChildren(libraries, localJars);
    }

    @Override
    public void complete() {
        if (hasCyclicDependencies) {
            getTextOutput().withStyle(Info).println(
                    "\n(*) - dependencies omitted (listed previously)");
        }

        super.complete();
    }

    private void render(final LibraryDependency lib, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().text(((LibraryBundle)lib).getName());
            }
        }, lastChild);

        renderChildren(lib.getDependencies(), lib.getLocalDependencies());
    }

    private void render(final JarDependency jar, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().text("LOCAL: " + jar.getJarFile().getName());
            }
        }, lastChild);
    }

    private void renderChildren(@NonNull List<LibraryDependency> libraries,
                                @Nullable Collection<JarDependency> localJars) {
        renderer.startChildren();
        if (localJars != null) {
            final boolean emptyChildren = libraries.isEmpty();
            final int count = localJars.size();

            int i = 0;
            for (JarDependency jarDependency : localJars) {
                render(jarDependency, emptyChildren && i == count - 1);
                i++;
            }
        }

        final int count = libraries.size();
        for (int i = 0; i < count; i++) {
            LibraryDependency lib = libraries.get(i);
            render(lib, i == count - 1);
        }
        renderer.completeChildren();
    }
}
