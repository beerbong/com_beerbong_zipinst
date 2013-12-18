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

import android.content.Context;

import com.beerbong.zipinst.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {

    public interface ZipCallback {

        public void zipPercent(int percent);

        public void zipDone();

        public void zipCancelled();

        public void zipError(String error);
    }

    private class Monitor extends ProgressMonitor {

        @Override
        public void updateWorkCompleted(long workCompleted) {
            super.updateWorkCompleted(workCompleted);
            mCallback.zipPercent(getPercentDone());
        }

    }

    private static final int BUFFER = 8 * 1024;

    private ZipCallback mCallback;
    private boolean mCancelled = false;

    public Zip() {
    }

    public void setZipCallback(ZipCallback callback) {
        mCallback = callback;
    }

    public void cancel() {
        mCancelled = true;
    }

    public void zipIt(final Context context, final File directoryToZip, final String outputZip) {
        mCancelled = false;

        mCallback.zipPercent(0);

        try {
            ArrayList<File> fileList = new ArrayList<File>();
            getAllFiles(directoryToZip, fileList);

            ZipFile zipFile = new ZipFile(new File(outputZip), new Monitor());
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);

            zipFile.addFiles(fileList, parameters);

            if (mCancelled) {
                new File(outputZip).delete();
                mCallback.zipCancelled();
            } else {
                mCallback.zipDone();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCallback.zipError(context.getResources().getString(R.string.error_zipping));
        }
    }

    public void unzipIt(final Context context, final File zip, final String outputFolder) {
        mCancelled = false;

        mCallback.zipPercent(0);

        try {

            ZipFile zipFile = new ZipFile(zip, new Monitor());
            zipFile.extractAll(outputFolder);

            if (mCancelled) {
                zip.delete();
                mCallback.zipCancelled();
            } else {
                mCallback.zipDone();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCallback.zipError(context.getResources().getString(R.string.error_unzipping));
        }
    }

    private void getAllFiles(File dir, List<File> fileList) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
    }

    public boolean unZipIt(File zipFile, String outputFolder) {

        byte[] buffer = new byte[BUFFER];

        try {

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder, fileName);

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}
