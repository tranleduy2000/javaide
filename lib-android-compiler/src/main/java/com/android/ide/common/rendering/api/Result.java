/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.ide.common.rendering.api;

/**
 * Scene result class. This is an immutable class.
 * <p/>
 * This cannot be allocated directly, instead use
 * {@link Status#createResult()},
 * {@link Status#createResult(String, Throwable)},
 * {@link Status#createResult(String)}
 * {@link Status#createResult(Object)}
 */
public class Result {

    private final Status mStatus;
    private final String mErrorMessage;
    private final Throwable mThrowable;
    private Object mData;

    /**
     * Scene Status enum.
     * <p/>This indicates the status of all scene actions.
     */
    public enum Status {
        SUCCESS,
        NOT_IMPLEMENTED,
        ERROR_TIMEOUT,
        ERROR_LOCK_INTERRUPTED,
        ERROR_INFLATION,
        ERROR_VIEWGROUP_NO_CHILDREN,
        ERROR_NOT_INFLATED,
        ERROR_RENDER,
        ERROR_ANIM_NOT_FOUND,
        ERROR_NOT_A_DRAWABLE,
        ERROR_REFLECTION,
        ERROR_UNKNOWN;

        private Result mResult;

        /**
         * Returns a {@link Result} object with this status.
         * @return an instance of SceneResult;
         */
        public Result createResult() {
            // don't want to get generic error that way.
            assert this != ERROR_UNKNOWN;

            if (mResult == null) {
                mResult = new Result(this);
            }

            return mResult;
        }

        /**
         * Returns a {@link Result} object with this status, and the given data.
         * @return an instance of SceneResult;
         *
         * @see Result#getData()
         */
        public Result createResult(Object data) {
            Result res = createResult();

            if (data != null) {
                res = res.getCopyWithData(data);
            }

            return res;
        }

        /**
         * Returns a {@link #ERROR_UNKNOWN} result with the given message and throwable
         * @param errorMessage the error message
         * @param throwable the throwable
         * @return an instance of SceneResult.
         */
        public Result createResult(String errorMessage, Throwable throwable) {
            return new Result(this, errorMessage, throwable);
        }

        /**
         * Returns a {@link #ERROR_UNKNOWN} result with the given message
         * @param errorMessage the error message
         * @return an instance of SceneResult.
         */
        public Result createResult(String errorMessage) {
            return new Result(this, errorMessage, null /*throwable*/);
        }
    }

    /**
     * Creates a {@link Result} object with the given SceneStatus.
     *
     * @param status the status. Must not be null.
     */
    private Result(Status status) {
        this(status, null, null);
    }

    /**
     * Creates a {@link Result} object with the given SceneStatus, and the given message
     * and {@link Throwable}
     *
     * @param status the status. Must not be null.
     * @param errorMessage an optional error message.
     * @param t an optional exception.
     */
    private Result(Status status, String errorMessage, Throwable t) {
        assert status != null;
        mStatus = status;
        mErrorMessage = errorMessage;
        mThrowable = t;
    }

    private Result(Result result) {
        mStatus = result.mStatus;
        mErrorMessage = result.mErrorMessage;
        mThrowable = result.mThrowable;
    }

    /**
     * Returns a copy of the current result with the added (or replaced) given data
     * @param data the data bundle
     *
     * @return returns a new SceneResult instance.
     */
    public Result getCopyWithData(Object data) {
        Result r = new Result(this);
        r.mData = data;
        return r;
    }


    /**
     * Returns whether the status is successful.
     * <p>
     * This is the same as calling <code>getStatus() == SceneStatus.SUCCESS</code>
     * @return <code>true</code> if the status is successful.
     */
    public boolean isSuccess() {
        return mStatus == Status.SUCCESS;
    }

    /**
     * Returns the status. This is never null.
     */
    public Status getStatus() {
        return mStatus;
    }

    /**
     * Returns the error message. This is only non-null when {@link #getStatus()} returns
     * {@link Status#ERROR_UNKNOWN}
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Returns the exception. This is only non-null when {@link #getStatus()} returns
     * {@link Status#ERROR_UNKNOWN}
     */
    public Throwable getException() {
        return mThrowable;
    }

    /**
     * Returns the optional data bundle stored in the result object.
     * @return the data bundle or <code>null</code> if none have been set.
     */
    public Object getData() {
        return mData;
    }
}
