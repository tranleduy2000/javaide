/*
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
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

/* A package-private class implementing a signal handler in native code. */

final class NativeSignalHandler implements SignalHandler {

    private final long handler;

    long getHandler() {
        return handler;
    }

    NativeSignalHandler(long handler) {
        this.handler = handler;
    }

    public void handle(Signal sig) {
        handle0(sig.getNumber(), handler);
    }

    private static native void handle0(int number, long handler);
}
