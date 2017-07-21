package com.duy.ide.autocomplete.datastructure;


import android.util.Log;

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
    private HashMap<String, HashMap<String, Description>> map;

    public Dictionary() {
        map = new HashMap<>();
    }

    public void put(String category, String name, Description description) {
        Log.d(TAG, "put() called with: category = [" + category + "], name = [" + name + "], description = [" + description + "]");

        HashMap<String, Description> map = this.map.get(category);
        if (map == null) map = new HashMap<>();
        map.put(name, description);
        this.map.put(category, map);
    }

    public void addAdd(String category, String name, Description description) {
        HashMap<String, Description> map = this.map.get(category);
        if (map == null) map = new HashMap<>();
        map.put(name, description);
        this.map.put(category, map);
    }

    public void putAll(String category, HashMap<String, Description> hashMap) {
        map.put(category, hashMap);
    }

    public Description remove(String category, String name) {
        HashMap<String, Description> map = this.map.get(category);
        if (map != null) {
            return map.remove(name);
        }
        return null;
    }

    public HashMap<String, Description> removeCategory(String key) {

        return map.remove(key);
    }

    public ArrayList<Description> find(String category, String namePrefix) {
        HashMap<String, Description> map = this.map.get(category);
        ArrayList<Description> result = new ArrayList<>();
        if (map != null) {
            Set<Map.Entry<String, Description>> entries = map.entrySet();
            for (Map.Entry<String, Description> entry : entries) {
                if (entry.getKey().startsWith(namePrefix)) {
                    result.add(entry.getValue());
                }
            }
        }
        Log.d(TAG, "find() returned: " + result);
        return result;
    }

    public <T> ArrayList<T> find(Class<T> t, String category, String namePrefix) {
        HashMap<String, Description> map = this.map.get(category);
        ArrayList<T> result = new ArrayList<>();
        if (map != null) {
            Set<Map.Entry<String, Description>> entries = map.entrySet();
            for (Map.Entry<String, Description> entry : entries) {
                if (entry.getKey().startsWith(namePrefix)) {
                    try {
                        result.add(t.cast(entry.getValue()));
                    } catch (ClassCastException ignored) {
                    }
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
