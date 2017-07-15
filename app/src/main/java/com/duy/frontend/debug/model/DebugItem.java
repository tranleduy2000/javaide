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

package com.duy.frontend.debug.model;

/**
 * Created by Duy on 24-Mar-17.
 */

public class DebugItem {
    public static final int TYPE_VAR = 1;
    public static final int TYPE_MSG = 2;
    private int type = TYPE_VAR;
    private String msg1 = "", msg2 = "";

    public DebugItem(int type, String msg1, String msg2) {
        this.type = type;
        this.msg1 = msg1;
        this.msg2 = msg2;
    }

    public DebugItem(int type, String msg1) {
        this.type = type;
        this.msg1 = msg1;
        this.msg2 = msg2;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public String getMsg2() {
        return msg2;
    }

    public void setMsg2(String msg2) {
        this.msg2 = msg2;
    }

    @Override
    public String toString() {
        return type == TYPE_VAR ? this.msg1 + " = " + msg2 : msg1;
    }
}
