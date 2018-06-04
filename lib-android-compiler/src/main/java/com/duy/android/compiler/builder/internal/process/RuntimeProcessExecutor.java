package com.duy.android.compiler.builder.internal.process;

import com.android.ide.common.process.ProcessExecutor;
import com.android.ide.common.process.ProcessInfo;
import com.android.ide.common.process.ProcessOutput;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.process.ProcessResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class RuntimeProcessExecutor implements ProcessExecutor {
    private OutputStream errorOutput;
    private OutputStream standardOutput;

    @Override
    public ProcessResult execute(ProcessInfo processInfo, ProcessOutputHandler processOutputHandler) {
        ProcessOutput output = processOutputHandler.createOutput();
        errorOutput = output.getErrorOutput();
        standardOutput = output.getStandardOutput();

        ArrayList<String> args = new ArrayList<>(processInfo.getArgs());
        String executable = processInfo.getExecutable();
        args.add(0, executable);

        String[] array = new String[args.size()];
        args.toArray(array);

        System.out.println("array = " + Arrays.toString(array));

        try {
            int exitValue = exec(array);
            return new ProcessResultImpl(args.toString(), exitValue);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ProcessResultImpl(args.toString(), -1);
    }

    private int exec(String[] args) throws InterruptedException, IOException {
        final int[] exitCode = new int[1];
        final Process aaptProcess = Runtime.getRuntime().exec(args);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exitCode[0] = aaptProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        BufferedReader reader = new BufferedReader(new InputStreamReader(aaptProcess.getInputStream()));
        do {
            try {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                standardOutput.write(s.getBytes());
            } catch (Exception e) {
                break;
            }
        } while (thread.isAlive());
        reader.close();
        reader = new BufferedReader(new InputStreamReader(aaptProcess.getErrorStream()));
        do {
            try {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                errorOutput.write(s.getBytes());
                // TODO: 03-Jun-18 improve it , use com.android.ide.common.blame.parser.aapt.AaptOutputParser
                if (s.startsWith("ERROR")) {
                    return 1;
                }
            } catch (Exception e) {
                break;
            }
        } while (thread.isAlive());
        thread.join();

        return exitCode[0];
    }

}
