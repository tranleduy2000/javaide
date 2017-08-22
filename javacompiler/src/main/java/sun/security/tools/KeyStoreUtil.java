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

package sun.security.tools;

/**
 * <p> This class provides several utilities to <code>KeyStore</code>.
 *
 * @since 1.6.0
 */
public class KeyStoreUtil {

    // Class and methods marked as public so that they can be
    // accessed by JarSigner, which although lies in a package
    // with the same name, but bundled in tools.jar and loaded
    // by another class loader, hence in a different *runtime*
    // package.
    //
    // See JVM Spec, 5.3 and 5.4.4

    private KeyStoreUtil() {
        // this class is not meant to be instantiated
    }


    /**
     * Returns true if KeyStore has a password. This is true except for
     * MSCAPI KeyStores
     */
    public static boolean isWindowsKeyStore(String storetype) {
        return storetype.equalsIgnoreCase("Windows-MY")
                || storetype.equalsIgnoreCase("Windows-ROOT");
    }

    /**
     * Returns standard-looking names for storetype
     */
    public static String niceStoreTypeName(String storetype) {
        if (storetype.equalsIgnoreCase("Windows-MY")) {
            return "Windows-MY";
        } else if(storetype.equalsIgnoreCase("Windows-ROOT")) {
            return "Windows-ROOT";
        } else {
            return storetype.toUpperCase();
        }
    }
}
