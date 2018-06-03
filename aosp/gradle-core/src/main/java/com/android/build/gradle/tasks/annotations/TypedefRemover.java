/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.tasks.annotations;

import static com.android.SdkConstants.DOT_CLASS;
import static org.objectweb.asm.Opcodes.ASM5;

import com.android.annotations.NonNull;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Finds and deletes typedef annotation classes (and also warns if their
 * retention is wrong, such that usages of the annotation embeds data
 * into the .class file.)
 * <p>
 * (Based on the similar class in {@code development/tools/rmtypedefs/})
 */
@SuppressWarnings("SpellCheckingInspection")
public class TypedefRemover {
    private final Extractor mExtractor;
    private final boolean mQuiet;
    private final boolean mVerbose;
    private final boolean mDryRun;

    public TypedefRemover(
            @NonNull Extractor extractor,
            boolean quiet,
            boolean verbose,
            boolean dryRun) {
        mExtractor = extractor;
        mQuiet = quiet;
        mVerbose = verbose;
        mDryRun = dryRun;
    }

    private Set<String> mAnnotationNames = Sets.newHashSet();
    private List<File> mAnnotationClassFiles = Lists.newArrayList();
    private Set<File> mAnnotationOuterClassFiles = Sets.newHashSet();

    public void remove(@NonNull File classDir, @NonNull List<String> owners) {
        if (!mQuiet) {
            mExtractor.info("Deleting @IntDef and @StringDef annotation class files");
        }

        // Record typedef annotation names and files
        for (String owner : owners) {
            File file = new File(classDir, owner.replace('/', File.separatorChar) + DOT_CLASS);
            addTypeDef(owner, file);
        }

        // Rewrite the .class files for any classes that *contain* typedefs as innerclasses
        rewriteOuterClasses();

        // Removes the actual .class files for the typedef annotations
        deleteAnnotationClasses();
    }

    /**
     * Records the given class name (internal name) and class file path as corresponding to a
     * typedef annotation
     * */
    private void addTypeDef(String name, File file) {
        mAnnotationClassFiles.add(file);
        mAnnotationNames.add(name);

        String fileName = file.getName();
        int index = fileName.lastIndexOf('$');
        if (index != -1) {
            File parentFile = file.getParentFile();
            assert parentFile != null : file;
            File container = new File(parentFile, fileName.substring(0, index) + ".class");
            if (container.exists()) {
                mAnnotationOuterClassFiles.add(container);
            } else {
                Extractor.error("Warning: Could not find outer class " + container
                        + " for typedef " + file);
            }
        }
    }

    /**
     * Rewrites the outer classes containing the typedefs such that they no longer refer to
     * the (now removed) typedef annotation inner classes
     */
    private void rewriteOuterClasses() {
        for (File file : mAnnotationOuterClassFiles) {
            byte[] bytes;
            try {
                bytes = Files.toByteArray(file);
            } catch (IOException e) {
                Extractor.error("Could not read " + file + ": " + e.getLocalizedMessage());
                continue;
            }

            ClassWriter classWriter = new ClassWriter(ASM5);
            ClassVisitor classVisitor = new ClassVisitor(ASM5, classWriter) {
                @Override
                public void visitInnerClass(String name, String outerName, String innerName,
                        int access) {
                    if (!mAnnotationNames.contains(name)) {
                        super.visitInnerClass(name, outerName, innerName, access);
                    }
                }
            };
            ClassReader reader = new ClassReader(bytes);
            reader.accept(classVisitor, 0);
            byte[] rewritten = classWriter.toByteArray();
            try {
                Files.write(rewritten, file);
            } catch (IOException e) {
                Extractor.error("Could not write " + file + ": " + e.getLocalizedMessage());
                //noinspection UnnecessaryContinue
                continue;
            }
        }
    }

    /**
     * Performs the actual deletion (or display, if in dry-run mode) of the typedef annotation
     * files
     */
    private void deleteAnnotationClasses() {
        for (File mFile : mAnnotationClassFiles) {
            if (mVerbose) {
                if (mDryRun) {
                    mExtractor.info("Would delete " + mFile);
                } else {
                    mExtractor.info("Deleting " + mFile);
                }
            }
            if (!mDryRun) {
                boolean deleted = mFile.delete();
                if (!deleted) {
                    Extractor.warning("Could not delete " + mFile);
                }
            }
        }
    }
}