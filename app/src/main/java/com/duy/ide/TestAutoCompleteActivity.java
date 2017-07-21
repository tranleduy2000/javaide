package com.duy.ide;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.autocomplete.autocomplete.AutoCompleteProvider;
import com.duy.ide.editor.EditorFragment;

import java.io.File;

/**
 * Created by Duy on 21-Jul-17.
 */

public class TestAutoCompleteActivity extends AbstractAppCompatActivity {
    private AutoCompleteProvider mAutoCompleteProvider;
    private EditorFragment mEditorFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_autocompelte);
        File file = new File(Environment.getExternalStorageDirectory(), "JavaNIDE/sample/src/main/java/com/duy/Main.java");
        mEditorFragment = EditorFragment.newInstance(file.getPath());
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mEditorFragment).commit();


        mAutoCompleteProvider = new AutoCompleteProvider(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAutoCompleteProvider.load();
                mEditorFragment.setAutoCompleteProvider(mAutoCompleteProvider);
            }
        }).start();
    }
}
