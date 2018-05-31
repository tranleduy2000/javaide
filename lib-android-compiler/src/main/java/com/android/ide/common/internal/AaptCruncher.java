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

package com.android.ide.common.internal;

import com.android.annotations.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of the PngCruncher using aapt underneath.
 */
public class AaptCruncher implements PngCruncher {

    private final String mAaptLocation;
    private final CommandLineRunner mCommandLineRunner;

    public AaptCruncher(@NonNull String aaptLocation, @NonNull CommandLineRunner commandLineRunner) {
        mAaptLocation = aaptLocation;
        mCommandLineRunner = commandLineRunner;
    }

    /**
     * Runs the aapt crunch command on a single file
     *
     * @param from the file to crunch
     * @param to the output file
     * @throws IOException
     * @throws InterruptedException
     * @throws LoggedErrorException
     */
    @Override
    public void crunchPng(@NonNull File from, @NonNull File to)
            throws InterruptedException, LoggedErrorException, IOException {
        String[] command = new String[] {
                mAaptLocation,
                "s",
                "-i",
                from.getAbsolutePath(),
                "-o",
                to.getAbsolutePath()
        };

        mCommandLineRunner.runCmdLine(command, null);
    }

    @Override
    public void end() throws InterruptedException {
        // nothing to do, it's all synchronous.
    }
}
