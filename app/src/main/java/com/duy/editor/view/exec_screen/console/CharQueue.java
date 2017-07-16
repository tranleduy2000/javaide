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

package com.duy.editor.view.exec_screen.console;

/**
 * Created by Duy on 10-Feb-17.
 */
@SuppressWarnings("DefaultFileTemplate")
public class CharQueue {
    public static final int QUEUE_SIZE = 2 * 1024; //2MB ram
    public char text[];
    public int front;
    public int rear;
    private int size;

    public CharQueue(int size) {
        this.size = size;
        text = new char[size];
        front = 0;
        rear = 0;
    }

    public int getFront() {
        return front;
    }

    public int getRear() {
        return rear;
    }

    public synchronized char pop() {
        while (front == rear) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        char b = text[front];
        front++;
        if (front >= text.length) front = 0;
        return b;
    }

    public synchronized void push(char b) {
        text[rear] = b;
        rear++;
        if (rear >= text.length) rear = 0;
        if (front == rear) {
            front++;
            if (front >= text.length) front = 0;
        }
        notify();
    }

    public synchronized void flush() {
        rear = front;
        notify();
    }

    public synchronized void clear() {
        rear = front;
        notify();
    }

    public boolean keyPressed() {
        return rear - front > 0;
    }
}
