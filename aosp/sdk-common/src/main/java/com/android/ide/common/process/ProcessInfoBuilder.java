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

package com.android.ide.common.process;

import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A builder to create a {@link ProcessInfo} or a {@link JavaProcessInfo}.
 */
public class ProcessInfoBuilder extends ProcessEnvBuilder<ProcessInfoBuilder> {

    private String mExecutable;
    private String mClasspath;
    private String mMain;
    private final List<String> mArgs = Lists.newArrayList();
    private final List<String> mJvmArgs = Lists.newArrayList();

    public ProcessInfoBuilder() {
    }

    /**
     * Creates a ProcessInfo from the the information added to the builder.
     * @return the ProcessInfo
     */
    @NonNull
    public ProcessInfo createProcess() {
        checkNotNull(mExecutable, "executable is missing");

        return new ProcessInfoImpl(
                mExecutable,
                ImmutableList.copyOf(mArgs),
                ImmutableMap.copyOf(mEnvironment));
    }

    /**
     * Creates a JavaProcessInfo from the the information added to the builder.
     * @return the JavaProcessInfo
     */
    @NonNull
    public JavaProcessInfo createJavaProcess() {
        checkNotNull(mClasspath, "classpath is missing");
        checkNotNull(mMain, "main class is missing");

        return new JavaProcessInfoImpl(
                mClasspath,
                mMain,
                ImmutableList.copyOf(mArgs),
                ImmutableMap.copyOf(mEnvironment),
                ImmutableList.copyOf(mJvmArgs));
    }

    /**
     * Sets the executable.
     * @param executable the executable
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder setExecutable(@NonNull String executable) {
        mExecutable = executable;
        return this;
    }

    /**
     * Sets the executable.
     * @param executable the executable
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder setExecutable(@NonNull File executable) {
        mExecutable = executable.getAbsolutePath();
        return this;
    }

    /**
     * Sets the Java classpath
     * @param classpath the classpath
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder setClasspath(@NonNull String classpath) {
        mClasspath = classpath;
        return this;
    }

    /**
     * Sets the Main Java class
     * @param mainClass the main class
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder setMain(@NonNull String mainClass) {
        mMain = mainClass;
        return this;
    }

    /**
     * Adds an command line argument.
     * @param arg the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(@NonNull String arg) {
        mArgs.add(arg);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(@NonNull String arg1, @NonNull String arg2) {
        mArgs.add(arg1);
        mArgs.add(arg2);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @param arg3 the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(
            @NonNull String arg1,
            @NonNull String arg2,
            @NonNull String arg3) {
        mArgs.add(arg1);
        mArgs.add(arg2);
        mArgs.add(arg3);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @param arg3 the argument
     * @param args the additional arguments
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(
            @NonNull String arg1,
            @NonNull String arg2,
            @NonNull String arg3,
            @NonNull String... args) {
        mArgs.add(arg1);
        mArgs.add(arg2);
        mArgs.add(arg3);
        mArgs.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param args the arguments
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(@NonNull List<String> args) {
        mArgs.addAll(args);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param args the arguments
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addArgs(@NonNull String[] args) {
        Collections.addAll(mArgs, args);
        return this;
    }

    /**
     * Adds an command line argument.
     * @param arg the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addJvmArg(@NonNull String arg) {
        mJvmArgs.add(arg);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addJvmArgs(@NonNull String arg1, @NonNull String arg2) {
        mJvmArgs.add(arg1);
        mJvmArgs.add(arg2);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @param arg3 the argument
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addJvmArgs(
            @NonNull String arg1,
            @NonNull String arg2,
            @NonNull String arg3) {
        mJvmArgs.add(arg1);
        mJvmArgs.add(arg2);
        mJvmArgs.add(arg3);
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param arg1 the argument
     * @param arg2 the argument
     * @param arg3 the argument
     * @param args the additional arguments
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addJvmArgs(
            @NonNull String arg1,
            @NonNull String arg2,
            @NonNull String arg3,
            @NonNull String... args) {
        mJvmArgs.add(arg1);
        mJvmArgs.add(arg2);
        mJvmArgs.add(arg3);
        mJvmArgs.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * Adds several command line arguments.
     * @param args the arguments
     * @return this
     */
    @NonNull
    public ProcessInfoBuilder addJvmArgs(@NonNull List<String> args) {
        mJvmArgs.addAll(args);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Executable : ").append(mExecutable).append("\n");
        sb.append("arguments : \n").append(Joiner.on("\n").join(mArgs)).append("\n");
        sb.append("jvmArgs : \n").append(Joiner.on(",").join(mJvmArgs)).append("\n");
        return sb.toString();
    }

    protected static class ProcessInfoImpl implements ProcessInfo {

        public ProcessInfoImpl(
                @NonNull String executable,
                @NonNull List<String> args,
                @NonNull Map<String, Object> environment) {
            mExecutable = executable;
            mArgs = args;
            mEnvironment = environment;
        }

        private final String mExecutable;
        private final List<String> mArgs;
        private final Map<String, Object> mEnvironment;

        @NonNull
        @Override
        public String getExecutable() {
            return mExecutable;
        }

        @NonNull
        @Override
        public List<String> getArgs() {
            return mArgs;
        }

        @NonNull
        @Override
        public Map<String, Object> getEnvironment() {
            return mEnvironment;
        }
    }

    protected static class JavaProcessInfoImpl implements JavaProcessInfo {

        public JavaProcessInfoImpl(
                @NonNull String classpath,
                @NonNull String main,
                @NonNull List<String> args,
                @NonNull Map<String, Object> environment,
                @NonNull List<String> jvmArgs) {
            mClasspath = classpath;
            mMain = main;
            mArgs = args;
            mEnvironment = environment;
            mJvmArgs = jvmArgs;
        }

        private final String mClasspath;
        private final String mMain;
        private final List<String> mArgs;
        private final Map<String, Object> mEnvironment;
        private final List<String> mJvmArgs;

        @NonNull
        @Override
        public String getExecutable() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public String getClasspath() {
            return mClasspath;
        }

        @NonNull
        @Override
        public String getMainClass() {
            return mMain;
        }

        @NonNull
        @Override
        public List<String> getArgs() {
            return mArgs;
        }

        @NonNull
        @Override
        public Map<String, Object> getEnvironment() {
            return mEnvironment;
        }

        @NonNull
        @Override
        public List<String> getJvmArgs() {
            return mJvmArgs;
        }
    }
}
