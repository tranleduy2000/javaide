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

package com.android.build.gradle.internal.api
import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.builder.model.BaseConfig
import com.android.builder.model.ClassField
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
/**
 * Read-only version of the BaseConfig wrapping another BaseConfig.
 *
 * In the variant API, it is important that the objects returned by the variants
 * are read-only.
 *
 * However, even though the API is defined to use the base interfaces as return
 * type (which all contain only getters), the dynamics of Groovy makes it easy to
 * actually use the setters of the implementation classes.
 *
 * This wrapper ensures that the returned instance is actually just a strict implementation
 * of the base interface and is read-only.
 */
abstract class ReadOnlyBaseConfig implements BaseConfig {

    @NonNull
    private BaseConfig baseConfig

    protected ReadOnlyBaseConfig(@NonNull BaseConfig baseConfig) {
        this.baseConfig = baseConfig
    }

    @NonNull
    @Override
    public String getName() {
        return baseConfig.getName()
    }

    @NonNull
    @Override
    public Map<String, ClassField> getBuildConfigFields() {
        // TODO: cache immutable map?
        return ImmutableMap.copyOf(baseConfig.getBuildConfigFields())
    }

    @NonNull
    @Override
    public Map<String, ClassField> getResValues() {
        return ImmutableMap.copyOf(baseConfig.getResValues())
    }

    @NonNull
    @Override
    public Collection<File> getProguardFiles() {
        return ImmutableList.copyOf(baseConfig.getProguardFiles())
    }

    @NonNull
    @Override
    public Collection<File> getConsumerProguardFiles() {
        return ImmutableList.copyOf(baseConfig.getConsumerProguardFiles())
    }

    @NonNull
    @Override
    Collection<File> getTestProguardFiles() {
        return ImmutableList.copyOf(baseConfig.getTestProguardFiles())
    }

    @NonNull
    @Override
    public Map<String, Object> getManifestPlaceholders() {
        return ImmutableMap.copyOf(baseConfig.getManifestPlaceholders())
    }

    @Nullable
    @Override
    public Boolean getMultiDexEnabled() {
        return baseConfig.getMultiDexEnabled()
    }

    @Nullable
    @Override
    public File getMultiDexKeepFile() {
        return baseConfig.getMultiDexKeepFile()
    }

    @Nullable
    @Override
    public File getMultiDexKeepProguard() {
        return baseConfig.getMultiDexKeepProguard()
    }

    /**
     * Some build scripts add dynamic properties to flavors declaration (and others) and expect
     * to retrieve such properties values through this model. Delegate any property we don't
     * know about to the {@see BaseConfig} groovy object which hopefully will know about the
     * dynamic property.
     * @param name the property name
     * @return the property value if exists or an exception will be thrown.
     */
    def propertyMissing(String name) {
        try {
            baseConfig."$name"
        } catch(MissingPropertyException e) {
            // do not leak implementation types, replace the delegate with ourselves in the message
            throw new MissingPropertyException("Could not find ${name} on ${this}")
        }
    }

    /**
     * Do not authorize setting dynamic properties values and provide a meaningful error message.
     */
    def propertyMissing(String name, value)  {
        throw new RuntimeException("Cannot set property @{name} on read-only ${baseConfig.class}")
    }

    /**
     * Provide dynamic properties refective access.
     * @param name a property name
     * @return true if this object of {@link #baseConfig} supports the passed property name
     */
    def hasProperty(String name) {
        if (super.hasProperty(name)) {
            return true
        }
        return baseConfig.hasProperty(name)
    }
}
