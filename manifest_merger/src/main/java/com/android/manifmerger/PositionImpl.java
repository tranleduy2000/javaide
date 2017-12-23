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

/**
 * Implementation of {@link PositionXmlParser.Position} capable of initializing
 * from xml definition or with given line, column and offset.
*/
final class PositionImpl implements PositionXmlParser.Position {

    /**
     * Unknown position on an action happens when the action did not originate from any source file
     * but from environmental factors like placeholder injection, implicit permissions when
     * upgrading, etc...
     */
    static final PositionXmlParser.Position UNKNOWN = new PositionImpl(0, 0, 0);

    private final int mLine;
    private final int mColumn;
    private final int mOffset;

    PositionImpl(int line, int column, int offset) {
        mLine = line;
        mColumn = column;
        mOffset = offset;
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

    @Override
    public String toString() {
        if (mLine == 0 && mColumn ==0) {
            return "(unknown)";
        } else {
            return mLine + ":" + mColumn;
        }
    }
}
