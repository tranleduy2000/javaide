package com.duy.android.compiler.builder.util;

import java.util.ArrayList;
import java.util.Arrays;

public class Argument {
    private ArrayList<String> args = new ArrayList<>();

    public Argument add(String... args) {
        this.args.addAll(Arrays.asList(args));
        return this;
    }

    public String[] toArray() {
        String[] arr = new String[args.size()];
        args.toArray(arr);
        return arr;
    }
}
