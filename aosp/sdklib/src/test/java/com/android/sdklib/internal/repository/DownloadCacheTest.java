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

package com.android.sdklib.internal.repository;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.AndroidLocationTestCase;
import com.android.sdklib.internal.repository.DownloadCache.Strategy;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.io.IFileOp;
import com.android.sdklib.io.MockFileOp;
import com.android.utils.Pair;
import com.google.common.base.Charsets;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DownloadCacheTest extends AndroidLocationTestCase {

    private MockFileOp mFileOp;
    private MockMonitor mMonitor;

    /**
     * A private version of DownloadCache that never calls {@link UrlOpener}.
     */
    private static class NoDownloadCache extends DownloadCache {

        private final Map<String, Pair<InputStream, HttpResponse>> mReplies =
            new HashMap<String, Pair<InputStream,HttpResponse>>();

        public NoDownloadCache(@NonNull Strategy strategy) {
            super(strategy);
        }

        public NoDownloadCache(@NonNull IFileOp fileOp, @NonNull Strategy strategy) {
            super(fileOp, strategy);
        }

        @Override
        protected Pair<InputStream, HttpResponse> openUrl(
                @NonNull String url,
                boolean needsMarkResetSupport,
                @NonNull ITaskMonitor monitor,
                @Nullable Header[] headers) throws IOException, CanceledByUserException {

            Pair<InputStream, HttpResponse> reply = mReplies.get(url);
            if (reply != null) {
                return reply;
            }

            // http-client's behavior is to return a FNF instead of 404.
            throw new FileNotFoundException(url);
        }

        public void registerResponse(@NonNull String url, int httpCode, @Nullable String content) {
            InputStream is = null;
            if (content != null) {
                is = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
            }

            ProtocolVersion p = new ProtocolVersion("HTTP", 1, 1);
            StatusLine statusLine = new BasicStatusLine(p, httpCode, "Code " + httpCode);
            HttpResponse httpResponse = new BasicHttpResponse(statusLine);
            Pair<InputStream, HttpResponse> reply = Pair.of(is, httpResponse);

            mReplies.put(url, reply);
        }

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mFileOp = new MockFileOp();
        mMonitor = new MockMonitor();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMissingResource() throws Exception {
        // Downloads must fail when using the only-cache strategy and there's nothing in the cache.
        // In that case, it returns null to indicate the resource is simply not found.
        // Since the mock implementation always returns a 404 and no stream, there is no
        // difference between the various cache strategies.

        mFileOp.reset();
        NoDownloadCache d1 = new NoDownloadCache(mFileOp, Strategy.ONLY_CACHE);
        InputStream is1 = d1.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNull(is1);
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d1.getCacheRoot()));
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        // HTTP-Client's behavior is to return a FNF instead of 404 so we'll try that first
        mFileOp.reset();
        NoDownloadCache d2 = new NoDownloadCache(mFileOp, Strategy.DIRECT);

        try {
            d2.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
            fail("Expected: NoDownloadCache.openCachedUrl should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
            assertEquals("http://www.example.com/download1.xml", e.getMessage());
        }
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        // Try again but this time we'll define a 404 reply to test the rest of the code path.
        mFileOp.reset();
        d2.registerResponse("http://www.example.com/download1.xml", 404, null);
        InputStream is2 = d2.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNull(is2);
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        mFileOp.reset();
        NoDownloadCache d3 = new NoDownloadCache(mFileOp, Strategy.SERVE_CACHE);
        d3.registerResponse("http://www.example.com/download1.xml", 404, null);
        InputStream is3 = d3.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNull(is3);
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        mFileOp.reset();
        NoDownloadCache d4 = new NoDownloadCache(mFileOp, Strategy.FRESH_CACHE);
        d4.registerResponse("http://www.example.com/download1.xml", 404, null);
        InputStream is4 = d4.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNull(is4);
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));
    }

    public void testExistingResource() throws Exception {
        // The resource exists but only-cache doesn't hit the network so it will
        // fail when the resource is not cached.
        mFileOp.reset();
        NoDownloadCache d1 = new NoDownloadCache(mFileOp, Strategy.ONLY_CACHE);
        d1.registerResponse("http://www.example.com/download1.xml", 200, "Blah blah blah");
        InputStream is1 = d1.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNull(is1);
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d1.getCacheRoot()));
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        // HTTP-Client's behavior is to return a FNF instead of 404 so we'll try that first
        mFileOp.reset();
        NoDownloadCache d2 = new NoDownloadCache(mFileOp, Strategy.DIRECT);
        d2.registerResponse("http://www.example.com/download1.xml", 200, "Blah blah blah");
        InputStream is2 = d2.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNotNull(is2);
        assertEquals("Blah blah blah", new BufferedReader(new InputStreamReader(is2, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals("[]", Arrays.toString(mFileOp.getOutputStreams()));

        mFileOp.reset();
        NoDownloadCache d3 = new NoDownloadCache(mFileOp, Strategy.SERVE_CACHE);
        d3.registerResponse("http://www.example.com/download1.xml", 200, "Blah blah blah");
        InputStream is3 = d3.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNotNull(is3);
        assertEquals("Blah blah blah", new BufferedReader(new InputStreamReader(is3, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals(
                "[<$CACHE/sdkbin-1_9b8dc757-download1_xml: 'Blah blah blah'>, " +
                 "<$CACHE/sdkinf-1_9b8dc757-download1_xml: '### Meta data for SDK Manager cache. Do not modify.\n" +
                  "#<creation timestamp>\n" +
                  "URL=http\\://www.example.com/download1.xml\n" +
                  "Status-Code=200\n" +
                "'>]",
                sanitize(d3, Arrays.toString(mFileOp.getOutputStreams())));

        mFileOp.reset();
        NoDownloadCache d4 = new NoDownloadCache(mFileOp, Strategy.FRESH_CACHE);
        d4.registerResponse("http://www.example.com/download1.xml", 200, "Blah blah blah");
        InputStream is4 = d4.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        assertNotNull(is4);
        assertEquals("Blah blah blah", new BufferedReader(new InputStreamReader(is4, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertEquals(
                "[<$CACHE/sdkbin-1_9b8dc757-download1_xml: 'Blah blah blah'>, " +
                 "<$CACHE/sdkinf-1_9b8dc757-download1_xml: '### Meta data for SDK Manager cache. Do not modify.\n" +
                  "#<creation timestamp>\n" +
                  "URL=http\\://www.example.com/download1.xml\n" +
                  "Status-Code=200\n" +
                "'>]",
                sanitize(d4, Arrays.toString(mFileOp.getOutputStreams())));
    }

    public void testCachedResource() throws Exception {
        mFileOp.reset();
        NoDownloadCache d1 = new NoDownloadCache(mFileOp, Strategy.ONLY_CACHE);
        d1.registerResponse("http://www.example.com/download1.xml", 200, "This is the new content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d1.getCacheRoot(), "sdkbin-1_9b8dc757-download1_xml")),
                123456L,
                "This is the cached content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d1.getCacheRoot(), "sdkinf-1_9b8dc757-download1_xml")),
                123456L,
                "URL=http\\://www.example.com/download1.xml\n" +
                "Status-Code=200\n");
        InputStream is1 = d1.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        // Only-cache strategy returns the value from the cache, not the actual resource.
        assertEquals("This is the cached content", new BufferedReader(new InputStreamReader(is1, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d1.getCacheRoot()));
        // The cache hasn't been modified, only read
        assertEquals("[]", sanitize(d1, Arrays.toString(mFileOp.getOutputStreams())));

        // Direct ignores the cache.
        mFileOp.reset();
        NoDownloadCache d2 = new NoDownloadCache(mFileOp, Strategy.DIRECT);
        d2.registerResponse("http://www.example.com/download1.xml", 200, "This is the new content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d2.getCacheRoot(), "sdkbin-1_9b8dc757-download1_xml")),
                123456L,
                "This is the cached content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d2.getCacheRoot(), "sdkinf-1_9b8dc757-download1_xml")),
                123456L,
                "URL=http\\://www.example.com/download1.xml\n" +
                "Status-Code=200\n");
        InputStream is2 = d2.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        // Direct strategy ignores the cache.
        assertEquals("This is the new content", new BufferedReader(new InputStreamReader(is2, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d2.getCacheRoot()));
        // Direct strategy doesn't update the cache.
        assertEquals("[]", sanitize(d2, Arrays.toString(mFileOp.getOutputStreams())));

        // Serve-cache reads from the cache if available, ignoring its freshness (here the timestamp
        // is way older than the 10-minute freshness encoded in the DownloadCache.)
        mFileOp.reset();
        NoDownloadCache d3 = new NoDownloadCache(mFileOp, Strategy.SERVE_CACHE);
        d3.registerResponse("http://www.example.com/download1.xml", 200, "This is the new content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d3.getCacheRoot(), "sdkbin-1_9b8dc757-download1_xml")),
                123456L,
                "This is the cached content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d3.getCacheRoot(), "sdkinf-1_9b8dc757-download1_xml")),
                123456L,
                "URL=http\\://www.example.com/download1.xml\n" +
                "Status-Code=200\n");
        InputStream is3 = d3.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        // We get content from the cache.
        assertEquals("This is the cached content", new BufferedReader(new InputStreamReader(is3, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d3.getCacheRoot()));
        // Cache isn't updated since nothing fresh was read.
        assertEquals("[]", sanitize(d3, Arrays.toString(mFileOp.getOutputStreams())));

        // fresh-cache reads the cache, finds it stale (here the timestamp
        // is way older than the 10-minute freshness encoded in the DownloadCache)
        // and will fetch the new resource instead and update the cache.
        mFileOp.reset();
        NoDownloadCache d4 = new NoDownloadCache(mFileOp, Strategy.FRESH_CACHE);
        d4.registerResponse("http://www.example.com/download1.xml", 200, "This is the new content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d4.getCacheRoot(), "sdkbin-1_9b8dc757-download1_xml")),
                123456L,
                "This is the cached content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d4.getCacheRoot(), "sdkinf-1_9b8dc757-download1_xml")),
                123456L,
                "URL=http\\://www.example.com/download1.xml\n" +
                "Status-Code=200\n");
        InputStream is4 = d4.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        // Cache is discarded, actual resource is returned.
        assertEquals("This is the new content", new BufferedReader(new InputStreamReader(is4, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d4.getCacheRoot()));
        // Cache isn updated since something fresh was read.
        assertEquals(
                "[<$CACHE/sdkbin-1_9b8dc757-download1_xml: 'This is the new content'>, " +
                 "<$CACHE/sdkinf-1_9b8dc757-download1_xml: '### Meta data for SDK Manager cache. Do not modify.\n" +
                  "#<creation timestamp>\n" +
                  "URL=http\\://www.example.com/download1.xml\n" +
                  "Status-Code=200\n" +
                "'>]",
                sanitize(d4, Arrays.toString(mFileOp.getOutputStreams())));

        // fresh-cache reads the cache, finds it still valid stale (less than 10-minute old),
        // and uses the cached resource.
        mFileOp.reset();
        NoDownloadCache d5 = new NoDownloadCache(mFileOp, Strategy.FRESH_CACHE);
        d5.registerResponse("http://www.example.com/download1.xml", 200, "This is the new content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d5.getCacheRoot(), "sdkbin-1_9b8dc757-download1_xml")),
                System.currentTimeMillis() - 1000,
                "This is the cached content");
        mFileOp.recordExistingFile(
                mFileOp.getAgnosticAbsPath(FileOp.append(d5.getCacheRoot(), "sdkinf-1_9b8dc757-download1_xml")),
                System.currentTimeMillis() - 1000,
                "URL=http\\://www.example.com/download1.xml\n" +
                "Status-Code=200\n");
        InputStream is5 = d5.openCachedUrl("http://www.example.com/download1.xml", mMonitor);
        // Cache is used.
        assertEquals("This is the cached content", new BufferedReader(new InputStreamReader(is5, Charsets.UTF_8)).readLine());
        assertEquals("", mMonitor.getAllCapturedLogs());
        assertTrue(mFileOp.hasRecordedExistingFolder(d5.getCacheRoot()));
        // Cache isn't updated since nothing fresh was read.
        assertEquals("[]", sanitize(d5, Arrays.toString(mFileOp.getOutputStreams())));
    }

    // --------

    @Nullable
    private String sanitize(@NonNull DownloadCache dc, @Nullable String msg) {
        if (msg != null) {
            msg = msg.replace("\r\n", "\n");

            String absRoot = mFileOp.getAgnosticAbsPath(dc.getCacheRoot());
            msg = msg.replace(absRoot, "$CACHE");

            // Cached files also contain a creation timestamp which we need to find and remove.
            msg = msg.replaceAll("\n#[A-Z][A-Za-z0-9: ]+20[0-9]{2}\n", "\n#<creation timestamp>\n");
        }
        return msg;
    }

}
