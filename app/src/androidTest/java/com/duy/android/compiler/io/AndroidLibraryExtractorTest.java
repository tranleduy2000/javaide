package com.duy.android.compiler.io;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidLibraryExtractorTest {

    @Test
    public void extract() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();
        File file = new File(context.getCacheDir(), "appcompat-v7-25.2.0.aar");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(context.getAssets().open("appcompat-v7-25.2.0.aar"), output);
        output.close();

        AndroidLibraryExtractor extractor = new AndroidLibraryExtractor(context);
        boolean result = extractor.extract(file);

        assertTrue(result);
    }
}
