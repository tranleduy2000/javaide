/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.zip;

import java.util.zip.ZipException;

/**
 * General format of extra field data. <p>
 *
 * Extra fields usually appear twice per file, once in the local file data and
 * once in the central directory. Usually they are the same, but they don't have
 * to be. {@link java.util.zip.ZipOutputStream java.util.zip.ZipOutputStream}
 * will only use the local file data in both places.</p>
 */
public interface ZipExtraField {
    /**
     * The Header-ID.
     *
     * @return The HeaderId value
     */
    ZipShort getHeaderId();

    /**
     * Length of the extra field in the local file data - without Header-ID or
     * length specifier.
     *
     * @return The LocalFileDataLength value
     */
    ZipShort getLocalFileDataLength();

    /**
     * Length of the extra field in the central directory - without Header-ID or
     * length specifier.
     *
     * @return The CentralDirectoryLength value
     */
    ZipShort getCentralDirectoryLength();

    /**
     * The actual data to put into local file data - without Header-ID or length
     * specifier.
     *
     * @return The LocalFileDataData value
     */
    byte[] getLocalFileDataData();

    /**
     * The actual data to put into central directory - without Header-ID or
     * length specifier.
     *
     * @return The CentralDirectoryData value
     */
    byte[] getCentralDirectoryData();

    /**
     * Populate data from this array as if it was in local file data.
     *
     * @param buffer the buffer to read data from
     * @param offset offset into buffer to read data
     * @param length the length of data
     * @exception ZipException on error
     */
    void parseFromLocalFileData(byte[] buffer, int offset, int length)
        throws ZipException;

    /**
     * Populate data from this array as if it was in central directory data.
     *
     * @param buffer the buffer to read data from
     * @param offset offset into buffer to read data
     * @param length the length of data
     * @exception ZipException on error
     */
    void parseFromCentralDirectoryData(byte[] buffer, int offset, int length)
        throws ZipException;
}
