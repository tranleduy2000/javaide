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
import com.android.annotations.Nullable;
import com.android.builder.testing.api.DeviceConfig;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.utils.ILogger;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Local device connected to with ddmlib. This is a wrapper around {@link IDevice}.
 */
public class ConnectedDevice extends DeviceConnector {

    private final IDevice iDevice;

    public ConnectedDevice(@NonNull IDevice iDevice) {
        this.iDevice = iDevice;
    }

    @NonNull
    @Override
    public String getName() {
        String version = iDevice.getProperty(IDevice.PROP_BUILD_VERSION);
        boolean emulator = iDevice.isEmulator();

        String name;
        if (emulator) {
            name = iDevice.getAvdName() != null ?
                    iDevice.getAvdName() + "(AVD)" :
                    iDevice.getSerialNumber();
        } else {
            String model = iDevice.getProperty(IDevice.PROP_DEVICE_MODEL);
            name = model != null ? model : iDevice.getSerialNumber();
        }

        return version != null ? name + " - " + version : name;
    }

    @Override
    public void connect(int timeout, ILogger logger) throws TimeoutException {
        // nothing to do here
    }

    @Override
    public void disconnect(int timeout, ILogger logger) throws TimeoutException {
        // nothing to do here
    }

    @Override
    public void installPackage(@NonNull File apkFile,
            @NonNull Collection<String> options,
            int timeout,
            ILogger logger) throws DeviceException {
        try {
            iDevice.installPackage(apkFile.getAbsolutePath(), true /*reinstall*/,
                    options.isEmpty() ? null : options.toArray(new String[options.size()]));
        } catch (Exception e) {
            logger.error(e, "Unable to install " + apkFile.getAbsolutePath());
            throw new DeviceException(e);
        }
    }

    @Override
    public void installPackages(@NonNull List<File> splitApkFiles,
            @NonNull Collection<String> options,
            int timeoutInMs,
            ILogger logger)
            throws DeviceException {

        List<String> apkFileNames = Lists.transform(splitApkFiles, new Function<File, String>() {
            @Override
            public String apply(@Nullable File input) {
                return input != null ? input.getAbsolutePath() : null;
            }
        });
        try {
            iDevice.installPackages(apkFileNames, timeoutInMs, true /*reinstall*/,
                    options.isEmpty() ? null : options.toArray(new String[options.size()]));
        } catch (Exception e) {
            logger.error(e, "Unable to install " + Joiner.on(',').join(apkFileNames));
            throw new DeviceException(e);
        }
    }

    @Override
    public void uninstallPackage(@NonNull String packageName, int timeout, ILogger logger) throws DeviceException {
        try {
            iDevice.uninstallPackage(packageName);
        } catch (Exception e) {
            logger.error(e, "Unable to uninstall " + packageName);
            throw new DeviceException(e);
        }
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
                                    long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
                                    throws TimeoutException, AdbCommandRejectedException,
                                    ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    @NonNull
    @Override
    public Future<String> getSystemProperty(@NonNull String name) {
        return iDevice.getSystemProperty(name);
    }

    @Override
    public void pullFile(String remote, String local) throws IOException {
        try {
            iDevice.pullFile(remote, local);

        } catch (TimeoutException e) {
            throw new IOException(String.format("Failed to pull %s from device", remote), e);
        } catch (AdbCommandRejectedException e) {
            throw new IOException(String.format("Failed to pull %s from device", remote), e);
        } catch (SyncException e) {
            throw new IOException(String.format("Failed to pull %s from device", remote), e);
        }
    }

    @NonNull
    @Override
    public String getSerialNumber() {
        return iDevice.getSerialNumber();
    }

    @Override
    public int getApiLevel() {
        String sdkVersion = iDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL);
        if (sdkVersion != null) {
            try {
                return Integer.valueOf(sdkVersion);
            } catch (NumberFormatException e) {

            }
        }

        // can't get it, return 0.
        return 0;
    }

    @Override
    public String getApiCodeName() {
        String codeName = iDevice.getProperty(IDevice.PROP_BUILD_CODENAME);
        if (codeName != null) {
            // if this is a release platform return null.
            if ("REL".equals(codeName)) {
                return null;
            }

            // else return the codename
            return codeName;
        }

        // can't get it, return 0.
        return null;
    }

    @Nullable
    @Override
    public IDevice.DeviceState getState() {
        return iDevice.getState();
    }

    @NonNull
    @Override
    public List<String> getAbis() {
        return iDevice.getAbis();
    }

    @Override
    public int getDensity() {
        return iDevice.getDensity();
    }

    @Override
    public int getHeight() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getWidth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLanguage() {
        return iDevice.getLanguage();
    }

    @Override
    public String getRegion() {
        return iDevice.getRegion();
    }

    @Override
    @NonNull
    public String getProperty(@NonNull String propertyName) {
        return iDevice.getProperty(propertyName);
    }

    @NonNull
    @Override
    public DeviceConfig getDeviceConfig() throws DeviceException {
        final List<String> output = new ArrayList<String>();
        final MultiLineReceiver receiver = new MultiLineReceiver() {
            @Override
            public void processNewLines(String[] lines) {
                output.addAll(Arrays.asList(lines));
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        };
        try {
            executeShellCommand("am get-config", receiver, 5, TimeUnit.SECONDS);
            return DeviceConfig.Builder.parse(output);
        } catch (Exception e) {
            throw new DeviceException(e);
        }
    }
}
