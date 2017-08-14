package com.nolanlawson.logcat.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.helper.PreferenceHelper;

public class TagAndProcessIdAdapter extends ArrayAdapter<CharSequence> {
	
	private List<CharSequence> texts;
	private List<CharSequence> subtexts;
	private int checked;
	private int resId;
	private int tagColor;
	
	public TagAndProcessIdAdapter(Context context, List<CharSequence> texts, List<CharSequence> subtexts, int tagColor,
			                      int checked) {
		
		super(context, -1, texts);
		this.texts = texts;
		this.subtexts = subtexts;
		this.checked = checked;
		this.tagColor = tagColor;
		this.resId = R.layout.spinner_dropdown_tag_pid;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		Context context = parent.getContext();
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(resId, parent, false);
		}
		
		CheckedTextView text1 = (CheckedTextView) view.findViewById(android.R.id.text1);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		
		String tag = texts.get(position).toString();
		
		text1.setText(tag);
		text2.setText(subtexts.get(position));
		ColorScheme colorScheme = PreferenceHelper.getColorScheme(context);

		int backgroundColor = colorScheme.getSpinnerColor(context);
		text2.setBackgroundColor(backgroundColor);
		
		if (position == 0) { // tag
			// color should match whatever's in the main log line listview
			text2.setTextColor(tagColor);
			
		} else { // process id
			text2.setTextColor(colorScheme.getForegroundColor(context));
		}
		
		
		text1.setChecked(checked == position);
		
		return view;
	}	
}
