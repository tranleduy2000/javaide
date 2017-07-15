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

package com.duy.frontend.debug.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.frontend.R;
import com.duy.frontend.code.CompileManager;
import com.duy.frontend.code.ExceptionManager;
import com.duy.frontend.debug.CallStack;
import com.duy.frontend.debug.fragments.FragmentFrame;
import com.duy.frontend.dialog.DialogManager;
import com.duy.frontend.editor.view.HighlightEditor;
import com.duy.frontend.editor.view.LineUtils;
import com.duy.frontend.runnable.AbstractExecActivity;
import com.duy.frontend.view.LockableScrollView;
import com.duy.frontend.view.exec_screen.console.ConsoleView;
import com.duy.pascal.interperter.ast.instructions.Executable;
import com.duy.pascal.interperter.ast.runtime_value.value.AssignableValue;
import com.duy.pascal.interperter.ast.runtime_value.value.RuntimeValue;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.builtin_libraries.io.IOLib;
import com.duy.pascal.interperter.config.DebugMode;
import com.duy.pascal.interperter.debugable.DebugListener;
import com.duy.pascal.interperter.declaration.lang.function.AbstractCallableFunction;
import com.duy.pascal.interperter.linenumber.LineInfo;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.ViewGroup.LayoutParams;


public class DebugActivity extends AbstractExecActivity implements DebugListener, ProgramHandler {

    private ConsoleView mConsoleView;
    private HighlightEditor mCodeView;
    private Toolbar toolbar;
    private LockableScrollView mScrollView;
    private Handler handler = new Handler();
    private AlertDialog alertDialog;
    private PopupWindow popupWindow;
    private AtomicBoolean endEnded = new AtomicBoolean(false);
    private Vibrator vibrator;

    private Runnable showDialog = new Runnable() {
        @Override
        public void run() {

        }
    };


    private FragmentFrame mFameFragment;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_debug);
        bindView();

        FirebaseAnalytics.getInstance(this).logEvent("open_debug", new Bundle());
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mConsoleView.updateSize();
        mConsoleView.showPrompt();
        mConsoleView.writeString("Enable DEBUG mode\n");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                debugProgram();
            }
        }, 100);
    }

    private void bindView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConsoleView = (ConsoleView) findViewById(R.id.console);
        mCodeView = (HighlightEditor) findViewById(R.id.code_editor);
        mScrollView = (LockableScrollView) findViewById(R.id.vertical_scroll);
        mCodeView.setVerticalScroll(mScrollView);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mFameFragment = (FragmentFrame) getSupportFragmentManager().findFragmentByTag("FragmentFrame");
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onError(Exception e) {
        ExceptionManager exceptionManager = new ExceptionManager(this);
        DialogManager.Companion.createFinishDialog(this, "Runtime error",
                exceptionManager.getMessage(e)).show();
        //DEBUG
        if (DEBUG) e.printStackTrace();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showDialogComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.complete)
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mMessageHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }
                })
                .setNegativeButton(R.string.view_console, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        try {
            builder.create().show();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void showKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mConsoleView.requestFocus();
        imm.showSoftInput(mConsoleView, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void debugProgram() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filePath = extras.getString(CompileManager.FILE_PATH);
            if (filePath == null || filePath.isEmpty()) return;
            File file = new File(filePath);
            if (!file.exists()) {
                finish();
                return;
            }
            String code = mFileManager.fileToString(file);
            mCodeView.setTextHighlighted(code);
            mCodeView.highlightAll();

            setTitle(file.getName());
            endEnded.set(false);
            setEnableDebug(true); //disable DEBUG
            createAndRunProgram(filePath); //execute file
        } else {
            finish();
        }
    }

    @Override
    public void onLine(Executable executable, @Nullable final LineInfo lineInfo) {
        Log.d(TAG, "onLine() called with: runtimeValue = [" + executable + "], lineInfo = [" + lineInfo + "]");
        if (lineInfo == null) {
            return;
        }

        scrollTo(lineInfo);

    }

    private void scrollTo(@NonNull final LineInfo lineInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCodeView.pinLine(lineInfo);
                int yCoordinate = LineUtils.getYAtLine(mScrollView,
                        mCodeView.getLineCount(), lineInfo.getLine());
                int heightVisible = mCodeView.getHeightVisible();
                if (!(mScrollView.getScrollY() < yCoordinate
                        && mScrollView.getScrollY() + heightVisible > yCoordinate)) {
                    mScrollView.smoothScrollTo(0, yCoordinate);
                }
            }
        });
    }

    @Override
    public void onLine(RuntimeValue executable, final LineInfo lineInfo) {
        Log.d(TAG, "onLine() called with: executable = [" + executable.getClass() +
                "], lineInfo = [" + lineInfo + "]");
        if (lineInfo == null) return;
        scrollTo(lineInfo);
    }

    @Override
    public void onEvaluatingExpr(LineInfo lineInfo, String expression) {
        Log.d(TAG, "onEvaluatingExpr() called with: lineInfo = [" + lineInfo + "], " +
                "expression = [" + expression + "]");

    }

    /**
     * This method will be show a small popup window for show result of expression
     *
     * @param lineInfo - the line of expression
     * @param expr     - input
     * @param result   - result value of expr
     */
    @Override
    public void onEvaluatedExpr(final LineInfo lineInfo, final String expr, final String result) {
        Log.d(TAG, "onEvaluatedExpr() called with: lineInfo = [" + lineInfo + "], expr = [" +
                expr + "], result = [" + result + "]");
        showPopupAt(lineInfo, expr + " = " + result);
    }

    @WorkerThread
    private void showPopupAt(final LineInfo lineInfo, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                //get relative position of expression at edittext
                Point position = mCodeView.getDebugPosition(lineInfo.getLine(), lineInfo.getColumn(),
                        Gravity.TOP);
                Log.d(TAG, "generate: " + position);
                dismissPopup();
                //create new popup
                PopupWindow window = new PopupWindow(DebugActivity.this);
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View container = inflater.inflate(R.layout.popup_expr_result, null);
                container.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int windowHeight = container.getMeasuredHeight();
                int windowWidth = container.getMeasuredWidth();

                window.setContentView(container);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setTouchable(true);
                window.setSplitTouchEnabled(true);
                window.setOutsideTouchable(true);

                window.showAtLocation(mCodeView, Gravity.NO_GRAVITY, position.x - windowWidth / 3,
                        position.y + toolbar.getHeight() - windowHeight);
                TextView txtResult = (TextView) container.findViewById(R.id.txt_result);
                txtResult.setText(msg);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
                alphaAnimation.setDuration(1000);
                alphaAnimation.setRepeatMode(Animation.REVERSE);
                alphaAnimation.setRepeatCount(Animation.INFINITE);
                txtResult.startAnimation(alphaAnimation);
                DebugActivity.this.popupWindow = window;
            }
        });
    }

    private void dismissPopup() {
        if (popupWindow != null) {
            if (this.popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    }

    @Override
    public void onAssignValue(LineInfo lineNumber, final AssignableValue left,
                              @NonNull final Object old, final Object value,
                              @NonNull VariableContext context) {
        Log.d(TAG, "onAssignValue() called with: lineNumber = [" + lineNumber + "], left = [" +
                left + "], value = [" + value + "]");
    }

    @Override
    public void onPreFunctionCall(AbstractCallableFunction function, RuntimeValue[] arguments) {
        Log.d(TAG, "onPreFunctionCall() called with: function = [" + function + "], arguments = ["
                + Arrays.toString(arguments) + "]");

    }

    @Override
    public void onFunctionCalled(AbstractCallableFunction function, RuntimeValue[] arguments, Object result) {
        Log.d(TAG, "onFunctionCalled() called with: function = [" + function + "], arguments = ["
                + Arrays.toString(arguments) + "], result = [" + result + "]");

    }

    @Override
    public void onEvalParameterFunction(LineInfo lineInfo, String name, @Nullable Object value) {
        if (value != null) {
            showPopupAt(lineInfo, name + " = " + value.toString());
        }
    }

    @Override
    public void onEndProgram() {
        dismissPopup();
        this.endEnded.set(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCodeView.pinLine(null);
            }
        });
    }

    @Override
    public void showMessage(LineInfo pos, String msg) {
        showPopupAt(pos, msg);
    }

    @Override
    public void onVariableChange(final CallStack currentFrame) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFameFragment.update(currentFrame);
            }
        });
    }

    @Override
    public void onVariableChange(CallStack currentFrame, Pair<String, Object> value) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_step_info) {
            if (program != null) {
                program.setDebugMode(DebugMode.STEP_INFO);
            }
            resumeProgram();
            return true;
        } else if (i == R.id.action_step_over) {
            if (program != null) {
                program.setDebugMode(DebugMode.STEP_OVER);
            }
            resumeProgram();
            return true;
        } else if (i == R.id.action_add_watch) {
            addWatchVariable();
            return true;

        } else if (i == R.id.action_show_soft) {
            showKeyBoard();
            return true;

        } else if (i == R.id.action_rerun) {
            CompileManager.debug(this, filePath);
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void resumeProgram() {
        if (program != null && !endEnded.get()) program.resume();
        else {
            vibrator.vibrate(100);
            Toast.makeText(this, R.string.program_stopped, Toast.LENGTH_SHORT).show();
        }
    }

    private void addWatchVariable() {
    }


    @Override
    protected void onDestroy() {
        if (alertDialog != null) alertDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public ConsoleView getConsoleView() {
        return mConsoleView;
    }

    @Override
    public void onNewMessage(String msg) {
        Log.d(TAG, "onNewMessage() called with: msg = [" + msg + "]");

    }

    @Override
    public void onClearDebug() {
        Log.d(TAG, "onClearDebug() called");

    }

    @Override
    public void onFunctionCall(String name) {
        Log.d(TAG, "onFunctionCall() called with: name = [" + name + "]");

    }

    @Override
    public synchronized void startInput(final IOLib lock) {
        this.mLock = lock;
        showDialogInput();
    }

    private void showDialogInput() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(DebugActivity.this);
                final AppCompatEditText editText = new AppCompatEditText(DebugActivity.this);
                editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                builder.setView(editText);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mLock instanceof IOLib) {
                            ((IOLib) mLock).setInputBuffer(editText.getText().toString());
                        }
                        mConsoleView.writeString(editText.getText().toString());
                        dialog.cancel();
                    }
                });
                builder.setTitle("Read/readln");
                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}
