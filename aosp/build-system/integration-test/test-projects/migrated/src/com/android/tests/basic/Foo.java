package com.android.tests.basic;

import android.os.Parcel;
import android.os.Parcelable;

public class Foo implements Parcelable {

    private final int foo;

    public static final Parcelable.Creator<Foo> CREATOR = new
            Parcelable.Creator<Foo>() {
                public Foo createFromParcel(Parcel in) {
                    return new Foo(in);
                }

                public Foo[] newArray(int size) {
                    return new Foo[size];
                }
            };


    public Foo(int foo) {
        this.foo = foo;
    }

    private Foo(Parcel in) {
        foo = in.readInt();
    }

    int getFoo() {
        return foo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(foo);
    }
}