/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.editor.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.duy.editor.adapters.SymbolAdapter;
import com.duy.editor.editor.completion.KeyWord;

import java.util.ArrayList;

/**
 * Created by Duy on 11-Feb-17.
 */

public class SymbolListView extends RecyclerView {
    private OnKeyListener mListener;
    private ArrayList<String> mKey = new ArrayList<>();
    private SymbolAdapter mAdapter;

    public SymbolListView(Context context) {
        super(context);
        setup(context);
    }

    public SymbolListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);

    }

    public SymbolListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context);

    }

    public OnKeyListener getListener() {
        return mListener;
    }

    public void setListener(OnKeyListener mListener) {
        this.mListener = mListener;
        mAdapter.setListener(mListener);
    }

    private void setup(Context context) {
        mAdapter = new SymbolAdapter();
        mAdapter.setListKey(KeyWord.SYMBOL_KEY);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(linearLayoutManager);
        setHasFixedSize(false);
        setAdapter(mAdapter);
    }

    public interface OnKeyListener {
        void onKeyClick(View view, String text);

        void onKeyLongClick(String text);
    }
}
