package com.android.tests.libstest.lib2;

import com.android.tests.libstest.lib2b.R;

import android.app.Activity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Lib2b {
    
    public static void handleTextView(Activity a) {
        TextView tv = (TextView) a.findViewById(R.id.lib2b_text2);
        if (tv != null) {
            tv.setText(getContent());
        }
    }

    private static String getContent() {
        InputStream input = Lib2b.class.getResourceAsStream("Lib2b.txt");
        if (input == null) {
            return "FAILED TO FIND Lib2b.txt";
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
