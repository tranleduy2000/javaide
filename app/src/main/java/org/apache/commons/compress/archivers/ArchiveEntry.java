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
package org.apache.commons.compress.archivers;

import java.util.Date;

/**
 * Represents an entry of an archive.
 */
public interface ArchiveEntry {

    /** The name of the entry in the archive. May refer to a file or directory or other item */
    public String getName();

    /** The (uncompressed) size of the entry. May be -1 (SIZE_UNKNOWN) if the size is unknown */
    public long getSize();

    /** Special value indicating that the size is unknown */
    public static final long SIZE_UNKNOWN = -1;

    /** True if the entry refers to a directory */
    public boolean isDirectory();

    /**
     * The last modified date of the entry.
     * 
     * @since Apache Commons Compress 1.1
     */
    public Date getLastModifiedDate();
}
