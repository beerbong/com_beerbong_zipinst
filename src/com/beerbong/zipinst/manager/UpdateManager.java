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

package com.beerbong.zipinst.manager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.DownloadTask;
import com.beerbong.zipinst.util.URLStringReader;
import com.beerbong.zipinst.util.URLStringReader.URLStringReaderListener;

public class UpdateManager extends Manager implements URLStringReaderListener {

    private int mVersion = -1;

    public UpdateManager(Context context) {
        super(context);

        try {
            mVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, R.string.check_for_updates_error, Toast.LENGTH_LONG).show();
        }

        if (!ManagerFactory.getProManager().iAmPro() && ManagerFactory.getPreferencesManager().isCheckUpdatesStartup()) {
            checkForUpdate(mContext);
        }
    }

    public void checkForUpdate(Context context) {
        if (mVersion == -1) {
            return;
        }
        new URLStringReader(this).execute(Constants.SEARCH_URL);
    }

    @Override
    public void onReadEnd(String buffer) {
        try {
            JSONObject object = (JSONObject) new JSONTokener(buffer).nextValue();
            JSONArray results = object.getJSONArray("search_result");
            int newVersion = -1;
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String path = result.getString("path");
                String fileName = result.getString("filename");
                if (path.indexOf("beerbong") < 0 || fileName.indexOf("ZipInstaller") < 0)
                    continue;
                newVersion = Math.max(newVersion, parseVersion(fileName));
            }
            if (mVersion >= newVersion) {
                showToastOnUiThread(R.string.no_new_version);
            } else {
                final int nVersion = newVersion;
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    public void run() {
                        requestForDownload(mContext, nVersion);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showToastOnUiThread(R.string.check_for_updates_error);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        showToastOnUiThread(R.string.check_for_updates_error);
    }

    private void requestForDownload(Context context, int version) {
        final String fileName = formatVersion(version);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.new_version_title);
        alert.setMessage(mContext.getResources().getString(R.string.new_version_summary,
                new Object[] { fileName }));
        alert.setPositiveButton(R.string.new_version_download,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        download(fileName);
                    }
                });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void download(String fileName) {

        final ProgressDialog progressDialog = new ProgressDialog(mContext);

        final DownloadTask downloadFile = new DownloadTask(progressDialog, Constants.DOWNLOAD_URL
                + fileName, fileName);

        progressDialog.setMessage(mContext.getResources()
                .getString(
                        R.string.downloading,
                        new Object[] {
                                fileName,
                                ManagerFactory.getPreferencesManager().getDownloadPath() }));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setButton(Dialog.BUTTON_NEGATIVE,
                mContext.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                        downloadFile.cancel(true);
                    }
                });

        downloadFile.attach(progressDialog);
        progressDialog.show();
        downloadFile.execute();
    }

    private void showToastOnUiThread(final int resourceId) {
        ((Activity) mContext).runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(mContext, resourceId, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int parseVersion(String fileName) {
        String v = fileName.replace("ZipInstaller-", "");
        v = v.replace(".apk", "");
        v = v.replace(".", "");
        return Integer.parseInt(v);
    }

    private String formatVersion(int versionNumber) {
        String version = String.valueOf(versionNumber);
        return "ZipInstaller-" + version.charAt(0) + "." + version.charAt(1) + "."
                + version.charAt(2) + ".apk";
    }
}