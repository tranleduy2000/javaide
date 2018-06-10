package com.duy.ide.javaide.editor.autocomplete.model;

import com.duy.ide.javaide.editor.view.EditorView;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Constructor;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ConstructorDescription extends DescriptionImpl {
    private Constructor constructor;
    private String simpleName;

    public ConstructorDescription(Constructor constructor) {
        this.constructor = constructor;
        this.simpleName = JavaUtil.getSimpleName(constructor.getName());
    }

    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String getName() {
        return constructor.getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getInsertText() {
        if (constructor.getParameterTypes().length > 0) {
            return getSimpleName() + "(" + EditorView.CURSOR + ");";
        } else {
            return getSimpleName() + "();" + EditorView.CURSOR;
        }
    }



    @Override
    public String toString() {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (i == parameterTypes.length - 1) {
                params.append(parameterType.getSimpleName());
                break;
            }
            params.append(parameterType.getSimpleName()).append(",");
        }
        return JavaUtil.getSimpleName(constructor.getName()) + "(" + params.toString() + ")";
    }
}
