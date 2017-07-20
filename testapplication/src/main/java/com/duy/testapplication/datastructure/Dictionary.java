package com.duy.testapplication.datastructure;

import com.duy.testapplication.model.ClassDescription;
import com.duy.testapplication.model.ClassDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Duy on 20-Jul-17.
 */

public class Dictionary {
    private HashMap<String, HashMap<String, ClassDescription>> mTrie;

    public Dictionary() {
        mTrie = new HashMap<>();
    }

    public void add(String category, String name, ClassDescription description) {
        HashMap<String, ClassDescription> map = mTrie.get(category);
        if (map == null) map = new HashMap<>();
        map.put(name, description);
        this.mTrie.put(category, map);
    }

    public ClassDescription remove(String caterogry, String name) {
        HashMap<String, ClassDescription> map = mTrie.get(caterogry);
        if (map != null) {
            return map.remove(name);
        }
        return null;
    }

    public HashMap<String, ClassDescription> removeCategory(String key) {
        return mTrie.remove(key);
    }

    public ArrayList<ClassDescription> find(String category, String namePrefix) {
        HashMap<String, ClassDescription> map = mTrie.get(category);
        Set<Map.Entry<String, ClassDescription>> entries = map.entrySet();
        ArrayList<ClassDescription> result = new ArrayList<>();
        for (Map.Entry<String, ClassDescription> entry : entries) {
            if (entry.getKey().startsWith(namePrefix)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public void touch(ClassDescription description) {
        description.setLastUsed(System.currentTimeMillis());
    }

    public HashMap<String, ClassDescription> getTrie(String category, boolean create) {
        HashMap<String, ClassDescription> map = mTrie.get(category);
        if (map != null && create) {
            map = new HashMap<>();
            mTrie.put(category, map);
        }
        return map;
    }
}
