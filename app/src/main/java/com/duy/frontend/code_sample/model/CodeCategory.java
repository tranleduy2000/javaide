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

package com.duy.frontend.code_sample.model;

import java.util.ArrayList;

/**
 * Created by Duy on 08-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodeCategory {
    private String title;
    private String description;
    private String imagePath;

    public ArrayList<CodeSampleEntry> getCodeSampleEntries() {
        return codeSampleEntries;
    }

    private ArrayList<CodeSampleEntry> codeSampleEntries = new ArrayList<>();

    public CodeCategory(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public CodeCategory(String title, String description, String imagePath) {
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;

    }

    public int getCodeSize() {
        return codeSampleEntries.size();
    }

    public void addCodeItem(CodeSampleEntry entry) {
        codeSampleEntries.add(entry);
    }

    public void removeCode(CodeSampleEntry entry) {
        codeSampleEntries.remove(entry);
    }

    public void removeCode(int position) {
        if (position > codeSampleEntries.size() - 1) return;
        codeSampleEntries.remove(position);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public CodeSampleEntry getCode(int childPosition) {
        return codeSampleEntries.get(childPosition);
    }
}
