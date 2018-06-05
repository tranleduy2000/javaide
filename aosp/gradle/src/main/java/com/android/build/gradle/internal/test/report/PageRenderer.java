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

import org.gradle.api.Action;
import org.gradle.reporting.ReportRenderer;

import java.io.IOException;
import java.util.Map;

/**
 * Custom PageRenderer based on Gradle's PageRenderer
 */
abstract class PageRenderer<T extends CompositeTestResults> extends TabbedPageRenderer<T> {
    private T results;
    private final TabsRenderer<T> tabsRenderer = new TabsRenderer<T>();
    protected final ReportType reportType;

    PageRenderer(ReportType reportType) {
        this.reportType = reportType;
    }

    protected T getResults() {
        return results;
    }

    protected abstract void renderBreadcrumbs(SimpleHtmlWriter htmlWriter) throws IOException;

    protected abstract void registerTabs();

    protected void addTab(String title, final Action<SimpleHtmlWriter> contentRenderer) {
        tabsRenderer.add(title, new ReportRenderer<T, SimpleHtmlWriter>() {
            @Override
            public void render(T model, SimpleHtmlWriter writer) {
                contentRenderer.execute(writer);
            }
        });
    }

    protected void renderTabs(SimpleHtmlWriter htmlWriter) throws IOException {
        tabsRenderer.render(getModel(), htmlWriter);
    }

    protected void addFailuresTab() {
        if (!results.getFailures().isEmpty()) {
            addTab("Failed tests", new ErroringAction<SimpleHtmlWriter>() {
                @Override
                public void doExecute(SimpleHtmlWriter writer) throws IOException {
                    renderFailures(writer);
                }
            });
        }
    }

    protected void addDeviceAndVariantTabs() {
        if (results.getResultsPerDevices().size() > 1) {
            addTab("Devices", new ErroringAction<SimpleHtmlWriter>() {
                @Override
                public void doExecute(SimpleHtmlWriter writer) throws IOException {
                    renderCompositeResults(writer, results.getResultsPerDevices(), "Devices");
                }
            });

        }

        if (results.getResultsPerVariants().size() > 1) {
            addTab("Variants", new ErroringAction<SimpleHtmlWriter>() {
                @Override
                public void doExecute(SimpleHtmlWriter writer) throws IOException {
                    renderCompositeResults(writer, results.getResultsPerVariants(), "Variants");
                }
            });
        }
    }

    protected void renderFailures(SimpleHtmlWriter htmlWriter) throws IOException {

        htmlWriter.startElement("ul").attribute("class", "linkList");

        boolean multiDevices = results.getResultsPerDevices().size() > 1;
        boolean multiVariants = results.getResultsPerVariants().size() > 1;

        htmlWriter.startElement("table");
        htmlWriter.startElement("thead");

        htmlWriter.startElement("tr");
        if (multiDevices) {
            htmlWriter.startElement("th").characters("Devices").endElement();
        }
        if (multiVariants) {
            if (reportType == ReportType.MULTI_PROJECT) {
                htmlWriter.startElement("th").characters("Project").endElement();
                htmlWriter.startElement("th").characters("Flavor").endElement();
            } else if (reportType == ReportType.MULTI_FLAVOR) {
                htmlWriter.startElement("th").characters("Flavor").endElement();
            }
        }
        htmlWriter.startElement("th").characters("Class").endElement();
        htmlWriter.startElement("th").characters("Test").endElement();

        htmlWriter.endElement(); //tr
        htmlWriter.endElement(); //thead

        for (TestResult test : results.getFailures()) {
            htmlWriter.startElement("tr");

            if (multiDevices) {
                htmlWriter.startElement("td").characters(test.getDevice()).endElement();
            }
            if (multiVariants) {
                if (reportType == ReportType.MULTI_PROJECT) {
                    htmlWriter.startElement("td").characters(test.getProject()).endElement();
                    htmlWriter.startElement("td").characters(test.getFlavor()).endElement();
                } else if (reportType == ReportType.MULTI_FLAVOR) {
                    htmlWriter.startElement("td").characters(test.getFlavor()).endElement();
                }
            }

            htmlWriter.startElement("td").attribute("class", test.getStatusClass())
                .startElement("a").attribute("href", String.format("%s.html", test.getClassResults().getFilename(reportType)))
                    .characters(test.getClassResults().getSimpleName()).endElement()
            .endElement();

            htmlWriter.startElement("td").attribute("class", test.getStatusClass())
                    .startElement("a").attribute("href", String.format("%s.html#s", test.getClassResults().getFilename(reportType), test.getName()))
                    .characters(test.getName()).endElement()
                    .endElement();
            htmlWriter.endElement(); //tr
        }
        htmlWriter.endElement(); //table
        htmlWriter.endElement(); // ul

    }

    protected void renderCompositeResults(SimpleHtmlWriter htmlWriter,
                                          Map<String, ? extends CompositeTestResults> map,
                                          String name) throws IOException {
        htmlWriter.startElement("table");
        htmlWriter.startElement("thead");
        htmlWriter.startElement("tr");
        htmlWriter.startElement("th").characters(name).endElement();
        htmlWriter.startElement("th").characters("Tests").endElement();
        htmlWriter.startElement("th").characters("Failures").endElement();
        htmlWriter.startElement("th").characters("Duration").endElement();
        htmlWriter.startElement("th").characters("Success rate").endElement();
        htmlWriter.endElement(); //tr
        htmlWriter.endElement(); //thead

        for (CompositeTestResults results : map.values()) {
            htmlWriter.startElement("tr");
            htmlWriter.startElement("td").attribute("class", results.getStatusClass()).characters(results.getName()).endElement();
            htmlWriter.startElement("td").characters(Integer.toString(results.getTestCount())).endElement();
            htmlWriter.startElement("td").characters(Integer.toString(results.getFailureCount())).endElement();
            htmlWriter.startElement("td").characters(results.getFormattedDuration()).endElement();
            htmlWriter.startElement("td").characters(results.getFormattedSuccessRate()).endElement();
            htmlWriter.endElement(); //tr
        }

        htmlWriter.endElement(); //table
    }

    @Override
    protected String getTitle() {
        return getModel().getTitle();
    }

    @Override
    protected String getPageTitle() {
        return String.format("Test results - %s", getModel().getTitle());
    }

    @Override
    protected ReportRenderer<T, SimpleHtmlWriter> getHeaderRenderer() {
        return new ReportRenderer<T, SimpleHtmlWriter>() {
            @Override
            public void render(T model, SimpleHtmlWriter htmlWriter) throws IOException {
                PageRenderer.this.results = model;
                renderBreadcrumbs(htmlWriter);

                // summary
                htmlWriter.startElement("div").attribute("id", "summary");
                htmlWriter.startElement("table");
                htmlWriter.startElement("tr");
                htmlWriter.startElement("td");
                htmlWriter.startElement("div").attribute("class", "summaryGroup");
                htmlWriter.startElement("table");
                htmlWriter.startElement("tr");
                htmlWriter.startElement("td");
                htmlWriter.startElement("div").attribute("class", "infoBox").attribute("id", "tests");
                htmlWriter.startElement("div").attribute("class", "counter").characters(Integer.toString(results.getTestCount())).endElement();
                htmlWriter.startElement("p").characters("tests").endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.startElement("td");
                htmlWriter.startElement("div").attribute("class", "infoBox").attribute("id", "failures");
                htmlWriter.startElement("div").attribute("class", "counter").characters(Integer.toString(results.getFailureCount())).endElement();
                htmlWriter.startElement("p").characters("failures").endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.startElement("td");
                htmlWriter.startElement("div").attribute("class", "infoBox").attribute("id", "duration");
                htmlWriter.startElement("div").attribute("class", "counter").characters(results.getFormattedDuration()).endElement();
                htmlWriter.startElement("p").characters("duration").endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.startElement("td");
                htmlWriter.startElement("div").attribute("class", String.format("infoBox %s", results.getStatusClass())).attribute("id", "successRate");
                htmlWriter.startElement("div").attribute("class", "percent").characters(results.getFormattedSuccessRate()).endElement();
                htmlWriter.startElement("p").characters("successful").endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
                htmlWriter.endElement();
            }
        };
    }

    @Override
    protected ReportRenderer<T, SimpleHtmlWriter> getContentRenderer() {
        return new ReportRenderer<T, SimpleHtmlWriter>() {
            @Override
            public void render(T model, SimpleHtmlWriter htmlWriter) throws IOException {
                PageRenderer.this.results = model;
                tabsRenderer.clear();
                registerTabs();
                renderTabs(htmlWriter);
            }
        };
    }
}
