package com.duy.run.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;

import com.duy.ide.setting.JavaPreferences;
import com.duy.run.utils.ByteQueue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ConsoleEditText extends AppCompatEditText {
    private static final String TAG = "ConsoleEditText";


    //output from program
    private AtomicBoolean fromConsole = new AtomicBoolean(false);

    //length of text
    private int mLength = 0;

    //out, in and err stream
    private ConsoleOutputStream outputStream;
    private InputStream inputStream;
    private ConsoleErrorStream errorStream;

    /**
     * uses for input
     */
    private ByteQueue mInputBuffer = new ByteQueue(ByteQueue.QUEUE_SIZE);

    /**
     * buffer for output
     */
    private ByteQueue mOutputBuffer = new ByteQueue(ByteQueue.QUEUE_SIZE);


    //filter input text, block a part of text
    private TextListener mTextListener = new TextListener();
    private EnterListener mEnterListener = new EnterListener();
    private Handler mHandler = new Handler();
    private Thread mOutputThread;

    public ConsoleEditText(Context context) {
        super(context);
        init(context);
    }

    public ConsoleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public ConsoleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode()) {
            JavaPreferences pref = new JavaPreferences(context);
            setTypeface(pref.getConsoleFont());
            setTextSize(pref.getConsoleTextSize());
        }
        setFilters(new InputFilter[]{mTextListener});
        setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        addTextChangedListener(mEnterListener);

        createIOStream();
    }

    private void createIOStream() {
        inputStream = new ConsoleInputStream();
        outputStream = new ConsoleOutputStream();
        errorStream = new ConsoleErrorStream();

        mOutputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int read = mOutputBuffer.read();
                    if (read == -1) {
                        return;
                    }
                    String out = new String(Character.toChars(read));
                    mLength = mLength + out.length();
                    appendStr(out);
                }
            }
        });
        mOutputThread.setName("Output reader thread");
        mOutputThread.start();
    }

    public ConsoleOutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public ConsoleErrorStream getErrorStream() {
        return errorStream;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @UiThread
    private void appendStr(final CharSequence spannableString) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                append(spannableString);
            }
        });
    }

    private class EnterListener implements TextWatcher {

        private CharSequence s;
        private int start;
        private int before;
        private int count;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged() called with: s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");

            this.s = s;
            this.start = start;
            this.before = before;
            this.count = count;
        }

        @Override
        public void afterTextChanged(Editable s) {
//            if (count == 1 && s.charAt(start) == '\n') {
//                String data = s.toString().substring(mLength);
//                Log.d(TAG, "afterTextChanged data = " + data);
//                for (char c : data.toCharArray()) {
//                    mByteQueue.write(c);
//                }
////                mByteQueue.flush();
//                mByteQueue.write(-1); //flush
//                mLength = s.length();
//            }
        }
    }

    private class ConsoleOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            mOutputBuffer.write(b);
        }
    }

    private class ConsoleErrorStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            mOutputBuffer.write(b);
//            String text = new String(Character.toChars(b));
//            SpannableString spannableString = new SpannableString(text);
//            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(),
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            mLength = length() + spannableString.length();
//            fromConsole.set(true);
//            appendStr(spannableString);
//            fromConsole.set(false);
        }
    }

    private class ConsoleInputStream extends InputStream {
        private final Object mLock = new Object();

        @Override
        public int read() throws IOException {
            synchronized (mLock) {
                return mInputBuffer.read();
            }
        }
    }

    private class TextListener implements InputFilter {
        public CharSequence removeStr(CharSequence removeChars, int startPos) {
            Log.d(TAG, "removeStr() called with: removeChars = [" + removeChars + "], startPos = [" + startPos + "]");
            if (startPos < mLength) { //this mean output from console
                return ""; //can not remove console output
            } else {
                return removeChars;
            }
        }

        public CharSequence insertStr(CharSequence newChars, int startPos) {
            Log.d(TAG, "insertStr() called with: newChars = [" + newChars + "], startPos = [" + startPos + "]");
            if (startPos < mLength) { //it mean output from console
                return newChars;

            } else { //(startPos >= mLength)
                SpannableString spannableString = new SpannableString(newChars);
                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
                        spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;

            }
        }

        public CharSequence updateStr(CharSequence oldChars, int startPos, CharSequence newChars) {
            Log.d(TAG, "updateStr() called with: oldChars = [" + oldChars + "], startPos = [" + startPos + "], newChars = [" + newChars + "]");
            if (startPos < mLength) {
                return newChars;
            } else if (startPos >= mLength) {
                SpannableString spannableString = new SpannableString(newChars);
                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
                        spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            } else {
                return "";
            }
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence returnStr = source;
            String curStr = dest.subSequence(dstart, dend).toString();
            String newStr = source.toString();
            int length = end - start;
            int dlength = dend - dstart;
            if (dlength > 0 && length == 0) {
                // Case: Remove chars, Simple
                returnStr = TextListener.this.removeStr(dest.subSequence(dstart, dend), dstart);
            } else if (length > 0 && dlength == 0) {
                // Case: Insert chars, Simple
                returnStr = TextListener.this.insertStr(source.subSequence(start, end), dstart);
            } else if (curStr.length() > newStr.length()) {
                // Case: Remove string or replace
                if (curStr.startsWith(newStr)) {
                    // Case: Insert chars, by append
                    returnStr = TextUtils.concat(curStr.subSequence(0, newStr.length()), TextListener.this.removeStr(curStr.subSequence(newStr.length(), curStr.length()), dstart + curStr.length()));
                } else {
                    // Case Replace chars.
                    returnStr = TextListener.this.updateStr(curStr, dstart, newStr);
                }
            } else if (curStr.length() < newStr.length()) {
                // Case: Append String or rrepace.
                if (newStr.startsWith(curStr)) {
                    // Addend, Insert
                    returnStr = TextUtils.concat(curStr, TextListener.this.insertStr(newStr.subSequence(curStr.length(), newStr.length()), dstart + curStr.length()));
                } else {
                    returnStr = TextListener.this.updateStr(curStr, dstart, newStr);
                }
            } else {
                // No update os str...
            }

            // If the return value is same as the source values, return the source value.
            return returnStr;
        }
    }
}
