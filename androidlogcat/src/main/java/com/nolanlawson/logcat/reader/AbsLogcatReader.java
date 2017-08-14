package com.nolanlawson.logcat.reader;


public abstract class AbsLogcatReader implements LogcatReader {

	protected boolean recordingMode;
	
	public AbsLogcatReader(boolean recordingMode) {
		this.recordingMode = recordingMode;
	}

	public boolean isRecordingMode() {
		return recordingMode;
	}
}
