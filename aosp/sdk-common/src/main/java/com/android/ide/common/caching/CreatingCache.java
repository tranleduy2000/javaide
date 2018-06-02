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

import static com.google.common.base.Preconditions.checkArgument;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.annotations.concurrency.GuardedBy;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * A cache that handles creating the values when they are not present in the map.
 *
 * Calls to {@link #get(Object)} returns the value, calling into {@link ValueFactory} if it
 * was not created. If the creation takes a long time, other threads can still query the cache
 * for the same or different keys. Calls for the same key will block until the value has been
 * created. Calls for different keys will return right away if the key is available.
 *
 * This is very similar to Guava's LoadingCache, without the automated clean-up based on size
 * or time.
 * This is extracted from the PreDexCache of the Gradle plugin which has different requirements
 * (reloading cached info from disk)
 *
 * This class is thread-safe.
 *
 * TODO Move PreDexCache to be based on this.
 *
 */
public class CreatingCache<K, V> {

    @GuardedBy("this")
    private final Map<Object, V> mCache = Maps.newHashMap();
    @GuardedBy("this")
    private final Map<Object, CountDownLatch> mProcessedValues = Maps.newHashMap();

    @NonNull
    private final ValueFactory<K, V> mValueFactory;

    /**
     * A factory creating values based on keys.
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    public interface ValueFactory<K, V> {

        /**
         * Creates a value based on a given key.
         * @param key the key
         * @return the value
         */
        @NonNull
        V create(@NonNull K key);
    }

    public CreatingCache(@NonNull ValueFactory<K, V> valueFactory) {
        mValueFactory = valueFactory;
    }

    /**
     * Queries the cache for a given key. If the value is not present, this blocks until it is.
     *
     * If this is the first thread requesting the value, then this trigger creation of the value
     * through the {@link ValueFactory}.
     *
     * @param key the given key.
     * @return the value, or null if the thread was interrupted while waiting for the value to be created.
     */
    @Nullable
    public V get(@NonNull K key) {
        return get(key, null);
    }

    /**
     * A Query Listener used for testing.
     *
     * @see #get(Object, QueryListener)
     */
    @VisibleForTesting
    interface QueryListener {
        void onQueryState(@NonNull State state);
    }

    /**
     * Queries the cache for a given key. If the value is not present, this blocks until it is.
     *
     * This version allows for a listener that is notified when the state of the query is known.
     * This allows knowing the state while the method is blocked waiting for creation of the value
     * in this thread or another. This is used for testing.
     *
     * @param key the given key.
     * @param queryListener the listener.
     * @return the value, or null if the thread was interrupted while waiting for the value to be created.
     *
     * @see #get(Object)
     */
    @VisibleForTesting
    V get(@NonNull K key, @Nullable QueryListener queryListener) {
        ValueState<V> state = findValueState(key);

        if (queryListener != null) {
            queryListener.onQueryState(state.getState());
        }

        switch (state.getState()) {
            case EXISTING_VALUE:
                return state.getValue();
            case NEW_VALUE:
                // create the actual value content.
                V value = mValueFactory.create(key);

                // add to cache, and enable other threads to use the value.
                addNewValue(key, value, state.getLatch());

                return value;
            case PROCESSED_VALUE:
                // wait for value to become available
                try {
                    state.getLatch().await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                synchronized (this) {
                    // get it from the map cache.
                    return mCache.get(key);
                }
            default:
                throw new IllegalStateException("unsupported ResultType: " + state.getState());
        }
    }

    /**
     * Clears the cache of all values.
     *
     * @throws IllegalStateException if values are currently being created.
     */
    public synchronized void clear() {
        if (!mProcessedValues.isEmpty()) {
            throw new IllegalStateException("Cache values are being processed");
        }

        mCache.clear();
    }

    /**
     * State of values.
     */
    @VisibleForTesting
    enum State { EXISTING_VALUE, NEW_VALUE, PROCESSED_VALUE
    }

    /**
     * A Value State. This contains the Type as {@link State}, and a optional value {@link V}
     * or latch.
     * @param <V> the value type
     */
    private static final class ValueState<V> {

        @NonNull
        private final State mType;
        private final V mValue;
        private final CountDownLatch mLatch;

        ValueState(V value) {
            this(State.EXISTING_VALUE, value, null);
        }

        ValueState(@NonNull State type, CountDownLatch latch) {
            this(type, null, latch);
            checkArgument(type != State.EXISTING_VALUE);
        }

        private ValueState(@NonNull State type, V value, CountDownLatch latch) {
            mType = type;
            mValue = value;
            mLatch = latch;
        }

        @NonNull
        public State getState() {
            return mType;
        }

        @NonNull
        public V getValue() {
            return mValue;
        }

        @NonNull
        public CountDownLatch getLatch() {
            return mLatch;
        }
    }

    /**
     * Returns the state of the value for a given key.
     *
     * If the value does not exist, prepares a latch to control availability of the value.
     *
     * @param key the key
     * @return a ValueState instance.
     */
    @NonNull
    private synchronized ValueState<V> findValueState(@NonNull K key) {
        V value = mCache.get(key);

        // value exists, just return the state.
        if (value != null) {
            return new ValueState<V>(value);
        }

        // check if the value is currently being created
        CountDownLatch latch = mProcessedValues.get(key);
        if (latch != null) {
            // return the latch allowing to wait for end of creation.
            return new ValueState<V>(State.PROCESSED_VALUE, latch);
        }

        // new value: create a latch to allow others to wait until creation is done.
        latch = new CountDownLatch(1);
        mProcessedValues.put(key, latch);
        return new ValueState<V>(State.NEW_VALUE, latch);
    }

    /**
     * Adds a new value to the cache and release threads waiting for it.
     * @param key the key
     * @param value the value
     * @param latch the latch holding the threads.
     */
    private synchronized void addNewValue(
            @NonNull K key,
            @NonNull V value,
            @NonNull CountDownLatch latch) {
        mCache.put(key, value);
        mProcessedValues.remove(key);
        latch.countDown();
    }
}
