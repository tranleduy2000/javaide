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

package com.android.sdklib.internal.repository;

import com.android.sdklib.ISdkLog;
import com.android.sdklib.NullSdkLog;
import com.android.util.Pair;


/**
 * A no-op implementation of the {@link ITaskMonitor} interface.
 * <p/>
 * This can be passed to methods that require a monitor when the caller doesn't
 * have any UI to update or means to report tracked progress.
 * A custom {@link ISdkLog} is used. Clients could use {@link NullSdkLog} if
 * they really don't care about the logging either.
 */
public class NullTaskMonitor implements ITaskMonitor {

    private final ISdkLog mLog;

    /**
     * Creates a no-op {@link ITaskMonitor} that defers logging to the specified
     * logger.
     * <p/>
     * This can be passed to methods that require a monitor when the caller doesn't
     * have any UI to update or means to report tracked progress.
     *
     * @param log An {@link ISdkLog}. Must not be null. Consider using {@link NullSdkLog}.
     */
    public NullTaskMonitor(ISdkLog log) {
        mLog = log;
    }

    public void setDescription(String format, Object...args) {
        // pass
    }

    public void log(String format, Object...args) {
        mLog.printf(format, args);
    }

    public void logError(String format, Object...args) {
        mLog.error(null /*throwable*/, format, args);
    }

    public void logVerbose(String format, Object...args) {
        mLog.printf(format, args);
    }

    public void setProgressMax(int max) {
        // pass
    }

    public int getProgressMax() {
        return 0;
    }

    public void incProgress(int delta) {
        // pass
    }

    /** Always return 1. */
    public int getProgress() {
        return 1;
    }

    /** Always return false. */
    public boolean isCancelRequested() {
        return false;
    }

    public ITaskMonitor createSubMonitor(int tickCount) {
        return this;
    }

    /** Always return false. */
    public boolean displayPrompt(final String title, final String message) {
        return false;
    }

    /** Always return null. */
    public Pair<String, String> displayLoginPasswordPrompt(String title, String message) {
        return null;
    }

    // --- ISdkLog ---

    public void error(Throwable t, String errorFormat, Object... args) {
        mLog.error(t, errorFormat, args);
    }

    public void warning(String warningFormat, Object... args) {
        mLog.warning(warningFormat, args);
    }

    public void printf(String msgFormat, Object... args) {
        mLog.printf(msgFormat, args);
    }

}
