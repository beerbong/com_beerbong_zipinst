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
import android.os.Build;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;

public class DownloadTask extends AsyncTask<Void, Integer, Integer> {

    public interface OnDownloadFinishListener {

        public void onDownloadFinish(File file);
    }

    private int mScale = 1048576;

    private OnDownloadFinishListener mListener;
    private ProgressDialog mDialog = null;
    private Context mContext;
    private String mUrl;
    private String mFileName;
    private String mMd5;
    private final WakeLock mWakeLock;

    private boolean mDone = false;

    @SuppressWarnings("deprecation")
    public DownloadTask(ProgressDialog dialog, String url, String fileName, String md5, OnDownloadFinishListener listener) {
        this.attach(dialog);

        mListener = listener;

        File dPath = new File(ManagerFactory.getPreferencesManager().getDownloadPath());
        dPath.mkdirs();

        mUrl = url;
        mFileName = fileName;
        mMd5 = md5;

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, MainActivity.class.getName());

        ManagerFactory.getDownloadManager().addDownloadTask(this);
    }

    public void attach(ProgressDialog dialog) {
        mDialog = dialog;
        mContext = dialog.getContext();
    }

    public void detach() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = null;
        ManagerFactory.getDownloadManager().removeDownloadTask(this);
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

        if (mUrl.contains("goo.im")) {
            String login = pManager.getLogin();
            if (login != null && !"".equals(login)) {
                mUrl = mUrl + "&hash=" + login;
            }
        }

        if (mMd5 != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(ManagerFactory.getPreferencesManager()
                        .getDownloadPath(), mFileName + ".md5sum"));
                fos.write((mMd5 + " " + mFileName).getBytes());
            } catch (Exception ex) {
            } finally {
                if (fos != null)
                    try {
                        fos.close();
                    } catch (Exception ex) {
                    }
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            URL getUrl = new URL(mUrl);
            URLConnection conn = getUrl.openConnection();
            if (getUrl.toString().contains("goo.im") && !pManager.isLogged()) {
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
            long availSpace = 0;
            if (Build.VERSION.SDK_INT > 17) {
                availSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            } else {
                availSpace = stat.getAvailableBlocks() * stat.getBlockSize();
            }
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
        if (mDialog != null) {
            mDialog.dismiss();
        }
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
        ManagerFactory.getDownloadManager().removeDownloadTask(this);
    }

    @Override
    protected void onPostExecute(Integer result) {
        mDone = true;
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mWakeLock.release();
        mWakeLock.acquire(30000);

        switch (result) {
            case 0:
                String path = ManagerFactory.getPreferencesManager().getDownloadPath() + mFileName;
                if (mListener != null) {
                    mListener.onDownloadFinish(new File(path));
                }
                if (mFileName.endsWith(".apk")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(path)),
                            "application/vnd.android.package-archive");
                    mContext.startActivity(intent);
                } else if (mFileName.endsWith(".zip")) {
                    ManagerFactory.getFileManager().addFile(path);
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
        ManagerFactory.getDownloadManager().removeDownloadTask(this);
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