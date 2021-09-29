/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.builder.core.BuilderConstants;
import com.android.utils.StringHelper;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;

/**
 * Class containing a ProductFlavor and associated data (sourcesets)
 */
public class ProductFlavorData<T extends CoreProductFlavor> extends VariantDimensionData {
    private final T productFlavor;
    private final Task assembleTask;

    ProductFlavorData(
            @NonNull T productFlavor,
            @NonNull DefaultAndroidSourceSet sourceSet,
            @NonNull Project project) {
        super(sourceSet, project);

        this.productFlavor = productFlavor;

        if (!BuilderConstants.MAIN.equals(sourceSet.getName())) {
            String sourceSetName = StringHelper.capitalize(sourceSet.getName());
            assembleTask = project.getTasks().create("assemble" + sourceSetName);
            assembleTask.setDescription("Assembles all " + sourceSetName + " builds.");
            assembleTask.setGroup(BasePlugin.BUILD_GROUP);
        } else {
            assembleTask = null;
        }
    }

    public T getProductFlavor() {
        return productFlavor;
    }

    public Task getAssembleTask() {
        return assembleTask;
    }
}
