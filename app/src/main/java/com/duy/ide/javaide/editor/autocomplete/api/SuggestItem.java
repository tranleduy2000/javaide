package com.duy.ide.javaide.editor.autocomplete.api;

/**
 * The item will be display in list suggestion
 * <p>
 * Type|name|description
 * <p>
 * Created by Duy on 20-Jul-17.
 */

public interface SuggestItem {
    /**
     * Display name
     */
    String getName();

    /**
     * Display description
     */
    String getDescription();

    /**
     * Display type
     */
    String getType();

    /**
     * @return the text will be insert then user click suggestion item
     */
    String getInsertText();

    int getSuggestionPriority();
}