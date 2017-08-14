package com.nolanlawson.logcat.data;

import java.util.List;

public class SavedLog {
	private List<String> logLines;
	private boolean truncated;
	
	public void setLogLines(List<String> logLines) {
		this.logLines = logLines;
	}
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
	public List<String> getLogLines() {
		return logLines;
	}
	public boolean isTruncated() {
		return truncated;
	}
}
