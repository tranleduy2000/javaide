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

package com.android.build.gradle.internal;

import com.android.utils.ILogger;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

/**
 * Utilities for logging with pre-formatted message.
 */
public class LoggingUtil {

    public static void displayWarning(ILogger logger, Project project, String message) {
        logger.warning(createWarning(project.getPath(), message));
    }

    public static void displayWarning(Logger logger, Project project, String message) {
        logger.warn(createWarning(project.getPath(), message));
    }

    public static void displayDeprecationWarning(Logger logger, Project project, String message) {
        displayWarning(logger, project, message);
    }

    private static String createWarning(String projectName, String message) {
        return "WARNING [Project: " + projectName + "] " + message;
    }

}
