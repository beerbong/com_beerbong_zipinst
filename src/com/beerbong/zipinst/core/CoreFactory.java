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

package com.beerbong.zipinst.core;

import com.beerbong.zipinst.core.Core.CoreListener;

import android.content.Context;

public class CoreFactory {

    private static Core sInstance;

    public static synchronized void init(Context context) {
        init(context, null);
    }

    public static synchronized void init(Context context, CoreListener listener) {
        if (sInstance == null) {
            sInstance = new CoreImpl(context, listener);
        } else {
            sInstance.setContext(context);
        }
    }

    public static Core getCore() {
        return sInstance;
    }

}
