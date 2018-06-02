/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.io;

import com.android.annotations.NonNull;
import com.android.io.NonClosingInputStream.CloseBehavior;
import com.google.common.base.Charsets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import junit.framework.TestCase;

/**
 *
 */
public class NonClosingInputStreamTest extends TestCase {

    private File mFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFile = File.createTempFile("test", "txt");
        FileWrapper fw = new FileWrapper(mFile);
        fw.setContents(new ByteArrayInputStream("1234".getBytes(Charsets.UTF_8)));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (!mFile.delete()) {
            mFile.deleteOnExit();
        }
    }

    /**
     * Tests the normal case where a method parses a stream closes it.
     * Next parser/reset operation fails with an IOException since the stream is closed.
     */
    public void testFailure() throws IOException {
        InputStream is = loadResource();
        try {

            assertEquals('1', parseAndClose(is));
            try {
                assertEquals('2', parse(is));
                fail("Expected: IOException 'stream closed'; Actual: no error.");
            } catch (IOException e) {
                assertEquals("Stream closed", e.getMessage());
            }

        } finally {
            // Stream should have been closed already.
            // This is to prevent from keeping a stream open in case the test fails.
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Tests with a NonClosingInputStream using CLOSE behavior.
     * This should do exactly like in the previous testFailure case.
     */
    public void testCloseBehavior() throws IOException {
        InputStream is = loadResource();
        try {
            InputStream ncis = new NonClosingInputStream(is);

            assertEquals('1', parseAndClose(ncis));
            try {
                assertEquals('2', parse(ncis));
                fail("Expected: IOException 'stream closed'; Actual: no error.");
            } catch (IOException e) {
                assertEquals("stream closed", e.getMessage().toLowerCase(Locale.US));
            }
        } finally {
            // Stream should have been closed already.
            // This is to prevent from keeping a stream open in case the test fails.
            //
            // Note that to really close, we need to invoke the original stream
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Tests with a NonClosingInputStream using IGNORE behavior.
     * In this case, when the parser tries to close the stream, nothing happens.
     */
    public void testIgnoreBehavior() throws IOException {
        InputStream is = loadResource();
        try {
            InputStream ncis = new NonClosingInputStream(is);
            ((NonClosingInputStream) ncis).setCloseBehavior(CloseBehavior.IGNORE);
            assertTrue(ncis.markSupported());

            assertEquals('1', parseAndClose(ncis));

            // the first parser's close should have done nothing
            assertEquals('2', parse(ncis));

            // closing does nothing
            ncis.close();
            assertEquals('3', parse(ncis));
            assertEquals('4', parse(ncis));

            // we can change the closing behavior to really close the stream
            ((NonClosingInputStream) ncis).setCloseBehavior(CloseBehavior.CLOSE);
            try {
                ncis.close();
                assertEquals('5', parse(ncis));
                fail("Expected: IOException 'stream closed'; Actual: no error.");
            } catch (IOException e) {
                assertEquals("stream closed", e.getMessage().toLowerCase(Locale.US));
            }

        } finally {
            // Stream should have been closed already.
            // This is to prevent from keeping a stream open in case the test fails.
            //
            // Note that to really close, we need to invoke the original stream
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Tests with a NonClosingInputStream using RESET behavior.
     * In this case, when the parser tries to close the stream, it actually
     * calls reset and reverts it the last marked position.
     */
    public void testResetBehavior() throws IOException {
        InputStream is = loadResource();
        try {
            InputStream ncis = new NonClosingInputStream(is);
            ((NonClosingInputStream) ncis).setCloseBehavior(CloseBehavior.RESET);
            assertTrue(ncis.markSupported());
            ncis.mark(10);

            assertEquals('1', parseAndClose(ncis));

            // the first parser's close should have reset to the beginning
            assertEquals('1', parse(ncis));
            assertEquals('2', parse(ncis));

            // a reset takes us back to the beginning
            is.reset();
            assertEquals('1', parse(ncis));
            assertEquals('2', parse(ncis));

            // a direct close on the wrapper also resets
            ncis.close();
            assertEquals('1', parse(ncis));
            assertEquals('2', parse(ncis));

            // we can change the closing behavior to really close the stream
            ((NonClosingInputStream) ncis).setCloseBehavior(CloseBehavior.CLOSE);
            try {
                ncis.close();
                assertEquals('3', parse(ncis));
                fail("Expected: IOException 'stream closed'; Actual: no error.");
            } catch (IOException e) {
                assertEquals("stream closed", e.getMessage().toLowerCase(Locale.US));
            }

        } finally {
            // Stream should have been closed already.
            // This is to prevent from keeping a stream open in case the test fails.
            //
            // Note that to really close, we need to invoke the original stream
            if (is != null) {
                is.close();
            }
        }
    }

    //---

    private InputStream loadResource() throws FileNotFoundException {
        InputStream is = new BufferedInputStream(new FileInputStream(mFile));
        assertNotNull("test.txt not found", is);
        return is;
    }

    private char parse(@NonNull InputStream is) throws IOException {
        return (char) is.read();
    }

    private char parseAndClose(@NonNull InputStream is) throws IOException {
        try {
            return (char) is.read();
        } finally {
            is.close();
        }
    }

}
