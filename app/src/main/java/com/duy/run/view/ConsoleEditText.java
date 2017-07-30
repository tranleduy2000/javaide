package com.duy.run.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.duy.ide.setting.JavaPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ConsoleEditText extends AppCompatEditText {
    private ConsoleOutputStream outputStream;
    private ConsoleInputStream inputStream;
    private ConsoleErrorStream errorStream;

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
        inputStream = new ConsoleInputStream();
        outputStream = new ConsoleOutputStream();
        errorStream = new ConsoleErrorStream();
    }

    public ConsoleOutputStream getOutputStream() {
        return outputStream;
    }

    public ConsoleInputStream getInputStream() {
        return inputStream;
    }

    public ConsoleErrorStream getErrorStream() {
        return errorStream;
    }

    public void print(String out) {
        append(out);
    }

    public void println(String out) {
        append(out);
        append("\n");
    }

    private class ConsoleOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            append(new String(Character.toChars(b)));
        }
    }

    private class ConsoleErrorStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            String text = new String(Character.toChars(b));
            append(text);
        }
    }

    private class ConsoleInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return 0;
        }
    }
}
