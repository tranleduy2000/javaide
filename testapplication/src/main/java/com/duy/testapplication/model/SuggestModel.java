package com.duy.testapplication.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public class SuggestModel {
    private String name;
    private Description description;
    private int kind;
    private int modifier;
    private String type;
    private String snippet;

    public SuggestModel() {

    }

    public Description getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getSnippet() {
        return snippet;
    }
}
