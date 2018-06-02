/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sdklib.repository;

import com.google.common.collect.Lists;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Utilities related to the respository XSDs.
 */
public class RepoXsdUtil {

    public static final String NODE_IMPORT = "import";
    public static final String NODE_INCLUDE = "include";

    public static final String ATTR_SCHEMA_LOCATION = "schemaLocation";


    /**
     * Gets StreamSources for the given xsd (implied by the name and version), as well as any xsds imported or included by the main one.
     *
     * @param rootElement The root of the filename of the XML schema. This is by convention the same
     *                    as the root element declared by the schema.
     * @param version     The XML schema revision number, an integer >= 1.
     */
    public static StreamSource[] getXsdStream(final String rootElement, int version) {
        String filename = String.format("%1$s-%2$02d.xsd", rootElement, version);      //$NON-NLS-1$
        final List<StreamSource> streams = Lists.newArrayList();
        InputStream stream = null;
        try {
            stream = RepoXsdUtil.class.getResourceAsStream(filename);
            if (stream == null) {
                filename = String.format("%1$s-%2$d.xsd", rootElement, version);      //$NON-NLS-1$
                stream = RepoXsdUtil.class.getResourceAsStream(filename);
            }
            if (stream == null) {
                // Try the alternate schemas that are not published yet.
                // This allows us to internally test with new schemas before the
                // public repository uses it.
                filename = String.format("-%1$s-%2$02d.xsd", rootElement, version);      //$NON-NLS-1$
                stream = RepoXsdUtil.class.getResourceAsStream(filename);
            }

            // Parse the schema and find any imports or includes so we can return them as well.
            // Currently transitive includes are not supported.
            SAXParserFactory.newInstance().newSAXParser().parse(stream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
                    name = name.substring(name.indexOf(':') + 1);
                    if (name.equals(NODE_IMPORT) || name.equals(NODE_INCLUDE)) {
                        String importFile = attributes.getValue(ATTR_SCHEMA_LOCATION);
                        streams.add(new StreamSource(RepoXsdUtil.class.getResourceAsStream(importFile)));
                    }
                }
            });
            // create and add the first stream again, since SaxParser closes the original one
            streams.add(new StreamSource(RepoXsdUtil.class.getResourceAsStream(filename)));
        } catch (Exception e) {
            // Some implementations seem to return null on failure,
            // others throw an exception. We want to return null.
            return null;
        }
        return streams.toArray(new StreamSource[streams.size()]);
    }
}
