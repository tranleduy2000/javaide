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

package com.android.dx.io;

import com.android.dx.util.Unsigned;

public final class ProtoId implements Comparable<ProtoId> {
    private final DexBuffer buffer;
    private final int shortyIndex;
    private final int returnTypeIndex;
    private final int parametersOffset;

    public ProtoId(DexBuffer buffer, int shortyIndex, int returnTypeIndex, int parametersOffset) {
        this.buffer = buffer;
        this.shortyIndex = shortyIndex;
        this.returnTypeIndex = returnTypeIndex;
        this.parametersOffset = parametersOffset;
    }

    public int compareTo(ProtoId other) {
        if (returnTypeIndex != other.returnTypeIndex) {
            return Unsigned.compare(returnTypeIndex, other.returnTypeIndex);
        }
        return Unsigned.compare(parametersOffset, other.parametersOffset);
    }

    public int getShortyIndex() {
        return shortyIndex;
    }

    public int getReturnTypeIndex() {
        return returnTypeIndex;
    }

    public int getParametersOffset() {
        return parametersOffset;
    }

    public void writeTo(DexBuffer.Section out) {
        out.writeInt(shortyIndex);
        out.writeInt(returnTypeIndex);
        out.writeInt(parametersOffset);
    }

    @Override public String toString() {
        if (buffer == null) {
            return shortyIndex + " " + returnTypeIndex + " " + parametersOffset;
        }

        return buffer.strings().get(shortyIndex)
                + ": " + buffer.typeNames().get(returnTypeIndex)
                + " " + buffer.readTypeList(parametersOffset);
    }
}
