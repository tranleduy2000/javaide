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

public class Messages_es extends java.util.ListResourceBundle {

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

    private static final Object[][] contents = {
        { "optpkg.versionerror", "ERROR: el formato del archivo JAR {0} pertenece a una versi\u00F3n no v\u00E1lida. Busque en la documentaci\u00F3n el formato de una versi\u00F3n soportada." },
        { "optpkg.attributeerror", "ERROR: el atributo obligatorio JAR manifest {0} no est\u00E1 definido en el archivo JAR {1}." },
        { "optpkg.attributeserror", "ERROR: algunos atributos obligatorios JAR manifest no est\u00E1n definidos en el archivo JAR {0}." }
    };

}
