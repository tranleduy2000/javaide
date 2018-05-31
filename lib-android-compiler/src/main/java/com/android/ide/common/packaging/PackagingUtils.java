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
                !"aidl".equalsIgnoreCase(extension) &&        // Aidl files
                !"rs".equalsIgnoreCase(extension) &&          // RenderScript files
                !"fs".equalsIgnoreCase(extension) &&          // FilterScript files
                !"rsh".equalsIgnoreCase(extension) &&         // RenderScript header files
                !"d".equalsIgnoreCase(extension) &&           // Dependency files
                !"java".equalsIgnoreCase(extension) &&        // Java files
                !"scala".equalsIgnoreCase(extension) &&       // Scala files
                !"class".equalsIgnoreCase(extension) &&       // Java class files
                !"scc".equalsIgnoreCase(extension) &&         // VisualSourceSafe
                !"swp".equalsIgnoreCase(extension) &&         // vi swap file
                !"thumbs.db".equalsIgnoreCase(fileName) &&    // image index file
                !"picasa.ini".equalsIgnoreCase(fileName) &&   // image index file
                !"about.html".equalsIgnoreCase(fileName) &&   // Javadoc
                !"package.html".equalsIgnoreCase(fileName) && // Javadoc
                !"overview.html".equalsIgnoreCase(fileName);  // Javadoc
    }
}
