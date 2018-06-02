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

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for the {@link ThreadRecorder} class.
 */
public class ThreadRecorderTest {

    @Before
    public void setUp() {

        // reset for each test.
        ProcessRecorderFactory.sINSTANCE = new ProcessRecorderFactory();
        ProcessRecorderFactory.sINSTANCE.setRecordWriter(
                Mockito.mock(ProcessRecorder.ExecutionRecordWriter.class));
    }

    @Test
    public void testBasicTracing() {
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception{
                        return 10;
                    }
                });

        Assert.assertNotNull(value);
        Assert.assertEquals(10, value.intValue());
    }

    @Test
    public void testBasicNoExceptionHandling() {
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception{
                        return 10;
                    }

                    @Override
                    public void handleException(@NonNull Exception e) {
                        handlerCalled.set(true);
                    }
                });

        Assert.assertNotNull(value);
        Assert.assertEquals(10, value.intValue());
        // exception handler shouldn't have been called.
        Assert.assertFalse(handlerCalled.get());
    }

    @Test
    public void testBasicExceptionHandling() {
        final Exception toBeThrown = new Exception("random");
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        throw toBeThrown;
                    }

                    @Override
                    public void handleException(@NonNull Exception e) {
                        handlerCalled.set(true);
                        Assert.assertEquals(toBeThrown, e);
                    }
                });

        Assert.assertTrue(handlerCalled.get());
        Assert.assertNull(value);
    }

    @Test
    public void testBlocks() {
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                                new Recorder.Block<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return 10;
                                    }
                                });
                    }
                });

        Assert.assertNotNull(value);
        Assert.assertEquals(10, value.intValue());
    }

    @Test
    public void testBlocksWithInnerException() {
        final Exception toBeThrown = new Exception("random");
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                                new Recorder.Block<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        throw toBeThrown;
                                    }

                                    @Override
                                    public void handleException(@NonNull Exception e) {
                                        handlerCalled.set(true);
                                        Assert.assertEquals(toBeThrown, e);
                                    }
                                });
                    }
                });
        Assert.assertTrue(handlerCalled.get());
        Assert.assertNull(value);
    }

    @Test
    public void testBlocksWithOuterException() {
        final Exception toBeThrown = new Exception("random");
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                                new Recorder.Block<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return 10;
                                    }
                                });
                        throw toBeThrown;
                    }

                    @Override
                    public void handleException(@NonNull Exception e) {
                        handlerCalled.set(true);
                        Assert.assertEquals(toBeThrown, e);
                    }
                });
        Assert.assertTrue(handlerCalled.get());
        Assert.assertNull(value);
    }

    @Test
    public void testBlocksWithInnerExceptionRepackaged() {
        final Exception toBeThrown = new Exception("random");
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                                new Recorder.Block<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        throw toBeThrown;
                                    }
                                });
                    }

                    @Override
                    public void handleException(@NonNull Exception e) {
                        handlerCalled.set(true);
                        Assert.assertTrue(e instanceof RuntimeException);
                        Assert.assertEquals(toBeThrown, e.getCause());
                    }
                });
        Assert.assertTrue(handlerCalled.get());
        Assert.assertNull(value);
    }

    @Test
    public void testWithMulipleInnerBlocksWithExceptionRepackaged() {
        final Exception toBeThrown = new Exception("random");
        final AtomicBoolean handlerCalled = new AtomicBoolean(false);
        // make three layers and throw an exception from the bottom layer, ensure the exception
        // is not repackaged in a RuntimeException several time as it makes its way back up
        // to the handler.
        Integer value = ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                new Recorder.Block<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                                new Recorder.Block<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return ThreadRecorder.get().record(
                                                ExecutionType.SOME_RANDOM_PROCESSING,
                                                new Recorder.Block<Integer>() {
                                                    @Override
                                                    public Integer call() throws Exception {
                                                        throw toBeThrown;
                                                    }
                                                });
                                    }
                                });
                    }

                    @Override
                    public void handleException(@NonNull Exception e) {
                        handlerCalled.set(true);
                        Assert.assertTrue(e instanceof RuntimeException);
                        Assert.assertEquals(toBeThrown, e.getCause());
                    }
                });
        Assert.assertTrue(handlerCalled.get());
        Assert.assertNull(value);
    }

    @Test
    public void testExceptionPropagation() {
        final Exception toBeThrown = new Exception("random");
        try {
            ThreadRecorder.get().record(ExecutionType.SOME_RANDOM_PROCESSING,
                    new Recorder.Block<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            throw toBeThrown;
                        }
                    });
        } catch (Exception e) {
            Assert.assertEquals(toBeThrown, e.getCause());
            return;
        }
        Assert.fail("Exception not propagated.");
    }
}
