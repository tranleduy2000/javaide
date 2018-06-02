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

package com.android.ide.common.caching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 */
public class CreatingCacheTest {

    private static class FakeFactory implements CreatingCache.ValueFactory<String, String> {
        @Override
        @NonNull
        public String create(@NonNull String key) {
            return key;
        }
    }

    private static class DelayedFactory implements CreatingCache.ValueFactory<String, String> {

        @NonNull
        private final CountDownLatch mLatch;

        public DelayedFactory(@NonNull CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        @NonNull
        public String create(@NonNull String key) {
            try {
                mLatch.await();
            } catch (InterruptedException ignored) {
            }
            return key;
        }
    }

    @Test
    public void testSingleThread() throws Exception {
        CreatingCache<String, String> cache = new CreatingCache<String, String>(new FakeFactory());

        String value1 = cache.get("key");
        assertEquals("key", value1);
        String value2 = cache.get("key");
        assertEquals("key", value2);
        //noinspection StringEquality
        assertTrue("repetitive calls give same instance", value1 == value2);
    }

    private static class CacheRunnable implements Runnable {

        @NonNull
        private final CreatingCache<String, String> mCache;
        @Nullable
        private final CountDownLatch mLatch;

        private String mResult;
        private InterruptedException mException;

        CacheRunnable(@NonNull CreatingCache<String, String> cache) {
            this(cache, null);
        }

        /**
         * Creates a runnable, that will notify when it's pending on a query.
         *
         * @param cache the cache to query
         * @param latch the latch to countdown when the query is being processed.
         */
        CacheRunnable(
                @NonNull CreatingCache<String, String> cache,
                @Nullable CountDownLatch latch) {
            mCache = cache;
            mLatch = latch;
        }

        @Override
        public void run() {
            if (mLatch != null) {
                mResult = mCache.get("foo", new CreatingCache.QueryListener() {
                    @Override
                    public void onQueryState(@NonNull CreatingCache.State state) {
                        mLatch.countDown();
                    }
                });
            } else {
                mResult = mCache.get("foo");
            }
        }

        public String getResult() {
            return mResult;
        }

        public InterruptedException getException() {
            return mException;
        }
    }

    @Test
    public void testMultiThread() throws Exception {
        // the latch that controls whether the factory will "create" an item.
        CountDownLatch factoryLatch = new CountDownLatch(1);

        CreatingCache<String, String>
                cache = new CreatingCache<String, String>(new DelayedFactory(factoryLatch));

        // the latch that will be released when the runnable1 is pending its query.
        CountDownLatch latch1 = new CountDownLatch(1);

        CacheRunnable runnable1 = new CacheRunnable(cache, latch1);
        Thread t1 = new Thread(runnable1);
        t1.start();

        // wait on thread1 being waiting on the query, before creating thread2
        latch1.await();

        // the latch that will be released when the runnable1 is pending its query.
        CountDownLatch latch2 = new CountDownLatch(1);

        CacheRunnable runnable2 = new CacheRunnable(cache,latch2);
        Thread t2 = new Thread(runnable2);
        t2.start();

        // wait on thread2 being waiting on the query, before releasing the factory
        latch2.await();

        factoryLatch.countDown();

        // wait on threads being done.
        t1.join();
        t2.join();

        assertEquals("foo", runnable1.getResult());
        assertEquals("foo", runnable2.getResult());
        //noinspection StringEquality
        assertTrue("repetitive calls give same instance", runnable1.getResult() == runnable2.getResult());
    }

    @Test(expected = IllegalStateException.class)
    public void testClear() throws Exception {
        // the latch that controls whether the factory will "create" an item.
        // this is never released in this test since we want to try clearing the cache while an
        // item is pending creation.
        CountDownLatch factoryLatch = new CountDownLatch(1);

        final CreatingCache<String, String>
                cache = new CreatingCache<String, String>(new DelayedFactory(factoryLatch));

        // the latch that will be released when the thread is pending its query.
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(new CacheRunnable(cache, latch)).start();

        // wait on thread to be waiting, before trying to clear the cache.
        latch.await();

        cache.clear();
    }
}