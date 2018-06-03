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

import com.google.common.io.Closeables;

import org.gradle.api.GradleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Custom test reporter based on Gradle's DefaultTestReport
 */
public class TestReport {
    private final HtmlReportRenderer htmlRenderer = new HtmlReportRenderer();
    private final ReportType reportType;
    private final File resultDir;
    private final File reportDir;

    public TestReport(ReportType reportType, File resultDir, File reportDir) {
        this.reportType = reportType;
        this.resultDir = resultDir;
        this.reportDir = reportDir;
        htmlRenderer.requireResource(getClass().getResource("report.js"));
        htmlRenderer.requireResource(getClass().getResource("base-style.css"));
        htmlRenderer.requireResource(getClass().getResource("style.css"));
    }

    public void generateReport() {
        AllTestResults model = loadModel();
        generateFiles(model);
    }

    private AllTestResults loadModel() {
        AllTestResults model = new AllTestResults();
        if (resultDir.exists()) {
            File[] files = resultDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("TEST-") && file.getName().endsWith(".xml")) {
                        mergeFromFile(file, model);
                    }
                }
            }
        }
        return model;
    }

    private void mergeFromFile(File file, AllTestResults model) {
        InputStream inputStream = null;
        try {
            //noinspection IOResourceOpenedButNotSafelyClosed
            inputStream = new FileInputStream(file);
            Document document;
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                        new InputSource(inputStream));
            } finally {
                inputStream.close();
            }

            String deviceName = null;
            String projectName = null;
            String flavorName = null;
            NodeList propertiesList = document.getElementsByTagName("properties");
            for (int i = 0; i < propertiesList.getLength(); i++) {
                Element properties = (Element) propertiesList.item(i);
                XPath xPath = XPathFactory.newInstance().newXPath();
                deviceName = xPath.evaluate("property[@name='device']/@value",properties);
                projectName = xPath.evaluate("property[@name='project']/@value",properties);
                flavorName = xPath.evaluate("property[@name='flavor']/@value",properties);
            }

            NodeList testCases = document.getElementsByTagName("testcase");
            for (int i = 0; i < testCases.getLength(); i++) {
                Element testCase = (Element) testCases.item(i);
                String className = testCase.getAttribute("classname");
                String testName = testCase.getAttribute("name");
                BigDecimal duration = parse(testCase.getAttribute("time"));
                duration = duration.multiply(BigDecimal.valueOf(1000));
                NodeList failures = testCase.getElementsByTagName("failure");
                TestResult testResult = model.addTest(className, testName, duration.longValue(),
                        deviceName, projectName, flavorName);
                for (int j = 0; j < failures.getLength(); j++) {
                    Element failure = (Element) failures.item(j);
                    testResult.addFailure(
                            failure.getAttribute("message"), failure.getTextContent(),
                            deviceName, projectName, flavorName);
                }
            }
            NodeList ignoredTestCases = document.getElementsByTagName("ignored-testcase");
            for (int i = 0; i < ignoredTestCases.getLength(); i++) {
                Element testCase = (Element) ignoredTestCases.item(i);
                String className = testCase.getAttribute("classname");
                String testName = testCase.getAttribute("name");
                model.addTest(className, testName, 0, deviceName, projectName, flavorName).ignored();
            }
            String suiteClassName = document.getDocumentElement().getAttribute("name");
            ClassTestResults suiteResults = model.addTestClass(suiteClassName);
            NodeList stdOutElements = document.getElementsByTagName("system-out");
            for (int i = 0; i < stdOutElements.getLength(); i++) {
                suiteResults.addStandardOutput(stdOutElements.item(i).getTextContent());
            }
            NodeList stdErrElements = document.getElementsByTagName("system-err");
            for (int i = 0; i < stdErrElements.getLength(); i++) {
                suiteResults.addStandardError(stdErrElements.item(i).getTextContent());
            }
        } catch (Exception e) {
            throw new GradleException(String.format("Could not load test results from '%s'.", file), e);
        } finally {
            try {
                Closeables.close(inputStream, true /* swallowIOException */);
            } catch (IOException e) {
                // cannot happen
            }
        }
    }

    private void generateFiles(AllTestResults model) {
        try {
            generatePage(model, new OverviewPageRenderer(reportType), new File(reportDir, "index.html"));
            for (PackageTestResults packageResults : model.getPackages()) {
                generatePage(packageResults, new PackagePageRenderer(reportType),
                        new File(reportDir, packageResults.getFilename(reportType) + ".html"));
                for (ClassTestResults classResults : packageResults.getClasses()) {
                    generatePage(classResults, new ClassPageRenderer(reportType),
                            new File(reportDir, classResults.getFilename(reportType) + ".html"));
                }
            }
        } catch (Exception e) {
            throw new GradleException(
                    String.format("Could not generate test report to '%s'.", reportDir), e);
        }
    }

    private <T extends CompositeTestResults> void generatePage(T model, PageRenderer<T> renderer,
                                                               File outputFile) throws Exception {
        htmlRenderer.renderer(renderer).writeTo(model, outputFile);
    }

    /**
     * Regardless of the default locale, comma ('.') is used as decimal separator
     *
     * @param source
     * @return
     * @throws java.text.ParseException
     */
    public BigDecimal parse(String source) throws ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#.#", symbols);
        format.setParseBigDecimal(true);
        return (BigDecimal) format.parse(source);
    }
}
