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

package com.duy.ide.javaide.sample.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Duy on 08-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodeCategory implements Serializable, Cloneable {
    private String title;
    private String description;
    private String imagePath;
    private String categoryPath;
    private ArrayList<CodeProjectSample> codeSampleEntries = new ArrayList<>();

    public CodeCategory(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public CodeCategory(String title, String description, String imagePath, String categoryPath) {
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.categoryPath = categoryPath;

    }

    public String getProjectPath() {
        return categoryPath;
    }

    public void setProjectPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public ArrayList<CodeProjectSample> getCodeSampleEntries() {
        return codeSampleEntries;
    }

    public int size() {
        return codeSampleEntries.size();
    }

    public void addCodeItem(CodeProjectSample entry) {
        codeSampleEntries.add(entry);
    }

    public void removeCode(CodeProjectSample entry) {
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

    public CodeProjectSample getProject(int childPosition) {
        return codeSampleEntries.get(childPosition);
    }
}
