/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
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
 * Requests are functor objects; that is, they provide part of the mechanism
 * for deferred function application.
 *
 * @author      Steven B. Byrne
 */

abstract public class Request {
    /**
     * The main task of the Request object is to be exectuted from a request
     * queue.
     */
    abstract public void execute();
}
