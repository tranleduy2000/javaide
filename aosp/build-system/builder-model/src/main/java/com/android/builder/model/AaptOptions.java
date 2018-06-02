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

package com.android.builder.model;

import java.util.Collection;
import java.util.List;

/**
 * Options for aapt.
 */
public interface AaptOptions {
    /**
     * Returns the value for the --ignore-assets option, or null
     */
    String getIgnoreAssets();

    /**
     * Returns the list of values for the -0 (disabled compression) option, or null
     */
    Collection<String> getNoCompress();

    /**
     * passes the --error-on-missing-config-entry parameter to the aapt command, by default false.
     */
    boolean getFailOnMissingConfigEntry();

    /**
     * Returns the list of additional parameters to pass.
     * @return
     */
    List<String> getAdditionalParameters();
}
