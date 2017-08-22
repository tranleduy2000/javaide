/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.util.resources;

import java.util.Locale;
import java.util.ResourceBundle;

public final class LocaleNames_zh_HK extends OpenListResourceBundle {

    // reparent to zh_TW for traditional Chinese names
    public LocaleNames_zh_HK() {
        ResourceBundle bundle = LocaleData.getLocaleNames(Locale.TAIWAN);
        setParent(bundle);
    }

    protected final Object[][] getContents() {
        return new Object[][] {};
    }
}
