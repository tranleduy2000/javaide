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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Preconditions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link PositionXmlParser.Position} capable of initializing
 * from xml definition or with given line, column and offset.
*/
final class PositionImpl implements PositionXmlParser.Position {

    private static final String POSITION_ELEMENT = "position";
    private static final String LINE_ATTRIBUTE = "line";
    private static final String COLUMN_ATTRIBUTE = "col";
    private static final String OFFSET_ATTRIBUTE = "offset";

    /**
     * Unknown position on an action happens when the action did not originate from any source file
     * but from environmental factors like placeholder injection, implicit permissions when
     * upgrading, etc...
     */
    static final PositionXmlParser.Position UNKNOWN = new PositionImpl(0, 0, 0);

    private final int mLine;
    private final int mColumn;
    private final int mOffset;

    private PositionImpl(int line, int column, int offset) {
        mLine = line;
        mColumn = column;
        mOffset = offset;
    }

    /**
     * Creates a {@link PositionXmlParser.Position} from its Xml reprentation.
     * @param xml the xml representation of the element
     * @return the {link Position} initialized from its Xml representation.
     */
    public static PositionXmlParser.Position fromXml(Element xml) {
        Preconditions.checkArgument(xml.getNodeName().equals(POSITION_ELEMENT));
        return new PositionImpl(
                Integer.parseInt(xml.getAttribute(LINE_ATTRIBUTE)),
                Integer.parseInt(xml.getAttribute(COLUMN_ATTRIBUTE)),
                Integer.parseInt(xml.getAttribute(OFFSET_ATTRIBUTE)));
    }

    /**
     * Persists a position to an xml representation.
     * @param position the position to be persisted.
     * @param document the document to persist into.
     * @return the xml {@link Element} containing the persisted position.
     */
    public static Element toXml(PositionXmlParser.Position position, Document document) {
        Element xml = document.createElement(PositionImpl.POSITION_ELEMENT);
        xml.setAttribute(LINE_ATTRIBUTE, String.valueOf(position.getLine()));
        xml.setAttribute(COLUMN_ATTRIBUTE, String.valueOf(position.getColumn()));
        xml.setAttribute(OFFSET_ATTRIBUTE, String.valueOf(position.getOffset()));
        return xml;

    }

    @Nullable
    @Override
    public PositionXmlParser.Position getEnd() {
        return null;
    }

    @Override
    public void setEnd(@NonNull PositionXmlParser.Position end) {

    }

    @Override
    public int getLine() {
        return mLine;
    }

    @Override
    public int getOffset() {
        return mOffset;
    }

    @Override
    public int getColumn() {
        return mColumn;
    }
}
