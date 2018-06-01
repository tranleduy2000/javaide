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

public class Messages_fr extends java.util.ListResourceBundle {

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

    private static final Object[][] contents = {
        { "optpkg.versionerror", "ERREUR\u00A0: le format de version utilis\u00E9 pour le fichier JAR {0} n''est pas valide. Pour conna\u00EEtre le format de version pris en charge, consultez la documentation." },
        { "optpkg.attributeerror", "ERREUR\u00A0: l''attribut manifest JAR {0} obligatoire n''est pas d\u00E9fini dans le fichier JAR {1}." },
        { "optpkg.attributeserror", "ERREUR\u00A0: certains attributs manifest JAR obligatoires ne sont pas d\u00E9finis dans le fichier JAR {0}." }
    };

}
