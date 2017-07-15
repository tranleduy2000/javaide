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

package com.duy.frontend.alogrithm;

public class InputData {
    public static final int MAX_INPUT = 4 * 1024; //4 MB
    public String[] data = new String[MAX_INPUT]; // the array of the characters
    public int last;    // number of char in the input buffer
    public int first;    // index of the first character

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = first; i < last; i++) stringBuilder.append(data[i]);
        return stringBuilder.toString();
    }

    /**
     * clear text
     */
    public void clear() {
        first = 0;
        last = 0;
    }
}
