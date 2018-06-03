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

package com.android.build.gradle.internal.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.model.SigningConfig;
import com.android.ide.common.signing.CertificateInfo;
import com.android.ide.common.signing.KeystoreHelper;
import com.android.ide.common.signing.KeytoolException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.StyledTextOutput;
import org.gradle.logging.StyledTextOutputFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.gradle.logging.StyledTextOutput.Style.Description;
import static org.gradle.logging.StyledTextOutput.Style.Failure;
import static org.gradle.logging.StyledTextOutput.Style.Identifier;
import static org.gradle.logging.StyledTextOutput.Style.Normal;

/**
 * Report tasks displaying the signing information for all variants.
 */
public class SigningReportTask extends DefaultTask {

    private Set<BaseVariantData> variants = Sets.newHashSet();

    @TaskAction
    public void generate() throws IOException {

        StyledTextOutput textOutput = getServices().get(
                StyledTextOutputFactory.class).create(getClass());

        Map<SigningConfig, SigningInfo> cache = Maps.newHashMap();

        for (BaseVariantData variant : variants) {
            textOutput.withStyle(Identifier).text("Variant: ");
            textOutput.withStyle(Description).text(variant.getName());
            textOutput.println();

            // get the data
            SigningConfig signingConfig = variant.getVariantConfiguration().getSigningConfig();
            if (signingConfig == null) {
                textOutput.withStyle(Identifier).text("Config: ");
                textOutput.withStyle(Normal).text("none");
                textOutput.println();
            } else {
                SigningInfo signingInfo = getSigningInfo(signingConfig, cache);


                textOutput.withStyle(Identifier).text("Config: ");
                textOutput.withStyle(Description).text(signingConfig.getName());
                textOutput.println();

                textOutput.withStyle(Identifier).text("Store: ");
                textOutput.withStyle(Description).text(signingConfig.getStoreFile());
                textOutput.println();

                textOutput.withStyle(Identifier).text("Alias: ");
                textOutput.withStyle(Description).text(signingConfig.getKeyAlias());
                textOutput.println();

                if (signingInfo.isValid()) {
                    if (signingInfo.error != null) {
                        textOutput.withStyle(Identifier).text("Error: ");
                        textOutput.withStyle(Failure).text(signingInfo.error);
                        textOutput.println();
                    } else {
                        textOutput.withStyle(Identifier).text("MD5: ");
                        textOutput.withStyle(Description).text(signingInfo.md5);
                        textOutput.println();

                        textOutput.withStyle(Identifier).text("SHA1: ");
                        textOutput.withStyle(Description).text(signingInfo.sha1);
                        textOutput.println();

                        textOutput.withStyle(Identifier).text("Valid until: ");
                        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
                        textOutput.withStyle(Description).text(df.format(signingInfo.notAfter));
                        textOutput.println();
                    }
                }
            }

            textOutput.withStyle(Normal).text("----------");
            textOutput.println();
        }
    }

    /**
     * Sets the configurations to generate the report for.
     */
    public void setVariants(@NonNull Collection<? extends BaseVariantData> variants) {
        this.variants.addAll(variants);
    }

    private static SigningInfo getSigningInfo(
            @NonNull SigningConfig signingConfig,
            @NonNull Map<SigningConfig, SigningInfo> cache) {
        SigningInfo signingInfo = cache.get(signingConfig);

        if (signingInfo == null) {
            signingInfo = new SigningInfo();

            if (signingConfig.isSigningReady()) {
                try {
                    CertificateInfo certificateInfo = KeystoreHelper.getCertificateInfo(
                            signingConfig.getStoreType(), signingConfig.getStoreFile(),
                            signingConfig.getStorePassword(), signingConfig.getKeyPassword(),
                            signingConfig.getKeyAlias());
                    if (certificateInfo != null) {
                        signingInfo.md5 = getFingerprint(certificateInfo.getCertificate(), "MD5");
                        signingInfo.sha1 = getFingerprint(certificateInfo.getCertificate(), "SHA1");
                        signingInfo.notAfter = certificateInfo.getCertificate().getNotAfter();
                    }
                } catch (KeytoolException e) {
                    signingInfo.error = e.getMessage();
                } catch (FileNotFoundException e) {
                    signingInfo.error = "Missing keystore";
                }
            }

            cache.put(signingConfig, signingInfo);
        }

        return signingInfo;
    }

    private static final class SigningInfo {
        String md5;
        String sha1;
        Date notAfter;
        String error;

        boolean isValid() {
            return md5 != null || error != null;
        }
    }

    /**
     * Returns the {@link Certificate} fingerprint as returned by <code>keytool</code>.
     */
    public static String getFingerprint(Certificate cert, String hashAlgorithm) {
        if (cert == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            return toHexadecimalString(digest.digest(cert.getEncoded()));
        } catch(NoSuchAlgorithmException e) {
            // ignore
        } catch(CertificateEncodingException e) {
            // ignore
        }
        return null;
    }

    private static String toHexadecimalString(byte[] value) {
        StringBuilder sb = new StringBuilder();
        int len = value.length;
        for (int i = 0; i < len; i++) {
            int num = ((int) value[i]) & 0xff;
            if (num < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(num));
            if (i < len - 1) {
                sb.append(':');
            }
        }
        return sb.toString().toUpperCase(Locale.US);
    }
}
