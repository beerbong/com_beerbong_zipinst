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

package com.beerbong.zipinst.core.plugins.update;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.core.plugins.license.LicensePlugin;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.http.URLStringReader;
import com.beerbong.zipinst.http.URLStringReader.URLStringReaderListener;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.io.Version;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;

public class UpdatePlugin extends Plugin implements URLStringReaderListener {

    private static final String SEARCH_URL = "http://goo.im/json2&action=search&query=ZipInstaller";
    private static final String DOWNLOAD_URL = "http://goo.im";

    private Version mNewVersion;
    private boolean mIsStartup = true;

    public UpdatePlugin(Core core) {
        super(core, Core.PLUGIN_UPDATE);
    }

    @Override
    public void start() {
        LicensePlugin plugin = (LicensePlugin) getCore().getPlugin(Core.PLUGIN_LICENSE);
        if (!plugin.isPurchased() && getCore().getPreferences().isCheckUpdatesStartup()) {
            checkApplication();
        } else {
            mIsStartup = false;
        }

        if (!SystemProperties.alarmExists(getCore().getContext())) {
            SystemProperties.setAlarm(getCore().getContext(), getCore().getPreferences()
                    .getTimeNotifications(), true);
        }

        started();
    }

    @Override
    public void stop() {
        stopped();
    }

    public void checkApplication() {
        Version version = getCore().getVersion();
        mNewVersion = null;
        if (version.isEmpty()) {
            return;
        }
        new URLStringReader(this).execute(SEARCH_URL);
    }

    @Override
    public void onReadEnd(String buffer) {
        try {
            JSONObject object = (JSONObject) new JSONTokener(buffer).nextValue();
            JSONArray results = object.getJSONArray("search_result");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String path = result.getString("path");
                String fileName = result.getString("filename");
                if (path.indexOf("beerbong") < 0 || fileName.indexOf("ZipInstaller") < 0
                        || !fileName.endsWith(".apk")) {
                    continue;
                }
                Version version = new Version(fileName, DOWNLOAD_URL + path,
                        result.getString("md5"));
                mNewVersion = mNewVersion == null ? version : Version.max(mNewVersion, version);
            }
            Version version = getCore().getVersion();
            if (mNewVersion == null || Version.compare(version, mNewVersion) >= 0) {
                mNewVersion = null;
                if (!mIsStartup) {
                    Dialog.toast(getCore().getContext(), R.string.no_new_version);
                }
            } else {
                requestForDownload();
            }
            mIsStartup = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Dialog.toast(getCore().getContext(), R.string.check_for_updates_error);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        Dialog.toast(getCore().getContext(), R.string.check_for_updates_error);
    }

    private void requestForDownload() {
        Context context = getCore().getContext();
        String message = context.getResources().getString(R.string.new_version_summary,
                new Object[] { mNewVersion.getFileName() });
        Dialog.dialog(context, message, R.string.new_version_title, true,
                R.string.new_version_download, android.R.string.cancel,
                new OnDialogClosedListener() {

                    @Override
                    public void dialogOk() {
                        new DownloadFile(getCore(), mNewVersion.getFilePath(), mNewVersion
                                .getFileName(), mNewVersion.getFileMd5());
                    }

                    @Override
                    public void dialogCancel() {
                    }

                });
    }

}
