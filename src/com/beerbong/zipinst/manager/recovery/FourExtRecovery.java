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

package com.beerbong.zipinst.manager.recovery;

import android.content.Context;

import com.beerbong.zipinst.R;

public class FourExtRecovery extends CwmRecovery {

    public FourExtRecovery(Context context) {
        super(context);

        setId(R.id.fourext);
        setName("fourext");
        setInternalSdcard("sdcard");
        setExternalSdcard("external_sd");
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_4ext);
    }

    @Override
    public String getFolderPath() {
        return "/cache/4ext/";
    }
}
