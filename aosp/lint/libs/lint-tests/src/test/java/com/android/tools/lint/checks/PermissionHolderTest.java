/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.sdklib.AndroidVersion;
import com.android.tools.lint.checks.PermissionHolder.SetPermissionLookup;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.Set;

public class PermissionHolderTest extends TestCase {
    public void test() {
        assertTrue(new SetPermissionLookup(Sets.newHashSet("foo")).hasPermission("foo"));
        assertTrue(new SetPermissionLookup(Sets.newHashSet("foo","bar")).hasPermission("foo"));
        assertTrue(new SetPermissionLookup(Sets.newHashSet("foo","bar")).hasPermission("bar"));
        assertFalse(new SetPermissionLookup(Sets.newHashSet("foo")).hasPermission("bar"));
        assertFalse(new SetPermissionLookup(Sets.newHashSet("foo")).hasPermission(""));
        assertFalse(new SetPermissionLookup(Sets.<String>newHashSet()).hasPermission(""));

        Set<String> empty = Sets.newHashSet();
        assertFalse(new SetPermissionLookup(Sets.newHashSet("foo"), empty).isRevocable("foo"));
        assertTrue(new SetPermissionLookup(empty, Sets.newHashSet("foo")).isRevocable("foo"));
        assertFalse(new SetPermissionLookup(empty, Sets.newHashSet("foo")).isRevocable("bar"));

        SetPermissionLookup lookup1 = new SetPermissionLookup(Sets.newHashSet("foo", "bar"));
        assertTrue(SetPermissionLookup.join(lookup1, Sets.newHashSet("baz")).hasPermission("foo"));
        assertTrue(SetPermissionLookup.join(lookup1, Sets.newHashSet("baz")).hasPermission("bar"));
        assertTrue(SetPermissionLookup.join(lookup1, Sets.newHashSet("baz")).hasPermission("baz"));
        assertFalse(SetPermissionLookup.join(lookup1, Sets.newHashSet("baz")).hasPermission("a"));

        AndroidVersion version = new AndroidVersion(5, null);
        assertSame(version, new SetPermissionLookup(Sets.newHashSet("foo"), Sets.newHashSet("bar"),
                version, AndroidVersion.DEFAULT).getMinSdkVersion());
        assertSame(version, new SetPermissionLookup(Sets.newHashSet("foo"), Sets.newHashSet("bar"),
                AndroidVersion.DEFAULT, version).getTargetSdkVersion());
    }
}