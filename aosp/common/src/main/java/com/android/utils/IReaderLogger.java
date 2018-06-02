/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.utils;

import com.android.annotations.NonNull;

import java.io.IOException;

/**
 * Interface to read a line from the {@link System#in} input stream.
 * <p/>
 * The interface also implements {@link ILogger} since code that needs to ask for
 * a command-line input will most likely also want to use {@link ILogger#info(String, Object...)}
 * to print information such as an input prompt.
 */
public interface IReaderLogger extends ILogger {

    /**
     * Reads a line from {@link System#in}.
     * <p/>
     * This call is blocking and should only be called from command-line enabled applications.
     *
     * @param inputBuffer A non-null buffer where to place the input.
     * @return The number of bytes read into the buffer.
     * @throws IOException as returned by {code System.in.read()}.
     */
    int readLine(@NonNull byte[] inputBuffer) throws IOException;
}
