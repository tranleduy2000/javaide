/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.sdklib.repository;

import static org.junit.Assert.fail;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A SAX error handler that captures the errors and warnings.
 * This allows us to capture *all* errors and just not get an exception on the first one.
 */
class CaptureErrorHandler implements ErrorHandler {

    private String mWarnings = "";
    private String mErrors = "";

    public String getErrors() {
        return mErrors;
    }

    public String getWarnings() {
        return mWarnings;
    }

    /**
     * Verifies if the handler captures some errors or warnings.
     * Prints them on stderr.
     * Also fails the unit test if any error was generated.
     */
    public void verify() {
        if (!mWarnings.isEmpty()) {
            System.err.println(mWarnings);
        }

        if (!mErrors.isEmpty()) {
            System.err.println(mErrors);
            fail(mErrors);
        }
    }

    /**
     * @throws SAXException
     */
    @Override
    public void error(SAXParseException ex) throws SAXException {
        mErrors += "Error: " + ex.getMessage() + "\n";
    }

    /**
     * @throws SAXException
     */
    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
        mErrors += "Fatal Error: " + ex.getMessage() + "\n";
    }

    /**
     * @throws SAXException
     */
    @Override
    public void warning(SAXParseException ex) throws SAXException {
        mWarnings += "Warning: " + ex.getMessage() + "\n";
    }
}
