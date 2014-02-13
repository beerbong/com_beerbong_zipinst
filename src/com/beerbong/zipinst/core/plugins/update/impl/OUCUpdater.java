/*
 * Copyright 2014 ZipInstaller Project
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

package com.beerbong.zipinst.core.plugins.update.impl;

import java.util.ArrayList;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.beerbong.zipinst.core.plugins.update.Updater;
import com.beerbong.zipinst.http.HttpStringReader;
import com.beerbong.zipinst.io.SystemProperties;

public class OUCUpdater implements Updater {

    public static final String URL = "https://www.otaupdatecenter.pro/pages/romupdate.php";
    public static final String PROPERTY_OTA_ID = "otaupdater.otaid";
    public static final String PROPERTY_OTA_VER = "otaupdater.otaver";
    public static final String PROPERTY_OTA_TIME = "otaupdater.otatime";

    private UpdaterListener mListener;
    private boolean mScanning = false;

    public OUCUpdater(UpdaterListener listener) {
        mListener = listener;
    }

    @Override
    public String getRomName() {
         return SystemProperties.getProperty(PROPERTY_OTA_ID);
    }

    @Override
    public int getRomVersion() {
        String version = SystemProperties.getProperty(PROPERTY_OTA_TIME);
        if (version != null) {
            try {
                version = version.replace("-", "");
                return Integer.parseInt(version);
            } catch (NumberFormatException ex) {
            }
        }
        return -1;
    }

    @Override
    public void onReadEnd(String buffer) {
        mScanning = false;
        try {
            JSONObject json = new JSONObject(buffer);

            if (json.has("error")) {
                String error = json.getString("error");
                mListener.versionError(error);
                return;
            }

            String date = json.getString("date");
            date = date.replace("-", "");

            RomInfo info = new RomInfo();
            info.md5 = json.getString("md5");
            info.version = Long.parseLong(date);
            info.path = json.getString("url");
            info.filename = json.getString("rom") + "-" + json.getString("date") + ".zip";

            if (getRomVersion() < info.version) {
                mListener.versionFound(info);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mListener.versionError(null);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        mListener.versionError(null);
    }

    @Override
    public void searchVersion() {
        mScanning = true;
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("device", android.os.Build.DEVICE.toLowerCase()));
        params.add(new BasicNameValuePair("rom", getRomName()));
        new HttpStringReader(this).execute(URL + "?" + URLEncodedUtils.format(params, "UTF-8"));
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

}