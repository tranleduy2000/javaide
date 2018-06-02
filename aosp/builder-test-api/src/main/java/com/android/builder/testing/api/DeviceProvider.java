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

package com.android.builder.testing.api;

import com.android.annotations.NonNull;
import com.google.common.annotations.Beta;

import java.util.List;

/**
 * Provides a list of remote or local devices.
 */
@Beta
public abstract class DeviceProvider {

    /**
     * Returns the name of the provider. Must be unique, not contain spaces, and start with a lower
     * case.
     *
     * @return the name of the provider.
     */
    @NonNull
    public abstract String getName();

    /**
     * Initializes the provider. This is called before any other method (except {@link #getName()}).
     * @throws DeviceException
     */
    public abstract void init() throws DeviceException;

    public abstract void terminate() throws DeviceException;

    /**
     * Returns a list of DeviceConnector.
     * @return a non-null list (but could be empty.)
     */
    @NonNull
    public abstract List<? extends DeviceConnector> getDevices();

    /**
     * Returns the timeout to use.
     * @return the time out in milliseconds.
     */
    public abstract int getTimeoutInMs();

    /**
     * Returns true if the provider is configured and able to run.
     *
     * @return if the provider is configured.
     */
    public abstract boolean isConfigured();

    public int getMaxThreads() {
        return 0;
    }
}
