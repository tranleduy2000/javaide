/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.ide.common.packaging;

import com.google.common.collect.ImmutableList;

/**
 * Utility class for packaging.
 */
public class PackagingUtils {

    /**
     * Checks whether a folder and its content is valid for packaging into the .apk as
     * standard Java resource.
     * @param folderName the name of the folder.
     *
     * @return true if the folder is valid for packaging.
     */
    public static boolean checkFolderForPackaging(String folderName) {
        return !folderName.equalsIgnoreCase("CVS") &&
                !folderName.equalsIgnoreCase(".svn") &&
                !folderName.equalsIgnoreCase("SCCS") &&
                !folderName.startsWith("_");
    }

    /**
     * Checks a file to make sure it should be packaged as standard resources.
     * @param fileName the name of the file (including extension)
     * @return true if the file should be packaged as standard java resources.
     */
    public static boolean checkFileForPackaging(String fileName) {
        String[] fileSegments = fileName.split("\\.");
        String fileExt = "";
        if (fileSegments.length > 1) {
            fileExt = fileSegments[fileSegments.length-1];
        }

        return checkFileForPackaging(fileName, fileExt);
    }

    /**
     * Checks a file to make sure it should be packaged as standard resources.
     * @param fileName the name of the file (including extension)
     * @param extension the extension of the file (excluding '.')
     * @return true if the file should be packaged as standard java resources.
     */
    public static boolean checkFileForPackaging(String fileName, String extension) {
        // ignore hidden files and backup files
        return !(fileName.charAt(0) == '.' || fileName.charAt(fileName.length() - 1) == '~') &&
                !isOfNonResourcesExtensions(extension) &&
                !isNotAResourceFile(fileName);
    }

    private static boolean isOfNonResourcesExtensions(String extension) {
        for (String ext : NON_RESOURCES_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotAResourceFile(String fileName) {
        for (String name : NON_RESOURCES_FILENAMES) {
            if (name.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of file extensions that represents non resources files.
     */
    public static final ImmutableList<String> NON_RESOURCES_EXTENSIONS =
            ImmutableList.<String>builder()
                    .add("aidl")            // Aidl files
                    .add("rs")              // RenderScript files
                    .add("fs")              // FilterScript files
                    .add("rsh")             // RenderScript header files
                    .add("d")               // Dependency files
                    .add("java")            // Java files
                    .add("scala")           // Scala files
                    .add("class")           // Java class files
                    .add("so")              // native .so libraries
                    .add("scc")             // VisualSourceSafe
                    .add("swp")             // vi swap file
                    .build();

    /**
     * Return file names that are not resource files.
     */
    public static final ImmutableList<String> NON_RESOURCES_FILENAMES =
            ImmutableList.<String>builder()
                    .add("thumbs.db")       // image index file
                    .add("picasa.ini")      // image index file
                    .add("about.html")      // Javadoc
                    .add("package.html")    // Javadoc
                    .add("overview.html")   // Javadoc
                    .build();
}
