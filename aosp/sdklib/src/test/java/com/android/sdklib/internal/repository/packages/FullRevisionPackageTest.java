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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.PkgProps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import junit.framework.TestCase;

public class FullRevisionPackageTest extends TestCase {

    /**
     * Helper that creates the {@link Properties} from a {@link FullRevision}
     * as expected by {@link FullRevisionPackage}.
     */
    public static Properties createProps(FullRevision revision) {
        Properties props = new Properties();
        if (revision != null) {
            props.setProperty(PkgProps.PKG_REVISION, revision.toString());
        }
        return props;
    }

    public void testCompareTo() throws Exception {
        // Test order of full revision packages.
        //
        // Note that Package.compareTo() is designed to return the desired
        // ordering for a list display and as such a final/release package
        // needs to be listed before its rc/preview package.
        //
        // This differs from the order used by FullRevision.compareTo().

        ArrayList<Package> list = new ArrayList<Package>();

        list.add(new MockToolPackage(null, new FullRevision(1, 0, 0, 0), 8));
        list.add(new MockToolPackage(null, new FullRevision(1, 0, 0, 1), 8));
        list.add(new MockToolPackage(null, new FullRevision(1, 0, 1, 0), 8));
        list.add(new MockToolPackage(null, new FullRevision(1, 0, 1, 1), 8));
        list.add(new MockToolPackage(null, new FullRevision(1, 1, 0, 0), 8));
        list.add(new MockToolPackage(null, new FullRevision(1, 1, 0, 1), 8));
        list.add(new MockToolPackage(null, new FullRevision(2, 1, 1, 0), 8));
        list.add(new MockToolPackage(null, new FullRevision(2, 1, 1, 1), 8));

        Collections.sort(list);

        assertEquals(
                 "[Android SDK Tools, revision 1, " +
                  "Android SDK Tools, revision 1 rc1, " +
                  "Android SDK Tools, revision 1.0.1, " +
                  "Android SDK Tools, revision 1.0.1 rc1, " +
                  "Android SDK Tools, revision 1.1, " +
                  "Android SDK Tools, revision 1.1 rc1, " +
                  "Android SDK Tools, revision 2.1.1, " +
                  "Android SDK Tools, revision 2.1.1 rc1]",
                Arrays.toString(list.toArray()));
    }
}
