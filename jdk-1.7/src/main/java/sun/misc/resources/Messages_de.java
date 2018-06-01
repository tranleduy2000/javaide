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

public class Messages_de extends java.util.ListResourceBundle {

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

    private static final Object[][] contents = {
        { "optpkg.versionerror", "ERROR: In JAR-Datei {0} wurde ein ung\u00FCltiges Versionsformat verwendet. Pr\u00FCfen Sie in der Dokumentation, welches Versionsformat unterst\u00FCtzt wird." },
        { "optpkg.attributeerror", "ERROR: In JAR-Datei {1} ist das erforderliche JAR-Manifestattribut {0} nicht festgelegt." },
        { "optpkg.attributeserror", "ERROR: In JAR-Datei {0} sind einige erforderliche JAR-Manifestattribute nicht festgelegt." }
    };

}
