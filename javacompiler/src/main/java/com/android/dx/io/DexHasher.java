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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

/**
 * Generates and stores the checksum and signature of a dex file.
 */
public final class DexHasher {
    private static final int CHECKSUM_OFFSET = 8;
    private static final int CHECKSUM_SIZE = 4;
    private static final int SIGNATURE_OFFSET = CHECKSUM_OFFSET + CHECKSUM_SIZE;
    private static final int SIGNATURE_SIZE = 20;

    /**
     * Returns the signature of all but the first 32 bytes of {@code dex}. The
     * first 32 bytes of dex files are not specified to be included in the
     * signature.
     */
    public byte[] computeSignature(DexBuffer dex) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
        int offset = SIGNATURE_OFFSET + SIGNATURE_SIZE;

        byte[] bytes = dex.getBytes();
        digest.update(bytes, offset, bytes.length - offset);
        return digest.digest();
    }

    /**
     * Returns the checksum of all but the first 12 bytes of {@code dex}.
     */
    public int computeChecksum(DexBuffer dex) throws IOException {
        Adler32 adler32 = new Adler32();
        int offset = CHECKSUM_OFFSET + CHECKSUM_SIZE;

        byte[] bytes = dex.getBytes();
        adler32.update(bytes, offset, bytes.length - offset);
        return (int) adler32.getValue();
    }

    /**
     * Generates the signature and checksum of the dex file {@code out} and
     * writes them to the file.
     */
    public void writeHashes(DexBuffer dex) throws IOException {
        byte[] signature = computeSignature(dex);
        dex.open(SIGNATURE_OFFSET).write(signature);

        int checksum = computeChecksum(dex);
        dex.open(CHECKSUM_OFFSET).writeInt(checksum);
    }
}
