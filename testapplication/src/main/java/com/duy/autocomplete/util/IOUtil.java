package com.duy.autocomplete.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Duy on 20-Jul-17.
 */

public class IOUtil {


    @Nullable
    public ArrayList<String> readDir(String path) {
        try {
            File file = new File(path);
            String[] list = file.list();
            return (ArrayList<String>) Arrays.asList(list);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public String readFile(String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void exec() {
    }
}