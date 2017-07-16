/***
 Copyright (c) 2013 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.pager;

import android.os.Parcel;

public class SimplePageDescriptor implements PageDescriptor {
    public static final Creator<SimplePageDescriptor> CREATOR =
            new Creator<SimplePageDescriptor>() {
                public SimplePageDescriptor createFromParcel(Parcel in) {
                    return new SimplePageDescriptor(in);
                }

                public SimplePageDescriptor[] newArray(int size) {
                    return new SimplePageDescriptor[size];
                }
            };
    private String tag = null;
    private String title = null;

    public SimplePageDescriptor(String tag, String title) {
        this.tag = tag;
        this.title = title;
    }

    private SimplePageDescriptor(Parcel in) {
        tag = in.readString();
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return (0);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(tag);
        out.writeString(title);
    }

    public String getTitle() {
        return (title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFragmentTag() {
        return (tag);
    }
}