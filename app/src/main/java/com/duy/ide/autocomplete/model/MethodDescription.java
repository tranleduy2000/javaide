package com.duy.ide.autocomplete.model;

import android.support.annotation.NonNull;

import com.duy.ide.autocomplete.util.JavaUtil;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription implements Member, Description {

    private Method method;
    private String simpleName;

    public MethodDescription(@NonNull Method method) {
        this.method = method;
        method.getClass(); //check null
        this.simpleName = JavaUtil.getSimpleName(method.getName());
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public long getLastUsed() {
        return 0;
    }

    @Override
    public void setLastUsed(long time) {

    }

    @Override
    public Class getType() {
        return method.getReturnType();
    }

    @Override
    public String getSnippet() {
        return method.getName() + "()";
    }

    @Override
    public String getPrototype() {
        return method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
    }

    @Override
    public String toString() {
        Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (i == parameterTypes.length - 1) {
                params.append(parameterType.getSimpleName());
                break;
            }
            params.append(parameterType.getSimpleName()).append(",");
        }
        return method.getName() + "(" + params.toString() + ")";
    }

    @Override
    public Class getReturnType() {
        return method.getReturnType();
    }

    public String getSimpleName() {
        return simpleName;
    }
}
