package com.pluscubed.logcat.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.helper.PackageHelper;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutDialogActivity extends AppCompatActivity {

    private static UtilLogger log = new UtilLogger(AboutDialogActivity.class);


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogFragment fragment = new AboutDialog();
        fragment.show(getFragmentManager(), "aboutDialog");

    }

    public static class AboutDialog extends DialogFragment {

        private Handler handler = new Handler();

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            getActivity().finish();
        }


        public void initializeWebView(WebView view) {

            String text = loadTextFile(R.raw.about_body);
            String version = PackageHelper.getVersionName(getActivity());
            String changelog = loadTextFile(R.raw.changelog);
            String css = loadTextFile(R.raw.about_css);
            text = String.format(text, version, changelog, css);

            WebSettings settings = view.getSettings();
            settings.setDefaultTextEncodingName("utf-8");

            view.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        }

        private String loadTextFile(int resourceId) {

            InputStream is = getResources().openRawResource(resourceId);

            BufferedReader buff = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            try {
                while (buff.ready()) {
                    sb.append(buff.readLine()).append("\n");
                }
            } catch (IOException e) {
                log.e(e, "This should not happen");
            }

            return sb.toString();

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView view = new WebView(getActivity());
/*
            view.setWebViewClient(new AboutWebClient());*/
            initializeWebView(view);

            return new MaterialDialog.Builder(getActivity())
                    .customView(view, false)
                    .title(R.string.about_matlog)
                    .iconRes(R.mipmap.ic_launcher)
                    .positiveText(android.R.string.ok)
                    .build();
        }


        /*private void loadExternalUrl(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(url));

            startActivity(intent);
        }*/

        /*private class AboutWebClient extends WebViewClient {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                log.d("shouldOverrideUrlLoading");

                // XXX hack to make the webview go to an external url if the hyperlink is
                // in my own HTML file - otherwise it says "Page not available" because I'm not calling
                // loadDataWithBaseURL.  But if I call loadDataWithBaseUrl using a fake URL, then
                // the links within the page itself don't work!!  Arggggh!!!

                if (url.startsWith("http") || url.startsWith("mailto") || url.startsWith("market")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadExternalUrl(url);
                        }
                    });
                    return true;
                }
                return false;
            }
        }*/
    }
}
