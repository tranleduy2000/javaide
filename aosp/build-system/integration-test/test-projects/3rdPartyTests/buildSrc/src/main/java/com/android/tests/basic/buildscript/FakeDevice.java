package com.android.tests.basic.buildscript;

import com.android.annotations.NonNull;
import com.android.builder.testing.api.DeviceConfig;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.utils.ILogger;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class FakeDevice extends DeviceConnector {

    private final String name;
    private boolean connectCalled = false;
    private boolean disconnectCalled = false;
    private boolean installCalled = false;
    private boolean uninstallCalled = false;
    private boolean execShellCalled = false;
    private boolean pullFileCalled = false;

    private final List<File> installedApks = Lists.newArrayList();

    FakeDevice(String name) {
        this.name = name;
    }

    @Override
    public void connect(int timeOut, ILogger logger) throws TimeoutException {
        logger.info("CONNECT(%S) CALLED", name);
        connectCalled = true;
    }

    @Override
    public void disconnect(int timeOut, ILogger logger) throws TimeoutException {
        logger.info("DISCONNECTED(%S) CALLED", name);
        disconnectCalled = true;
    }

    @Override
    public void installPackage(@NonNull File apkFile, Collection<String> installOptions,
            int timeout, ILogger logger) throws DeviceException {
        logger.info("INSTALL(%S) CALLED", name);

        if (apkFile == null) {
            throw new NullPointerException("Null testApk");
        }

        System.out.println(String.format("\t(%s)ApkFile: %s", name, apkFile.getAbsolutePath()));

        if (!apkFile.isFile()) {
            throw new RuntimeException("Missing file: " + apkFile.getAbsolutePath());
        }

        if (!apkFile.getAbsolutePath().endsWith(".apk")) {
            throw new RuntimeException("Wrong extension: " + apkFile.getAbsolutePath());
        }

        if (installedApks.contains(apkFile)) {
            throw new RuntimeException("Already added: " + apkFile.getAbsolutePath());
        }

        installedApks.add(apkFile);

        installCalled = true;
    }

    public void installPackages(@NonNull List<File> apkFiles, Collection<String> installOptions,
            int timeout, ILogger logger)
            throws DeviceException {

        if (apkFiles == null || apkFiles.isEmpty()) {
            throw new NullPointerException("Null testApks");
        }
        for (File apkFile : apkFiles) {
            System.out.println(String.format("\t(%s)ApkFile: %s", name, apkFile.getAbsolutePath()));

            if (!apkFile.isFile()) {
                throw new RuntimeException("Missing file: " + apkFile.getAbsolutePath());
            }

            if (!apkFile.getAbsolutePath().endsWith(".apk")) {
                throw new RuntimeException("Wrong extension: " + apkFile.getAbsolutePath());
            }

            if (installedApks.contains(apkFile)) {
                throw new RuntimeException("Already added: " + apkFile.getAbsolutePath());
            }

            installedApks.add(apkFile);
        }

        installCalled = true;

    }

    @Override
    public void uninstallPackage(@NonNull String packageName, int timeout, ILogger logger) throws DeviceException {
        logger.info("UNINSTALL(%S) CALLED", name);
        uninstallCalled = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
                                    long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
            IOException {
        System.out.println(String.format("EXECSHELL(%S) CALLED", name));

        // now fake out some tests result to make the test runner happy.
        addLineToReceiver("INSTRUMENTATION_STATUS: numtests=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: numtests=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: stream=\r\n", receiver);
        addLineToReceiver("com.android.tests.basic.MainTest:\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: id=InstrumentationTestRunner\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: test=testBuildConfig\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: class=com.android.tests.basic.MainTest\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: current=1\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS_CODE: 1\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: numtests=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: stream=.\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: id=InstrumentationTestRunner\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: test=testBuildConfig\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: class=com.android.tests.basic.MainTest\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: current=1\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS_CODE: 0\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: numtests=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: stream=\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: id=InstrumentationTestRunner\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: test=testPreconditions\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: class=com.android.tests.basic.MainTest\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: current=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS_CODE: 1\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: numtests=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: stream=.\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: id=InstrumentationTestRunner\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: test=testPreconditions\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: class=com.android.tests.basic.MainTest\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS: current=2\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_STATUS_CODE: 0\r\n", receiver);
        addLineToReceiver("INSTRUMENTATION_RESULT: stream=\r\n", receiver);
        addLineToReceiver("Test results for InstrumentationTestRunner=..\r\n", receiver);
        addLineToReceiver("Time: 0.247\r\n", receiver);
        addLineToReceiver("\r\n", receiver);
        addLineToReceiver("OK (2 tests)\r\n", receiver);
        receiver.flush();

        execShellCalled = true;
    }

    private void addLineToReceiver(String line, IShellOutputReceiver receiver) {
        byte[] bytes = line.getBytes();
        receiver.addOutput(bytes, 0, bytes.length);
    }

    @Override
    public void pullFile(String remote, String local) throws IOException {
        System.out.println(String.format("PULL_FILE(%S) CALLED", name));
        pullFileCalled = true;
    }

    public String isValid() {
        if (!connectCalled) {
            return "connect not called on " + name;
        }

        if (!disconnectCalled) {
            return "disconnect not called on " + name;
        }

        if (!installCalled) {
            return "installPackage not called on " + name;
        }

        if (!uninstallCalled) {
            return "uninstallPackage not called on " + name;
        }

        if (!execShellCalled) {
            return "executeShellCommand not called on " + name;
        }

        if (!pullFileCalled) {
            return "pullFile not called on " + name;
        }

        return null;
    }

    public String getSerialNumber() {
        return "1234";
    }


    public IDevice.DeviceState getState() {
        return IDevice.DeviceState.ONLINE;
    }

    public int getApiLevel() {
        return 99;
    }

    public String getApiCodeName() {
        return null;
    }

    @NonNull
    public List<String> getAbis() {
        return Collections.singletonList("fake");
    }

    public int getDensity() {
        return 160;
    }

    public int getHeight() {
        return 800;
    }

    public int getWidth() {
        return 480;
    }

    public String getLanguage() {
        return "en";
    }

    public String getRegion() {
        return null;
    }

    public String getProperty(String propertyName) {
        if ("ro.sf.lcd_density".equals(propertyName)) return "160";
        return null;
    }

    public Future<String> getSystemProperty(@NonNull String name) {
        return SettableFuture.create();
    }

    public DeviceConfig getDeviceConfig() {
        return DeviceConfig.Builder.parse(Collections.<String>emptyList());
    }
}
