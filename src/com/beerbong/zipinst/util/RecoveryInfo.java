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

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class RecoveryInfo {

    private int id;
    private String name = null;
    private String sdcard = null;

    public RecoveryInfo(int id, String name, String sdcard) {
        this.id = id;
        this.name = name;
        this.sdcard = sdcard;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSdcard() {
        return sdcard;
    }

    public void setSdcard(String sdcard) {
        this.sdcard = sdcard;
    }
}