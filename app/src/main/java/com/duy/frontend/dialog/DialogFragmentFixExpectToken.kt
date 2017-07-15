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

package com.duy.frontend.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RadioButton
import com.duy.pascal.interperter.parse_exception.syntax.ExpectedTokenException
import com.duy.frontend.R

/**
 * Created by Duy on 28-May-17.
 */
open class DialogFragmentFixExpectToken : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(e: ExpectedTokenException): DialogFragmentFixExpectToken {
            val dialog = DialogFragmentFixExpectToken();
            val bundle = Bundle();
            bundle.putString("current", e.current);
            bundle.putInt("lineInfo", e.lineInfo?.line!!)
            bundle.putInt("column", e.lineInfo?.column!!)
            bundle.putStringArray("expect", e.expected);
            dialog.arguments = bundle;
            return dialog
        }
    }

    var listener: OnSelectExpectListener? = null;

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as OnSelectExpectListener;


    }


    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.dialog_fix_expect, null);


        val current = arguments.getString("current")
        val line = arguments.getInt("lineInfo");
        val col = arguments.getInt("column");
        val radInsert: RadioButton = view.findViewById(R.id.radio_insert)

        val stringArray = arguments.getStringArray("expect")
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, stringArray);
        val listView: ListView = view.findViewById(R.id.list_expect)
        if (true) {
            listView.adapter = adapter;
            listView.setOnItemClickListener { _, _, position, _ ->
                val get = stringArray.get(position)
                listener?.onSelectedExpect(current, get, radInsert.isChecked, line, col);
                dismiss();
            }
        }
        dialog?.setContentView(view)
    }

    class CallBack : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }
    }


    interface OnSelectExpectListener {
        fun onSelectedExpect(current: String, expect: String, insert: Boolean, line: Int, column: Int);
    }
}
