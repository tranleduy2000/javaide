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

package com.android.utils;

import static com.google.common.base.Preconditions.checkArgument;

import com.android.annotations.NonNull;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static void deleteFolder(final File folder) throws IOException {
        if (!folder.exists()) {
            return;
        }
        File[] files = folder.listFiles();
        if (files != null) { // i.e. is a directory.
            for (final File file : files) {
                deleteFolder(file);
            }
        }
        if (!folder.delete()) {
            throw new IOException(String.format("Could not delete folder %s", folder));
        }
    }

    public static void emptyFolder(final File folder) throws IOException {
        deleteFolder(folder);
        if (!folder.mkdirs()) {
            throw new IOException(String.format("Could not create empty folder %s", folder));
        }
    }

    public static void copyFile(File from, File to) throws IOException {
        to = new File(to, from.getName());
        if (from.isDirectory()) {
            if (!to.exists()) {
                if (!to.mkdirs()) {
                    throw new IOException(String.format("Could not create directory %s", to));
                }
            }

            File[] children = from.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyFile(child, to);
                }
            }
        } else if (from.isFile()) {
            Files.copy(from, to);
        }
    }

    public static File join(File dir, String... paths) {
        return new File(dir, Joiner.on(File.separatorChar).join(paths));
    }

    public static String relativePath(@NonNull File file, @NonNull File dir) {
        checkArgument(file.isFile(), "%s is not a file.", file.getPath());
        checkArgument(dir.isDirectory(), "%s is not a directory.", dir.getPath());

        return dir.toURI().relativize(file.toURI()).getPath();
    }

    public static String sha1(@NonNull File file) throws IOException {
        return Hashing.sha1().hashBytes(Files.toByteArray(file)).toString();
    }

    public static String getNamesAsCommaSeparatedList(Iterable<File> files) {
        return Joiner.on(", ").join(Iterables.transform(files, GET_NAME));
    }

    private static final Function<File, String> GET_NAME = new Function<File, String>() {
        @Override
        public String apply(File file) {
            return file.getName();
        }
    };
}
