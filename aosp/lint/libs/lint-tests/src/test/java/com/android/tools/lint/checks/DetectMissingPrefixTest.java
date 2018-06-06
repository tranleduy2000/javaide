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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class DetectMissingPrefixTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DetectMissingPrefix();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/namespace.xml:2: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" xmlns:other=\"http://foo.bar\" android:id=\"@+id/newlinear\" android:orientation=\"vertical\" android:layout_width=\"match_parent\" android:layout_height=\"match_parent\" orientation=\"true\">\n" +
            "                                                                                                                                                                                                                                          ~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/namespace.xml:3: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "    <Button style=\"@style/setupWizardOuterFrame\" android.text=\"Button\" android:id=\"@+id/button1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"></Button>\n" +
            "                                                 ~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/namespace.xml:5: Error: Unexpected namespace prefix \"other\" found for tag LinearLayout [MissingPrefix]\n" +
            "    <LinearLayout other:orientation=\"horizontal\"/>\n" +
            "                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "3 errors, 0 warnings\n",

            lintFiles("res/layout/namespace.xml"));
    }

    public void testCustomNamespace() throws Exception {
        assertEquals(
            "res/layout/namespace2.xml:9: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "    orientation=\"true\">\n" +
            "    ~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n",

            lintFiles("res/layout/namespace2.xml"));
    }

    public void testManifest() throws Exception {
        assertEquals(
            "AndroidManifest.xml:4: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "    versionCode=\"1\"\n" +
            "    ~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:11: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "        android.label=\"@string/app_name\" >\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:18: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "                <category name=\"android.intent.category.LAUNCHER\" />\n" +
            "                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "3 errors, 0 warnings\n",

            lintFiles("missingprefix.xml=>AndroidManifest.xml"));
    }

    public void testLayoutAttributes() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/namespace3.xml"));
    }

    public void testLayoutAttributes2() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/namespace4.xml"));
    }

    public void testUnusedNamespace() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject("res/layout/message_edit_detail.xml"));
    }

    public void testMissingLayoutAttribute() throws Exception {
        assertEquals(
            "res/layout/rtl.xml:7: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "        layout_gravity=\"left\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/rtl.xml:8: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "        layout_alignParentLeft=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"  +
            "res/layout/rtl.xml:9: Error: Attribute is missing the Android namespace prefix [MissingPrefix]\n" +
            "        editable=\"false\"\n" +
            "        ~~~~~~~~~~~~~~~~\n" +
            "3 errors, 0 warnings\n",

            lintProject(
                    "overdraw/project.properties=>project.properties",
                    "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                    "rtl/rtl_noprefix.xml=>res/layout/rtl.xml"
            ));
    }

    public void testDataBinding() throws Exception {
        assertEquals("No warnings.",
                lintProject(xml("res/layout/test.xml", "\n"
                        + "<layout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        + "    xmlns:bind=\"http://schemas.android.com/apk/res-auto\"\n"
                        + "    xmlns:tools=\"http://schemas.android.com/tools\">\n"
                        + "    <data>\n"
                        + "        <variable name=\"activity\" type=\"com.android.example.bindingdemo.MainActivity\"/>\n"
                        + "        <!---->\n"
                        + "        <import\n"
                        + "            type=\"android.view.View\"\n"
                        + "            />\n"
                        + "        <!---->\n"
                        + "        <import type=\"com.android.example.bindingdemo.R.string\" alias=\"Strings\"/>\n"
                        + "        <import type=\"com.android.example.bindingdemo.vo.User\"/>\n"
                        + "    </data>\n"
                        + "    <LinearLayout\n"
                        + "        android:layout_width=\"match_parent\"\n"
                        + "        android:layout_height=\"match_parent\"\n"
                        + "        android:orientation=\"vertical\"\n"
                        + "        android:id=\"@+id/activityRoot\"\n"
                        + "        android:clickable=\"true\"\n"
                        + "        android:onClickListener=\"@{activity.onUnselect}\">\n"
                        + "        <android.support.v7.widget.CardView\n"
                        + "            android:id=\"@+id/selected_card\"\n"
                        + "            bind:contentPadding=\"@{activity.selected == null ? 5 : activity.selected.name.length()}\"\n"
                        + "            android:layout_width=\"match_parent\"\n"
                        + "            android:layout_height=\"wrap_content\"\n"
                        + "            bind:visibility=\"@{activity.selected == null ? View.INVISIBLE : View.VISIBLE}\">\n"
                        + "\n"
                        + "            <GridLayout\n"
                        + "                android:layout_width=\"match_parent\"\n"
                        + "                android:layout_height=\"wrap_content\"\n"
                        + "                android:columnCount=\"2\"\n"
                        + "                android:rowCount=\"4\">\n"
                        + "                <Button\n"
                        + "                    android:id=\"@+id/edit_button\"\n"
                        + "                    bind:onClickListener=\"@{activity.onSave}\"\n"
                        + "                    android:text='@{\"Save changes to \" + activity.selected.name}'\n"
                        + "                    android:layout_width=\"wrap_content\"\n"
                        + "                    android:layout_height=\"wrap_content\"\n"
                        + "                    android:layout_column=\"1\"\n"
                        + "                    android:layout_gravity=\"right\"\n"
                        + "                    android:layout_row=\"2\"/>\n"
                        + "            </GridLayout>\n"
                        + "        </android.support.v7.widget.CardView>"
                        + "    </LinearLayout>\n"
                        + "</layout>")));
    }
}
