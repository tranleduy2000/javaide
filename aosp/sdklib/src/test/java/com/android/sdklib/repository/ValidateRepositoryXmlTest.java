/*
 * Copyright (C) 2009 The Android Open Source Project
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
import org.xml.sax.SAXParseException;

import java.io.InputStream;
import java.io.StringReader;

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
public class ValidateRepositoryXmlTest extends ValidateTestCase {

    private static String OPEN_TAG_REPO =
        "<r:sdk-repository xmlns:r=\"http://schemas.android.com/sdk/android/repository/" +
        Integer.toString(SdkRepoConstants.NS_LATEST_VERSION) +
        "\">";
    private static String CLOSE_TAG_REPO = "</r:sdk-repository>";

    // --- Tests ------------

    /** Validates that NS_LATEST_VERSION points to the max available XSD schema. */
    public void testRepoLatestVersionNumber() throws Exception {
        CaptureErrorHandler handler = new CaptureErrorHandler();

        // There should be a schema matching NS_LATEST_VERSION
        assertNotNull(getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, handler));

        // There should NOT be a schema with NS_LATEST_VERSION+1
        assertNull(
                String.format(
                        "There's a REPO XSD at version %d but SdkRepoConstants.NS_LATEST_VERSION is still set to %d.",
                        SdkRepoConstants.NS_LATEST_VERSION + 1,
                        SdkRepoConstants.NS_LATEST_VERSION),
                getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION + 1, handler));
    }

    /** Validate the XSD version 1 */
    public void testValidateRepositoryXsd1() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(1));
    }

    /** Validate a valid sample using namespace version 1 using an InputStream */
    public void testValidateLocalRepositoryFile1() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_01.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(1, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 2 */
    public void testValidateRepositoryXsd2() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(2));
    }

    /** Validate a valid sample using namespace version 2 using an InputStream */
    public void testValidateLocalRepositoryFile2() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_02.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(2, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 3 */
    public void testValidateRepositoryXsd3() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(3));
    }

    /** Validate a valid sample using namespace version 3 using an InputStream */
    public void testValidateLocalRepositoryFile3() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_03.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(3, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 4 */
    public void testValidateRepositoryXsd4() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(4));
    }

    /** Validate a valid sample using namespace version 4 using an InputStream */
    public void testValidateLocalRepositoryFile4() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_04.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(4, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 5 */
    public void testValidateRepositoryXsd5() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(5));
    }

    /** Validate a valid sample using namespace version 5 using an InputStream */
    public void testValidateLocalRepositoryFile5() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_05.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(5, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 6 */
    public void testValidateRepositoryXsd6() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(6));
    }

    /** Validate a valid sample using namespace version 6 using an InputStream */
    public void testValidateLocalRepositoryFile6() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_06.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(6, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 7 */
    public void testValidateRepositoryXsd7() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(7));
    }

    /** Validate a valid sample using namespace version 7 using an InputStream */
    public void testValidateLocalRepositoryFile7() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_07.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(7, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 8 */
    public void testValidateRepositoryXsd8() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(8));
    }

    /** Validate a valid sample using namespace version 8 using an InputStream */
    public void testValidateLocalRepositoryFile8() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_08.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(8, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 9 */
    public void testValidateRepositoryXsd9() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(9));
    }

    /** Validate a valid sample using namespace version 9 using an InputStream */
    public void testValidateLocalRepositoryFile9() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_09.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(9, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Validate the XSD version 10 */
    public void testValidateRepositoryXsd10() throws Exception {
        validateXsd(SdkRepoConstants.getXsdStream(10));
    }

    /** Validate a valid sample using namespace version 10 using an InputStream */
    public void testValidateLocalRepositoryFile10() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_10.xml");
        Source source = new StreamSource(xmlStream);

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(10, handler);
        validator.validate(source);
        handler.verify();
    }

    /** Make sure we don't have a next-version sample that is not validated yet */
    public void testValidateLocalRepositoryFile11() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_11.xml");
        assertNull(xmlStream);
    }

    // IMPORTANT: each time you add a test here, you should add a corresponding
    // test in SdkRepoSourceTest to validate the XML content is parsed correctly.


    // ---

    /** A document should at least have a root to be valid */
    public void testEmptyXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>";

        Source source = new StreamSource(new StringReader(document));

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, handler);

        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect to get this specific exception message
            assertRegex("Premature end of file.*", e.getMessage());
            return;
        }
        // We shouldn't get here
        handler.verify();
        fail();
    }

    /** A document with a root element containing no platform, addon, etc., is valid. */
    public void testEmptyRootXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, handler);
        validator.validate(source);
        handler.verify();
    }

    /** A document with an unknown element. */
    public void testUnknownContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            "<r:unknown />" +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        // don't capture the validator errors, we want it to fail and catch the exception
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect a parse expression referring to this grammar rule
            assertRegex("cvc-complex-type.2.4.a: Invalid content was found.*", e.getMessage());
            return;
        }
        // If we get here, the validator has not failed as we expected it to.
        fail();
    }

    /** A document with an incomplete element. */
    public void testIncompleteContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            "<r:platform> <r:api-level>1</r:api-level> <r:libs /> </r:platform>" +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        // don't capture the validator errors, we want it to fail and catch the exception
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect a parse error referring to this grammar rule
            assertRegex("cvc-complex-type.2.4.a: Invalid content was found.*", e.getMessage());
            return;
        }
        // If we get here, the validator has not failed as we expected it to.
        fail();
    }

    /** A document with a wrong type element. */
    public void testWrongTypeContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            "<r:platform> <r:api-level>NotAnInteger</r:api-level> <r:libs /> </r:platform>" +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        // don't capture the validator errors, we want it to fail and catch the exception
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect a parse error referring to this grammar rule
            assertRegex("cvc-datatype-valid.1.2.1: 'NotAnInteger' is not a valid value.*",
                    e.getMessage());
            return;
        }
        // If we get here, the validator has not failed as we expected it to.
        fail();
    }

    /** A document with an unknown license id. */
    public void testLicenseIdNotFound() throws Exception {
        // we define a license named "lic1" and then reference "lic2" instead
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            "<r:license id=\"lic1\"> some license </r:license> " +
            "<r:tool> <r:uses-license ref=\"lic2\" /> <r:revision> <r:major>1</r:major> </r:revision> " +
            "<r:min-platform-tools-rev> <r:major>1</r:major> </r:min-platform-tools-rev> " +
            "<r:archives> <r:archive> <r:size>1</r:size> <r:checksum>2822ae37115ebf13412bbef91339ee0d9454525e</r:checksum> " +
            "<r:url>url</r:url> </r:archive> </r:archives> </r:tool>" +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        // don't capture the validator errors, we want it to fail and catch the exception
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect a parse error referring to this grammar rule
            assertRegex("cvc-id.1: There is no ID/IDREF binding for IDREF 'lic2'.*",
                    e.getMessage());
            return;
        }
        // If we get here, the validator has not failed as we expected it to.
        fail();
    }

    /** The latest XSD repository-6 should fail when an 'extra' is present. */
    public void testExtraPathWithSlash() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG_REPO +
            "<r:extra> <r:revision>1</r:revision> <r:path>path</r:path> " +
            "<r:archives> <r:archive> <r:size>1</r:size> <r:checksum>2822ae37115ebf13412bbef91339ee0d9454525e</r:checksum> " +
            "<r:url>url</r:url> </r:archive> </r:archives> </r:extra>" +
            CLOSE_TAG_REPO;

        Source source = new StreamSource(new StringReader(document));

        // don't capture the validator errors, we want it to fail and catch the exception
        Validator validator = getRepoValidator(SdkRepoConstants.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            // We expect a parse error referring to this grammar rule
            assertRegex("cvc-complex-type.2.4.a: Invalid content was found starting with element 'r:extra'.*",
                    e.getMessage());
            return;
        }
        // If we get here, the validator has not failed as we expected it to.
        fail();
    }

    // --- Helpers ------------

    /**
     * Helper method that returns a validator for our Repository XSD
     *
     * @param version The version number, in range {@code 1..NS_LATEST_VERSION}
     * @param handler A {@link CaptureErrorHandler}. If null the default will be used,
     *   which will most likely print errors to stderr.
     */
    private Validator getRepoValidator(int version, @Nullable CaptureErrorHandler handler)
            throws SAXException {
        Validator validator = null;
        InputStream xsdStream = SdkRepoConstants.getXsdStream(version);
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
