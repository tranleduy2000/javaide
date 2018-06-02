/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.repository;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * Mock implementation of {@link ITaskMonitor} that simply captures
 * the output in local strings. Does not provide any UI and has no
 * support for creating sub-monitors.
 */
public class MockMonitor implements ITaskMonitor {

    String mCapturedLog = "";                                           //$NON-NLS-1$
    String mCapturedErrorLog = "";                                      //$NON-NLS-1$
    String mCapturedVerboseLog = "";                                    //$NON-NLS-1$
    String mCapturedDescriptions = "";                                  //$NON-NLS-1$

    public String getCapturedLog() {
        return mCapturedLog;
    }

    public String getCapturedErrorLog() {
        return mCapturedErrorLog;
    }

    public String getCapturedVerboseLog() {
        return mCapturedVerboseLog;
    }

    public String getCapturedDescriptions() {
        return mCapturedDescriptions;
    }

    public String getAllCapturedLogs() {
        return mCapturedLog + mCapturedVerboseLog + mCapturedDescriptions + mCapturedErrorLog;
    }

    @Override
    public void log(String format, Object... args) {
        mCapturedLog += String.format(format, args) + "\n";             //$NON-NLS-1$
    }

    @Override
    public void logError(String format, Object... args) {
        mCapturedErrorLog += String.format(format, args) + "\n";        //$NON-NLS-1$
    }

    @Override
    public void logVerbose(String format, Object... args) {
        mCapturedVerboseLog += String.format(format, args) + "\n";      //$NON-NLS-1$
    }

    @Override
    public void setProgressMax(int max) {
    }

    @Override
    public int getProgressMax() {
        return 0;
    }

    @Override
    public void setDescription(String format, Object... args) {
        mCapturedDescriptions += String.format(format, args) + "\n";    //$NON-NLS-1$
    }

    @Override
    public boolean isCancelRequested() {
        return false;
    }

    @Override
    public void incProgress(int delta) {
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public boolean displayPrompt(String title, String message) {
        return false;
    }

    @Override
    public ITaskMonitor createSubMonitor(int tickCount) {
        return null;
    }

    @Override
    public void error(@Nullable Throwable t, @Nullable String errorFormat, Object... args) {
    }

    @Override
    public void info(@NonNull String msgFormat, Object... args) {
    }

    @Override
    public void verbose(@NonNull String msgFormat, Object... args) {
    }

    @Override
    public void warning(@NonNull String warningFormat, Object... args) {
    }

    @Override
    public UserCredentials displayLoginCredentialsPrompt(String title, String message) {
        return null;
    }
}
