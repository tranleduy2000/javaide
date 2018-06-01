/**
 * Copyright 2012, Red Hat Inc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duy.android.compiler.repo.maven;

import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mickael Istria (Red Hat Inc)
 */
public class SignatureUtils {

    public static void verifySignature(File file, String expectedDigest, MessageDigest digest) throws Exception {
        String actualDigestHex = SignatureUtils.computeSignatureAsString(file, digest);
        if (!actualDigestHex.equals(expectedDigest)) {
            throw new MojoFailureException("Not same digest as expected: expected <" + expectedDigest + "> was <" + actualDigestHex + ">");
        }
    }

    public static String computeSignatureAsString(File file,
                                                  MessageDigest digest) throws IOException {
        InputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                digest.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        byte[] actualDigest = digest.digest();
        return new String(Hex.encodeHex(actualDigest));
    }

    public static String getMD5(File file) throws IOException, NoSuchAlgorithmException {
        return computeSignatureAsString(file, MessageDigest.getInstance("MD5"));
    }

    public static String getSHA1(File file) throws IOException, NoSuchAlgorithmException {
        return computeSignatureAsString(file, MessageDigest.getInstance("SHA1"));
    }

    public static String getSHA512(File file) throws IOException, NoSuchAlgorithmException {
        return computeSignatureAsString(file, MessageDigest.getInstance("SHA-512"));
    }
}
