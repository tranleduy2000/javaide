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

package com.duy.ide.javaide.utils;

/**
 * A multi-thread-safe produce-consumer byte array.
 * Only allows one producer and one consumer.
 */

public class ByteQueue {
    private byte[] mBuffer;
    private int mHead;
    private int mStoredBytes;

    public ByteQueue(int size) {
        mBuffer = new byte[size];
    }

    public int getBytesAvailable() {
        synchronized (this) {
            return mStoredBytes;
        }
    }

    public  int read(byte[] buffer, int offset, int length)
            throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                    new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
                    new IllegalArgumentException("length < 0");

        }
        if (length == 0) {
            return 0;
        }
        synchronized (this) {
            while (mStoredBytes == 0) {
                wait();
            }
            int totalRead = 0;
            int bufferLength = mBuffer.length;
            boolean wasFull = bufferLength == mStoredBytes;
            while (length > 0 && mStoredBytes > 0) {
                int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
                int bytesToCopy = Math.min(length, oneRun);
                System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
                mHead += bytesToCopy;
                if (mHead >= bufferLength) {
                    mHead = 0;
                }
                mStoredBytes -= bytesToCopy;
                length -= bytesToCopy;
                offset += bytesToCopy;
                totalRead += bytesToCopy;
            }
            if (wasFull) {
                notify();
            }
            return totalRead;
        }
    }

    public  void write(byte[] buffer, int offset, int length) throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                    new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
                    new IllegalArgumentException("length < 0");

        }
        if (length == 0) {
            return;
        }
        synchronized (this) {
            int bufferLength = mBuffer.length;
            boolean wasEmpty = mStoredBytes == 0;
            while (length > 0) {
                while (bufferLength == mStoredBytes) {
                    wait();
                }
                int tail = mHead + mStoredBytes;
                int oneRun;
                if (tail >= bufferLength) {
                    tail = tail - bufferLength;
                    oneRun = mHead - tail;
                } else {
                    oneRun = bufferLength - tail;
                }
                int bytesToCopy = Math.min(oneRun, length);
                System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
                offset += bytesToCopy;
                mStoredBytes += bytesToCopy;
                length -= bytesToCopy;
            }
            if (wasEmpty) {
                notify();
            }
        }
    }
}
