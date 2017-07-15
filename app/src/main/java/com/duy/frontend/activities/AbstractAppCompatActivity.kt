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

package com.duy.frontend.activities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.duy.frontend.DLog
import com.duy.frontend.R
import com.duy.frontend.setting.PascalPreferences
import java.util.*

abstract class AbstractAppCompatActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    protected var preferences: PascalPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PascalPreferences(this)
        setFullScreen()
        setLocale(false)
        setTheme(false)
    }


    /**
     * set language
     */
    private fun setLocale(create: Boolean) {
        val locale: Locale
        val code = preferences!!.sharedPreferences.getString(getString(R.string.key_pref_lang), "default_lang")
        if (code == "default_lang") {
            if (DEBUG) DLog.d(TAG, "setLocale: default")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = Resources.getSystem().configuration.locales.get(0)
            } else {
                locale = Resources.getSystem().configuration.locale
            }
        } else {
            if (code == "zh_CN") {
                locale = Locale.SIMPLIFIED_CHINESE
            } else {
                locale = Locale(code)
            }
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        val resources = resources
        resources.updateConfiguration(config, resources.displayMetrics)
        if (create) recreate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    override fun onStart() {
        super.onStart()
        if (preferences != null)
            preferences!!.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()

    }

    /**
     * set theme for app

     * @param recreate -call method onCreate
     */
    protected fun setTheme(recreate: Boolean) {}

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (DEBUG) DLog.d(TAG, "onSharedPreferenceChanged: " + s)
        if (s == getString(R.string.key_pref_lang)) {
            setLocale(true)
            //            Toast.makeText(this, readBuffer(R.string.change_lang_msg), Toast.LENGTH_SHORT).show();
        } else if (s.equals(getString(R.string.key_full_screen), ignoreCase = true)) {
            setFullScreen()
        }
    }

    fun setFullScreen() {
        if (preferences!!.useFullScreen()) {
            hideStatusBar()
        } else {
            showStatusBar()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        DLog.d(TAG, "onDestroy: ")
        if (preferences != null)
            preferences!!.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


    /**
     * show dialog with title and messenger

     * @param title - title
     * *
     * @param msg   - messenger
     */
    protected fun showDialog(title: String, msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(msg)
        builder.setNegativeButton(this.getString(R.string.close)) { dialogInterface, _ -> dialogInterface.cancel() }
        builder.create().show()
    }

    /**
     * show dialog with title and messenger

     * @param msg - messenger
     */
    protected fun showDialog(msg: String) {
        this.showDialog("", msg)
    }

    protected fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun showStatusBar() {
        if (Build.VERSION.SDK_INT < 30) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun hideStatusBar() {
        // TODO: 30-Mar-17
        if (android.os.Build.VERSION.SDK_INT < 30) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

    /**
     * set support action bar for activity
     */
    protected open fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        val TAG = AbstractAppCompatActivity::class.java.simpleName
        private val DEBUG = DLog.DEBUG
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true;
        }
        return super.onOptionsItemSelected(item)
    }
}
