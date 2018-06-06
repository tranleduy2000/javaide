/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.io.File;
import java.io.IOException;

@SuppressWarnings("javadoc")
public class LocationTest extends TestCase {
    public void testReverse() throws IOException {
        File file1 = new File("parent/location1");
        File file2 = new File("parent/location2");
        File file3 = new File("parent/location3");
        File file4 = new File("parent/location4");

        Location location1 = Location.create(file1);
        Location location2 = Location.create(file2);
        Location location3 = Location.create(file3);
        Location location4 = Location.create(file4);

        // 1-element location list
        assertSame(location1, Location.reverse(location1));
        assertFalse(containsCycle(location1));

        // 2-element location list
        location1.setSecondary(location2);
        assertSame(location2, Location.reverse(location1));
        assertFalse(containsCycle(location2));
        assertSame(location1, location2.getSecondary());

        // 3-element location list
        location1.setSecondary(location2);
        location2.setSecondary(location3);
        assertSame(location3, Location.reverse(location1));
        assertFalse(containsCycle(location3));
        assertSame(location2, location3.getSecondary());
        assertSame(location1, location2.getSecondary());

        // 4-element location list
        location1.setSecondary(location2);
        location2.setSecondary(location3);
        location3.setSecondary(location4);
        assertSame(location4, Location.reverse(location1));
        assertFalse(containsCycle(location4));
        assertSame(location3, location4.getSecondary());
        assertSame(location2, location3.getSecondary());
        assertSame(location1, location2.getSecondary());
    }

    public void testFaen() throws Exception {
        File[] paths = new File[] {
                new File("values-zh-rTW/arrays.xml"), new File("values-zh-rCN/arrays.xml"),
                new File("values-vi/arrays.xml"), new File("values-uk/arrays.xml"),
                new File("values-tr/arrays.xml"), new File("values-tl/arrays.xml"),
                new File("values-th/arrays.xml"), new File("values-sv/arrays.xml"),
                new File("values-sr/arrays.xml"), new File("values-sl/arrays.xml"),
                new File("values-sk/arrays.xml"), new File("values-ru/arrays.xml"),
                new File("values-ro/arrays.xml"), new File("values-rm/arrays.xml"),
                new File("values-pt-rPT/arrays.xml"), new File("values-pt/arrays.xml"),
                new File("values-pl/arrays.xml"), new File("values-nl/arrays.xml"),
                new File("values-nb/arrays.xml"), new File("values-lv/arrays.xml"),
                new File("values-lt/arrays.xml"), new File("values-ko/arrays.xml"),
                new File("values-ja/arrays.xml"), new File("values-iw/arrays.xml"),
                new File("values-it/arrays.xml"), new File("values-in/arrays.xml"),
                new File("values-hu/arrays.xml"), new File("values-hr/arrays.xml"),
                new File("values-fr/arrays.xml"), new File("values-fi/arrays.xml"),
                new File("values-fa/arrays.xml"), new File("values-es-rUS/arrays.xml"),
                new File("values-es/arrays.xml"), new File("values-en-rGB/arrays.xml"),
                new File("values-el/arrays.xml"), new File("values-de/arrays.xml"),
                new File("values-da/arrays.xml"), new File("values-cs/arrays.xml"),
                new File("values-ca/arrays.xml"), new File("values-bg/arrays.xml"),
                new File("values-ar/arrays.xml"), new File("values/arrays.xml")
        };

        Location last = null;
        for (int i = paths.length - 1; i >= 0; i--) {
            Location location = Location.create(paths[i]);
            location.setSecondary(last);
            last = location;
        }

        assertFalse(containsCycle(last));
        Location.reverse(last);
        assertFalse(containsCycle(last));
    }

    private static boolean containsCycle(Location location) {
        // Make sure there's no cycle: iterate
        Location a = location;
        Location b = location;

        while (true) {
            b = b.getSecondary();
            if (b == null) {
                // OK! Found list end
                return false;
            }
            if (b == a) {
                return true;
            }
            b = b.getSecondary();
            if (b == null) {
                // OK! Found list end
                return false;
            }
            if (b == a) {
                return true;
            }

            a = a.getSecondary();
            assert a != null;
        }
    }
}
