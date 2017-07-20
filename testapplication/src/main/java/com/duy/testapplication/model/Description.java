package com.duy.testapplication.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public interface Description {
    String getName();

    String getDescription();

    long getLastUsed();

    void setLastUsed(long time);

    Class getType();

    String getSnippet();
}