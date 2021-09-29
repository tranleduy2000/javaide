package com.duy.java8.util;

import com.duy.java8.util.function.BiFunction;
import com.duy.java8.util.function.Function;

import java.util.Map;

public class DMap {
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V v = map.get(key);
        if (v == null) {
            v = map.put(key, value);
        }

        return v;
    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key,
                                           Function<? super K, ? extends V> mappingFunction) {
        V v;
        if ((v = map.get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                map.put(key, newValue);
                return newValue;
            }
        }

        return v;
    }

    public static <K, V> V merge(Map<K, V> map, K key, V value,
                                 BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V oldValue = map.get(key);
        V newValue = (oldValue == null) ? value :
                remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            map.remove(key);
        } else {
            map.put(key, newValue);
        }
        return newValue;
    }
}
