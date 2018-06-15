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

import android.widget.Toast;

import com.android.annotations.NonNull;
import com.duy.android.compiler.builder.BuildTask;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.JarBuilder;
import com.duy.android.compiler.builder.internal.jar.JarOptions;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.interfaces.Action;
import com.duy.ide.R;
import com.duy.ide.diagnostic.DiagnosticPresenter;
import com.duy.ide.javaide.JavaIdeActivity;
import com.duy.ide.javaide.run.dialog.JarConfigDialog;

import java.io.PrintStream;

public class BuildJarAction implements Action<JavaIdeActivity>, JarConfigDialog.JarConfigListener {
    private JavaProject mProject;
    private JavaIdeActivity mActivity;

    public BuildJarAction(JavaProject project) {
        this.mProject = project;
    }

    @Override
    public void execute(@NonNull JavaIdeActivity javaIdeActivity) {
        this.mActivity = javaIdeActivity;
        JarConfigDialog jarConfigDialog = JarConfigDialog.newInstance(mProject, this);
        jarConfigDialog.show(
                javaIdeActivity.getSupportFragmentManager(),
                JarConfigDialog.class.getName());
    }

    @Override
    public void onCompleteConfig(@NonNull JarOptions jarOptions) {
        final DiagnosticPresenter diagnosticPresenter = mActivity.getDiagnosticPresenter();
        final IBuilder<JavaProject> builder = new JarBuilder(mActivity, mProject, jarOptions);

        builder.setStdOut(new PrintStream(diagnosticPresenter.getStandardOutput()));
        builder.setStdErr(new PrintStream(diagnosticPresenter.getErrorOutput()));

        final BuildTask.CompileListener<JavaProject> listener =
                new BuildTask.CompileListener<JavaProject>() {
                    @Override
                    public void onStart() {
                        mActivity.updateUiStartCompile();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(mActivity, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                        diagnosticPresenter.showPanel();
                        mActivity.updateUIFinishCompile();
                    }

                    @Override
                    public void onComplete() {
                        mActivity.updateUIFinishCompile();
                        Toast.makeText(mActivity, R.string.build_success, Toast.LENGTH_SHORT).show();
                    }
                };
        BuildTask<JavaProject> buildTask = new BuildTask<>(builder, listener);
        buildTask.execute();
    }
}
