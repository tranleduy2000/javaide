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

package com.android.ide.common.internal;

import com.android.annotations.NonNull;

import java.io.File;

/**
 * An object able to crunch a png.
 */
public interface PngCruncher {

    /**
     * Initiates a series of crunching requests. The call to this method must be followed
     * by a call to {@link #end(int)} that will allow to wait for all crunching requests made
     * with the {@link #crunchPng(int, File, File)} method.
     *
     * @return the key for this set of crunching requests.
     */
    int start();

    /**
     * Crunch a given file into another given file. This may be implemented synchronously or
     * asynchronously. Therefore the output file may not be present until {@link #end()} is called
     * and returned. When implemented asynchronously, this act like queueing a crunching request.
     * So this can be called multiple times and when
     * {@link #end(int)} is called and returned, all output files will be present.
     *
     * @param key obtained from the {@link #start()}
     * @param from the file to crunch
     * @param to the output file
     *
     * @throws PngException
     */
    void crunchPng(int key, @NonNull File from, @NonNull File to) throws PngException;

    /**
     * Wait until all Png crunching requests have been executed. If there are no other users of
     * this PNG cruncher service, it can shutdown all associated thread pools or native resources.
     *
     * @param key obtained in the {@link #start()}
     * @throws InterruptedException
     */
    void end(int key) throws InterruptedException;
}
