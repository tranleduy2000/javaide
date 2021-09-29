/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.duy.common.io;

import java.io.Serializable;
import java.io.Writer;

/**
 * {@link Writer} implementation that outputs to a {@link StringBuilder}.
 * <p>
 * <strong>NOTE:</strong> This implementation, as an alternative to
 * <code>java.io.StringWriter</code>, provides an <i>un-synchronized</i>
 * (i.e. for use in a single thread) implementation for better performance.
 * For safe usage with multiple {@link Thread}s then
 * <code>java.io.StringWriter</code> should be used.
 *
 * @since 2.0
 */
public class StringBuilderWriter extends Writer implements Serializable {

    private static final long serialVersionUID = -146927496096066153L;
    private final StringBuilder builder;

    /**
     * Constructs a new {@link StringBuilder} instance with default capacity.
     */
    public StringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    /**
     * Constructs a new {@link StringBuilder} instance with the specified capacity.
     *
     * @param capacity The initial capacity of the underlying {@link StringBuilder}
     */
    public StringBuilderWriter(final int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    /**
     * Constructs a new instance with the specified {@link StringBuilder}.
     *
     * <p>If {@code builder} is null a new instance with default capacity will be created.</p>
     *
     * @param builder The String builder. May be null.
     */
    public StringBuilderWriter(final StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    /**
     * Appends a single character to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final char value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a character sequence to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a portion of a character sequence to the {@link StringBuilder}.
     *
     * @param value The character to append
     * @param start The index of the first character
     * @param end The index of the last character + 1
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value, final int start, final int end) {
        builder.append(value, start, end);
        return this;
    }

    /**
     * Closing this writer has no effect.
     */
    @Override
    public void close() {
        // no-op
    }

    /**
     * Flushing this writer has no effect.
     */
    @Override
    public void flush() {
        // no-op
    }


    /**
     * Writes a String to the {@link StringBuilder}.
     *
     * @param value The value to write
     */
    @Override
    public void write(final String value) {
        if (value != null) {
            builder.append(value);
        }
    }

    /**
     * Writes a portion of a character array to the {@link StringBuilder}.
     *
     * @param value The value to write
     * @param offset The index of the first character
     * @param length The number of characters to write
     */
    @Override
    public void write(final char[] value, final int offset, final int length) {
        if (value != null) {
            builder.append(value, offset, length);
        }
    }

    /**
     * Returns the underlying builder.
     *
     * @return The underlying builder
     */
    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns {@link StringBuilder#toString()}.
     *
     * @return The contents of the String builder.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
