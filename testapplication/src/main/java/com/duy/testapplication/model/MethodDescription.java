package com.duy.testapplication.model;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription implements Member, Description {

    private Method method;

    public MethodDescription(Method method) {
        this.method = method;
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
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public String getPrototype() {
        return method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
    }

    @Override
    public Class getReturnType() {
        return null;
    }
}
