/*
 * Copyright (C) 2011 The Android Open Source Project
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
import com.android.annotations.Nullable;
import com.android.utils.ILogger;
import com.google.common.io.Files;
import junit.framework.TestCase;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class KeyStoreHelperTest extends TestCase {

    public void testCreateAndCheckKey() throws Exception {
        File tempFolder = Files.createTempDir();
        File keystoreFile = new File(tempFolder, "debug.keystore");
        keystoreFile.deleteOnExit();

        FakeLogger fakeLogger = new FakeLogger();

        // "now" is just slightly before the key was created
        long now = System.currentTimeMillis();

        // create the keystore
        KeystoreHelper.createDebugStore(null, keystoreFile, "android", "android", "AndroidDebugKey",
                                        fakeLogger);

        // read the key back
        CertificateInfo certificateInfo = KeystoreHelper.getCertificateInfo(
            null, keystoreFile, "android", "android", "AndroidDebugKey");

        assertNotNull(certificateInfo);

        assertEquals("", fakeLogger.getErr());

        PrivateKey key = certificateInfo.getKey();
        assertNotNull(key);

        X509Certificate certificate = certificateInfo.getCertificate();
        assertNotNull(certificate);

        // The "not-after" (a.k.a. expiration) date should be after "now"
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        assertTrue(certificate.getNotAfter().compareTo(c.getTime()) > 0);

        // It should be valid after 1 year from now (adjust by a second since the 'now' time
        // doesn't exactly match the creation time... 1 second should be enough.)
        c.add(Calendar.DAY_OF_YEAR, 365);
        c.add(Calendar.SECOND, -1);
        assertTrue("1 year expiration failed",
                certificate.getNotAfter().compareTo(c.getTime()) > 0);

        // and 30 years from now
        c.add(Calendar.DAY_OF_YEAR, 29 * 365);
        // remove 1 hour to handle for PST/PDT issue
        c.add(Calendar.HOUR_OF_DAY, -1);
        assertTrue("30 year expiration failed",
                certificate.getNotAfter().compareTo(c.getTime()) > 0);

        // however expiration date should be passed in 30 years + a few hours
        c.add(Calendar.HOUR, 5);
        assertFalse("30 year and few hours expiration failed",
                certificate.getNotAfter().compareTo(c.getTime()) > 0);
    }

    private static class FakeLogger implements ILogger {
        private String mOut = "";
        private String mErr = "";

        public String getOut() {
            return mOut;
        }

        public String getErr() {
            return mErr;
        }

        @Override
        public void error(@Nullable Throwable t, @Nullable String msgFormat, Object... args) {
            String message = msgFormat != null ?
                    String.format(msgFormat, args) :
                    t != null ? t.getClass().getCanonicalName() : "ERROR!";
            mErr += message + "\n";
        }

        @Override
        public void warning(@NonNull String msgFormat, Object... args) {
            String message = String.format(msgFormat, args);
            mOut += message + "\n";
        }

        @Override
        public void info(@NonNull String msgFormat, Object... args) {
            String message = String.format(msgFormat, args);
            mOut += message + "\n";
        }

        @Override
        public void verbose(@NonNull String msgFormat, Object... args) {
            String message = String.format(msgFormat, args);
            mOut += message + "\n";
        }
    }
}
