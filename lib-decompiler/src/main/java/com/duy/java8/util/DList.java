package com.duy.java8.util;

import com.duy.java8.util.function.Consumer;
import com.duy.java8.util.function.Predicate;

import java.util.ArrayList;
import java.util.List;

public class DList {
    public static <E> void forEach(List<E> list, Consumer<? super E> action) {
        for (E e : list) {
            action.accept(e);
        }
    }

    public static <E> List<E> filter(List<E> input, Predicate<E> predicate) {
        List<E> list = new ArrayList<>();
        for (E e : input) {
            if (predicate.test(e)) {
                list.add(e);
            }
        }
        return list;
    }
}
