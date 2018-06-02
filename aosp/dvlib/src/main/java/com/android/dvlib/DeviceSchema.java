/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dvlib;

import com.android.annotations.Nullable;
import com.android.io.NonClosingInputStream;
import com.android.io.NonClosingInputStream.CloseBehavior;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class DeviceSchema {

    // ---- XSD ----

    /**
     * The latest version of the device XML Schema.
     * Valid version numbers are between 1 and this number, included.
     */
    public static final int NS_LATEST_VERSION = 2;

    /** The XML namespace of the latest device XML. */
    public static final String NS_DEVICES_URI = getSchemaUri(NS_LATEST_VERSION);

    /** Base for the devices XSD URI, without the terminal version number. */
    private static final String NS_DEVICES_URI_BASE = "http://schemas.android.com/sdk/devices/";

    /** Regex pattern to find the terminal version number in an XSD URI. */
    static final String NS_DEVICES_URI_PATTERN = NS_DEVICES_URI_BASE + "([0-9]+)";   //$NON-NLS-1$

    // ----- XML ----

    /**
     * The "devices" element is the root element of this schema.
     *
     * It must contain one or more "device" elements that each define the
     * hardware, software, and states for a given device.
     */
    public static final String NODE_DEVICES = "devices";

    /**
     * A "device" element contains a "hardware" element, a "software" element
     * for each API version it supports, and a "state" element for each possible
     * state the device could be in.
     */
    public static final String NODE_DEVICE = "device";

    /**
     * The "hardware" element contains all of the hardware information for a
     * given device.
     */
    public static final String NODE_HARDWARE = "hardware";

    /**
     * The "software" element contains all of the software information for an
     * API version of the given device.
     */
    public static final String NODE_SOFTWARE = "software";

    /**
     * The "state" element contains all of the parameters for a given state of
     * the device. It's also capable of redefining hardware configurations if
     * they change based on state.
     */
    public static final String NODE_STATE = "state";

    public static final String NODE_KEYBOARD = "keyboard";

    public static final String NODE_TOUCH = "touch";

    public static final String NODE_GL_EXTENSIONS = "gl-extensions";

    public static final String NODE_GL_VERSION = "gl-version";

    public static final String NODE_NETWORKING = "networking";

    public static final String NODE_REMOVABLE_STORAGE = "removable-storage";

    public static final String NODE_FLASH = "flash";

    public static final String NODE_LIVE_WALLPAPER_SUPPORT = "live-wallpaper-support";

    public static final String NODE_STATUS_BAR = "status-bar";

    public static final String NODE_BUTTONS = "buttons";

    public static final String NODE_CAMERA = "camera";

    public static final String NODE_LOCATION = "location";

    public static final String NODE_GPU = "gpu";

    public static final String NODE_DOCK = "dock";

    public static final String NODE_YDPI = "ydpi";

    public static final String NODE_POWER_TYPE= "power-type";

    public static final String NODE_Y_DIMENSION = "y-dimension";

    public static final String NODE_SCREEN_RATIO = "screen-ratio";

    public static final String NODE_NAV_STATE = "nav-state";

    public static final String NODE_MIC = "mic";

    public static final String NODE_RAM = "ram";

    public static final String NODE_XDPI = "xdpi";

    public static final String NODE_DIMENSIONS = "dimensions";

    public static final String NODE_ABI = "abi";

    public static final String NODE_MECHANISM = "mechanism";

    public static final String NODE_MULTITOUCH = "multitouch";

    public static final String NODE_NAV = "nav";

    public static final String NODE_PIXEL_DENSITY = "pixel-density";

    public static final String NODE_SCREEN_ORIENTATION = "screen-orientation";

    public static final String NODE_AUTOFOCUS = "autofocus";

    public static final String NODE_SCREEN_SIZE = "screen-size";

    public static final String NODE_DESCRIPTION = "description";

    public static final String NODE_BLUETOOTH_PROFILES = "bluetooth-profiles";

    public static final String NODE_SCREEN = "screen";

    public static final String NODE_SENSORS = "sensors";

    public static final String NODE_DIAGONAL_LENGTH = "diagonal-length";

    public static final String NODE_SCREEN_TYPE = "screen-type";

    public static final String NODE_KEYBOARD_STATE = "keyboard-state";

    public static final String NODE_X_DIMENSION = "x-dimension";

    public static final String NODE_CPU = "cpu";

    public static final String NODE_INTERNAL_STORAGE = "internal-storage";

    public static final String NODE_META = "meta";

    public static final String NODE_ICONS = "icons";

    public static final String NODE_SIXTY_FOUR = "sixty-four";

    public static final String NODE_SIXTEEN = "sixteen";

    public static final String NODE_FRAME = "frame";

    public static final String NODE_PATH = "path";

    public static final String NODE_PORTRAIT_X_OFFSET = "portrait-x-offset";

    public static final String NODE_PORTRAIT_Y_OFFSET = "portrait-y-offset";

    public static final String NODE_LANDSCAPE_X_OFFSET = "landscape-x-offset";

    public static final String NODE_LANDSCAPE_Y_OFFSET = "landscape-y-offset";

    public static final String NODE_NAME = "name";

    public static final String NODE_ID = "id";

    public static final String NODE_API_LEVEL = "api-level";

    public static final String NODE_MANUFACTURER = "manufacturer";

    public static final String NODE_TAG_ID = "tag-id";

    public static final String NODE_BOOT_PROPS = "boot-props";

    public static final String NODE_BOOT_PROP = "boot-prop";

    public static final String NODE_PROP_NAME = "prop-name";

    public static final String NODE_PROP_VALUE = "prop-value";

    public static final String NODE_SKIN = "skin";

    public static final String ATTR_DEFAULT = "default";

    public static final String ATTR_UNIT = "unit";

    public static final String ATTR_NAME = "name";

    /**
     * Returns the URI of the SDK Repository schema for the given version number.
     * @param version Between 1 and {@link #NS_LATEST_VERSION} included.
     */
    public static String getSchemaUri(int version) {
        return String.format(NS_DEVICES_URI_BASE + "%d", version);                   //$NON-NLS-1$
    }

    /**
     * Returns a stream to the requested {@code device} XML Schema.
     *
     * @param version Between 1 and {@link #NS_LATEST_VERSION}, included.
     * @return An {@link InputStream} object for the local XSD file or
     *         null if there is no schema for the requested version.
     */
    public static InputStream getXsdStream(int version) {
        assert version >= 1 && version <= NS_LATEST_VERSION
                : "Unexpected schema version " + version;
        String rootElement = NODE_DEVICES;                                           //$NON-NLS-1$
        String filename = String.format("%1$s-%2$d.xsd", rootElement, version);      //$NON-NLS-1$

        try {
            return DeviceSchema.class.getResourceAsStream(filename);
        } catch (Exception ignore) {
            // Some implementations seem to return null on failure,
            // others throw an exception. We want to return null.
        }
        return null;
    }

    /**
     * Validates the input stream against the corresponding Devices XSD schema
     * and then does a sanity check on the content.
     *
     * @param deviceXml The XML InputStream to validate.
     *                  The XML input stream must supports the mark/reset() methods
     *                  (that is its {@link InputStream#markSupported()} must return true)
     *                   and which mark has already been set to the beginning of the stream.
     * @param out       The OutputStream for error messages.
     * @param parent    The parent directory of the input stream.
     * @return Whether the given input constitutes a valid devices file.
     */
    public static boolean validate(InputStream deviceXml, OutputStream out, File parent) {
        PrintWriter writer = new PrintWriter(out);

        try {
            if (!(deviceXml instanceof NonClosingInputStream)) {
                deviceXml = new NonClosingInputStream(deviceXml);
                ((NonClosingInputStream) deviceXml).setCloseBehavior(CloseBehavior.RESET);
            }

            int version = getXmlSchemaVersion(deviceXml);
            if (version < 1 || version > NS_LATEST_VERSION) {
                writer.println(String.format("Devices XSD version %1$d is out of valid range 1..%2$d",
                        version, NS_LATEST_VERSION));
                return false;
            }

            assert deviceXml.markSupported();

            // First check the input against the XSD schema

            // Check the input, both against the XSD schema discovered above and also
            // by using a custom validation which tests some properties not encoded in the XSD.

            Schema schema = DeviceSchema.getSchema(version);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            DevicesValidationHandler devicesValidator = new DevicesValidationHandler(parent, writer);
            SAXParser parser = factory.newSAXParser();

            deviceXml.reset();
            parser.parse(deviceXml, devicesValidator);
            return devicesValidator.isValidDevicesFile();
        } catch (SAXException e) {
            writer.println(e.getMessage());
            return false;
        } catch (ParserConfigurationException e) {
            writer.println("Error creating SAX parser:");
            writer.println(e.getMessage());
            return false;
        } catch (IOException e) {
            writer.println("Error reading file stream:");
            writer.println(e.getMessage());
            return false;
        } finally {
            writer.flush();
        }
    }

    /**
     * Helper method that returns a validator for a specific version of the XSD.
     *
     * @param version Between 1 and {@link #NS_LATEST_VERSION}, included.
     * @return A {@link Schema} validator or null.
     */
    @Nullable
    public static Schema getSchema(int version) throws SAXException {
        InputStream xsdStream = getXsdStream(version);
        if (xsdStream == null) {
            return null;
        }
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        return schema;
    }

    /**
     * Manually parses the root element of the XML to extract the schema version
     * at the end of the xmlns:sdk="http://schemas.android.com/sdk/devices/$N"
     * declaration.
     *
     * @param xml An XML input stream that supports the mark/reset() methods
     *              (that is its {@link InputStream#markSupported()} must return true)
     *              and which mark has already been set to the beginning of the stream.
     * @return 1+ for a valid schema version
     *         or 0 if no schema could be found.
     */
    public static int getXmlSchemaVersion(InputStream xml) {
        if (xml == null) {
            return 0;
        }

        // Get an XML document
        Document doc = null;
        try {
            assert xml.markSupported();
            xml.reset();

            if (!(xml instanceof NonClosingInputStream)) {
                xml = new NonClosingInputStream(xml);
                ((NonClosingInputStream) xml).setCloseBehavior(CloseBehavior.RESET);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            factory.setValidating(false);

            // Parse the document using a non namespace aware builder
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // We don't want the default handler which prints errors to stderr.
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    // pass
                }
                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }
                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }
            });

            doc = builder.parse(xml);

            // Prepare a new document using a namespace aware builder
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();

        } catch (Exception e) {
            // Failed to reset XML stream
            // Failed to get builder factor
            // Failed to create XML document builder
            // Failed to parse XML document
            // Failed to read XML document
        }

        if (doc == null) {
            return 0;
        }

        // Check the root element is an XML with at least the following properties:
        // <sdk:sdk-repository
        //    xmlns:sdk="http://schemas.android.com/sdk/devices/$N">
        //
        // Note that we don't have namespace support enabled, we just do it manually.

        Pattern nsPattern = Pattern.compile(NS_DEVICES_URI_PATTERN);

        String prefix = null;
        for (Node child = doc.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                prefix = null;
                String name = child.getNodeName();
                int pos = name.indexOf(':');
                if (pos > 0 && pos < name.length() - 1) {
                    prefix = name.substring(0, pos);
                    name = name.substring(pos + 1);
                }
                if (NODE_DEVICES.equals(name)) {
                    NamedNodeMap attrs = child.getAttributes();
                    String xmlns = "xmlns";                                         //$NON-NLS-1$
                    if (prefix != null) {
                        xmlns += ":" + prefix;                                      //$NON-NLS-1$
                    }
                    Node attr = attrs.getNamedItem(xmlns);
                    if (attr != null) {
                        String uri = attr.getNodeValue();
                        if (uri != null) {
                            Matcher m = nsPattern.matcher(uri);
                            if (m.matches()) {
                                String version = m.group(1);
                                try {
                                    return Integer.parseInt(version);
                                } catch (NumberFormatException e) {
                                    return 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    /**
     * A DefaultHandler that parses only to validate the XML is actually a valid
     * devices config, since validation can't be entirely encoded in the devices
     * schema.
     */
    private static class DevicesValidationHandler extends DefaultHandler {
        private boolean mValidDevicesFile = true;
        private boolean mDefaultSeen = false;
        private String mDeviceName;
        private final File mDirectory;
        private final PrintWriter mWriter;
        private final StringBuilder mStringAccumulator = new StringBuilder();

        public DevicesValidationHandler(File directory, PrintWriter writer) {
            mDirectory = directory; // Possibly null
            mWriter = writer;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            if (NODE_DEVICE.equals(localName)) {
                // Reset for a new device
                mDefaultSeen = false;
            } else if (NODE_STATE.equals(localName)) {
                // Check if the state is set to be a default state
                String val = attributes.getValue(ATTR_DEFAULT);
                if (val != null && ("1".equals(val) || Boolean.parseBoolean(val))) {
                    /*
                     * If it is and we already have a default state for this
                     * device, then the device configuration is invalid.
                     * Otherwise, set that we've seen a default state for this
                     * device and continue
                     */

                    if (mDefaultSeen) {
                        validationError("More than one default state for device " + mDeviceName);
                    } else {
                        mDefaultSeen = true;
                    }
                }
            }
            mStringAccumulator.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            mStringAccumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            // If this is the end of a device node, make sure we have at least
            // one default state
            if (NODE_DEVICE.equals(localName) && !mDefaultSeen) {
                validationError("No default state for device " + mDeviceName);
            } else if (NODE_NAME.equals(localName)) {
                mDeviceName = mStringAccumulator.toString().trim();
            } else if (NODE_PATH.equals(localName) || NODE_SIXTY_FOUR.equals(localName)
                    || NODE_SIXTEEN.equals(localName)) {
                if (mDirectory == null) {
                    // There is no given parent directory, so this is not a
                    // valid devices file
                    validationError("No parent directory given, but relative paths exist.");
                    return;
                }
                // This is going to break on any files that end with a space,
                // but that should be an incredibly rare corner case.
                String relativePath = mStringAccumulator.toString().trim();
                File f = new File(mDirectory, relativePath);
                if (f == null || !f.isFile()) {
                    validationError(relativePath + " is not a valid path.");
                    return;
                }
                String fileName = f.getName();
                int extensionStart = fileName.lastIndexOf('.');
                if (extensionStart == -1 || !fileName.substring(extensionStart + 1).equals("png")) {
                    validationError(relativePath + " is not a valid file type.");
                }
            }
        }

        @Override
        public void error(SAXParseException e) {
            validationError(e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) {
            validationError(e.getMessage());
        }

        public boolean isValidDevicesFile() {
            return mValidDevicesFile;
        }

        private void validationError(String reason) {
            mWriter.println("Error: " + reason);
            mValidDevicesFile = false;
        }

    }
}
