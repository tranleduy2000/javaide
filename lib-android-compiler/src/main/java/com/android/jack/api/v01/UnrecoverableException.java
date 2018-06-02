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

import javax.annotation.Nonnull;

/**
 * Thrown when a major problem occurred because of an event out of control (e.g. an external process
 * remove a temporary file used by Jack). Handling this error should only be reporting to the user
 * and maybe just retry exactly the same thing as the one that has thrown.
 */
public class UnrecoverableException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnrecoverableException() {
    super();
  }

  public UnrecoverableException(@Nonnull String message) {
    super(message);
  }

  public UnrecoverableException(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  public UnrecoverableException(@Nonnull Throwable cause) {
    super(cause);
  }
}
