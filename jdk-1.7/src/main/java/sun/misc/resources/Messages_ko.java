/*
 * Copyright (c) 2002, 2005, Oracle and/or its affiliates. All rights reserved.
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

package sun.misc.resources;

/**
 * <p> This class represents the <code>ResourceBundle</code>
 * for sun.misc.
 *
 * @author Michael Colburn
 */

public class Messages_ko extends java.util.ListResourceBundle {

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

    private static final Object[][] contents = {
        { "optpkg.versionerror", "\uC624\uB958: {0} JAR \uD30C\uC77C\uC5D0 \uBD80\uC801\uD569\uD55C \uBC84\uC804 \uD615\uC2DD\uC774 \uC0AC\uC6A9\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uC124\uBA85\uC11C\uC5D0\uC11C \uC9C0\uC6D0\uB418\uB294 \uBC84\uC804 \uD615\uC2DD\uC744 \uD655\uC778\uD558\uC2ED\uC2DC\uC624." },
        { "optpkg.attributeerror", "\uC624\uB958: \uD544\uC694\uD55C {0} JAR manifest \uC18D\uC131\uC774 {1} JAR \uD30C\uC77C\uC5D0 \uC124\uC815\uB418\uC5B4 \uC788\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4." },
        { "optpkg.attributeserror", "\uC624\uB958: \uD544\uC694\uD55C \uC77C\uBD80 JAR manifest \uC18D\uC131\uC774 {0} JAR \uD30C\uC77C\uC5D0 \uC124\uC815\uB418\uC5B4 \uC788\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4." }
    };

}
