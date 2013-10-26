/*
 * Copyright 2013 ZipInstaller Project
 *
 * This file is part of ZipInstaller.
 *
 * ZipInstaller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZipInstaller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZipInstaller.  If not, see <http://www.gnu.org/licenses/>.
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