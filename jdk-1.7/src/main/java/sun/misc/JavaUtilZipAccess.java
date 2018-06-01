/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.zip.Adler32;
import java.nio.ByteBuffer;

public interface JavaUtilZipAccess {
    public void update(Adler32 adler32, ByteBuffer buf);
}
