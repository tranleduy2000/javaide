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

package com.duy.frontend.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.duy.frontend.R;

/**
 * Created by Duy on 08-Apr-17.
 */

public class DialogFragmentErrorMsg extends AppCompatDialogFragment {
    public static final String TAG = DialogFragmentErrorMsg.class.getSimpleName();

    public static DialogFragmentErrorMsg newInstance(CharSequence lineError, CharSequence msg) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence("lineInfo", lineError);
        bundle.putCharSequence("msg", msg);
        DialogFragmentErrorMsg dialogFragmentErrorMsg = new DialogFragmentErrorMsg();
        dialogFragmentErrorMsg.setArguments(bundle);
        return dialogFragmentErrorMsg;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_show_error, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView txtLine = (TextView) view.findViewById(R.id.txt_message);
        txtLine.setText(getArguments().getCharSequence("lineInfo"));

        TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.compile_error));
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

}
