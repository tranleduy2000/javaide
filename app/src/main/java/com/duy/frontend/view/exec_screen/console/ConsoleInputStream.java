/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.view.exec_screen.console;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Duy on 10-Feb-17.
 */
public class ConsoleInputStream extends InputStream {
    public static final int QUEUE_SIZE = 2 * 1024;

    /**
     * this array store text of screen, include unicode character
     * the length of string only 1
     */
    public String text[];

    public int keyEvent[];

    public int front;
    public int rear;
    private int size;

    public ConsoleInputStream(int size) {
        this.size = size;
        text = new String[size];
        keyEvent = new int[size];
        front = 0;
        rear = 0;
    }

    public ConsoleInputStream() {
        this(QUEUE_SIZE);
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    public int getFront() {
        return front;
    }

    public int getRear() {
        return rear;
    }

    public synchronized int getKey() {
        while (front == rear) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        int b = keyEvent[front];
        front++;
        if (front >= size) front = 0;
        return b;
    }

    /**
     * @return the string in the rear of queue
     */
    public synchronized String readBuffer() {
        while (front == rear) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        String b = text[front];
        front++;
        if (front >= text.length) front = 0;
        return b;
    }

    public synchronized void writeBuffer(String b) {
        text[rear] = b;
        rear++;
        if (rear >= text.length) rear = 0;
        if (front == rear) {
            front++;
            if (front >= text.length) front = 0;
        }
        notify();
    }

    public synchronized void erase() {
        if (rear != front) {
            rear--;
            if (rear < 0) rear = 0;
        }
        notify();
    }

    public synchronized void flush() {
        rear = front;
        notify();
    }
}
