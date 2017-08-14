package com.nolanlawson.logcat.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.data.SavedLog;
import com.nolanlawson.logcat.util.UtilLogger;

public class SaveLogHelper {

	private static final int BUFFER = 0x1000; // 4K
	
	public static final String TEMP_DEVICE_INFO_FILENAME = "device_info.txt";
	public static final String TEMP_LOG_FILENAME = "logcat.txt";
	public static final String TEMP_ZIP_FILENAME = "logcat_and_device_info.zip";
	
	private static final String LEGACY_SAVED_LOGS_DIR = "catlog_saved_logs";
	private static final String CATLOG_DIR = "catlog";
	private static final String SAVED_LOGS_DIR = "saved_logs";
	private static final String TMP_DIR = "tmp";
	
	private static UtilLogger log = new UtilLogger(SaveLogHelper.class);
	
	public static File saveTemporaryFile(Context context, String filename, CharSequence text, List<CharSequence> lines) {
		PrintStream out = null;
		try {
			
			File tempFile = new File(getTempDirectory(), filename);
			
			// specifying BUFFER gets rid of an annoying warning message
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(tempFile, false), BUFFER));
			if (text != null) { // one big string
				out.print(text);
			} else { // multiple lines separated by newline
				for (CharSequence line : lines) {
					out.println(line);
				}
			}
			
			log.d("Saved temp file: %s", tempFile);
			
			return tempFile;
			
		} catch (FileNotFoundException ex) {
			log.e(ex,"unexpected exception");
			return null;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static boolean checkSdCard(Context context) {
		
		boolean result = SaveLogHelper.checkIfSdCardExists();
		
		if (!result) {
			Toast.makeText(context, R.string.sd_card_not_found, Toast.LENGTH_LONG).show();
		}
		return result;
	}
	public static boolean checkIfSdCardExists() {
		
		File sdcardDir = Environment.getExternalStorageDirectory();
			
		return sdcardDir != null && sdcardDir.listFiles() != null;
		
	}
	
	public static File getFile(String filename) {
		
		File catlogDir = getSavedLogsDirectory();
		
		File file = new File(catlogDir, filename);
	
		return file;
	}
	
	public static void deleteLogIfExists(String filename) {
		
		File catlogDir = getSavedLogsDirectory();
		
		File file = new File(catlogDir, filename);
		
		if (file.exists()) {
			file.delete();
		}
		
	}
	
	public static Date getLastModifiedDate(String filename) {
		
		File catlogDir = getSavedLogsDirectory();
		
		File file = new File(catlogDir, filename);
		
		if (file.exists()) {
			return new Date(file.lastModified());
		} else {
			// shouldn't happen
			log.e("file last modified date not found: %s", filename);
			return new Date();
		}
	}
	
	/**
	 * Get all the log filenames, order by last modified descending
	 * @return
	 */
	public static List<String> getLogFilenames() {
		
		File catlogDir = getSavedLogsDirectory();
		
		File[] filesArray = catlogDir.listFiles();
		
		if (filesArray == null) {
			return Collections.emptyList();
		}
		
		List<File> files = new ArrayList<File>(Arrays.asList(filesArray));
		
		Collections.sort(files, new Comparator<File>(){

			@Override
			public int compare(File object1, File object2) {
				return new Long(object2.lastModified()).compareTo(object1.lastModified());
			}});
		
		List<String> result = new ArrayList<String>();
		
		for (File file : files) {
			result.add(file.getName());
		}
		
		return result;
		
	}
	
	public static SavedLog openLog(String filename, int maxLines) {
		
		File catlogDir = getSavedLogsDirectory();
		File logFile = new File(catlogDir, filename);	
		
		LinkedList<String> logLines = new LinkedList<String>();
		boolean truncated = false;
		
		BufferedReader bufferedReader = null;
		
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)), BUFFER);
			
			while (bufferedReader.ready()) {
				logLines.add(bufferedReader.readLine());
				if (logLines.size() > maxLines) {
					logLines.removeFirst();
					truncated = true;
				}
			}
		} catch (IOException ex) {
			log.e(ex, "couldn't read file");
			
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.e(e, "couldn't close buffered reader");
				}
			}
		}
		
		SavedLog result = new SavedLog();
		result.setLogLines(logLines);
		result.setTruncated(truncated);
		
		return result;
	}
	
	public static synchronized boolean saveLog(CharSequence logString, String filename) {
		return saveLog(null, logString, filename);
	}
	
	public static synchronized boolean saveLog(List<CharSequence> logLines, String filename) {
		return saveLog(logLines, null, filename);
	}
	
	private static boolean saveLog(List<CharSequence> logLines, CharSequence logString, String filename) {
		
		File catlogDir = getSavedLogsDirectory();
		
		File newFile = new File(catlogDir, filename);
		try {
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
		} catch (IOException ex) {
			log.e(ex, "couldn't create new file");
			return false;
		}
		PrintStream out = null;
		try {
			// specifying BUFFER gets rid of an annoying warning message
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(newFile, true), BUFFER));
			
			// save a log as either a list of strings or as a charsequence
			if (logLines != null) {
				for (CharSequence line : logLines) {
					out.println(line);
				}				
			} else if (logString != null) {
				out.print(logString);
			}
			
			
		} catch (FileNotFoundException ex) {
			log.e(ex,"unexpected exception");
			return false;
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		return true;
		
		
	}
	
	public static File getTempDirectory() {
		File catlogDir = getCatlogDirectory();
		
		File tmpDir = new File(catlogDir, TMP_DIR);
		
		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		
		return tmpDir;
	}
	
	private static File getSavedLogsDirectory() {
		File catlogDir = getCatlogDirectory();
		
		File savedLogsDir = new File(catlogDir, SAVED_LOGS_DIR);
		
		if (!savedLogsDir.exists()) {
			savedLogsDir.mkdir();
		}
		
		return savedLogsDir;
		
	}

	private static File getCatlogDirectory() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		
		File catlogDir = new File(sdcardDir, CATLOG_DIR);
		
		if (!catlogDir.exists()) {
			catlogDir.mkdir();
		}
		return catlogDir;
	}

	/**
	 * I used to save logs to /sdcard/catlog_saved_logs.  Now it's /sdcard/catlog/saved_logs.  Move any files that
	 * need to be moved to the new directory.
	 * 
	 * @param sdcardDir
	 * @param savedLogsDir
	 */
	public static synchronized void moveLogsFromLegacyDirIfNecessary() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		File legacyDir = new File(sdcardDir, LEGACY_SAVED_LOGS_DIR);
		
		
		if (legacyDir.exists() && legacyDir.isDirectory()) {
			File savedLogsDir = getSavedLogsDirectory();
			for (File file : legacyDir.listFiles()) {
				file.renameTo(new File(savedLogsDir, file.getName()));
			}
			legacyDir.delete();
		}
	}
	
	public static boolean legacySavedLogsDirExists() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		File legacyDir = new File(sdcardDir, LEGACY_SAVED_LOGS_DIR);
		
		return legacyDir.exists() && legacyDir.isDirectory();
	}
	
	public static File saveTemporaryZipFile(String filename, List<File> files) {
		try {
			return saveTemporaryZipFileAndThrow(filename, files);
		} catch (IOException e) {
			log.e(e, "unexpected error");
		}
		return null;
	}
	
	private static File saveTemporaryZipFileAndThrow(String filename, List<File> files) throws IOException {
		File zipFile = new File(getTempDirectory(), filename);

		ZipOutputStream output = null;
		try {
			output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFFER));

			for (File file : files) {
				FileInputStream fi = new FileInputStream(file);
				BufferedInputStream input = null;
				try {
					input = new BufferedInputStream(fi, BUFFER);
					
					ZipEntry entry = new ZipEntry(file.getName());
					output.putNextEntry(entry);
					copy(input, output);
				} finally {
					if (input != null) {
						input.close();
					}
				}
	
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}
		return zipFile;
	}
	
	/**
	 * Copies all bytes from the input stream to the output stream. Does not
	 * close or flush either stream.
	 * 
	 * Taken from Google Guava ByteStreams.java
	 * 
	 * @param from
	 *            the input stream to read from
	 * @param to
	 *            the output stream to write to
	 * @return the number of bytes copied
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private static long copy(InputStream from, OutputStream to)	throws IOException {
		byte[] buf = new byte[BUFFER];
		long total = 0;
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}
}
