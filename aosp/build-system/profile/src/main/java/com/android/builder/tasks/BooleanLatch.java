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

package com.android.builder.tasks;

import com.google.common.base.Objects;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Implementation of a 2 state boolean latch, which can either be signaled or not.
 * Thread can block on the signal using the {@link #await()} method, worker threads can
 * release blocked threads by using the {@link #signal()} method.
 */
public class BooleanLatch {

    private static class Sync extends AbstractQueuedSynchronizer {
        boolean isSignalled() { return getState() != 0; }

        @Override
        protected int tryAcquireShared(int ignore) {
            return isSignalled() ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }
    }

    private final Sync sync = new Sync();
    public boolean isSignalled() { return sync.isSignalled(); }
    public void signal()         { sync.releaseShared(1); }
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }
    public boolean await(long nanosTimeout) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, nanosTimeout);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("signaled", isSignalled())
                .toString();

    }
}