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

package com.android.build.gradle.internal.variant;

import com.android.annotations.NonNull;
import com.google.common.collect.Sets;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.maven.MavenDeployer;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.tasks.Upload;

import java.util.Collections;
import java.util.Set;

/**
 */
public class VariantHelper {

    public static void setupDefaultConfig(
            @NonNull final Project project,
            @NonNull Configuration configuration) {
        // The library artifact is published (inter-project( for the "default" configuration so
        // we make sure "default" extends from the actual configuration used for building.
        Configuration defaultConfig = project.getConfigurations().getAt("default");
        defaultConfig.setExtendsFrom(Collections.singleton(configuration));

        // for the maven publication (for now), we need to manually include all the configuration
        // object in a special mapping.
        // It's not possible to put the top level config object as extended from config won't
        // be included.
        final Set<Configuration> flattenedConfigs = flattenConfigurations(configuration);

        project.getPlugins().withType(MavenPlugin.class, new Action<MavenPlugin>() {
            @Override
            public void execute(MavenPlugin mavenPlugin) {
                project.getTasks().withType(Upload.class, new Action<Upload>() {
                    @Override
                    public void execute(Upload upload) {
                        upload.getRepositories().withType(
                                MavenDeployer.class,
                                new Action<MavenDeployer>() {
                                    @Override
                                    public void execute(MavenDeployer mavenDeployer) {
                                        for (Configuration config : flattenedConfigs) {
                                            mavenDeployer.getPom().getScopeMappings().addMapping(
                                                    300,
                                                    project.getConfigurations().getByName(
                                                            config.getName()),
                                                    "compile");
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    /**
     * Build a set of configuration containing all the Configuration object that a given
     * configuration extends from, directly or transitively.
     *
     * @param configuration the configuration
     * @return a set of config.
     */
    private static Set<Configuration> flattenConfigurations(@NonNull Configuration configuration) {
        Set<Configuration> configs = Sets.newHashSet();
        configs.add(configuration);

        for (Configuration extend : configuration.getExtendsFrom()) {
            configs.addAll(flattenConfigurations(extend));
        }

        return configs;
    }

}
