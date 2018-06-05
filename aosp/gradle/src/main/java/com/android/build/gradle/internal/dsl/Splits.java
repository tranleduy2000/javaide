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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;

import org.gradle.api.Action;
import org.gradle.internal.reflect.Instantiator;

import java.util.Set;

/**
 * DSL object for configuring APK Splits options.
 *
 * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/user-guide/apk-splits">APK Splits</a>.
 */
public class Splits {

    private final DensitySplitOptions density;
    private final AbiSplitOptions abi;
    private final LanguageSplitOptions language;

    public Splits(@NonNull Instantiator instantiator) {
        density = instantiator.newInstance(DensitySplitOptions.class);
        abi = instantiator.newInstance(AbiSplitOptions.class);
        language = instantiator.newInstance(LanguageSplitOptions.class);
    }

    /**
     * Density settings.
     */
    public DensitySplitOptions getDensity() {
        return density;
    }

    /**
     * Configures density split settings.
     */
    public void density(Action<DensitySplitOptions> action) {
        action.execute(density);
    }

    /**
     * ABI settings.
     */
    public AbiSplitOptions getAbi() {
        return abi;
    }

    /**
     * Configures ABI split settings.
     */
    public void abi(Action<AbiSplitOptions> action) {
        action.execute(abi);
    }

    /**
     * Language settings.
     */
    public LanguageSplitOptions getLanguage() {
        return language;
    }

    /**
     * Configures the language split settings.
     */
    public void language(Action<LanguageSplitOptions> action) {
        action.execute(language);
    }

    /**
     * Returns the list of Density filters used for multi-apk.
     *
     * <p>null value is allowed, indicating the need to generate an apk with all densities.
     *
     * @return a set of filters.
     */
    @NonNull
    public Set<String> getDensityFilters() {
        return density.getApplicableFilters();
    }

    /**
     * Returns the list of ABI filters used for multi-apk.
     *
     * <p>null value is allowed, indicating the need to generate an apk with all abis.
     *
     * @return a set of filters.
     */
    @NonNull
    public Set<String> getAbiFilters() {
        return abi.getApplicableFilters();
    }

    /**
     * Returns the list of language filters used for multi-apk.
     *
     * <>null value is allowed, indicating the need to generate an apk with all languages.
     *
     * @return a set of language filters.
     */
    @NonNull
    public Set<String> getLanguageFilters() {
        return language.getApplicationFilters();
    }
}
