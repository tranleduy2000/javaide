package com.duy.compile.external.android;

import com.duy.project.file.android.AndroidProject;

interface IAndroidBuilder {
    void build(AndroidProject projectFolder, BuildType buildType);
}
