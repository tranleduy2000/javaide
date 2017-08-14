package com.nolanlawson.logcat.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.db.CatlogDBHelper;
import com.nolanlawson.logcat.db.FilterItem;

public class FilterAdapter extends ArrayAdapter<FilterItem> {

	public FilterAdapter(Context context, List<FilterItem> items) {
		super(context, R.layout.filter_with_delete, items);
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}



	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int resId = position == 0 ? R.layout.filter_with_add : R.layout.filter_with_delete;
		view = inflater.inflate(resId, parent, false);
		
		final FilterItem filterItem = getItem(position);
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		

		if (position != 0) { // delete type
			textView.setText(filterItem.getText());
			// add listener to the delete button
			Button button = (Button) view.findViewById(android.R.id.button1);
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//delete
					CatlogDBHelper dbHelper = null;
					try {
						dbHelper = new CatlogDBHelper(getContext());
						dbHelper.deleteFilter(filterItem.getId());
					} finally {
						if (dbHelper != null) {
							dbHelper.close();
						}
					}
					remove(filterItem);
					notifyDataSetChanged();
				}
			});
		}
		
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? 0 : 1; // first one is 'add', the rest are different
	}

	@Override
	public int getViewTypeCount() {
		return 2;// one for 'add', one for the others
	}
	
}
