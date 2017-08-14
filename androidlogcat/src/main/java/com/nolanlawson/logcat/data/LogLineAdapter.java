/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * stolen almost completely from ArrayAdapter.java - nolan
 */

package com.nolanlawson.logcat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.helper.PreferenceHelper;
import com.nolanlawson.logcat.util.LogLineAdapterUtil;
import com.nolanlawson.logcat.util.StopWatch;
import com.nolanlawson.logcat.util.UtilLogger;

/**
 * A ListAdapter that manages a ListView backed by an array of arbitrary
 * objects.  By default this class expects that the provided resource id references
 * a single TextView.  If you want to use a more complex layout, use the constructors that
 * also takes a field id.  That field id should reference a TextView in the larger layout
 * resource.
 *
 * However the TextView is referenced, it will be filled with the toString() of each object in
 * the array. You can add lists or arrays of custom objects. Override the toString() method
 * of your objects to determine what text will be displayed for the item in the list.
 *
 * To use something other than TextViews for the array display, for instance, ImageViews,
 * or to have some of data besides toString() results fill the views,
 * override {@link #getView(int, View, ViewGroup)} to return the type of view you want.
 */
public class LogLineAdapter extends BaseAdapter implements Filterable {
	
	private static UtilLogger log = new UtilLogger(LogLineAdapter.class);
	
	private Comparator<? super LogLine> mComparator;
	
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<LogLine> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    private Context mContext;    

    private ArrayList<LogLine> mOriginalValues;
    private ArrayFilter mFilter;

    private LayoutInflater mInflater;
    
    private int logLevelLimit = 0;




    /**
     * Constructor
     *
     * @param context The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    public LogLineAdapter(Context context, int textViewResourceId, List<LogLine> objects) {
        init(context, textViewResourceId, 0, objects);
    }


    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(LogLine object) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(object);
                mObjects.add(object);
                
                if (mNotifyOnChange) notifyDataSetChanged();
            }
        } else {
            mObjects.add(object);
            if (mNotifyOnChange) notifyDataSetChanged();
        }
    }
    

	public void addWithFilter(LogLine object, CharSequence text) {
		
        if (mOriginalValues != null) {
        	
        	List<LogLine> inputList = Arrays.asList(object);
            
            if (mFilter == null) {
            	mFilter = new ArrayFilter();
            }
            
            List<LogLine> filteredObjects = mFilter.performFilteringOnList(inputList, text);
        	
            synchronized (mLock) {
                mOriginalValues.add(object);
                
                mObjects.addAll(filteredObjects);
                
                if (mNotifyOnChange) notifyDataSetChanged();
            }
        } else {
        	synchronized (mLock) {
        		mObjects.add(object);
        	}
            if (mNotifyOnChange) notifyDataSetChanged();
        }
        
		
	}    

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(LogLine object, int index) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(index, object);
                if (mNotifyOnChange) notifyDataSetChanged();
            }
        } else {
            mObjects.add(index, object);
            if (mNotifyOnChange) notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(LogLine object) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.remove(object);
            }
        } else {
            mObjects.remove(object);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }
    
    public void removeFirst(int n) {
    	StopWatch stopWatch = new StopWatch("removeFirst()");
    	if (mOriginalValues != null) {
    		synchronized (mLock) {
    			List<LogLine> subList = mOriginalValues.subList(n, mOriginalValues.size());
    			for (int i = 0; i < n; i++) { 
    				// value to delete - delete it from the mObjects as well
    				mObjects.remove(mOriginalValues.get(i));
    			}
    			mOriginalValues = new ArrayList<LogLine>(subList);
    		}
    	} else {
    		synchronized (mLock) {
    			mObjects = new ArrayList<LogLine>(mObjects.subList(n, mObjects.size()));
    		}
    	}
    	if (mNotifyOnChange) notifyDataSetChanged();
    	stopWatch.log(log);
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.clear();
                mObjects.clear();
            }
        } else {
            mObjects.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super LogLine> comparator) {
    	this.mComparator = comparator;
        Collections.sort(mObjects, comparator);
        if (mNotifyOnChange) notifyDataSetChanged();        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
        
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, int resource, int textViewResourceId, List<LogLine> objects) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = resource;
        mObjects = objects;
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
    	synchronized (mLock) {
    		return mObjects.size();
    	}
    }

    /**
     * {@inheritDoc}
     */
    public LogLine getItem(int position) {
        return mObjects.get(position);
    }
    
    public List<LogLine> getTrueValues() {
    	return mOriginalValues != null ? mOriginalValues : mObjects;
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(LogLine item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	return createViewFromResource(position, convertView, parent, mResource);
    }


    private View createViewFromResource(int position, View view, ViewGroup parent,
            int resource) {
    	
    	Context context = parent.getContext();
    	LogLineViewWrapper wrapper;
		if (view == null) {
			view = mInflater.inflate(R.layout.logcat_list_item, parent, false);
			wrapper = new LogLineViewWrapper(view);
			view.setTag(wrapper);
		} else {
			wrapper = (LogLineViewWrapper) view.getTag();
		}
		
		TextView levelTextView = wrapper.getLevelTextView();
		TextView outputTextView = wrapper.getOutputTextView();
		TextView tagTextView = wrapper.getTagTextView();
		TextView pidTextView = wrapper.getPidTextView();
		TextView timestampTextView = wrapper.getTimestampTextView();
		
		
		LogLine logLine;
		try {
			logLine = getItem(position);
		} catch (IndexOutOfBoundsException ignore) {
			// XXX hack - I sometimes get array index out of bounds exceptions here
			// no idea how to solve it, so this is the best I can do
			logLine = LogLine.newLogLine("", PreferenceHelper.getExpandedByDefaultPreference(context));
		}
		
		levelTextView.setText(Character.toString(LogLine.convertLogLevelToChar(logLine.getLogLevel())));
		levelTextView.setBackgroundColor(LogLineAdapterUtil.getBackgroundColorForLogLevel(context, logLine.getLogLevel()));
		levelTextView.setTextColor(LogLineAdapterUtil.getForegroundColorForLogLevel(context, logLine.getLogLevel()));
		levelTextView.setVisibility(logLine.getLogLevel() == -1 ? View.GONE : View.VISIBLE);
		
		int foregroundColor = PreferenceHelper.getColorScheme(context).getForegroundColor(context);
		
		CharSequence output = (logLine.isExpanded() || TextUtils.isEmpty(logLine.getTag())) // empty tag indicates this is the line like "beginning of dev/log..."
				? logLine.getLogOutput() 
				: ellipsizeString(logLine.getLogOutput(), outputTextView);
		
		outputTextView.setSingleLine(!logLine.isExpanded());
		outputTextView.setText(output);
		outputTextView.setTextColor(foregroundColor);
		
		CharSequence tag = logLine.isExpanded()
				? logLine.getTag()
				: ellipsizeString(logLine.getTag(), tagTextView);

		tagTextView.setSingleLine(!logLine.isExpanded());
		tagTextView.setText(tag);
		tagTextView.setVisibility(logLine.getLogLevel() == -1 ? View.GONE : View.VISIBLE);

		
		// set the text size based on the preferences
		
		float textSize = PreferenceHelper.getTextSizePreference(context);
		
		tagTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		outputTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		levelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		
		boolean extraInfoIsVisible = logLine.isExpanded() 
				&& PreferenceHelper.getShowTimestampAndPidPreference(context)
				&& logLine.getProcessId() != -1; // -1 marks lines like 'beginning of /dev/log...' 
		
		pidTextView.setVisibility(extraInfoIsVisible ? View.VISIBLE : View.GONE);
		timestampTextView.setVisibility(extraInfoIsVisible ? View.VISIBLE : View.GONE);
		
		if (extraInfoIsVisible) {
			
			pidTextView.setTextColor(foregroundColor);
			timestampTextView.setTextColor(foregroundColor);
			
			pidTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
			timestampTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
			
			pidTextView.setText(logLine.getProcessId() != -1 ? Integer.toString(logLine.getProcessId()) : null);
			timestampTextView.setText(logLine.getTimestamp());
		}

		tagTextView.setTextColor(LogLineAdapterUtil.getOrCreateTagColor(context, logLine.getTag()));
		
		// if this is a "partially selected" log, change the color to orange or whatever
		
		int selectedBackground = logLine.isHighlighted() 
				? PreferenceHelper.getColorScheme(context).getSelectedColor(context) 
				: context.getResources().getColor(android.R.color.transparent);
		view.setBackgroundColor(selectedBackground);
		
		return view;
    }

    /**
     * Optimization for when you have TextViews that truncate, per
     * http://www.martinadamek.com/2011/01/05/performance-of-android-listview-containing-textviews/
     * @param str
     * @param textView
     * @return
     */
    private CharSequence ellipsizeString(CharSequence str, TextView textView) {
    	
    	int width = textView.getWidth() - textView.getCompoundPaddingLeft() - textView.getCompoundPaddingRight();
    	
		return ellipsizeString(str, width, textView.getPaint());
	}

    private CharSequence ellipsizeString(CharSequence str, int width, TextPaint textPaint) {
    	
		if (TextUtils.isEmpty(str)) {
			return str;
		}
		
		if (width <= 0) { // view probably hasn't been drawn yet; just return the whole string to avoid blank textviews
			return str;
		}
		
		return ellipsizeFromCache(str, width, textPaint);
	}    
    
    

	private CharSequence ellipsizeFromCache(CharSequence str, int width, TextPaint paint) {
		// the TextUtils.ellipsize method is really expensive, so we can exploit the fact that we're using monospace-style text
		// to just cache the correct number of characters given the width
		
		Integer maxLength = PreferenceHelper.ellipsisLengthsCache.get(width);
		
		if (maxLength != null) { // cached
			
			if (str.length() < maxLength) {
				return str;
			}
			
			// truncate and add ellipsis
			StringBuilder stringBuilder = new StringBuilder(str);
			if (stringBuilder.length() > maxLength) {
				stringBuilder.delete(maxLength, stringBuilder.length());
			}
			if (stringBuilder.length() > 0) {
				stringBuilder.setCharAt(stringBuilder.length() - 1, (char) 8230); // add ellipsis character
			}
			return stringBuilder;
		}
		
		CharSequence result = TextUtils.ellipsize(str, paint, width, TruncateAt.END);
		if (result.length() < str.length()) { // was ellipsized
			PreferenceHelper.ellipsisLengthsCache.put(width, result.length());
		}
		return result;
		
	}


	/**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

	public int getLogLevelLimit() {
		return logLevelLimit;
	}


	public void setLogLevelLimit(int logLevelLimit) {
		this.logLevelLimit = logLevelLimit;
	}

	/**
     * {@inheritDoc}
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public List<LogLine> getObjects() {
    	return mObjects;
    }
    
    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<LogLine>(mObjects);
                }
            }
            
            ArrayList<LogLine> allValues = performFilteringOnList(mOriginalValues, prefix);
            
            results.values = allValues;
            results.count = allValues.size();
            
            return results;
        }
        
        public ArrayList<LogLine> performFilteringOnList(List<LogLine> inputList, CharSequence query) {
            
        	SearchCriteria searchCriteria = new SearchCriteria(query);
            
            // search by log level
            ArrayList<LogLine> allValues = new ArrayList<LogLine>();
            
            ArrayList<LogLine> logLines;
            synchronized (mLock) {
            	logLines = new ArrayList<LogLine>(inputList);
            }
            
            for (LogLine logLine : logLines) {
            	if (logLine != null && 
            			LogLineAdapterUtil.logLevelIsAcceptableGivenLogLevelLimit(logLine.getLogLevel(), logLevelLimit)) {
            		allValues.add(logLine);
            	}
            }
            ArrayList<LogLine> finalValues = allValues;
            
            // search by criteria
            if (!searchCriteria.isEmpty()) {

                final ArrayList<LogLine> values = allValues;
                final int count = values.size();

                final ArrayList<LogLine> newValues = new ArrayList<LogLine>(count);

                for (int i = 0; i < count; i++) {
                    final LogLine value = values.get(i);
                    // search the logline based on the criteria
                    if (searchCriteria.matches(value)) {
                        newValues.add(value);
                    }
                }

                finalValues = newValues;
            }
            
            // sort here to ensure that filtering the list doesn't mess up the sorting
            if (mComparator != null) {
            	Collections.sort((List<LogLine>)finalValues, mComparator);
            }
            
            return finalValues;        	
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
        	
        	//log.d("filtering: %s", constraint);
        	
			
            mObjects = (List<LogLine>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}