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
import com.android.annotations.Nullable;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellEnabledDevice;
import com.android.ddmlib.TimeoutException;
import com.android.utils.ILogger;
import com.google.common.annotations.Beta;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * A connector to a device to install/uninstall APKs, and run shell command.
 */
@Beta
public abstract class DeviceConnector implements IShellEnabledDevice {

    /**
     * Establishes the connection with the device. Called before any other actions.
     * @param timeOut the time out.
     * @throws TimeoutException
     */
    public abstract void connect(int timeOut, ILogger logger) throws TimeoutException;

    /**
     * Disconnects from the device. No other action is called afterwards.
     * @param timeOut the time out.
     * @throws TimeoutException
     */
    public abstract void disconnect(int timeOut, ILogger logger) throws TimeoutException;

    /**
     * Installs the given APK on the device.
     * @param apkFile the APK file to install.
     * @param timeout the time out.
     * @param logger the logger to use to log debug, warnings and errors.
     * @throws DeviceException
     */
    public abstract void installPackage(
            @NonNull File apkFile,
            @NonNull Collection<String> options,
            int timeout,
            ILogger logger)
            throws DeviceException;

    /**
     * Installs the given APK files on the device. It requires a device with API level 21+ otherwise
     * a {@link com.android.builder.testing.api.DeviceException} will be thrown.
     *
     * @param apkFiles the APK files to install.
     * @param options the install options.
     * @param timeoutInMs the time out in milliseconds.
     * @param logger the logger to use to log debug, warnings and errors.
     * @throws DeviceException
     */
    public abstract void installPackages(
            @NonNull List<File> apkFiles,
            @NonNull Collection<String> options,
            int timeoutInMs,
            ILogger logger)
            throws DeviceException;

    /**
     * Uninstall the given package name from the device
     * @param packageName the package name
     * @param timeout the time out
     * @throws DeviceException
     */
    public abstract void uninstallPackage(@NonNull String packageName, int timeout, ILogger logger)
            throws DeviceException;

    /**
     * Pulls a single file.
     *
     * @param remote the full path to the remote file
     * @param local The local destination.
     *
     * @throws IOException in case of an IO exception.
     */
    public abstract void pullFile(String remote, String local) throws IOException;

    /** Returns the serial number of the device. */
    @NonNull
    public abstract String getSerialNumber();

    /**
     * Returns the API level of the device, or 0 if it could not be queried.
     * @return the api level
     */
    public abstract int getApiLevel();

    /**
     * Returns the API codename for the device, or null if it's a release device.
     * @return the API codename
     */
    public abstract String getApiCodeName();

    /**
     * Returns the {@link com.android.ddmlib.IDevice.DeviceState} for the device, or null
     * if if cannot determined.
     * @return the device state.
     */
    public abstract IDevice.DeviceState getState();

    /**
     * The device supported ABIs. This is in preferred order.
     * @return the list of supported ABIs
     */
    @NonNull
    public abstract List<String> getAbis();

    /**
     * Returns the screen density bucket for the device.
     * @return the density.
     */
    public abstract int getDensity();

    public abstract int getHeight();

    public abstract int getWidth();

    /**
     * Returns the user's language.
     * @return the user's language or null if it's unknown.
     */
    @Nullable
    public abstract String getLanguage();

    /**
     * Returns the user's region.
     *
     * @return the user's region or null if it's unknown
     */
    @Nullable
    public abstract String getRegion();

    @NonNull
    public abstract String getProperty(@NonNull String propertyName);

    @NonNull
    public abstract DeviceConfig getDeviceConfig() throws DeviceException;
}
