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

package com.android.builder.internal.compiler;

import com.android.annotations.NonNull;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.DexOptions;
import com.android.ide.common.process.JavaProcessExecutor;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.android.utils.Pair;
import com.google.common.io.Files;

import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Cache for jar -> jack conversion, using the Jill tool.
 *
 * Since we cannot yet have a single task for each library that needs to be run through Jill
 * (because there is no task-level parallelization), this class allows reusing the output of
 * the jill process for a library in a project in other projects.
 *
 * Because different project could use different build-tools, both the library to be converted
 * and the version of the build tools are used as keys in the cache.
 *
 * The API is fairly simple, just call {@link #convertLibrary(File, File, DexOptions, BuildToolInfo, boolean, JavaProcessExecutor, ProcessOutputHandler)}
 *
 * The call will be blocking until the conversion happened, either through actually running Jill or
 * through copying the output of a previous Jill run.
 *
 * After a build a call to {@link #clear(java.io.File, com.android.utils.ILogger)} with a file
 * will allow saving the known converted libraries for future reuse.
 */
public class JackConversionCache extends PreProcessCache<PreProcessCache.Key> {

    private static final JackConversionCache sSingleton = new JackConversionCache();

    public static JackConversionCache getCache() {
        return sSingleton;
    }

    @NonNull
    @Override
    protected KeyFactory<Key> getKeyFactory() {
        return new KeyFactory<Key>() {
            @Override
            public Key of(@NonNull File sourceFile, @NonNull FullRevision revision,
                    @NonNull NamedNodeMap attrMap) {
                return Key.of(sourceFile, revision);
            }
        };
    }

    /**
     * Converts a given library to a given output with Jill, using a specific version of the
     * build-tools.
     *
     * @param inputFile the jar to pre-dex
     * @param outFile the output file.
     * @param dexOptions the dex options to run pre-dex
     * @param buildToolInfo the build tools info
     * @param verbose verbose flag
     * @param processExecutor the java process executor.
     * @throws ProcessException
     */
    public void convertLibrary(
            @NonNull File inputFile,
            @NonNull File outFile,
            @NonNull DexOptions dexOptions,
            @NonNull BuildToolInfo buildToolInfo,
            boolean verbose,
            @NonNull JavaProcessExecutor processExecutor,
            @NonNull ProcessOutputHandler processOutputHandler,
            @NonNull ILogger logger)
            throws ProcessException, InterruptedException, IOException {

        Key itemKey = Key.of(inputFile, buildToolInfo.getRevision());

        Pair<PreProcessCache.Item, Boolean> pair = getItem(itemKey);
        Item item = pair.getFirst();

        // if this is a new item
        if (pair.getSecond()) {
            try {
                // haven't process this file yet so do it and record it.
                List<File> files = AndroidBuilder.convertLibaryToJackUsingApis(
                        inputFile,
                        outFile,
                        dexOptions,
                        buildToolInfo,
                        verbose,
                        processExecutor,
                        processOutputHandler,
                        logger);
                item.getOutputFiles().addAll(files);

                incrementMisses();
            } catch (ProcessException exception) {
                // in case of error, delete (now obsolete) output file
                outFile.delete();
                // and rethrow the error
                throw exception;
            } finally {
                // enable other threads to use the output of this pre-dex.
                // if something was thrown they'll handle the missing output file.
                item.getLatch().countDown();
            }
        } else {
            // wait until the file is pre-dexed by the first thread.
            item.getLatch().await();

            // check that the generated file actually exists
            // while the api allow for 2+ files, there's only ever one in this case.
            File fromFile = item.getOutputFiles().get(0);

            if (fromFile.isFile()) {
                // file already pre-dex, just copy the output.
                // while the api allow for 2+ files, there's only ever one in this case.
                Files.copy(fromFile, outFile);
                incrementHits();
            }
        }
    }
}
