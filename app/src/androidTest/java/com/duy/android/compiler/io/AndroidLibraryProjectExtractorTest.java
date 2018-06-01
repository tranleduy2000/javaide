package com.duy.android.compiler.io;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.android.io.StreamException;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.file.AndroidLibraryProject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidLibraryProjectExtractorTest {

    @Test
    public void extract() throws IOException, StreamException, SAXException, ParserConfigurationException {
        Context context = InstrumentationRegistry.getTargetContext();

        String[] list = context.getAssets().list("");
        System.out.println("list = " + Arrays.toString(list));

        File file = new File(context.getCacheDir(), "appcompat-v7-25.2.0.aar");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(context.getAssets().open("appcompat-v7-25.2.0.aar"), output);
        output.close();

        String libraryName = "appcompat-v7-25.2.0";
        AndroidLibraryExtractor extractor = new AndroidLibraryExtractor(context);
        boolean result = extractor.extract(file, libraryName);

        assertTrue(result);

        File libDir = new File(Environment.getSdCardLibraryCachedDir(context), libraryName);


        AndroidLibraryProject library = new AndroidLibraryProject(libDir, libraryName);

    }
}
