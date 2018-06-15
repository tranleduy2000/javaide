/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.run.action;

import com.android.annotations.NonNull;
import com.duy.android.compiler.builder.internal.jar.JarOptions;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.interfaces.Action;
import com.duy.ide.javaide.JavaIdeActivity;
import com.duy.ide.javaide.run.dialog.JarConfigDialog;

public class BuildJarAction implements Action<JavaIdeActivity>, JarConfigDialog.JarConfigListener {
    private JavaProject project;

    public BuildJarAction(JavaProject project) {
        this.project = project;
    }

    @Override
    public void execute(@NonNull JavaIdeActivity javaIdeActivity) {
        JarConfigDialog jarConfigDialog = JarConfigDialog.newInstance(project, this);
        jarConfigDialog.show(javaIdeActivity.getSupportFragmentManager(), JarConfigDialog.class.getName());
    }

    @Override
    public void onCompleteConfig(JarOptions jarOptions) {

    }
}
