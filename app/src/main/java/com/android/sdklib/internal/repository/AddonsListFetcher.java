/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.sdklib.internal.repository;

import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.sdklib.repository.SdkAddonsListConstants;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLKeyException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Fetches and loads an sdk-addons-list XML.
 * <p/>
 * Such an XML contains a simple list of add-ons site that are to be loaded by default by the
 * SDK Manager. <br/>
 * The XML must conform to the sdk-addons-list-N.xsd. <br/>
 * Constants used in the XML are defined in {@link SdkAddonsListConstants}.
 */
public class AddonsListFetcher {

    /**
     * An immutable structure representing an add-on site.
     */
    public static class Site {
        private final String mUrl;
        private final String mUiName;

        private Site(String url, String uiName) {
            mUrl = url.trim();
            mUiName = uiName;
        }

        public String getUrl() {
            return mUrl;
        }

        public String getUiName() {
            return mUiName;
        }
    }

    /**
     * Fetches the addons list from the given URL.
     *
     * @param monitor A monitor to report errors. Cannot be null.
     * @param url The URL of an XML file resource that conforms to the latest sdk-addons-list-N.xsd.
     *   For the default operation, use {@link SdkAddonsListConstants#URL_ADDON_LIST}.
     *   Cannot be null.
     * @return An array of {@link Site} on success (possibly empty), or null on error.
     */
    public Site[] fetch(ITaskMonitor monitor, String url) {

        url = url == null ? "" : url.trim();

        monitor.setProgressMax(5);
        monitor.setDescription("Fetching %1$s", url);
        monitor.incProgress(1);

        Exception[] exception = new Exception[] { null };
        Boolean[] validatorFound = new Boolean[] { Boolean.FALSE };
        String[] validationError = new String[] { null };
        Document validatedDoc = null;
        String validatedUri = null;

        ByteArrayInputStream xml = fetchUrl(url, monitor.createSubMonitor(1), exception);

        if (xml != null) {
            monitor.setDescription("Validate XML");

            // Explore the XML to find the potential XML schema version
            int version = getXmlSchemaVersion(xml);

            if (version >= 1 && version <= SdkAddonsListConstants.NS_LATEST_VERSION) {
                // This should be a version we can handle. Try to validate it
                // and report any error as invalid XML syntax,

                String uri = validateXml(xml, url, version, validationError, validatorFound);
                if (uri != null) {
                    // Validation was successful
                    validatedDoc = getDocument(xml, monitor);
                    validatedUri = uri;

                }
            } else if (version > SdkAddonsListConstants.NS_LATEST_VERSION) {
                // The schema used is more recent than what is supported by this tool.
                // We don't have an upgrade-path support yet, so simply ignore the document.
                return null;
            }
        }

        // If any exception was handled during the URL fetch, display it now.
        if (exception[0] != null) {
            String reason = null;
            if (exception[0] instanceof FileNotFoundException) {
                // FNF has no useful getMessage, so we need to special handle it.
                reason = "File not found";
            } else if (exception[0] instanceof UnknownHostException &&
                    exception[0].getMessage() != null) {
                // This has no useful getMessage yet could really use one
                reason = String.format("Unknown Host %1$s", exception[0].getMessage());
            } else if (exception[0] instanceof SSLKeyException) {
                // That's a common error and we have a pref for it.
                reason = "HTTPS SSL error. You might want to force download through HTTP in the settings.";
            } else if (exception[0].getMessage() != null) {
                reason = exception[0].getMessage();
            } else {
                // We don't know what's wrong. Let's give the exception class at least.
                reason = String.format("Unknown (%1$s)", exception[0].getClass().getName());
            }

            monitor.logError("Failed to fetch URL %1$s, reason: %2$s", url, reason);
        }

        if (validationError[0] != null) {
            monitor.logError("%s", validationError[0]);  //$NON-NLS-1$
        }

        // Stop here if we failed to validate the XML. We don't want to load it.
        if (validatedDoc == null) {
            return null;
        }

        monitor.incProgress(1);

        Site[] result = null;

        if (xml != null) {
            monitor.setDescription("Parse XML");
            monitor.incProgress(1);
            result = parseAddonsList(validatedDoc, validatedUri, monitor);
        }

        // done
        monitor.incProgress(1);

        return result;
    }

    /**
     * Fetches the document at the given URL and returns it as a stream. Returns
     * null if anything wrong happens. References: <br/>
     * URL Connection:
     *
     * @param urlString The URL to load, as a string.
     * @param monitor {@link ITaskMonitor} related to this URL.
     * @param outException If non null, where to store any exception that
     *            happens during the fetch.
     * @see UrlOpener UrlOpener, which handles all URL logic.
     */
    private ByteArrayInputStream fetchUrl(String urlString, ITaskMonitor monitor,
            Exception[] outException) {
        try {

            InputStream is = null;

            int inc = 65536;
            int curr = 0;
            byte[] result = new byte[inc];

            try {
                is = UrlOpener.openUrl(urlString, monitor);

                int n;
                while ((n = is.read(result, curr, result.length - curr)) != -1) {
                    curr += n;
                    if (curr == result.length) {
                        byte[] temp = new byte[curr + inc];
                        System.arraycopy(result, 0, temp, 0, curr);
                        result = temp;
                    }
                }

                return new ByteArrayInputStream(result, 0, curr);

            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // pass
                    }
                }
            }

        } catch (Exception e) {
            if (outException != null) {
                outException[0] = e;
            }
        }

        return null;
    }

    /**
     * Manually parses the root element of the XML to extract the schema version
     * at the end of the xmlns:sdk="http://schemas.android.com/sdk/android/addons-list/$N"
     * declaration.
     *
     * @return 1..{@link SdkAddonsListConstants#NS_LATEST_VERSION} for a valid schema version
     *         or 0 if no schema could be found.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected int getXmlSchemaVersion(InputStream xml) {
        if (xml == null) {
            return 0;
        }

        // Get an XML document
        Document doc = null;
        try {
            xml.reset();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            factory.setValidating(false);

            // Parse the old document using a non namespace aware builder
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
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
        // <sdk:sdk-addons-list
        //    xmlns:sdk="http://schemas.android.com/sdk/android/addons-list/$N">
        //
        // Note that we don't have namespace support enabled, we just do it manually.

        Pattern nsPattern = Pattern.compile(SdkAddonsListConstants.NS_PATTERN);

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
                if (SdkAddonsListConstants.NODE_SDK_ADDONS_LIST.equals(name)) {
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
     * Validates this XML against one of the requested SDK Repository schemas.
     * If the XML was correctly validated, returns the schema that worked.
     * If it doesn't validate, returns null and stores the error in outError[0].
     * If we can't find a validator, returns null and set validatorFound[0] to false.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected String validateXml(InputStream xml, String url, int version,
            String[] outError, Boolean[] validatorFound) {

        if (xml == null) {
            return null;
        }

        try {
            Validator validator = getValidator(version);

            if (validator == null) {
                validatorFound[0] = Boolean.FALSE;
                outError[0] = String.format(
                        "XML verification failed for %1$s.\nNo suitable XML Schema Validator could be found in your Java environment. Please consider updating your version of Java.",
                        url);
                return null;
            }

            validatorFound[0] = Boolean.TRUE;

            // Reset the stream if it supports that operation.
            xml.reset();

            // Validation throws a bunch of possible Exceptions on failure.
            validator.validate(new StreamSource(xml));
            return SdkAddonsListConstants.getSchemaUri(version);

        } catch (SAXParseException e) {
            outError[0] = String.format(
                    "XML verification failed for %1$s.\nLine %2$d:%3$d, Error: %4$s",
                    url,
                    e.getLineNumber(),
                    e.getColumnNumber(),
                    e.toString());

        } catch (Exception e) {
            outError[0] = String.format(
                    "XML verification failed for %1$s.\nError: %2$s",
                    url,
                    e.toString());
        }
        return null;
    }

    /**
     * Helper method that returns a validator for our XSD, or null if the current Java
     * implementation can't process XSD schemas.
     *
     * @param version The version of the XML Schema.
     *        See {@link SdkAddonsListConstants#getXsdStream(int)}
     */
    private Validator getValidator(int version) throws SAXException {
        InputStream xsdStream = SdkAddonsListConstants.getXsdStream(version);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        if (factory == null) {
            return null;
        }

        // This may throw a SAX Exception if the schema itself is not a valid XSD
        Schema schema = factory.newSchema(new StreamSource(xsdStream));

        Validator validator = schema == null ? null : schema.newValidator();

        return validator;
    }

    /**
     * Takes an XML document as a string as parameter and returns a DOM for it.
     *
     * On error, returns null and prints a (hopefully) useful message on the monitor.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected Document getDocument(InputStream xml, ITaskMonitor monitor) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            xml.reset();
            Document doc = builder.parse(new InputSource(xml));

            return doc;
        } catch (ParserConfigurationException e) {
            monitor.logError("Failed to create XML document builder");

        } catch (SAXException e) {
            monitor.logError("Failed to parse XML document");

        } catch (IOException e) {
            monitor.logError("Failed to read XML document");
        }

        return null;
    }

    /**
     * Parse all sites defined in the Addaons list XML and returns an array of sites.
     */
    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected Site[] parseAddonsList(Document doc, String nsUri, ITaskMonitor monitor) {

        Node root = getFirstChild(doc, nsUri, SdkAddonsListConstants.NODE_SDK_ADDONS_LIST);
        if (root != null) {
            ArrayList<Site> sites = new ArrayList<Site>();

            for (Node child = root.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        child.getLocalName().equals(SdkAddonsListConstants.NODE_ADDON_SITE)) {

                    Node url = getFirstChild(child, nsUri, SdkAddonsListConstants.NODE_URL);
                    Node name = getFirstChild(child, nsUri, SdkAddonsListConstants.NODE_NAME);

                    if (name != null && url != null) {
                        String strUrl  = url.getTextContent().trim();
                        String strName = name.getTextContent().trim();

                        if (strUrl.length() > 0 && strName.length() > 0) {
                            sites.add(new Site(strUrl, strName));
                        }
                    }
                }
            }

            return sites.toArray(new Site[sites.size()]);
        }

        return null;
    }

    /**
     * Returns the first child element with the given XML local name.
     * If xmlLocalName is null, returns the very first child element.
     */
    private Node getFirstChild(Node node, String nsUri, String xmlLocalName) {

        for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    nsUri.equals(child.getNamespaceURI())) {
                if (xmlLocalName == null || child.getLocalName().equals(xmlLocalName)) {
                    return child;
                }
            }
        }

        return null;
    }


}
