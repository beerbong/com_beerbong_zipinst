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