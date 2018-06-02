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

package com.android.jack.api.v01;


/**
 * A task allowing to run the Jack compiler once.
 */
public interface Api01CompilationTask {

  /**
   * Runs the Jack compiler. May be called only once.
   * @throws CompilationException If a fatal error occurred during the compilation
   * @throws UnrecoverableException If an error out of Jack's control occurred
   * @throws ConfigurationException If there is an error in the configuration
   * @throws IllegalStateException If Jack is run more than once
   */
  void run() throws CompilationException, UnrecoverableException, ConfigurationException,
      IllegalStateException;
}
