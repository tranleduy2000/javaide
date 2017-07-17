/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.run.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;

import com.duy.editor.R;
import com.spartacusrex.spartacuside.TermDebug;
import com.spartacusrex.spartacuside.TermService;
import com.spartacusrex.spartacuside.TermViewFlipper;
import com.spartacusrex.spartacuside.model.TextRenderer;
import com.spartacusrex.spartacuside.model.UpdateCallback;
import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.session.TerminalEmulator;
import com.spartacusrex.spartacuside.session.TranscriptScreen;
import com.spartacusrex.spartacuside.util.TermSettings;
import com.spartacusrex.spartacuside.util.hardkeymappings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A view on a transcript and a terminal emulator. Displays the text of the
 * transcript and the current cursor position of the terminal emulator.
 */
public class EmulatorView extends View implements GestureDetector.OnGestureListener {

    private static final int SCREEN_CHECK_PERIOD = 1000;
    private static final int CURSOR_BLINK_PERIOD = 1000;
    private static final int SELECT_TEXT_OFFSET_Y = -40;
    private final String TAG = "EmulatorView";
    private final boolean LOG_KEY_EVENTS = false;
    /**
     * Our message handler class. Implements a periodic callback.
     */
    private final Handler mHandler = new Handler();
    Animation mAnimLeftIn;
    Animation mAnimRightIn;
    Animation mAnimLeftOut;
    Animation mAnimRightOut;
    private TermSettings mSettings;
    private TermViewFlipper mViewFlipper;
    /**
     * We defer some initialization until we have been layed out in the view
     * hierarchy. The boolean tracks when we know what our size is.
     */
    private boolean mKnownSize;
    private int mVisibleWidth;
    private int mVisibleHeight;
    private Rect mVisibleRect = new Rect();
    private TermSession mTermSession;
    /**
     * Our transcript. Contains the screen and the transcript.
     */
    private TranscriptScreen mTranscriptScreen;
    /**
     * Total width of each character, in pixels
     */
    private float mCharacterWidth;
    /**
     * Total height of each character, in pixels
     */
    private int mCharacterHeight;
    /**
     * Used to render text
     */
    private TextRenderer mTextRenderer;
    /**
     * Text size. Zero means 4 x 8 font.
     */
    private int mTextSize;
    private int mCursorStyle;
    private int mCursorBlink;
    /**
     * Foreground color.
     */
    private int mForeground;
    /**
     * Background color.
     */
    private int mBackground;
    /**
     * Used to paint the cursor
     */
    private Paint mCursorPaint;
    private Paint mBackgroundPaint;
    private boolean mUseCookedIme;
    /**
     * Our terminal emulator. We use this to get the current cursor position.
     */
    private TerminalEmulator mEmulator;
    /**
     * The number of rows of text to display.
     */
    private int mRows;
    /**
     * The number of columns of text to display.
     */
    private int mColumns;
    /**
     * The number of columns that are visible on the display.
     */

    private int mVisibleColumns;
    /**
     * The top row of text to display. Ranges from -activeTranscriptRows to 0
     */
    private int mTopRow;
    private int mLeftColumn;
    /**
     * Used to receive data from the remote process.
     */
    private FileOutputStream mTermOut;
    private boolean mCursorVisible = true;
    private boolean mIsSelectingText = false;
    private float mDensity;
    private float mScaledDensity;
    private int mSelXAnchor = -1;
    private int mSelYAnchor = -1;
    private int mSelX1 = -1;
    private int mSelY1 = -1;
    private int mSelX2 = -1;
    private int mSelY2 = -1;
    /**
     * Used to poll if the view has changed size. Wish there was a better way to do this.
     */
    private Runnable mCheckSize = new Runnable() {

        public void run() {
            updateSize(false);
            mHandler.postDelayed(this, SCREEN_CHECK_PERIOD);
        }
    };
    private Runnable mBlinkCursor = new Runnable() {
        public void run() {
            if (mCursorBlink != 0) {
                mCursorVisible = !mCursorVisible;
                mHandler.postDelayed(this, CURSOR_BLINK_PERIOD);
            } else {
                mCursorVisible = true;
            }
            // Perhaps just invalidate the character with the cursor.
            invalidate();
        }
    };
    private GestureDetector mGestureDetector;
    private float mScrollRemainder;
    private TermKeyListener mKeyListener;
    private String mImeBuffer = "";
    /**
     * Called by the TermSession when the contents of the view need updating
     */
    private UpdateCallback mUpdateNotify = new UpdateCallback() {
        public void onUpdate() {
            if (mIsSelectingText) {
                int rowShift = mEmulator.getScrollCounter();
                mSelY1 -= rowShift;
                mSelY2 -= rowShift;
                mSelYAnchor -= rowShift;
            }
            mEmulator.clearScrollCounter();
            ensureCursorVisible();
            invalidate();
        }
    };

    public EmulatorView(Context context, TermSession session, TermViewFlipper viewFlipper, DisplayMetrics metrics) {
        super(context);
        commonConstructor(session, viewFlipper);
        setDensity(metrics);

        mAnimLeftIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        mAnimLeftOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
        mAnimRightIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        mAnimRightOut = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
    }

    public UpdateCallback getUpdateCallback() {
        return mUpdateNotify;
    }

    public void setDensity(DisplayMetrics metrics) {
        mDensity = metrics.density;
        mScaledDensity = metrics.scaledDensity;
    }

    public void onResume() {
        updateSize(false);
        mHandler.postDelayed(mCheckSize, SCREEN_CHECK_PERIOD);
        if (mCursorBlink != 0) {
            mHandler.postDelayed(mBlinkCursor, CURSOR_BLINK_PERIOD);
        }
    }

    public void onPause() {
        mHandler.removeCallbacks(mCheckSize);
        if (mCursorBlink != 0) {
            mHandler.removeCallbacks(mBlinkCursor);
        }
    }

    public void updatePrefs(TermSettings settings) {
        mSettings = settings;
        setTextSize((int) (mSettings.getFontSize() * mDensity));
        setCursorStyle(mSettings.getCursorStyle(), mSettings.getCursorBlink());
        setUseCookedIME(mSettings.useCookedIME());
        setColors();
    }

    public void setColors() {
        int[] scheme = mSettings.getColorScheme();
        mForeground = scheme[0];
        mBackground = scheme[1];
        updateText();
    }

    public void resetTerminal() {
        mEmulator.reset();
        invalidate();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = mUseCookedIme ?
                EditorInfo.TYPE_CLASS_TEXT :
                EditorInfo.TYPE_NULL;
        return new InputConnection() {
            private boolean mInBatchEdit;
            /**
             * Used to handle composing text requests
             */
            private int mCursor;
            private int mComposingTextStart;
            private int mComposingTextEnd;
            private int mSelectedTextStart = 0;
            private int mSelectedTextEnd = 0;

            private void sendChar(int c) {
                try {
                    mapAndSend(c);
                } catch (IOException ex) {

                }
            }

            private void sendText(CharSequence text) {
                int n = text.length();
                try {
                    for (int i = 0; i < n; i++) {
                        char c = text.charAt(i);
                        mapAndSend(c);
                    }
                    mTermOut.flush();
                } catch (IOException e) {
                    Log.e(TAG, "error writing ", e);
                }
            }

            private void mapAndSend(int c) throws IOException {
                int result = mKeyListener.mapControlChar(c);
                if (result < TermKeyListener.KEYCODE_OFFSET) {
//                    Log.v("SpartacusRex","EMVIEW : 1) mapAndSend "+c+" "+result);

                    //Check for ALT
                    //if(mAl)

                    mTermOut.write(result);
                } else {
                    int code = result - TermKeyListener.KEYCODE_OFFSET;
//                    Log.v("SpartacusRex","EMVIEW : 2) mapAndSend "+c+" "+code);
                    mKeyListener.handleKeyCode(result - TermKeyListener.KEYCODE_OFFSET, mTermOut, getKeypadApplicationMode());
                }
            }

            public boolean beginBatchEdit() {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "beginBatchEdit");
                }
                setImeBuffer("");
                mCursor = 0;
                mComposingTextStart = 0;
                mComposingTextEnd = 0;
                mInBatchEdit = true;
                return true;
            }

            public boolean clearMetaKeyStates(int arg0) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "clearMetaKeyStates " + arg0);
                }
                return false;
            }

            public boolean commitCompletion(CompletionInfo arg0) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "commitCompletion " + arg0);
                }
                return false;
            }

            @Override
            public boolean commitCorrection(CorrectionInfo correctionInfo) {
                return false;
            }

            public boolean endBatchEdit() {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "endBatchEdit");
                }
                mInBatchEdit = false;
                return true;
            }

            public boolean finishComposingText() {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "finishComposingText");
                }
                sendText(mImeBuffer);
                setImeBuffer("");
                mComposingTextStart = 0;
                mComposingTextEnd = 0;
                mCursor = 0;
                return true;
            }

            public int getCursorCapsMode(int arg0) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "getCursorCapsMode(" + arg0 + ")");
                }
                return 0;
            }

            public ExtractedText getExtractedText(ExtractedTextRequest arg0,
                                                  int arg1) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "getExtractedText" + arg0 + "," + arg1);
                }
                return null;
            }

            public CharSequence getTextAfterCursor(int n, int flags) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "getTextAfterCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, mImeBuffer.length() - mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= mImeBuffer.length()) {
                    return "";
                }
                return mImeBuffer.substring(mCursor, mCursor + len);
            }

            public CharSequence getTextBeforeCursor(int n, int flags) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "getTextBeforeCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= mImeBuffer.length()) {
                    return "";
                }
                return mImeBuffer.substring(mCursor - len, mCursor);
            }

            public boolean performContextMenuAction(int arg0) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "performContextMenuAction" + arg0);
                }
                return true;
            }

            public boolean performPrivateCommand(String arg0, Bundle arg1) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "performPrivateCommand" + arg0 + "," + arg1);
                }
                return true;
            }

            @Override
            public boolean requestCursorUpdates(int i) {
                return false;
            }

            @Override
            public Handler getHandler() {
                return null;
            }

            @Override
            public void closeConnection() {

            }

            @Override
            public boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle) {
                return false;
            }

            public boolean reportFullscreenMode(boolean arg0) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "reportFullscreenMode" + arg0);
                }
                return true;
            }

//            public boolean commitCorrection (CorrectionInfo correctionInfo) {
//                if (TermDebug.LOG_IME) {
//                    Log.w(TAG, "commitCorrection");
//                }
//                return true;
//            }

            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "commitText(\"" + text + "\", " + newCursorPosition + ")");
                }
                clearComposingText();
                sendText(text);
                setImeBuffer("");
                mCursor = 0;
                return true;
            }

            private void clearComposingText() {
                setImeBuffer(mImeBuffer.substring(0, mComposingTextStart) +
                        mImeBuffer.substring(mComposingTextEnd));
                if (mCursor < mComposingTextStart) {
                    // do nothing
                } else if (mCursor < mComposingTextEnd) {
                    mCursor = mComposingTextStart;
                } else {
                    mCursor -= mComposingTextEnd - mComposingTextStart;
                }
                mComposingTextEnd = mComposingTextStart = 0;
            }

            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "deleteSurroundingText(" + leftLength +
                            "," + rightLength + ")");
                }
                if (leftLength > 0) {
                    for (int i = 0; i < leftLength; i++) {
                        sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    }
                } else if ((leftLength == 0) && (rightLength == 0)) {
                    // Delete key held down / repeating
                    sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
                // TODO: handle forward deletes.
                return true;
            }

            @Override
            public boolean deleteSurroundingTextInCodePoints(int i, int i1) {
                return false;
            }

            public boolean performEditorAction(int actionCode) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "performEditorAction(" + actionCode + ")");
                }
                if (actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // The "return" key has been pressed on the IME.
                    sendText("\n");
                }
                return true;
            }

            public boolean sendKeyEvent(KeyEvent event) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "sendKeyEvent(" + event + ")");
                }
                // Some keys are sent here rather than to commitText.
                // In particular, del and the digit keys are sent here.
                // (And I have reports that the HTC Magic also sends Return here.)
                // As a bit of defensive programming, handle every key.
                dispatchKeyEvent(event);
                return true;
            }

            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "setComposingText(\"" + text + "\", " + newCursorPosition + ")");
                }
                setImeBuffer(mImeBuffer.substring(0, mComposingTextStart) +
                        text + mImeBuffer.substring(mComposingTextEnd));
                mComposingTextEnd = mComposingTextStart + text.length();
                mCursor = newCursorPosition > 0 ? mComposingTextEnd + newCursorPosition - 1
                        : mComposingTextStart - newCursorPosition;
                return true;
            }

            public boolean setSelection(int start, int end) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "setSelection" + start + "," + end);
                }
                int length = mImeBuffer.length();
                if (start == end && start > 0 && start < length) {
                    mSelectedTextStart = mSelectedTextEnd = 0;
                    mCursor = start;
                } else if (start < end && start > 0 && end < length) {
                    mSelectedTextStart = start;
                    mSelectedTextEnd = end;
                    mCursor = start;
                }
                return true;
            }

            public boolean setComposingRegion(int start, int end) {
                if (TermDebug.LOG_IME) {
                    Log.w(TAG, "setComposingRegion " + start + "," + end);
                }
                if (start < end && start > 0 && end < mImeBuffer.length()) {
                    clearComposingText();
                    mComposingTextStart = start;
                    mComposingTextEnd = end;
                }
                return true;
            }

            public CharSequence getSelectedText(int flags) {

                try {

                    if (TermDebug.LOG_IME) {
                        Log.w(TAG, "getSelectedText " + flags);
                    }

                    if (mImeBuffer.length() < 1) {
                        return "";
                    }

                    return mImeBuffer.substring(mSelectedTextStart, mSelectedTextEnd + 1);

                } catch (Exception e) {

                }

                return "";
            }

        };
    }

    private void setImeBuffer(String buffer) {
        if (!buffer.equals(mImeBuffer)) {
            invalidate();
        }
        mImeBuffer = buffer;
    }

    public boolean getKeypadApplicationMode() {
        return mEmulator.getKeypadApplicationMode();
    }

    private void commonConstructor(TermSession session, TermViewFlipper viewFlipper) {
        mTextRenderer = null;
        mCursorPaint = new Paint();
        mCursorPaint.setARGB(255, 128, 128, 128);
        mBackgroundPaint = new Paint();
        mTopRow = 0;
        mLeftColumn = 0;
        mGestureDetector = new GestureDetector(this);
        // mGestureDetector.setIsLongpressEnabled(false);
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            public boolean onSingleTapConfirmed(MotionEvent arg0) {
                return true;
            }

            public boolean onDoubleTap(MotionEvent arg0) {
                //Toggle the Soft Keyboard..
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            }

            public boolean onDoubleTapEvent(MotionEvent arg0) {
                return true;
            }
        });

        setVerticalScrollBarEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        initialize(session, viewFlipper);
    }

    @Override
    protected int computeVerticalScrollRange() {
        return mTranscriptScreen.getActiveRows();
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return mRows;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return mTranscriptScreen.getActiveRows() + mTopRow - mRows;
    }

    /**
     * Call this to initialize the view.
     *
     * @param session The terminal session this view will be displaying
     */
    private void initialize(TermSession session, TermViewFlipper viewFlipper) {
        mTermSession = session;
        mTranscriptScreen = session.getTranscriptScreen();
        mEmulator = session.getEmulator();
        mTermOut = session.getTermOut();

        mViewFlipper = viewFlipper;

        mKeyListener = new TermKeyListener();
        mTextSize = 10;
        mForeground = TermSettings.WHITE;
        mBackground = TermSettings.BLACK;
        updateText();

        requestFocus();
    }

    public TermSession getTermSession() {
        return mTermSession;
    }

    /**
     * Page the terminal view (scroll it up or down by delta screenfulls.)
     *
     * @param delta the number of screens to scroll. Positive means scroll down,
     *              negative means scroll up.
     */
    public void page(int delta) {
        mTopRow =
                Math.min(0, Math.max(-(mTranscriptScreen
                        .getActiveTranscriptRows()), mTopRow + mRows * delta));
        invalidate();
    }

    /**
     * Page the terminal view horizontally.
     *
     * @param deltaColumns the number of columns to scroll. Positive scrolls to
     *                     the right.
     */
    public void pageHorizontal(int deltaColumns) {
        mLeftColumn =
                Math.max(0, Math.min(mLeftColumn + deltaColumns, mColumns
                        - mVisibleColumns));
        invalidate();
    }

    /**
     * Sets the text size, which in turn sets the number of rows and columns
     *
     * @param fontSize the new font size, in pixels.
     */
    public void setTextSize(int fontSize) {
        mTextSize = fontSize;
        updateText();
    }

    public void setCursorStyle(int style, int blink) {
        mCursorStyle = style;
        if (blink != 0 && mCursorBlink == 0) {
            mHandler.postDelayed(mBlinkCursor, CURSOR_BLINK_PERIOD);
        } else if (blink == 0 && mCursorBlink != 0) {
            mHandler.removeCallbacks(mBlinkCursor);
        }
        mCursorBlink = blink;
    }

    public void setUseCookedIME(boolean useCookedIME) {
        mUseCookedIme = useCookedIME;
    }

    // Begin GestureDetector.OnGestureListener methods

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    public void onLongPress(MotionEvent e) {
        showContextMenu();
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        distanceY += mScrollRemainder;
        int deltaRows = (int) (distanceY / mCharacterHeight);
        mScrollRemainder = distanceY - deltaRows * mCharacterHeight;
        mTopRow =
                Math.min(0, Math.max(-(mTranscriptScreen
                        .getActiveTranscriptRows()), mTopRow + deltaRows));
        invalidate();

        return true;
    }

    public void onSingleTapConfirmed(MotionEvent e) {
    }

    public boolean onJumpTapDown(MotionEvent e1, MotionEvent e2) {
        // Scroll to bottom
        mTopRow = 0;
        invalidate();
        return true;
    }

    public boolean onJumpTapUp(MotionEvent e1, MotionEvent e2) {
        // Scroll to top
        mTopRow = -mTranscriptScreen.getActiveTranscriptRows();
        invalidate();
        return true;
    }


    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            // Assume user wanted side to side movement
            if (velocityX > 0) {
                mViewFlipper.setOutAnimation(mAnimRightOut);
                mViewFlipper.setInAnimation(mAnimLeftIn);

                // Left to right swipe -- previous window
                mViewFlipper.showPrevious();
            } else {
                mViewFlipper.setOutAnimation(mAnimLeftOut);
                mViewFlipper.setInAnimation(mAnimRightIn);

                // Right to left swipe -- next window
                mViewFlipper.showNext();
            }
        } else {
            // TODO: add animation man's (non animated) fling
            mScrollRemainder = 0.0f;
            onScroll(e1, e2, 2 * velocityX, -2 * velocityY);
        }
        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        mScrollRemainder = 0.0f;
        return true;
    }

    // End GestureDetector.OnGestureListener methods

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsSelectingText) {
            return onTouchEventWhileSelectingText(ev);
        } else {
            return mGestureDetector.onTouchEvent(ev);
        }
    }

    private boolean onTouchEventWhileSelectingText(MotionEvent ev) {
        int action = ev.getAction();
        int cx = (int) (ev.getX() / mCharacterWidth);
        int cy = Math.max(0,
                (int) ((ev.getY() + SELECT_TEXT_OFFSET_Y * mScaledDensity)
                        / mCharacterHeight) + mTopRow);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mSelXAnchor = cx;
                mSelYAnchor = cy;
                mSelX1 = cx;
                mSelY1 = cy;
                mSelX2 = mSelX1;
                mSelY2 = mSelY1;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int minx = Math.min(mSelXAnchor, cx);
                int maxx = Math.max(mSelXAnchor, cx);
                int miny = Math.min(mSelYAnchor, cy);
                int maxy = Math.max(mSelYAnchor, cy);
                mSelX1 = minx;
                mSelY1 = miny;
                mSelX2 = maxx;
                mSelY2 = maxy;
                if (action == MotionEvent.ACTION_UP) {
                    ClipboardManager clip = (ClipboardManager)
                            getContext().getApplicationContext()
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                    clip.setText(getSelectedText().trim());
                    toggleSelectingText();
                }
                invalidate();
                break;
            default:
                toggleSelectingText();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int zKeyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS) {
            Log.v("Terminal IDE", "EmulatorView onKeyDown TOP EVENT code:" + zKeyCode);
        }

        //Could be scanning..
        TermService.keyLoggerKey(zKeyCode);

        //Check with HardKey Mappings..!
        KeyEvent newevent = handleKeyCodeMapper(event.getAction(), zKeyCode);
        if (newevent == null) {
            //Function press..
            return true;
        }

        //The new Key Code
        int keyCode = newevent.getKeyCode();

        if (handleControlKey(keyCode, true)) {
            return true;
        } else if (handleFnKey(keyCode, true)) {
            //Send the escape key
            try {
                mKeyListener.keyDown(TermKeyListener.KEYCODE_ESCAPE, newevent, mTermOut, getKeypadApplicationMode());
            } catch (IOException iOException) {
            }
            return true;
        } else if (isSystemKey(keyCode, newevent) && keyCode != 122 && keyCode != 123 && keyCode != 92 && keyCode != 93) {
            // Don't intercept the system keys And the HOME /END / PGUP / PGDOWN KEYS
            return super.onKeyDown(keyCode, newevent);
        }

        try {
            mKeyListener.keyDown(keyCode, newevent, mTermOut, getKeypadApplicationMode());
        } catch (IOException e) {
            // Ignore I/O exceptions
        }

        return true;
    }

    @Override
    public boolean onKeyUp(int zKeyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS) {
            Log.w(TAG, "onKeyUp " + zKeyCode);
        }

        //Check with HardKey Mappings..!
        KeyEvent newevent = handleKeyCodeMapper(event.getAction(), zKeyCode);
        if (newevent == null) {
            //Function press..
            return true;
        }

        //The new Key Code
        int keyCode = newevent.getKeyCode();

        if (handleControlKey(keyCode, false)) {
            return true;
        } else if (handleFnKey(keyCode, false)) {
            mKeyListener.keyUp(TermKeyListener.KEYCODE_ESCAPE);
            return true;
        } else if (isSystemKey(keyCode, newevent)) {
            // Don't intercept the system keys
            return super.onKeyUp(keyCode, newevent);
        }

        mKeyListener.keyUp(keyCode);
        return true;
    }

    private KeyEvent handleKeyCodeMapper(int zAction, int zKeyCode) {
        //Check with HardKey Mappings..!
        KeyEvent newevent = new KeyEvent(zAction, zKeyCode);

        if (TermService.isHardKeyEnabled()) {
            int hardmap = TermService.isSpecialKeyCode(zKeyCode);

            //Valid.. ?
            if (hardmap != -1) {
                //Its a special key code..
                if (hardmap == hardkeymappings.HARDKEY_CTRL_LEFT || hardmap == hardkeymappings.HARDKEY_CTRL_RIGHT) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_CTRL_LEFT);

                } else if (hardmap == hardkeymappings.HARDKEY_ALT_LEFT || hardmap == hardkeymappings.HARDKEY_ALT_RIGHT) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_ALT_LEFT);

                } else if (hardmap == hardkeymappings.HARDKEY_ESCAPE) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_ESCAPE);

                } else if (hardmap == hardkeymappings.HARDKEY_FUNCTION) {
                    //Just Update the Function Key Settings
                    mKeyListener.handleFunctionKey(false);
                    return null;

                } else if (hardmap == hardkeymappings.HARDKEY_TAB) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_TAB);

                } else if (hardmap == hardkeymappings.HARDKEY_LSHIFT || hardmap == hardkeymappings.HARDKEY_RSHIFT) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_SHIFT_LEFT);

                } else if (hardmap == hardkeymappings.HARDKEY_SPACE) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_SPACE);

                } else if (hardmap == hardkeymappings.HARDKEY_ENTER) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_ENTER);

                } else if (hardmap == hardkeymappings.HARDKEY_DELETE) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_FORWARD_DEL);

                } else if (hardmap == hardkeymappings.HARDKEY_BACKSPACE) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_DEL);

                } else if (hardmap == hardkeymappings.HARDKEY_UP) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_DPAD_UP);

                } else if (hardmap == hardkeymappings.HARDKEY_DOWN) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_DPAD_DOWN);

                } else if (hardmap == hardkeymappings.HARDKEY_LEFT) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_DPAD_LEFT);

                } else if (hardmap == hardkeymappings.HARDKEY_RIGHT) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_DPAD_RIGHT);

                } else if (hardmap == hardkeymappings.HARDKEY_PGUP) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_PAGE_UP);

                } else if (hardmap == hardkeymappings.HARDKEY_PGDOWN) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_PAGE_DOWN);

                } else if (hardmap == hardkeymappings.HARDKEY_HOME) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_MOVE_HOME);

                } else if (hardmap == hardkeymappings.HARDKEY_END) {
                    newevent = new KeyEvent(zAction, TermKeyListener.KEYCODE_MOVE_END);
                }
            }
        }

        return newevent;
    }


    private boolean handleControlKey(int keyCode, boolean down) {
        if (keyCode == mSettings.getControlKeyCode()) {
            if (LOG_KEY_EVENTS) {
                Log.w(TAG, "handleControlKey " + keyCode);
            }
            mKeyListener.handleControlKey(down);
            return true;
        }

        return false;
    }

    private boolean handleFnKey(int keyCode, boolean down) {
        if (keyCode == mSettings.getFnKeyCode()) {
            if (LOG_KEY_EVENTS) {
                Log.w(TAG, "handleFnKey " + keyCode);
            }
            //if(down){
            //Send the escape sequence..
            //}
            //mKeyListener.handleFnKey(down);
            return true;
        }
        return false;
    }

    private boolean handleFunctionKey(boolean down) {
        if (LOG_KEY_EVENTS) {
            Log.w(TAG, "handleFunctionKey ");
        }
        mKeyListener.handleFunctionKey(down);
        return true;
    }

    private boolean isSystemKey(int keyCode, KeyEvent event) {
        return event.isSystem();
    }

    private void updateText() {
        if (mTextSize > 0) {
            mTextRenderer = new PaintRenderer(mTextSize, mForeground,
                    mBackground);
        } else {
            mTextRenderer = new Bitmap4x8FontRenderer(getResources(),
                    mForeground, mBackground);
        }
        mBackgroundPaint.setColor(mBackground);
        mCharacterWidth = mTextRenderer.getCharacterWidth();
        mCharacterHeight = mTextRenderer.getCharacterHeight();

        updateSize(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        boolean oldKnownSize = mKnownSize;
        if (!mKnownSize) {
            mKnownSize = true;
        }
        updateSize(false);
    }

    private void updateSize(int w, int h) {
        mColumns = Math.max(1, (int) (((float) w) / mCharacterWidth));
        mRows = Math.max(1, h / mCharacterHeight);
        mVisibleColumns = (int) (((float) mVisibleWidth) / mCharacterWidth);

        mTermSession.updateSize(mColumns, mRows);

        // Reset our paging:
        mTopRow = 0;
        mLeftColumn = 0;

        invalidate();
    }

    public void updateSize(boolean force) {
        if (mKnownSize) {
            getWindowVisibleDisplayFrame(mVisibleRect);
            int w = mVisibleRect.width();
            int h = mVisibleRect.height();
            // Log.w("Term", "(" + w + ", " + h + ")");
            if (force || w != mVisibleWidth || h != mVisibleHeight) {
                mVisibleWidth = w;
                mVisibleHeight = h;
                updateSize(mVisibleWidth, mVisibleHeight);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateSize(false);
        int w = getWidth();
        int h = getHeight();
        canvas.drawRect(0, 0, w, h, mBackgroundPaint);
        float x = -mLeftColumn * mCharacterWidth;
        float y = mCharacterHeight;
        int endLine = mTopRow + mRows;
        int cx = mEmulator.getCursorCol();
        int cy = mEmulator.getCursorRow();
        for (int i = mTopRow; i < endLine; i++) {
            int cursorX = -1;
            if (i == cy && mCursorVisible) {
                cursorX = cx;
            }
            int selx1 = -1;
            int selx2 = -1;
            if (i >= mSelY1 && i <= mSelY2) {
                if (i == mSelY1) {
                    selx1 = mSelX1;
                }
                if (i == mSelY2) {
                    selx2 = mSelX2;
                } else {
                    selx2 = mColumns;
                }
            }
            mTranscriptScreen.drawText(i, canvas, x, y, mTextRenderer, cursorX, selx1, selx2, mImeBuffer);
            y += mCharacterHeight;
        }
    }

    private void ensureCursorVisible() {
        mTopRow = 0;
        if (mVisibleColumns > 0) {
            int cx = mEmulator.getCursorCol();
            int visibleCursorX = mEmulator.getCursorCol() - mLeftColumn;
            if (visibleCursorX < 0) {
                mLeftColumn = cx;
            } else if (visibleCursorX >= mVisibleColumns) {
                mLeftColumn = (cx - mVisibleColumns) + 1;
            }
        }
    }

    public void toggleSelectingText() {
        mIsSelectingText = !mIsSelectingText;
        setVerticalScrollBarEnabled(!mIsSelectingText);
        if (!mIsSelectingText) {
            mSelX1 = -1;
            mSelY1 = -1;
            mSelX2 = -1;
            mSelY2 = -1;
        }
    }

    public boolean getSelectingText() {
        return mIsSelectingText;
    }

    public String getSelectedText() {
        return mEmulator.getSelectedText(mSelX1, mSelY1, mSelX2, mSelY2);
    }
}

abstract class BaseTextRenderer implements TextRenderer {
    protected final static int mCursorPaint = 0xff808080;
    protected int[] mForePaint = {
            0xff000000, // Black
            0xffff0000, // Red
            0xff00ff00, // green
            0xffffff00, // yellow
            0xff0000ff, // blue
            0xffff00ff, // magenta
            0xff00ffff, // cyan
            0xffffffff  // white -- is overridden by constructor
    };
    protected int[] mBackPaint = {
            0xff000000, // Black -- is overridden by constructor
            0xffcc0000, // Red
            0xff00cc00, // green
            0xffcccc00, // yellow
            0xff0000cc, // blue
            0xffff00cc, // magenta
            0xff00cccc, // cyan
            0xffffffff  // white
    };

    public BaseTextRenderer(int forePaintColor, int backPaintColor) {
        mForePaint[7] = forePaintColor;
        mBackPaint[0] = backPaintColor;

    }
}

class Bitmap4x8FontRenderer extends BaseTextRenderer {
    private final static int kCharacterWidth = 4;
    private final static int kCharacterHeight = 8;
    private static final float BYTE_SCALE = 1.0f / 255.0f;
    private Bitmap mFont;
    private int mCurrentForeColor;
    private int mCurrentBackColor;
    private float[] mColorMatrix;
    private Paint mPaint;

    public Bitmap4x8FontRenderer(Resources resources,
                                 int forePaintColor, int backPaintColor) {
        super(forePaintColor, backPaintColor);
        mFont = BitmapFactory.decodeResource(resources, R.drawable.atari_small);
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public float getCharacterWidth() {
        return kCharacterWidth;
    }

    public int getCharacterHeight() {
        return kCharacterHeight;
    }

    public void drawTextRun(Canvas canvas, float x, float y,
                            int lineOffset, char[] text, int index, int count,
                            boolean cursor, int foreColor, int backColor) {
        setColorMatrix(mForePaint[foreColor & 7],
                cursor ? mCursorPaint : mBackPaint[backColor & 7]);
        int destX = (int) x + kCharacterWidth * lineOffset;
        int destY = (int) y;
        Rect srcRect = new Rect();
        Rect destRect = new Rect();
        destRect.top = (destY - kCharacterHeight);
        destRect.bottom = destY;
        for (int i = 0; i < count; i++) {
            char c = text[i + index];
            if ((cursor || (c != 32)) && (c < 128)) {
                int cellX = c & 31;
                int cellY = (c >> 5) & 3;
                int srcX = cellX * kCharacterWidth;
                int srcY = cellY * kCharacterHeight;
                srcRect.set(srcX, srcY,
                        srcX + kCharacterWidth, srcY + kCharacterHeight);
                destRect.left = destX;
                destRect.right = destX + kCharacterWidth;
                canvas.drawBitmap(mFont, srcRect, destRect, mPaint);
            }
            destX += kCharacterWidth;
        }
    }

    private void setColorMatrix(int foreColor, int backColor) {
        if ((foreColor != mCurrentForeColor)
                || (backColor != mCurrentBackColor)
                || (mColorMatrix == null)) {
            mCurrentForeColor = foreColor;
            mCurrentBackColor = backColor;
            if (mColorMatrix == null) {
                mColorMatrix = new float[20];
                mColorMatrix[18] = 1.0f; // Just copy Alpha
            }
            for (int component = 0; component < 3; component++) {
                int rightShift = (2 - component) << 3;
                int fore = 0xff & (foreColor >> rightShift);
                int back = 0xff & (backColor >> rightShift);
                int delta = back - fore;
                mColorMatrix[component * 6] = delta * BYTE_SCALE;
                mColorMatrix[component * 5 + 4] = fore;
            }
            mPaint.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
        }
    }
}

class PaintRenderer extends BaseTextRenderer {
    private static final char[] EXAMPLE_CHAR = {'X'};
    private Paint mTextPaint;
    private float mCharWidth;
    private int mCharHeight;
    private int mCharAscent;
    private int mCharDescent;

    public PaintRenderer(int fontSize, int forePaintColor, int backPaintColor) {
        super(forePaintColor, backPaintColor);
        mTextPaint = new Paint();
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(fontSize);

        mCharHeight = (int) Math.ceil(mTextPaint.getFontSpacing());
        mCharAscent = (int) Math.ceil(mTextPaint.ascent());
        mCharDescent = mCharHeight + mCharAscent;
        mCharWidth = mTextPaint.measureText(EXAMPLE_CHAR, 0, 1);
    }

    public void drawTextRun(Canvas canvas, float x, float y, int lineOffset,
                            char[] text, int index, int count,
                            boolean cursor, int foreColor, int backColor) {
        if (cursor) {
            mTextPaint.setColor(mCursorPaint);
        } else {
            mTextPaint.setColor(mBackPaint[backColor & 0x7]);
        }
        float left = x + lineOffset * mCharWidth;
        canvas.drawRect(left, y + mCharAscent,
                left + count * mCharWidth, y + mCharDescent,
                mTextPaint);
        boolean bold = (foreColor & 0x8) != 0;
        boolean underline = (backColor & 0x8) != 0;
        if (bold) {
            mTextPaint.setFakeBoldText(true);
        }
        if (underline) {
            mTextPaint.setUnderlineText(true);
        }
        mTextPaint.setColor(mForePaint[foreColor & 0x7]);
        canvas.drawText(text, index, count, left, y, mTextPaint);
        if (bold) {
            mTextPaint.setFakeBoldText(false);
        }
        if (underline) {
            mTextPaint.setUnderlineText(false);
        }
    }

    public int getCharacterHeight() {
        return mCharHeight;
    }

    public float getCharacterWidth() {
        return mCharWidth;
    }
}

