package com.duy.android.compiler.builder.parser;

import com.android.annotations.NonNull;
import com.android.builder.core.DefaultApiVersion;
import com.android.builder.core.DefaultProductFlavor;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastGradleParser {
    public static final Pattern COMPILE_SDK_VERSION = Pattern.compile("compileSdkVersion\\s+([0-9]+)");
    public static final Pattern BUILD_TOOLS_VERSION = Pattern.compile("buildToolsVersion\\s+\"(\\S+)\"");

    public static final Pattern APPLICATION_ID = Pattern.compile("applicationId\\s+\"(\\S+)\"");
    public static final Pattern MIN_SDK_VERSION = Pattern.compile("minSdkVersion\\s+(\\S+)");
    public static final Pattern TARGET_SDK_VERSION = Pattern.compile("targetSdkVersion\\s+(\\S+)");
    public static final Pattern VERSION_CODE = Pattern.compile("versionCode\\s+([0-9]+)");
    public static final Pattern VERSION_NAME = Pattern.compile("versionName\\s+\"(\\S+)\"");
    public static final Pattern APPLY_PLUGIN = Pattern.compile("apply\\s+plugin\\s+\'(\\S+)\'");

    public void parseDefaultProductFavor(@NonNull File gradleFile,
                                         @NonNull DefaultProductFlavor productFlavor) throws IOException {
        String content = IOUtils.toString(new FileInputStream(gradleFile));
        Matcher matcher = APPLICATION_ID.matcher(content);
        if (matcher.find()) {
            productFlavor.setApplicationId(matcher.group(1));
        }

        //required
        matcher = MIN_SDK_VERSION.matcher(content);
        DefaultApiVersion apiVersion = new DefaultApiVersion(Integer.parseInt(matcher.group(1)), "");
        productFlavor.setMinSdkVersion(apiVersion);

        //required
        matcher = TARGET_SDK_VERSION.matcher(content);
        matcher.find();
        apiVersion = new DefaultApiVersion(Integer.parseInt(matcher.group(1)), "");
        productFlavor.setTargetSdkVersion(apiVersion);

        matcher = VERSION_CODE.matcher(content);
        if (matcher.find()) {
            productFlavor.setVersionCode(Integer.valueOf(matcher.group(1)));
        }
        matcher = VERSION_NAME.matcher(content);
        if (matcher.find()) {
            productFlavor.setVersionName(matcher.group(1));
        }
    }

    public int getCompileSdkVersion(File gradleFile) throws IOException {
        String content = IOUtils.toString(new FileInputStream(gradleFile));
        Matcher matcher = COMPILE_SDK_VERSION.matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        System.err.println("Not found compileSdkVersion in " + gradleFile);
        return 0;
    }

    public String getBuildToolsVersion(File gradleFile) throws IOException {
        String content = IOUtils.toString(new FileInputStream(gradleFile));
        Matcher matcher = BUILD_TOOLS_VERSION.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        System.err.println("Not found buildToolsVersion in " + gradleFile);
        return null;
    }
}



