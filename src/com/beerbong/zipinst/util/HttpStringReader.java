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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class HttpStringReader extends AsyncTask<String, Void, Void> {

    public static interface HttpStringReaderListener {

        public void onReadEnd(String buffer);
        public void onReadError(Exception ex);
    };

    private HttpStringReaderListener mListener;
    
    public HttpStringReader(HttpStringReaderListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(urls[0]);
            HttpResponse r = client.execute(get);
            int status = r.getStatusLine().getStatusCode();
            HttpEntity e = r.getEntity();
            if (status == 200) {
                mListener.onReadEnd(EntityUtils.toString(e));
            } else {
                if (e != null) e.consumeContent();
                String error = "Server responded with error " + status;
                mListener.onReadError(new Exception(error));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mListener.onReadError(ex);
        }
        return null;
    }
}