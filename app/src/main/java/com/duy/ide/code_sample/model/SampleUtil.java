package com.duy.ide.code_sample.model;

import android.content.Context;

import com.duy.ide.file.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SampleUtil {
    public static final String ASSET_SAMPLE_PATH = "sample";

    public static boolean extractTo(Context context, File out, String category, String name) {
        try {
            FileManager.extractAsset(context, ASSET_SAMPLE_PATH + "/" + category + "/" + name, out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
