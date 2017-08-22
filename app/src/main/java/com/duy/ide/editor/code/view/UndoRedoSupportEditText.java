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

package com.duy.ide.editor.code.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.duy.ide.DLog;
import com.duy.ide.EditorControl;
import com.duy.ide.keyboard.KeyListener;
import com.duy.ide.keyboard.KeySettings;
import com.duy.ide.utils.clipboard.ClipboardManagerCompat;
import com.duy.ide.utils.clipboard.ClipboardManagerCompatFactory;
import com.google.firebase.crash.FirebaseCrash;

import java.util.LinkedList;

//import android.util.Log;

/**
 * EditText with undo and redo support
 * <p>
 * Created by Duy on 15-Mar-17.
 */

public class UndoRedoSupportEditText extends HighlightEditor {
    private static final String PREF_HISTORY_EDIT = "pref_history_edit";

    private UndoRedoHelper mUndoRedoHelper;
    private KeySettings mSettings;
    private KeyListener mKeyListener;
    private ClipboardManagerCompat mClipboardManager;
    private EditorControl editorControl;

    public UndoRedoSupportEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UndoRedoSupportEditText(Context context) {
        super(context);
        init();
    }

    public UndoRedoSupportEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mUndoRedoHelper = new UndoRedoHelper(this);
        mUndoRedoHelper.setMaxHistorySize(mEditorSetting.getMaxHistoryEdit());

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSettings = new KeySettings(mPrefs, getContext());
        mKeyListener = new KeyListener();
        mClipboardManager = ClipboardManagerCompatFactory.newInstance(getContext());
    }


    /**
     * undo text
     */
    public void undo() {
        if (canUndo()) {
            try {
                mUndoRedoHelper.undo();
            } catch (Exception e) {
                // TODO: 19-May-17 fix bug index out of bound
            }
        }
    }

    /**
     * redo text
     */
    public void redo() {
        if (canRedo()) {
            try {
                mUndoRedoHelper.redo();
            } catch (Exception e) {
                // TODO: 19-May-17 fix bug index out of bound
            }
        }
    }

    /**
     * @return <code>true</code> if stack not empty
     */
    public boolean canUndo() {
        return mUndoRedoHelper.getCanUndo();
    }

    /**
     * @return <code>true</code> if stack not empty
     */
    public boolean canRedo() {
        return mUndoRedoHelper.getCanRedo();
    }

    /**
     * clear history
     */
    public void clearHistory() {
        mUndoRedoHelper.clearHistory();
    }

    public void saveHistory(@NonNull String key) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(PREF_HISTORY_EDIT, Context.MODE_PRIVATE).edit();
        mUndoRedoHelper.storePersistentState(editor, key);
        editor.apply();
    }

    public void restoreHistory(String key) {
        try {
            SharedPreferences pref = getContext().getSharedPreferences(PREF_HISTORY_EDIT, Context.MODE_PRIVATE);
            mUndoRedoHelper.restorePersistentState(pref, key);
        } catch (Exception ignored) {
        }
    }

    /**
     * @param keyCode - key code event
     * @param down    - is down
     * @return - <code>true</code> if is ctrl key
     */
    private boolean handleControlKey(int keyCode, KeyEvent event, boolean down) {
        if (keyCode == mSettings.getControlKeyCode()
                || event.isCtrlPressed()) {
//            Log.w(TAG, "handler control key: ");
            mKeyListener.handleControlKey(down);
            return true;
        }
        return false;
    }

    /**
     * CTRL + C copy
     * CTRL + V paste
     * CTRL + B: compile
     * CTRL + R generate
     * CTRL + X cut
     * CTRL + Z undo
     * CTRL + Y redo
     * CTRL + Q quit
     * CTRL + S save
     * CTRL + O open
     * CTRL + F find
     * CTRL + H find and replace
     * CTRL + L format code
     * CTRL + G: goto line
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DLog.DEBUG) Log.w(TAG, "onKeyDown: " + keyCode + " " + event);
        if (handleControlKey(keyCode, event, false)) {
            return true;
        }
        if (event.isCtrlPressed() || mKeyListener.mControlKey.isActive()) {
//            Log.i(TAG, "onKeyDown: process");
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    selectAll();
                    return true;
                case KeyEvent.KEYCODE_X:
                    cut();
                    return true;
                case KeyEvent.KEYCODE_C:
                    copy();
                    return true;
                case KeyEvent.KEYCODE_V:
                    paste();
                    return true;
                case KeyEvent.KEYCODE_G: //go to line
                    if (editorControl != null)
                        editorControl.goToLine();
                    return true;
                case KeyEvent.KEYCODE_L: //format
                    if (editorControl != null)
                        editorControl.formatCode();
                    return true;
                case KeyEvent.KEYCODE_Z:
                    if (canUndo()) {
                        undo();
                    }
                    return true;
                case KeyEvent.KEYCODE_Y:
                    if (canRedo()) {
                        redo();
                    }
                    return true;
                case KeyEvent.KEYCODE_S:
                    if (editorControl != null)
                        editorControl.saveCurrentFile();
                    return true;
                case KeyEvent.KEYCODE_N:
                    if (editorControl != null)
                        editorControl.saveAs();
                    return true;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_TAB:
                    String textToInsert = mSettings.getTabStr();
                    int start, end;
                    start = Math.max(getSelectionStart(), 0);
                    end = Math.max(getSelectionEnd(), 0);
                    getText().replace(Math.min(start, end), Math.max(start, end),
                            textToInsert, 0, textToInsert.length());
                    return true;
                default:
                    try {
                        return super.onKeyDown(keyCode, event);
                    } catch (Exception e) {
                    }
                    return false;

            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (DLog.DEBUG) {
            Log.w(TAG, "onKeyUp " + event);
        }
        if (handleControlKey(keyCode, event, false)) {
            return true;
        }
        if (event.isCtrlPressed() || mKeyListener.mControlKey.isActive()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                case KeyEvent.KEYCODE_X:
                case KeyEvent.KEYCODE_C:
                case KeyEvent.KEYCODE_V:
                case KeyEvent.KEYCODE_Z:
                case KeyEvent.KEYCODE_Y:
                case KeyEvent.KEYCODE_S:
                case KeyEvent.KEYCODE_R:
                case KeyEvent.KEYCODE_F:
                case KeyEvent.KEYCODE_L:
                    return true;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_TAB:
                    return true;
            }
        }
        return super.onKeyUp(keyCode, event);

    }

    public void cut() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        selectionStart = Math.max(0, selectionStart);
        selectionEnd = Math.max(0, selectionEnd);
        selectionStart = Math.min(selectionStart, getText().length() - 1);
        selectionEnd = Math.min(selectionEnd, getText().length() - 1);
        try {
            mClipboardManager.setText(getText().subSequence(selectionStart, selectionEnd));
            getEditableText().delete(selectionStart, selectionEnd);
        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
    }

    public void paste() {
        if (mClipboardManager.hasText()) {
            insert(mClipboardManager.getText().toString());
        }
    }

    /**
     * insert text
     */
    public void insert(CharSequence delta) {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        selectionStart = Math.max(0, selectionStart);
        selectionEnd = Math.max(0, selectionEnd);
        selectionStart = Math.min(selectionStart, selectionEnd);
        selectionEnd = Math.max(selectionStart, selectionEnd);
        try {
            getText().delete(selectionStart, selectionEnd);
            getText().insert(selectionStart, delta);
        } catch (Exception ignored) {
        }
    }

    public void copy() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        selectionStart = Math.max(0, selectionStart);
        selectionEnd = Math.max(0, selectionEnd);
        selectionStart = Math.min(selectionStart, getText().length() - 1);
        selectionEnd = Math.min(selectionEnd, getText().length() - 1);
        try {
            mClipboardManager.setText(getText().subSequence(selectionStart, selectionEnd));
        } catch (Exception ignored) {
            FirebaseCrash.report(ignored);
        }
    }

    public void setEditorControl(EditorControl editorControl) {
        this.editorControl = editorControl;
    }


    public void copyAll() {
        mClipboardManager.setText(getText());
    }

    public static class UndoRedoHelper {
        private static final String TAG = UndoRedoHelper.class.getCanonicalName();
        private boolean mIsUndoOrRedo = false;

        private EditHistory mEditHistory;

        private EditTextChangeListener mChangeListener;
        private TextView mTextView;

        public UndoRedoHelper(TextView textView) {
            mTextView = textView;
            mEditHistory = new EditHistory();
            mChangeListener = new EditTextChangeListener();
            mTextView.addTextChangedListener(mChangeListener);
        }

        public void disconnect() {
            if (mTextView != null) {
                mTextView.removeTextChangedListener(mChangeListener);
            }
        }

        public void setMaxHistorySize(int maxHistorySize) {
            mEditHistory.setMaxHistorySize(maxHistorySize);
        }

        public void clearHistory() {
            mEditHistory.clear();
        }

        public boolean getCanUndo() {
            return (mEditHistory.mmPosition > 0);
        }

        public void undo() {
            EditItem edit = mEditHistory.getPrevious();
            if (edit == null) {
                return;
            }

            Editable editable = mTextView.getEditableText();
            int start = edit.start;
            int end = start + (edit.after != null ? edit.after.length() : 0);

            mIsUndoOrRedo = true;
            editable.replace(start, end, edit.before);
            mIsUndoOrRedo = false;

            for (Object o : editable.getSpans(0, editable.length(), UnderlineSpan.class)) {
                editable.removeSpan(o);
            }

            Selection.setSelection(editable, edit.before == null ? start : (start + edit.before.length()));
        }

        public boolean getCanRedo() {
            return (mEditHistory.mmPosition < mEditHistory.mmHistory.size());
        }

        public void redo() {
            EditItem edit = mEditHistory.getNext();
            if (edit == null) {
                return;
            }

            Editable text = mTextView.getEditableText();
            int start = edit.start;
            int end = start + (edit.before != null ? edit.before.length() : 0);

            mIsUndoOrRedo = true;
            text.replace(start, end, edit.after);
            mIsUndoOrRedo = false;

            // This will get rid of underlines inserted when editor tries to come
            // up with a suggestion.
            for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
                text.removeSpan(o);
            }

            Selection.setSelection(text, edit.after == null ? start
                    : (start + edit.after.length()));
        }

        public void storePersistentState(SharedPreferences.Editor editor, String prefix) {
            // Store hash code of text in the editor so that we can check if the
            // editor contents has changed.
            editor.putString(prefix + ".hash", String.valueOf(prefix.hashCode()));
            editor.putInt(prefix + ".maxSize", mEditHistory.mmMaxHistorySize);
            editor.putInt(prefix + ".position", mEditHistory.mmPosition);
            editor.putInt(prefix + ".size", mEditHistory.mmHistory.size());

            int i = 0;
            for (EditItem ei : mEditHistory.mmHistory) {
                String pre = prefix + "." + i;

                editor.putInt(pre + ".start", ei.start);
                editor.putString(pre + ".before", ei.before.toString());
                editor.putString(pre + ".after", ei.after.toString());

                i++;
            }
        }

        public boolean restorePersistentState(SharedPreferences sp, String prefix)
                throws IllegalStateException {
            boolean ok = doRestorePersistentState(sp, prefix);
            if (!ok) {
                mEditHistory.clear();
            }
            return ok;
        }

        private boolean doRestorePersistentState(SharedPreferences sp, String prefix) {
            String hash = sp.getString(prefix + ".hash", null);
            if (hash == null) {
                // No state to be restored.
                return true;
            }

            if (Integer.valueOf(hash) != prefix.hashCode()) {
                return false;
            }

            mEditHistory.clear();
            mEditHistory.mmMaxHistorySize = sp.getInt(prefix + ".maxSize", -1);

            int count = sp.getInt(prefix + ".size", -1);
            if (count == -1) {
                return false;
            }

            for (int i = 0; i < count; i++) {
                String pre = prefix + "." + i;

                int start = sp.getInt(pre + ".start", -1);
                String before = sp.getString(pre + ".before", null);
                String after = sp.getString(pre + ".after", null);

                if (start == -1 || before == null || after == null) {
                    return false;
                }
                mEditHistory.add(new EditItem(start, before, after));
            }

            mEditHistory.mmPosition = sp.getInt(prefix + ".position", -1);
            return mEditHistory.mmPosition != -1;

        }

        // =================================================================== //

        enum ActionType {
            INSERT, DELETE, PASTE, NOT_DEF
        }

        private final class EditHistory {
            private final LinkedList<EditItem> mmHistory = new LinkedList<>();
            private int mmPosition = 0;
            private int mmMaxHistorySize = -1;

            private void clear() {
                mmPosition = 0;
                mmHistory.clear();
            }

            private void add(EditItem item) {
                while (mmHistory.size() > mmPosition) {
                    mmHistory.removeLast();
                }
                mmHistory.add(item);
                mmPosition++;

                if (mmMaxHistorySize >= 0) {
                    trimHistory();
                }
            }

            private void setMaxHistorySize(int maxHistorySize) {
                mmMaxHistorySize = maxHistorySize;
                if (mmMaxHistorySize >= 0) {
                    trimHistory();
                }
            }

            private void trimHistory() {
                while (mmHistory.size() > mmMaxHistorySize) {
                    mmHistory.removeFirst();
                    mmPosition--;
                }

                if (mmPosition < 0) {
                    mmPosition = 0;
                }
            }

            private EditItem getCurrent() {
                if (mmPosition == 0) {
                    return null;
                }
                return mmHistory.get(mmPosition - 1);
            }

            private EditItem getPrevious() {
                if (mmPosition == 0) {
                    return null;
                }
                mmPosition--;
                return mmHistory.get(mmPosition);
            }

            private EditItem getNext() {
                if (mmPosition >= mmHistory.size()) {
                    return null;
                }

                EditItem item = mmHistory.get(mmPosition);
                mmPosition++;
                return item;
            }
        }

        private final class EditItem {
            private int start;
            private CharSequence before;
            private CharSequence after;

            public EditItem(int start, CharSequence before, CharSequence after) {
                this.start = start;
                this.before = before;
                this.after = after;
            }

            @Override
            public String toString() {
                return "EditItem{" +
                        "start=" + start +
                        ", before=" + before +
                        ", after=" + after +
                        '}';
            }
        }

        private final class EditTextChangeListener implements TextWatcher {
            private CharSequence mBeforeChange;
            private CharSequence mAfterChange;
            private ActionType lastActionType = ActionType.NOT_DEF;
            private long lastActionTime = 0;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mIsUndoOrRedo) {
                    return;
                }
                mBeforeChange = s.subSequence(start, start + count);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mIsUndoOrRedo) {
                    return;
                }
                mAfterChange = s.subSequence(start, start + count);
                makeBatch(start);
            }

            private void makeBatch(int start) {
                ActionType at = getActionType();
                EditItem editItem = mEditHistory.getCurrent();
                if ((lastActionType != at || ActionType.PASTE == at || System.currentTimeMillis() - lastActionTime > 1000) || editItem == null) {
                    mEditHistory.add(new EditItem(start, mBeforeChange, mAfterChange));
                } else {
                    if (at == ActionType.DELETE) {
                        editItem.start = start;
                        editItem.before = mBeforeChange + editItem.before.toString();
                    } else {
                        editItem.after = editItem.after + mAfterChange.toString();
                    }
                }
                lastActionType = at;
                lastActionTime = System.currentTimeMillis();
            }

            private ActionType getActionType() {
                if (!TextUtils.isEmpty(mBeforeChange) && TextUtils.isEmpty(mAfterChange)) {
                    return ActionType.DELETE;
                } else if (TextUtils.isEmpty(mBeforeChange) && !TextUtils.isEmpty(mAfterChange)) {
                    return ActionType.INSERT;
                } else {
                    return ActionType.PASTE;
                }
            }

            public void afterTextChanged(Editable s) {
            }
        }
    }
}
