/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sdklib.internal.repository;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.Pair;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/** A mock UpdaterData that simply records what would have been installed. */
public class MockDownloadCache extends DownloadCache {

    private final File mCacheRoot;

    /** Map url => payload bytes, http code response.
     *  If the payload pair is null, an exception such as FNF is thrown. */
    private final Map<String, Payload> mDirectPayloads = new HashMap<String, Payload>();
    /** Map url => payload bytes, http code response.
     *  If the payload pair is null, an exception such as FNF is thrown. */
    private final Map<String, Payload> mCachedPayloads = new HashMap<String, Payload>();

    private final Map<String, Integer> mDirectHits = new TreeMap<String, Integer>();
    private final Map<String, Integer> mCachedHits = new TreeMap<String, Integer>();

    private Strategy mOverrideStrategy;

    public static final int THROW_FNF = -1;

    /**
     * Creates a download cache with a {@code DIRECT} strategy and
     * no root {@code $HOME/.android} folder, which effectively disables the cache.
     */
    public MockDownloadCache() {
        super(DownloadCache.Strategy.DIRECT);
        mCacheRoot = null;
    }

    /**
     * Creates a download with the given strategy and the given cache root.
     */
    public MockDownloadCache(DownloadCache.Strategy strategy, File cacheRoot) {
        super(strategy);
        mCacheRoot = cacheRoot;
    }

    @Override
    protected File initCacheRoot() {
        return mCacheRoot;
    }

    /**
     * Override the {@link DownloadCache.Strategy} of the cache.
     * This lets us set it temporarily to {@link DownloadCache.Strategy#ONLY_CACHE},
     * which will force {@link #openCachedUrl(String, ITaskMonitor)} to throw an FNF,
     * essentially simulating an empty cache at first.
     * <p/>
     * Setting it back to null reverts the behavior to its default.
     */
    public void overrideStrategy(DownloadCache.Strategy strategy) {
        mOverrideStrategy = strategy;
    }

    /**
     * Register a direct payload response.
     *
     * @param url The URL to match.
     * @param httpCode The expected response code.
     *                 Use {@link #THROW_FNF} to mean an FNF should be thrown (which is what the
     *                 httpClient stack seems to return instead of {@link HttpStatus#SC_NOT_FOUND}.)
     * @param content The payload to return.
     *                As a shortcut a null will be replaced by an empty byte array.
     */
    public void registerDirectPayload(String url, int httpCode, byte[] content) {
        mDirectPayloads.put(url, new Payload(httpCode, content));
    }

    /**
     * Register a cached payload response.
     *
     * @param url The URL to match.
     * @param content The payload to return or null to throw a FNF.
     */
    public void registerCachedPayload(String url, byte[] content) {
        mCachedPayloads.put(url,
                new Payload(content == null ? THROW_FNF : HttpStatus.SC_OK, content));
    }

    public String[] getDirectHits() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mDirectHits) {
            for (Entry<String, Integer> entry : mDirectHits.entrySet()) {
                list.add(String.format("<%1$s : %2$d>",
                        entry.getKey(), entry.getValue().intValue()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getCachedHits() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mCachedHits) {
            for (Entry<String, Integer> entry : mCachedHits.entrySet()) {
                list.add(String.format("<%1$s : %2$d>",
                        entry.getKey(), entry.getValue().intValue()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public void clearDirectHits() {
        synchronized (mDirectHits) {
            mDirectHits.clear();
        }
    }

    public void clearCachedHits() {
        synchronized (mCachedHits) {
            mCachedHits.clear();
        }
    }

    /**
     * Override openDirectUrl to return one of the registered payloads or throw a FNF exception.
     * This totally ignores the cache's {@link DownloadCache.Strategy}.
     */
    @Override
    public Pair<InputStream, HttpResponse> openDirectUrl(
            @NonNull String urlString,
            @Nullable Header[] headers,
            @NonNull ITaskMonitor monitor) throws IOException, CanceledByUserException {

        synchronized (mDirectHits) {
            Integer count = mDirectHits.get(urlString);
            mDirectHits.put(urlString, (count == null ? 0 : count.intValue()) + 1);
        }

        Payload payload = mDirectPayloads.get(urlString);

        if (payload == null || payload.mHttpCode == THROW_FNF) {
            throw new FileNotFoundException(urlString);
        }

        byte[] content = payload.mContent;
        if (content == null) {
            content = new byte[0];
        }

        InputStream is  = new ByteArrayInputStream(content);
        HttpResponse hr = new BasicHttpResponse(
                new ProtocolVersion("HTTP", 1, 1),
                payload.mHttpCode,
                "Http-Code-" + payload.mHttpCode);

        return Pair.of(is, hr);
    }

    /**
     * Override openCachedUrl to return one of the registered payloads or throw a FNF exception.
     * This totally ignores the cache's {@link DownloadCache.Strategy}.
     * It will however throw a FNF if {@link #overrideStrategy(Strategy)} is set to
     * {@link DownloadCache.Strategy#ONLY_CACHE}.
     */
    @Override
    public InputStream openCachedUrl(String urlString, ITaskMonitor monitor)
            throws IOException, CanceledByUserException {

        synchronized (mCachedHits) {
            Integer count = mCachedHits.get(urlString);
            mCachedHits.put(urlString, (count == null ? 0 : count.intValue()) + 1);
        }

        if (Strategy.ONLY_CACHE.equals(mOverrideStrategy)) {
            // Override the cache to read only "local cached" data.
            // In this first phase, we assume there's nothing cached.
            // TODO register first-pass files later.
            throw new FileNotFoundException(urlString);
        }

        Payload payload = mCachedPayloads.get(urlString);

        if (payload == null || payload.mHttpCode != HttpStatus.SC_OK) {
            throw new FileNotFoundException(urlString);
        }

        byte[] content = payload.mContent;
        if (content == null) {
            content = new byte[0];
        }

        return new ByteArrayInputStream(content);
    }

    private static class Payload {
        final byte[] mContent;
        final int    mHttpCode;

        Payload(int httpCode, byte[] content) {
            mHttpCode = httpCode;
            mContent = content;
        }
    }

}
