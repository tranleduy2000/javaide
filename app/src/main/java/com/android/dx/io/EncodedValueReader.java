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

import com.android.dx.util.ByteInput;
import com.android.dx.util.Leb128Utils;

/**
 * SAX-style reader for encoded values.
 * TODO: convert this to a pull-style reader
 */
public class EncodedValueReader {
    public static final int ENCODED_BYTE = 0x00;
    public static final int ENCODED_SHORT = 0x02;
    public static final int ENCODED_CHAR = 0x03;
    public static final int ENCODED_INT = 0x04;
    public static final int ENCODED_LONG = 0x06;
    public static final int ENCODED_FLOAT = 0x10;
    public static final int ENCODED_DOUBLE = 0x11;
    public static final int ENCODED_STRING = 0x17;
    public static final int ENCODED_TYPE = 0x18;
    public static final int ENCODED_FIELD = 0x19;
    public static final int ENCODED_ENUM = 0x1b;
    public static final int ENCODED_METHOD = 0x1a;
    public static final int ENCODED_ARRAY = 0x1c;
    public static final int ENCODED_ANNOTATION = 0x1d;
    public static final int ENCODED_NULL = 0x1e;
    public static final int ENCODED_BOOLEAN = 0x1f;

    protected final ByteInput in;

    public EncodedValueReader(ByteInput in) {
        this.in = in;
    }

    public EncodedValueReader(EncodedValue in) {
        this(in.asByteInput());
    }

    public final void readArray() {
        int size = Leb128Utils.readUnsignedLeb128(in);
        visitArray(size);

        for (int i = 0; i < size; i++) {
            readValue();
        }
    }

    public final void readAnnotation() {
        int typeIndex = Leb128Utils.readUnsignedLeb128(in);
        int size = Leb128Utils.readUnsignedLeb128(in);
        visitAnnotation(typeIndex, size);

        for (int i = 0; i < size; i++) {
            visitAnnotationName(Leb128Utils.readUnsignedLeb128(in));
            readValue();
        }
    }

    public final void readValue() {
        int argAndType = in.readByte() & 0xff;
        int type = argAndType & 0x1f;
        int arg = (argAndType & 0xe0) >> 5;
        int size = arg + 1;

        switch (type) {
        case ENCODED_BYTE:
        case ENCODED_SHORT:
        case ENCODED_CHAR:
        case ENCODED_INT:
        case ENCODED_LONG:
        case ENCODED_FLOAT:
        case ENCODED_DOUBLE:
            visitPrimitive(argAndType, type, arg, size);
            break;
        case ENCODED_STRING:
            visitString(type, readIndex(in, size));
            break;
        case ENCODED_TYPE:
            visitType(type, readIndex(in, size));
            break;
        case ENCODED_FIELD:
        case ENCODED_ENUM:
            visitField(type, readIndex(in, size));
            break;
        case ENCODED_METHOD:
            visitMethod(type, readIndex(in, size));
            break;
        case ENCODED_ARRAY:
            visitArrayValue(argAndType);
            readArray();
            break;
        case ENCODED_ANNOTATION:
            visitAnnotationValue(argAndType);
            readAnnotation();
            break;
        case ENCODED_NULL:
            visitEncodedNull(argAndType);
            break;
        case ENCODED_BOOLEAN:
            visitEncodedBoolean(argAndType);
            break;
        }
    }

    protected void visitArray(int size) {}
    protected void visitAnnotation(int typeIndex, int size) {}
    protected void visitAnnotationName(int nameIndex) {}
    protected void visitPrimitive(int argAndType, int type, int arg, int size) {
        for (int i = 0; i < size; i++) {
            in.readByte();
        }
    }
    protected void visitString(int type, int index) {}
    protected void visitType(int type, int index) {}
    protected void visitField(int type, int index) {}
    protected void visitMethod(int type, int index) {}
    protected void visitArrayValue(int argAndType) {}
    protected void visitAnnotationValue(int argAndType) {}
    protected void visitEncodedBoolean(int argAndType) {}
    protected void visitEncodedNull(int argAndType) {}

    private int readIndex(ByteInput in, int byteCount) {
        int result = 0;
        int shift = 0;
        for (int i = 0; i < byteCount; i++) {
            result += (in.readByte() & 0xff) << shift;
            shift += 8;
        }
        return result;
    }
}
