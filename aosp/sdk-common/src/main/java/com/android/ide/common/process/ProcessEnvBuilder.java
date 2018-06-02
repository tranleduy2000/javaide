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

package com.android.ide.common.process;

import com.android.annotations.NonNull;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * An abstract process builder that can hold environment variable information.
 */
public abstract class ProcessEnvBuilder<T extends ProcessEnvBuilder> {

    protected final Map<String, Object> mEnvironment = Maps.newHashMap();

    /**
     * Adds env variables to use when running the process.
     * @param map the map of env var/values to add
     * @return this
     */
    @NonNull
    public T addEnvironments(@NonNull Map<String, ?> map) {
        mEnvironment.putAll(map);
        return thisAsT();
    }

    /**
     * Adds an env variable and value to use when running the process.
     * @param name the name of the env var
     * @param value the env var value
     * @return this
     */
    @NonNull
    public T addEnvironment(@NonNull String name, Object value) {
        mEnvironment.put(name, value);
        return thisAsT();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private T thisAsT() {
        return (T) this;
    }
}
