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

    private static ExecutorService sExecutorService;

    private static int sThreadPoolSize = Runtime.getRuntime().availableProcessors();

    public static synchronized ExecutorService getExecutor() {
        if (sExecutorService == null) {
            sExecutorService = Executors.newFixedThreadPool(sThreadPoolSize);
        }

        return sExecutorService;
    }

    public static synchronized void shutdown() {
        if (sExecutorService != null) {
            sExecutorService.shutdown();
            sExecutorService = null;
        }
    }

    /**
     * Changes the thread pool size for the singleton ExecutorService.
     *
     * <b>Caution</b>: This will have no effect if getExecutor() has already been called until the
     * executor is shutdown and reinitialized.
     *
     * @param threadPoolSize the number of threads to use.
     */
    public static void setThreadPoolSize(int threadPoolSize) {
        sThreadPoolSize = threadPoolSize;
    }
}
