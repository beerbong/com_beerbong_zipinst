/*
 * Copyright (C) 2013 ZipInstaller
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

package com.beerbong.zipinst.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class URLStringReader extends AsyncTask<String, Void, Void> {

    public static interface URLStringReaderListener {

        public void onReadEnd(String buffer);
        public void onReadError(Exception ex);
    };

    private URLStringReaderListener mListener;
    
    public URLStringReader(URLStringReaderListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            String buffer = readString(params[0]);
            if (mListener != null) {
                mListener.onReadEnd(buffer);
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (mListener != null) {
                mListener.onReadError(ex);
            }
        }
        return null;
    }

    private String readString(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
        } finally {
            if (in != null)
                in.close();
        }
        return sb.toString();
    }
}