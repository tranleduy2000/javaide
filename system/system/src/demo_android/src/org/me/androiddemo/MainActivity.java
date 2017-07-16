/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.me.androiddemo;

import android.app.Activity;
import android.os.Bundle;

import org.library.*;

/**
 *
 * @author Spartacus Rex
 */
public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here
        setContentView(R.layout.main);
	
		String test = libfunc.getMessage();

    }

}
