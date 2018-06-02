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

import com.android.annotations.NonNull;
import com.android.utils.ILogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A work queue that accepts jobs and treat them in order.
 *
 * @author jedo@google.com (Jerome Dochez)
 */
public class WorkQueue<T> implements Runnable {

    private static final boolean VERBOSE = System.getenv("GRADLE_WORK_QUEUE_VERBOSE") != null;

    private final ILogger mLogger;

    // queue name as human would understand.
    private final String mName;

    // the user throttling has already happened before so I am using a potentially
    // infinite linked list of request.
    private final LinkedBlockingQueue<QueueTask<T>> mPendingJobs =
            new LinkedBlockingQueue<QueueTask<T>>();

    // List of working threads pumping from this queue.
    private final List<Thread> mWorkThreads = new ArrayList<Thread>();

    private final float mGrowthTriggerRation;
    private final int mMWorkforceIncrement;
    private final AtomicInteger mThreadId = new AtomicInteger(0);
    private final QueueThreadContext<T> mQueueThreadContext;

    // we could base this on the number of processors this machine has, etc...
    private static final int MAX_WORKFORCE_SIZE = 20;


    /**
     * Private queue structure to store queue items.
     */
    private static class QueueTask<T> {

        enum ActionType { Death, Normal }
        final ActionType actionType;
        final Job<T> job;

        private QueueTask(ActionType actionType, Job<T> job) {
            this.actionType = actionType;
            this.job = job;
        }
    }

    /**
     * Creates a non expanding queue, with a number of dedicated threads to process
     * the queue's jobs.
     *
     * @param logger to log messages
     * @param queueName a meaningful descriptive name.
     * @param workforce the number of dedicated threads for this queue.
     */
    public WorkQueue(
            @NonNull ILogger logger,
            @NonNull QueueThreadContext<T> queueThreadContext,
            @NonNull String queueName,
            int workforce) {
        this(logger, queueThreadContext, queueName, workforce, Float.MAX_VALUE);
    }

    /**
     * Creates a new queue, with a number of dedicated threads to process
     * the queue's jobs.
     *
     * @param logger to log messages
     * @param queueName a meaningful descriptive name.
     * @param workforce the number of dedicated threads for this queue.
     * @param growthTriggerRatio the ratio between outstanding requests and worker threads that
     *                           should trigger a growth in worker threads.
     */
    public WorkQueue(
            @NonNull ILogger logger,
            @NonNull QueueThreadContext<T> queueThreadContext,
            @NonNull String queueName,
            int workforce,
            float growthTriggerRatio) {

        this.mLogger = logger;
        this.mName = queueName;
        this.mGrowthTriggerRation = growthTriggerRatio;
        this.mMWorkforceIncrement = workforce;
        this.mQueueThreadContext = queueThreadContext;
    }

    public void push(Job<T> job) throws InterruptedException {
        _push(new QueueTask<T>(QueueTask.ActionType.Normal, job));
        checkWorkforce();
    }

    private void _push(QueueTask<T> task) throws InterruptedException {
        // at this point, I am not trying to limit the number of pending jobs.
        // eventually we would want to put some limit to the size of the pending jobs
        // queue so it does not grow out of control.
        mPendingJobs.put(task);
    }

    private synchronized void checkWorkforce() {
        if (mWorkThreads.isEmpty()
                || (mPendingJobs.size() / mWorkThreads.size() > mGrowthTriggerRation)) {
            verbose("Request to incrementing workforce from %1$d", mWorkThreads.size());
            if (mWorkThreads.size() >= MAX_WORKFORCE_SIZE) {
                verbose("Already at max workforce %1$d, denied.", MAX_WORKFORCE_SIZE);
                return;
            }
            for (int i = 0; i < mMWorkforceIncrement; i++) {
                Thread t = new Thread(this, mName + "_" + mThreadId.incrementAndGet());
                t.setDaemon(true);
                mWorkThreads.add(t);
                t.start();
            }
            verbose("thread-pool size=%1$d", mWorkThreads.size());
        }
    }

    private synchronized void reduceWorkforce() throws InterruptedException {
        verbose("Decrementing workforce from " + mWorkThreads.size());
        // push a the right number of kiss of death tasks to shutdown threads.
        for (int i = 0; i < mMWorkforceIncrement; i++) {
           _push(new QueueTask<T>(QueueTask.ActionType.Death, null));
        }
    }

    /**
     * Shutdowns the working queue and wait until all pending requests have
     * been processed. This needs to be reviewed as jobs can still be added
     * to the queue once the shutdown process has started....
     * @throws InterruptedException if the shutdown sequence is interrupted
     */
    public synchronized void shutdown() throws InterruptedException {

        // push as many death pills as necessary
        for (Thread t : mWorkThreads) {
            _push(new QueueTask<T>(QueueTask.ActionType.Death, null));
        }
        // we could use a latch.
        for (Thread t : mWorkThreads) {
            t.join();
        }
        mWorkThreads.clear();
        mQueueThreadContext.shutdown();
    }

    /**
     * Return a human readable queue name, mainly used for identification
     * purposes.
     *
     * @return a unique meaningful descriptive name
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the number of jobs waiting to be scheduled.
     *
     * @return the size of the queue.
     */
    public int size() {
        return mPendingJobs.size();
    }


    /**
     * each thread in the mWorkThreads will run this single infinite processing loop until a
     * death action is received.
     */
    @Override
    public void run() {
        final String threadName = Thread.currentThread().getName();
        // this
        try {
            try {
                verbose("Creating a new working thread %1$s", threadName);
                mQueueThreadContext.creation(Thread.currentThread());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true) {
                final QueueTask<T> queueTask = mPendingJobs.take();
                if (queueTask.actionType== QueueTask.ActionType.Death) {
                    verbose("Thread(%1$s): Death requested", threadName);
                    // we are done.
                    return;
                }
                final Job<T> job = queueTask.job;
                if (job == null) {
                    // this clearly should not happen.
                    Logger.getAnonymousLogger().severe(
                            "I got a null pending job out of the priority queue");
                    return;
                }
                verbose("Thread(%1$s): scheduling %2$s", threadName, job.getJobTitle());

                try {
                    mQueueThreadContext.runTask(job);
                } catch (Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING, "Exception while processing task ", e);
                    job.error();
                    return;
                }
                // wait for the job completion.
                job.await();
                verbose("Thread(%1$s): job %2$s finished", threadName, job.getJobTitle());
                // we could potentially reduce the workforce at this point if we have little
                // queuing comparatively to the number of worker threads but at this point, the
                // overall process (gradle activity) is fairly short lived so skipping at this
                // point.
                verbose("Thread(%1$s): queue size %2$d", threadName, mPendingJobs.size());
            }
        } catch (InterruptedException e) {
            mLogger.error(e, "Thread(%1$s): Interrupted", threadName);
        } finally {
            try {
                verbose("Thread(%1$s): destruction", threadName);
                mQueueThreadContext.destruction(Thread.currentThread());
            } catch (IOException e) {
                mLogger.error(e, "Thread(%1$s): %2$s", threadName, e.getMessage());
            } catch (InterruptedException e) {
                mLogger.error(e, "Thread(%1$s): %2$s", threadName, e.getMessage());
            }
        }
    }

    private void verbose(String format, Object...args) {
        if (VERBOSE) {
            mLogger.verbose(format, args);
        }
    }
}
