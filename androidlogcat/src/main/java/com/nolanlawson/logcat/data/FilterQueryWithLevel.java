package com.nolanlawson.logcat.data;

public class FilterQueryWithLevel {

	private String filterQuery;
	private String logLevel;
	
	public FilterQueryWithLevel(String filterQuery, String logLevel) {
		this.filterQuery = filterQuery;
		this.logLevel = logLevel;
	}
	public String getFilterQuery() {
		return filterQuery;
	}
	public String getLogLevel() {
		return logLevel;
	}
}
