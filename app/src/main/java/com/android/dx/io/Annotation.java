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

/**
 * An annotation.
 */
public final class Annotation implements Comparable<Annotation> {
    private final DexBuffer buffer;
    private final byte visibility;
    private final int typeIndex;
    private final int[] names;
    private final EncodedValue[] values;

    public Annotation(DexBuffer buffer, byte visibility, int typeIndex, int[] names,
            EncodedValue[] values) {
        this.buffer = buffer;
        this.visibility = visibility;
        this.typeIndex = typeIndex;
        this.names = names;
        this.values = values;
    }

    public byte getVisibility() {
        return visibility;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public int[] getNames() {
        return names;
    }

    public EncodedValue[] getValues() {
        return values;
    }

    public void writeTo(DexBuffer.Section out) {
        out.writeByte(visibility);
        out.writeUleb128(typeIndex);
        out.writeUleb128(names.length);
        for (int i = 0; i < names.length; i++) {
            out.writeUleb128(names[i]);
            values[i].writeTo(out);
        }
    }

    @Override public int compareTo(Annotation other) {
        if (typeIndex != other.typeIndex) {
            return Unsigned.compare(typeIndex, other.typeIndex);
        }
        int size = Math.min(names.length, other.names.length);
        for (int i = 0; i < size; i++) {
            if (names[i] != other.names[i]) {
                return Unsigned.compare(names[i], other.names[i]);
            }
            int compare = values[i].compareTo(other.values[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return names.length - other.names.length;
    }

    @Override public String toString() {
        if (buffer == null) {
            return visibility + " " + typeIndex;
        }

        StringBuilder result = new StringBuilder();
        result.append(visibility);
        result.append(" ");
        result.append(buffer.typeNames().get(typeIndex));
        result.append("[");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(buffer.strings().get(names[i]));
            result.append("=");
            result.append(values[i]);
        }
        result.append("]");
        return result.toString();
    }
}
