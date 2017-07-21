package com.duy.ide.autocomplete.datastructure;


import com.duy.ide.autocomplete.model.Description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Duy on 20-Jul-17.
 */

public class Dictionary {
    private static final String TAG = "Dictionary";
    private HashMap<String, Description> map;

    public Dictionary() {
        map = new HashMap<>();
    }


    public void put(String category, Description value) {
        map.put(category, value);
    }

    public Description remove(String category) {
        return map.remove(category);
    }

    public <T> ArrayList<T> find(Class<T> t, String namePrefix) {
        ArrayList<T> result = new ArrayList<>();
        Set<Map.Entry<String, Description>> entries = map.entrySet();
        for (Map.Entry<String, Description> entry : entries) {
            if (entry.getKey().startsWith(namePrefix)) {
                try {
                    result.add(t.cast(entry.getValue()));
                } catch (ClassCastException ignored) {
                }
            }
        }
        return result;
    }

    public void touch(Description description) {
        description.setLastUsed(System.currentTimeMillis());
    }

    public int size() {
        return map.size();
    }
}
