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

package com.android.build.gradle.internal.coverage
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Jacoco plugin. This is very similar to the built-in support for Jacoco but we dup it in order
 * to control it as we need our own offline instrumentation.
 *
 * This may disappear if we can ever reuse the built-in support.
 *
 */
class JacocoPlugin implements Plugin<Project> {
    public static final String ANT_CONFIGURATION_NAME = 'androidJacocoAnt'
    public static final String AGENT_CONFIGURATION_NAME = 'androidJacocoAgent'

    private Project project;

    @Override
    void apply(Project project) {
        this.project = project

        addJacocoConfigurations()
        configureAgentDependencies()
        configureTaskClasspathDefaults()
    }

    /**
     * Creates the configurations used by plugin.
     * @param project the project to add the configurations to
     */
    private void addJacocoConfigurations() {
        this.project.configurations.create(AGENT_CONFIGURATION_NAME).with {
            visible = false
            transitive = true
            description = 'The Jacoco agent to use to get coverage data.'
        }
        this.project.configurations.create(ANT_CONFIGURATION_NAME).with {
            visible = false
            transitive = true
            description = 'The Jacoco ant tasks to use to get execute Gradle tasks.'
        }
    }

    /**
     * Configures the agent dependencies using the 'jacocoAnt' configuration.
     * Uses the version declared in 'toolVersion' of the Jacoco extension if no dependencies are explicitly declared.
     */
    private void configureAgentDependencies() {
        def config = project.configurations[AGENT_CONFIGURATION_NAME]
        config.incoming.beforeResolve {
            if (config.dependencies.empty) {
                config.dependencies.add(project.dependencies.create("org.jacoco:org.jacoco.agent:${project.android.jacoco.version}"))
            }
        }
    }

    /**
     * Configures the classpath for Jacoco tasks using the 'jacocoAnt' configuration.
     * Uses the version information declared in 'toolVersion' of the Jacoco extension if no dependencies are explicitly declared.
     */
    private void configureTaskClasspathDefaults() {
        def config = project.configurations[ANT_CONFIGURATION_NAME]
        config.incoming.beforeResolve {
            if (config.dependencies.empty) {
                config.dependencies.add(project.dependencies.create("org.jacoco:org.jacoco.ant:${project.android.jacoco.version}"))
            }
        }
    }
}
