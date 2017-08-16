package com.duy.ide.autocomplete.model;

/**
 * Created by Duy on 21-Jul-17.
 */

public abstract class DescriptionImpl implements Description {
    public static final int FIELD_DESC = 0;
    public static final int METHOD_DESC = 1;
    public static final int CLASS_DESC = 2;
    public static final int OTHER_DESC = 3;

    protected long lastUsed;

    @Override
    public long getLastUsed() {
        return lastUsed;
    }

    @Override
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public int getDescriptionType() {
        return OTHER_DESC;
    }

}
