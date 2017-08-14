package com.nolanlawson.logcat.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.util.UtilLogger;
/* 
 * Starting in JellyBean, the READ_LOGS permission must be requested as super user
 * or else you can only read your own app's logs.
 * 
 * This class contains helper methods to correct the problem.
 */
public class SuperUserHelper {
	
	private static UtilLogger log = new UtilLogger(SuperUserHelper.class);
	
	private static boolean failedToObtainRoot = false;
	
	private static final Pattern PID_PATTERN = Pattern.compile("\\d+");
	private static final Pattern SPACES_PATTERN = Pattern.compile("\\s+");
	
	private static void showToast(final Context context, final int resId) {
		Handler handler = new Handler(Looper.getMainLooper());
		
		handler.post(new Runnable(){

			@Override
			public void run() {
				Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
				
			}});
	}
	
	private static List<Integer> getAllRelatedPids(final int pid) {
	    List<Integer> result = new ArrayList<Integer>(Arrays.asList(pid));
	    // use 'ps' to get this pid and all pids that are related to it (e.g. spawned by it)
        try {
                
            final Process suProcess = Runtime.getRuntime().exec("su");
    
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    PrintStream outputStream = null;
                    try {
                        outputStream = new PrintStream(new BufferedOutputStream(suProcess.getOutputStream(), 8192));
                        outputStream.println("ps");
                        outputStream.println("exit");
                        outputStream.flush();
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                    
                }
            }).run();
            
            if (suProcess != null) {
                try {
                    suProcess.waitFor();
                } catch (InterruptedException e) {
                    log.e(e, "cannot get pids");
                }
            }
            
            
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()), 8192);
                while (bufferedReader.ready()) {
                    String[] line = SPACES_PATTERN.split(bufferedReader.readLine());
                    if (line.length >= 3 ) {
                        try {
                            if (pid == Integer.parseInt(line[2])) {
                                result.add(Integer.parseInt(line[1]));
                            }
                        } catch (NumberFormatException ignore) {}
                    }
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException e1) {
            log.e(e1, "cannot get process ids");
        }
        
        return result;
	}
	
	public static void destroy(Process process) {
	    // stupid method for getting the pid, but it actually works
	    Matcher matcher = PID_PATTERN.matcher(process.toString());
	    matcher.find();
	    int pid = Integer.parseInt(matcher.group());
	    List<Integer> allRelatedPids = getAllRelatedPids(pid);
	    log.d("Killing %s", allRelatedPids);
	    for (Integer relatedPid : allRelatedPids) {
	        destroyPid(relatedPid);
	    }
        
	}
	
	private static void destroyPid(int pid) {

        Process suProcess = null;
        PrintStream outputStream = null;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            outputStream = new PrintStream(new BufferedOutputStream(suProcess.getOutputStream(), 8192));
            outputStream.println("kill " + pid);
            outputStream.println("exit");
            outputStream.flush();
        } catch (IOException e) {
            log.e(e, "cannot kill process " + pid);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (suProcess != null) {
                try {
                    suProcess.waitFor();
                } catch (InterruptedException e) {
                    log.e(e, "cannot kill process " + pid);
                }
            }
        }
	}
	
	public static void requestRoot(Context context) {

		showToast(context, R.string.toast_request_root);
		
		Process process = null;
		try {
			// Preform su to get root privledges
			process = Runtime.getRuntime().exec("su");

			// confirm that we have root
			DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
			outputStream.writeBytes("echo hello\n");

			// Close the terminal
			outputStream.writeBytes("exit\n");
			outputStream.flush();

			process.waitFor();
			if (process.exitValue() != 0) {
				showToast(context, R.string.toast_no_root);
				failedToObtainRoot = true;
			} else {
				// success
                PreferenceHelper.setJellybeanRootRan(context);
			}

		} catch (IOException e) {
			log.w(e, "Cannot obtain root");
			showToast(context, R.string.toast_no_root);
			failedToObtainRoot = true;
		} catch (InterruptedException e) {
			log.w(e, "Cannot obtain root");
			showToast(context, R.string.toast_no_root);
			failedToObtainRoot = true;
		}

	}
	
	public static boolean isFailedToObtainRoot() {
		return failedToObtainRoot;
	}
}
