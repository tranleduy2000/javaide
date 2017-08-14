package com.nolanlawson.logcat.db;

import java.util.Comparator;


public class FilterItem implements Comparable<FilterItem> {
	
	public static final Comparator<FilterItem> DEFAULT_COMPARATOR = new Comparator<FilterItem>(){

		@Override
		public int compare(FilterItem lhs, FilterItem rhs) {
			String leftText = lhs.text != null ? lhs.text : "";
			String rightText = rhs.text != null ? rhs.text : "";
			return leftText.compareToIgnoreCase(rightText);
		}};

	private FilterItem() {
	}
	
	private int id;
	private String text;

	public String getText() {
		return text;
	}
	
	public int getId() {
		return id;
	}

	public static FilterItem create(int id, String text) {
		FilterItem filterItem = new FilterItem();
		filterItem.id = id;
		filterItem.text = text;
		return filterItem;
	}

	@Override
	public int compareTo(FilterItem another) {
		return DEFAULT_COMPARATOR.compare(this, another);
	}
}
