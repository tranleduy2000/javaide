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

package com.android.sdklib.devices;

import com.android.resources.Keyboard;
import com.android.resources.Navigation;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManagerTestCase;
import com.android.sdklib.SystemImage;
import com.android.sdklib.devices.Device.Builder;
import com.android.sdklib.devices.DeviceManager.DeviceFilter;
import com.android.sdklib.devices.DeviceManager.DeviceStatus;
import com.android.sdklib.mock.MockLog;
import com.android.sdklib.repository.descriptors.IdDisplay;

import java.io.File;
import java.util.*;

public class DeviceManagerTest extends SdkManagerTestCase {

    private DeviceManager dm;

    private MockLog log;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dm = createDeviceManager();
    }

    private DeviceManager createDeviceManager() {
        log = super.getLog();
        File sdkLocation = getSdkManager().getLocalSdk().getLocation();
        return DeviceManager.createInstance(sdkLocation, log);
    }

    /**
     * Returns a list of just the devices' display names, for unit test comparisons.
     */
    private static String listDisplayName(Device device) {
        if (device == null) {
            return null;
        }
        return device.getDisplayName();
    }

    /**
     * Returns a list of just the devices' display names, for unit test comparisons.
     */
    private static List<String> listDisplayNames(Collection<Device> devices) {
        if (devices == null) {
            return null;
        }
        List<String> names = new ArrayList<String>();
        for (Device d : devices) {
            names.add(listDisplayName(d));
        }
        Collections.sort(names);
        return names;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testGetDevices_Default() {
        // no user devices defined in the test's custom .android home folder
        assertEquals("[]", dm.getDevices(DeviceFilter.USER).toString());
        assertEquals("", log.toString());

        // no system-images devices defined in the SDK by default
        assertEquals("[]", dm.getDevices(DeviceFilter.SYSTEM_IMAGES).toString());
        assertEquals("", log.toString());

        // this list comes from devices.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/devices.xml
        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, " +
                        "3.2\" HVGA slider (ADP1), 3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, " +
                        "3.7\" FWVGA slider, 3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), " +
                        "4.65\" 720p (Galaxy Nexus), 4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, " +
                        "7\" WSVGA (Tablet)]",
                listDisplayNames(dm.getDevices(DeviceFilter.DEFAULT)).toString());
        assertEquals("", log.toString());

        assertEquals("2.7\" QVGA",
                listDisplayName(dm.getDevice("2.7in QVGA", "Generic")));

        // this list comes from the nexus.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/nexus.xml
        assertEquals("[Android TV (1080p), Android TV (720p), Android Wear Round, " +
                        "Android Wear Round Chin, Android Wear Square, " +
                        "Galaxy Nexus, Nexus 10, Nexus 4, Nexus 5, Nexus 6, Nexus 7, " +
                        "Nexus 7 (2012), Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm.getDevices(DeviceFilter.VENDOR)).toString());
        assertEquals("", log.toString());

        assertEquals("Nexus One",
                listDisplayName(dm.getDevice("Nexus One", "Google")));

        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, 3.2\" HVGA slider (ADP1), " +
                        "3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, 3.7\" FWVGA slider, " +
                        "3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), 4.65\" 720p (Galaxy Nexus), " +
                        "4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, 7\" WSVGA (Tablet), Android TV (1080p), "
                        +
                        "Android TV (720p), Android Wear Round, Android Wear Round Chin, " +
                        "Android Wear Square, Galaxy Nexus, Nexus 10, Nexus 4, Nexus 5, " +
                        "Nexus 6, Nexus 7, Nexus 7 (2012), Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm.getDevices(DeviceManager.ALL_DEVICES)).toString());
        assertEquals("", log.toString());
    }

    public final void testGetDevice() {
        // get a definition from the bundled devices.xml file
        Device d1 = dm.getDevice("7in WSVGA (Tablet)", "Generic");
        assertEquals("7\" WSVGA (Tablet)", d1.getDisplayName());
        assertEquals("", log.toString());

        // get a definition from the bundled nexus.xml file
        Device d2 = dm.getDevice("Nexus One", "Google");
        assertEquals("Nexus One", d2.getDisplayName());
        assertEquals("", log.toString());
    }

    public final void testGetDevices_UserDevice() {

        Device d1 = dm.getDevice("7in WSVGA (Tablet)", "Generic");

        Builder b = new Device.Builder(d1);
        b.setId("MyCustomTablet");
        b.setName("My Custom Tablet");
        b.setManufacturer("OEM");

        Device d2 = b.build();

        dm.addUserDevice(d2);
        dm.saveUserDevices();

        assertEquals("My Custom Tablet", dm.getDevice("MyCustomTablet", "OEM").getDisplayName());
        assertEquals("", log.toString());

        // create a new device manager, forcing it reload all files
        dm = null;
        DeviceManager dm2 = createDeviceManager();

        assertEquals("My Custom Tablet", dm2.getDevice("MyCustomTablet", "OEM").getDisplayName());
        assertEquals("", log.toString());

        // 1 user device defined in the test's custom .android home folder
        assertEquals("[My Custom Tablet]",
                listDisplayNames(dm2.getDevices(DeviceFilter.USER)).toString());
        assertEquals("", log.toString());

        // no system-images devices defined in the SDK by default
        assertEquals("[]",
                listDisplayNames(dm2.getDevices(DeviceFilter.SYSTEM_IMAGES)).toString());
        assertEquals("", log.toString());

        // this list comes from devices.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/devices.xml
        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, 3.2\" HVGA slider (ADP1), " +
                        "3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, 3.7\" FWVGA slider, " +
                        "3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), 4.65\" 720p (Galaxy Nexus), " +
                        "4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, 7\" WSVGA (Tablet)]",
                listDisplayNames(dm2.getDevices(DeviceFilter.DEFAULT)).toString());
        assertEquals("", log.toString());

        // this list comes from the nexus.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/nexus.xml
        assertEquals("[Android TV (1080p), Android TV (720p), Android Wear Round, " +
                        "Android Wear Round Chin, Android Wear Square, Galaxy Nexus, " +
                        "Nexus 10, Nexus 4, Nexus 5, Nexus 6, Nexus 7, Nexus 7 (2012), " +
                        "Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm2.getDevices(DeviceFilter.VENDOR)).toString());
        assertEquals("", log.toString());

        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, 3.2\" HVGA slider (ADP1), " +
                        "3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, 3.7\" FWVGA slider, " +
                        "3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), 4.65\" 720p (Galaxy Nexus), " +
                        "4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, 7\" WSVGA (Tablet), Android TV (1080p), "
                        +
                        "Android TV (720p), Android Wear Round, Android Wear Round Chin, " +
                        "Android Wear Square, Galaxy Nexus, My Custom Tablet, Nexus 10, " +
                        "Nexus 4, Nexus 5, Nexus 6, Nexus 7, Nexus 7 (2012), Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm2.getDevices(DeviceManager.ALL_DEVICES)).toString());
        assertEquals("", log.toString());
    }

    public final void testGetDevices_SysImgDevice() throws Exception {
        // this adds a devices.xml with one device
        makeSystemImageFolder(TARGET_DIR_NAME_0, "tag-1", "x86");

        // no user devices defined in the test's custom .android home folder
        assertEquals("[]", listDisplayNames(dm.getDevices(DeviceFilter.USER)).toString());
        assertEquals("", log.toString());

        // find the system-images specific device added by makeSystemImageFolder above
        // using both the getDevices() API and the device-specific getDevice() API.
        assertEquals("[Mock Tag 1 Device Name]",
                listDisplayNames(dm.getDevices(DeviceFilter.SYSTEM_IMAGES)).toString());
        assertEquals("", log.toString());

        assertEquals("Mock Tag 1 Device Name",
                listDisplayName(dm.getDevice("MockDevice-tag-1", "Mock Tag 1 OEM")));

        // this list comes from devices.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/devices.xml
        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, 3.2\" HVGA slider (ADP1), " +
                        "3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, 3.7\" FWVGA slider, " +
                        "3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), 4.65\" 720p (Galaxy Nexus), " +
                        "4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, 7\" WSVGA (Tablet)]",
                listDisplayNames(dm.getDevices(DeviceFilter.DEFAULT)).toString());
        assertEquals("", log.toString());

        // this list comes from the nexus.xml bundled in the JAR
        // cf /sdklib/src/main/java/com/android/sdklib/devices/nexus.xml
        assertEquals(
                "[Android TV (1080p), Android TV (720p), Android Wear Round, Android Wear Round Chin, "
                        +
                        "Android Wear Square, Galaxy Nexus, Nexus 10, Nexus 4, Nexus 5, Nexus 6, Nexus 7, "
                        +
                        "Nexus 7 (2012), Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm.getDevices(DeviceFilter.VENDOR)).toString());
        assertEquals("", log.toString());

        assertEquals(
                "[10.1\" WXGA (Tablet), 2.7\" QVGA, 2.7\" QVGA slider, 3.2\" HVGA slider (ADP1), " +
                        "3.2\" QVGA (ADP2), 3.3\" WQVGA, 3.4\" WQVGA, 3.7\" FWVGA slider, " +
                        "3.7\" WVGA (Nexus One), 4\" WVGA (Nexus S), 4.65\" 720p (Galaxy Nexus), " +
                        "4.7\" WXGA, 5.1\" WVGA, 5.4\" FWVGA, 7\" WSVGA (Tablet), Android TV (1080p), "
                        +
                        "Android TV (720p), Android Wear Round, Android Wear Round Chin, " +
                        "Android Wear Square, Galaxy Nexus, Mock Tag 1 Device Name, Nexus 10, Nexus 4, "
                        +
                        "Nexus 5, Nexus 6, Nexus 7, Nexus 7 (2012), Nexus 9, Nexus One, Nexus S]",
                listDisplayNames(dm.getDevices(DeviceManager.ALL_DEVICES)).toString());
        assertEquals("", log.toString());
    }

    public final void testGetDeviceStatus() {
        // get a definition from the bundled devices.xml file
        assertEquals(DeviceStatus.EXISTS,
                dm.getDeviceStatus("7in WSVGA (Tablet)", "Generic"));

        // get a definition from the bundled oem file
        assertEquals(DeviceStatus.EXISTS,
                dm.getDeviceStatus("Nexus One", "Google"));

        // try a device that does not exist
        assertEquals(DeviceStatus.MISSING,
                dm.getDeviceStatus("My Device", "Custom OEM"));
    }

    public final void testHasHardwarePropHashChanged_Generic() {
        final Device d1 = dm.getDevice("7in WSVGA (Tablet)", "Generic");

        assertEquals("MD5:750a657019b49e621c42ce9a20c2cc30",
                DeviceManager.hasHardwarePropHashChanged(
                        d1,
                        "invalid"));

        assertEquals(null,
                DeviceManager.hasHardwarePropHashChanged(d1, "MD5:750a657019b49e621c42ce9a20c2cc30"));

        // change the device hardware props, this should change the hash
        d1.getDefaultHardware().setNav(Navigation.TRACKBALL);

        assertEquals("MD5:9c4dd5018987da51f7166f139f4361a2",
                     DeviceManager.hasHardwarePropHashChanged(d1, "MD5:750a657019b49e621c42ce9a20c2cc30"));

        // change the property back, should revert its hash to the previous one
        d1.getDefaultHardware().setNav(Navigation.NONAV);

        assertEquals(null,
                DeviceManager.hasHardwarePropHashChanged(d1, "MD5:750a657019b49e621c42ce9a20c2cc30"));
    }

    public final void testHasHardwarePropHashChanged_Oem() {
        final Device d2 = dm.getDevice("Nexus One", "Google");

        assertEquals("MD5:36362a51e6c830c2ab515a312c9ecbff",
                DeviceManager.hasHardwarePropHashChanged(
                        d2,
                        "invalid"));

        assertEquals(null,
                DeviceManager.hasHardwarePropHashChanged(
                        d2,
                        "MD5:36362a51e6c830c2ab515a312c9ecbff"));

        // change the device hardware props, this should change the hash
        d2.getDefaultHardware().setKeyboard(Keyboard.QWERTY);

        assertEquals("MD5:f8f4b390755f2f58dfeb7d3020cd87db",
                DeviceManager.hasHardwarePropHashChanged(
                        d2,
                        "MD5:36362a51e6c830c2ab515a312c9ecbff"));

        // change the property back, should revert its hash to the previous one
        d2.getDefaultHardware().setKeyboard(Keyboard.NOKEY);

        assertEquals(null,
                DeviceManager.hasHardwarePropHashChanged(
                        d2,
                        "MD5:36362a51e6c830c2ab515a312c9ecbff"));
    }

    public final void testDeviceOverrides() throws Exception {
        try {
            File location = getSdkManager().getLocalSdk().getLocation();
            SystemImage imageWithDevice = new SystemImage(
              new File(location, "system-images/android-22/android-wear/x86"),
              ISystemImage.LocationType.IN_SYSTEM_IMAGE,
              new IdDisplay("android-wear", "android-wear"), new IdDisplay("Google", "Google1"),
              "x86", new File[]{});
            DeviceManager manager = DeviceManager.createInstance(location, log);
            int count = manager.getDevices(EnumSet.allOf(DeviceFilter.class)).size();
            Device d = manager.getDevice("wear_round", "Google");
            assertEquals(d.getDisplayName(), "Android Wear Round");

            makeSystemImageFolder(imageWithDevice, "wear_round");
            manager = DeviceManager.createInstance(location, log);

            d = manager.getDevice("wear_round", "Google");
            assertEquals(d.getDisplayName(), "Mock Android wear Device Name");

            Device d1 = dm.getDevice("wear_round", "Google");

            Builder b = new Device.Builder(d1);
            b.setName("Custom");

            Device d2 = b.build();

            manager.addUserDevice(d2);
            manager.saveUserDevices();

            d = manager.getDevice("wear_round", "Google");
            assertEquals(d.getDisplayName(), "Custom");
            assertEquals(count, manager.getDevices(EnumSet.allOf(DeviceFilter.class)).size());
        }
        finally {
            super.setUp();
        }
    }
}
