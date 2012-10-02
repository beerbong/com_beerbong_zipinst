package com.beerbong.zipinst.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.beerbong.zipinst.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class License extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BufferedReader in = null;
        StringBuilder data = null;
        try {
            data = new StringBuilder(2048);
            char[] buf = new char[2048];
            int nRead = -1;
            in = new BufferedReader(new InputStreamReader(getAssets().open("license.html")));
            while ((nRead = in.read(buf)) != -1) {
                data.append(buf, 0, nRead);
            }
        } catch (IOException e) {
            showErrorAndFinish();
            return;
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException e) {}
            }
        }

        if (TextUtils.isEmpty(data)) {
            showErrorAndFinish();
            return;
        }

        WebView webView = new WebView(this);

        // Begin the loading.  This will be done in a separate thread in WebView.
        webView.loadDataWithBaseURL(null, data.toString(), "text/html", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                setContentView(view);
            }
        });

    }

    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.license_error, Toast.LENGTH_LONG).show();
        finish();
    }
}