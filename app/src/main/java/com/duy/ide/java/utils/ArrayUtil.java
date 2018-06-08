/*
 *  Copyright 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.java.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Duy on 24-Mar-17.
 */

public class ArrayUtil {
    public static String arrayToString(Object[] array) {
        return Arrays.toString(array);
    }

    public static String toStringWithoutBracket(Object[] array) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            b.append(array[i].toString());
            if (i == array.length - 1) {
                break;
            }
            b.append(", ");
        }
        return b.toString();
    }



    public static String arrayToString(String[] array) {
        StringBuilder res = new StringBuilder();
        for (String textObject : array) {
            res.append(textObject);
        }
        return res.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] join(Class<T> c, @NonNull T[]... objects) {
        int size = 0;
        for (T[] object : objects) {
            size += object.length;
        }
        T[] result = (T[]) Array.newInstance(c, size);
        int index = 0;
        for (T[] object : objects) {
            for (T t : object) {
                Array.set(result, index, t);
                index++;
            }
        }
        return result;
    }

    public static Class[] join(@NonNull Class[]... objects) {
        int size = 0;
        for (Object[] object : objects) {
            size += object.length;
        }
        Class[] result = (Class[]) Array.newInstance(Class.class, size);
        int index = 0;
        for (Class[] object : objects) {
            for (Class t : object) {
                Array.set(result, index, t);
                index++;
            }
        }
        return result;
    }

    /**
     * uses for function pascal, such as textColor(integer)
     */
    public static String argToString(Object[] argumentTypes) {
        if (argumentTypes == null) return "()";
        int iMax = argumentTypes.length - 1;
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; i < argumentTypes.length; i++) {
            b.append(argumentTypes[i].toString());
            if (i == iMax) {
                b.append(')');
                break;
            }
            b.append(", ");
        }
        if (argumentTypes.length == 0)
            b.append(")");
        return b.toString();
    }



    public static String expectToString(String[] expected, Context context) {
        if (expected.length == 0) return "";
        if (expected.length == 1) return expected[0];
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < expected.length; i++) {
            result.append(expected[i]);
            if (i == expected.length - 1) {
                break;
            }
            result.append(" | ");
        }
        return result.toString();
    }


    public static String toString(@Nullable Object value) {
        if (value == null) return "";
        if (value instanceof Object[]) {
            StringBuilder result = new StringBuilder();
            Object[] arr = (Object[]) value;
            if (arr.length == 0) {
                return "[]";
            }
            if (arr[0] instanceof Object[]) {
                for (Object o : arr) {
                    result.append(toString(o)).append(",");
                }
            } else {
                return Arrays.toString(arr);
            }
        } else {
            return value.toString();
        }
        return "";
    }


}
