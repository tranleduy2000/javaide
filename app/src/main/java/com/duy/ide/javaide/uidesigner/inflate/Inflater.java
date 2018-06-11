/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.uidesigner.inflate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.duy.ide.javaide.uidesigner.dynamiclayoutinflator.DynamicLayoutInflator;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Duy on 13-Aug-17.
 */

public class Inflater {
    private final Object delegate;
    private ViewGroup container;
    private TextView txtError;
    private LayoutInflater layoutInflater;
    private Context context;

    public Inflater(Context context, Object delegate, ViewGroup container, TextView txtError) {
        this.context = context;
        this.delegate = delegate;
        this.container = container;
        this.layoutInflater = LayoutInflater.from(context);
        this.txtError = txtError;
    }

    public void inflate(File file) {
//        try {
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            factory.setNamespaceAware(true);
//            XmlPullParser xpp = null;
//            xpp = factory.newPullParser();
//            xpp.setInput(new FileReader(file));
//            View inflate = layoutInflater.inflate(xpp, container);
//            container.removeAllViews();
//            container.addView(inflate, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT));
//        } catch (Exception e) {
//            e.printStackTrace();
//            txtError.setText(e.getMessage());
//            txtError.setVisibility(View.VISIBLE);
//        }
        try {
            View view = DynamicLayoutInflator.inflate(context, new FileInputStream(file), container);
            DynamicLayoutInflator.setDelegate(view, delegate);
            container.removeAllViews();
            container.addView(view, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        } catch (Exception e) {
            e.printStackTrace();
            txtError.setText(e.getMessage());
            txtError.setVisibility(View.VISIBLE);
        }
    }
}
