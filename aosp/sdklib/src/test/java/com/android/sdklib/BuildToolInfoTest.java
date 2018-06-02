/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sdklib;

import com.android.annotations.Nullable;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.NoPreviewRevision;
import com.android.utils.ILogger;

import java.util.Properties;

public class BuildToolInfoTest extends SdkManagerTestCase {

    /**
     * Wraps an *existing* build-tool info object to expose some of its internals for testing.
     */
    public static class BuildToolInfoWrapper extends BuildToolInfo {

        private final BuildToolInfo mInfo;
        private NoPreviewRevision mOverrideJvmVersion;

        public BuildToolInfoWrapper(BuildToolInfo info) {
            super(info.getRevision(), info.getLocation());
            mInfo = info;
        }

        @Override
        public String getPath(PathId pathId) {
            return mInfo.getPath(pathId);
        }

        @Override
        public Properties getRuntimeProps() {
            return mInfo.getRuntimeProps();
        }

        @Override
        public boolean isValid(ILogger log) {
            return mInfo.isValid(log);
        }

        @Override
        public boolean canRunOnJvm() {
            // This runs canRunOnJvm on *this* instance so that it can
            // access the overridden getCurrentJvmVersion below rather than
            // the original that we are wrapping.
            return super.canRunOnJvm();
        }

        @Override
        protected NoPreviewRevision getCurrentJvmVersion() throws NumberFormatException {
            if (mOverrideJvmVersion != null) {
                return mOverrideJvmVersion;
            }
            return mInfo.getCurrentJvmVersion();
        }

        public void overrideJvmVersion(@Nullable NoPreviewRevision jvmVersion) {
            mOverrideJvmVersion = jvmVersion;
        }
    }

    public void testGetCurrentJvmVersion() {
        SdkManager sdkman = getSdkManager();
        BuildToolInfo bt = sdkman.getBuildTool(new FullRevision(18, 3, 4, 5));
        assertNotNull(bt);

        // Check the actual JVM running this test.
        NoPreviewRevision curr = bt.getCurrentJvmVersion();
        // We can reasonably expect this to at least run with JVM 1.5 or more
        assertTrue(curr.compareTo(new FullRevision(1, 5, 0)) > 0);
        // and we can reasonably expect to not be running with JVM 42.0.0
        assertTrue(curr.compareTo(new FullRevision(42, 0, 0)) < 0);
    }

}
