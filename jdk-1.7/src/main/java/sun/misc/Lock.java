/*
 * Copyright (c) 1994, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * The Lock class provides a simple, useful interface to a lock.
 * Unlike monitors which synchronize access to an object, locks
 * synchronize access to an arbitrary set of resources (objects,
 * methods, variables, etc.). <p>
 *
 * The programmer using locks must be responsible for clearly defining
 * the semantics of their use and should handle deadlock avoidance in
 * the face of exceptions. <p>
 *
 * For example, if you want to protect a set of method invocations with
 * a lock, and one of the methods may throw an exception, you must be
 * prepared to release the lock similarly to the following example:
 * <pre>
 *      class SomeClass {
 *          Lock myLock = new Lock();

 *          void someMethod() {
 *              myLock.lock();
 *              try {
 *                  StartOperation();
 *                  ContinueOperation();
 *                  EndOperation();
 *              } finally {
 *                  myLock.unlock();
 *              }
 *          }
 *      }
 * </pre>
 *
 * @author      Peter King
 */
public
class Lock {
    private boolean locked = false;

    /**
     * Create a lock, which is initially not locked.
     */
    public Lock () {
    }

    /**
     * Acquire the lock.  If someone else has the lock, wait until it
     * has been freed, and then try to acquire it again.  This method
     * will not return until the lock has been acquired.
     *
     * @exception  InterruptedException if any thread has
     *               interrupted this thread.
     */
    public final synchronized void lock() throws InterruptedException {
        while (locked) {
            wait();
        }
        locked = true;
    }

    /**
     * Release the lock.  If someone else is waiting for the lock, the
     * will be notitified so they can try to acquire the lock again.
     */
    public final synchronized void unlock() {
        locked = false;
        notifyAll();
    }
}
