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

public class Messages_pt_BR extends java.util.ListResourceBundle {

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

    private static final Object[][] contents = {
        { "optpkg.versionerror", "ERRO: formato de vers\u00E3o inv\u00E1lido usado no arquivo JAR {0}. Verifique a documenta\u00E7\u00E3o para obter o formato de vers\u00E3o suportado." },
        { "optpkg.attributeerror", "ERRO: o atributo de manifesto JAR {0} necess\u00E1rio n\u00E3o est\u00E1 definido no arquivo JAR {1}." },
        { "optpkg.attributeserror", "ERRO: alguns atributos de manifesto JAR necess\u00E1rios n\u00E3o est\u00E3o definidos no arquivo JAR {0}." }
    };

}
