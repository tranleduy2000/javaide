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
package org.apache.commons.compress.archivers.jar;

import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 *
 * @NotThreadSafe
 */
public class JarArchiveEntry extends ZipArchiveEntry implements ArchiveEntry {

    private Attributes manifestAttributes = null;
    private Certificate[] certificates = null;

    public JarArchiveEntry(ZipEntry entry) throws ZipException {
        super(entry);
    }

    public JarArchiveEntry(String name) {
        super(name);
    }

    public JarArchiveEntry(ZipArchiveEntry entry) throws ZipException {
        super(entry);
    }

    public JarArchiveEntry(JarEntry entry) throws ZipException {
        super(entry);

    }

    public Attributes getManifestAttributes() {
        return manifestAttributes;
    }

    public Certificate[] getCertificates() {
            if (certificates != null) {
                Certificate[] certs = new Certificate[certificates.length];
                System.arraycopy(certificates, 0, certs, 0, certs.length);
                return certs;
            }
            return null;
    }

}
