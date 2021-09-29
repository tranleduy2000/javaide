/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.common.purchase;

import android.support.annotation.NonNull;
import android.util.Base64;


/**
 * Created by Duy on 23-Jul-17.
 */

public class StringXor {
    @NonNull
    public static String encode(String s, String key) {
        byte[] xor = xor(s.getBytes(), key.getBytes());
        byte[] encoded = Base64.encode(xor, Base64.DEFAULT);
        return new String(encoded);
    }

    @NonNull
    public static String encode(String s) {
        byte[] bytes = s.getBytes();
        byte[] encode = Base64.encode(bytes, Base64.DEFAULT);
        return new String(encode);
    }

    @NonNull
    public static String decode(String s, String key) {
        byte[] decode = Base64.decode(s.getBytes(), Base64.DEFAULT);
        byte[] xor = xor(decode, key.getBytes());
        return new String(xor);
    }

    @NonNull
    public static String decode(String s) {
        byte[] decode = Base64.decode(s.getBytes(), Base64.DEFAULT);
        return new String(decode);
    }

    private static byte[] xor(byte[] src, byte[] key) {
        byte[] out = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            out[i] = (byte) (src[i] ^ key[i % key.length]);
        }
        return out;
    }

}
