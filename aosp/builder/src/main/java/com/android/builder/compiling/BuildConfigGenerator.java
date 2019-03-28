/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.ClassField;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.squareup.javawriter.JavaWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Class able to generate a BuildConfig class in an Android project.
 * The BuildConfig class contains constants related to the build target.
 */
public class BuildConfigGenerator {

    public static final String BUILD_CONFIG_NAME = "BuildConfig.java";

    private static final Set<Modifier> PUBLIC_FINAL = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
    private static final Set<Modifier> PUBLIC_STATIC_FINAL =
            EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    private final File mGenFolder;
    private final String mBuildConfigPackageName;

    private final List<ClassField> mFields = Lists.newArrayList();
    private final List<Object> mItems = Lists.newArrayList();

    /**
     * Creates a generator
     * @param genFolder the gen folder of the project
     * @param buildConfigPackageName the package in which to create the class.
     */
    public BuildConfigGenerator(@NonNull File genFolder, @NonNull String buildConfigPackageName) {
        mGenFolder = checkNotNull(genFolder);
        mBuildConfigPackageName = checkNotNull(buildConfigPackageName);
    }

    public BuildConfigGenerator addField(
            @NonNull String type, @NonNull String name, @NonNull String value) {
        mFields.add(AndroidBuilder.createClassField(type, name, value));
        return this;
    }

    public BuildConfigGenerator addItems(@Nullable Collection<Object> items) {
        if (items != null) {
            mItems.addAll(items);
        }
        return this;
    }

    /**
     * Returns a File representing where the BuildConfig class will be.
     */
    public File getFolderPath() {
        return new File(mGenFolder, mBuildConfigPackageName.replace('.', File.separatorChar));
    }

    public File getBuildConfigFile() {
        File folder = getFolderPath();
        return new File(folder, BUILD_CONFIG_NAME);
    }

    /**
     * Generates the BuildConfig class.
     */
    public void generate() throws IOException {
        File pkgFolder = getFolderPath();
        if (!pkgFolder.isDirectory()) {
            if (!pkgFolder.mkdirs()) {
                throw new RuntimeException("Failed to create " + pkgFolder.getAbsolutePath());
            }
        }

        File buildConfigJava = new File(pkgFolder, BUILD_CONFIG_NAME);

        Closer closer = Closer.create();
        try {
            FileOutputStream fos = closer.register(new FileOutputStream(buildConfigJava));
            OutputStreamWriter out = closer.register(new OutputStreamWriter(fos, Charsets.UTF_8));
            JavaWriter writer = closer.register(new JavaWriter(out));

            writer.emitJavadoc("Automatically generated file. DO NOT MODIFY")
                    .emitPackage(mBuildConfigPackageName)
                    .beginType("BuildConfig", "class", PUBLIC_FINAL);

            for (ClassField field : mFields) {
                emitClassField(writer, field);
            }

            for (Object item : mItems) {
                if (item instanceof ClassField) {
                    emitClassField(writer, (ClassField) item);
                } else if (item instanceof String) {
                    writer.emitSingleLineComment((String) item);
                }
            }

            writer.endType();
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static void emitClassField(JavaWriter writer, ClassField field) throws IOException {
        String documentation = field.getDocumentation();
        if (!documentation.isEmpty()) {
            writer.emitJavadoc(documentation);
        }
        for (String annotation : field.getAnnotations()) {
            writer.emitAnnotation(annotation);
        }
        writer.emitField(
                field.getType(),
                field.getName(),
                PUBLIC_STATIC_FINAL,
                field.getValue());
    }
}
