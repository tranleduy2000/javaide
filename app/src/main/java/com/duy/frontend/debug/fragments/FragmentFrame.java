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

package com.duy.frontend.debug.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.duy.pascal.interperter.declaration.lang.value.VariableDeclaration;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.frontend.R;
import com.duy.frontend.debug.CallStack;
import com.duy.frontend.debug.adapter.FrameAdapter;
import com.duy.frontend.debug.adapter.VariableAdapter;
import com.duy.frontend.debug.utils.SpanUtils;
import com.duy.frontend.dialog.DialogManager;
import com.duy.frontend.view.MonospaceRadioButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 08-Jun-17.
 */

public class FragmentFrame extends Fragment implements FrameAdapter.OnFrameListener, VariableAdapter.OnExpandValueListener {

    private static final String TAG = "FragmentFrame";
    private RadioGroup mListFrame;
    private RecyclerView mListVars;
    private VariableAdapter mVariableAdapter;
    @Nullable
    private CallStack mLastStack;
    private Dialog dialog;

    public static FragmentFrame newInstance() {

        Bundle args = new Bundle();

        FragmentFrame fragment = new FragmentFrame();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frame, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListFrame = (RadioGroup) view.findViewById(R.id.group_frame);

        mListVars = (RecyclerView) view.findViewById(R.id.rc_vars);
        mListVars.setHasFixedSize(true);
        mListVars.setLayoutManager(new LinearLayoutManager(getContext()));
        mVariableAdapter = new VariableAdapter(getContext());
        mListVars.setAdapter(mVariableAdapter);
        mListVars.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mVariableAdapter.setOnExpandValueListener(this);

    }

    public void displayFrame(CallStack callStack) {
        ArrayList<VariableContext> stacks = callStack.getStacks();
        mListFrame.removeAllViews();
        for (int i = 0; i < stacks.size(); i++) {
            MonospaceRadioButton radioButton = new MonospaceRadioButton(getContext());
            radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            radioButton.setText(stacks.get(i).toString());
            radioButton.setOnCheckedChangeListener(new OnFrameChangeListener(stacks.get(i)));
            mListFrame.addView(radioButton);
        }
        RadioButton rad = (RadioButton) mListFrame.getChildAt(mListFrame.getChildCount() - 1);
        rad.setChecked(true);
    }

    public void displayVars(CallStack callStack, boolean update) {
        List<VariableDeclaration> vars = callStack.cloneDefineVars();
        List<Boolean> updateList = new ArrayList<>();

        for (int i = 0; i < vars.size(); i++) updateList.add(false);

        if (update) {
            ArrayList<VariableDeclaration> old = mVariableAdapter.getVariableItems();
            for (int i = 0; i < vars.size(); i++) {
                VariableDeclaration var = vars.get(i);
                if (var.getName().equalsIgnoreCase(old.get(i).getName())) {
                    if (var.getInitialValue() == null) {
                        if (old.get(i).getInitialValue() == null) {
                            updateList.set(i, true);
                        } else {
                            updateList.set(i, false);
                        }
                    } else {
                        if (var.getInitialValue().equals(old.get(i).getInitialValue())) {
                            updateList.set(i, false);
                        } else {
                            updateList.set(i, true);
                        }
                    }
                }
            }
        }
        mVariableAdapter.setData(vars, updateList);
    }

    public void update(CallStack callStack) {
        if (!(mLastStack != null && mLastStack.equals(callStack))) {
            displayFrame(callStack);
            this.mLastStack = callStack;
            displayVars(callStack, false);
        } else {
            displayVars(callStack, true);
        }
    }

    @Override
    public void onDestroy() {
        mLastStack = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (dialog != null) dialog.dismiss();
        mVariableAdapter.clearData();
        super.onDestroyView();
    }

    @Override
    public void onSelectFrame(CallStack stack) {
        displayVars(stack, false);
    }

    @Override
    public void onExpand(VariableDeclaration var) {
        SpanUtils spanUtils = new SpanUtils(mVariableAdapter.getCodeTheme());
        spanUtils.setMaxLengthArray(-1);
        AlertDialog msgDialog = DialogManager.Companion.createMsgDialog(getActivity(), var.getName(),
                spanUtils.createVarSpan(var));
        msgDialog.show();
        this.dialog = msgDialog;
    }

    private final class OnFrameChangeListener implements CompoundButton.OnCheckedChangeListener {
        private CallStack callStack;

        public CallStack getCallStack() {
            return callStack;
        }

        public void setCallStack(CallStack callStack) {
            this.callStack = callStack;
        }

        public OnFrameChangeListener(VariableContext callStack) {
            this.callStack = new CallStack(callStack);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                displayVars(callStack, false);
            }
        }
    }
}