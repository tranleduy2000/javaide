/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.tasks;

import com.android.builder.core.VariantConfiguration;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

import proguard.ParseException;
import proguard.gradle.ProGuardTask;

/**
 * Specialization of the {@link ProGuardTask} that can use {@link Configuration} objects to retrieve
 * input files like the tested application classes and the tested application mapping file.
 */
public class TestModuleProGuardTask extends ProGuardTask {
    private Logger logger;
    private Configuration mappingConfiguration;
    private VariantConfiguration variantConfiguration;


    /**
     * Sets the {@link Configuration} to later retrieve the tested application mapping file
     */
    public void setMappingConfiguration(Configuration configuration) {
        this.mappingConfiguration = configuration;
        dependsOn(configuration);
    }

    /**
     * Sets the {@link Configuration} to later retrieve the test application classes jar file.
     */
    public void setClassesConfiguration(Configuration configuration) {
        dependsOn(configuration);
    }


    public void setVariantConfiguration(
            VariantConfiguration variantConfiguration) {
        this.variantConfiguration = variantConfiguration;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    @TaskAction
    public void proguard() throws ParseException, IOException {
        if (logger.isEnabled(LogLevel.INFO)) {
            logger.info("test module mapping file " + mappingConfiguration.getSingleFile());
            for (Object file : variantConfiguration.getPackagedJars()) {
                logger.info("test module proguard input " + file);

            }
            for (Object file : variantConfiguration.getProvidedOnlyJars()) {
                logger.info("test module proguard library " + file);
            }
        }

        if (mappingConfiguration.getSingleFile().isFile()) {
            applymapping(mappingConfiguration.getSingleFile());
        }
        super.proguard();
    }
}
