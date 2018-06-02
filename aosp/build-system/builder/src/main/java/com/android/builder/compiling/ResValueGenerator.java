/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.builder.compiling;

import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.ATTR_TRANSLATABLE;
import static com.android.SdkConstants.ATTR_TYPE;
import static com.android.SdkConstants.TAG_ITEM;
import static com.android.SdkConstants.TAG_RESOURCES;
import static com.android.SdkConstants.VALUE_FALSE;
import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.ClassField;
import com.android.ide.common.xml.XmlPrettyPrinter;
import com.android.resources.ResourceType;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class able to generate a res value file in an Android project.
 */
public class ResValueGenerator {

    public static final String RES_VALUE_FILENAME_XML = "generated.xml";

    private static final List<ResourceType> RESOURCES_WITH_TAGS = ImmutableList.of(
            ResourceType.ARRAY,
            ResourceType.ATTR,
            ResourceType.BOOL,
            ResourceType.COLOR,
            ResourceType.DECLARE_STYLEABLE,
            ResourceType.DIMEN,
            ResourceType.FRACTION,
            ResourceType.INTEGER,
            ResourceType.PLURALS,
            ResourceType.STRING,
            ResourceType.STYLE);

    private final File mGenFolder;

    private final List<ClassField> mFields = Lists.newArrayList();
    private List<Object> mItems = Lists.newArrayList();

    /**
     * Creates a generator
     * @param genFolder the gen folder of the project
     */
    public ResValueGenerator(@NonNull File genFolder) {
        mGenFolder = checkNotNull(genFolder);
    }

    public ResValueGenerator addResource(
            @NonNull String type, @NonNull String name, @NonNull String value) {
        mFields.add(AndroidBuilder.createClassField(type, name, value));
        return this;
    }

    public ResValueGenerator addItems(@Nullable Collection<Object> items) {
        if (items != null) {
            mItems.addAll(items);
        }
        return this;
    }

    /**
     * Returns a File representing where the BuildConfig class will be.
     */
    public File getFolderPath() {
        return new File(mGenFolder, "values");
    }

    /**
     * Generates the resource files
     */
    public void generate() throws IOException, ParserConfigurationException {
        File pkgFolder = getFolderPath();
        if (!pkgFolder.isDirectory()) {
            if (!pkgFolder.mkdirs()) {
                throw new RuntimeException("Failed to create " + pkgFolder.getAbsolutePath());
            }
        }

        File resFile = new File(pkgFolder, RES_VALUE_FILENAME_XML);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Node rootNode = document.createElement(TAG_RESOURCES);
        document.appendChild(rootNode);

        rootNode.appendChild(document.createTextNode("\n"));
        rootNode.appendChild(document.createComment("Automatically generated file. DO NOT MODIFY"));
        rootNode.appendChild(document.createTextNode("\n\n"));

        for (Object item : mItems) {
            if (item instanceof ClassField) {
                ClassField field = (ClassField)item;

                ResourceType type = ResourceType.getEnum(field.getType());
                boolean hasResourceTag = (type != null && RESOURCES_WITH_TAGS.contains(type));

                Node itemNode = document.createElement(hasResourceTag ? field.getType() : TAG_ITEM);
                Attr nameAttr = document.createAttribute(ATTR_NAME);

                nameAttr.setValue(field.getName());
                itemNode.getAttributes().setNamedItem(nameAttr);

                if (!hasResourceTag) {
                    Attr typeAttr = document.createAttribute(ATTR_TYPE);
                    typeAttr.setValue(field.getType());
                    itemNode.getAttributes().setNamedItem(typeAttr);
                }

                if (type == ResourceType.STRING) {
                    Attr translatable = document.createAttribute(ATTR_TRANSLATABLE);
                    translatable.setValue(VALUE_FALSE);
                    itemNode.getAttributes().setNamedItem(translatable);
                }

                if (!field.getValue().isEmpty()) {
                    itemNode.appendChild(document.createTextNode(field.getValue()));
                }

                rootNode.appendChild(itemNode);
            } else if (item instanceof String) {
                rootNode.appendChild(document.createTextNode("\n"));
                rootNode.appendChild(document.createComment((String) item));
                rootNode.appendChild(document.createTextNode("\n"));
            }
        }

        String content;
        try {
            content = XmlPrettyPrinter.prettyPrint(document, true);
        } catch (Throwable t) {
            content = XmlUtils.toXml(document);
        }

        Files.write(content, resFile, Charsets.UTF_8);
    }
}
