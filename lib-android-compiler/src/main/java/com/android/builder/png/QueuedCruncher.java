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

package com.android.builder.png;

import com.android.annotations.NonNull;
import com.android.builder.tasks.Job;
import com.android.builder.tasks.JobContext;
import com.android.builder.tasks.QueueThreadContext;
import com.android.builder.tasks.Task;
import com.android.builder.tasks.WorkQueue;
import com.android.ide.common.internal.PngCruncher;
import com.android.ide.common.internal.PngException;
import com.android.utils.ILogger;
import com.google.common.base.Objects;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * implementation of {@link com.android.ide.common.internal.PngCruncher} that queues request and
 * use a pool or aapt server processes to serve those.
 */
public class QueuedCruncher implements PngCruncher {

    // use an enum to ensure singleton.
    public enum Builder {
        INSTANCE;

        private final Map<String, QueuedCruncher> sInstances =
                new ConcurrentHashMap<String, QueuedCruncher>();
        private final Object sLock = new Object();

        /**
         * Creates a new {@link com.android.builder.png.QueuedCruncher} or return an existing one
         * based on the underlying AAPT executable location.
         * @param aaptLocation the APPT executable location.
         * @param logger the logger to use
         * @return a new of existing instance of the {@link com.android.builder.png.QueuedCruncher}
         */
        public QueuedCruncher newCruncher(
                @NonNull String aaptLocation,
                @NonNull ILogger logger) {
            synchronized (sLock) {
                logger.info("QueuedCruncher is using %1$s", aaptLocation);
                if (!sInstances.containsKey(aaptLocation)) {
                    QueuedCruncher queuedCruncher = new QueuedCruncher(aaptLocation, logger);
                    sInstances.put(aaptLocation, queuedCruncher);
                }
                return sInstances.get(aaptLocation);
            }
        }
    }


    @NonNull private final String mAaptLocation;
    @NonNull private final ILogger mLogger;
    // Queue responsible for handling all passed jobs with a pool of worker threads.
    @NonNull private final WorkQueue<AaptProcess> mCrunchingRequests;
    // list of outstanding jobs.
    @NonNull private final Map<Integer, ConcurrentLinkedQueue<Job<AaptProcess>>> mOutstandingJobs =
            new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Job<AaptProcess>>>();
    // ref count of active users, if it drops to zero, that means there are no more active users
    // and the queue should be shutdown.
    @NonNull private final AtomicInteger refCount = new AtomicInteger(0);

    // per process unique key provider to remember which users enlisted which requests.
    @NonNull private final AtomicInteger keyProvider = new AtomicInteger(0);


    private QueuedCruncher(
            @NonNull String aaptLocation,
            @NonNull ILogger iLogger) {
        mAaptLocation = aaptLocation;
        mLogger = iLogger;
        QueueThreadContext<AaptProcess> queueThreadContext = new QueueThreadContext<AaptProcess>() {

            // move this to a TLS, but do not store instances of AaptProcess in it.
            @NonNull private final Map<String, AaptProcess> mAaptProcesses =
                    new ConcurrentHashMap<String, AaptProcess>();

            @Override
            public void creation(@NonNull Thread t) throws IOException {
                try {
                    mLogger.verbose("Thread(%1$s): create aapt slave",
                            Thread.currentThread().getName());
                    AaptProcess aaptProcess = new AaptProcess.Builder(mAaptLocation, mLogger).start();
                    assert aaptProcess != null;
                    aaptProcess.waitForReady();
                    mAaptProcesses.put(t.getName(), aaptProcess);
                } catch (InterruptedException e) {
                    mLogger.error(e, "Cannot start slave process");
                    e.printStackTrace();
                }
            }

            @Override
            public void runTask(@NonNull Job<AaptProcess> job) throws Exception {
                job.runTask(
                        new JobContext<AaptProcess>(
                                mAaptProcesses.get(Thread.currentThread().getName())));
                mOutstandingJobs.get(((QueuedJob) job).key).remove(job);
            }

            @Override
            public void destruction(@NonNull Thread t) throws IOException, InterruptedException {

                AaptProcess aaptProcess = mAaptProcesses.get(Thread.currentThread().getName());
                if (aaptProcess != null) {
                    mLogger.verbose("Thread(%1$s): notify aapt slave shutdown",
                            Thread.currentThread().getName());
                    aaptProcess.shutdown();
                    mAaptProcesses.remove(t.getName());
                    mLogger.verbose("Thread(%1$s): after shutdown queue_size=%2$d",
                            Thread.currentThread().getName(), mAaptProcesses.size());
                }
            }

            @Override
            public void shutdown() {
                if (!mAaptProcesses.isEmpty()) {
                    mLogger.warning("Process list not empty");
                    for (Map.Entry<String, AaptProcess> aaptProcessEntry : mAaptProcesses
                            .entrySet()) {
                        mLogger.warning("Thread(%1$s): queue not cleaned", aaptProcessEntry.getKey());
                        try {
                            aaptProcessEntry.getValue().shutdown();
                        } catch (Exception e) {
                            mLogger.error(e, "while shutting down" + aaptProcessEntry.getKey());
                        }
                    }
                }
                mAaptProcesses.clear();
            }
        };
        mCrunchingRequests = new WorkQueue<AaptProcess>(
                mLogger, queueThreadContext, "png-cruncher", 5, 2f);
    }

    private static final class QueuedJob extends Job<AaptProcess> {

        private final int key;
        public QueuedJob(int key, String jobTile, Task<AaptProcess> task) {
            super(jobTile, task);
            this.key = key;
        }
    }

    @Override
    public void crunchPng(int key, @NonNull final File from, @NonNull final File to)
            throws PngException {

        try {
            final Job<AaptProcess> aaptProcessJob = new QueuedJob(
                    key,
                    "Cruncher " + from.getName(),
                    new Task<AaptProcess>() {
                        @Override
                        public void run(@NonNull Job<AaptProcess> job,
                                @NonNull JobContext<AaptProcess> context) throws IOException {
                            mLogger.verbose("Thread(%1$s): begin executing job %2$s",
                                    Thread.currentThread().getName(), job.getJobTitle());
                            context.getPayload().crunch(from, to, job);
                            mLogger.verbose("Thread(%1$s): done executing job %2$s",
                                    Thread.currentThread().getName(), job.getJobTitle());
                        }

                        @Override
                        public String toString() {
                            return Objects.toStringHelper(this)
                                    .add("from", from.getAbsolutePath())
                                    .add("to", to.getAbsolutePath())
                                    .toString();
                        }
                    });
            mOutstandingJobs.get(key).add(aaptProcessJob);
            mCrunchingRequests.push(aaptProcessJob);
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            throw new PngException(e);
        }
    }

    private void waitForAll(int key) throws InterruptedException {
        mLogger.verbose("Thread(%1$s): begin waitForAll", Thread.currentThread().getName());
        ConcurrentLinkedQueue<Job<AaptProcess>> jobs = mOutstandingJobs.get(key);
        Job<AaptProcess> aaptProcessJob = jobs.poll();
        while (aaptProcessJob != null) {
            mLogger.verbose("Thread(%1$s) : wait for {%2$s)", Thread.currentThread().getName(),
                    aaptProcessJob.toString());
            if (!aaptProcessJob.await()) {
                throw new RuntimeException(
                        "Crunching " + aaptProcessJob.getJobTitle() + " failed, see logs");
            }
            aaptProcessJob = jobs.poll();
        }
        mLogger.verbose("Thread(%1$s): end waitForAll", Thread.currentThread().getName());
    }

    @Override
    public synchronized int start() {
        // increment our reference count.
        refCount.incrementAndGet();
        // get a unique key for the lifetime of this process.
        int key = keyProvider.incrementAndGet();
        mOutstandingJobs.put(key, new ConcurrentLinkedQueue<Job<AaptProcess>>());
        return key;
    }

    @Override
    public synchronized void end(int key) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        try {
            waitForAll(key);
            mOutstandingJobs.get(key).clear();
            mLogger.verbose("Job finished in %1$d", System.currentTimeMillis() - startTime);
        } finally {
            // even if we have failures, we need to shutdown property the sub processes.
            if (refCount.decrementAndGet() == 0) {
                mCrunchingRequests.shutdown();
                mLogger.verbose("Shutdown finished in %1$d",
                        System.currentTimeMillis() - startTime);
            }
        }
    }
}
