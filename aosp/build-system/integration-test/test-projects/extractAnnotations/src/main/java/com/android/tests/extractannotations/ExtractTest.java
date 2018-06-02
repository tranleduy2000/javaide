package com.android.tests.extractannotations;

import static com.android.tests.extractannotations.Constants.FLAG_VALUE_1;
import static com.android.tests.extractannotations.Constants.FLAG_VALUE_2;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

@SuppressWarnings({"JavaDoc", "UnusedDeclaration", "SpellCheckingInspection"})
public class ExtractTest {
    public ExtractTest(@IdRes int param1, @NonNull @StringRes String param2) {
    }

    // Nullness annotations

    @Nullable
    public Object getNullableReturn() { return null; }

    @NonNull
    protected Object getNonNullableReturn() { return ""; }

    public static void setNullableNonNullable(@Nullable Number param1, @NonNull String param2) {
    }

    // Resource type annotations
    @StringRes @IdRes
    public int resourceTypeMethod(@DrawableRes int arg1, @IdRes @ColorRes int arg2) {
        return 0;
    }

    // Complicated signature: check that annotation signature extracted in XML is correct
    public <T> void resourceTypeMethodWithTypeArgs(@StringRes Map<String,? extends Number> map,
                                                   @DrawableRes T myArg,
                                                   @IdRes int arg2) {
    }

    // Typedefs
    public void checkForeignTypeDef(@TopLevelTypeDef int topLevel) {
    }

    /** @hide */
    @IntDef(flag=true, value={0, FLAG_VALUE_1, FLAG_VALUE_2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Mask {}

    public void testMask(@Mask int mask) {
    }

    @IntDef(flag=false, value={0, Constants.CONSTANT_1, Constants.CONSTANT_3})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface NonMaskType {}

    public void testNonMask(@NonMaskType int mask) {
    }

    @IntDef({VISIBLE, INVISIBLE, GONE, 5, 7 + 10, Constants.CONSTANT_1})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}

    public static final int VISIBLE = 0x00000000;
    public static final int INVISIBLE = 0x00000004;
    public static final int GONE = 0x00000008;

    @Visibility
    public int getVisibility() {
        return VISIBLE;
    }

    /** @hide */
    @StringDef({STRING_1, STRING_2, "literalValue", "conc" + "atenated"})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StringMode {}

    public static final String STRING_1 = "String1";
    public static final String STRING_2 = "String1";

    @StringMode
    public String getStringMode(@Visibility int visibility) {
        return STRING_1;
    }

    // Hidden annotations: not extracted for various reasons

    // This method should not be included: it's private
    @IdRes
    private int getPrivate() { return 0; }

    // This method should not be included: it's package private
    @IdRes
    private Object getPackagePrivate() { return 0; }

    // This method should not be included: method is hidden
    /** @hide */
    @IdRes
    public int getHiddenMethod() { return 0; }

    // This method should not be included: method is hidden
    /** @hide */
    private static class HiddenClass {
        @IdRes
        public int getHiddenMember() { return 0; }
    }
}
