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

package com.android.sdklib.internal.repository.updater;

import com.android.annotations.NonNull;
import com.android.sdklib.SdkManager;
import com.android.sdklib.SdkManagerTestCase;
import com.android.sdklib.internal.repository.packages.MockEmptyPackage;
import com.android.sdklib.mock.MockLog;
import com.android.sdklib.repository.PkgProps;
import com.android.utils.IReaderLogger;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class UpdaterDataTest extends SdkManagerTestCase {

    private static class MockReaderLogger implements IReaderLogger {
        private final MockLog mLog;
        private final LinkedList<String> mInputLines = new LinkedList<String>();

        public MockReaderLogger(MockLog log, String...inputLines) {
            mLog = log;
            mInputLines.addAll(Arrays.asList(inputLines));
        }

        @Override
        public void warning(String msgFormat, Object... args) {
            mLog.warning(msgFormat, args);
        }

        @Override
        public void verbose(String msgFormat, Object... args) {
            mLog.verbose(msgFormat, args);
        }

        @Override
        public void info(String msgFormat, Object... args) {
            mLog.info(msgFormat, args);
        }

        @Override
        public void error(Throwable t, String msgFormat, Object... args) {
            mLog.error(t, msgFormat, args);
        }

        @Override
        public int readLine(byte[] inputBuffer) throws IOException {
            if (!mInputLines.isEmpty()) {
                String line = mInputLines.remove();
                byte[] bytes = line.getBytes(Charsets.UTF_8);
                int len = bytes.length;
                if (inputBuffer.length < len) {
                    len = inputBuffer.length;
                }
                System.arraycopy(bytes, 0, inputBuffer, 0, len);
                for (int a = len, n = inputBuffer.length; a < n; a++) {
                    inputBuffer[a] = 0;
                }
                return len;
            }
            return 0;
        }

        public String[] getUnreadInput() {
            return mInputLines.toArray(new String[mInputLines.size()]);
        }

        // -- from mock log --

        @Override
        public String toString() {
            return mLog.toString();
        }

        @NonNull
        public List<String> getMessages() {
            return mLog.getMessages();
        }

        public void clear() {
            mLog.clear();
        }
    }

    public final void testAcceptLicenses_Empty() {
        SdkManager sdkman = getSdkManager();
        MockReaderLogger inputLog = new MockReaderLogger(new MockLog(), "");
        UpdaterData data = new UpdaterData(sdkman.getLocation(), inputLog);
        String acceptLicenses = null;
        List<ArchiveInfo> archives = new ArrayList<ArchiveInfo>();
        data.acceptLicense(archives , acceptLicenses, 3);
        assertTrue(archives.isEmpty());
        assertEquals("[]", Arrays.toString(inputLog.getMessages().toArray()));
        assertEquals("[]", Arrays.toString(inputLog.getUnreadInput()));
    }

    public final void testAcceptLicenses_NoAnswer() {
        SdkManager sdkman = getSdkManager();
        MockReaderLogger inputLog = new MockReaderLogger(new MockLog(), "");
        UpdaterData data = new UpdaterData(sdkman.getLocation(), inputLog);
        String acceptLicenses = null;
        List<ArchiveInfo> infos = new ArrayList<ArchiveInfo>();

        Properties props = new Properties();
        props.setProperty(PkgProps.PKG_LICENSE_REF, "sdk-license");
        props.setProperty(PkgProps.PKG_LICENSE, "This is the license text.\nEtc etc.\n");
        MockEmptyPackage p = new MockEmptyPackage("test", props);
        infos.add(new ArchiveInfo(p.getLocalArchive(), null, null));

        assertEquals("[MockEmptyPackage 'test']", Arrays.toString(infos.toArray()));
        data.acceptLicense(infos , acceptLicenses, 3);
        assertEquals(
                "[P -------------------------------\n" +
                ", P License id: sdk-license-dddb8a39\n" +
                ", P Used by: \n" +
                " - MockEmptyPackage 'test'\n" +
                ", P -------------------------------\n" +
                "\n" +
                ", P This is the license text.\n" +
                "Etc etc.\n" +
                "\n" +
                ", P Do you accept the license 'sdk-license-dddb8a39' [y/n]: , P \n" +
                ", P Unknown response ''.\n" +
                ", P Do you accept the license 'sdk-license-dddb8a39' [y/n]: , P \n" +
                ", P Unknown response ''.\n" +
                ", P Do you accept the license 'sdk-license-dddb8a39' [y/n]: , P \n" +
                ", P Unknown response ''.\n" +
                ", P Max number of retries exceeded. Rejecting 'sdk-license-dddb8a39'\n" +
                ", P Package MockEmptyPackage 'test' not installed due to rejected license 'sdk-license-dddb8a39'.\n" +
                "]", Arrays.toString(inputLog.getMessages().toArray()));
        assertEquals("[]", Arrays.toString(infos.toArray()));
        assertEquals("[]", Arrays.toString(inputLog.getUnreadInput()));
    }


    public final void testAcceptLicenses_Answer() {
        SdkManager sdkman = getSdkManager();
        MockReaderLogger inputLog = new MockReaderLogger(new MockLog(), "yes");
        UpdaterData data = new UpdaterData(sdkman.getLocation(), inputLog);
        String acceptLicenses = null;
        List<ArchiveInfo> infos = new ArrayList<ArchiveInfo>();

        Properties props = new Properties();
        props.setProperty(PkgProps.PKG_LICENSE_REF, "sdk-license");
        props.setProperty(PkgProps.PKG_LICENSE, "This is the license text.\nEtc etc.\n");
        MockEmptyPackage p = new MockEmptyPackage("test", props);
        infos.add(new ArchiveInfo(p.getLocalArchive(), null, null));

        assertEquals("[MockEmptyPackage 'test']", Arrays.toString(infos.toArray()));
        data.acceptLicense(infos , acceptLicenses, 3);
        assertEquals(
                "[P -------------------------------\n" +
                ", P License id: sdk-license-dddb8a39\n" +
                ", P Used by: \n" +
                " - MockEmptyPackage 'test'\n" +
                ", P -------------------------------\n" +
                "\n" +
                ", P This is the license text.\n" +
                "Etc etc.\n" +
                "\n" +
                ", P Do you accept the license 'sdk-license-dddb8a39' [y/n]: , P \n" +
                "]", Arrays.toString(inputLog.getMessages().toArray()));
        assertEquals("[MockEmptyPackage 'test']", Arrays.toString(infos.toArray()));
        assertEquals("[]", Arrays.toString(inputLog.getUnreadInput()));
    }
}
