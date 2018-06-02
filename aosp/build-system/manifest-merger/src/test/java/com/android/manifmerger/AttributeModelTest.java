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

package com.android.manifmerger;

import static org.mockito.Mockito.verify;

import com.android.utils.ILogger;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for the {@link com.android.manifmerger.AttributeModel} class
 */
public class AttributeModelTest extends TestCase {

    @Mock
    AttributeModel.Validator mValidator;

    @Mock
    XmlAttribute mXmlAttribute;

    @Mock
    ILogger mMockLog;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testGetters() {
        AttributeModel attributeModel = AttributeModel.newModel("someName")
                .setIsPackageDependent()
                .setDefaultValue("default_value")
                .setOnReadValidator(mValidator)
                .build();

        assertEquals(XmlNode.fromXmlName("android:someName"), attributeModel.getName());
        assertTrue(attributeModel.isPackageDependent());
        assertEquals("default_value", attributeModel.getDefaultValue());

        attributeModel = AttributeModel.newModel("someName").build();

        assertEquals(XmlNode.fromXmlName("android:someName"), attributeModel.getName());
        assertFalse(attributeModel.isPackageDependent());
        assertEquals(null, attributeModel.getDefaultValue());

        Mockito.verifyZeroInteractions(mValidator);
    }

    public void testBooleanValidator() {

        AttributeModel.BooleanValidator booleanValidator = new AttributeModel.BooleanValidator();
        MergingReport.Builder mergingReport = new MergingReport.Builder(mMockLog);
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "false"));
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "true"));
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "FALSE"));
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "TRUE"));
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "False"));
        assertTrue(booleanValidator.validates(mergingReport, mXmlAttribute, "True"));

        assertFalse(booleanValidator.validates(mergingReport, mXmlAttribute, "foo"));
        verify(mXmlAttribute).printPosition();
    }

    public void testMultiValuesValidator() {
        AttributeModel.MultiValueValidator multiValueValidator =
                new AttributeModel.MultiValueValidator("foo", "bar", "doh !");
        MergingReport.Builder mergingReport = new MergingReport.Builder(mMockLog);
        assertTrue(multiValueValidator.validates(mergingReport, mXmlAttribute, "foo"));
        assertTrue(multiValueValidator.validates(mergingReport, mXmlAttribute, "bar"));
        assertTrue(multiValueValidator.validates(mergingReport, mXmlAttribute, "doh !"));

        assertFalse(multiValueValidator.validates(mergingReport, mXmlAttribute, "oh no !"));
    }

    public void testIntegerValueValidator() {
        AttributeModel.IntegerValueValidator integerValueValidator =
                new AttributeModel.IntegerValueValidator();
        MergingReport.Builder mergingReport = new MergingReport.Builder(mMockLog);
        assertFalse(integerValueValidator.validates(mergingReport, mXmlAttribute, "abcd"));
        assertFalse(integerValueValidator.validates(mergingReport, mXmlAttribute,
                "123456789123456789"));
        assertFalse(integerValueValidator.validates(mergingReport, mXmlAttribute,
                "0xFFFFFFFFFFFFFFFF"));
    }

    public void testStrictMergingPolicy() {
        assertEquals("ok", AttributeModel.STRICT_MERGING_POLICY.merge("ok", "ok"));
        assertNull(AttributeModel.STRICT_MERGING_POLICY.merge("one", "two"));
    }

    public void testOrMergingPolicy() {
        assertEquals("true", AttributeModel.OR_MERGING_POLICY.merge("true", "true"));
        assertEquals("true", AttributeModel.OR_MERGING_POLICY.merge("true", "false"));
        assertEquals("true", AttributeModel.OR_MERGING_POLICY.merge("false", "true"));
        assertEquals("false", AttributeModel.OR_MERGING_POLICY.merge("false", "false"));
    }

    public void testNumericalSuperiorityPolicy() {
        assertEquals("5", AttributeModel.NO_MERGING_POLICY.merge("5", "10"));
        assertEquals("10", AttributeModel.NO_MERGING_POLICY.merge("10", "5"));
    }
}
