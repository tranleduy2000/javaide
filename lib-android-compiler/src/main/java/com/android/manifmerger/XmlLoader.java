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

package com.android.manifmerger;

import com.android.annotations.Nullable;
import com.android.utils.Pair;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Strings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Responsible for loading XML files.
 */
public final class XmlLoader {

    /**
     * Abstraction for the notion of source location. This is useful for logging and records
     * collection when a origin of an xml declaration is needed.
     */
    public interface SourceLocation {

        /**
         * print this source location in a human and machine readable format.
         *
         * @param shortFormat whether or not to use the short format. For instance, for a file, a
         *                    short format is the file name while the long format is its path.
         * @return the human and machine readable source location.
         */
        String print(boolean shortFormat);

        /**
         * Persist a location to an xml node.
         *
         * @param document the document in which the node will exist.
         * @return the persisted location as a xml node.
         */
        Node toXml(Document document);
    }

    private XmlLoader() {}

    /**
     * Loads an xml file without doing xml validation and return a {@link XmlDocument}
     *
     * @param displayName the xml file display name.
     * @param xmlFile the xml file.
     * @return the initialized {@link com.android.manifmerger.XmlDocument}
     */
    public static XmlDocument load(
            KeyResolver<String> selectors, String displayName, File xmlFile)
            throws IOException, SAXException, ParserConfigurationException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(xmlFile));

        PositionXmlParser positionXmlParser = new PositionXmlParser();
        Document domDocument = positionXmlParser.parse(inputStream);
        return domDocument != null
                ? new XmlDocument(positionXmlParser,
                new FileSourceLocation(displayName, xmlFile),
                selectors,
                domDocument.getDocumentElement())
                : null;
    }


    /**
     * Loads a xml document from its {@link String} representation without doing xml validation and
     * return a {@link com.android.manifmerger.XmlDocument}
     * @param sourceLocation the source location to use for logging and record collection.
     * @param xml the persisted xml.
     * @return the initialized {@link com.android.manifmerger.XmlDocument}
     * @throws IOException this should never be thrown.
     * @throws SAXException if the xml is incorrect
     * @throws ParserConfigurationException if the xml engine cannot be configured.
     */
    public static XmlDocument load(
            KeyResolver<String> selectors, SourceLocation sourceLocation, String xml)
            throws IOException, SAXException, ParserConfigurationException {
        PositionXmlParser positionXmlParser = new PositionXmlParser();
        Document domDocument = positionXmlParser.parse(xml);
        return domDocument != null
                ? new XmlDocument(
                        positionXmlParser,
                        sourceLocation,
                        selectors,
                        domDocument.getDocumentElement())
                : null;
    }

    /**
     * Implementation of {@link SourceLocation} describing a local file.
     */
    private static class FileSourceLocation implements SourceLocation {

        private final File mFile;
        private final String mName;

        private FileSourceLocation(@Nullable String name, File file) {
            this.mFile = file;
            mName = Strings.isNullOrEmpty(name)
                    ? file.getName()
                    : name;
        }

        @Override
        public String print(boolean shortFormat) {
            return shortFormat ? mName : mFile.getAbsolutePath();
        }

        @Override
        public Node toXml(Document document) {
            Element location = document.createElement("source");
            location.setAttribute("name", mName);
            location.setAttribute("scheme", "file://");
            location.setAttribute("value", mFile.getAbsolutePath());
            return location;
        }
    }

    public static SourceLocation locationFromXml(Element location) {
        String scheme = location.getAttribute("scheme");
        if (Strings.isNullOrEmpty(scheme)) {
            return UNKNOWN;
        }
        if (scheme.equals("file://")) {
            return new FileSourceLocation(
                    location.getAttribute("name"),
                    new File(location.getAttribute("value")));
        }
        throw new RuntimeException(scheme + " scheme unsupported");
    }

    public static final SourceLocation UNKNOWN = new SourceLocation() {
        @Override
        public String print(boolean shortFormat) {
            return "Unknown location";
        }

        @Override
        public Node toXml(Document document) {
            // empty node.
            return document.createElement("source");
        }
    };
}
