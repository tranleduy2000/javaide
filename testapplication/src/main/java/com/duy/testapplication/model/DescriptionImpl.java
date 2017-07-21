package com.duy.testapplication.model;

/**
 * Created by Duy on 21-Jul-17.
 */

public abstract class DescriptionImpl implements Description{
    protected long lastUsed;

    @Override
    public long getLastUsed() {
        return lastUsed;
    }

    @Override
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }
}
