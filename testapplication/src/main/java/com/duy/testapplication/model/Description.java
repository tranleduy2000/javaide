package com.duy.testapplication.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public class Description {
    private String className;
    private Member member;
    private Type type;
    private String simpleName;

    public Type getType() {
        return type;
    }

    public Member getMember() {
        return member;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
