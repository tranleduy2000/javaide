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

package com.android.sdklib.devices;

import com.android.dvlib.DeviceSchemaTest;

import com.google.common.collect.Table;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DeviceWriterTest extends TestCase {

    public void testWriteIsValid_Minimal() throws Exception {
        InputStream devicesFile =
            DeviceSchemaTest.class.getResourceAsStream("devices_minimal.xml");
        Table<String, String, Device> devices = DeviceParser.parse(devicesFile);
        assertEquals("Parsed devices contained an un expected number of devices",
                1, devices.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        String written = baos.toString();
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(written.getBytes()));
        assertEquals(devices, writtenDevices);
    }

    public void testWriteIsValid_Full() throws Exception {
        InputStream devicesFile =
            DeviceSchemaTest.class.getResourceAsStream("devices.xml");
        Table<String, String, Device> devices = DeviceParser.parse(devicesFile);
        assertEquals("Parsed devices contained an unexpected number of devices",
                4, devices.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        String written = baos.toString();
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
          new ByteArrayInputStream(written.getBytes()));
        assertEquals(devices, writtenDevices);
    }

    public void testLocale() throws Exception {
        Locale prevLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.FRANCE);
            InputStream devicesFile =
                DeviceSchemaTest.class.getResourceAsStream("devices.xml");
            Table<String, String, Device> devices = DeviceParser.parse(devicesFile);
            assertEquals("Parsed devices contained an unexpected number of devices",
                    4, devices.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeviceWriter.writeToXml(baos, devices.values());
            String xml = baos.toString();
            assertTrue(xml.contains(".00"));
            assertFalse(xml.contains(",00"));
        } finally {
            Locale.setDefault(prevLocale);
        }
    }

    public void testApiLowerBound() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("api-level", "1-");
        InputStream stream = DeviceSchemaTest.getReplacedStream(replacements);
        Table<String, String, Device> devices = DeviceParser.parse(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(baos.toString().getBytes()));
        assertEquals(devices, writtenDevices);
    }

    public void testApiUpperBound() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("api-level", "-10");
        InputStream stream = DeviceSchemaTest.getReplacedStream(replacements);
        Table<String, String, Device> devices = DeviceParser.parse(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(baos.toString().getBytes()));
        assertEquals(devices, writtenDevices);
    }

    public void testApiNeitherBound() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("api-level", "-");
        InputStream stream = DeviceSchemaTest.getReplacedStream(replacements);
        Table<String, String, Device> devices = DeviceParser.parse(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(baos.toString().getBytes()));
        assertEquals(devices, writtenDevices);
    }

    public void testApiBothBound() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("api-level", "9-10");
        InputStream stream = DeviceSchemaTest.getReplacedStream(replacements);
        Table<String, String, Device> devices = DeviceParser.parse(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(baos.toString().getBytes()));
        assertEquals(devices, writtenDevices);
    }
    public void testApiSingle() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("api-level", "10");
        InputStream stream = DeviceSchemaTest.getReplacedStream(replacements);
        Table<String, String, Device> devices = DeviceParser.parse(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeviceWriter.writeToXml(baos, devices.values());
        Table<String, String, Device> writtenDevices = DeviceParser.parse(
                new ByteArrayInputStream(baos.toString().getBytes()));
        assertEquals(devices, writtenDevices);
    }
}
