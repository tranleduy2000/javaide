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

import static com.android.manifmerger.ManifestMerger2.SystemProperty;
import static com.android.manifmerger.PlaceholderHandler.KeyBasedValueResolver;

import com.android.annotations.Nullable;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Optional;
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
    public static class SourceLocation {

        @Nullable
        private final File mSource;

        @Nullable
        private final String mDescription;

        /**
         * Build a source location, one of the parameter must not be null.
         */
        public SourceLocation(@Nullable String description, @Nullable File source) {
            if (description == null && source == null) {
                throw new IllegalArgumentException("description and source cannot be both null");
            }
            mDescription = description == null ? source.getName() : description;
            mSource = source;
        }

        /**
         * print this source location in a human and machine readable format.
         *
         * @param shortFormat whether or not to use the short format. For instance, for a file, a
         *                    short format is the file name while the long format is its path.
         * @return the human and machine readable source location.
         */
        String print(boolean shortFormat) {
            return shortFormat
                    ? mDescription
                    : mSource == null
                            ? mDescription :
                            mSource.getAbsolutePath();
        }
    }

    private XmlLoader() {}

    /**
     * Loads an xml file without doing xml validation and return a {@link XmlDocument}
     *
     * @param displayName the xml file display name.
     * @param xmlFile the xml file.
     * @return the initialized {@link XmlDocument}
     */
    public static XmlDocument load(
            KeyResolver<String> selectors,
            KeyBasedValueResolver<SystemProperty> systemPropertyResolver,
            String displayName,
            File xmlFile,
            XmlDocument.Type type,
            Optional<String> mainManifestPackageName)
            throws IOException, SAXException, ParserConfigurationException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(xmlFile));

        PositionXmlParser positionXmlParser = new PositionXmlParser();
        Document domDocument = positionXmlParser.parse(inputStream);
        return domDocument != null
                ? new XmlDocument(positionXmlParser,
                new SourceLocation(displayName, xmlFile),
                selectors,
                systemPropertyResolver,
                domDocument.getDocumentElement(),
                type,
                mainManifestPackageName)
                : null;
    }


    /**
     * Loads a xml document from its {@link String} representation without doing xml validation and
     * return a {@link XmlDocument}
     * @param sourceLocation the source location to use for logging and record collection.
     * @param xml the persisted xml.
     * @return the initialized {@link XmlDocument}
     * @throws IOException this should never be thrown.
     * @throws SAXException if the xml is incorrect
     * @throws ParserConfigurationException if the xml engine cannot be configured.
     */
    public static XmlDocument load(
            KeyResolver<String> selectors,
            KeyBasedValueResolver<SystemProperty> systemPropertyResolver,
            SourceLocation sourceLocation,
            String xml,
            XmlDocument.Type type,
            Optional<String> mainManifestPackageName)
            throws IOException, SAXException, ParserConfigurationException {
        PositionXmlParser positionXmlParser = new PositionXmlParser();
        Document domDocument = positionXmlParser.parse(xml);
        return domDocument != null
                ? new XmlDocument(
                        positionXmlParser,
                        sourceLocation,
                        selectors,
                        systemPropertyResolver,
                        domDocument.getDocumentElement(),
                        type,
                        mainManifestPackageName)
                : null;
    }

    public static final SourceLocation UNKNOWN =
            new SourceLocation("Unknown location", null /* source */);
}
