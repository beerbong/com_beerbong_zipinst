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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.manager.RecoveryManager;
import com.beerbong.zipinst.util.CloudEntry;
import com.beerbong.zipinst.util.Constants;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Dropbox extends CloudActivity {

    private static final String MIME_TYPE = "application/zip";

    private DropboxAPI<AndroidAuthSession> mDBApi;
    private UploadRequest mRequest;
    private DropboxInputStream mInputStream;
    private String mRemoteFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDBApi = Constants.createDropboxAPI(this);

        if (mDBApi.getSession().getAccessTokenPair() == null) {
            mDBApi.getSession().startAuthentication(this);
        } else {
            init();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                PreferencesManager pManager = ManagerFactory.getPreferencesManager();
                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
                pManager.setDropboxKey(tokens.key);
                pManager.setDropboxSecret(tokens.secret);

                init();

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    @Override
    public List<CloudEntry> getCloudEntries() {
        RecoveryManager rManager = ManagerFactory.getRecoveryManager();
        String board = Constants.getProperty("ro.product.device");
        mRemoteFolder = board + "/CWM";
        if (rManager.getRecovery().getId() == R.id.twrp) {
            mRemoteFolder = board + "/TWRP";
        }
        try {
            mDBApi.createFolder(board);
        } catch (DropboxException ex) {
            // already exists
        }
        try {
            mDBApi.createFolder(mRemoteFolder);
        } catch (DropboxException ex) {
            // already exists
        }

        mRemoteFolder = "/" + mRemoteFolder;

        List<CloudEntry> entries = new ArrayList<CloudEntry>();
        try {
            Entry dirent = mDBApi.metadata(mRemoteFolder, 1000, null, true, null);
            for (Entry e : dirent.contents) {
                if (!e.isDeleted && !e.isDir && MIME_TYPE.equals(e.mimeType)) {
                    CloudEntry entry = new CloudEntry(e.fileName(), e.path, e.bytes);
                    entries.add(entry);
                }
            }
        } catch (DropboxException ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    @Override
    public int getCloudIcon() {
        return R.drawable.ic_dropbox_icon;
    }

    @Override
    public boolean deleteRemote(String toDelete) {
        try {
            mDBApi.delete(toDelete);
            return true;
        } catch (DropboxException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void cancelUpload() {
        mRequest.abort();
    }

    @Override
    public void upload() {
        try {
            mRequest.upload();
        } catch (DropboxException ex) {
            // cancelled
        }
    }

    protected void upload(final String path) {

        final File file = new File(path);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            mRequest = mDBApi.putFileRequest(
                    mRemoteFolder + "/" + file.getName(),
                    inputStream,
                    file.length(), null, new ProgressListener() {

                        @Override
                        public void onProgress(long bytes, long total) {
                            int percent = Math.round(bytes * 100 / total);
                            setUploadProgress(percent);
                        }

                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            Constants.showToastOnUiThread(Dropbox.this, R.string.backup_error_uploading);
            return;
        }

        super.upload(path);
    }

    @Override
    public boolean download(String folder, String name, long bytes, File file, ProgressDialog pDialog) {
        BufferedOutputStream bw = null;

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            mInputStream = mDBApi.getFileStream(folder + name, null);
            bw = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[4096];
            int read;
            long total = 0;
            while (true) {
                read = mInputStream.read(buffer);
                if (read <= 0) {
                    break;
                }
                bw.write(buffer, 0, read);
                total += read;
                int percent = (int)(total * 100 / bytes);
                pDialog.setProgress(percent);
            }
            return true;
        } catch (Exception ex) {
            // cancelled?
            ex.printStackTrace();
        } finally {
            try {
                mInputStream.close();
            } catch (Exception ex) {
            }
            try {
                bw.close();
            } catch (Exception ex) {
            }
        }
        return false;
    }

    @Override
    public void cancelDownload() {
        try {
            mInputStream.close();
        } catch (Exception ex) {
            // ignore
        }
    }
}
