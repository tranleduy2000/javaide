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
import com.android.annotations.Nullable;
import com.android.builder.tasks.BooleanLatch;
import com.android.builder.tasks.Job;
import com.android.utils.GrabProcessOutput;
import com.android.utils.ILogger;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * interface to the aapt long running process.
 */
public class AaptProcess {

    private static final int DEFAULT_SLAVE_APPT_TIMEOUT_IN_SECONDS = 5;
    private static final int SLAVE_AAPT_TIMEOUT_IN_SECONDS =
            System.getenv("SLAVE_AAPT_TIMEOUT") == null
                    ? DEFAULT_SLAVE_APPT_TIMEOUT_IN_SECONDS
                    : Integer.parseInt(System.getenv("SLAVE_AAPT_TIMEOUT"));

    private final Process mProcess;
    private final ILogger mLogger;

    private final ProcessOutputFacade mProcessOutputFacade = new ProcessOutputFacade();
    private final List<String> mMessages = new ArrayList<String>();
    private final AtomicBoolean mReady = new AtomicBoolean(false);
    private final BooleanLatch mReadyLatch = new BooleanLatch();
    private final OutputStreamWriter mWriter;

    private AaptProcess(@NonNull Process process, @NonNull ILogger iLogger)
            throws InterruptedException {
        mProcess = process;
        mLogger = iLogger;
        GrabProcessOutput.grabProcessOutput(process, GrabProcessOutput.Wait.ASYNC,
                        mProcessOutputFacade);
        mWriter = new OutputStreamWriter(mProcess.getOutputStream());
    }

    /**
     * Notifies the slave process of a new crunching request, do not block on completion, the
     * notification will be issued through the job parameter's
     * {@link com.android.builder.tasks.Job#finished()} or
     * {@link com.android.builder.tasks.Job#error()}
     * functions.
     *
     * @param in the source file to crunch
     * @param out where to place the crunched file
     * @param job the job to notify when the crunching is finished successfully or not.
     * @throws IOException
     */
    public void crunch(@NonNull File in, @NonNull File out, @NonNull Job<AaptProcess> job)
            throws IOException {

        mLogger.verbose("Process(" + mProcess.hashCode() + ")" + in.getName() +
                "job: " + job.toString());
        if (!mReady.get()) {
            throw new RuntimeException("AAPT process not ready to receive commands");
        }
        NotifierProcessOutput notifier =
                new NotifierProcessOutput(job, mProcessOutputFacade, mLogger);

        mProcessOutputFacade.setNotifier(notifier);
        mWriter.write("s\n");
        mWriter.write(in.getAbsolutePath());
        mWriter.write("\n");
        mWriter.write(out.getAbsolutePath());
        mWriter.write("\n");
        mWriter.flush();
        mLogger.verbose("Processed(" + mProcess.hashCode() + ")" + in.getName() +
                "job: " + job.toString());
        mMessages.add("Process(" + mProcess.hashCode() + ") processed " + in.getName() +
            "job: " + job.toString());
    }

    public void waitForReady() throws InterruptedException {
        if (!mReadyLatch.await(TimeUnit.NANOSECONDS.convert(
                SLAVE_AAPT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))) {
            throw new RuntimeException("Timed out while waiting for slave aapt process, "
                    + "try setting environment variable SLAVE_AAPT_TIMEOUT to a value bigger than "
                    + SLAVE_AAPT_TIMEOUT_IN_SECONDS + " seconds");
        }

        mLogger.info("Slave %1$s is ready", hashCode());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("ready", mReady.get())
                .add("process", mProcess.hashCode())
                .toString();
    }

    /**
     * Shutdowns the slave process and release all resources.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void shutdown() throws IOException, InterruptedException {

        mReady.set(false);
        mWriter.write("quit\n");
        mWriter.flush();
        mProcess.waitFor();
        mLogger.verbose("Process (%1$s) processed %2$s files", mProcess.hashCode(),
                mMessages.size());
        for (String message : mMessages) {
            mLogger.verbose(message);
        }
    }

    public static class Builder {
        private final String mAaptLocation;
        private final ILogger mLogger;
        public Builder(@NonNull String aaptPath, @NonNull ILogger iLogger) {
            mAaptLocation = aaptPath;
            mLogger = iLogger;
        }

        public AaptProcess start() throws IOException, InterruptedException {
            String[] command = new String[] {
                    mAaptLocation,
                    "m",
            };

            mLogger.verbose("Trying to start %1$s", command[0]);
            Process process = new ProcessBuilder(command).start();
            mLogger.verbose("Started %1$d", process.hashCode());
            return new AaptProcess(process, mLogger);
        }
    }

    private class ProcessOutputFacade implements GrabProcessOutput.IProcessOutput {
        @Nullable NotifierProcessOutput notifier = null;
        AtomicBoolean ready = new AtomicBoolean(false);

        synchronized void setNotifier(@NonNull NotifierProcessOutput notifierProcessOutput) {
            if (notifier != null) {
                throw new RuntimeException("Notifier already set, threading issue");
            }
            notifier = notifierProcessOutput;
        }

        synchronized void reset() {
            notifier = null;
        }

        @Nullable
        synchronized NotifierProcessOutput getNotifier() {
            return notifier;
        }

        @Override
        public synchronized void out(@Nullable String line) {

            // an empty message or aapt startup message are ignored.
            if (Strings.isNullOrEmpty(line)) {
                return;
            }
            if (line.equals("Ready")) {
                AaptProcess.this.mReady.set(true);
                AaptProcess.this.mReadyLatch.signal();
                return;
            }
            NotifierProcessOutput delegate = getNotifier();
            mLogger.verbose("AAPT out(%1$s): %2$s", mProcess.hashCode(), line);
            if (delegate != null) {
                mLogger.verbose("AAPT out(%1$s): -> %2$s", mProcess.hashCode(), delegate.mJob);
                delegate.out(line);
            } else {
                mLogger.error(null, "AAPT out(%1$s) : No Delegate set : lost message:%2$s",
                        mProcess.hashCode(), line);
            }
        }

        @Override
        public synchronized void err(@Nullable String line) {

            if (Strings.isNullOrEmpty(line)) {
                return;
            }
            NotifierProcessOutput delegate = getNotifier();
            if (delegate != null) {
                mLogger.verbose("AAPT err(%1$s): %2$s -> %3$s", mProcess.hashCode(), line,
                        delegate.mJob);
                delegate.err(line);
            } else {
                if (!mReady.get()) {
                    if (line.equals("ERROR: Unknown command 'm'")) {
                       throw new RuntimeException("Invalid aapt version, version 21 or above is required");
                    }
                    mLogger.error(null, "AAPT err(%1$s): %2$s", mProcess.hashCode(), line);
                } else {
                    mLogger.error(null, "AAPT err(%1$s) : No Delegate set : lost message:%2$s",
                            mProcess.hashCode(), line);
                }
            }
        }

        Process getProcess() {
            return mProcess;
        }
    }

    private static class NotifierProcessOutput implements GrabProcessOutput.IProcessOutput {

        @NonNull private final Job<AaptProcess> mJob;
        @NonNull private final ProcessOutputFacade mOwner;
        @NonNull private final ILogger mLogger;

        NotifierProcessOutput(
                @NonNull Job<AaptProcess> job,
                @NonNull ProcessOutputFacade owner,
                @NonNull ILogger iLogger) {
            mOwner = owner;
            mJob = job;
            mLogger = iLogger;
        }

        @Override
        public void out(@Nullable String line) {
            if (line != null) {
                mLogger.verbose("AAPT notify(%1$s): %2$s", mJob, line);
                if (line.equalsIgnoreCase("Done")) {
                    mOwner.reset();
                    mJob.finished();
                } else if (line.equalsIgnoreCase("Error")) {
                    mOwner.reset();
                    mJob.error();
                } else {
                    mLogger.verbose("AAPT(%1$s) discarded: %2$s", mJob, line);
                }
            }
        }

        @Override
        public void err(@Nullable String line) {
            if (line != null) {
                mLogger.verbose("AAPT warning(%1$s), Job(%2$s): %3$s",
                        mOwner.getProcess().hashCode(), mJob, line);
                mLogger.warning("AAPT: %3$s",
                        mOwner.getProcess().hashCode(), mJob, line);

            }
        }
    }
}
