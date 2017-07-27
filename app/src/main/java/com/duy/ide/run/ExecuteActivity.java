package com.duy.ide.run;

import android.content.Intent;
import android.os.Bundle;

import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.compile.CompileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by Duy on 15-Jul-17.
 */

public class ExecuteActivity extends AbstractAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra(CompileManager.FILE_PATH);
        if (filePath == null) {
            finish();
            return;
        }
        compileAndRun(filePath);
    }


    private void compileAndRun(String filePath) {
        File home = getFilesDir();
        File init = new File(home, ".init");
        File javaFile = new File(filePath);
        File parent = javaFile.getParentFile();
        String nameWithoutExtension = javaFile.getName().substring(0, javaFile.getName().indexOf("."));
        try {
            FileOutputStream fos = new FileOutputStream(init);
            PrintWriter pw = new PrintWriter(fos);
            pw.print("cd");
            pw.println("cd " + parent.getPath());
            pw.println("javac -verbose " + javaFile.getName());
            pw.println("dx --dex --verbose --output=" + nameWithoutExtension + ".jar " + "./" + nameWithoutExtension + ".class");
            pw.println("java -jar " + nameWithoutExtension + ".jar " + nameWithoutExtension);
            pw.flush();
            pw.close();
            fos.close();

            //Make sure the /tmp folder ALWAYS exists
            File temp = new File(home, "tmp");
            if (!temp.exists()) temp.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
