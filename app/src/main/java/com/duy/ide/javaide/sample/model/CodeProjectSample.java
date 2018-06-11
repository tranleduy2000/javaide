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

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by Duy on 08-Apr-17.
 */

public class CodeProjectSample implements Serializable{
    /**
     * name of file code
     */
    private String name;
    private String path;
    private String description;
    /**
     * code
     */
    private String content;
    private String query;

    public CodeProjectSample(String name, String path, String description) {
        this.name = name;
        this.path = path;
        this.description = description;
    }

    public CodeProjectSample(String name, CharSequence content) {
        this.name = name;
        this.content = content.toString();
    }

    public CodeProjectSample(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("path", path)
                .add("description", description)
                .add("content", content)
                .add("query", query)
                .toString();
    }

    public CodeProjectSample clone() {
        return new CodeProjectSample(name, content);
    }
}
