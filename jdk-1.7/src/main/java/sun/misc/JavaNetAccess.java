/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URLClassLoader;

public interface JavaNetAccess {
    /**
     * return the URLClassPath belonging to the given loader
     */
    URLClassPath getURLClassPath(URLClassLoader u);
}
