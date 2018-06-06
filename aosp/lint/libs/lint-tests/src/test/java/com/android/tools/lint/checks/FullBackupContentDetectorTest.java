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

import com.android.tools.lint.detector.api.Detector;

public class FullBackupContentDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new FullBackupContentDetector();
    }

    public void testOk() throws Exception {
        assertEquals("No warnings.",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <include domain=\"file\" path=\"dd\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/ss/foo.txt\"/>\n"
                        + "</full-backup-content>")));
    }

    public void test20890435() throws Exception {
        assertEquals(""
                + "res/xml/backup.xml:6: Error: foo.xml is not in an included path [FullBackupContent]\n"
                + "     <exclude domain=\"sharedpref\" path=\"foo.xml\"/>\n"
                + "                                        ~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <include domain=\"file\" path=\"dd\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/ss/foo.txt\"/>\n"
                        + "     <exclude domain=\"sharedpref\" path=\"foo.xml\"/>\n"
                        + "</full-backup-content>")));
    }

    public void testImplicitInclude() throws Exception {
        // If there is no include, then everything is considered included
        assertEquals("No warnings.",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                        + "</full-backup-content>")));
    }

    public void testImplicitPath() throws Exception {
        // If you specify an include, but no path attribute, that's defined to mean include
        // everything
        assertEquals("No warnings.",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <include domain=\"file\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                        + "     <include domain=\"sharedpref\" path=\"something\"/>\n"
                        + "</full-backup-content>")));
    }

    public void testSuppressed() throws Exception {
        assertEquals("No warnings.",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content xmlns:tools=\"http://schemas.android.com/tools\">\n"
                        + "     <include domain=\"file\" path=\"dd\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                        + "     <exclude domain=\"file\" path=\"dd/ss/foo.txt\"/>\n"
                        + "     <exclude domain=\"sharedpref\" path=\"foo.xml\" tools:ignore=\"FullBackupContent\"/>\n"
                        + "</full-backup-content>")));
    }

    public void testIncludeWrongDomain() throws Exception {
        // Ensure that the path prefix check is done independently for each domain
        assertEquals(""
                + "res/xml/backup.xml:4: Error: abc/def.txt is not in an included path [FullBackupContent]\n"
                + "     <exclude domain=\"external\" path=\"abc/def.txt\"/>\n"
                + "                                      ~~~~~~~~~~~\n"
                + "res/xml/backup.xml:6: Error: def/ghi.txt is not in an included path [FullBackupContent]\n"
                + "     <exclude domain=\"external\" path=\"def/ghi.txt\"/>\n"
                + "                                      ~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <include domain=\"file\" path=\"abc\"/>\n"
                        + "     <exclude domain=\"external\" path=\"abc/def.txt\"/>\n"
                        + "     <include domain=\"file\" path=\"def\"/>\n"
                        + "     <exclude domain=\"external\" path=\"def/ghi.txt\"/>\n"
                        + "</full-backup-content>")));
    }

    public void testValidation() throws Exception {
        assertEquals(""
                + "res/xml/backup.xml:7: Error: Subdirectories are not allowed for domain sharedpref [FullBackupContent]\n"
                + "     <include domain=\"sharedpref\" path=\"dd/subdir\"/>\n"
                + "                                        ~~~~~~~~~\n"
                + "res/xml/backup.xml:8: Error: Paths are not allowed to contain .. [FullBackupContent]\n"
                + "     <include domain=\"file\" path=\"../outside\"/>\n"
                + "                                  ~~~~~~~~~~\n"
                + "res/xml/backup.xml:9: Error: Paths are not allowed to contain // [FullBackupContent]\n"
                + "     <include domain=\"file\" path=\"//wrong\"/>\n"
                + "                                  ~~~~~~~\n"
                + "res/xml/backup.xml:11: Error: Include dd is also excluded [FullBackupContent]\n"
                + "     <exclude domain=\"external\" path=\"dd\"/>\n"
                + "                                      ~~\n"
                + "    res/xml/backup.xml:10: Unnecessary/conflicting <include>\n"
                + "res/xml/backup.xml:12: Error: Unexpected domain unknown-domain, expected one of root, file, database, sharedpref, external [FullBackupContent]\n"
                + "     <exclude domain=\"unknown-domain\" path=\"dd\"/>\n"
                + "                      ~~~~~~~~~~~~~~\n"
                + "res/xml/backup.xml:12: Error: dd is not in an included path [FullBackupContent]\n"
                + "     <exclude domain=\"unknown-domain\" path=\"dd\"/>\n"
                + "                                            ~~\n"
                + "res/xml/backup.xml:13: Error: Missing domain attribute, expected one of root, file, database, sharedpref, external [FullBackupContent]\n"
                + "     <include path=\"dd\"/>\n"
                + "     ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/xml/backup.xml:15: Error: Unexpected element <wrongtag> [FullBackupContent]\n"
                + "     <wrongtag />\n"
                + "      ~~~~~~~~\n"
                + "8 errors, 0 warnings\n",

                lintProject(xml("res/xml/backup.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<full-backup-content>\n"
                        + "     <include domain=\"root\" path=\"dd\"/>\n" // OK
                        + "     <include domain=\"file\" path=\"dd\"/>\n" // OK
                        + "     <include domain=\"database\" path=\"dd\"/>\n" // OK
                        + "     <include domain=\"sharedpref\" path=\"dd\"/>\n" // OK
                        + "     <include domain=\"sharedpref\" path=\"dd/subdir\"/>\n" // Not allowed
                        + "     <include domain=\"file\" path=\"../outside\"/>\n" // Not allowed
                        + "     <include domain=\"file\" path=\"//wrong\"/>\n" // Not allowed
                        + "     <include domain=\"external\" path=\"dd\"/>\n" // OK
                        + "     <exclude domain=\"external\" path=\"dd\"/>\n" // same as included
                        + "     <exclude domain=\"unknown-domain\" path=\"dd\"/>\n"
                        + "     <include path=\"dd\"/>\n" // No domain
                        + "     <include domain=\"root\" />\n" // OK (means include everything
                        + "     <wrongtag />\n"
                        + "</full-backup-content>")));
    }
}