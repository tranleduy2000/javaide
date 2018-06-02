/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.validator;

import com.android.dvlib.DeviceSchema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DeviceValidator {

    public static void main(String[] args) {
        if (args.length == 0){
            printHelp();
            System.exit(1);
        }
        int ret = 0;
        for (String a : args) {
            File f = (new File(a)).getAbsoluteFile();
            try {
                if (!DeviceSchema.validate(new FileInputStream(f), System.err, f.getParentFile())) {
                    System.err.println("Error validating " + f.getAbsolutePath());
                    System.out.println();
                    ret = 1;
                } else {
                    System.out.println(f.getAbsolutePath() + " validated successfully.");
                }
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + a);
                ret = 1;
            }
        }
        System.exit(ret);
    }

    private static void printHelp() {
        System.err.printf("Usage: device_validator [files to validate]...\n");
    }

}
