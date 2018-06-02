/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.ide.common.resources.configuration;


/**
 * Base class for resource qualifiers.
 * <p/>The resource qualifier classes are designed as immutable.
 */
public abstract class ResourceQualifier implements Comparable<ResourceQualifier> {

    /**
     * Returns the human readable name of the qualifier.
     */
    public abstract String getName();

    /**
     * Returns a shorter human readable name for the qualifier.
     * @see #getName()
     */
    public abstract String getShortName();

    /**
     * Returns when this qualifier was added to Android.
     */
    public abstract int since();

    /**
     * Whether this qualifier is deprecated.
     */
    public boolean deprecated() {
        return false;
    }

    /**
     * Returns whether the qualifier has a valid filter value.
     */
    public abstract boolean isValid();

    /**
     * Returns whether the qualifier has a fake value.
     * <p/>Fake values are used internally and should not be used as real qualifier value.
     */
    public abstract boolean hasFakeValue();

    /**
     * Check if the value is valid for this qualifier, and if so sets the value
     * into a Folder Configuration.
     * @param value The value to check and set. Must not be null.
     * @param config The folder configuration to receive the value. Must not be null.
     * @return true if the value was valid and was set.
     */
    public abstract boolean checkAndSet(String value, FolderConfiguration config);

    /**
     * Returns a string formatted to be used in a folder name.
     * <p/>This is declared as abstract to force children classes to implement it.
     */
    public abstract String getFolderSegment();

    /**
     * Returns whether the given qualifier is a match for the receiver.
     * <p/>The default implementation returns the result of {@link #equals(Object)}.
     * <p/>Children class that re-implements this must implement
     * {@link #isBetterMatchThan(ResourceQualifier, ResourceQualifier)} too.
     * @param qualifier the reference qualifier
     * @return true if the receiver is a match.
     */
    public boolean isMatchFor(ResourceQualifier qualifier) {
        return equals(qualifier);
    }

    /**
     * Returns true if the receiver is a better match for the given <var>reference</var> than
     * the given <var>compareTo</var> comparable.
     * @param compareTo The {@link ResourceQualifier} to compare to. Can be null, in which
     * case the method must return <code>true</code>.
     * @param reference The reference qualifier value for which the match is.
     * @return true if the receiver is a better match.
     */
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        // the default is to always return false. This gives less overhead than always returning
        // true, as it would only compare same values anyway.
        return compareTo == null;
    }

    @Override
    public String toString() {
        return getFolderSegment();
    }

    /**
     * Returns a string formatted for display purpose.
     */
    public abstract String getShortDisplayValue();

    /**
     * Returns a string formatted for display purpose.
     */
    public abstract String getLongDisplayValue();

    /**
     * Returns <code>true</code> if both objects are equal.
     * <p/>This is declared as abstract to force children classes to implement it.
     */
    @Override
    public abstract boolean equals(Object object);

    /**
     * Returns a hash code value for the object.
     * <p/>This is declared as abstract to force children classes to implement it.
     */
    @Override
    public abstract int hashCode();

    @Override
    public final int compareTo(ResourceQualifier o) {
        return toString().compareTo(o.toString());
    }
}
