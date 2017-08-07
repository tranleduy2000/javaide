package {PACKAGE};

import android.view.TextView;

import {PACKAGE}.R;

public class {ACTIVITY_NAME} extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.txt_hello);
        
    }

}
