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

package com.android.sdklib.devices;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.prefs.AndroidLocation;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.repository.PkgProps;
import com.android.utils.ILogger;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.Closeables;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * Manager class for interacting with {@link Device}s within the SDK
 */
public class DeviceManager {

    private static final String  DEVICE_PROFILES_PROP = "DeviceProfiles";
    private static final Pattern PATH_PROPERTY_PATTERN =
        Pattern.compile('^' + PkgProps.EXTRA_PATH + '=' + DEVICE_PROFILES_PROP + '$');
    private ILogger mLog;
    private Table<String, String, Device> mVendorDevices;
    private Table<String, String, Device> mSysImgDevices;
    private Table<String, String, Device> mUserDevices;
    private Table<String, String, Device> mDefaultDevices;
    private final Object mLock = new Object();
    private final List<DevicesChangedListener> sListeners = new ArrayList<DevicesChangedListener>();
    private final String mOsSdkPath;

    public enum DeviceFilter {
        /** getDevices() flag to list default devices from the bundled devices.xml definitions. */
        DEFAULT,
        /** getDevices() flag to list user devices saved in the .android home folder. */
        USER,
        /** getDevices() flag to list vendor devices -- the bundled nexus.xml devices
         *  as well as all those coming from extra packages. */
        VENDOR,
        /** getDevices() flag to list devices from system-images/platform-N/tag/abi/devices.xml */
        SYSTEM_IMAGES,
    }

    /** getDevices() flag to list all devices. */
    public static final EnumSet<DeviceFilter> ALL_DEVICES  = EnumSet.allOf(DeviceFilter.class);

    public enum DeviceStatus {
        /**
         * The device exists unchanged from the given configuration
         */
        EXISTS,
        /**
         * A device exists with the given name and manufacturer, but has a different configuration
         */
        CHANGED,
        /**
         * There is no device with the given name and manufacturer
         */
        MISSING
    }

    /**
     * Creates a new instance of DeviceManager.
     *
     * @param sdkLocation Path to the current SDK. If null or invalid, vendor and system images
     *                    devices are ignored.
     * @param log SDK logger instance. Should be non-null.
     */
    public static DeviceManager createInstance(@Nullable File sdkLocation, @NonNull ILogger log) {
        // TODO consider using a cache and reusing the same instance of the device manager
        // for the same manager/log combo.
        return new DeviceManager(sdkLocation == null ? null : sdkLocation.getPath(), log);
    }

    /**
     * Creates a new instance of DeviceManager.
     *
     * @param osSdkPath Path to the current SDK. If null or invalid, vendor devices are ignored.
     * @param log SDK logger instance. Should be non-null.
     */
    private DeviceManager(@Nullable String osSdkPath, @NonNull ILogger log) {
        mOsSdkPath = osSdkPath;
        mLog = log;
    }

    /**
     * Interface implemented by objects which want to know when changes occur to the {@link Device}
     * lists.
     */
    public interface DevicesChangedListener {
        /**
         * Called after one of the {@link Device} lists has been updated.
         */
        void onDevicesChanged();
    }

    /**
     * Register a listener to be notified when the device lists are modified.
     *
     * @param listener The listener to add. Ignored if already registered.
     */
    public void registerListener(@NonNull DevicesChangedListener listener) {
        synchronized (sListeners) {
            if (!sListeners.contains(listener)) {
                sListeners.add(listener);
            }
        }
    }

    /**
     * Removes a listener from the notification list such that it will no longer receive
     * notifications when modifications to the {@link Device} list occur.
     *
     * @param listener The listener to remove.
     */
    public boolean unregisterListener(@NonNull DevicesChangedListener listener) {
        synchronized (sListeners) {
            return sListeners.remove(listener);
        }
    }

    @NonNull
    public DeviceStatus getDeviceStatus(@NonNull String name, @NonNull String manufacturer) {
        Device d = getDevice(name, manufacturer);
        if (d == null) {
            return DeviceStatus.MISSING;
        }

        return DeviceStatus.EXISTS;
    }

    @Nullable
    public Device getDevice(@NonNull String id, @NonNull String manufacturer) {
        initDevicesLists();
        Device d = mUserDevices.get(id, manufacturer);
        if (d != null) {
            return d;
        }
        d = mSysImgDevices.get(id, manufacturer);
        if (d != null) {
            return d;
        }
        d = mDefaultDevices.get(id, manufacturer);
        if (d != null) {
            return d;
        }
        d = mVendorDevices.get(id, manufacturer);
        return d;
    }

    @Nullable
    private Device getDeviceImpl(@NonNull Iterable<Device> devicesList,
                                 @NonNull String id,
                                 @NonNull String manufacturer) {
        for (Device d : devicesList) {
            if (d.getId().equals(id) && d.getManufacturer().equals(manufacturer)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Returns the known {@link Device} list.
     *
     * @param deviceFilter One of the {@link DeviceFilter} constants.
     * @return A copy of the list of {@link Device}s. Can be empty but not null.
     */
    @NonNull
    public Collection<Device> getDevices(@NonNull DeviceFilter deviceFilter) {
        return getDevices(EnumSet.of(deviceFilter));
    }

    /**
     * Returns the known {@link Device} list.
     *
     * @param deviceFilter A combination of the {@link DeviceFilter} constants
     *                     or the constant {@link DeviceManager#ALL_DEVICES}.
     * @return A copy of the list of {@link Device}s. Can be empty but not null.
     */
    @NonNull
    public Collection<Device> getDevices(@NonNull EnumSet<DeviceFilter> deviceFilter) {
        initDevicesLists();
        Table<String, String, Device> devices = HashBasedTable.create();
        if (mUserDevices != null && (deviceFilter.contains(DeviceFilter.USER))) {
            devices.putAll(mUserDevices);
        }
        if (mDefaultDevices != null && (deviceFilter.contains(DeviceFilter.DEFAULT))) {
            devices.putAll(mDefaultDevices);
        }
        if (mVendorDevices != null && (deviceFilter.contains(DeviceFilter.VENDOR))) {
            devices.putAll(mVendorDevices);
        }
        if (mSysImgDevices != null && (deviceFilter.contains(DeviceFilter.SYSTEM_IMAGES))) {
            devices.putAll(mSysImgDevices);
        }
        return Collections.unmodifiableCollection(devices.values());
    }

    private void initDevicesLists() {
        boolean changed = initDefaultDevices();
        changed |= initVendorDevices();
        changed |= initSysImgDevices();
        changed |= initUserDevices();
        if (changed) {
            notifyListeners();
        }
    }

    /**
     * Initializes the {@link Device}s packaged with the SDK.
     * @return True if the list has changed.
     */
    private boolean initDefaultDevices() {
        synchronized (mLock) {
            if (mDefaultDevices != null) {
                return false;
            }
            InputStream stream = DeviceManager.class
                    .getResourceAsStream(SdkConstants.FN_DEVICES_XML);
            try {
                assert stream != null : SdkConstants.FN_DEVICES_XML + " not bundled in sdklib.";
                mDefaultDevices = DeviceParser.parse(stream);
                return true;
            } catch (IllegalStateException e) {
                // The device builders can throw IllegalStateExceptions if
                // build gets called before everything is properly setup
                mLog.error(e, null);
                mDefaultDevices = HashBasedTable.create();
            } catch (Exception e) {
                mLog.error(e, "Error reading default devices");
                mDefaultDevices = HashBasedTable.create();
            } finally {
                Closeables.closeQuietly(stream);
            }
        }
        return false;
    }

    /**
     * Initializes all vendor-provided {@link Device}s: the bundled nexus.xml devices
     * as well as all those coming from extra packages.
     * @return True if the list has changed.
     */
    private boolean initVendorDevices() {
        synchronized (mLock) {
            if (mVendorDevices != null) {
                return false;
            }

            mVendorDevices = HashBasedTable.create();

            // Load builtin devices
            InputStream stream = DeviceManager.class.getResourceAsStream("nexus.xml");
            try {
                mVendorDevices.putAll(DeviceParser.parse(stream));
            } catch (Exception e) {
                mLog.error(e, "Could not load nexus devices");
            } finally {
                Closeables.closeQuietly(stream);
            }

            stream = DeviceManager.class.getResourceAsStream("wear.xml");
            try {
                mVendorDevices.putAll(DeviceParser.parse(stream));
            } catch (Exception e) {
                mLog.error(e, "Could not load wear devices");
            } finally {
                Closeables.closeQuietly(stream);
            }

            stream = DeviceManager.class.getResourceAsStream("tv.xml");
            try {
                mVendorDevices.putAll(DeviceParser.parse(stream));
            } catch (Exception e) {
                mLog.error(e, "Could not load tv devices");
            } finally {
                Closeables.closeQuietly(stream);
            }

            if (mOsSdkPath != null) {
                // Load devices from vendor extras
                File extrasFolder = new File(mOsSdkPath, SdkConstants.FD_EXTRAS);
                List<File> deviceDirs = getExtraDirs(extrasFolder);
                for (File deviceDir : deviceDirs) {
                    File deviceXml = new File(deviceDir, SdkConstants.FN_DEVICES_XML);
                    if (deviceXml.isFile()) {
                        mVendorDevices.putAll(loadDevices(deviceXml));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes all system-image provided {@link Device}s.
     * @return True if the list has changed.
     */
    private boolean initSysImgDevices() {
        synchronized (mLock) {
            if (mSysImgDevices != null) {
                return false;
            }
            mSysImgDevices = HashBasedTable.create();

            if (mOsSdkPath == null) {
                return false;
            }

            // Load devices from tagged system-images
            // Path pattern is /sdk/system-images/<platform-N>/<tag>/<abi>/devices.xml

            FileOp fop = new FileOp();
            File sysImgFolder = new File(mOsSdkPath, SdkConstants.FD_SYSTEM_IMAGES);

            for (File platformFolder : fop.listFiles(sysImgFolder)) {
                if (!fop.isDirectory(platformFolder)) {
                    continue;
                }

                for (File tagFolder : fop.listFiles(platformFolder)) {
                    if (!fop.isDirectory(tagFolder)) {
                        continue;
                    }

                    for (File abiFolder : fop.listFiles(tagFolder)) {
                        if (!fop.isDirectory(abiFolder)) {
                            continue;
                        }

                        File deviceXml = new File(abiFolder, SdkConstants.FN_DEVICES_XML);
                        if (fop.isFile(deviceXml)) {
                            mSysImgDevices.putAll(loadDevices(deviceXml));
                        }
                    }
                }
            }
            return true;
        }
    }

    /**
     * Initializes all user-created {@link Device}s
     * @return True if the list has changed.
     */
    private boolean initUserDevices() {
        synchronized (mLock) {
            if (mUserDevices != null) {
                return false;
            }
            // User devices should be saved out to
            // $HOME/.android/devices.xml
            mUserDevices = HashBasedTable.create();
            File userDevicesFile = null;
            try {
                userDevicesFile = new File(
                        AndroidLocation.getFolder(),
                        SdkConstants.FN_DEVICES_XML);
                if (userDevicesFile.exists()) {
                    mUserDevices.putAll(DeviceParser.parse(userDevicesFile));
                    return true;
                }
            } catch (AndroidLocationException e) {
                mLog.warning("Couldn't load user devices: %1$s", e.getMessage());
            } catch (SAXException e) {
                // Probably an old config file which we don't want to overwrite.
                if (userDevicesFile != null) {
                    String base = userDevicesFile.getAbsoluteFile() + ".old";
                    File renamedConfig = new File(base);
                    int i = 0;
                    while (renamedConfig.exists()) {
                        renamedConfig = new File(base + '.' + (i++));
                    }
                    mLog.error(e, "Error parsing %1$s, backing up to %2$s",
                            userDevicesFile.getAbsolutePath(),
                            renamedConfig.getAbsolutePath());
                    userDevicesFile.renameTo(renamedConfig);
                }
            } catch (ParserConfigurationException e) {
                mLog.error(e, "Error parsing %1$s",
                        userDevicesFile == null ? "(null)" : userDevicesFile.getAbsolutePath());
            } catch (IOException e) {
                mLog.error(e, "Error parsing %1$s",
                        userDevicesFile == null ? "(null)" : userDevicesFile.getAbsolutePath());
            }
        }
        return false;
    }

    public void addUserDevice(@NonNull Device d) {
        boolean changed = false;
        synchronized (mLock) {
            if (mUserDevices == null) {
                initUserDevices();
                assert mUserDevices != null;
            }
            if (mUserDevices != null) {
                mUserDevices.put(d.getId(), d.getManufacturer(), d);
            }
            changed = true;
        }
        if (changed) {
            notifyListeners();
        }
    }

    public void removeUserDevice(@NonNull Device d) {
        synchronized (mLock) {
            if (mUserDevices == null) {
                initUserDevices();
                assert mUserDevices != null;
            }
            if (mUserDevices != null) {
                if (mUserDevices.contains(d.getId(), d.getManufacturer())) {
                    mUserDevices.remove(d.getId(), d.getManufacturer());
                    notifyListeners();
                }
            }
        }
    }

    public void replaceUserDevice(@NonNull Device d) {
        synchronized (mLock) {
            if (mUserDevices == null) {
                initUserDevices();
            }
            removeUserDevice(d);
            addUserDevice(d);
        }
    }

    /**
     * Saves out the user devices to {@link SdkConstants#FN_DEVICES_XML} in
     * {@link AndroidLocation#getFolder()}.
     */
    public void saveUserDevices() {
        if (mUserDevices == null) {
            return;
        }

        File userDevicesFile = null;
        try {
            userDevicesFile = new File(AndroidLocation.getFolder(),
                    SdkConstants.FN_DEVICES_XML);
        } catch (AndroidLocationException e) {
            mLog.warning("Couldn't find user directory: %1$s", e.getMessage());
            return;
        }

        if (mUserDevices.isEmpty()) {
            userDevicesFile.delete();
            return;
        }

        synchronized (mLock) {
            if (!mUserDevices.isEmpty()) {
                try {
                    DeviceWriter.writeToXml(new FileOutputStream(userDevicesFile), mUserDevices.values());
                } catch (FileNotFoundException e) {
                    mLog.warning("Couldn't open file: %1$s", e.getMessage());
                } catch (ParserConfigurationException e) {
                    mLog.warning("Error writing file: %1$s", e.getMessage());
                } catch (TransformerFactoryConfigurationError e) {
                    mLog.warning("Error writing file: %1$s", e.getMessage());
                } catch (TransformerException e) {
                    mLog.warning("Error writing file: %1$s", e.getMessage());
                }
            }
        }
    }


    @NonNull
    private Table<String, String, Device> loadDevices(@NonNull File deviceXml) {
        try {
            return DeviceParser.parse(deviceXml);
        } catch (SAXException e) {
            mLog.error(e, "Error parsing %1$s", deviceXml.getAbsolutePath());
        } catch (ParserConfigurationException e) {
            mLog.error(e, "Error parsing %1$s", deviceXml.getAbsolutePath());
        } catch (IOException e) {
            mLog.error(e, "Error reading %1$s", deviceXml.getAbsolutePath());
        } catch (AssertionError e) {
            mLog.error(e, "Error parsing %1$s", deviceXml.getAbsolutePath());
        } catch (IllegalStateException e) {
            // The device builders can throw IllegalStateExceptions if
            // build gets called before everything is properly setup
            mLog.error(e, null);
        }
        return HashBasedTable.create();
    }

    private void notifyListeners() {
        synchronized (sListeners) {
            for (DevicesChangedListener listener : sListeners) {
                listener.onDevicesChanged();
            }
        }
    }

    /* Returns all of DeviceProfiles in the extras/ folder */
    @NonNull
    private List<File> getExtraDirs(@NonNull File extrasFolder) {
        List<File> extraDirs = new ArrayList<File>();
        // All OEM provided device profiles are in
        // $SDK/extras/$VENDOR/$ITEM/devices.xml
        if (extrasFolder != null && extrasFolder.isDirectory()) {
            for (File vendor : extrasFolder.listFiles()) {
                if (vendor.isDirectory()) {
                    for (File item : vendor.listFiles()) {
                        if (item.isDirectory() && isDevicesExtra(item)) {
                            extraDirs.add(item);
                        }
                    }
                }
            }
        }

        return extraDirs;
    }

    /*
     * Returns whether a specific folder for a specific vendor is a
     * DeviceProfiles folder
     */
    private boolean isDevicesExtra(@NonNull File item) {
        File properties = new File(item, SdkConstants.FN_SOURCE_PROP);
        try {
            BufferedReader propertiesReader = new BufferedReader(new FileReader(properties));
            try {
                String line;
                while ((line = propertiesReader.readLine()) != null) {
                    Matcher m = PATH_PROPERTY_PATTERN.matcher(line);
                    if (m.matches()) {
                        return true;
                    }
                }
            } finally {
                propertiesReader.close();
            }
        } catch (IOException ignore) {
        }
        return false;
    }
}
