/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package sun.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** NOTE: obsolete as of JDK 1.4 B75 and should be removed from the
    workspace (FIXME) */

public class FieldInfo {
    // Set by the VM directly. Do not move these fields around or add
    // others before (or after) them without also modifying the VM's code.
    private String name;
    private String signature;
    private int    modifiers;
    // This is compatible with the old reflection implementation's
    // "slot" value to allow sun.misc.Unsafe to work
    private int    slot;

    // Not really necessary to provide a constructor since the VM
    // creates these directly
    FieldInfo() {
    }

    public String name() {
        return name;
    }

    /** This is in "external" format, i.e. having '.' as separator
        rather than '/' */
    public String signature() {
        return signature;
    }

    public int modifiers() {
        return modifiers;
    }

    public int slot() {
        return slot;
    }

    /** Convenience routine */
    public boolean isPublic() {
        return (Modifier.isPublic(modifiers()));
    }
}
