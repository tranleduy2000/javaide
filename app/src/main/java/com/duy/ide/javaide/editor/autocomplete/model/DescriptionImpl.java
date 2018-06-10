package com.duy.ide.javaide.editor.autocomplete.model;


import com.duy.ide.code.api.SuggestItem;

/**
 * Created by Duy on 21-Jul-17.
 */

public abstract class DescriptionImpl implements SuggestItem {
    public static final int FIELD_DESC = 0;
    public static final int METHOD_DESC = 1;
    public static final int CLASS_DESC = 2;
    public static final int OTHER_DESC = 3;

    protected long lastUsed;

    @Override
    public int getSuggestionPriority() {
        return OTHER_DESC;
    }

}
