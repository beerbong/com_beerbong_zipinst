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

package com.beerbong.zipinst.store;

import java.io.Serializable;

public class FileItem implements Serializable {

    private String mKey;
    private String mName;
    private String mPath;
    private String mMd5;
    private boolean mDelete;
    private long mSize = -1;
    private int mImageAttr = 0;
    private int mImage = 0;

    public FileItem(String key, String name, String path, boolean delete) {
        mKey = key;
        mName = name;
        mPath = path;
        mDelete = delete;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    protected void setPath(String path) {
        mPath = path;
    }

    protected void setName(String name) {
        mName = name;
    }

    public String getShortPath() {
        return mPath.substring(0, mPath.lastIndexOf("/") + 1);
    }

    public void setDelete(boolean delete) {
        mDelete = delete;
    }

    public boolean isDelete() {
        return mDelete;
    }

    public void setMd5(String md5) {
        mMd5 = md5;
    }

    public String getMd5() {
        return mMd5;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getSize() {
        return mSize;
    }

    public void setImage(int image) {
        mImage = image;
    }

    public int getImage() {
        return mImage;
    }

    public void setImageAttr(int imageAttr) {
        mImageAttr = imageAttr;
    }

    public int getImageAttr() {
        return mImageAttr;
    }

    public boolean isZip() {
        return mKey.endsWith(".zip") || mPath.endsWith(".zip");
    }

    public boolean isScript() {
        return mKey.endsWith(".sh") || mPath.endsWith(".sh");
    }
}