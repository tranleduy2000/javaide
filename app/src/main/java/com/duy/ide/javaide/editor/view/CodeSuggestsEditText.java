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

package com.duy.ide.javaide.editor.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

import com.duy.ide.R;
import com.duy.ide.editor.view.EditActionSupportEditor;
import com.duy.ide.java.DLog;
import com.duy.ide.java.EditorSetting;
import com.duy.ide.javaide.editor.view.adapters.CodeSuggestAdapter;
import com.duy.ide.javaide.editor.autocomplete.JavaAutoCompleteProvider;
import com.duy.ide.javaide.editor.autocomplete.model.Description;

import java.util.ArrayList;

/**
 * AutoSuggestsEditText
 * show hint when typing
 * Created by Duy on 28-Feb-17.
 */

public class CodeSuggestsEditText extends EditActionSupportEditor
        implements CodeSuggestAdapter.OnSuggestItemClickListener {

    protected static final String TAG = CodeSuggestsEditText.class.getSimpleName();
    public int mCharHeight = 0;
    protected EditorSetting mEditorSetting;
    protected SymbolsTokenizer mTokenizer;
    private CodeSuggestAdapter mAdapter;
    @Nullable
    private JavaAutoCompleteProvider mAutoCompleteProvider;
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAdapter != null) {
                Description description = mAdapter.getAllItems().get(position);
                onClickSuggest(description);
            }
        }
    };
    private GenerateSuggestDataTask mGenerateSuggestDataTask = null;

    public CodeSuggestsEditText(Context context) {
        super(context);
        init(context);
    }

    public CodeSuggestsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CodeSuggestsEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * slipt string in edittext and put it to list keyword
     */
    public void setDefaultKeyword() {
//        ArrayList<InfoItem> data = new ArrayList<>();
//        for (String s : KeyWord.RESERVED_KEY_WORDS) {
//            data.add(new InfoItem(StructureType.TYPE_KEY_WORD, s));
//        }
//        setSuggestData(data);
    }

    /**
     * @return the height of view display on screen
     */
    public int getHeightVisible() {
        Rect r = new Rect();
        // r will be populated with the coordinates of     your view
        // that area still visible.
        getWindowVisibleDisplayFrame(r);
        return r.bottom - r.top;
    }

    private void invalidateCharHeight() {
        mCharHeight = (int) Math.ceil(getPaint().getFontSpacing());
        mCharHeight = (int) getPaint().measureText("M");
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mAutoCompleteProvider != null) {
            if (mGenerateSuggestDataTask != null) {
                mGenerateSuggestDataTask.cancel(true);
            }
            mGenerateSuggestDataTask = new GenerateSuggestDataTask(this, mAutoCompleteProvider);
            mGenerateSuggestDataTask.execute();
        }
        onPopupChangePosition();
    }

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

                int offsetHorizontal = (int) x + getPaddingLeft();
                setDropDownHorizontalOffset(offsetHorizontal);

                int heightVisible = getHeightVisible();
                int offsetVertical = (int) ((y + mCharHeight) - getScrollY());

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        DLog.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" +
                oldw + "], oldh = [" + oldh + "]");
        onDropdownChangeSize();
    }

    /**
     * this method will be change size of popup window
     */
    protected void onDropdownChangeSize() {

        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);

        Log.d(TAG, "onDropdownChangeSize: " + rect);
        int w = rect.width();
        int h = rect.height();
        // 1/2 width of screen
        setDropDownWidth((int) (w * 0.6f));

        // 0.5 height of screen
        setDropDownHeight((int) (h * 0.5f));

        //change position
        onPopupChangePosition();
    }


    @Override
    public void showDropDown() {
        if (!isPopupShowing()) {
            if (hasFocus()) {
                try {
                    super.showDropDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean enoughToFilter() {
        return mAdapter != null;
    }

    public void restoreAfterClick(final String[] data) {
//        final ArrayList<InfoItem> suggestData = (ArrayList<InfoItem>) getSuggestData().clone();
//        setSuggestData(data);
//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setEnoughToFilter(true);
//                showDropDown();
//                setEnoughToFilter(false);
//
//                //when user click item, restore data
//                setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        setSuggestData(suggestData);
//
//                        //don't handle twice
//                        setOnItemClickListener(null);
//                    }
//                });
//
//            }
//        }, 50);
    }

    public ArrayList<Description> getSuggestData() {
        return mAdapter.getAllItems();
    }

    /**
     * invalidate data for auto suggest
     */
    public void setSuggestData(ArrayList<Description> data) {
        Log.d(TAG, "setSuggestData() called with: data = [" + data.size() + "]");

        if (mAdapter != null) {
            mAdapter.clearAllData(); //gc
            mAdapter.setListener(null);
            if (isPopupShowing()) {
                dismissDropDown();
            }
        }
        mAdapter = new CodeSuggestAdapter(getContext(), R.layout.list_item_suggest, data);
        mAdapter.setListener(this);

        setAdapter(mAdapter);
        if (data.size() > 0) {
            onDropdownChangeSize();
            showDropDown();
        }
    }

    public void setAutoCompleteProvider(JavaAutoCompleteProvider autoCompleteProvider) {
        this.mAutoCompleteProvider = autoCompleteProvider;
    }

    private void init(Context context) {
        mEditorSetting = new EditorSetting(getContext());
        setDefaultKeyword();
        mTokenizer = new SymbolsTokenizer();
        setTokenizer(mTokenizer);
        invalidateCharHeight();

        setDropDownBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_suggest));
        setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onClickSuggest(Description description) {
        Log.d(TAG, "onClickSuggest() called with: description = [" + description + "]");
        if (mAutoCompleteProvider != null) {
            mAutoCompleteProvider.onInsertSuggestion(getText(), description);
        }
    }


    private class GenerateSuggestDataTask extends AsyncTask<Void, Void, ArrayList<Description>> {
        private final EditText editText;
        private final JavaAutoCompleteProvider provider;

        private GenerateSuggestDataTask(@NonNull EditText editText, @NonNull JavaAutoCompleteProvider provider) {
            this.editText = editText;
            this.provider = provider;
            provider.getClass();
            editText.getClass();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Description> doInBackground(Void... params) {
            try {
                return provider.getSuggestions(editText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Description> descriptions) {
            super.onPostExecute(descriptions);
            if (isCancelled()) {
                Log.d(TAG, "onPostExecute: canceled");
                return;
            }
            if (descriptions == null) {
                setSuggestData(new ArrayList<Description>());
            } else {
                setSuggestData(descriptions);
            }
        }
    }


    private class SymbolsTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        static final String TOKEN = "!@#$%^&*()_+-={}|[]:;'<>/<? .\r\n\t";

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && !TOKEN.contains(Character.toString(text.charAt(i - 1)))) {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (TOKEN.contains(Character.toString(text.charAt(i - 1)))) {
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            return text;
        }
    }
}

