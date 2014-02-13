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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class DropboxStorage extends CloudStorage {

    private static final String MIME_TYPE = "application/zip";

    protected static DropboxAPI<AndroidAuthSession> mDBApi;

    private UploadRequest mRequest;
    private DropboxInputStream mInputStream;
    private String mRemoteFolder;

    public DropboxStorage(Core core) {
        super(core, STORAGE_DROPBOX);

        Intent intent = new Intent(core.getContext(), DropboxLoginActivity.class);
        core.getContext().startActivity(intent);
    }

    @Override
    protected List<CloudEntry> getCloudEntries() {
        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        String board = SystemProperties.getProperty("ro.product.device");
        mRemoteFolder = board + "/CWM";
        if (rPlugin.getRecovery().getId() == RecoveryInfo.RECOVERY_TWRP) {
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
                    String path = e.path;
                    path = path.substring(0, path.lastIndexOf("/") + 1);
                    CloudEntry entry = new CloudEntry(e.fileName(), path);
                    entry.setSize(e.bytes);
                    entry.setImageAttr(R.attr.cloudIcon);
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
    public int getName() {
        return R.string.dropbox;
    }

    @Override
    protected boolean delete(CloudEntry toDelete) {
        try {
            mDBApi.delete(toDelete.getPath());
            return true;
        } catch (DropboxException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean download(CloudEntry entry, File file) {
        BufferedOutputStream bw = null;

        try {
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException ex) {
                    SuperUserPlugin suPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
                    suPlugin.run("rm -f " + file.getAbsolutePath());
                    suPlugin.run("touch " + file.getAbsolutePath());
                    suPlugin.run("chmod 777 " + file.getAbsolutePath());
//                }
//            }

            mInputStream = mDBApi.getFileStream(entry.getPath(), null);
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
                int percent = (int)(total * 100 / entry.getSize());
                setDownloadProgress(percent);
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
    protected void cancelDownload() {
        try {
            mInputStream.close();
        } catch (Exception ex) {
            // ignore
        }
    }

    @Override
    protected void cancelUpload() {
        mRequest.abort();
    }

    @Override
    protected void upload() {
        try {
            mRequest.upload();
        } catch (DropboxException ex) {
            // cancelled
        }
    }

    @Override
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
            Dialog.toast(getCore().getContext(), R.string.cloud_backup_error_uploading);
            return;
        }

        super.upload(path);
    }

}
