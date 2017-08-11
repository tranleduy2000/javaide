/*
 * Copyright (C) 2010 Ken Ellinwood.
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
package kellinwood.logging.android;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import kellinwood.logging.AbstractLogger;

// enable output via 'adb shell setprop log.tag.<YOUR TAG> <LEVEL>'
public class AndroidLogger extends AbstractLogger 
{

	// Constructs the logger so that only the last component of the class name is used for the logging tag.
	// E.g., A parameter value of kellinwood.zipsigner.ZipPickerActivity results in a tag value of "ZipPickerActivity".
	public AndroidLogger(String c) {
		super(c);
		int pos = category.lastIndexOf('.');
		if (pos > 0) category = category.substring(pos+1);
		
	}

	Context toastContext;
	
	boolean isErrorToastEnabled = true;
	boolean isWarningToastEnabled = true;
	boolean isInfoToastEnabled = false;
	boolean isDebugToastEnabled = false;

	public Context getToastContext() {
		return toastContext;
	}

	public void setToastContext(Context toastContext) {
		this.toastContext = toastContext;
	}
	
	public boolean isErrorToastEnabled() {
		return isErrorToastEnabled;
	}

	public void setErrorToastEnabled(boolean isErrorToastEnabled) {
		this.isErrorToastEnabled = isErrorToastEnabled;
	}

	public boolean isWarningToastEnabled() {
		return isWarningToastEnabled;
	}

	public void setWarningToastEnabled(boolean isWarningToastEnabled) {
		this.isWarningToastEnabled = isWarningToastEnabled;
	}

	public boolean isInfoToastEnabled() {
		return isInfoToastEnabled;
	}

	public void setInfoToastEnabled(boolean isInfoToastEnabled) {
		this.isInfoToastEnabled = isInfoToastEnabled;
	}

	public boolean isDebugToastEnabled() {
		return isDebugToastEnabled;
	}

	public void setDebugToastEnabled(boolean isDebugToastEnabled) {
		this.isDebugToastEnabled = isDebugToastEnabled;
	}

    // error, log only (no toast)
	public void errorLO(String message, Throwable t) {
        boolean toastState = isErrorToastEnabled;
        isErrorToastEnabled = false;
		writeFixNullMessage( ERROR, message, t);
        isErrorToastEnabled = toastState;
	}

    // warning, log only (no toast)
	public void warningLO(String message, Throwable t) {
        boolean toastState = isWarningToastEnabled;
        isWarningToastEnabled = false;
        writeFixNullMessage( WARNING, message, t);
        isWarningToastEnabled = toastState;
	}
    
    // info, log only (no toast)
	public void infoLO(String message, Throwable t) {
        boolean toastState = isInfoToastEnabled;
        isInfoToastEnabled = false;
        writeFixNullMessage( INFO, message, t);
        isInfoToastEnabled = toastState;
	}
    
    // debug, log only (no toast)
	public void debugLO(String message, Throwable t) {
        boolean toastState = isDebugToastEnabled;
        isDebugToastEnabled = false;
        writeFixNullMessage( DEBUG, message, t);
        isDebugToastEnabled = toastState;
	}

    protected void toast( String message) 
    {
        try {
            if (toastContext != null) Toast.makeText(toastContext, message, Toast.LENGTH_LONG).show();
        }
        catch (Throwable t) {
            Log.e(category, message, t);
        }
    }
    
	@Override
	public void write(String level, String message, Throwable t) {
		if (ERROR.equals(level)) {
			if (t != null) Log.e(category, message, t);
			else Log.e( category, message);
			if (isErrorToastEnabled) toast( message);
		}
		else if (DEBUG.equals(level)) { 
			if (t != null) Log.d(category, message, t);
			else Log.d( category, message);
			if (isDebugToastEnabled) toast(message);
		}		
		else if (WARNING.equals(level)) { 
			if (t != null) Log.w(category, message, t);
			else Log.w( category, message);
			if (isWarningToastEnabled) toast(message);
		}
		else if (INFO.equals(level)) { 
			if (t != null) Log.i(category, message, t);
			else Log.i( category, message);
			if (isInfoToastEnabled) toast( message);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		boolean enabled = Log.isLoggable(category, Log.DEBUG);
		return enabled;
	}

	@Override
	public boolean isErrorEnabled() {
		return Log.isLoggable(category, Log.ERROR);
	}

	@Override
	public boolean isInfoEnabled() {
		return Log.isLoggable(category, Log.INFO);
	}

	@Override
	public boolean isWarningEnabled() {
		return Log.isLoggable(category, Log.WARN);
	}

	
}
