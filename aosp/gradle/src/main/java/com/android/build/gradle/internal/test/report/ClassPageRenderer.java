/*
 * Copyright 2011 the original author or authors.
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

package com.android.build.gradle.internal.test.report;

import static org.gradle.api.tasks.testing.TestResult.ResultType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom ClassPageRenderer based on Gradle's ClassPageRenderer
 */
class ClassPageRenderer extends PageRenderer<ClassTestResults> {
    private final CodePanelRenderer codePanelRenderer = new CodePanelRenderer();

    ClassPageRenderer(ReportType reportType) {
        super(reportType);
    }

    @Override
    protected String getTitle() {
        return getModel().getTitle();
    }

    @Override
    protected void renderBreadcrumbs(SimpleHtmlWriter htmlWriter) throws IOException {
        htmlWriter.startElement("div").attribute("class", "breadcrumbs")
                .startElement("a").attribute("href", "index.html").characters("all").endElement()
                .characters(" > ")
                .startElement("a").attribute("href", String.format("%s.html", getResults().getPackageResults().getFilename(reportType))).characters(getResults().getPackageResults().getName()).endElement()
                .characters(String.format(" > %s", getResults().getSimpleName()))
        .endElement();
    }

    private void renderTests(SimpleHtmlWriter htmlWriter) throws IOException {
        htmlWriter.startElement("table")
                .startElement("thead")
                .startElement("tr")
                .startElement("th").characters("Test").endElement();

        // get all the results per device and per test name
        Map<String, Map<String, TestResult>> results = getResults().getTestResultsMap();

        // gather all devices.
        List<String> devices = Lists.newArrayList(results.keySet());
        Collections.sort(devices);

        for (String device : devices) {
            htmlWriter.startElement("th").characters(device).endElement();
        }
        htmlWriter.endElement().endElement(); // tr/thead

        // gather all tests
        Set<String> tests = Sets.newHashSet();
        for (Map<String, TestResult> deviceMap : results.values()) {
            tests.addAll(deviceMap.keySet());
        }
        List<String> sortedTests = Lists.newArrayList(tests);
        Collections.sort(sortedTests);

        for (String testName : sortedTests) {
            htmlWriter.startElement("tr").startElement("td").characters(testName).endElement();

            ResultType currentType = ResultType.SKIPPED;

            // loop for all devices to find this test and put its result
            for (String device : devices) {
                Map<String, TestResult> deviceMap = results.get(device);
                TestResult test = deviceMap.get(testName);

                htmlWriter.startElement("td").attribute("class", test.getStatusClass())
                        .characters(String.format("%s (%s)",
                            test.getFormattedResultType(), test.getFormattedDuration()))
                .endElement();

                currentType = combineResultType(currentType, test.getResultType());
            }

            // finally based on whether if a single test failed, set the class on the test name.
//todo            td.setAttribute("class", getStatusClass(currentType));

            htmlWriter.endElement(); //tr
        }
        htmlWriter.endElement(); // table
    }

    public static ResultType combineResultType(ResultType currentType, ResultType newType) {
        switch (currentType) {
            case SUCCESS:
                if (newType == ResultType.FAILURE) {
                    return newType;
                }

                return currentType;
            case FAILURE:
                return currentType;
            case SKIPPED:
                if (newType != ResultType.SKIPPED) {
                    return newType;
                }
                return currentType;
            default:
                throw new IllegalStateException();
        }
    }

    public String getStatusClass(ResultType resultType) {
        switch (resultType) {
            case SUCCESS:
                return "success";
            case FAILURE:
                return "failures";
            case SKIPPED:
                return "skipped";
            default:
                throw new IllegalStateException();
        }
    }

    private static final class TestPercent {
        int failed;
        int total;
        TestPercent(int failed, int total) {
            this.failed = failed;
            this.total = total;
        }

        boolean isFullFailure() {
            return failed == total;
        }
    }

    @Override
    protected void renderFailures(SimpleHtmlWriter htmlWriter) throws IOException {
        // get all the results per device and per test name
        Map<String, Map<String, TestResult>> results = getResults().getTestResultsMap();

        Map<String, TestPercent> testPassPercent = Maps.newHashMap();

        for (TestResult test : getResults().getFailures()) {
            String testName = test.getName();
            // compute the display name which will include the name of the device and how many
            // devices are impact so to not force counting.
            // If all devices, then we don't display all of them.
            // (The off chance that all devices fail the test with a different stack trace is slim)
            TestPercent percent = testPassPercent.get(testName);
            if (percent != null && percent.isFullFailure()) {
                continue;
            }

            if (percent == null) {
                int failed = 0;
                int total = 0;
                for (Map<String, TestResult> deviceMap : results.values()) {
                    ResultType resultType = deviceMap.get(testName).getResultType();

                    if (resultType == ResultType.FAILURE) {
                        failed++;
                    }

                    if (resultType != ResultType.SKIPPED) {
                        total++;
                    }
                }

                percent = new TestPercent(failed, total);
                testPassPercent.put(testName, percent);
            }

            String name;
            if (percent.total == 1) {
                name = testName;
            } else if (percent.isFullFailure()) {
                name = testName + " [all devices]";
            } else {
                name = String.format("%s [%s] (on %d/%d devices)", testName, test.getDevice(),
                        percent.failed, percent.total);
            }

            htmlWriter.startElement("div").attribute("class", "test")
                .startElement("a").attribute("name", test.getId().toString()).characters("").endElement() //browsers dont understand <a name="..."/>
                    .startElement("h3").attribute("class", test.getStatusClass()).characters(name).endElement();
            for (TestResult.TestFailure failure : test.getFailures()) {
                codePanelRenderer.render(failure.getStackTrace(), htmlWriter);
            }
            htmlWriter.endElement();
        }
    }

    @Override
    protected void registerTabs() {
        addFailuresTab();
        addTab("Tests", new ErroringAction<SimpleHtmlWriter>() {
            @Override
            public void doExecute(SimpleHtmlWriter writer) throws IOException {
                renderTests(writer);
            }
        });
        addDeviceAndVariantTabs();
    }
}
