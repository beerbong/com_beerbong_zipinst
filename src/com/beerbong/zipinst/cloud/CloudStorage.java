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

package com.beerbong.zipinst.cloud;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.Zip;
import com.beerbong.zipinst.io.Zip.ZipCallback;
import com.beerbong.zipinst.ui.widget.Dialog;

public abstract class CloudStorage {

    public static final int STORAGE_NONE = 0;
    public static final int STORAGE_DROPBOX = 1;
    public static final int STORAGE_DRIVE = 2;

    public interface CloudStorageListener {

        public void cloudStorageLoading();

        public void cloudStorageLoaded();

        public void cloudDownloadComplete();
    }

    private int mType = STORAGE_NONE;
    private Core mCore;
    private List<CloudEntry> mEntries;
    private CloudStorageListener mListener;

    private ProgressDialog mUploadDialog;
    private ProgressDialog mDownloadDialog;
    private boolean mDownloadCancelled;
    private boolean mUploadCancelled;

    public CloudStorage(Core core, int type) {
        mCore = core;
        mType = type;
    }

    protected Core getCore() {
        return mCore;
    }

    public int getType() {
        return mType;
    }

    public void setCloudStorageListener(CloudStorageListener listener) {
        mListener = listener;
    }

    protected abstract List<CloudEntry> getCloudEntries();

    public abstract int getCloudIcon();

    public abstract int getName();

    protected abstract boolean delete(CloudEntry toDelete);

    protected abstract boolean download(CloudEntry entry, File file);

    protected abstract void cancelDownload();

    protected abstract void cancelUpload();

    protected abstract void upload();

    public List<CloudEntry> getEntries() {
        return mEntries;
    }

    public void refresh() {
        mListener.cloudStorageLoading();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                mEntries = getCloudEntries();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                mListener.cloudStorageLoaded();
            }

        }.execute((Void) null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    protected boolean isUploadCancelled()  {
        return mUploadCancelled || !mUploadDialog.isShowing();
    }

    protected void setUploadProgress(int value) {
        mUploadDialog.setProgress(value);
    }

    protected void upload(final String path) {

        mUploadCancelled = false;
        mUploadDialog = new ProgressDialog(mCore.getContext());

        final File file = new File(path);

        Resources res = mCore.getContext().getResources();

        mUploadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUploadDialog.setMessage(res.getString(R.string.cloud_backup_uploading));
        mUploadDialog.setCancelable(false);
        mUploadDialog.setCanceledOnTouchOutside(false);
        mUploadDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                res.getString(android.R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                mUploadCancelled = true;
                                cancelUpload();
                                file.delete();
                                mUploadDialog.dismiss();

                                return null;
                            }

                        }.execute((Void) null);
                    }

                });
        mUploadDialog.show();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    try {
                        upload();
                    } finally {
                        file.delete();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Dialog.toast(mCore.getContext(), R.string.cloud_backup_error_uploading);
                }
                mUploadDialog.dismiss();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                refresh();
            }

        }.execute((Void) null);
    }

    public void zipIt(final String folder) {

        final Zip zip = new Zip();

        Resources res = mCore.getContext().getResources();

        final ProgressDialog pDialog = new ProgressDialog(mCore.getContext());
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(res.getString(R.string.cloud_backup_zipping));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                res.getString(android.R.string.cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        zip.cancel();
                        pDialog.dismiss();
                    }

                });
        pDialog.show();

        new AsyncTask<Void, Void, Void>() {

            private String path;
            private boolean resultOk = false;

            @Override
            protected Void doInBackground(Void... params) {

                final File file = new File(folder);
                path = "/sdcard/" + file.getName() + ".zip";
                File tmp = new File(path);
                if (tmp.exists()) {
                    tmp.delete();
                }
                zip.setZipCallback(new ZipCallback() {

                    @Override
                    public void zipPercent(int percent) {
                        pDialog.setProgress(percent);
                    }

                    @Override
                    public void zipDone() {
                        pDialog.dismiss();
                        resultOk = true;
                    }

                    @Override
                    public void zipError(String error) {
                        file.delete();
                        pDialog.dismiss();
                        Dialog.toast(mCore.getContext(), error);
                    }

                    @Override
                    public void zipCancelled() {
                        file.delete();
                        pDialog.dismiss();
                    }

                });
                zip.zipIt(mCore.getContext(), file, path);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (resultOk) {
                    upload(path);
                }
            }

        }.execute((Void) null);
    }

    protected boolean isDownloadCancelled()  {
        return mDownloadCancelled || !mDownloadDialog.isShowing();
    }

    protected void setDownloadProgress(int value) {
        mDownloadDialog.setProgress(value);
    }

    public void download(final CloudEntry entry) {

        mDownloadCancelled = false;

        RecoveryPlugin rPlugin = (RecoveryPlugin) mCore.getPlugin(Core.PLUGIN_RECOVERY);

        final File file = new File(rPlugin.getBackupDir(true), entry.getName());

        Resources res = mCore.getContext().getResources();

        mDownloadDialog = new ProgressDialog(mCore.getContext());
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setMessage(res.getString(R.string.cloud_backup_downloading));
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                res.getString(android.R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                mDownloadCancelled = true;
                                cancelDownload();
                                file.delete();
                                mDownloadDialog.dismiss();

                                return null;
                            }

                        }.execute((Void) null);
                    }

                });
        mDownloadDialog.show();

        new AsyncTask<Void, Void, Void>() {

            private boolean resultOk = false;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    resultOk = download(entry, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Dialog.toast(mCore.getContext(), R.string.cloud_backup_error_downloading);
                }
                mDownloadDialog.dismiss();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (resultOk) {
                    unzipIt(file);
                }
            }

        }.execute((Void) null);
    }

    private void unzipIt(final File file) {

        final Zip zip = new Zip();

        Resources res = mCore.getContext().getResources();

        final ProgressDialog pDialog = new ProgressDialog(mCore.getContext());
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(res.getString(R.string.cloud_backup_unzipping));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                res.getString(android.R.string.cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        zip.cancel();
                        file.delete();
                        pDialog.dismiss();
                    }

                });
        pDialog.show();

        new AsyncTask<Void, Void, Void>() {

            private boolean resultOk = false;

            @Override
            protected Void doInBackground(Void... params) {

                String path = file.getAbsolutePath().replace(".zip", "");
                zip.setZipCallback(new ZipCallback() {

                    @Override
                    public void zipPercent(int percent) {
                        pDialog.setProgress(percent);
                    }

                    @Override
                    public void zipDone() {
                        pDialog.dismiss();
                        file.delete();
                        resultOk = true;
                    }

                    @Override
                    public void zipError(String error) {
                        file.delete();
                        pDialog.dismiss();
                    }

                    @Override
                    public void zipCancelled() {
                        file.delete();
                        pDialog.dismiss();
                    }

                });
                SuperUserPlugin sPlugin = (SuperUserPlugin) getCore().getPlugin(
                        Core.PLUGIN_SUPERUSER);
                sPlugin.run("mkdir -p " + path + ";chmod -R 777 " + path);
                zip.unzipIt(mCore.getContext(), file, path);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (resultOk) {
//                    RecoveryPlugin rPlugin = (RecoveryPlugin) mCore.getPlugin(Core.PLUGIN_RECOVERY);
//                    String storageFolder = rPlugin.getBackupDir(false);
//                    if (!storageFolder.endsWith("/")) {
//                        storageFolder += "/";
//                    }
                    mListener.cloudDownloadComplete();
                }
            }

        }.execute((Void) null);
    }

    public void remove(final CloudEntry entry) {

        final ProgressDialog pDialog = new ProgressDialog(mCore.getContext());
        pDialog.setIndeterminate(true);
        pDialog.setMessage(mCore.getContext().getResources().getString(
                R.string.alert_deleting_folder, entry.getName()));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        (new Thread() {

            public void run() {

                if (delete(entry)) {
                    ((Activity) mCore.getContext()).runOnUiThread(new Runnable() {

                        public void run() {
                            refresh();
                        }
                    });
                } else {
                    Dialog.toast(mCore.getContext(), R.string.cloud_error_deleting_remote_file);
                }

                pDialog.dismiss();
            }
        }).start();
    }
}
