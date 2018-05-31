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

package com.duy.ide.java.code_sample.model;

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
