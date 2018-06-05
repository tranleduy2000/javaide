/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;

import java.io.File;

/**
 * Task for generating a mockable android.jar
 */
public class MockableAndroidJarTask extends DefaultTask {

   private File mAndroidJar;

   private File mOutputFile;

   /**
    * Whether the generated jar should return default values from all methods or throw exceptions.
    */
   private boolean mReturnDefaultValues;

   public void setReturnDefaultValues(boolean returnDefaultValues) {
      mReturnDefaultValues = returnDefaultValues;
   }

   @OutputFile
   public File getOutputFile() {
      return mOutputFile;
   }

   public void setOutputFile(File outputFile) {
       mOutputFile = outputFile;
   }

   public void setAndroidJar(File androidJar) {
       mAndroidJar = androidJar;
   }
}
