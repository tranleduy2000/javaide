package com.nolanlawson.logcat.widget;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * ListPreference that I can give a disabled-looking appearance to, while it still remains enabled.  Also,
 * I can override the onClick() so that it doesn't actually pop up the list of options.
 * @author nlawson
 *
 */
public class MockDisabledListPreference extends ListPreference {

	private boolean mEnabledAppearance = false;
	private OnPreferenceClickListener onClickOverride;
	
	public MockDisabledListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MockDisabledListPreference(Context context) {
		super(context);
	}

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        boolean viewEnabled = isEnabled() && mEnabledAppearance;
        enableView(view, viewEnabled);
    }

    protected void enableView( View view, boolean enabled){
        view.setEnabled(enabled);
        if ( view instanceof ViewGroup){
            ViewGroup grp = (ViewGroup)view;
            for ( int index = 0; index < grp.getChildCount(); index++)
                enableView(grp.getChildAt(index), enabled);
        }
    }
    public void setEnabledAppearance( boolean enabled){
        mEnabledAppearance = enabled; 
        notifyChanged();
    }
    
    @Override
    protected void onClick() {
    	if (onClickOverride != null) {
    		onClickOverride.onPreferenceClick(this);
    	} else {
    		super.onClick();
    	}
    		
    }
    
    public void overrideOnClick(OnPreferenceClickListener onClickOverride) {
    	this.onClickOverride = onClickOverride;
    }
}
