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

package com.android.sdklib.internal.repository.archives;

import junit.framework.TestCase;

public class BitSizeTest extends TestCase {

    public void testGetSize() {
        assertEquals(32, BitSize._32.getSize());
        assertEquals(64, BitSize._64.getSize());
    }

    public void testGetXmlName() {
        assertEquals("32", BitSize._32.getXmlName());
        assertEquals("64", BitSize._64.getXmlName());
    }

}
