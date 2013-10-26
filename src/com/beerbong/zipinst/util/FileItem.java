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

public class FileItem {

    private String key;
    private String name;
    private String path;
    private boolean delete;

    public FileItem(String key, String name, String path, boolean delete) {
        this.key = key;
        this.name = name;
        this.path = path;
        this.delete = delete;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getShortPath() {
        return this.path.substring(0, this.path.lastIndexOf("/") + 1);
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean isZip() {
        return key.endsWith(".zip") || path.endsWith(".zip");
    }

    public boolean isScript() {
        return key.endsWith(".sh") || path.endsWith(".sh");
    }
}