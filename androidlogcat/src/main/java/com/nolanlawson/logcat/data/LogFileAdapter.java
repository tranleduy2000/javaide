package com.nolanlawson.logcat.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.helper.SaveLogHelper;

public class LogFileAdapter extends ArrayAdapter<CharSequence> {

	public static final String USER_READABLE_DATE_FORMAT = "MMM dd, yyyy hh:mm aaa";
	
	private List<CharSequence> objects;
	private int checked;
	private boolean multiMode;
	private boolean[] checkedItems;
	private int resId;
	
	public LogFileAdapter(Context context, List<CharSequence> objects, int checked, boolean multiMode) {
		
		super(context, -1, objects);
		this.objects = objects;
		this.checked = checked;
		this.multiMode = multiMode;
		if (multiMode) {
			checkedItems = new boolean[objects.size()];
		}
		resId = multiMode? R.layout.checkbox_dropdown_filename : R.layout.spinner_dropdown_filename;
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
		
		CharSequence filename = objects.get(position);

		text1.setText(filename);
		
		
		if (multiMode) {
			text1.setChecked(checkedItems[position]);
		} else {
			text1.setChecked(checked == position);
		}
		
		Date lastModified = SaveLogHelper.getLastModifiedDate(filename.toString());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(USER_READABLE_DATE_FORMAT);
		
		text2.setText(simpleDateFormat.format(lastModified));
		
		return view;
	}
	
	public void checkOrUncheck(int position) {
		checkedItems[position] = !checkedItems[position];
		notifyDataSetChanged();
	}
	
	public boolean[] getCheckedItems() {
		return checkedItems;
	}
}
