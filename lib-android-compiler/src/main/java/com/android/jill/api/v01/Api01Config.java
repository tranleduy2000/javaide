/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jill.api.v01;


import com.android.jill.api.JillConfig;

import java.io.File;

import javax.annotation.Nonnull;

/**
* A configuration implementation for API level 01 of Jill.
*/
public interface Api01Config extends JillConfig {

  /**
   * Sets verbosity mode.
   * @param isVerbose the desired verbosity mode
   */
  void setVerbose(boolean isVerbose) throws ConfigurationException;

  /**
   * Sets jar file to apply the Jill translation onto. The file must exist.
   * @param input jar file to translate
   */
  void setInputJavaBinaryFile(@Nonnull File input) throws ConfigurationException;

  /**
   * Sets the file where the output Jack library will be written. The file may already exist and
   * will be overwritten.
   * @param outputJackFile The output Jack library file
   * @throws ConfigurationException
   */
  void setOutputJackFile(@Nonnull File outputJackFile) throws ConfigurationException;


  /**
   * Sets whether debug info should be emitted.
   * @param debugInfo the desired mode for debug info emission
   */
  void setDebugInfo(boolean debugInfo) throws ConfigurationException;

  /**
   * Creates an instance of the {@link Api01TranslationTask} according to this configuration.
   * @return The {@link Api01TranslationTask}
   */
  @Nonnull
  Api01TranslationTask getTask() throws ConfigurationException;
}
