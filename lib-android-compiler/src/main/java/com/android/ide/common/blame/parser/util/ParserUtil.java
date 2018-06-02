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
package com.android.ide.common.blame.parser.util;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

public class ParserUtil {
  private ParserUtil() {
  }

  @Nullable
  public static String digestStackTrace(@NonNull OutputLineReader reader) {
    String next = reader.peek(0);
    if (next == null) {
      return null;
    }

    int index = next.indexOf(':');
    if (index == -1) {
      return null;
    }

    String message = null;
    String exceptionName = next.substring(0, index);
    if (exceptionName.endsWith("Exception") || exceptionName.endsWith("Error")) {
      message = next.substring(index + 1).trim();
      reader.readLine();

      // Digest stack frames below it
      while (true) {
        String peek = reader.peek(0);
        if (peek != null && peek.startsWith("\tat")) {
          reader.readLine();
        } else {
          break;
        }
      }
    }

    return message;
  }
}
