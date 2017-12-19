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

import com.android.dx.dex.DexFormat;
import com.android.dx.dex.SizeOf;
import com.android.dx.dex.TableOfContents;
import com.android.dx.merge.TypeList;
import com.android.dx.util.ByteInput;
import com.android.dx.util.ByteOutput;
import com.android.dx.util.DexException;
import com.android.dx.util.FileUtils;
import com.android.dx.util.Leb128Utils;
import com.android.dx.util.Mutf8;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The bytes of a dex file in memory for reading and writing. All int offsets
 * are unsigned.
 */
public final class DexBuffer {
    private byte[] data;
    private final TableOfContents tableOfContents = new TableOfContents();
    private int length = 0;

    private final List<String> strings = new AbstractList<String>() {
        @Override public String get(int index) {
            checkBounds(index, tableOfContents.stringIds.size);
            return open(tableOfContents.stringIds.off + (index * SizeOf.STRING_ID_ITEM))
                    .readString();
        }
        @Override public int size() {
            return tableOfContents.stringIds.size;
        }
    };

    private final List<Integer> typeIds = new AbstractList<Integer>() {
        @Override public Integer get(int index) {
            checkBounds(index, tableOfContents.typeIds.size);
            return open(tableOfContents.typeIds.off + (index * SizeOf.TYPE_ID_ITEM)).readInt();
        }
        @Override public int size() {
            return tableOfContents.typeIds.size;
        }
    };

    private final List<String> typeNames = new AbstractList<String>() {
        @Override public String get(int index) {
            checkBounds(index, tableOfContents.typeIds.size);
            return strings.get(typeIds.get(index));
        }
        @Override public int size() {
            return tableOfContents.typeIds.size;
        }
    };

    private final List<ProtoId> protoIds = new AbstractList<ProtoId>() {
        @Override public ProtoId get(int index) {
            checkBounds(index, tableOfContents.protoIds.size);
            return open(tableOfContents.protoIds.off + (SizeOf.PROTO_ID_ITEM * index))
                    .readProtoId();
        }
        @Override public int size() {
            return tableOfContents.protoIds.size;
        }
    };

    private final List<FieldId> fieldIds = new AbstractList<FieldId>() {
        @Override public FieldId get(int index) {
            checkBounds(index, tableOfContents.fieldIds.size);
            return open(tableOfContents.fieldIds.off + (SizeOf.MEMBER_ID_ITEM * index))
                    .readFieldId();
        }
        @Override public int size() {
            return tableOfContents.fieldIds.size;
        }
    };

    private final List<MethodId> methodIds = new AbstractList<MethodId>() {
        @Override public MethodId get(int index) {
            checkBounds(index, tableOfContents.methodIds.size);
            return open(tableOfContents.methodIds.off + (SizeOf.MEMBER_ID_ITEM * index))
                    .readMethodId();
        }
        @Override public int size() {
            return tableOfContents.methodIds.size;
        }
    };

    /**
     * Creates a new dex buffer defining no classes.
     */
    public DexBuffer() {
        this.data = new byte[0];
    }

    /**
     * Creates a new dex buffer that reads from {@code data}. It is an error to
     * modify {@code data} after using it to create a dex buffer.
     */
    public DexBuffer(byte[] data) throws IOException {
        this.data = data;
        this.length = data.length;
        this.tableOfContents.readFrom(this);
    }

    /**
     * Creates a new dex buffer of the dex in {@code in}, and closes {@code in}.
     */
    public DexBuffer(InputStream in) throws IOException {
        loadFrom(in);
    }

    /**
     * Creates a new dex buffer from the dex file {@code file}.
     */
    public DexBuffer(File file) throws IOException {
        if (FileUtils.hasArchiveSuffix(file.getName())) {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(DexFormat.DEX_IN_JAR_NAME);
            if (entry != null) {
                loadFrom(zipFile.getInputStream(entry));
                zipFile.close();
            } else {
                throw new DexException("Expected " + DexFormat.DEX_IN_JAR_NAME + " in " + file);
            }
        } else if (file.getName().endsWith(".dex")) {
            loadFrom(new FileInputStream(file));
        } else {
            throw new DexException("unknown output extension: " + file);
        }
    }

    private void loadFrom(InputStream in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];

        int count;
        while ((count = in.read(buffer)) != -1) {
            bytesOut.write(buffer, 0, count);
        }
        in.close();

        this.data = bytesOut.toByteArray();
        this.length = data.length;
        this.tableOfContents.readFrom(this);
    }

    private static void checkBounds(int index, int length) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index:" + index + ", length=" + length);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(data);
    }

    public void writeTo(File dexOut) throws IOException {
        OutputStream out = new FileOutputStream(dexOut);
        writeTo(out);
        out.close();
    }

    public TableOfContents getTableOfContents() {
        return tableOfContents;
    }

    public Section open(int position) {
        if (position < 0 || position > length) {
            throw new IllegalArgumentException("position=" + position + " length=" + length);
        }
        return new Section(position);
    }

    public Section appendSection(int maxByteCount, String name) {
        int limit = fourByteAlign(length + maxByteCount);
        Section result = new Section(name, length, limit);
        length = limit;
        return result;
    }

    public void noMoreSections() {
        data = new byte[length];
    }

    public int getLength() {
        return length;
    }

    private static int fourByteAlign(int position) {
        return (position + 3) & ~3;
    }

    public byte[] getBytes() {
        return data;
    }

    public List<String> strings() {
        return strings;
    }

    public List<Integer> typeIds() {
        return typeIds;
    }

    public List<String> typeNames() {
        return typeNames;
    }

    public List<ProtoId> protoIds() {
        return protoIds;
    }

    public List<FieldId> fieldIds() {
        return fieldIds;
    }

    public List<MethodId> methodIds() {
        return methodIds;
    }

    public Iterable<ClassDef> classDefs() {
        return new Iterable<ClassDef>() {
            public Iterator<ClassDef> iterator() {
                if (!tableOfContents.classDefs.exists()) {
                    return Collections.<ClassDef>emptySet().iterator();
                }
                return new Iterator<ClassDef>() {
                    private Section in = open(tableOfContents.classDefs.off);
                    private int count = 0;

                    public boolean hasNext() {
                        return count < tableOfContents.classDefs.size;
                    }
                    public ClassDef next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        count++;
                        return in.readClassDef();
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public TypeList readTypeList(int offset) {
        if (offset == 0) {
            return TypeList.EMPTY;
        }
        return open(offset).readTypeList();
    }

    public ClassData readClassData(ClassDef classDef) {
        int offset = classDef.getClassDataOffset();
        if (offset == 0) {
            throw new IllegalArgumentException("offset == 0");
        }
        return open(offset).readClassData();
    }

    public Code readCode(ClassData.Method method) {
        int offset = method.getCodeOffset();
        if (offset == 0) {
            throw new IllegalArgumentException("offset == 0");
        }
        return open(offset).readCode();
    }

    public final class Section implements ByteInput, ByteOutput {
        private final String name;
        private int position;
        private final int limit;

        private Section(String name, int position, int limit) {
            this.name = name;
            this.position = position;
            this.limit = limit;
        }

        private Section(int position) {
            this("section", position, data.length);
        }

        public int getPosition() {
            return position;
        }

        public int readInt() {
            int result = (data[position] & 0xff)
                    | (data[position + 1] & 0xff) << 8
                    | (data[position + 2] & 0xff) << 16
                    | (data[position + 3] & 0xff) << 24;
            position += 4;
            return result;
        }

        public short readShort() {
            int result = (data[position] & 0xff)
                    | (data[position + 1] & 0xff) << 8;
            position += 2;
            return (short) result;
        }

        public int readUnsignedShort() {
            return readShort() & 0xffff;
        }

        public byte readByte() {
            return (byte) (data[position++] & 0xff);
        }

        public byte[] readByteArray(int length) {
            byte[] result = Arrays.copyOfRange(data, position, position + length);
            position += length;
            return result;
        }

        public short[] readShortArray(int length) {
            short[] result = new short[length];
            for (int i = 0; i < length; i++) {
                result[i] = readShort();
            }
            return result;
        }

        public int readUleb128() {
            return Leb128Utils.readUnsignedLeb128(this);
        }

        public int readSleb128() {
            return Leb128Utils.readSignedLeb128(this);
        }

        public TypeList readTypeList() {
            int size = readInt();
            short[] types = new short[size];
            for (int i = 0; i < size; i++) {
                types[i] = readShort();
            }
            alignToFourBytes();
            return new TypeList(DexBuffer.this, types);
        }

        public String readString() {
            int offset = readInt();
            int savedPosition = position;
            position = offset;
            try {
                int expectedLength = readUleb128();
                String result = Mutf8.decode(this, new char[expectedLength]);
                if (result.length() != expectedLength) {
                    throw new DexException("Declared length " + expectedLength
                            + " doesn't match decoded length of " + result.length());
                }
                return result;
            } catch (UTFDataFormatException e) {
                throw new DexException(e);
            } finally {
                position = savedPosition;
            }
        }

        public FieldId readFieldId() {
            int declaringClassIndex = readUnsignedShort();
            int typeIndex = readUnsignedShort();
            int nameIndex = readInt();
            return new FieldId(DexBuffer.this, declaringClassIndex, typeIndex, nameIndex);
        }

        public MethodId readMethodId() {
            int declaringClassIndex = readUnsignedShort();
            int protoIndex = readUnsignedShort();
            int nameIndex = readInt();
            return new MethodId(DexBuffer.this, declaringClassIndex, protoIndex, nameIndex);
        }

        public ProtoId readProtoId() {
            int shortyIndex = readInt();
            int returnTypeIndex = readInt();
            int parametersOffset = readInt();
            return new ProtoId(DexBuffer.this, shortyIndex, returnTypeIndex, parametersOffset);
        }

        public ClassDef readClassDef() {
            int offset = getPosition();
            int type = readInt();
            int accessFlags = readInt();
            int supertype = readInt();
            int interfacesOffset = readInt();
            int sourceFileIndex = readInt();
            int annotationsOffset = readInt();
            int classDataOffset = readInt();
            int staticValuesOffset = readInt();
            return new ClassDef(DexBuffer.this, offset, type, accessFlags, supertype,
                    interfacesOffset, sourceFileIndex, annotationsOffset, classDataOffset,
                    staticValuesOffset);
        }

        private Code readCode() {
            int registersSize = readUnsignedShort();
            int insSize = readUnsignedShort();
            int outsSize = readUnsignedShort();
            int triesSize = readUnsignedShort();
            int debugInfoOffset = readInt();
            int instructionsSize = readInt();
            short[] instructions = readShortArray(instructionsSize);
            Code.Try[] tries = new Code.Try[triesSize];
            Code.CatchHandler[] catchHandlers = new Code.CatchHandler[0];
            if (triesSize > 0) {
                if (instructions.length % 2 == 1) {
                    readShort(); // padding
                }

                for (int i = 0; i < triesSize; i++) {
                    int startAddress = readInt();
                    int instructionCount = readUnsignedShort();
                    int handlerOffset = readUnsignedShort();
                    tries[i] = new Code.Try(startAddress, instructionCount, handlerOffset);
                }

                int catchHandlersSize = readUleb128();
                catchHandlers = new Code.CatchHandler[catchHandlersSize];
                for (int i = 0; i < catchHandlersSize; i++) {
                    catchHandlers[i] = readCatchHandler();
                }
            }
            return new Code(registersSize, insSize, outsSize, debugInfoOffset, instructions,
                    tries, catchHandlers);
        }

        private Code.CatchHandler readCatchHandler() {
            int size = readSleb128();
            int handlersCount = Math.abs(size);
            int[] typeIndexes = new int[handlersCount];
            int[] addresses = new int[handlersCount];
            for (int i = 0; i < handlersCount; i++) {
                typeIndexes[i] = readUleb128();
                addresses[i] = readUleb128();
            }
            int catchAllAddress = size <= 0 ? readUleb128() : -1;
            return new Code.CatchHandler(typeIndexes, addresses, catchAllAddress);
        }

        private ClassData readClassData() {
            int staticFieldsSize = readUleb128();
            int instanceFieldsSize = readUleb128();
            int directMethodsSize = readUleb128();
            int virtualMethodsSize = readUleb128();
            ClassData.Field[] staticFields = readFields(staticFieldsSize);
            ClassData.Field[] instanceFields = readFields(instanceFieldsSize);
            ClassData.Method[] directMethods = readMethods(directMethodsSize);
            ClassData.Method[] virtualMethods = readMethods(virtualMethodsSize);
            return new ClassData(staticFields, instanceFields, directMethods, virtualMethods);
        }

        private ClassData.Field[] readFields(int count) {
            ClassData.Field[] result = new ClassData.Field[count];
            int fieldIndex = 0;
            for (int i = 0; i < count; i++) {
                fieldIndex += readUleb128(); // field index diff
                int accessFlags = readUleb128();
                result[i] = new ClassData.Field(fieldIndex, accessFlags);
            }
            return result;
        }

        private ClassData.Method[] readMethods(int count) {
            ClassData.Method[] result = new ClassData.Method[count];
            int methodIndex = 0;
            for (int i = 0; i < count; i++) {
                methodIndex += readUleb128(); // method index diff
                int accessFlags = readUleb128();
                int codeOff = readUleb128();
                result[i] = new ClassData.Method(methodIndex, accessFlags, codeOff);
            }
            return result;
        }

        public Annotation readAnnotation() {
            byte visibility = readByte();
            int typeIndex = readUleb128();
            int size = readUleb128();
            int[] names = new int[size];
            EncodedValue[] values = new EncodedValue[size];
            for (int i = 0; i < size; i++) {
                names[i] = readUleb128();
                values[i] = readEncodedValue();
            }
            return new Annotation(DexBuffer.this, visibility, typeIndex, names, values);
        }

        public EncodedValue readEncodedValue() {
            int start = position;
            new EncodedValueReader(this).readValue();
            int end = position;
            return new EncodedValue(Arrays.copyOfRange(data, start, end));
        }

        public EncodedValue readEncodedArray() {
            int start = position;
            new EncodedValueReader(this).readArray();
            int end = position;
            return new EncodedValue(Arrays.copyOfRange(data, start, end));
        }

        private void ensureCapacity(int size) {
            if (position + size > limit) {
                throw new DexException("Section limit " + limit + " exceeded by " + name);
            }
        }

        /**
         * Writes 0x00 until the position is aligned to a multiple of 4.
         */
        public void alignToFourBytes() {
            int unalignedCount = position;
            position = DexBuffer.fourByteAlign(position);
            for (int i = unalignedCount; i < position; i++) {
                data[i] = 0;
            }
        }

        public void assertFourByteAligned() {
            if ((position & 3) != 0) {
                throw new IllegalStateException("Not four byte aligned!");
            }
        }

        public void write(byte[] bytes) {
            ensureCapacity(bytes.length);
            System.arraycopy(bytes, 0, data, position, bytes.length);
            position += bytes.length;
        }

        public void writeByte(int b) {
            ensureCapacity(1);
            data[position++] = (byte) b;
        }

        public void writeShort(short i) {
            ensureCapacity(2);
            data[position    ] = (byte) i;
            data[position + 1] = (byte) (i >>> 8);
            position += 2;
        }

        public void writeUnsignedShort(int i) {
            short s = (short) i;
            if (i != (s & 0xffff)) {
                throw new IllegalArgumentException("Expected an unsigned short: " + i);
            }
            writeShort(s);
        }

        public void write(short[] shorts) {
            for (short s : shorts) {
                writeShort(s);
            }
        }

        public void writeInt(int i) {
            ensureCapacity(4);
            data[position    ] = (byte) i;
            data[position + 1] = (byte) (i >>>  8);
            data[position + 2] = (byte) (i >>> 16);
            data[position + 3] = (byte) (i >>> 24);
            position += 4;
        }

        public void writeUleb128(int i) {
            try {
                Leb128Utils.writeUnsignedLeb128(this, i);
                ensureCapacity(0);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DexException("Section limit " + limit + " exceeded by " + name);
            }
        }

        public void writeSleb128(int i) {
            try {
                Leb128Utils.writeSignedLeb128(this, i);
                ensureCapacity(0);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DexException("Section limit " + limit + " exceeded by " + name);
            }
        }

        public void writeStringData(String value) {
            try {
                int length = value.length();
                writeUleb128(length);
                write(Mutf8.encode(value));
                writeByte(0);
            } catch (UTFDataFormatException e) {
                throw new AssertionError();
            }
        }

        public void writeTypeList(TypeList typeList) {
            short[] types = typeList.getTypes();
            writeInt(types.length);
            for (short type : types) {
                writeShort(type);
            }
            alignToFourBytes();
        }

        /**
         * Returns the number of bytes remaining in this section.
         */
        public int remaining() {
            return limit - position;
        }
    }
}
