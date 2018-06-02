/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.ide.common.repository;

import com.android.ide.common.res2.BaseTestCase;
import com.google.common.collect.Lists;

import java.util.List;

import static com.android.ide.common.repository.GradleCoordinate.COMPARE_PLUS_HIGHER;
import static com.android.ide.common.repository.GradleCoordinate.COMPARE_PLUS_LOWER;

/**
 * Test class for {@see GradleCoordinate}
 */
public class GradleCoordinateTest extends BaseTestCase {

    public void testParseCoordinateString() throws Exception {
        GradleCoordinate expected = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        GradleCoordinate actual = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.2");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package", 5, 4, GradleCoordinate.PLUS_REV_VALUE);
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.+");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package", 5, GradleCoordinate.PLUS_REV_VALUE);
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:5.+");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package", GradleCoordinate.PLUS_REV);
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:+");
        assertEquals(expected, actual);

        List<GradleCoordinate.RevisionComponent> revisionList =
                Lists.<GradleCoordinate.RevisionComponent>newArrayList(GradleCoordinate.PLUS_REV);
        expected = new GradleCoordinate("a.b.c", "package", revisionList,
                GradleCoordinate.ArtifactType.JAR);
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:+@jar");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package", revisionList,
                GradleCoordinate.ArtifactType.AAR);
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:+@AAR");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package",
                new GradleCoordinate.StringComponent("v1"),
                new GradleCoordinate.StringComponent("v2"));
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:v1.v2");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package",
                GradleCoordinate.ListComponent.of(
                        new GradleCoordinate.StringComponent("v1"),
                        new GradleCoordinate.NumberComponent(1)));
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:v1-1");
        assertEquals(expected, actual);

        expected = new GradleCoordinate("a.b.c", "package",
                GradleCoordinate.ListComponent.of(
                        new GradleCoordinate.StringComponent("v1"),
                        new GradleCoordinate.NumberComponent(1)),
                new GradleCoordinate.NumberComponent(17),
                GradleCoordinate.ListComponent.of(
                        new GradleCoordinate.NumberComponent(0),
                        new GradleCoordinate.StringComponent("rc"),
                        new GradleCoordinate.StringComponent("SNAPSHOT")));
        actual = GradleCoordinate.parseCoordinateString("a.b.c:package:v1-1.17.0-rc-SNAPSHOT");
        assertEquals(expected, actual);
    }

    public void testToString() throws Exception {
        String expected = "a.b.c:package:5.4.2";
        String actual = new GradleCoordinate("a.b.c", "package", 5, 4, 2).toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:5.4.+";
        actual = new GradleCoordinate("a.b.c", "package", 5, 4, GradleCoordinate.PLUS_REV_VALUE)
                .toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:5.+";
        actual = new GradleCoordinate("a.b.c", "package", 5, GradleCoordinate.PLUS_REV_VALUE)
                .toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:+";
        actual = new GradleCoordinate("a.b.c", "package", GradleCoordinate.PLUS_REV).toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:+@jar";
        List<GradleCoordinate.RevisionComponent> revisionList =
                Lists.<GradleCoordinate.RevisionComponent>newArrayList(GradleCoordinate.PLUS_REV);
        actual = new GradleCoordinate("a.b.c", "package", revisionList,
                GradleCoordinate.ArtifactType.JAR).toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:+@aar";
        actual = new GradleCoordinate("a.b.c", "package", revisionList,
                GradleCoordinate.ArtifactType.AAR).toString();
        assertEquals(expected, actual);

        expected = "com.google.maps.android:android-maps-utils:0.3";
        actual = GradleCoordinate.parseCoordinateString(expected).toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:v1.v2";
        actual = new GradleCoordinate("a.b.c", "package",
                new GradleCoordinate.StringComponent("v1"),
                new GradleCoordinate.StringComponent("v2")).toString();
        assertEquals(expected, actual);

        expected = "a.b.c:package:v1-1.17.0-rc-SNAPSHOT";
        actual = new GradleCoordinate("a.b.c", "package",
                GradleCoordinate.ListComponent.of(
                        new GradleCoordinate.StringComponent("v1"),
                        new GradleCoordinate.NumberComponent(1)),
                new GradleCoordinate.NumberComponent(17),
                GradleCoordinate.ListComponent.of(
                        new GradleCoordinate.NumberComponent(0),
                        new GradleCoordinate.StringComponent("rc"),
                        new GradleCoordinate.StringComponent("SNAPSHOT"))).toString();
        assertEquals(expected, actual);
    }

    public void testIsSameArtifact() throws Exception {
        GradleCoordinate a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        GradleCoordinate b = new GradleCoordinate("a.b.c", "package", 5, 5, 5);
        assertTrue(a.isSameArtifact(b));
        assertTrue(b.isSameArtifact(a));

        a = new GradleCoordinate("a.b", "package", 5, 4, 2);
        b = new GradleCoordinate("a.b.c", "package", 5, 5, 5);
        assertFalse(a.isSameArtifact(b));
        assertFalse(b.isSameArtifact(a));

        a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        b = new GradleCoordinate("a.b.c", "feature", 5, 5, 5);
        assertFalse(a.isSameArtifact(b));
        assertFalse(b.isSameArtifact(a));
    }

    public void testCompareVersions() {
        // Requirements order
        GradleCoordinate a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        GradleCoordinate b = new GradleCoordinate("a.b.c", "package", 5, 5, 5);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 4, 10);
        b = new GradleCoordinate("a.b.c", "package", 5, 4, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, GradleCoordinate.PLUS_REV_VALUE);
        b = new GradleCoordinate("a.b.c", "package", 6, 0, 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) == 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        b = new GradleCoordinate("a.b.c", "feature", 5, 4, 2);

        assertTrue((COMPARE_PLUS_HIGHER.compare(a, b) < 0) == ("package".compareTo("feature") < 0));

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, 6, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);

        a = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.2");
        b = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.+");
        assert a != null;
        assert b != null;
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);

        a = GradleCoordinate.parseCoordinateString("a.b.c:package:5");
        b = GradleCoordinate.parseCoordinateString("a.b.c:package:+");
        assert a != null;
        assert b != null;
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);

        a = GradleCoordinate.parseCoordinateString("a.b.c:package:1.any");
        b = GradleCoordinate.parseCoordinateString("a.b.c:package:1.1");
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);

        a = GradleCoordinate.parseCoordinateString("a.b.c:package:1-1");
        b = GradleCoordinate.parseCoordinateString("a.b.c:package:1-2");
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);
    }

    public void testCompareSpecificity() {
        // Order of specificity
        GradleCoordinate a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        GradleCoordinate b = new GradleCoordinate("a.b.c", "package", 5, 5, 5);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(b, a) > 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 4, 10);
        b = new GradleCoordinate("a.b.c", "package", 5, 4, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) > 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, GradleCoordinate.PLUS_REV_VALUE);
        b = new GradleCoordinate("a.b.c", "package", 6, 0, 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) < 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) == 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        b = new GradleCoordinate("a.b.c", "feature", 5, 4, 2);

        assertTrue((COMPARE_PLUS_LOWER.compare(a, b) < 0) == ("package".compareTo("feature") < 0));

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, 6, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) > 0);

        a = new GradleCoordinate("a.b.c", "package", 5, 6, 0);
        b = new GradleCoordinate("a.b.c", "package", 5, GradleCoordinate.PLUS_REV_VALUE);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) > 0);

        a = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.2");
        b = GradleCoordinate.parseCoordinateString("a.b.c:package:5.4.+");
        assert a != null;
        assert b != null;
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) > 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(b, a) < 0);
    }

    public void testGetVersions() {
        GradleCoordinate c = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        assertEquals(5, c.getMajorVersion());
        assertEquals(4, c.getMinorVersion());
        assertEquals(2, c.getMicroVersion());
    }

    public void testSameSharedDifferentLengths() {
        GradleCoordinate a = new GradleCoordinate("a.b.c", "package", 5, 4);
        GradleCoordinate b = new GradleCoordinate("a.b.c", "package", 5, 4, 2);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) > 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) < 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(b, a) > 0);
    }

    public void testSameSharedDifferentLengthsWithZeros() {
        GradleCoordinate a = new GradleCoordinate("a.b.c", "package", 5, 4);
        GradleCoordinate b = new GradleCoordinate("a.b.c", "package", 5, 4, 0, 0, 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(a, b) == 0);
        assertTrue(COMPARE_PLUS_HIGHER.compare(b, a) == 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(a, b) == 0);
        assertTrue(COMPARE_PLUS_LOWER.compare(b, a) == 0);
    }

    public void testParseVersionOnly() {
        String revision = "15.32.64";
        GradleCoordinate a = GradleCoordinate.parseVersionOnly(revision);
        assertEquals(revision, a.getFullRevision());
        String revisionB = "16.12.0-rc1";
        GradleCoordinate b = GradleCoordinate.parseVersionOnly(revisionB);
        assertEquals(revisionB, b.getFullRevision());
        assertTrue(b.isPreview());
    }

    public void testIsPreview_ignoresFalsePositives() {
      String revisionB = "16.12.0-march";
      GradleCoordinate b = GradleCoordinate.parseVersionOnly(revisionB);
      assertFalse(b.isPreview());
    }

    public void testLeadingZeroes() {
        // Regression test for https://code.google.com/p/android/issues/detail?id=74612
        // The Gradle dependency
        //   compile 'com.google.android.gms:play-services:5.2.08'
        // is not the same as
        //   compile 'com.google.android.gms:play-services:5.2.8'
        // So we have to keep string representations around

        GradleCoordinate v5_0_89 = GradleCoordinate.parseCoordinateString(
                "com.google.android.gms:play-services:5.0.89");
        assertNotNull(v5_0_89);
        assertEquals("com.google.android.gms:play-services:5.0.89", v5_0_89.toString());
        assertEquals("5.0.89", v5_0_89.getFullRevision());

        GradleCoordinate v5_2_08 = GradleCoordinate.parseCoordinateString(
                "com.google.android.gms:play-services:5.2.08");
        assertNotNull(v5_2_08);
        assertEquals("5.2.08", v5_2_08.getFullRevision());

        assertEquals("com.google.android.gms:play-services:5.2.08", v5_2_08.toString());

        // Same artifact: 5.2.08 == 5.2.8
        //noinspection ConstantConditions
        assertFalse(v5_2_08.equals(GradleCoordinate.parseCoordinateString(
                "com.google.android.gms:play-services:5.2.8")));

        assertEquals(
                GradleCoordinate.parseCoordinateString(
                        "com.google.android.gms:play-services:5.2.08"),
                GradleCoordinate.parseCoordinateString(
                        "com.google.android.gms:play-services:5.2.08"));

    }
}