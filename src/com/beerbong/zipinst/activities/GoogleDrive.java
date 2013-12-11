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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.RecoveryManager;
import com.beerbong.zipinst.util.CloudEntry;
import com.beerbong.zipinst.util.Constants;
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
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class GoogleDrive extends CloudActivity {

    private GoogleAccountCredential mCredential;
    private Drive mService;
    private String mRemoteFolderName;
    private File mRemoteFolder;
    private List<CloudEntry> mEntries;
    private List<File> mFiles;
    private Drive.Files.Insert mInsert;

    private class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

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
                    int percent = (int) mediaHttpUploader.getProgress() * 100;
                    System.out.println(percent);
                    setUploadProgress(percent);
                    break;
                case MEDIA_COMPLETE:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecoveryManager rManager = ManagerFactory.getRecoveryManager();
        String board = Constants.getProperty("ro.product.device");
        mRemoteFolderName = board + "-CWM";
        if (rManager.getRecovery().getId() == R.id.twrp) {
            mRemoteFolderName = board + "-TWRP";
        }

        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
        startActivityForResult(mCredential.newChooseAccountIntent(),
                Constants.REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onResume() {
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(statusCode, this, 0).show();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mService = getDriveService(mCredential);
                        init();
                    }
                }
                break;
            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    init();
                } else {
                    startActivityForResult(mCredential.newChooseAccountIntent(),
                            Constants.REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    @Override
    public List<CloudEntry> getCloudEntries() {
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
                entry.putExtra("id", file.getId());
                mEntries.add(entry);
                mFiles.add(file);
            }
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), Constants.REQUEST_AUTHORIZATION);
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
    public boolean deleteRemote(String toDelete) {
        String title = toDelete.substring(toDelete.lastIndexOf("/") + 1);
        CloudEntry entry = searchEntry(title);
        try {
            mService.files().delete((String) entry.getExtra("id")).execute();
            return true;
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), Constants.REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean download(String folder, String name, Bundle extras, java.io.File file,
            final ProgressDialog pDialog) {
        try {
            String id = extras.getString("id");
            File cloudFile = searchFile(id);
            java.io.File parent = new java.io.File(file.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }
            OutputStream out = new FileOutputStream(file);

            // TODO progress doesn't work
            MediaHttpDownloader downloader = new MediaHttpDownloader(mService.getRequestFactory()
                    .getTransport(), mService.getRequestFactory().getInitializer());
            downloader.setDirectDownloadEnabled(false);
            downloader.setChunkSize(MediaHttpDownloader.MAXIMUM_CHUNK_SIZE);
            downloader.setProgressListener(
                    new MediaHttpDownloaderProgressListener() {

                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) {
                            switch (downloader.getDownloadState()) {
                                case NOT_STARTED:
                                    break;
                                case MEDIA_IN_PROGRESS:
                                    int percent = (int) downloader.getProgress() * 100;
                                    System.out.println(percent);
                                    pDialog.setProgress(percent);
                                    break;
                                case MEDIA_COMPLETE:
                                    break;
                            }
                        }
                    });
            downloader.download(new GenericUrl(cloudFile.getDownloadUrl()), out);
            return true;
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), Constants.REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void cancelDownload() {
        // TODO implement this
    }

    @Override
    public void cancelUpload() {
        // TODO implement this
    }

    @Override
    public void upload() {
        try {
            mInsert.execute();
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), Constants.REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void upload(final String path) {
        try {
            java.io.File file = new java.io.File(path);
            FileContent fileContent = new FileContent("application/zip", file);
            //            InputStreamContent fileContent = new InputStreamContent("application/zip",
            //                    new BufferedInputStream(new FileInputStream(file)));
            //            fileContent.setLength(file.length());

            File body = new File();
            body.setTitle(file.getName());
            body.setMimeType("application/zip");
            body.setParents(Arrays.asList(new ParentReference().setId(mRemoteFolder.getId())));

            mInsert = mService.files().insert(body, fileContent);
            // TODO progress doesn't work
            MediaHttpUploader uploader = mInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
            uploader.setProgressListener(new FileUploadProgressListener());
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), Constants.REQUEST_AUTHORIZATION);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        super.upload(path);
    }

    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                mCredential).setApplicationName(getResources().getString(R.string.app_name))
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
            if (entry.getExtra("id").equals(title) || entry.getFileName().equals(title)) {
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
