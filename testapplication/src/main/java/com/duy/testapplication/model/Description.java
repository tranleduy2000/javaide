package com.duy.testapplication.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public class Description {
    private String className;
    private MemberDescription member;
    private Type type;
    private String simpleName;
    private long lastUsed;
    private Object superClass;

    public long getLastUsed() {
        return lastUsed;

    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Type getType() {
        return type;
    }

    public MemberDescription getMember() {
        return member;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public Object getSuperClass() {
        return superClass;
    }
}
