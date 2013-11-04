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

package com.beerbong.zipinst.manager.pro;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.Manager;
import com.beerbong.zipinst.manager.ProManager;
import com.beerbong.zipinst.util.ILicenseCallback;

public class ProManagerImpl extends Manager implements ProManager {

    private static final String PRO_CLASS = "com.beerbong.zipinst.pro.ProManager";
    private static final String PRO_LICENSE_CLASS = "com.beerbong.zipinst.pro.LicenseCallback";

    private ProManager mRealPro;
    private ILicenseCallback mLicenseCallback;

    public ProManagerImpl(Context context) {
        super(context);

        try {
            Class<?> pClass = Class.forName(PRO_CLASS);
            mRealPro = (ProManager) pClass.newInstance();
            mRealPro.setContext(context);
            pClass = Class.forName(PRO_LICENSE_CLASS);
            mLicenseCallback = (ILicenseCallback) pClass.newInstance();
            mLicenseCallback.setActivity((Activity) context);
            mLicenseCallback.check();
        } catch (Throwable t) {
            // sorry, you are not a pro
        }

        if (!iAmPro()) {
            Toast.makeText(context, R.string.consider_becoming_a_pro, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
        if (mRealPro != null) {
            mRealPro.setContext(context);
        }
    }

    @Override
    public boolean iAmPro() {
        return mRealPro != null;
    }

    @Override
    public void manage(Object input, ManageMode mode) {
        if (iAmPro()) {
            mRealPro.manage(input, mode);
        }
    }
}