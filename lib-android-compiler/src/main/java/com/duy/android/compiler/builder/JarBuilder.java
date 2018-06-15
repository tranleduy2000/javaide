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

package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.project.JavaProject;

public class JarBuilder extends BuilderImpl<JavaProject> {

    private final JavaProject javaProject;

    public JarBuilder(Context context, JavaProject javaProject) {
        super(context);
        this.javaProject = javaProject;
    }

    @Override
    public JavaProject getProject() {
        return javaProject;
    }

    @Override
    public boolean build(BuildType buildType) {
        return false;
    }

}
