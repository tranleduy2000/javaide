package com.android.tests.assets.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Lib {
    
    public static void handleTextView(Activity a) {
        TextView tv = (TextView) a.findViewById(R.id.lib_text2);
        if (tv != null) {
            tv.setText(getContent(a));
        }
    }

    private static String getContent(Context context) {
        AssetManager assets = context.getAssets();

        BufferedReader reader = null;
        try {
            InputStream input = assets.open("Lib.txt");
            if (input == null) {
                return "FAILED TO FIND Lib.txt";
            }
            reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            return reader.readLine();
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        
        return "FAILED TO READ CONTENT";
    }
}
