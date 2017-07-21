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
    private HashMap<String, HashMap<String, Description>> mTrie;

    public Dictionary() {
        mTrie = new HashMap<>();
    }

    public void add(String category, String name, Description description) {
        HashMap<String, Description> map = mTrie.get(category);
        if (map == null) map = new HashMap<>();
        map.put(name, description);
        this.mTrie.put(category, map);
    }

    public void addAdd(String category, String name, Description description) {
        HashMap<String, Description> map = mTrie.get(category);
        if (map == null) map = new HashMap<>();
        map.put(name, description);
        this.mTrie.put(category, map);
    }

    public void putAll(String category, HashMap<String, Description> hashMap) {
        mTrie.put(category, hashMap);
    }

    public Description remove(String caterogry, String name) {
        HashMap<String, Description> map = mTrie.get(caterogry);
        if (map != null) {
            return map.remove(name);
        }
        return null;
    }

    public HashMap<String, Description> removeCategory(String key) {

        return mTrie.remove(key);
    }

    public ArrayList<Description> find(String category, String namePrefix) {
        HashMap<String, Description> map = mTrie.get(category);
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
        HashMap<String, Description> map = mTrie.get(category);
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

    public HashMap<String, Description> getTrie(String category, boolean create) {
        HashMap<String, Description> map = mTrie.get(category);
        if (map != null && create) {
            map = new HashMap<>();
            mTrie.put(category, map);
        }
        return map;
    }
}
