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

package com.duy.frontend.structure.viewholder;

import java.io.Serializable;
import java.util.ArrayList;

import static android.app.ApplicationErrorReport.TYPE_NONE;

public class StructureItem implements Serializable {

    public String text;
    public int type;
    public ArrayList<StructureItem> listNode = new ArrayList<>();

    public StructureItem(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public StructureItem(String text) {
        this.type = TYPE_NONE;
        this.text = text;
    }

    public void addNode(StructureItem node) {
        listNode.add(node);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}