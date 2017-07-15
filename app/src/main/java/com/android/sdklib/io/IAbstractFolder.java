/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.sdklib.io;

/**
 *  A folder.
 */
public interface IAbstractFolder extends IAbstractResource {
    /**
     * Instances of classes that implement this interface are used to
     * filter filenames.
     */
    public interface FilenameFilter {
        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param   dir    the directory in which the file was found.
         * @param   name   the name of the file.
         * @return  <code>true</code> if and only if the name should be
         * included in the file list; <code>false</code> otherwise.
         */
        boolean accept(IAbstractFolder dir, String name);
    }

    /**
     * Returns true if the receiver contains a file with a given name
     * @param name the name of the file. This is the name without the path leading to the
     * parent folder.
     */
    boolean hasFile(String name);

    /**
     * returns an {@link IAbstractFile} representing a child of the current folder with the
     * given name. The file may not actually exist.
     * @param name the name of the file.
     */
    IAbstractFile getFile(String name);

    /**
     * returns an {@link IAbstractFolder} representing a child of the current folder with the
     * given name. The folder may not actually exist.
     * @param name the name of the folder.
     */
    IAbstractFolder getFolder(String name);

    /**
     * returns a list of existing members in this folder.
     */
    IAbstractResource[] listMembers();

    String[] list(FilenameFilter filter);
}
