/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.java.file;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.duy.ide.java.setting.AppSetting;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * File Manager
 * Created by Duy on 10-Feb-17.
 */
public class FileManager {
    public static final String EXTERNAL_DIR_SRC;
    public static final String EXTERNAL_DIR;

    static {
        EXTERNAL_DIR_SRC = Environment.getExternalStorageDirectory() + "/JavaNIDE/src/";
        EXTERNAL_DIR = new File(Environment.getExternalStorageDirectory(), "JavaNIDE").getAbsolutePath();
    }

    private Context context;
    private Database mDatabase;
    private AppSetting mPascalPreferences;

    public FileManager(Context context) {
        this.context = context;
        mDatabase = new Database(context);
        mPascalPreferences = new AppSetting(context);
    }

    public static StringBuilder streamToString(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                result.append(mLine).append("\n");
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return result;
    }

    /**
     * saveFile file
     *
     * @param filePath - name of file
     * @param text     - content of file
     */
    public static boolean saveFile(@NonNull String filePath, String text) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            if (text.length() > 0) writer.write(text);
            writer.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean deleteFolder(File zFile) {
        if (!zFile.exists()) return false;
        if (zFile.isDirectory()) {
            //Its a directory
            File[] files = zFile.listFiles();
            for (File ff : files) {
                deleteFolder(ff);
            }
        }
        //Now delete
        return zFile.delete();
    }

    public static ArrayList<String> listClassName(File src) {
        if (!src.exists()) return new ArrayList<>();

        String[] exts = new String[]{"java"};
        Collection<File> files = FileUtils.listFiles(src, exts, true);

        ArrayList<String> classes = new ArrayList<>();
        String srcPath = src.getPath();
        for (File file : files) {
            String javaPath = file.getPath();
            javaPath = javaPath.substring(srcPath.length() + 1, javaPath.length() - 5); //.java
            javaPath = javaPath.replace(File.separator, ".");
            classes.add(javaPath);
        }
        return classes;
    }


    public Set<File> getEditorFiles() {
        return mDatabase.getListFile();
    }

    /**
     * get all file with filter
     *
     * @param path      - folder path
     * @param extension - extension of file
     * @return - list file
     */
    public ArrayList<File> getListFile(String path, String extension) {
        ArrayList<File> list = new ArrayList<>();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            int ind = file.getPath().lastIndexOf('.');
            if (ind > 0) {
                String tmp = file.getPath().substring(ind + 1);// this is the extension
                if (tmp.equals(extension)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public String loadInMode(File file) {
        String res = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line;
            while ((line = in.readLine()) != null) {
                res += line;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean deleteFile(File file) {
        try {
            if (file.isDirectory()) {
                return deleteFolder(file);
            }
            return file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    public void addNewPath(String path) {
        mDatabase.addNewFile(new File(path));
    }

    public void removeTabFile(String path) {
        mDatabase.removeFile(path);
    }

    /**
     * copy data from in to inType
     */
    public void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();

        out.flush();
        out.close();

    }

    /**
     * copy data from file in to file inType
     */
    public void copy(String pathIn, String pathOut) throws IOException {
        InputStream in = new FileInputStream(new File(pathIn));
        OutputStream out = new FileOutputStream(new File(pathOut));
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();

        out.flush();
        out.close();

    }

    public void destroy() {
        mDatabase.close();
    }

    public static class SAVE_MODE {
        static final int INTERNAL = 1;
        static final int EXTERNAL = 2;
    }
}
