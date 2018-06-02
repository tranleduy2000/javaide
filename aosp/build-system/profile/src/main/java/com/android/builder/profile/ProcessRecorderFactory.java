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

package com.android.builder.profile;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Configures and creates instances of {@link ProcessRecorder}.
 *
 * There can be only one instance of {@link ProcessRecorder} per process (well class loader
 * to be exact). This instance can be configured initially before any calls to
 * {@link ThreadRecorder#get()} is made. An exception will be thrown if an attempt is made to
 * configure the instance of {@link ProcessRecorder} past this initialization window.
 *
 */
public class ProcessRecorderFactory {

    public static void shutdown() throws InterruptedException {
        synchronized (LOCK) {
            List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory
                    .getGarbageCollectorMXBeans();
            ThreadRecorder.get().record(ExecutionType.FINAL_METADATA, Recorder.EmptyBlock,
                    new Recorder.Property("build_time",
                            Long.toString(System.currentTimeMillis() - sINSTANCE.startTime)),
                    new Recorder.Property("gc_count",
                            Long.toString(garbageCollectorMXBeans.get(0).getCollectionCount()
                                    - sINSTANCE.gcCountAtStart)),
                    new Recorder.Property("gc_time",
                            Long.toString(garbageCollectorMXBeans.get(0).getCollectionTime()
                                    - sINSTANCE.gcTimeAtStart)));
            if (sINSTANCE.isInitialized()) {
                sINSTANCE.get().finish();
                sINSTANCE.uploadData();
            }
            sINSTANCE.processRecorder = null;
        }
    }

    public static void initialize(
            @NonNull ILogger logger,
            @NonNull File out,
            @NonNull List<Recorder.Property> properties) throws IOException {

        synchronized (LOCK) {
            if (sINSTANCE.isInitialized() || !isEnabled()) {
                return;
            }
            sINSTANCE.setLogger(logger);
            sINSTANCE.setOutputFile(out);
            sINSTANCE.setRecordWriter(new ProcessRecorder.JsonRecordWriter(new FileWriter(out)));
            sINSTANCE.get(); // Initialize the ProcessRecorder instance
            publishInitialRecords(properties);
        }
    }

    public static void publishInitialRecords(@NonNull List<Recorder.Property> properties) {

        List<Recorder.Property> propertyList = Lists.newArrayListWithExpectedSize(
                6 + properties.size());

        propertyList.add(new Recorder.Property(
                "build_id",
                UUID.randomUUID().toString()));
        propertyList.add(new Recorder.Property(
                "os_name",
                System.getProperty("os.name")));
        propertyList.add(new Recorder.Property(
                "os_version",
                System.getProperty("os.version")));
        propertyList.add(new Recorder.Property(
                "java_version",
                System.getProperty("java.version")));
        propertyList.add(new Recorder.Property(
                "java_vm_version",
                System.getProperty("java.vm.version")));
        propertyList.add(new Recorder.Property(
                "max_memory",
                Long.toString(Runtime.getRuntime().maxMemory())));
        propertyList.addAll(properties);

        ThreadRecorder.get().record(
                ExecutionType.INITIAL_METADATA,
                Recorder.EmptyBlock,
                propertyList);
    }

    private static boolean sENABLED = !Strings.isNullOrEmpty(System.getenv("RECORD_SPANS"));

    private final long startTime;
    private final long gcCountAtStart;
    private final long gcTimeAtStart;

    ProcessRecorderFactory() {
        startTime = System.currentTimeMillis();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory
                .getGarbageCollectorMXBeans();
        gcCountAtStart = garbageCollectorMXBeans.get(0).getCollectionCount();
        gcTimeAtStart = garbageCollectorMXBeans.get(0).getCollectionTime();
    }

    public static void initializeForTests(ProcessRecorder.ExecutionRecordWriter recordWriter) {
        sINSTANCE = new ProcessRecorderFactory();
        ProcessRecorder.resetForTests();
        setEnabled(true);
        sINSTANCE.setRecordWriter(recordWriter);
        sINSTANCE.get(); // Initialize the ProcessRecorder instance
        publishInitialRecords(ImmutableList.<Recorder.Property>of());
    }

    static boolean isEnabled() {
        return sENABLED;
    }

    @VisibleForTesting
    static void setEnabled(boolean enabled) {
        sENABLED = enabled;
    }

    /**
     * Sets the {@link ProcessRecorder.JsonRecordWriter }
     * @param recordWriter
     */
    public synchronized void setRecordWriter(
            @NonNull ProcessRecorder.ExecutionRecordWriter recordWriter) {
        assertRecorderNotCreated();
        this.recordWriter = recordWriter;
    }

    public synchronized void setLogger(@NonNull ILogger iLogger) {
        assertRecorderNotCreated();
        this.iLogger = iLogger;
    }

    public static ProcessRecorderFactory getFactory() {
        return sINSTANCE;
    }

    boolean isInitialized() {
        return processRecorder != null;
    }

    @SuppressWarnings("VariableNotUsedInsideIf")
    private void assertRecorderNotCreated() {
        if (isInitialized()) {
            throw new RuntimeException("ProcessRecorder already created.");
        }
    }

    static final Object LOCK = new Object();
    static ProcessRecorderFactory sINSTANCE = new ProcessRecorderFactory();

    @Nullable
    private ProcessRecorder processRecorder = null;
    @Nullable
    private ProcessRecorder.ExecutionRecordWriter recordWriter = null;
    @Nullable
    private ILogger iLogger = null;

    private File outputFile = null;

    private void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    synchronized ProcessRecorder get() {
        if (processRecorder == null) {
            if (recordWriter == null) {
                throw new RuntimeException("recordWriter not configured.");
            }
            if (iLogger == null) {
                iLogger = new StdLogger(StdLogger.Level.INFO);
            }
            processRecorder = new ProcessRecorder(recordWriter, iLogger);
        }
        return processRecorder;
    }

    private void uploadData() {

        if (outputFile == null) {
            return;
        }
        try {
            URL u = new URL("http://android-devtools-logging.appspot.com/log/");
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) u.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(outputFile.length()));
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(outputFile));
                OutputStream os = conn.getOutputStream();
                ByteStreams.copy(is, os);
                os.close();
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = reader.readLine()) != null) {
                if (iLogger != null) {
                    iLogger.info("From POST : " + line);
                }
            }
            reader.close();
        } catch(Exception e) {
            if (iLogger != null) {
                iLogger.warning("An exception while generated while uploading the profiler data");
                iLogger.error(e, "Exception while uploading the profiler data");
            }
        }
    }
}
