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

package com.duy.frontend.themefont.fonts;

public class FontEntry {
    public boolean fromStorage;
    public String name;

    public FontEntry(boolean fromStorage, String name) {
        this.fromStorage = fromStorage;
        this.name = name;
    }


    @Override
    public String toString() {
        return "FontEntry{" +
                "fromStorage=" + fromStorage +
                ", name='" + name + '\'' +
                '}';
    }
}