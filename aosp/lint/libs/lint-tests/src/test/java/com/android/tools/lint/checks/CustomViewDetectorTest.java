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

public class CustomViewDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new CustomViewDetector();
    }

    public void test() throws Exception {
        assertEquals(""
            + "src/test/pkg/CustomView1.java:18: Warning: By convention, the custom view (CustomView1) and the declare-styleable (MyDeclareStyleable) should have the same name (various editor features rely on this convention) [CustomViewStyleable]\n"
            + "        context.obtainStyledAttributes(R.styleable.MyDeclareStyleable);\n"
            + "                                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/CustomView1.java:19: Warning: By convention, the custom view (CustomView1) and the declare-styleable (MyDeclareStyleable) should have the same name (various editor features rely on this convention) [CustomViewStyleable]\n"
            + "        context.obtainStyledAttributes(defStyleRes, R.styleable.MyDeclareStyleable);\n"
            + "                                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/CustomView1.java:20: Warning: By convention, the custom view (CustomView1) and the declare-styleable (MyDeclareStyleable) should have the same name (various editor features rely on this convention) [CustomViewStyleable]\n"
            + "        context.obtainStyledAttributes(attrs, R.styleable.MyDeclareStyleable);\n"
            + "                                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/CustomView1.java:21: Warning: By convention, the custom view (CustomView1) and the declare-styleable (MyDeclareStyleable) should have the same name (various editor features rely on this convention) [CustomViewStyleable]\n"
            + "        context.obtainStyledAttributes(attrs, R.styleable.MyDeclareStyleable, defStyleAttr,\n"
            + "                                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/CustomView1.java:46: Warning: By convention, the declare-styleable (MyLayout) for a layout parameter class (MyLayoutParams) is expected to be the surrounding class (MyLayout) plus \"_Layout\", e.g. MyLayout_Layout. (Various editor features rely on this convention.) [CustomViewStyleable]\n"
            + "                context.obtainStyledAttributes(R.styleable.MyLayout); // Wrong\n"
            + "                                               ~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/CustomView1.java:47: Warning: By convention, the declare-styleable (MyDeclareStyleable) for a layout parameter class (MyLayoutParams) is expected to be the surrounding class (MyLayout) plus \"_Layout\", e.g. MyLayout_Layout. (Various editor features rely on this convention.) [CustomViewStyleable]\n"
            + "                context.obtainStyledAttributes(R.styleable.MyDeclareStyleable); // Wrong\n"
            + "                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 6 warnings\n",

            lintProject("src/test/pkg/CustomView1.java.txt=>" +
                    "src/test/pkg/CustomView1.java"));
    }
}
