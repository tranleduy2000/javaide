///*
// * Copyright (C) 2015 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.android.ide.common.vectordrawable;
//
//import android.graphics.drawable.Icon;
//
//import java.net.URL;
//
///**
// * VdIcon wrap every vector drawable from Material Library into an icon.
// * All of them are shown in a table for developer to pick.
// */
//public class VdIcon implements Icon, Comparable<VdIcon> {
//    private final String mName;
//    private final URL mUrl;
//    private VdTree mVdTree;
//
//    public VdIcon(URL url) {
//        setDynamicIcon(url);
//        mUrl = url;
//        String fileName = url.getFile();
//        mName = fileName.substring(fileName.lastIndexOf("/") + 1);
//    }
//
//    public String getName() {
//        return mName;
//    }
//
//    public URL getURL() {
//        return mUrl;
//    }
//
//    public void setDynamicIcon(URL url) {
//        final VdParser p = new VdParser();
//        try {
//            mVdTree = p.parse(url.openStream(), null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public int getIconWidth() {
//        return (int) (mVdTree != null ? mVdTree.mPortWidth : 0);
//    }
//
//    @Override
//    public int getIconHeight() {
//        return (int) (mVdTree != null ? mVdTree.mPortHeight : 0);
//    }
//
//    @Override
//    public int compareTo(VdIcon other) {
//        return mName.compareTo(other.mName);
//    }
//}