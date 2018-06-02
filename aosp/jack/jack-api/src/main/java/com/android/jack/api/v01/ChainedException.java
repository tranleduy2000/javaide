/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.api.v01;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Abstract class to easily chain exceptions together.
 *
 * The exception can be managed like any other exception. In this case, the first one will be the
 * only one used.
 *
 * Special management can use the {@link #iterator()} or the {@link #getNextException()} to browse
 * all chained exceptions and dispatch them.
 *
 * See {@link ChainedExceptionBuilder} to build the chain of exceptions.
 */
public abstract class ChainedException extends Exception
    implements Iterable<ChainedException> {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private String message;

  @Nonnegative
  private int count = 1;

  @Nonnull
  private ChainedException tail = this;

  @CheckForNull
  private ChainedException next = null;

  public ChainedException(@Nonnull String message) {
    super("");
    this.message = message;
  }

  public ChainedException(@Nonnull String message, @Nonnull Throwable cause) {
    super("", cause);
    this.message = message;
  }

  public ChainedException(@Nonnull Throwable cause) {
    super(cause);
    this.message = cause.getMessage();
  }

  @Override
  @Nonnull
  public String getMessage() {
    return message;
  }

  @Override
  @Nonnull
  public String getLocalizedMessage() {
    return message;
  }

  public void setMessage(@Nonnull String message) {
    this.message = message;
  }

  @Nonnull
  protected ChainedException putAsLastExceptionOf(
      @CheckForNull ChainedException head) {
    if (head == null) {
      this.tail  = this;
      this.next  = null;
      this.count = 1;

      return this;
    } else {
      head.tail.next = this;
      head.tail = this;
      head.count++;

      return head;
    }
  }

  @CheckForNull
  public ChainedException getNextException() {
    return next;
  }

  @Nonnegative
  public int getNextExceptionCount() {
    return count;
  }

  @Override
  @Nonnull
  public Iterator<ChainedException> iterator() {
    ArrayList<ChainedException> list = new ArrayList<ChainedException>(count);

    ChainedException exception = this;
    do {
      list.add(exception);
      exception = exception.next;
    } while (exception != null);

    return list.iterator();
  }

  /**
   * Builder to construct a chain of exceptions.
   */
  public static class ChainedExceptionBuilder<T extends ChainedException> {
    @CheckForNull
    private T head = null;

    @SuppressWarnings("unchecked")
    public void appendException(@Nonnull T exceptions) {
      for (ChainedException exception : exceptions) {
        head = (T) exception.putAsLastExceptionOf(head);
      }
    }

    public void throwIfNecessary() throws T {
      if (head != null) {
        throw head;
      }
    }

    @Nonnull
    public T getException() {
      assert head != null;
      return head;
    }
  }
}
