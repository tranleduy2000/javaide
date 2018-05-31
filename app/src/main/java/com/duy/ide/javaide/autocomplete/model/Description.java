package com.duy.ide.javaide.autocomplete.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public interface Description {
    String getName();

    String getDescription();

    long getLastUsed();

    void setLastUsed(long time);

    String getType();

    String getSnippet();

    int getDescriptionType();
}