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

package com.android.dx.merge;

import com.android.dx.dex.TableOfContents;
import com.android.dx.io.Annotation;
import com.android.dx.io.ClassDef;
import com.android.dx.io.DexBuffer;
import com.android.dx.io.EncodedValue;
import com.android.dx.io.EncodedValueReader;
import com.android.dx.io.FieldId;
import com.android.dx.io.MethodId;
import com.android.dx.io.ProtoId;
import com.android.dx.util.ByteArrayAnnotatedOutput;
import com.android.dx.util.ByteInput;
import com.android.dx.util.ByteOutput;
import com.android.dx.util.Leb128Utils;
import com.android.dx.util.Unsigned;
import java.util.HashMap;

/**
 * Maps the index offsets from one dex file to those in another. For example, if
 * you have string #5 in the old dex file, its position in the new dex file is
 * {@code strings[5]}.
 */
public final class IndexMap {
    private final DexBuffer target;
    public final int[] stringIds;
    public final short[] typeIds;
    public final short[] protoIds;
    public final short[] fieldIds;
    public final short[] methodIds;
    private final HashMap<Integer, Integer> typeListOffsets;
    private final HashMap<Integer, Integer> annotationOffsets;
    private final HashMap<Integer, Integer> annotationSetOffsets;
    private final HashMap<Integer, Integer> annotationDirectoryOffsets;

    public IndexMap(DexBuffer target, TableOfContents tableOfContents) {
        this.target = target;
        this.stringIds = new int[tableOfContents.stringIds.size];
        this.typeIds = new short[tableOfContents.typeIds.size];
        this.protoIds = new short[tableOfContents.protoIds.size];
        this.fieldIds = new short[tableOfContents.fieldIds.size];
        this.methodIds = new short[tableOfContents.methodIds.size];
        this.typeListOffsets = new HashMap<Integer, Integer>();
        this.annotationOffsets = new HashMap<Integer, Integer>();
        this.annotationSetOffsets = new HashMap<Integer, Integer>();
        this.annotationDirectoryOffsets = new HashMap<Integer, Integer>();

        /*
         * A type list, annotation set, or annotation directory at offset 0 is
         * always empty. Always map offset 0 to 0.
         */
        this.typeListOffsets.put(0, 0);
        this.annotationSetOffsets.put(0, 0);
        this.annotationDirectoryOffsets.put(0, 0);
    }

    public void putTypeListOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        typeListOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationSetOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationSetOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationDirectoryOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationDirectoryOffsets.put(oldOffset, newOffset);
    }

    public int adjustString(int stringIndex) {
        return stringIndex == ClassDef.NO_INDEX ? ClassDef.NO_INDEX : stringIds[stringIndex];
    }

    public int adjustType(int typeIndex) {
        return (typeIndex == ClassDef.NO_INDEX) ? ClassDef.NO_INDEX : (typeIds[typeIndex] & 0xffff);
    }

    public TypeList adjustTypeList(TypeList typeList) {
        if (typeList == TypeList.EMPTY) {
            return typeList;
        }
        short[] types = typeList.getTypes().clone();
        for (int i = 0; i < types.length; i++) {
            types[i] = (short) adjustType(types[i]);
        }
        return new TypeList(target, types);
    }

    public int adjustProto(int protoIndex) {
        return protoIds[protoIndex] & 0xffff;
    }

    public int adjustField(int fieldIndex) {
        return fieldIds[fieldIndex] & 0xffff;
    }

    public int adjustMethod(int methodIndex) {
        return methodIds[methodIndex] & 0xffff;
    }

    public int adjustTypeListOffset(int typeListOffset) {
        return typeListOffsets.get(typeListOffset);
    }

    public int adjustAnnotation(int annotationOffset) {
        return annotationOffsets.get(annotationOffset);
    }

    public int adjustAnnotationSet(int annotationSetOffset) {
        return annotationSetOffsets.get(annotationSetOffset);
    }

    public int adjustAnnotationDirectory(int annotationDirectoryOffset) {
        return annotationDirectoryOffsets.get(annotationDirectoryOffset);
    }

    public MethodId adjust(MethodId methodId) {
        return new MethodId(target,
                adjustType(methodId.getDeclaringClassIndex()),
                adjustProto(methodId.getProtoIndex()),
                adjustString(methodId.getNameIndex()));
    }

    public FieldId adjust(FieldId fieldId) {
        return new FieldId(target,
                adjustType(fieldId.getDeclaringClassIndex()),
                adjustType(fieldId.getTypeIndex()),
                adjustString(fieldId.getNameIndex()));

    }

    public ProtoId adjust(ProtoId protoId) {
        return new ProtoId(target,
                adjustString(protoId.getShortyIndex()),
                adjustType(protoId.getReturnTypeIndex()),
                adjustTypeListOffset(protoId.getParametersOffset()));
    }

    public ClassDef adjust(ClassDef classDef) {
        return new ClassDef(target, classDef.getOffset(), adjustType(classDef.getTypeIndex()),
                classDef.getAccessFlags(), adjustType(classDef.getSupertypeIndex()),
                adjustTypeListOffset(classDef.getInterfacesOffset()), classDef.getSourceFileIndex(),
                classDef.getAnnotationsOffset(), classDef.getClassDataOffset(),
                classDef.getStaticValuesOffset());
    }

    public SortableType adjust(SortableType sortableType) {
        return new SortableType(sortableType.getBuffer(), adjust(sortableType.getClassDef()));
    }

    public EncodedValue adjustEncodedValue(EncodedValue encodedValue) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput(32);
        new EncodedValueTransformer(encodedValue, out).readValue();
        return new EncodedValue(out.toByteArray());
    }

    public EncodedValue adjustEncodedArray(EncodedValue encodedArray) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput(32);
        new EncodedValueTransformer(encodedArray, out).readArray();
        return new EncodedValue(out.toByteArray());
    }

    public Annotation adjust(Annotation annotation) {
        int[] names = annotation.getNames().clone();
        EncodedValue[] values = annotation.getValues().clone();
        for (int i = 0; i < names.length; i++) {
            names[i] = adjustString(names[i]);
            values[i] = adjustEncodedValue(values[i]);
        }
        return new Annotation(target, annotation.getVisibility(),
                adjustType(annotation.getTypeIndex()), names, values);
    }

    /**
     * Adjust an encoded value or array.
     */
    private final class EncodedValueTransformer extends EncodedValueReader {
        private final ByteOutput out;

        public EncodedValueTransformer(EncodedValue encodedValue, ByteOutput out) {
            super(encodedValue);
            this.out = out;
        }

        protected void visitArray(int size) {
            Leb128Utils.writeUnsignedLeb128(out, size);
        }

        protected void visitAnnotation(int typeIndex, int size) {
            Leb128Utils.writeUnsignedLeb128(out, adjustType(typeIndex));
            Leb128Utils.writeUnsignedLeb128(out, size);
        }

        protected void visitAnnotationName(int index) {
            Leb128Utils.writeUnsignedLeb128(out, adjustString(index));
        }

        protected void visitPrimitive(int argAndType, int type, int arg, int size) {
            out.writeByte(argAndType);
            copyBytes(in, out, size);
        }

        protected void visitString(int type, int index) {
            writeTypeAndSizeAndIndex(type, adjustString(index));
        }

        protected void visitType(int type, int index) {
            writeTypeAndSizeAndIndex(type, adjustType(index));
        }

        protected void visitField(int type, int index) {
            writeTypeAndSizeAndIndex(type, adjustField(index));
        }

        protected void visitMethod(int type, int index) {
            writeTypeAndSizeAndIndex(type, adjustMethod(index));
        }

        protected void visitArrayValue(int argAndType) {
            out.writeByte(argAndType);
        }

        protected void visitAnnotationValue(int argAndType) {
            out.writeByte(argAndType);
        }

        protected void visitEncodedBoolean(int argAndType) {
            out.writeByte(argAndType);
        }

        protected void visitEncodedNull(int argAndType) {
            out.writeByte(argAndType);
        }

        private void writeTypeAndSizeAndIndex(int type, int index) {
            int byteCount;
            if (Unsigned.compare(index, 0xff) <= 0) {
                byteCount = 1;
            } else if (Unsigned.compare(index, 0xffff) <= 0) {
                byteCount = 2;
            } else if (Unsigned.compare(index, 0xffffff) <= 0) {
                byteCount = 3;
            } else {
                byteCount = 4;
            }
            int argAndType = ((byteCount - 1) << 5) | type;
            out.writeByte(argAndType);

            for (int i = 0; i < byteCount; i++) {
                out.writeByte(index & 0xff);
                index >>>= 8;
            }
        }

        private void copyBytes(ByteInput in, ByteOutput out, int size) {
            for (int i = 0; i < size; i++) {
                out.writeByte(in.readByte());
            }
        }
    }
}
