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

package com.beerbong.zipinst.widget;

import android.os.Bundle;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;

public class Activity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean darkTheme = ManagerFactory.getPreferencesManager().isDarkTheme();
        setTheme(darkTheme ? R.style.AppTheme_Dark : R.style.AppTheme);

        super.onCreate(savedInstanceState);
    }
}