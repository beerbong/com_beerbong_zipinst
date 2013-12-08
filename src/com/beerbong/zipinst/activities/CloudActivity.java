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

package com.beerbong.zipinst.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.RecoveryManager;
import com.beerbong.zipinst.util.CloudEntry;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.Zip;
import com.beerbong.zipinst.util.Zip.ZipCallback;
import com.beerbong.zipinst.widget.PreferenceActivity;

import java.io.File;
import java.util.List;

public abstract class CloudActivity extends PreferenceActivity {

    private OnPreferenceClickListener mStorageListener;
    private OnPreferenceClickListener mCloudListener;
    private ProgressDialog mUploadDialog;

    public abstract List<CloudEntry> getCloudEntries();

    public abstract int getCloudIcon();

    public abstract boolean deleteRemote(String toDelete);

    public abstract boolean download(String folder, String name, long bytes, File file, ProgressDialog pDialog);

    public abstract void cancelDownload();

    public abstract void cancelUpload();

    public abstract void upload();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.sync_backups);

        mStorageListener = new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showStorageDialog(preference);
                return false;
            }
        };

        mCloudListener = new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showCloudDialog(preference);
                return false;
            }
        };
    }

    @SuppressWarnings("deprecation")
    protected void init() {
        final PreferenceCategory storageCategory = (PreferenceCategory) findPreference("category_in_storage");
        final PreferenceCategory cloudCategory = (PreferenceCategory) findPreference("category_in_cloud");
        storageCategory.removeAll();
        cloudCategory.removeAll();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(getResources().getString(R.string.backups_reading));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                RecoveryManager rManager = ManagerFactory.getRecoveryManager();

                List<CloudEntry> entries = getCloudEntries();

                String[] storageBackups = rManager.getBackupList();
                String storageFolder = rManager.getBackupDir(true);
                if (!storageFolder.startsWith("/")) {
                    storageFolder = "/" + storageFolder;
                }
                if (!storageFolder.endsWith("/")) {
                    storageFolder = storageFolder + "/";
                }

                boolean darkTheme = ManagerFactory.getPreferencesManager().isDarkTheme();

                for (int i = 0; i < storageBackups.length; i++) {
                    Preference pref = new Preference(CloudActivity.this);
                    pref.setTitle(storageBackups[i]);
                    pref.setSummary(storageFolder);
                    if (darkTheme) {
                        pref.setIcon(R.drawable.ic_sdcard_dark);
                    } else {
                        pref.setIcon(R.drawable.ic_sdcard_light);
                    }
                    pref.setOnPreferenceClickListener(mStorageListener);
                    storageCategory.addPreference(pref);
                }
                for (CloudEntry e : entries) {
                    Preference pref = new Preference(CloudActivity.this);
                    pref.getExtras().putLong("bytes", e.getSize());
                    pref.setTitle(e.getFileName());
                    pref.setSummary(e.getPath());
                    pref.setIcon(getCloudIcon());
                    pref.setOnPreferenceClickListener(mCloudListener);
                    cloudCategory.addPreference(pref);
                }

                pDialog.dismiss();

                return null;
            }

        }.execute((Void) null);

    }

    private void showStorageDialog(final Preference preference) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_backup_synchronize_title);
        alert.setMessage(getResources().getString(
                R.string.alert_backup_synchronize_message, preference.getTitle()));
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNeutralButton(R.string.alert_backup_delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                deleteLocal((String) preference.getTitle(),
                        (String) preference.getSummary());
            }
        });
        alert.setPositiveButton(R.string.alert_backup_synchronize,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        zipIt((String) preference.getSummary(),
                                (String) preference.getTitle());
                    }
                });
        alert.show();
    }

    private void showCloudDialog(final Preference preference) {
        final long bytes = preference.getExtras().getLong("bytes");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_backup_remote_title);
        alert.setMessage(getResources().getString(
                R.string.alert_backup_remote_message, preference.getTitle()));
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNeutralButton(R.string.alert_backup_delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                deleteRemote((String) preference.getTitle(),
                        (String) preference.getSummary());
            }
        });
        alert.setPositiveButton(R.string.alert_backup_download,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        download((String) preference.getSummary(),
                                (String) preference.getTitle(), bytes);
                    }
                });
        alert.show();
    }

    protected void setUploadProgress(int value) {
        mUploadDialog.setProgress(value);
    }

    protected void upload(final String path) {

        mUploadDialog = new ProgressDialog(this);

        final File file = new File(path);

        mUploadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUploadDialog.setMessage(getResources().getString(R.string.backup_uploading));
        mUploadDialog.setCancelable(false);
        mUploadDialog.setCanceledOnTouchOutside(false);
        mUploadDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(android.R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
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

                    runOnUiThread(new Runnable() {

                        public void run() {
                            init();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Constants.showToastOnUiThread(CloudActivity.this, R.string.backup_error_uploading);
                }
                mUploadDialog.dismiss();

                return null;
            }

        }.execute((Void) null);
    }

    private void zipIt(final String folder, final String name) {

        final Zip zip = new Zip();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(getResources().getString(R.string.backup_zipping));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(android.R.string.cancel), new OnClickListener() {

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

                final File file = new File(folder, name);
                path = file.getAbsolutePath() + ".zip";
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
                    }

                    @Override
                    public void zipCancelled() {
                        file.delete();
                        pDialog.dismiss();
                    }

                });
                zip.zipIt(CloudActivity.this, file, path);

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

    protected void download(final String folder, final String name, final long bytes) {

        final File file = new File(ManagerFactory.getRecoveryManager().getBackupDir(true), name);

        final ProgressDialog pDialog = new ProgressDialog(CloudActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(getResources().getString(R.string.backup_downloading));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(android.R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                cancelDownload();
                                file.delete();
                                pDialog.dismiss();

                                return null;
                            }

                        }.execute((Void) null);
                    }

                });
        pDialog.show();

        new AsyncTask<Void, Void, Void>() {

            private boolean resultOk = false;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    download(folder, name, bytes, file, pDialog);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Constants.showToastOnUiThread(CloudActivity.this, R.string.backup_error_downloading);
                }
                pDialog.dismiss();

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

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(getResources().getString(R.string.backup_unzipping));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(android.R.string.cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        zip.cancel();
                        file.delete();
                        pDialog.dismiss();
                    }

                });
        pDialog.show();

        new AsyncTask<Void, Void, Void>() {

            private String path;
            private boolean resultOk = false;

            @Override
            protected Void doInBackground(Void... params) {

                path = file.getAbsolutePath().replace(".zip", "");
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
                zip.unzipIt(CloudActivity.this, file, path);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (resultOk) {
                    showRestoreDialog(path);
                }
            }

        }.execute((Void) null);
    }

    private void deleteLocal(String name, String folder) {
        final String toDelete = folder + name;

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(getResources().getString(
                R.string.alert_deleting_folder, name));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        (new Thread() {

            public void run() {

                pDialog.dismiss();

                ManagerFactory.getFileManager().recursiveDelete(new File(toDelete));
                runOnUiThread(new Runnable() {

                    public void run() {
                        init();
                    }
                });
            }
        }).start();
    }

    private void deleteRemote(String name, String folder) {
        final String toDelete = folder + name;

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(getResources().getString(
                R.string.alert_deleting_folder, name));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        (new Thread() {

            public void run() {

                if (deleteRemote(toDelete)) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            init();
                        }
                    });
                } else {
                    Constants
                            .showToastOnUiThread(CloudActivity.this, R.string.error_deleting_remote_file);
                }

                pDialog.dismiss();
            }
        }).start();
    }

    private void showRestoreDialog(final String path) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_restore_title);
        alert.setMessage(CloudActivity.this.getResources().getString(
                R.string.alert_restore_message, path));
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                init();
            }
        });
        alert.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        ManagerFactory.getRebootManager().restore(path);
                    }
                });
        alert.show();
    }
}
