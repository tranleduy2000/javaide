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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.duy.pascal.interperter.parse_exception.ParsingException
import com.duy.pascal.interperter.parse_exception.define.UnknownIdentifierException
import com.duy.frontend.R
import com.duy.frontend.code.ExceptionManager
import com.duy.frontend.editor.EditorActivity
import com.duy.frontend.editor.autofix.DefineType
import com.duy.frontend.utils.DonateUtils

/**
 * Created by Duy on 29-Mar-17.
 */

class DialogManager {

    fun onDestroy() {

    }

    companion object {
        fun createFinishDialog(activity: Activity,
                               title: CharSequence, msg: CharSequence): android.support.v7.app.AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            return builder.create()

        }

        fun createMsgDialog(activity: Activity,
                            title: CharSequence, msg: CharSequence): android.support.v7.app.AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(activity.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            return builder.create()
        }

        /**
         * create and set event for error dialog
         */
        fun createErrorDialog(activity: EditorActivity,
                              e: Exception): android.support.v7.app.AlertDialog {
            val exceptionManager = ExceptionManager(activity)
            val title = activity.getString(R.string.compile_error)
            val msg = exceptionManager.getMessage(e)

            //create builder
            val builder = AlertDialog.Builder(activity)
            builder.setView(R.layout.dialog_show_error)

            //show dialog
            val dialog = builder.create()
            dialog.show()

            //set title and message for dialog
            val txtTitle = dialog.findViewById(R.id.txt_title) as TextView?
            txtTitle?.text = title
            val txtMsg = dialog.findViewById(R.id.txt_message) as TextView?
            txtMsg?.text = msg

            //set event for button
            dialog.findViewById(R.id.btn_cancel)?.setOnClickListener { dialog.cancel() }

            if (DonateUtils.DONATED) {
                if (e is ParsingException) {
                    if (e.isAutoFix) {
                        var container: RadioGroup? = null
                        if (e is UnknownIdentifierException) {
                            container = dialog.findViewById(R.id.container_define)!! as RadioGroup
                            container.visibility = View.VISIBLE
                        }
                        //set event for button Auto fix
                        dialog.findViewById(R.id.btn_auto_fix)?.visibility = View.VISIBLE
                        dialog.findViewById(R.id.btn_auto_fix)?.setOnClickListener {
                            if (e is UnknownIdentifierException) {
                                val checkedRadioButtonId = container?.checkedRadioButtonId
                                when (checkedRadioButtonId) {
                                    R.id.rad_var -> e.fitType = DefineType.DECLARE_VAR
                                    R.id.rad_fun -> e.fitType = DefineType.DECLARE_FUNCTION
                                    R.id.rad_const -> e.fitType = DefineType.DECLARE_CONST
                                }
                            }
                            activity.autoFix(e)
                            dialog.cancel()
                        }
                    }
                }
            }
            return dialog

        }

        fun createFinishDialog(activity: Activity,
                               title: CharSequence, msg: CharSequence,
                               resourceIcon: Int): android.support.v7.app.AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setIcon(resourceIcon)
            builder.setPositiveButton(activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            return builder.create()

        }

        fun createDialogReportBug(activity: Activity, code: String): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.report_bug).setView(R.layout.report_bug_dialog).setIcon(R.drawable.ic_bug_report_white_24dp)
            val alertDialog = builder.create()
            alertDialog.show()
            val editTitle = alertDialog.findViewById(R.id.edit_title) as EditText?
            val editContent = alertDialog.findViewById(R.id.edit_content) as EditText?
            val btnSend = alertDialog.findViewById(R.id.btn_email) as Button?
            val editExpect = alertDialog.findViewById(R.id.edit_expect) as EditText?
            assert(btnSend != null)
            btnSend!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    sendBug()
                    alertDialog.cancel()
                }

                private fun sendBug() {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_EMAIL, arrayOf("tranleduy1233@gmail.com"))
                    i.putExtra(Intent.EXTRA_SUBJECT, "Report bug for PASCAL NIDE: " + editTitle!!.text.toString())

                    val content = "Cause: \n" + editContent!!.text.toString() + "\n" +
                            "Expect:  " + editExpect!!.text.toString() + "\n" + "Code:\n" + code

                    i.putExtra(Intent.EXTRA_TEXT, content)

                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.send_mail)))
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(activity, R.string.no_mail_clients, Toast.LENGTH_SHORT).show()
                    }

                }
            })
            return alertDialog
        }
    }
}
