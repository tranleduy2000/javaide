package com.duy.compile.builder.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class MD5Hash {

    /**
     * @source http://stackoverflow.com/a/304350
     */
    private static byte[] hash(String alg, InputStream in) throws Exception {
        MessageDigest md = MessageDigest.getInstance(alg);
        DigestInputStream dis = new DigestInputStream(new BufferedInputStream(in), md);
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int readCount = dis.read(buffer);
                if (readCount < 0) {
                    break;
                }
            }
            return md.digest();
        } finally {
            in.close();
        }
    }

    /**
     * @source http://stackoverflow.com/a/304275
     */
    public static String getMD5Checksum(File file) throws Exception {
        byte[] b = hash("MD5", new FileInputStream(file));
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}
