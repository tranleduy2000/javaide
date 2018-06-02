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

package com.example.android.render;

import com.android.ide.common.log.ILogger;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.sdklib.ISdkLog;

/**
 * Class implementing the 3 different log interface we use!
 *
 * At least ILogger and ISdkLog are identical...
 *
 */
public class StdOutLogger extends LayoutLog implements ILogger, ISdkLog {

    // LayoutLog

    @Override
    public void error(String tag, String message, Object data) {
        if (tag != null) {
            System.err.println("ERROR: [" + tag + "] " +  message);
        } else {
            System.err.println("ERROR: " +  message);
        }
    }

    @Override
    public void error(String tag, String message, Throwable throwable, Object data) {
        error(tag, message, data);
        throwable.printStackTrace();
    }

    @Override
    public void fidelityWarning(String tag, String message, Throwable throwable, Object data) {
        if (tag != null) {
            System.out.println("warning: [" + tag + "] " +  message);
        } else {
            System.out.println("warning: " +  message);
        }
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void warning(String tag, String message, Object data) {
        fidelityWarning(tag, message, null /*throwable*/, data);
    }

    // ILogger / ISdkLog

    public void error(Throwable t, String errorFormat, Object... args) {
        error(null /*tag*/, String.format(errorFormat, args), t, null /*data*/);
    }

    public void printf(String msgFormat, Object... args) {
        System.out.println(String.format(msgFormat, args));
    }

    public void warning(String warningFormat, Object... args) {
        warning(null /*tag*/, String.format(warningFormat, args), null /*data*/);
    }
}
