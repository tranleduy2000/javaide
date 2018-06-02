package com.android.tests.basic;

public class IndirectlyReferencedClass {
    @Override
    public String toString() {
        return getClass().getCanonicalName();
    }
}
