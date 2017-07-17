package com.duy.project_files;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.duy.editor.R;
import com.duy.editor.activities.AbstractAppCompatActivity;
import com.duy.editor.file.FileManager;
import com.duy.project_files.fragments.FolderStructureFragment;

import java.io.File;
import java.io.IOException;


public class FileManagerActivity extends AbstractAppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        try {
            ProjectFile projectFile = new ProjectFile("Main", "com.duy.example", "Demo100");
             projectFile.create(new File(FileManager.EXTERNAL_DIR));
            fragmentTransaction.replace(R.id.container, FolderStructureFragment.newInstance(projectFile)).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}