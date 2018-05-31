package com.duy.compile.external.android;

import com.duy.project.file.android.AndroidProjectFolder;

interface IAndroidBuilder {
    void build(AndroidProjectFolder projectFolder, BuildType buildType);
}
