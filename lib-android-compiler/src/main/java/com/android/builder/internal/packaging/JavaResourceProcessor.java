/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.builder.internal.packaging;


import com.android.builder.packaging.DuplicateFileException;
import com.android.builder.packaging.PackagerException;
import com.android.builder.packaging.SealedPackageException;
import com.android.ide.common.packaging.PackagingUtils;

import java.io.File;
import java.io.IOException;

public class JavaResourceProcessor {

    private final IArchiveBuilder mBuilder;

    public interface IArchiveBuilder {

        /**
         * Adds a file to the archive at a given path
         * @param file the file to add
         * @param archivePath the path of the file inside the APK archive.
         * @throws com.android.builder.packaging.PackagerException if an error occurred
         * @throws com.android.builder.packaging.SealedPackageException if the archive is already sealed.
         * @throws com.android.builder.packaging.DuplicateFileException if a file conflicts with another already added to the APK
         *                                   at the same location inside the APK archive.
         */
        void addFile(File file, String archivePath) throws PackagerException,
                SealedPackageException, DuplicateFileException;
    }


    public JavaResourceProcessor(IArchiveBuilder builder) {
        mBuilder = builder;
    }

    /**
     * Adds the resources from a source folder to a given {@link IArchiveBuilder}
     * @param sourceLocation the source folder.
     * @throws PackagerException if an error occurred
     * @throws SealedPackageException if the APK is already sealed.
     * @throws DuplicateFileException if a file conflicts with another already added to the APK
     *                                   at the same location inside the APK archive.
     */
    public void addSourceFolder(String sourceLocation)
            throws PackagerException, DuplicateFileException, SealedPackageException {
        File sourceFolder = new File(sourceLocation);
        if (sourceFolder.isDirectory()) {
            try {
                // file is a directory, process its content.
                File[] files = sourceFolder.listFiles();
                for (File file : files) {
                    processFileForResource(file, null);
                }
            } catch (DuplicateFileException e) {
                throw e;
            } catch (SealedPackageException e) {
                throw e;
            } catch (Exception e) {
                throw new PackagerException(e, "Failed to add %s", sourceFolder);
            }
        } else {
            // not a directory? check if it's a file or doesn't exist
            if (sourceFolder.exists()) {
                throw new PackagerException("%s is not a folder", sourceFolder);
            }
        }
    }


    /**
     * Processes a {@link File} that could be an APK {@link File}, or a folder containing
     * java resources.
     *
     * @param file the {@link File} to process.
     * @param path the relative path of this file to the source folder.
     *          Can be <code>null</code> to identify a root file.
     * @throws IOException
     * @throws DuplicateFileException if a file conflicts with another already added
     *          to the APK at the same location inside the APK archive.
     * @throws PackagerException if an error occurred
     * @throws SealedPackageException if the APK is already sealed.
     */
    private void processFileForResource(File file, String path)
            throws IOException, DuplicateFileException, PackagerException, SealedPackageException {
        if (file.isDirectory()) {
            // a directory? we check it
            if (PackagingUtils.checkFolderForPackaging(file.getName())) {
                // if it's valid, we append its name to the current path.
                if (path == null) {
                    path = file.getName();
                } else {
                    path = path + "/" + file.getName();
                }

                // and process its content.
                File[] files = file.listFiles();
                for (File contentFile : files) {
                    processFileForResource(contentFile, path);
                }
            }
        } else {
            // a file? we check it to make sure it should be added
            if (PackagingUtils.checkFileForPackaging(file.getName())) {
                // we append its name to the current path
                if (path == null) {
                    path = file.getName();
                } else {
                    path = path + "/" + file.getName();
                }

                // and add it to the apk
                mBuilder.addFile(file, path);
            }
        }
    }
}
