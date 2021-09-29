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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class ViewHolderDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ViewHolderDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/ViewHolderTest.java:42: Warning: Unconditional layout inflation from view adapter: Should use View Holder pattern (use recycled view passed into this method as the second parameter) for smoother scrolling [ViewHolder]\n"
                + "            convertView = mInflater.inflate(R.layout.your_layout, null);\n"
                + "                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                    "src/test/pkg/ViewHolderTest.java.txt=>src/test/pkg/ViewHolderTest.java"));
    }
}
