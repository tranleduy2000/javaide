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

package com.android.builder.testing;

import com.android.annotations.NonNull;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.builder.testing.api.DeviceProvider;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DeviceProvider for locally connected devices. Basically returns the list of devices that
 * are currently connected at the time {@link #init()} is called.
 */
public class ConnectedDeviceProvider extends DeviceProvider {

    @NonNull
    private final File adbLocation;
    @NonNull
    private final ILogger iLogger;

    @NonNull
    private final List<ConnectedDevice> localDevices = Lists.newArrayList();

    public ConnectedDeviceProvider(@NonNull File adbLocation, @NonNull ILogger logger) {
        this.adbLocation = adbLocation;
        iLogger = logger;
    }

    @Override
    @NonNull
    public String getName() {
        return "connected";
    }

    @Override
    @NonNull
    public List<? extends DeviceConnector> getDevices() {
        return localDevices;
    }

    @Override
    public void init() throws DeviceException {
        AndroidDebugBridge.initIfNeeded(false /*clientSupport*/);

        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                adbLocation.getAbsolutePath(), false /*forceNewBridge*/);

        long timeOut = 30000; // 30 sec
        int sleepTime = 1000;
        while (!bridge.hasInitialDeviceList() && timeOut > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new DeviceException(e);
            }
            timeOut -= sleepTime;
        }

        if (timeOut <= 0 && !bridge.hasInitialDeviceList()) {
            throw new DeviceException("Timeout getting device list.");
        }

        IDevice[] devices = bridge.getDevices();

        if (devices.length == 0) {
            throw new DeviceException("No connected devices!");
        }

        final String androidSerialsEnv = System.getenv("ANDROID_SERIAL");
        final boolean isValidSerial = androidSerialsEnv != null && !androidSerialsEnv.isEmpty();

        final Set<String> serials;
        if (isValidSerial) {
            serials = Sets.newHashSet(Splitter.on(',').split(androidSerialsEnv));
        } else {
            serials = Collections.emptySet();
        }

        final List<IDevice> filteredDevices = Lists.newArrayListWithCapacity(devices.length);
        for (IDevice iDevice : devices) {
            if (!isValidSerial || serials.contains(iDevice.getSerialNumber())) {
                serials.remove(iDevice.getSerialNumber());
                filteredDevices.add(iDevice);
            }
        }

        if (!serials.isEmpty()) {
            throw new DeviceException(String.format(
                    "Connected device with serial%s '%s' not found!",
                    serials.size() == 1 ? "" : "s",
                    Joiner.on("', '").join(serials)));
        }

        for (IDevice device : filteredDevices) {
            if (device.getState() == IDevice.DeviceState.ONLINE) {
                localDevices.add(new ConnectedDevice(device));
            } else {
                iLogger.info(
                        "Skipping device '%s' (%s): Device is %s%s.",
                        device.getName(), device.getSerialNumber(), device.getState(),
                        device.getState() == IDevice.DeviceState.UNAUTHORIZED ? ",\n"
                                + "    see http://d.android.com/tools/help/adb.html#Enabling" : "");
            }
        }

        if (localDevices.isEmpty()) {
            if (isValidSerial) {
                throw new DeviceException(String.format(
                        "Connected device with serial $1%s is not online.",
                        androidSerialsEnv));
            } else {
                throw new DeviceException("No online devices found.");
            }
        }
    }

    @Override
    public void terminate() throws DeviceException {
        // nothing to be done here.
    }

    @Override
    public int getTimeoutInMs() {
        return 0;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}
