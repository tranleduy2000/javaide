package com.nolanlawson.logcat.util;

public interface Function<E,T> {

	T apply(E input);
}
