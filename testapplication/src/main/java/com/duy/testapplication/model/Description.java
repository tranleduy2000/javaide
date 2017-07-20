package com.duy.testapplication.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public class Description {
    private String className;
    private Member member;
    private Type type;
    private String simpleName;
    private long lastUsed;

    public long getLastUsed() {
        return lastUsed;

    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

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
