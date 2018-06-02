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
package com.android.ide.common.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A utility wrapper around a {@link CompletionService} using an ThreadPoolExecutor so that it
 * is possible to wait on all the tasks.
 *
 * Tasks are submitted as {@link Callable} with {@link #execute(java.util.concurrent.Callable)}.
 *
 * After executing all tasks, it is possible to wait on them with
 * {@link #waitForTasksWithQuickFail(boolean)}, or {@link #waitForAllTasks()}.
 *
 * This class is not Thread safe!
 */
public class WaitableExecutor<T> {

    private final ExecutorService mExecutorService;
    private final CompletionService<T> mCompletionService;
    private final Set<Future<T>> mFutureSet = Sets.newHashSet();

    /**
     * Creates an executor that will use at most <var>nThreads</var> threads.
     * @param nThreads the number of threads, or zero for default count (which is number of core)
     */
    public WaitableExecutor(int nThreads) {
        if (nThreads < 1) {
            nThreads = Runtime.getRuntime().availableProcessors();
        }

        mExecutorService = Executors.newFixedThreadPool(nThreads);
        mCompletionService = new ExecutorCompletionService<T>(mExecutorService);
    }

    /**
     * Creates an executor that will use at most 1 thread per core.
     */
    public WaitableExecutor() {
        mExecutorService = null;
        mCompletionService = new ExecutorCompletionService<T>(ExecutorSingleton.getExecutor());
    }

    /**
     * Submits a Callable for execution.
     *
     * @param runnable the callable to run.
     */
    public void execute(Callable<T> runnable) {
        mFutureSet.add(mCompletionService.submit(runnable));
    }

    /**
     * Waits for all tasks to be executed. If a tasks throws an exception, it will be thrown from
     * this method inside the ExecutionException, preventing access to the result of the other
     * threads.
     *
     * If you want to get the results of all tasks (result and/or exception), use
     * {@link #waitForAllTasks()}
     *
     * @param cancelRemaining if true, and a task fails, cancel all remaining tasks.
     *
     * @return a list of all the return values from the tasks.
     *
     * @throws InterruptedException if this thread was interrupted. Not if the tasks were interrupted.
     */
    public List<T> waitForTasksWithQuickFail(boolean cancelRemaining) throws InterruptedException,
            LoggedErrorException {
        List<T> results = Lists.newArrayListWithCapacity(mFutureSet.size());
        try {
            while (!mFutureSet.isEmpty()) {
                Future<T> future = mCompletionService.take();

                assert mFutureSet.contains(future);
                mFutureSet.remove(future);

                // Get the result from the task. If the task threw an exception,
                // this will throw it, wrapped in an ExecutionException, caught below.
                results.add(future.get());
            }
        } catch (ExecutionException e) {
            if (cancelRemaining) {
                cancelAllTasks();
            }

            // get the original exception adn throw that one.
            Throwable cause = e.getCause();
            if (cause instanceof LoggedErrorException) {
                throw (LoggedErrorException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } finally {
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
            }
        }

        return results;
    }

    public static final class TaskResult<T> {
        public T value;
        public Throwable exception;

        static <T> TaskResult<T> withValue(T value) {
            TaskResult<T> result = new TaskResult<T>(null);
            result.value = value;
            return result;
        }

        TaskResult(Throwable cause) {
            exception = cause;
        }
    }

    /**
     * Waits for all tasks to be executed, and returns a {@link TaskResult} for each, containing
     * either the result or the exception thrown by the task.
     *
     * If a task is cancelled (and it threw InterruptedException) then the result for the task
     * is *not* included.
     *
     * @return a list of all the return values from the tasks.
     *
     * @throws InterruptedException if this thread was interrupted. Not if the tasks were interrupted.
     */
    public List<TaskResult<T>> waitForAllTasks() throws InterruptedException {
        List<TaskResult<T>> results = Lists.newArrayListWithCapacity(mFutureSet.size());
        try {
            while (!mFutureSet.isEmpty()) {
                Future<T> future = mCompletionService.take();

                assert mFutureSet.contains(future);
                mFutureSet.remove(future);

                // Get the result from the task.
                try {
                    results.add(TaskResult.withValue(future.get()));
                } catch (ExecutionException e) {
                    // the original exception thrown by the task is the cause of this one.
                    Throwable cause = e.getCause();

                    //noinspection StatementWithEmptyBody
                    if (cause instanceof InterruptedException) {
                        // if the task was cancelled we probably don't care about its result.
                    } else {
                        // there was an error.
                        results.add(new TaskResult<T>(cause));
                    }
                }
            }
        } finally {
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
            }
        }

        return results;
    }

    /**
     * Cancel all remaining tasks.
     */
    public void cancelAllTasks() {
        for (Future<T> future : mFutureSet) {
            future.cancel(true /*mayInterruptIfRunning*/);
        }
    }
}
