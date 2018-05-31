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

package com.android.ide.common.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton executor service.
 */
public class ExecutorSingleton {

    private static ExecutorService sExecutorService = create();

    public static synchronized ExecutorService getExecutor() {
        checkExecutor();
        return sExecutorService;
    }

    public static synchronized void shutdown() {
        if (sExecutorService != null) {
            sExecutorService.shutdown();
            sExecutorService = null;
        }
    }

    public static synchronized void restart() {
        shutdown();
        sExecutorService = create();
    }

    private static void checkExecutor() {
        if (sExecutorService == null) {
            throw new RuntimeException("Executor Singleton not started");
        }
    }

    private static ExecutorService create() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
