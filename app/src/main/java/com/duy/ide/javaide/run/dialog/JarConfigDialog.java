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

package com.duy.ide.javaide.run.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.android.compiler.builder.internal.jar.JarOptions;
import com.duy.android.compiler.builder.internal.jar.JarOptionsImpl;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.R;

import java.io.File;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.regex.Pattern;

import static com.duy.ide.javaide.editor.autocomplete.parser.JavaUtil.listClassName;

public class JarConfigDialog extends AppCompatDialogFragment {
    private JavaProject mJavaProject;
    private Spinner mSpinnerClasses;
    private EditText mEditAttrs;
    private JarConfigListener mJarConfigListener;

    public static JarConfigDialog newInstance(JavaProject project, JarConfigListener jarConfigListener) {
        JarConfigDialog fragment = new JarConfigDialog();
        fragment.mJavaProject = project;
        fragment.mJarConfigListener = jarConfigListener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_jar_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mJavaProject == null) {
            return;
        }
        mEditAttrs = view.findViewById(R.id.edit_attrs);

        ArrayList<File> javaSrcDirs = mJavaProject.getJavaSrcDirs();
        ArrayList<String> names = new ArrayList<>();
        for (File javaSrcDir : javaSrcDirs) {
            names.addAll(listClassName(javaSrcDir));
        }
        names.add(0, ""); //empty class, not have main class

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                names);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mSpinnerClasses = view.findViewById(R.id.spinner_classes);
        mSpinnerClasses.setAdapter(adapter);

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOk();
            }
        });
    }

    private void clickOk() {
        JarOptionsImpl jarOptions = createManifestData();
        if (jarOptions != null) {
            mJarConfigListener.onCompleteConfig(jarOptions);
            dismiss();
        }
    }

    private JarOptionsImpl createManifestData() {
        try {
            String mainClass = mSpinnerClasses.getSelectedItem().toString();
            Attributes attributes = new Attributes();
            if (mainClass != null && !mainClass.isEmpty()) {
                attributes.put(Attributes.Name.MAIN_CLASS, mainClass);
            }

            String[] attrs = mEditAttrs.getText().toString().split("\n+");
            Pattern namePattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\-]*");
            for (String attr : attrs) {
                String[] pair = attr.split(":");
                if (pair.length == 2) {
                    String name = pair[0];
                    if (!name.matches(namePattern.pattern())) {
                        mEditAttrs.setError("Invalid name " + name);
                        return null;
                    }
                    attributes.put(name, pair[1]);
                }
            }

            return new JarOptionsImpl(attributes);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    public JarConfigListener getJarConfigListener() {
        return mJarConfigListener;
    }

    public void setJarConfigListener(JarConfigListener jarConfigListener) {
        this.mJarConfigListener = jarConfigListener;
    }

    public interface JarConfigListener {
        void onCompleteConfig(JarOptions jarOptions);
    }

}
