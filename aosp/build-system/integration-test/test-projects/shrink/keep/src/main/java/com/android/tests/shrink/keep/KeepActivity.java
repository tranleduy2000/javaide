package com.android.tests.shrink.keep;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

public class KeepActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.used1);
        reference((int)System.currentTimeMillis());
    }

    private void reference(int count) {
        Resources resources = getResources();
        int dynamicId1 = resources.getIdentifier("unused" + count, "layout", getPackageName());
        System.out.println(dynamicId1);

        System.out.println(R.layout.unused2);
    }
}
