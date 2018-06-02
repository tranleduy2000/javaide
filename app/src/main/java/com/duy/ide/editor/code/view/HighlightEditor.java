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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.duy.ide.R;
import com.duy.ide.editor.code.highlight.Highlighter;
import com.duy.ide.editor.code.highlight.java.BracketHighlighter;
import com.duy.ide.editor.code.highlight.java.JavaHighlighter;
import com.duy.ide.editor.code.highlight.xml.XmlHighlighter;
import com.duy.ide.themefont.themes.ThemeManager;
import com.duy.ide.themefont.themes.database.CodeTheme;
import com.duy.ide.themefont.themes.database.CodeThemeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HighlightEditor extends CodeSuggestsEditText
        implements View.OnKeyListener, GestureDetector.OnGestureListener {
    public static final String TAG = HighlightEditor.class.getSimpleName();
    public static final int SYNTAX_DELAY_MILLIS_SHORT = 100;
    public static final int SYNTAX_DELAY_MILLIS_LONG = 700;
    public static final int CHARS_TO_COLOR = 2500;
    private final Handler updateHandler = new Handler();
    private final Object objectThread = new Object();
    public boolean showLines = true;
    public boolean wordWrap = true;
    public LineInfo lineError = null;
    protected Paint mPaintNumbers;
    protected Paint mPaintHighlight;
    protected int mPaddingDP = 4;
    protected int mPadding, mLinePadding;
    protected float mScale;
    protected int mHighlightedLine;
    protected int mHighlightStart;
    protected Rect mDrawingRect, mLineBounds;
    /**
     * the scroller instance
     */
    protected Scroller mTedScroller;
    /**
     * the velocity tracker
     */
    protected GestureDetector mGestureDetector;
    /**
     * the Max size of the view
     */
    protected Point mMaxSize;
    //Colors
    private boolean autoCompile = false;
    private CodeTheme codeTheme = new CodeTheme(true);
    private Context mContext;
    private boolean canEdit = true;
    @Nullable
    private ScrollView verticalScroll;
    private int lastPinLine = -1;
    private LineUtils lineUtils;
    private boolean[] isGoodLineArray;
    private int[] realLines;
    private int lineCount;
    private boolean isFinding = false;
    /**
     * Disconnect this undo/redo from the text
     * view.
     */
    private boolean enabledChangeListener = false;
    /**
     * The change listener.
     */
    private EditTextChangeListener mChangeListener;
    private int numberWidth = 0;
    @Nullable
    private Highlighter mHighlighter;
    private final Runnable colorRunnable_duringEditing =
            new Runnable() {
                @Override
                public void run() {
                    highlightText();
                }
            };
    private final Runnable colorRunnable_duringScroll =
            new Runnable() {
                @Override
                public void run() {
                    highlightText();
                }
            };
    private BracketHighlighter mBracketHighlighter;

    public HighlightEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public HighlightEditor(Context context) {
        super(context);
        setup(context);
    }

    public HighlightEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    public CodeTheme getCodeTheme() {
        return codeTheme;
    }

    public void setCodeTheme(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
        if (mHighlighter != null) {
            mHighlighter.setCodeTheme(codeTheme);
        }
        this.mBracketHighlighter.setCodeTheme(codeTheme);
        setTextColor(codeTheme.getTextColor());
        setBackgroundColor(codeTheme.getBackground());
        mPaintNumbers.setColor(codeTheme.getNumberColor());
        refresh();
    }

    public boolean isAutoCompile() {
        return autoCompile;
    }

    public void setAutoCompile(boolean autoCompile) {
        this.autoCompile = autoCompile;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    private void setup(Context context) {
        setSaveEnabled(false);
        this.mContext = context;

        lineUtils = new LineUtils();
        mPaintNumbers = new Paint();
        mPaintNumbers.setColor(getResources().getColor(R.color.color_number_color));
        mPaintNumbers.setAntiAlias(true);

        mPaintHighlight = new Paint();

        mScale = context.getResources().getDisplayMetrics().density;
        mPadding = (int) (mPaddingDP * mScale);
        mHighlightedLine = mHighlightStart = -1;
        mDrawingRect = new Rect();
        mLineBounds = new Rect();
        mGestureDetector = new GestureDetector(getContext(), HighlightEditor.this);
        mChangeListener = new EditTextChangeListener();
        mBracketHighlighter = new BracketHighlighter(this, codeTheme);
        updateFromSettings();
        enableTextChangedListener();
    }

    public void setFileExt(String fileExt) {
        if (fileExt.equalsIgnoreCase("java")) {
            this.mHighlighter = new JavaHighlighter(this);
        } else if (fileExt.equalsIgnoreCase("xml")) {
            this.mHighlighter = new XmlHighlighter(this);
        }
    }

    public void setLineError(@NonNull LineInfo lineError) {
        this.lineError = lineError;
    }

    public void computeScroll() {

        if (mTedScroller != null) {
            if (mTedScroller.computeScrollOffset()) {
                scrollTo(mTedScroller.getCurrX(), mTedScroller.getCurrY());
            }
        } else {
            super.computeScroll();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);
        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(event);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        if (isEnabled()) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this,
                    InputMethodManager.SHOW_IMPLICIT);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mEditorSetting.flingToScroll()) {
            return true;
        }

        if (mTedScroller != null) {
            mTedScroller.fling(getScrollX(), getScrollY(), -(int) velocityX, -(int) velocityY, 0, mMaxSize.x, 0,
                    mMaxSize.y);
        }
        return true;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        int lineX, baseline;
        if (lineCount != getLineCount()) {
            lineCount = getLineCount();
            lineUtils.updateHasNewLineArray(lineCount, getLayout(), getText().toString());
            isGoodLineArray = lineUtils.getGoodLines();
            realLines = lineUtils.getRealLines();
        }
        if (showLines) {
            int padding = calculateLinePadding();
            if (mLinePadding != padding) {
                mLinePadding = padding;
                setPadding(mLinePadding, mPadding, mPadding, mPadding);
            }
        }

        getDrawingRect(mDrawingRect);
        lineX = mDrawingRect.left + mLinePadding - mPadding;
        int min = 0;
        int max = lineCount;
        getLineBounds(0, mLineBounds);
        int startBottom = mLineBounds.bottom;
        int startTop = mLineBounds.top;
        getLineBounds(lineCount - 1, mLineBounds);
        int endBottom = mLineBounds.bottom;
        int endTop = mLineBounds.top;
        if (lineCount > 1 && endBottom > startBottom && endTop > startTop) {
            min = Math.max(min, ((mDrawingRect.top - startBottom) * (lineCount - 1)) / (endBottom - startBottom));
            max = Math.min(max, ((mDrawingRect.bottom - startTop) * (lineCount - 1)) / (endTop - startTop) + 1);
        }
        for (int i = min; i < max; i++) {
            baseline = getLineBounds(i, mLineBounds);

            if ((mMaxSize != null) && (mMaxSize.x < mLineBounds.right)) {
                mMaxSize.x = mLineBounds.right;
            }

            if ((i == mHighlightedLine) && (!wordWrap)) {
                canvas.drawRect(mLineBounds, mPaintHighlight);
            }
            if (showLines && isGoodLineArray[i]) {
                int realLine = realLines[i];
                canvas.drawText("" + (realLine + 1), mDrawingRect.left, baseline, mPaintNumbers);
            }
        }
        if (showLines) {
            canvas.drawLine(lineX, mDrawingRect.top, lineX, mDrawingRect.bottom, mPaintNumbers);
        }

        getLineBounds(lineCount - 1, mLineBounds);
        if (mMaxSize != null) {
            mMaxSize.y = mLineBounds.bottom;
            mMaxSize.x = Math.max(mMaxSize.x + mPadding - mDrawingRect.width(), 0);
            mMaxSize.y = Math.max(mMaxSize.y + mPadding - mDrawingRect.height(), 0);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void updateFromSettings() {
        String name = mEditorSetting.getString(mContext.getString(R.string.key_code_theme));
        CodeTheme theme = ThemeManager.getTheme(name, getContext());
        setCodeTheme(theme);

        int style = CodeThemeUtils.getCodeTheme(mContext, "");
        TypedArray typedArray = mContext.obtainStyledAttributes(style,
                R.styleable.CodeTheme);
        this.canEdit = typedArray.getBoolean(R.styleable.CodeTheme_can_edit, true);
        typedArray.recycle();

        setTypeface(mEditorSetting.getEditorFont());
        setHorizontallyScrolling(!mEditorSetting.isWrapText());
        setOverScrollMode(OVER_SCROLL_ALWAYS);

        setTextSize(mEditorSetting.getEditorTextSize());
        mPaintNumbers.setTextSize(getTextSize());

        showLines = mEditorSetting.isShowLines();

        int count = getLineCount();
        if (showLines) {
            mLinePadding = calculateLinePadding();
            setPadding(mLinePadding, mPadding, mPadding, mPadding);
        } else {
            setPadding(mPadding, mPadding, mPadding, mPadding);
        }
        autoCompile = mEditorSetting.isAutoCompile();
        wordWrap = mEditorSetting.isWrapText();
        if (wordWrap) {
            setHorizontalScrollBarEnabled(false);
        } else {
            setHorizontalScrollBarEnabled(true);
        }

        postInvalidate();
        refreshDrawableState();

        if (mEditorSetting.useImeKeyboard()) {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }  // use Fling when scrolling settings ?
        if (mEditorSetting.flingToScroll()) {
            mTedScroller = new Scroller(getContext());
            mMaxSize = new Point();
        } else {
            mTedScroller = null;
            mMaxSize = null;
        }

    }

    private int calculateLinePadding() {
        int count = getLineCount();
        int result = (int) (Math.floor(Math.log10(count)) + 1);

        Rect bounds = new Rect();
        mPaintNumbers.getTextBounds("0", 0, 1, bounds);
        numberWidth = bounds.width();
        result = (result * numberWidth) + numberWidth + mPadding;
        return result;
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    /**
     * This method used to set text and high light text
     */
    public void setTextHighlighted(CharSequence text) {
        lineError = null;
        setText(text);
        refresh();
    }

    public void refresh() {
        updateHandler.removeCallbacks(colorRunnable_duringEditing);
        updateHandler.removeCallbacks(colorRunnable_duringScroll);
        updateHandler.postDelayed(colorRunnable_duringEditing, SYNTAX_DELAY_MILLIS_SHORT);
    }

    public String getCleanText() {
        return getText().toString();
    }

    /**
     * Gets the first lineInfo that is visible on the screen.
     */
    @SuppressWarnings("unused")
    public int getFirstLineIndex() {
        int scrollY;
        if (verticalScroll != null) {
            scrollY = verticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY);
        }
        return -1;
    }

    /**
     * Gets the last visible lineInfo number on the screen.
     *
     * @return last lineInfo that is visible on the screen.
     */
    public int getLastLineIndex() {
        int height;
        if (verticalScroll != null) {
            height = verticalScroll.getHeight();
        } else {
            height = getHeight();
        }
        int scrollY;
        if (verticalScroll != null) {
            scrollY = verticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY + height);
        }
        return -1;
    }

    private void highlightLineError(Editable e) {
        try {
//
        } catch (Exception ignored) {
        }
    }

    public void replaceAll(String what, String replace, boolean regex, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (matchCase) {
                pattern = Pattern.compile(Pattern.quote(what));
            } else {
                pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        }
        setText(getText().toString().replaceAll(pattern.toString(), replace));
    }

    /**
     * move cursor to lineInfo
     *
     * @param line - lineInfo in editor, start at 0
     */
    public void goToLine(int line) {
        Layout layout = getLayout();
        line = Math.min(line - 1, getLineCount() - 1);
        line = Math.max(0, line);
        if (layout != null) {
            int index = layout.getLineEnd(line);
            setSelection(index);
        }
    }

    /**
     * @param line   - current line
     * @param column - column of line
     * @return Position (in pixels) for edittext at line and column
     */
    public Point getDebugPosition(int line, int column, int gravity) {
        Layout layout = getLayout();
        if (layout != null) {
            int pos = layout.getLineStart(line) + column;

            int baseline = layout.getLineBaseline(line);
            int ascent = layout.getLineAscent(line);

            int offsetHorizontal = (int) layout.getPrimaryHorizontal(pos) + mLinePadding; //x

            float y;
            int offsetVertical = 0;

            if (gravity == Gravity.BOTTOM) {
                y = baseline + ascent;
                if (verticalScroll != null) {
                    offsetVertical = (int) ((y + mCharHeight) - verticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) ((y + mCharHeight) - getScrollY());
                }
                return new Point(offsetHorizontal, offsetVertical);
            } else if (gravity == Gravity.TOP) {
                y = layout.getLineTop(line);
                if (verticalScroll != null) {
                    offsetVertical = (int) (y - verticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) (y - getScrollY());
                }
                return new Point(offsetHorizontal, offsetVertical);
            }

            return new Point(offsetHorizontal, offsetVertical);
        }
        return new Point();
    }

    @Override
    public void onPopupChangePosition() {
        try {
            Layout layout = getLayout();
            if (layout != null) {
                int pos = getSelectionStart();
                int line = layout.getLineForOffset(pos);
                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);

                float x = layout.getPrimaryHorizontal(pos);
                float y = baseline + ascent;

                int offsetHorizontal = (int) x + mLinePadding;
                setDropDownHorizontalOffset(offsetHorizontal);

                int heightVisible = getHeightVisible();
                int offsetVertical = 0;
                if (verticalScroll != null) {
                    offsetVertical = (int) ((y + mCharHeight) - verticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) ((y + mCharHeight) - getScrollY());
                }

                int tmp = offsetVertical + getDropDownHeight() + mCharHeight;
                if (tmp < heightVisible) {
                    tmp = offsetVertical + mCharHeight / 2;
                    setDropDownVerticalOffset(tmp);
                } else {
                    tmp = offsetVertical - getDropDownHeight() - mCharHeight;
                    setDropDownVerticalOffset(tmp);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void setVerticalScroll(@Nullable ScrollView verticalScroll) {
        this.verticalScroll = verticalScroll;
    }

    /**
     * highlight find word
     *
     * @param what     - input
     * @param regex    - is java regex
     * @param wordOnly - find word only
     */
    public void find(String what, boolean regex, boolean wordOnly, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (wordOnly) {
                if (matchCase) {
                    pattern = Pattern.compile("\\s" + what + "\\s");
                } else {
                    pattern = Pattern.compile("\\s" + Pattern.quote(what) + "\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            } else {
                if (matchCase) {
                    pattern = Pattern.compile(Pattern.quote(what));
                } else {
                    pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            }
        }
        Editable e = getEditableText();
        //remove all span
        BackgroundColorSpan spans[] = e.getSpans(0, e.length(), BackgroundColorSpan.class);
        for (int n = spans.length; n-- > 0; )
            e.removeSpan(spans[n]);
        //set span

        for (Matcher m = pattern.matcher(e); m.find(); ) {
            e.setSpan(new BackgroundColorSpan(codeTheme.getErrorColor()),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void pinLine(@Nullable LineInfo lineInfo) {
//        Layout layout = getLayout();
//        Editable e = getEditableText();
//
//        if (lastPinLine < getLineCount() && lastPinLine >= 0) {
//            int lineStart = getLayout().getLineStart(lastPinLine);
//            int lineEnd = getLayout().getLineEnd(lastPinLine);
//            BackgroundColorSpan[] backgroundColorSpan = e.getSpans(lineStart, lineEnd,
//                    BackgroundColorSpan.class);
//            for (BackgroundColorSpan colorSpan : backgroundColorSpan) {
//                e.removeSpan(colorSpan);
//            }
//        }
//        if (lineInfo == null) return;
//        if (layout != null && lineInfo.getLine() < getLineCount()) {
//            try {
//                int lineStart = getLayout().getLineStart(lineInfo.getLine());
//                int lineEnd = getLayout().getLineEnd(lineInfo.getLine());
//                lineStart += lineInfo.getColumn();
//
//                //normalize
//                lineStart = Math.max(0, lineStart);
//                lineEnd = Math.min(lineEnd, getText().length());
//
//                if (lineStart < lineEnd) {
//                    e.setSpan(new BackgroundColorSpan(codeTheme.getErrorColor()),
//                            lineStart,
//                            lineEnd,
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                }
//                lastPinLine = lineInfo.getLine();
//            } catch (Exception ignored) {
//            }
//        }
    }

    public void highlightText() {
        if (isFinding) return;
        if (length() > 262144){
            return;
        }
        disableTextChangedListener();
        highlight(false);
        enableTextChangedListener();
    }

    /**
     * remove span from start to end
     */
    private void clearSpans(Editable e, int start, int end) {
        {
            ForegroundColorSpan spans[] = e.getSpans(start, end, ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            BackgroundColorSpan spans[] = e.getSpans(start, end, BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            StyleSpan[] spans = e.getSpans(start, end, StyleSpan.class);
            for (StyleSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            UnderlineSpan[] spans = e.getSpans(start, end, UnderlineSpan.class);
            for (UnderlineSpan span : spans) {
                e.removeSpan(span);
            }
        }
    }

    public void highlight(boolean newText) {
        if (mHighlighter == null) return;
        Editable editable = getText();
        if (editable.length() == 0) return;

        int editorHeight = getHeightVisible();

        int firstVisibleIndex;
        int lastVisibleIndex;
        if (!newText && editorHeight > 0) {
            if (verticalScroll != null && getLayout() != null) {
                firstVisibleIndex = getLayout().getLineStart(Math.max(0, getFirstLineIndex() - 3));
            } else {
                firstVisibleIndex = 0;
            }
            if (verticalScroll != null && getLayout() != null) {
                lastVisibleIndex = getLayout().getLineStart(Math.min(getLayout().getLineCount() - 1, getLastLineIndex() + 3));
            } else {
                lastVisibleIndex = getText().length();
            }
        } else {
            firstVisibleIndex = 0;
            lastVisibleIndex = CHARS_TO_COLOR;
        }
        // normalize
        if (firstVisibleIndex < 0) firstVisibleIndex = 0;
        if (lastVisibleIndex > editable.length()) lastVisibleIndex = editable.length();
        if (firstVisibleIndex > lastVisibleIndex) firstVisibleIndex = lastVisibleIndex;

        //clear all span for firstVisibleIndex to lastVisibleIndex
        clearSpans(editable, firstVisibleIndex, lastVisibleIndex);

        CharSequence textToHighlight = editable.subSequence(firstVisibleIndex, lastVisibleIndex);
        mHighlighter.highlight(editable, textToHighlight, firstVisibleIndex);
        applyTabWidth(editable, firstVisibleIndex, lastVisibleIndex);
    }

    public void enableTextChangedListener() {
        if (!enabledChangeListener) {
            addTextChangedListener(mChangeListener);
            enabledChangeListener = true;
        }
    }

    public void disableTextChangedListener() {
        enabledChangeListener = false;
        removeTextChangedListener(mChangeListener);
    }

    public void updateTextHighlight() {
        if (hasSelection() || updateHandler == null)
            return;
        updateHandler.removeCallbacks(colorRunnable_duringEditing);
        updateHandler.removeCallbacks(colorRunnable_duringScroll);
        updateHandler.postDelayed(colorRunnable_duringEditing, SYNTAX_DELAY_MILLIS_LONG);
    }

    public void showKeyboard() {
        requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    public void highlightError(long startPosition, long endPosition) {
        if (mHighlighter == null) return;
        mHighlighter.setErrorRange(startPosition, endPosition);
        refresh();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mBracketHighlighter != null) {
            mBracketHighlighter.onSelectChange(selStart, selEnd);
        }
    }

    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener
            implements TextWatcher {

        public void beforeTextChanged(
                CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before,
                                  int count) {
            isFinding = false;
            if (mHighlighter != null) {
                mHighlighter.setErrorRange(-1, -1);
            }
        }

        public void afterTextChanged(Editable s) {
            updateTextHighlight();

            if (!autoCompile) {
                lineError = null;
            }

        }
    }
}
