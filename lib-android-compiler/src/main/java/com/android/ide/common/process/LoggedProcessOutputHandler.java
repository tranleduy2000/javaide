/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.ide.common.process;

import com.android.annotations.NonNull;
import com.android.utils.ILogger;

/**
 * Implementation of ProcessOutputHandler that dumps the output onto an ILogger object.
 */
public class LoggedProcessOutputHandler extends BaseProcessOutputHandler {

    @NonNull
    private final ILogger mLogger;

    public LoggedProcessOutputHandler(@NonNull ILogger logger) {
        mLogger = logger;
    }

    @Override
    public void handleOutput(@NonNull ProcessOutput processOutput) throws ProcessException {
        if (processOutput instanceof BaseProcessOutput) {
            BaseProcessOutput impl = (BaseProcessOutput) processOutput;
            String stdout = impl.getStandardOutputAsString();
            if (!stdout.isEmpty()) {
                mLogger.info(stdout);
            }
            String stderr = impl.getErrorOutputAsString();
            if (!stderr.isEmpty()) {
                mLogger.error(null, stderr);
            }
        } else {
            throw new IllegalArgumentException("processOutput was not created by this handler.");
        }
    }
}
