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

package com.beerbong.zipinst.http;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;


public class DownloadFile {

    public DownloadFile(Core core, String url, String fileName, String md5) {
        this(core, url, fileName, md5, null);
    }

    public DownloadFile(Core core, String url, String fileName, String md5, DownloadTask.OnDownloadFinishListener listener) {

        Context context = core.getContext();

        final ProgressDialog progressDialog = new ProgressDialog(context);

        if (fileName == null) {
            fileName = url.substring(url.lastIndexOf("/") + 1);
            if (fileName.indexOf("?") >= 0) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            if (!fileName.contains(".zip") && !fileName.contains(".apk")) {
                fileName = "unknown-file-name.zip";
            }
        }

        final DownloadTask downloadFile = new DownloadTask(core, progressDialog, url, fileName, md5, listener);

        progressDialog.setTitle(R.string.download);
        progressDialog.setMessage(context.getResources().getString(R.string.downloading,
                new Object[] { url, core.getPreferences().getDownloadPath() }));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setButton(Dialog.BUTTON_NEGATIVE,
                context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFile.detach();
                        downloadFile.cancel(true);
                    }
                });

        downloadFile.attach(progressDialog);
        progressDialog.show();
        downloadFile.execute();
    }
}
