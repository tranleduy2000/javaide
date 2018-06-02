package com.android.tests.libstest.lib1;

import android.app.Activity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Lib1 {
    
    public static void handleTextView(Activity a) {
        TextView tv = (TextView) a.findViewById(R.id.lib1_text2);
        if (tv != null) {
            tv.setText(Lib1.getContent());
        }
    }

    public static String getContent() {
        InputStream input = Lib1.class.getResourceAsStream("Lib1.txt");
        if (input == null) {
            return "FAILED TO FIND Lib1.txt";
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
