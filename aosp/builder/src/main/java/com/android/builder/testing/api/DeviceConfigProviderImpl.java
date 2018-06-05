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

package com.android.builder.testing.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.util.List;

/**
 * Implementation of {@link DeviceConfigProvider} using a {@link DeviceConnector} and
 * {@link DeviceConfig}
 */
public class DeviceConfigProviderImpl implements DeviceConfigProvider {

    private final DeviceConnector deviceConnector;
    private final DeviceConfig deviceConfig;


    public DeviceConfigProviderImpl(DeviceConnector deviceConnector) throws DeviceException {
        this.deviceConnector = deviceConnector;
        this.deviceConfig = deviceConnector.getDeviceConfig();
    }

    @NonNull
    @Override
    public String getConfigFor(String abi) {
        return deviceConfig.getConfigFor(abi);
    }

    @Override
    public int getDensity() {
        return deviceConnector.getDensity();
    }

    @Nullable
    @Override
    public String getLanguage() {
        return deviceConnector.getLanguage();
    }

    @Nullable
    @Override
    public String getRegion() {
        return deviceConnector.getRegion();
    }

    @NonNull
    @Override
    public List<String> getAbis() {
        return deviceConnector.getAbis();
    }
}
