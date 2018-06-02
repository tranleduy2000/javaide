package com.android.tests.libstest.lib2;

import android.app.Activity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Lib2 {
    
    public static void handleTextView(Activity a) {
        TextView tv = (TextView) a.findViewById(R.id.lib2_text2);
        if (tv != null) {
            tv.setText(getContent());
        }
    }

    private static String getContent() {
        InputStream input = Lib2.class.getResourceAsStream("Lib2.txt");
        if (input == null) {
            return "FAILED TO FIND Lib2.txt";
        }

        BufferedReader reader = null;
        try {
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
