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

import com.android.dvlib.DeviceSchema;
import com.android.resources.UiMode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Point;
import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DeviceWriter {

    public static final String LOCAL_NS = "d";
    public static final String PREFIX = LOCAL_NS + ":";

    private DeviceWriter() {
    }

    /**
     * Writes the XML definition of the given {@link Collection} of {@link Device}s according to
     * {@link DeviceSchema#NS_DEVICES_URI} to the {@link OutputStream}.
     * Note that it is up to the caller to close the {@link OutputStream}.
     * @param out The {@link OutputStream} to write the resulting XML to.
     * @param devices The {@link Device}s from which to generate the XML.
     * @throws ParserConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void writeToXml(OutputStream out, Collection<Device> devices) throws
            ParserConfigurationException,
            TransformerFactoryConfigurationError,
            TransformerException {

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement(PREFIX + DeviceSchema.NODE_DEVICES);
        root.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":xsi",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        root.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + LOCAL_NS, DeviceSchema.NS_DEVICES_URI);
        doc.appendChild(root);

        for (Device device : devices) {
            Element deviceNode = doc.createElement(PREFIX + DeviceSchema.NODE_DEVICE);
            root.appendChild(deviceNode);

            Element name = doc.createElement(PREFIX + DeviceSchema.NODE_NAME);
            String displayName = device.getDisplayName();
            name.appendChild(doc.createTextNode(displayName));
            deviceNode.appendChild(name);

            String deviceId = device.getId();
            if (!deviceId.equals(displayName)) {
                Element id = doc.createElement(PREFIX + DeviceSchema.NODE_ID);
                id.appendChild(doc.createTextNode(deviceId));
                deviceNode.appendChild(id);
            }

            Element manufacturer = doc.createElement(PREFIX + DeviceSchema.NODE_MANUFACTURER);
            manufacturer.appendChild(doc.createTextNode(device.getManufacturer()));
            deviceNode.appendChild(manufacturer);

            deviceNode.appendChild(generateMetaNode(device.getMeta(), doc));
            deviceNode.appendChild(generateHardwareNode(device.getDefaultHardware(), doc));
            for (Software sw : device.getAllSoftware()) {
                deviceNode.appendChild(generateSoftwareNode(sw, doc));
            }
            for (State s : device.getAllStates()) {
                deviceNode.appendChild(generateStateNode(s, doc, device.getDefaultHardware()));
            }

            String tagId = device.getTagId();
            if (tagId != null) {
                Element e = doc.createElement(PREFIX + DeviceSchema.NODE_TAG_ID);
                e.appendChild(doc.createTextNode(tagId));
                deviceNode.appendChild(e);
            }

            Map<String, String> bootProps = device.getBootProps();
            if (bootProps != null && !bootProps.isEmpty()) {
                Element props = doc.createElement(PREFIX + DeviceSchema.NODE_BOOT_PROPS);
                for (Map.Entry<String, String> bootProp : bootProps.entrySet()) {
                    Element prop = doc.createElement(PREFIX + DeviceSchema.NODE_BOOT_PROP);
                    Element propName = doc.createElement(PREFIX + DeviceSchema.NODE_PROP_NAME);
                    propName.appendChild(doc.createTextNode(bootProp.getKey()));
                    Element propValue = doc.createElement(PREFIX + DeviceSchema.NODE_PROP_VALUE);
                    propValue.appendChild(doc.createTextNode(bootProp.getValue()));
                    prop.appendChild(propName);
                    prop.appendChild(propValue);
                    props.appendChild(prop);
                }
                deviceNode.appendChild(props);
            }
        }

        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        tf.transform(source, result);
    }

    /* This returns the XML Element for the given instance of Meta */
    private static Node generateMetaNode(Meta meta, Document doc) {
        Element m = doc.createElement(PREFIX + DeviceSchema.NODE_META);
        if (meta.hasIconSixtyFour() || meta.hasIconSixteen()) {
            Element icons = doc.createElement(PREFIX + DeviceSchema.NODE_ICONS);
            m.appendChild(icons);
            if (meta.hasIconSixtyFour()) {
                addElement(doc, icons, DeviceSchema.NODE_SIXTY_FOUR,
                        meta.getIconSixtyFour().getPath());
            }
            if (meta.hasIconSixteen()) {
                addElement(doc, icons, DeviceSchema.NODE_SIXTEEN, meta.getIconSixteen().getPath());
            }
        }

        if (meta.hasFrame()) {
            Element frame = doc.createElement(PREFIX + DeviceSchema.NODE_FRAME);
            addElement(doc, frame, DeviceSchema.NODE_PATH, meta.getFrame().getPath());
            Point offset = meta.getFrameOffsetPortrait();
            addElement(doc, frame, DeviceSchema.NODE_PORTRAIT_X_OFFSET, Integer.toString(offset.x));
            addElement(doc, frame, DeviceSchema.NODE_PORTRAIT_Y_OFFSET, Integer.toString(offset.y));
            offset = meta.getFrameOffsetLandscape();
            addElement(doc, frame, DeviceSchema.NODE_LANDSCAPE_X_OFFSET,
                    Integer.toString(offset.x));
            addElement(doc, frame, DeviceSchema.NODE_LANDSCAPE_Y_OFFSET,
                    Integer.toString(offset.y));
        }

        return m;
    }

    /* This returns the XML Element for the given instance of Hardware */
    private static Element generateHardwareNode(Hardware hw, Document doc) {
        Screen s = hw.getScreen();
        Element hardware = doc.createElement(PREFIX + DeviceSchema.NODE_HARDWARE);
        Element screen = doc.createElement(PREFIX + DeviceSchema.NODE_SCREEN);
        hardware.appendChild(screen);

        addElement(doc, screen, DeviceSchema.NODE_SCREEN_SIZE, s.getSize().getResourceValue());
        addElement(doc, screen, DeviceSchema.NODE_DIAGONAL_LENGTH,
                String.format(Locale.US, "%.2f",s.getDiagonalLength()));
        addElement(doc, screen, DeviceSchema.NODE_PIXEL_DENSITY,
                s.getPixelDensity().getResourceValue());
        addElement(doc, screen, DeviceSchema.NODE_SCREEN_RATIO, s.getRatio().getResourceValue());

        Element dimensions = doc.createElement(PREFIX + DeviceSchema.NODE_DIMENSIONS);
        screen.appendChild(dimensions);

        addElement(doc, dimensions, DeviceSchema.NODE_X_DIMENSION,
                Integer.toString(s.getXDimension()));
        addElement(doc, dimensions, DeviceSchema.NODE_Y_DIMENSION,
                Integer.toString(s.getYDimension()));
        addElement(doc, screen, DeviceSchema.NODE_XDPI, String.format(Locale.US,
                "%.2f", s.getXdpi()));
        addElement(doc, screen, DeviceSchema.NODE_YDPI, String.format(Locale.US,
                "%.2f", s.getYdpi()));

        Element touch = doc.createElement(PREFIX + DeviceSchema.NODE_TOUCH);
        screen.appendChild(touch);

        addElement(doc, touch, DeviceSchema.NODE_MULTITOUCH, s.getMultitouch().toString());
        addElement(doc, touch, DeviceSchema.NODE_MECHANISM, s.getMechanism().getResourceValue());
        addElement(doc, touch, DeviceSchema.NODE_SCREEN_TYPE, s.getScreenType().toString());

        addElement(doc, hardware, DeviceSchema.NODE_NETWORKING, hw.getNetworking());
        addElement(doc, hardware, DeviceSchema.NODE_SENSORS, hw.getSensors());
        addElement(doc, hardware, DeviceSchema.NODE_MIC, Boolean.toString(hw.hasMic()));

        for (Camera c : hw.getCameras()) {
            Element camera  = doc.createElement(PREFIX + DeviceSchema.NODE_CAMERA);
            hardware.appendChild(camera);
            addElement(doc, camera, DeviceSchema.NODE_LOCATION, c.getLocation().toString());
            addElement(doc, camera, DeviceSchema.NODE_AUTOFOCUS,
                    Boolean.toString(c.hasAutofocus()));
            addElement(doc, camera, DeviceSchema.NODE_FLASH, Boolean.toString(c.hasFlash()));
        }

        addElement(doc, hardware, DeviceSchema.NODE_KEYBOARD, hw.getKeyboard().getResourceValue());
        addElement(doc, hardware, DeviceSchema.NODE_NAV, hw.getNav().getResourceValue());

        Storage.Unit unit = hw.getRam().getAppropriateUnits();
        Element ram = addElement(doc, hardware, DeviceSchema.NODE_RAM,
                Long.toString(hw.getRam().getSizeAsUnit(unit)));
        ram.setAttribute(DeviceSchema.ATTR_UNIT, unit.toString());

        addElement(doc, hardware, DeviceSchema.NODE_BUTTONS, hw.getButtonType().toString());
        addStorageElement(doc, hardware, DeviceSchema.NODE_INTERNAL_STORAGE,
                hw.getInternalStorage());
        addStorageElement(doc, hardware, DeviceSchema.NODE_REMOVABLE_STORAGE,
                hw.getRemovableStorage());
        addElement(doc, hardware, DeviceSchema.NODE_CPU, hw.getCpu());
        addElement(doc, hardware, DeviceSchema.NODE_GPU, hw.getGpu());
        addElement(doc, hardware, DeviceSchema.NODE_ABI, hw.getSupportedAbis());

        StringBuilder sb = new StringBuilder();
        for (UiMode u : hw.getSupportedUiModes()) {
            sb.append('\n').append(u.getResourceValue());
        }
        addElement(doc, hardware, DeviceSchema.NODE_DOCK, sb.toString());

        addElement(doc, hardware, DeviceSchema.NODE_POWER_TYPE, hw.getChargeType().toString());

        File skinPath = hw.getSkinFile();
        if (skinPath != null) {
            String canonicalPath = skinPath.getPath().replace(File.separatorChar, '/');
            addElement(doc, hardware, DeviceSchema.NODE_SKIN, canonicalPath);
        }

        return hardware;
    }

    /* This returns the XML Element for the given instance of Software */
    private static Element generateSoftwareNode(Software sw, Document doc) {
        Element software = doc.createElement(PREFIX + DeviceSchema.NODE_SOFTWARE);

        String apiVersion = "";
        if (sw.getMinSdkLevel() != 0) {
            apiVersion += Integer.toString(sw.getMinSdkLevel());
        }
        apiVersion += "-";
        if (sw.getMaxSdkLevel() != Integer.MAX_VALUE) {
            apiVersion += Integer.toString(sw.getMaxSdkLevel());
        }
        addElement(doc, software, DeviceSchema.NODE_API_LEVEL, apiVersion);
        addElement(doc, software, DeviceSchema.NODE_LIVE_WALLPAPER_SUPPORT,
                Boolean.toString(sw.hasLiveWallpaperSupport()));
        addElement(doc, software, DeviceSchema.NODE_BLUETOOTH_PROFILES, sw.getBluetoothProfiles());
        addElement(doc, software, DeviceSchema.NODE_GL_VERSION, sw.getGlVersion());
        addElement(doc, software, DeviceSchema.NODE_GL_EXTENSIONS, sw.getGlExtensions());
        addElement(doc, software, DeviceSchema.NODE_STATUS_BAR,
                Boolean.toString(sw.hasStatusBar()));

        return software;
    }

    /* This returns the XML Element for the given instance of State */
    private static Element generateStateNode(State s, Document doc, Hardware defaultHardware) {
        Element state = doc.createElement(PREFIX + DeviceSchema.NODE_STATE);
        state.setAttribute(DeviceSchema.ATTR_NAME, s.getName());
        if (s.isDefaultState()) {
            state.setAttribute(DeviceSchema.ATTR_DEFAULT, Boolean.toString(s.isDefaultState()));
        }
        addElement(doc, state, DeviceSchema.NODE_DESCRIPTION, s.getDescription());
        addElement(doc, state, DeviceSchema.NODE_SCREEN_ORIENTATION,
                s.getOrientation().getResourceValue());
        addElement(doc, state, DeviceSchema.NODE_KEYBOARD_STATE,
                s.getKeyState().getResourceValue());
        addElement(doc, state, DeviceSchema.NODE_NAV_STATE, s.getNavState().getResourceValue());

        // Only if the hardware is different do we want to append hardware values
        if (!s.getHardware().equals(defaultHardware)) {
            // TODO: Only append nodes which are different from the default hardware
            Element hardware = generateHardwareNode(s.getHardware(), doc);
            NodeList children = hardware.getChildNodes();
            for (int i = 0 ; i < children.getLength(); i++) {
                Node child = children.item(i);
                state.appendChild(child);
            }
        }
        return state;
    }

    private static Element addElement(Document doc, Element parent, String tag, String content) {
        Element child = doc.createElement(PREFIX + tag);
        child.appendChild(doc.createTextNode(content));
        parent.appendChild(child);
        return child;
    }

    private static Element addElement(Document doc, Element parent, String tag,
            Collection<?> content) {
        StringBuilder sb = new StringBuilder();
        for (Object o : content) {
            sb.append('\n').append(o.toString());
        }
        return addElement(doc, parent,  tag, sb.toString());
    }

    /* This adds generates the XML for a Collection<Storage> and appends it to the parent. Note
     * that it picks the proper unit for the unit attribute and sets it on the node.
     */
    private static Element addStorageElement(Document doc, Element parent, String tag,
            Collection<Storage> content) {
        Storage.Unit unit = Storage.Unit.TiB;

        // Get the lowest common unit (so if one piece of storage is 128KiB and another is 1MiB,
        // use KiB for units)
        for (Storage storage : content) {
            if (storage.getAppropriateUnits().getNumberOfBytes() < unit.getNumberOfBytes()) {
                unit = storage.getAppropriateUnits();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Storage storage : content) {
            sb.append('\n').append(storage.getSizeAsUnit(unit));
        }
        Element storage = addElement(doc, parent, tag, sb.toString());
        storage.setAttribute(DeviceSchema.ATTR_UNIT, unit.toString());
        return storage;
    }

}
