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

package com.duy.editor.dialog

import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton

import com.duy.editor.R
import com.duy.editor.editor.completion.Template
import com.duy.editor.file.FileManager

import java.io.File

/**
 * Created by Duy on 10-Apr-17.
 */

class DialogCreateNewFile : AppCompatDialogFragment() {
    private var mEditFileName: EditText? = null
    private var btnOK: Button? = null
    private var btnCancel: Button? = null
    private var listener: OnCreateNewFileListener? = null
    private var checkBoxClass: RadioButton? = null
    private var checkBoxInterface: RadioButton? = null
    private var checkBoxEnum: RadioButton? = null
    private var mFileManager: FileManager? = null

    fun setListener(listener: OnCreateNewFileListener?) {
        this.listener = listener
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            listener = activity as OnCreateNewFileListener
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mFileManager = FileManager(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.dialog_new_file, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEditFileName = view!!.findViewById(R.id.edit_file_name)
        mEditFileName!!.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val file = doCreateFile()
                if (listener != null && file != null) {
                    listener!!.onFileCreated(file)
                    listener!!.onCancel()
                }
                dismiss()
                return@OnKeyListener true
            }
            false
        })

        btnOK = view.findViewById(R.id.btn_ok)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnCancel!!.setOnClickListener {
            if (listener != null) listener!!.onCancel()
            dismiss()
        }
        checkBoxClass = view.findViewById(R.id.rad_class)
        checkBoxInterface = view.findViewById(R.id.rad_interface)
        checkBoxEnum = view.findViewById(R.id.rad_enum)

        btnOK!!.setOnClickListener {
            val file = doCreateFile()
            if (listener != null && file != null) {

                listener!!.onFileCreated(file)
                listener!!.onCancel()
                dismiss()
            }
        }

    }

    private fun doCreateFile(): File? {
        //get string path of in edit text
        var fileName = mEditFileName!!.text.toString()
        if (fileName.isEmpty()) {
            mEditFileName!!.error = getString(R.string.enter_new_file_name)
            return null
        }
        if ((checkBoxInterface!!.isChecked ||
                checkBoxClass!!.isChecked || checkBoxEnum!!.isChecked) && !fileName.contains(".")) {
            fileName += ".java"
        }
        var file = File(FileManager.getApplicationPath() + fileName)
        if (file.exists()) {
            mEditFileName!!.error = getString(R.string.file_exist)
            return null
        }
        //create new file
        val filePath = mFileManager!!.createNewFile(FileManager.getApplicationPath() + fileName)
        file = File(filePath)

        if (checkBoxClass!!.isChecked) {
            FileManager.saveFile(file, Template.createClass("", file.nameWithoutExtension))
        } else if (checkBoxEnum!!.isChecked) {
            FileManager.saveFile(file, Template.createEnum(file.nameWithoutExtension))
        } else if (checkBoxInterface!!.isChecked) {
            FileManager.saveFile(file, Template.createInterface(file.nameWithoutExtension))
        }
        return file
    }

    interface OnCreateNewFileListener {
        fun onFileCreated(file: File)

        fun onCancel()
    }

    companion object {
        val TAG = DialogCreateNewFile::class.java.simpleName

        val instance: DialogCreateNewFile
            get() = DialogCreateNewFile()
    }

}
