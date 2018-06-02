/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.annotations.Nullable;

import org.xml.sax.SAXException;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Tests local validation of an SDK Repository sample XMLs using an XML Schema validator.
 *
 * References:
 * http://www.ibm.com/developerworks/xml/library/x-javaxmlvalidapi.html
 */
public class ValidateSysImgXmlTest extends ValidateTestCase {

    // --- Tests ------------

    /** Validates that NS_LATEST_VERSION points to the max available XSD schema. */
    public void testSysImgLatestVersionNumber() throws Exception {
        CaptureErrorHandler handler = new CaptureErrorHandler();

        // There should be a schema matching NS_LATEST_VERSION
        assertNotNull(getValidator(SdkSysImgConstants.NS_LATEST_VERSION, handler));

        // There should NOT be a schema with NS_LATEST_VERSION+1
        assertNull(
                String.format(
                        "There's a REPO XSD at version %d but SdkSysImgConstants.NS_LATEST_VERSION is still set to %d.",
                        SdkSysImgConstants.NS_LATEST_VERSION + 1,
                        SdkSysImgConstants.NS_LATEST_VERSION),
                getValidator(SdkSysImgConstants.NS_LATEST_VERSION + 1, handler));
    }

    /** Validate the XSD version 1 */
    public void testValidateSysImgXsd1() throws Exception {
        validateXsd(SdkSysImgConstants.getXsdStream(1));
    }

    /** Validate a valid sample using namespace version 1 using an InputStream */
    public void testValidateLocalSysImgFile1() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/sys_img_sample_1.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(1, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 2 */
    public void testValidateSysImgXsd2() throws Exception {
        validateXsd(SdkSysImgConstants.getXsdStream(2));
    }

    /** Validate a valid sample using namespace version 2 using an InputStream */
    public void testValidateLocalSysImgFile2() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/sys_img_sample_2.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(2, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 3 */
    public void testValidateSysImgXsd3() throws Exception {
        validateXsd(SdkSysImgConstants.getXsdStream(3));
    }

    /** Validate a valid sample using namespace version 3 using an InputStream */
    public void testValidateLocalSysImgFile3() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/sys_img_sample_3.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(3, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Make sure we don't have a next-version sample that is not validated yet */
    public void testValidateLocalSysImgFile4() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/sys_img_sample_4.xml");
        assertNull(xmlStream);
    }

    // IMPORTANT: each time you add a test here, you should add a corresponding
    // test in SdkSysImgSourceTest to validate the XML content is parsed correctly.


    // --- Helpers ------------

    /**
     * Helper method that returns a validator for our Repository XSD
     *
     * @param version The version number, in range {@code 1..NS_LATEST_VERSION}
     * @param handler A {@link CaptureErrorHandler}. If null the default will be used,
     *   which will most likely print errors to stderr.
     */
    private Validator getValidator(int version, @Nullable CaptureErrorHandler handler)
            throws SAXException {
        Validator validator = null;
        InputStream xsdStream = SdkSysImgConstants.getXsdStream(version);
        if (xsdStream != null) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            validator = schema.newValidator();

            if (handler != null) {
                validator.setErrorHandler(handler);
            }
        }

        return validator;
    }
}
