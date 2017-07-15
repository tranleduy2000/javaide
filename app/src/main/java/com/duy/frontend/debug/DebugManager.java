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

package com.duy.frontend.debug;

import com.duy.pascal.interperter.declaration.lang.function.AbstractCallableFunction;
import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.ast.runtime_value.value.AssignableValue;
import com.duy.pascal.interperter.ast.runtime_value.value.RuntimeValue;
import com.duy.pascal.interperter.debugable.DebugListener;
import com.duy.pascal.interperter.linenumber.LineInfo;

import java.lang.reflect.Method;

/**
 * Created by Duy on 24-Mar-17.
 */

public class DebugManager {

    public static void outputMethod(DebugListener debugListener, Method method) {
        if (debugListener != null) {
            debugListener.onFunctionCall(method.getName());
        }
    }

    public static void outputConditionWhile(DebugListener debugListener, boolean b) {
        if (debugListener != null) {
            debugListener.onNewMessage("Kiem tra dieu kien vong while la " + b);
        }
    }

    public static void outputConditionFor(DebugListener debugListener, boolean b) {
        if (debugListener != null) {
            debugListener.onNewMessage("Kiem tra dieu kien vong for la " + b);
        }
    }

    public static void debugAssign(LineInfo lineNumber, AssignableValue left, Object old,
                                   Object value, VariableContext context, RuntimeExecutableCodeUnit<?> main) {
        if (main.isDebug()) {
            main.getDebugListener().onAssignValue(lineNumber, left, old, value, context);
        }
    }

    public static void debugWhileStatement() {

    }

    public static void debugForStatement() {

    }

    public static void debugIfStatement() {

    }

    public static void onPreFunctionCall(AbstractCallableFunction function, RuntimeValue[] arguments,
                                         RuntimeExecutableCodeUnit<?> main) {
        if (main.isDebug()) {
            main.getDebugListener().onPreFunctionCall(function, arguments);
        }
    }

    public static void onFunctionCalled(AbstractCallableFunction function, RuntimeValue[] arguments,
                                        Object result, RuntimeExecutableCodeUnit<?> main) {
        if (main.isDebug()) {
            main.getDebugListener().onFunctionCalled(function, arguments, result);
        }
    }

    public static void onEvalParameterFunction(LineInfo lineInfo, String argName, Object value,
                                               RuntimeExecutableCodeUnit main) {
        if (main.isDebug()) {
            main.getDebugListener().onEvalParameterFunction(lineInfo, argName, value);
        }
    }

    public static void showMessage(LineInfo pos,String msg, RuntimeExecutableCodeUnit main) {
        if (main.isDebug()) {
            main.getDebugListener().showMessage(pos, msg);
        }
    }
}
