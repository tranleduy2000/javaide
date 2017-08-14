package com.pluscubed.logcat.data;

import java.util.List;

public class SavedLog {

    private final List<String> logLines;
    private final boolean truncated;

    public SavedLog(List<String> logLines, boolean truncated) {
        this.logLines = logLines;
        this.truncated = truncated;
    }

    public List<String> getLogLines() {
        return logLines;
    }

    public boolean isTruncated() {
        return truncated;
    }
}
