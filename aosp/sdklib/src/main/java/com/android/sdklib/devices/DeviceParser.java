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

import static com.android.SdkConstants.VALUE_FALSE;
import static com.android.SdkConstants.VALUE_TRUE;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.dvlib.DeviceSchema;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.NavigationState;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenRound;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;
import com.android.resources.UiMode;
import com.google.common.base.Splitter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

public class DeviceParser {

    private static class DeviceHandler extends DefaultHandler {
        private static final Splitter sSpaceSplitter = Splitter.on(' ').omitEmptyStrings();
        private static final String ROUND_BOOT_PROP = "ro.emulator.circular";
        private static final String CHIN_BOOT_PROP = "ro.emu.win_outset_bottom_px";

        private final Table<String, String, Device> mDevices = HashBasedTable.create();
        private final StringBuilder mStringAccumulator = new StringBuilder();
        private final File mParentFolder;
        private Meta mMeta;
        private Hardware mHardware;
        private Software mSoftware;
        private State mState;
        private Device.Builder mBuilder;
        private Camera mCamera;
        private Storage.Unit mUnit;
        private String[] mBootProp;

        public DeviceHandler(@Nullable File parentFolder) {
            mParentFolder = parentFolder;
        }

        @NonNull
        public Table<String, String, Device> getDevices() {
            return mDevices;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {

            if (DeviceSchema.NODE_DEVICE.equals(localName)) {
                // Reset everything
                mMeta = null;
                mHardware = null;
                mSoftware = null;
                mState = null;
                mCamera = null;
                mBuilder = new Device.Builder();
            } else if (DeviceSchema.NODE_META.equals(localName)) {
                mMeta = new Meta();
            } else if (DeviceSchema.NODE_HARDWARE.equals(localName)) {
                mHardware = new Hardware();
            } else if (DeviceSchema.NODE_SOFTWARE.equals(localName)) {
                mSoftware = new Software();
            } else if (DeviceSchema.NODE_STATE.equals(localName)) {
                mState = new State();
                // mState can embed a Hardware instance
                mHardware = mHardware.deepCopy();
                String defaultState = attributes.getValue(DeviceSchema.ATTR_DEFAULT);
                if ("true".equals(defaultState) || "1".equals(defaultState)) {
                    mState.setDefaultState(true);
                }
                mState.setName(attributes.getValue(DeviceSchema.ATTR_NAME).trim());
            } else if (DeviceSchema.NODE_CAMERA.equals(localName)) {
                mCamera = new Camera();
            } else if (DeviceSchema.NODE_RAM.equals(localName)
                    || DeviceSchema.NODE_INTERNAL_STORAGE.equals(localName)
                    || DeviceSchema.NODE_REMOVABLE_STORAGE.equals(localName)) {
                mUnit = Storage.Unit.getEnum(attributes.getValue(DeviceSchema.ATTR_UNIT));
            } else if (DeviceSchema.NODE_FRAME.equals(localName)) {
                mMeta.setFrameOffsetLandscape(new Point());
                mMeta.setFrameOffsetPortrait(new Point());
            } else if (DeviceSchema.NODE_SCREEN.equals(localName)) {
                mHardware.setScreen(new Screen());
            } else if (DeviceSchema.NODE_BOOT_PROP.equals(localName)) {
                mBootProp = new String[2];
            }
            mStringAccumulator.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            mStringAccumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (DeviceSchema.NODE_DEVICE.equals(localName)) {
                Device device = mBuilder.build();
                mDevices.put(device.getId(), device.getManufacturer(), device);
            } else if (DeviceSchema.NODE_NAME.equals(localName)) {
                mBuilder.setName(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_ID.equals(localName)) {
                mBuilder.setId(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_MANUFACTURER.equals(localName)) {
                mBuilder.setManufacturer(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_META.equals(localName)) {
                mBuilder.setMeta(mMeta);
            } else if (DeviceSchema.NODE_SOFTWARE.equals(localName)) {
                mBuilder.addSoftware(mSoftware);
            } else if (DeviceSchema.NODE_STATE.equals(localName)) {
                mState.setHardware(mHardware);
                mBuilder.addState(mState);
            } else if (DeviceSchema.NODE_SIXTY_FOUR.equals(localName)) {
                mMeta.setIconSixtyFour(new File(mParentFolder, getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_SIXTEEN.equals(localName)) {
                mMeta.setIconSixteen(new File(mParentFolder, getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_PATH.equals(localName)) {
                mMeta.setFrame(new File(mParentFolder, mStringAccumulator.toString().trim()));
            } else if (DeviceSchema.NODE_PORTRAIT_X_OFFSET.equals(localName)) {
                mMeta.getFrameOffsetPortrait().x = getInteger(mStringAccumulator);
            } else if (DeviceSchema.NODE_PORTRAIT_Y_OFFSET.equals(localName)) {
                mMeta.getFrameOffsetPortrait().y = getInteger(mStringAccumulator);
            } else if (DeviceSchema.NODE_LANDSCAPE_X_OFFSET.equals(localName)) {
                mMeta.getFrameOffsetLandscape().x = getInteger(mStringAccumulator);
            } else if (DeviceSchema.NODE_LANDSCAPE_Y_OFFSET.equals(localName)) {
                mMeta.getFrameOffsetLandscape().y = getInteger(mStringAccumulator);
            } else if (DeviceSchema.NODE_SCREEN_SIZE.equals(localName)) {
                mHardware.getScreen().setSize(ScreenSize.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_DIAGONAL_LENGTH.equals(localName)) {
                mHardware.getScreen().setDiagonalLength(getDouble(mStringAccumulator));
            } else if (DeviceSchema.NODE_PIXEL_DENSITY.equals(localName)) {
                mHardware.getScreen().setPixelDensity(
                        Density.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_SCREEN_RATIO.equals(localName)) {
                mHardware.getScreen().setRatio(
                    ScreenRatio.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_X_DIMENSION.equals(localName)) {
                mHardware.getScreen().setXDimension(getInteger(mStringAccumulator));
            } else if (DeviceSchema.NODE_Y_DIMENSION.equals(localName)) {
                mHardware.getScreen().setYDimension(getInteger(mStringAccumulator));
            } else if (DeviceSchema.NODE_XDPI.equals(localName)) {
                mHardware.getScreen().setXdpi(getDouble(mStringAccumulator));
            } else if (DeviceSchema.NODE_YDPI.equals(localName)) {
                mHardware.getScreen().setYdpi(getDouble(mStringAccumulator));
            } else if (DeviceSchema.NODE_MULTITOUCH.equals(localName)) {
                mHardware.getScreen().setMultitouch(
                        Multitouch.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_MECHANISM.equals(localName)) {
                mHardware.getScreen().setMechanism(
                        TouchScreen.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_SCREEN_TYPE.equals(localName)) {
                mHardware.getScreen().setScreenType(
                        ScreenType.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_NETWORKING.equals(localName)) {
                for (String n : getStringList(mStringAccumulator)) {
                    Network net = Network.getEnum(n);
                    if (net != null) {
                        mHardware.addNetwork(net);
                    }
                }
            } else if (DeviceSchema.NODE_SENSORS.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    Sensor sens = Sensor.getEnum(s);
                    if (sens != null) {
                        mHardware.addSensor(sens);
                    }
                }
            } else if (DeviceSchema.NODE_MIC.equals(localName)) {
                mHardware.setHasMic(getBool(mStringAccumulator));
            } else if (DeviceSchema.NODE_CAMERA.equals(localName)) {
                mHardware.addCamera(mCamera);
                mCamera = null;
            } else if (DeviceSchema.NODE_LOCATION.equals(localName)) {
                CameraLocation location = CameraLocation.getEnum(getString(mStringAccumulator));
                if (location != null) {
                    mCamera.setLocation(location);
                }
            } else if (DeviceSchema.NODE_AUTOFOCUS.equals(localName)) {
                mCamera.setFlash(getBool(mStringAccumulator));
            } else if (DeviceSchema.NODE_FLASH.equals(localName)) {
                mCamera.setFlash(getBool(mStringAccumulator));
            } else if (DeviceSchema.NODE_KEYBOARD.equals(localName)) {
                mHardware.setKeyboard(Keyboard.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_NAV.equals(localName)) {
                mHardware.setNav(Navigation.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_RAM.equals(localName)) {
                int val = getInteger(mStringAccumulator);
                mHardware.setRam(new Storage(val, mUnit));
            } else if (DeviceSchema.NODE_BUTTONS.equals(localName)) {
                ButtonType buttonType = ButtonType.getEnum(getString(mStringAccumulator));
                if (buttonType != null) {
                    mHardware.setButtonType(buttonType);
                }
            } else if (DeviceSchema.NODE_INTERNAL_STORAGE.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    int val = Integer.parseInt(s);
                    mHardware.addInternalStorage(new Storage(val, mUnit));
                }
            } else if (DeviceSchema.NODE_REMOVABLE_STORAGE.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    if (s != null && !s.isEmpty()) {
                        int val = Integer.parseInt(s);
                        mHardware.addRemovableStorage(new Storage(val, mUnit));
                    }
                }
            } else if (DeviceSchema.NODE_CPU.equals(localName)) {
                mHardware.setCpu(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_GPU.equals(localName)) {
                mHardware.setGpu(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_ABI.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    Abi abi = Abi.getEnum(s);
                    if (abi != null) {
                        mHardware.addSupportedAbi(abi);
                    }
                }
            } else if (DeviceSchema.NODE_DOCK.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    UiMode d = UiMode.getEnum(s);
                    if (d != null) {
                        mHardware.addSupportedUiMode(d);
                    }
                }
            } else if (DeviceSchema.NODE_POWER_TYPE.equals(localName)) {
                PowerType type = PowerType.getEnum(getString(mStringAccumulator));
                if (type != null) {
                    mHardware.setChargeType(type);
                }
            } else if (DeviceSchema.NODE_API_LEVEL.equals(localName)) {
                String val = getString(mStringAccumulator);
                // Can be one of 5 forms:
                // 1
                // 1-2
                // 1-
                // -2
                // -
                int index;
                if (val.charAt(0) == '-') {
                    if (val.length() == 1) { // -
                        mSoftware.setMinSdkLevel(0);
                        mSoftware.setMaxSdkLevel(Integer.MAX_VALUE);
                    } else { // -2
                        // Remove the front dash and any whitespace between it
                        // and the upper bound.
                        val = val.substring(1).trim();
                        mSoftware.setMinSdkLevel(0);
                        mSoftware.setMaxSdkLevel(Integer.parseInt(val));
                    }
                } else if ((index = val.indexOf('-')) > 0) {
                    if (index == val.length() - 1) { // 1-
                        // Strip the last dash and any whitespace between it and
                        // the lower bound.
                        val = val.substring(0, val.length() - 1).trim();
                        mSoftware.setMinSdkLevel(Integer.parseInt(val));
                        mSoftware.setMaxSdkLevel(Integer.MAX_VALUE);
                    } else { // 1-2
                        String min = val.substring(0, index).trim();
                        String max = val.substring(index + 1);
                        mSoftware.setMinSdkLevel(Integer.parseInt(min));
                        mSoftware.setMaxSdkLevel(Integer.parseInt(max));
                    }
                } else { // 1
                    int apiLevel = Integer.parseInt(val);
                    mSoftware.setMinSdkLevel(apiLevel);
                    mSoftware.setMaxSdkLevel(apiLevel);
                }
            } else if (DeviceSchema.NODE_LIVE_WALLPAPER_SUPPORT.equals(localName)) {
                mSoftware.setLiveWallpaperSupport(getBool(mStringAccumulator));
            } else if (DeviceSchema.NODE_BLUETOOTH_PROFILES.equals(localName)) {
                for (String s : getStringList(mStringAccumulator)) {
                    BluetoothProfile profile = BluetoothProfile.getEnum(s);
                    if (profile != null) {
                        mSoftware.addBluetoothProfile(profile);
                    }
                }
            } else if (DeviceSchema.NODE_GL_VERSION.equals(localName)) {
                // Guaranteed to be in the form [\d]\.[\d]
                mSoftware.setGlVersion(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_GL_EXTENSIONS.equals(localName)) {
                mSoftware.addAllGlExtensions(getStringList(mStringAccumulator));
            } else if (DeviceSchema.NODE_DESCRIPTION.equals(localName)) {
                mState.setDescription(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_SCREEN_ORIENTATION.equals(localName)) {
                mState.setOrientation(ScreenOrientation.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_KEYBOARD_STATE.equals(localName)) {
                mState.setKeyState(KeyboardState.getEnum(getString(mStringAccumulator)));
            } else if (DeviceSchema.NODE_NAV_STATE.equals(localName)) {
                // We have an extra state in our XML for nonav that
                // NavigationState doesn't contain
                String navState = getString(mStringAccumulator);
                if (navState.equals("nonav")) {
                    mState.setNavState(NavigationState.HIDDEN);
                } else {
                    mState.setNavState(NavigationState.getEnum(getString(mStringAccumulator)));
                }
            } else if (DeviceSchema.NODE_STATUS_BAR.equals(localName)) {
                mSoftware.setStatusBar(getBool(mStringAccumulator));

            } else if (DeviceSchema.NODE_TAG_ID.equals(localName)) {
                mBuilder.setTagId(getString(mStringAccumulator));
            } else if (DeviceSchema.NODE_PROP_NAME.equals(localName)) {
                assert mBootProp != null && mBootProp.length == 2;
                mBootProp[0] = getString(mStringAccumulator);
            } else if (DeviceSchema.NODE_PROP_VALUE.equals(localName)) {
                assert mBootProp != null && mBootProp.length == 2;
                mBootProp[1] = mStringAccumulator.toString();
            } else if (DeviceSchema.NODE_BOOT_PROP.equals(localName)) {
                assert mBootProp != null && mBootProp.length == 2 &&
                       mBootProp[0] != null && mBootProp[1] != null;
                mBuilder.addBootProp(mBootProp[0], mBootProp[1]);
                checkAndSetIfRound(mBootProp[0], mBootProp[1]);
                mBootProp = null;
            } else if (DeviceSchema.NODE_SKIN.equals(localName)) {
                String path = getString(mStringAccumulator).replace('/', File.separatorChar);
                mHardware.setSkinFile(new File(path));
            }
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            throw e;
        }

        private void checkAndSetIfRound(String bootPropKey, String bootPropValue) {
            // This is a ugly hack. To keep the existing devices.xmls working, the roundness of the
            // screen is stored in a boot property.
            ScreenRound roundness = null;
            if (ROUND_BOOT_PROP.equals(bootPropKey)) {
                if (VALUE_TRUE.equals(bootPropValue)) {
                    roundness = ScreenRound.ROUND;
                } else if (VALUE_FALSE.equals(bootPropValue)) {
                    roundness = ScreenRound.NOTROUND;
                }
                for (State state : mBuilder.getAllStates()) {
                    state.getHardware().getScreen().setScreenRound(roundness);
                }
            }
            if (CHIN_BOOT_PROP.equals(bootPropKey)) {
                int chin = Integer.parseInt(bootPropValue);
                for (State state : mBuilder.getAllStates()) {
                    state.getHardware().getScreen().setChin(chin);
                }
            }
        }

        private static List<String> getStringList(StringBuilder stringAccumulator) {
            List<String> filteredStrings = new ArrayList<String>();
            for (String s : sSpaceSplitter.split(stringAccumulator)) {
                if (s != null && !s.isEmpty()) {
                    filteredStrings.add(s.trim());
                }
            }
            return filteredStrings;
        }

        private static Boolean getBool(StringBuilder s) {
            return equals(s, "true") || equals(s, "1");
        }

        private static double getDouble(StringBuilder stringAccumulator) {
            return Double.parseDouble(getString(stringAccumulator));
        }

        private static String getString(StringBuilder s) {
            return s.toString().trim();
        }

        private static boolean equals(StringBuilder s, String t) {
            int start = 0;
            int length = s.length();
            while (start < length && Character.isWhitespace(s.charAt(start))) {
                start++;
            }
            if (start == length) {
                return t.isEmpty();
            }

            int end = length;
            while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
                end--;
            }

            if (t.length() != (end - start)) {
                return false;
            }

            for (int i = 0, n = t.length(), j = start; i < n; i++, j++) {
                if (Character.toLowerCase(s.charAt(j)) != Character.toLowerCase(t.charAt(i))) {
                    return false;
                }
            }

            return true;
        }

        private static int getInteger(StringBuilder stringAccumulator) {
            return Integer.parseInt(getString(stringAccumulator));
        }
    }

    private static final SAXParserFactory sParserFactory;

    static {
        sParserFactory = SAXParserFactory.newInstance();
        sParserFactory.setNamespaceAware(true);
    }

    @NonNull
    public static Table<String, String, Device> parse(@NonNull File devicesFile)
            throws SAXException, ParserConfigurationException, IOException {
        // stream closed by parseImpl.
        @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
        InputStream stream = new FileInputStream(devicesFile);
        return parseImpl(stream, devicesFile.getAbsoluteFile().getParentFile());
    }

    /**
     * This method closes the stream.
     */
    @NonNull
    public static Table<String, String, Device> parse(@NonNull InputStream devices)
            throws SAXException, IOException, ParserConfigurationException {
        return parseImpl(devices, null);
    }

    /**
     * After parsing, this method closes the stream.
     */
    @NonNull
    private static Table<String, String, Device> parseImpl(@NonNull InputStream devices, @Nullable File parentDir)
            throws SAXException, IOException, ParserConfigurationException {
        try {
            if (!devices.markSupported()) {
                //noinspection IOResourceOpenedButNotSafelyClosed
                devices = new BufferedInputStream(devices);  // closed in the finally block.
            }
            devices.mark(500000);
            int version = DeviceSchema.getXmlSchemaVersion(devices);
            SAXParser parser = getParser(version);
            DeviceHandler dHandler = new DeviceHandler(parentDir);
            devices.reset();
            parser.parse(devices, dHandler);
            return dHandler.getDevices();
        }
        finally {
            // It's better to close the stream here since we may have created it above.
            devices.close();
        }
    }

    @NonNull
    private static SAXParser getParser(int version) throws ParserConfigurationException, SAXException {
        Schema schema = DeviceSchema.getSchema(version);
        if (schema != null) {
            sParserFactory.setSchema(schema);
        }
        return sParserFactory.newSAXParser();
    }
}
