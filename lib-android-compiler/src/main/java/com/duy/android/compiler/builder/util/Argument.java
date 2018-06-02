package com.duy.android.compiler.builder.util;

import java.util.ArrayList;
import java.util.Arrays;

public class Argument {
    private ArrayList<String> args = new ArrayList<>();

    public Argument(String... args) {
        add(args);
    }

    public Argument add(String... args) {
        this.args.addAll(Arrays.asList(args));
        return this;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "args=" + args +
                '}';
    }

    public String[] toArray() {
        ArrayList<String> clean = new ArrayList<>();
        for (String arg : args) {
            if (arg != null && !arg.isEmpty()) {
                clean.add(arg);
            }
        }
        String[] array = new String[clean.size()];
        clean.toArray(array);
        return array;
    }
}
