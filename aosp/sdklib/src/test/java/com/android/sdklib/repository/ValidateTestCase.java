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

package com.android.sdklib.repository;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

abstract class ValidateTestCase extends TestCase {

    /**
     * Validates an XSD stream against the w3.org XSD schema.
     */
    protected void validateXsd(InputStream repoXsdStream) throws SAXException, IOException {
        final Class<? extends ValidateTestCase> clazz = this.getClass();
        InputStream xsdXsdStream = clazz.getResourceAsStream(
                "/com/android/sdklib/testdata/www.w3.org/2001/XMLSchema.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolver() {
            @Override
            public LSInput resolveResource(
                    String type,
                    String namespaceURI,
                    final String publicId,
                    final String systemId,
                    final String baseURI) {
                if (systemId != null) {
                    String resName = "/com/android/sdklib/testdata/www.w3.org/2001/";
                    int pos = systemId.lastIndexOf('/');
                    if (pos < 0) {
                        resName += systemId;
                    } else {
                        resName += systemId.substring(pos + 1);
                    }
                    final InputStream stream = clazz.getResourceAsStream(resName);
                    if (stream == null) {
                        fail("XSD validation requires missing file: " + resName);
                    }
                    return new LSInput() {
                        @SuppressWarnings("hiding")
                        @Override
                        public void setSystemId(String systemId) {}

                        @Override
                        public void setStringData(String stringData) {}

                        @SuppressWarnings("hiding")
                        @Override
                        public void setPublicId(String publicId) {}

                        @Override
                        public void setEncoding(String encoding) {}

                        @Override
                        public void setCharacterStream(Reader characterStream) {}

                        @Override
                        public void setCertifiedText(boolean certifiedText) {}

                        @Override
                        public void setByteStream(InputStream byteStream) {}

                        @SuppressWarnings("hiding")
                        @Override
                        public void setBaseURI(String baseURI) {}

                        @Override
                        public String getSystemId() {
                            return systemId;
                        }

                        @Override
                        public String getStringData() {
                            return null;
                        }

                        @Override
                        public String getPublicId() {
                            return publicId;
                        }

                        @Override
                        public String getEncoding() {
                            return null;
                        }

                        @Override
                        public Reader getCharacterStream() {
                            return null;
                        }

                        @Override
                        public boolean getCertifiedText() {
                            return false;
                        }

                        @Override
                        public InputStream getByteStream() {
                            return stream;
                        }

                        @Override
                        public String getBaseURI() {
                            return baseURI;
                        }
                    };
                }
                return null;
            }
        });
        Schema schema = factory.newSchema(new StreamSource(xsdXsdStream));
        Validator validator = schema.newValidator();

        CaptureErrorHandler handler = new CaptureErrorHandler();
        validator.setErrorHandler(handler);

        validator.validate(new StreamSource(repoXsdStream));
        handler.verify();
    }

    /** An helper that validates a string against an expected regexp. */
    protected void assertRegex(String expectedRegexp, String actualString) {
        assertNotNull(actualString);
        assertTrue(
                String.format("Regexp Assertion Failed:\nExpected: %s\nActual: %s\n",
                        expectedRegexp, actualString),
                actualString.matches(expectedRegexp));
    }
}
