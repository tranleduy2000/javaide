/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
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

public final class CurrencyNames_zh_SG extends OpenListResourceBundle {

    // reparent to zh_CN for simplified Chinese names
    public CurrencyNames_zh_SG() {
        ResourceBundle bundle = LocaleData.getCurrencyNames(Locale.CHINA);
        setParent(bundle);
    }

    protected final Object[][] getContents() {
        return new Object[][] {
            {"CNY", "CNY"},
            {"SGD", "S$"},
        };
    }
}
