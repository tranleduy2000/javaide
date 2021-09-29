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

package com.android.tools.lint.detector.api;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class ClassContextTest extends TestCase {
    public void testCreateSignature() {
        assertEquals("foo.bar.Foo.Bar",
                ClassContext.createSignature("foo/bar/Foo$Bar", null, null));
        assertEquals("void foo.bar.Foo.Bar#name(int)",
                ClassContext.createSignature("foo/bar/Foo$Bar", "name", "(I)V"));
        assertEquals("void foo.bar.Foo.Bar#name(Integer)",
                ClassContext.createSignature("foo/bar/Foo$Bar", "name", "(Ljava/lang/Integer;)V"));
    }

    public void testGetInternalName() {
        assertEquals("foo/bar/Foo$Bar",
                ClassContext.getInternalName("foo.bar.Foo.Bar"));
    }

    public void testGetFqcn() {
        assertEquals("foo.bar.Foo.Bar", ClassContext.getFqcn("foo/bar/Foo$Bar"));
    }
}
