/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.XmlParser;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Location.Handle;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.XmlContext;
import com.android.utils.PositionXmlParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * A customization of the {@link PositionXmlParser} which creates position
 * objects that directly extend the lint
 * {@link com.android.tools.lint.detector.api.Position} class.
 * <p>
 * It also catches and reports parser errors as lint errors.
 */
public class LintCliXmlParser extends XmlParser {
    @Override
    public Document parseXml(@NonNull XmlContext context) {
        String xml = null;
        try {
            // Do we need to provide an input stream for encoding?
            xml = context.getContents();
            if (xml != null) {
                return PositionXmlParser.parse(xml);
            }
        } catch (UnsupportedEncodingException e) {
            context.report(
                    // Must provide an issue since API guarantees that the issue parameter
                    // is valid
                    IssueRegistry.PARSER_ERROR, Location.create(context.file),
                    e.getCause() != null ? e.getCause().getLocalizedMessage() :
                            e.getLocalizedMessage()
            );
        } catch (SAXException e) {
            Location location = Location.create(context.file);
            String message = e.getCause() != null ? e.getCause().getLocalizedMessage() :
                    e.getLocalizedMessage();
            if (message.startsWith("The processing instruction target matching "
                    + "\"[xX][mM][lL]\" is not allowed.")) {
                int prologue = xml.indexOf("<?xml ");
                int comment = xml.indexOf("<!--");
                if (prologue != -1 && comment != -1 && comment < prologue) {
                    message = "The XML prologue should appear before, not after, the first XML "
                            + "header/copyright comment. " + message;
                }
            }
            context.report(
                    // Must provide an issue since API guarantees that the issue parameter
                    // is valid
                    IssueRegistry.PARSER_ERROR, location,
                    message
            );
        } catch (Throwable t) {
            context.log(t, null);
        }
        return null;
    }

    @NonNull
    @Override
    public Location getLocation(@NonNull XmlContext context, @NonNull Node node) {
        return Location.create(context.file, PositionXmlParser.getPosition(node));
    }

    @NonNull
    @Override
    public Location getLocation(@NonNull XmlContext context, @NonNull Node node,
            int start, int end) {
        return Location.create(context.file, PositionXmlParser.getPosition(node, start, end));
    }

    @Override
    @NonNull
    public Location getNameLocation(@NonNull XmlContext context, @NonNull Node node) {
        Location location = getLocation(context, node);
        Position start = location.getStart();
        Position end = location.getEnd();
        if (start == null || end == null) {
            return location;
        }
        int delta = node instanceof Element ? 1 : 0; // Elements: skip "<"
        int length = node.getNodeName().length();
        int startOffset = start.getOffset() + delta;
        int startColumn = start.getColumn() + delta;
        return Location.create(location.getFile(),
                new DefaultPosition(start.getLine(), startColumn, startOffset),
                new DefaultPosition(end.getLine(), startColumn + length, startOffset + length));
    }

    @Override
    @NonNull
    public Location getValueLocation(@NonNull XmlContext context, @NonNull Attr node) {
        Location location = getLocation(context, node);
        Position start = location.getStart();
        Position end = location.getEnd();
        if (start == null || end == null) {
            return location;
        }
        int totalLength = end.getOffset() - start.getOffset();
        int length = node.getValue().length();
        int delta = totalLength - 1 - length;
        int startOffset = start.getOffset() + delta;
        int startColumn = start.getColumn() + delta;
        return Location.create(location.getFile(),
                new DefaultPosition(start.getLine(), startColumn, startOffset),
                new DefaultPosition(end.getLine(), startColumn + length, startOffset + length));
    }

    @NonNull
    @Override
    public Handle createLocationHandle(@NonNull XmlContext context, @NonNull Node node) {
        return new LocationHandle(context.file, node);
    }

    @Override
    public int getNodeStartOffset(@NonNull XmlContext context, @NonNull Node node) {
        return  PositionXmlParser.getPosition(node).getStartOffset();
    }

    @Override
    public int getNodeEndOffset(@NonNull XmlContext context, @NonNull Node node) {
        return  PositionXmlParser.getPosition(node).getEndOffset();
    }

    /* Handle for creating DOM positions cheaply and returning full fledged locations later */
    private class LocationHandle implements Handle {
        private final File mFile;
        private final Node mNode;
        private Object mClientData;

        public LocationHandle(File file, Node node) {
            mFile = file;
            mNode = node;
        }

        @NonNull
        @Override
        public Location resolve() {
            return Location.create(mFile, PositionXmlParser.getPosition(mNode));
        }

        @Override
        public void setClientData(@Nullable Object clientData) {
            mClientData = clientData;
        }

        @Override
        @Nullable
        public Object getClientData() {
            return mClientData;
        }
    }
}
