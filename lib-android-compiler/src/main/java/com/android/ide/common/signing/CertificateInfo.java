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

package com.android.ide.common.signing;

import com.android.annotations.NonNull;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Signing information.
 *
 * Both the {@link PrivateKey} and the {@link X509Certificate} are guaranteed to be non-null.
 *
 */
public class CertificateInfo {
    public final PrivateKey mKey;
    public final X509Certificate mCertificate;

    public CertificateInfo(@NonNull PrivateKey key, @NonNull X509Certificate certificate) {
        mKey = checkNotNull(key, "Key cannot be null.");
        mCertificate = checkNotNull(certificate, "Certificate cannot be null.");
    }

    public PrivateKey getKey() {
        return mKey;
    }

    public X509Certificate getCertificate() {
        return mCertificate;
    }
}
