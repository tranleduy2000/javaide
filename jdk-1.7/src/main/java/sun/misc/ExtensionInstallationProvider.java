/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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

package sun.misc;

/**
 * This interface defines the contract a extension installation capable
 * provided to the extension installation dependency mechanism to
 * install new extensions on the user's disk
 *
 * @author  Jerome Dochez
 */
public interface ExtensionInstallationProvider {

    /*
     * <p>
     * Request the installation of an extension in the extension directory
     * </p>
     *
     * @param requestExtInfo information on the extension that need to be
     * installed
     * @param installedExtInfo information on the current compatible installed
     * extension. Can be null if no current installation has been found.
     * @return true if the installation is successful, false if the
     * installation could not be attempted.
     * @exception ExtensionInstallationException if an installation was
     * attempted but did not succeed.
     */
    boolean installExtension(ExtensionInfo requestExtInfo,
                             ExtensionInfo installedExtInfo)
        throws ExtensionInstallationException;
}
