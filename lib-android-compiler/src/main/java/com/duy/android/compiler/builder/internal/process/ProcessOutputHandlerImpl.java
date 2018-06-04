package com.duy.android.compiler.builder.internal.process;

import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutput;
import com.android.ide.common.process.ProcessOutputHandler;

import java.io.OutputStream;
import java.io.PrintStream;

public class ProcessOutputHandlerImpl implements ProcessOutputHandler {
    private final PrintStream stdout;
    private final PrintStream stderr;

    public ProcessOutputHandlerImpl(PrintStream stdout, PrintStream stderr){
        this.stdout = stdout;
        this.stderr = stderr;
    }
    @Override
    public ProcessOutput createOutput() {
     return new BaseProcessOutput(stdout, stderr);
    }

    @Override
    public void handleOutput(ProcessOutput processOutput) throws ProcessException {

    }

    private class BaseProcessOutput implements ProcessOutput {
        private final PrintStream stdout;
        private final PrintStream stderr;

        public BaseProcessOutput(PrintStream stdout, PrintStream stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        @Override
        public OutputStream getStandardOutput() {
            return stdout;
        }

        @Override
        public OutputStream getErrorOutput() {
            return stderr;
        }
    }
}
