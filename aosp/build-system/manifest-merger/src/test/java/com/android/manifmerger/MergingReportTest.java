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

package com.android.manifmerger;

import static com.android.manifmerger.MergingReport.Record.Severity;
import static com.android.manifmerger.PlaceholderHandler.KeyBasedValueResolver;

import com.android.ide.common.blame.SourceFile;
import com.android.utils.ILogger;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

/**
 * Tests for the {@link com.android.manifmerger.MergingReport} class
 */
public class MergingReportTest extends TestCase {

    @Mock ILogger mLoggerMock;
    @Mock Element mElement;
    SourceFile mSourceLocation = new SourceFile("location");
    @Mock KeyResolver<String> mKeyResolver;
    @Mock KeyBasedValueResolver<ManifestMerger2.SystemProperty> mPropertyResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testJustError() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.ERROR,"Something bad happened")
                .build();

        assertEquals(MergingReport.Result.ERROR, mergingReport.getResult());
    }

    public void testJustWarning() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.WARNING, "Something weird happened")
                .build();

        assertEquals(MergingReport.Result.WARNING, mergingReport.getResult());
    }

    public void testJustInfo() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.INFO, "merging info")
                .build();

        assertEquals(MergingReport.Result.SUCCESS, mergingReport.getResult());
    }


    public void testJustInfoAndWarning() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.INFO, "merging info")
                .addMessage(mSourceLocation,0, 0, Severity.WARNING, "Something weird happened")
                .build();

        assertEquals(MergingReport.Result.WARNING, mergingReport.getResult());
    }

    public void testJustInfoAndError() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.INFO, "merging info")
                .addMessage(mSourceLocation,0, 0, Severity.ERROR, "something bad happened")
                .build();

        assertEquals(MergingReport.Result.ERROR, mergingReport.getResult());
    }

    public void testJustWarningAndError() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.WARNING, "something weird happened")
                .addMessage(mSourceLocation,0, 0, Severity.ERROR, "something bad happened")
                .build();

        assertEquals(MergingReport.Result.ERROR, mergingReport.getResult());
    }
    public void testAllTypes() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,0, 0, Severity.INFO, "merging info")
                .addMessage(mSourceLocation,0, 0, Severity.WARNING, "something weird happened")
                .addMessage(mSourceLocation,0, 0, Severity.ERROR, "something bad happened")
                .build();

        assertEquals(MergingReport.Result.ERROR, mergingReport.getResult());
    }

    public void testLogging() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMessage(mSourceLocation,1, 1, Severity.INFO, "merging info")
                .addMessage(mSourceLocation,1, 1, Severity.WARNING, "something weird happened")
                .addMessage(mSourceLocation,1, 1, Severity.ERROR, "something bad happened")
                .build();

        mergingReport.log(mLoggerMock);
        Mockito.verify(mLoggerMock).verbose("location:1:1 Info:\n\tmerging info");
        Mockito.verify(mLoggerMock).warning("location:1:1 Warning:\n\tsomething weird happened");
        Mockito.verify(mLoggerMock).error(null /* throwable */,
                "location:1:1 Error:\n\tsomething bad happened");
        Mockito.verify(mLoggerMock).verbose(Actions.HEADER);
        Mockito.verify(mLoggerMock).warning("\nSee http://g.co/androidstudio/manifest-merger "
                + "for more information about the manifest merger.\n");
        Mockito.verifyNoMoreInteractions(mLoggerMock);
    }

    public void testItermediaryMerges() {
        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .addMergingStage("<first/>")
                .addMergingStage("<second/>")
                .addMergingStage("<third/>")
                .build();

        ImmutableList<String> intermediaryStages = mergingReport.getIntermediaryStages();
        assertEquals(3, intermediaryStages.size());
        assertEquals("<first/>", intermediaryStages.get(0));
        assertEquals("<second/>", intermediaryStages.get(1));
        assertEquals("<third/>", intermediaryStages.get(2));
    }

    public void testGetMergedDocument() {
        XmlDocument xmlDocument =
                new XmlDocument(
                        mSourceLocation,
                        mKeyResolver,
                        mPropertyResolver,
                        mElement,
                        XmlDocument.Type.MAIN,
                        Optional.<String>absent() /* mainManifestPackageName */);

        MergingReport mergingReport = new MergingReport.Builder(mLoggerMock)
                .setMergedDocument(xmlDocument)
                .build();

        assertTrue(mergingReport.getMergedDocument().isPresent());
        assertEquals(xmlDocument, mergingReport.getMergedDocument().get());
    }
}
