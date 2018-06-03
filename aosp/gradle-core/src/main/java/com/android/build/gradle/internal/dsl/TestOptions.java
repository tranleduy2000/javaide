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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.tasks.testing.Test;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

/**
 * Options for running tests.
 */
public class TestOptions {
    @Nullable
    private String resultsDir;

    @Nullable
    private String reportDir;

    /**
     * Options for controlling unit tests execution.
     *
     * @since 1.1
     */
    @NonNull
    private final UnitTestOptions unitTests = new UnitTestOptions();

    /**
     * Configures unit test options.
     *
     * @since 1.2
     */
    public void unitTests(Closure closure) {
        ConfigureUtil.configure(closure, unitTests);
    }

    /**
     * Configures unit test options.
     *
     * @since 1.2
     */
    @NonNull
    public UnitTestOptions getUnitTests() {
        return unitTests;
    }

    /** Name of the results directory. */
    @Nullable
    public String getResultsDir() {
        return resultsDir;
    }

    public void setResultsDir(@Nullable String resultsDir) {
        this.resultsDir = resultsDir;
    }

    /** Name of the reports directory. */
    @Nullable
    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(@Nullable String reportDir) {
        this.reportDir = reportDir;
    }

    /**
     * Options for controlling unit tests execution.
     */
    public static class UnitTestOptions {
        private DomainObjectSet<Test> testTasks = new DefaultDomainObjectSet<Test>(Test.class);
        private boolean returnDefaultValues;

        /**
         * Whether unmocked methods from android.jar should throw exceptions or return default
         * values (i.e. zero or null).
         *
         * <p>See <a href="http://tools.android.com/tech-docs/unit-testing-support">Unit testing support</a> for details.
         *
         * @since 1.1
         */
        public boolean isReturnDefaultValues() {
            return returnDefaultValues;
        }

        public void setReturnDefaultValues(boolean returnDefaultValues) {
            this.returnDefaultValues = returnDefaultValues;
        }

        /**
         * Configures all unit testing tasks.
         *
         * <p>See {@link Test} for available options.
         *
         * <p>Inside the closure you can check the name of the task to configure only some test
         * tasks, e.g.
         *
         * <pre>
         * android {
         *     testOptions {
         *         unitTests.all {
         *             if (it.name == 'testDebug') {
         *                 systemProperty 'debug', 'true'
         *             }
         *         }
         *     }
         * }
         * </pre>
         *
         * @since 1.2
         */
        public void all(final Closure<Test> configClosure) {
            testTasks.all(new Action<Test>() {
                @Override
                public void execute(Test testTask) {
                    ConfigureUtil.configure(configClosure, testTask);
                }
            });
        }


        /**
         * Configures a given test task. The configuration closures that were passed to
         * {@link #all(Closure)} will be applied to it.
         *
         * <p>Not meant to be called from build scripts. The reason it exists is that tasks
         * are created after the build scripts are evaluated, so users have to "register" their
         * configuration closures first and we can only apply them later.
         *
         * @since 1.2
         */
        public void applyConfiguration(@NonNull Test task) {
            this.testTasks.add(task);
        }
    }
}
