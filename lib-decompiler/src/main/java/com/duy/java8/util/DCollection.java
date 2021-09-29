package com.duy.java8.util;

import com.duy.java8.util.function.Predicate;

import java.util.Iterator;

public class DCollection {
    public static <E> boolean removeIf(java.util.Collection<E> collection,
                                       Predicate<? super E> filter) {
        boolean removed = false;
        final Iterator<E> each = collection.iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }
}
