/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

public interface JavaAWTAccess {
    public Object get(Object key);
    public void put(Object key, Object value);
    public void remove(Object key);
    public boolean isDisposed();
    public boolean isMainAppContext();
}
