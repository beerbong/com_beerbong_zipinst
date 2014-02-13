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

import com.beerbong.zipinst.store.FileItem;

public class CloudEntry extends FileItem {

    private String mRemoteId;
    private long mSize;

    public CloudEntry(String fileName, String path) {
        super(fileName, fileName, path + fileName, false);
    }

    public String getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(String remoteId) {
        this.mRemoteId = remoteId;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getSize() {
        return mSize;
    }

}
