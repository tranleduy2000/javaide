package com.duy.common.interfaces;

import com.android.annotations.Nullable;

public interface Action<T> {
    void execute(@Nullable T t);
}
