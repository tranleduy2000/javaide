package com.duy.java8.util;

import java.util.Comparator;

public class DComparator {
    public static <T> Comparator<T> thenComparing(final Comparator<? super T> first,
                                                  final Comparator<? super T> second) {
        return new Comparator<T>() {
            @Override
            public int compare(T c1, T c2) {
                int res = first.compare(c1, c2);
                return (res != 0) ? res : second.compare(c1, c2);
            }
        };
    }
}
