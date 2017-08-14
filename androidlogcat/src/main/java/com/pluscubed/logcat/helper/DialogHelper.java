package com.pluscubed.logcat.helper;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.R;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DialogHelper {

    public static boolean isInvalidFilename(CharSequence filename) {

        String filenameAsString;

        return TextUtils.isEmpty(filename)
                || (filenameAsString = filename.toString()).contains("/")
                || filenameAsString.contains(":")
                || filenameAsString.contains(" ")
                || !filenameAsString.endsWith(".txt");

    }


    public static void showFilenameSuggestingDialog(final Context context,
                                                    final MaterialDialog.SingleButtonCallback callback, final MaterialDialog.InputCallback inputCallback, int titleResId) {


        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(titleResId)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .content(R.string.enter_filename)
                .input("", "", inputCallback)
                .onAny(callback);

        MaterialDialog show = builder.show();
        initFilenameInputDialog(show);
    }

    public static void initFilenameInputDialog(MaterialDialog show) {
        final EditText editText = show.getInputEditText();
        editText.setSingleLine();
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // create an initial filename to suggest to the user
        String filename = createLogFilename();
        editText.setText(filename);

        // highlight everything but the .txt at the end
        editText.setSelection(0, filename.length() - 4);
    }

    public static String createLogFilename() {
        Date date = new Date();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("00");
        DecimalFormat fourDigitDecimalFormat = new DecimalFormat("0000");

        String year = fourDigitDecimalFormat.format(calendar.get(Calendar.YEAR));
        String month = twoDigitDecimalFormat.format(calendar.get(Calendar.MONTH) + 1);
        String day = twoDigitDecimalFormat.format(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = twoDigitDecimalFormat.format(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = twoDigitDecimalFormat.format(calendar.get(Calendar.MINUTE));
        String second = twoDigitDecimalFormat.format(calendar.get(Calendar.SECOND));

        return year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second + ".txt";
    }

}
