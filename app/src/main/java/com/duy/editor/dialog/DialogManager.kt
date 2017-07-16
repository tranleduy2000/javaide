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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.duy.editor.R

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
            btnSend!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    sendBug()
                    alertDialog.cancel()
                }

                private fun sendBug() {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_EMAIL, arrayOf("tranleduy1233@gmail.com"))
                    i.putExtra(Intent.EXTRA_SUBJECT, "Report bug for Java NIDE: " + editTitle!!.text.toString())

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
