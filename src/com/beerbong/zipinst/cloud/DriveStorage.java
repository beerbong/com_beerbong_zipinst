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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.SystemProperties;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class DriveStorage extends CloudStorage {

    private static final int REQUEST_ACCOUNT_PICKER = 204;
    private static final int REQUEST_AUTHORIZATION = 205;

    private GoogleAccountCredential mCredential;
    private Drive mService;
    private String mRemoteFolderName;
    private File mRemoteFolder;
    private List<CloudEntry> mEntries;
    private List<File> mFiles;
    private Drive.Files.Insert mInsert;
    private boolean mWaitingActivity;

    public DriveStorage(Core core) {
        super(core, STORAGE_DRIVE);

        RecoveryPlugin rPlugin = (RecoveryPlugin) core.getPlugin(Core.PLUGIN_RECOVERY);
        String board = SystemProperties.getProperty("ro.product.device");
        mRemoteFolderName = board + "-CWM";
        if (rPlugin.getRecovery().getId() == RecoveryInfo.RECOVERY_TWRP) {
            mRemoteFolderName = board + "-TWRP";
        }

        mWaitingActivity = true;
        mCredential = GoogleAccountCredential.usingOAuth2(core.getContext(),
                Arrays.asList(DriveScopes.DRIVE));
        ((Activity) core.getContext()).startActivityForResult(mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void refresh() {
        if (mWaitingActivity) {
            return;
        }
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCore().getContext());
        if (statusCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(statusCode, (Activity) getCore().getContext(), 0).show();
        }
        super.refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingActivity = false;
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mService = getDriveService(mCredential);
                        refresh();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    refresh();
                } else {
                    ((Activity) getCore().getContext()).startActivityForResult(
                            mCredential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    @Override
    protected List<CloudEntry> getCloudEntries() {
        mEntries = new ArrayList<CloudEntry>();
        mFiles = new ArrayList<File>();

        try {
            if (!checkFolder()) {
                File body = new File();
                body.setTitle(mRemoteFolderName);
                body.setMimeType("application/vnd.google-apps.folder");
                mRemoteFolder = mService.files().insert(body).execute();
            }
            List<File> files = listFiles();
            for (File file : files) {
                CloudEntry entry = new CloudEntry(file.getTitle(), "/" + mRemoteFolderName + "/");
                entry.setRemoteId(file.getId());
                entry.setSize(file.getFileSize());
                entry.setImageAttr(R.attr.cloudIcon);
                mEntries.add(entry);
                mFiles.add(file);
            }
        } catch (UserRecoverableAuthIOException e) {
            ((Activity) getCore().getContext()).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return mEntries;
    }

    @Override
    public int getCloudIcon() {
        return R.drawable.ic_drive_icon;
    }

    @Override
    public int getName() {
        return R.string.drive;
    }

    @Override
    protected boolean delete(CloudEntry toDelete) {
        String delete = toDelete.getPath();
        String title = delete.substring(delete.lastIndexOf("/") + 1);
        CloudEntry entry = searchEntry(title);
        try {
            mService.files().delete(entry.getRemoteId()).execute();
            return true;
        } catch (UserRecoverableAuthIOException e) {
            ((Activity) getCore().getContext()).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean download(CloudEntry entry, java.io.File file) {
        try {
            File cloudFile = searchFile(entry.getRemoteId());
            final long length = cloudFile.getFileSize();
//            java.io.File parent = new java.io.File(file.getParent());
//            if (!parent.exists()) {
//                parent.mkdirs();
//            }
            SuperUserPlugin suPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
            suPlugin.run("rm -f " + file.getAbsolutePath());
            suPlugin.run("touch " + file.getAbsolutePath());
            suPlugin.run("chmod 777 " + file.getAbsolutePath());
            OutputStream out = new FileOutputStream(file);

            MediaHttpDownloader downloader = new MediaHttpDownloader(mService.getRequestFactory()
                    .getTransport(), mService.getRequestFactory().getInitializer());
            downloader.setDirectDownloadEnabled(false);
            downloader.setChunkSize(2 * 1048576);
            downloader.setProgressListener(
                    new MediaHttpDownloaderProgressListener() {

                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                            switch (downloader.getDownloadState()) {
                                case NOT_STARTED:
                                    break;
                                case MEDIA_IN_PROGRESS:
                                    if (isDownloadCancelled()) {
                                        throw new IOException();
                                    }
                                    long bytes = downloader.getNumBytesDownloaded();
                                    int percent = (int) (bytes * 100 / length);
                                    setDownloadProgress(percent);
                                    break;
                                case MEDIA_COMPLETE:
                                    break;
                            }
                        }
                    });
            downloader.download(new GenericUrl(cloudFile.getDownloadUrl()), out);
            return true;
        } catch (UserRecoverableAuthIOException e) {
            ((Activity) getCore().getContext()).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void cancelDownload() {
    }

    @Override
    protected void cancelUpload() {
    }

    @Override
    public void upload() {
        try {
            mInsert.execute();
        } catch (UserRecoverableAuthIOException e) {
            ((Activity) getCore().getContext()).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void upload(final String path) {
        try {
            java.io.File file = new java.io.File(path);
            FileContent fileContent = new FileContent("application/zip", file);

            File body = new File();
            body.setFileSize(file.length());
            body.setTitle(file.getName());
            body.setMimeType("application/zip");
            body.setParents(Arrays.asList(new ParentReference().setId(mRemoteFolder.getId())));

            mInsert = mService.files().insert(body, fileContent);
            MediaHttpUploader uploader = mInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
            uploader.setProgressListener(new MediaHttpUploaderProgressListener() {

                @Override
                public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
                    if (mediaHttpUploader == null) {
                        return;
                    }
                    switch (mediaHttpUploader.getUploadState()) {
                        case NOT_STARTED:
                            break;
                        case INITIATION_STARTED:
                            break;
                        case INITIATION_COMPLETE:
                            break;
                        case MEDIA_IN_PROGRESS:
                            if (isUploadCancelled()) {
                                throw new IOException();
                            }
                            long length = mediaHttpUploader.getMediaContent().getLength();
                            long bytes = mediaHttpUploader.getNumBytesUploaded();
                            int percent = (int) (bytes * 100 / length);
                            setUploadProgress(percent);
                            break;
                        case MEDIA_COMPLETE:
                            break;
                    }
                }
            });
        } catch (UserRecoverableAuthIOException e) {
            ((Activity) getCore().getContext()).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        super.upload(path);
    }

    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                mCredential).setApplicationName(getCore().getContext().getResources().getString(R.string.app_name))
                .build();
    }

    private boolean checkFolder() throws IOException {
        Files.List request = mService
                .files()
                .list()
                .setQ("mimeType='application/vnd.google-apps.folder' and title='"
                        + mRemoteFolderName + "' and trashed=false");
        FileList files = request.execute();
        List<File> items = files.getItems();
        for (File file : items) {
            if (mRemoteFolderName.equals(file.getTitle())) {
                mRemoteFolder = file;
                return true;
            }
        }
        return false;
    }

    private List<File> listFiles() throws IOException {
        Files.List request = mService
                .files()
                .list()
                .setQ("mimeType='application/zip' and '" + mRemoteFolder.getId()
                        + "' in parents and trashed=false");
        FileList files = request.execute();
        return files.getItems();
    }

    private CloudEntry searchEntry(String title) {
        for (CloudEntry entry : mEntries) {
            if (entry.getRemoteId().equals(title) || entry.getName().equals(title)) {
                return entry;
            }
        }
        return null;
    }

    private File searchFile(String id) {
        for (File file : mFiles) {
            if (file.getId().equals(id)) {
                return file;
            }
        }
        return null;
    }

}
