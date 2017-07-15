/*
 *  Copyright (c) 2017 Tran Le Duy
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

package com.duy.frontend.utils;

/**
 * Created by Duy on 05-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class StringCompare {
    public static boolean isLessThan(String s1, String s2) {
        return s1.compareTo(s2) < 0;
    }

    public static boolean isGreaterThan(String s1, String s2) {
        return s1.compareTo(s2) > 0;
    }

    public static boolean isGreaterEqual(String s1, String s2) {
        return isGreaterThan(s1, s2) || s1.equals(s2);
    }
}
