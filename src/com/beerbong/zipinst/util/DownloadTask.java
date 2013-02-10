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

package com.beerbong.zipinst.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;

public class DownloadTask extends AsyncTask<Void, Integer, Integer> {

    private int mScale = 1048576;

    private ProgressDialog mDialog = null;
    private Context mContext;
    private String mUrl;
    private String mFileName;
    private final WakeLock mWakeLock;

    private boolean mDone = false;

    @SuppressWarnings("deprecation")
    public DownloadTask(ProgressDialog dialog, String url, String fileName) {
        this.attach(dialog);

        File dPath = new File(ManagerFactory.getPreferencesManager().getDownloadPath());
        dPath.mkdirs();

        mUrl = url;
        mFileName = fileName;

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, MainActivity.class.getName());
    }

    public void attach(ProgressDialog dialog) {
        mDialog = dialog;
        mContext = dialog.getContext();
    }

    public void detach() {
        if (mDialog != null)
            mDialog.dismiss();
        mDialog = null;
        mContext = null;
    }

    public boolean isDone() {
        return mDone;
    }

    @Override
    protected void onPreExecute() {
        mDone = false;
        mDialog.show();
        mWakeLock.acquire();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        File destFile = new File(pManager.getDownloadPath(), mFileName);

        String extension = mFileName.substring(mFileName.lastIndexOf("."));
        String name = mFileName.substring(0, mFileName.lastIndexOf("."));
        int i = 0;
        while (destFile.exists()) {
            i++;
            mFileName = name + "(" + i + ")" + extension;
            destFile = new File(pManager.getDownloadPath(), mFileName);
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            URL getUrl = new URL(mUrl);
            URLConnection conn = getUrl.openConnection();
            if (getUrl.toString().contains("goo.im")) {
                conn.connect();
                publishProgress(-1);
                is = new BufferedInputStream(conn.getInputStream());
                os = new FileOutputStream(destFile);
                byte[] buf = new byte[4096];
                int nRead = -1;
                while ((nRead = is.read(buf)) != -1) {
                    if (this.isCancelled())
                        break;
                    os.write(buf, 0, nRead);
                }
                try {
                    Thread.sleep(10500);
                } catch (InterruptedException e) {
                }
                getUrl = new URL(mUrl);
                conn = getUrl.openConnection();
            }
            final int lengthOfFile = conn.getContentLength();
            StatFs stat = new StatFs(pManager.getDownloadPath());
            long availSpace = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            if (lengthOfFile >= availSpace) {
                destFile.delete();
                return 3;
            }
            if (lengthOfFile < 10000000)
                mScale = 1024; // if less than 10 mb, scale using kb
            publishProgress(0, lengthOfFile);
            conn.connect();
            is = new BufferedInputStream(conn.getInputStream());
            os = new FileOutputStream(destFile);
            byte[] buf = new byte[4096];
            int nRead = -1;
            int totalRead = 0;
            while ((nRead = is.read(buf)) != -1) {
                if (this.isCancelled())
                    break;
                os.write(buf, 0, nRead);
                totalRead += nRead;
                publishProgress(totalRead, lengthOfFile);
            }

            if (isCancelled()) {
                destFile.delete();
                return 2;
            }

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            destFile.delete();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    @Override
    protected void onCancelled(Integer result) {
        mDone = true;
        mDialog.dismiss();
        mWakeLock.release();
        mWakeLock.acquire(30000);
        if (result == null) {
            Toast.makeText(mContext, R.string.downloading_error, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (result) {
            case 0:
                break;
            case 2:
                Toast.makeText(mContext, R.string.downloading_interrupted, Toast.LENGTH_SHORT)
                        .show();
                break;
            case 3:
                Toast.makeText(mContext, R.string.downloading_nospace, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mContext, R.string.downloading_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        mDone = true;
        mDialog.dismiss();
        mWakeLock.release();
        mWakeLock.acquire(30000);

        switch (result) {
            case 0:
                String path = ManagerFactory.getPreferencesManager().getDownloadPath() + mFileName;
                if (mFileName.endsWith(".apk")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(path)),
                            "application/vnd.android.package-archive");
                    mContext.startActivity(intent);
                } else if (mFileName.endsWith(".zip")) {
                    ManagerFactory.getFileManager().addZip(path);
                }
                break;
            case 2:
                Toast.makeText(mContext, R.string.downloading_interrupted, Toast.LENGTH_SHORT)
                        .show();
                break;
            case 3:
                Toast.makeText(mContext, R.string.downloading_nospace, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mContext, R.string.downloading_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mDialog == null)
            return;
        if (values[0] == -1) {
            mDialog.setIndeterminate(true);
            return;
        }
        mDialog.setIndeterminate(false);
        if (values.length == 0)
            return;
        mDialog.setProgress(values[0] / mScale);
        if (values.length == 1)
            return;
        mDialog.setMax(values[1] / mScale);
    }
}