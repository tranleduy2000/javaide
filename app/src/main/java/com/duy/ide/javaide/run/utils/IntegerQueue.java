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

package com.duy.ide.javaide.run.utils;

import android.util.Log;

/**
 * Created by Duy on 10-Feb-17.
 */
public class IntegerQueue {
    public static final int QUEUE_SIZE = 2 * 1024; //2MB ram
    private static final String TAG = "ByteQueue";
    public int text[];
    public int front;
    public int rear;
    private int size;

    public IntegerQueue(int size) {
        this.size = size;
        text = new int[size];
        front = 0;
        rear = 0;
    }

    public int getFront() {
        return front;
    }

    public int getRear() {
        return rear;
    }

    public synchronized int read() {
        Log.d(TAG, "read() called");

        while (front == rear) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        int b = text[front];
        front++;
        if (front >= text.length) front = 0;
        return b;
    }

    public synchronized void write(int b) {
        text[rear] = b;
        rear++;
        if (rear >= text.length) rear = 0;
        if (front == rear) {
            front++;
            if (front >= text.length) front = 0;
        }
        notify();
    }

    public synchronized void write(int[] data) {
        for (int i : data) {
            write(i);
        }
    }

    public synchronized void flush() {
        rear = front;
        notify();
    }

    public synchronized void clear() {
        rear = front;
        notify();
    }

}
