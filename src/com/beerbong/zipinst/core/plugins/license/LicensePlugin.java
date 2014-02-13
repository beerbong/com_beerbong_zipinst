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

package com.beerbong.zipinst.core.plugins.license;

import android.app.Activity;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreImpl;
import com.beerbong.zipinst.core.Plugin;

public class LicensePlugin extends Plugin {

    private static final String LICENSE_CALLBACK_CLASS = "com.beerbong.zipinst.pro.LicenseCallback";

    private ILicenseCallback mLicenseCallback;
    private boolean mPurchased = false;

    public LicensePlugin(Core core) {
        super(core, Core.PLUGIN_LICENSE);
    }

    @Override
    public void start() {
        try {
            Core core = getCore();
            Class<?> pClass = Class.forName(LICENSE_CALLBACK_CLASS);
            mLicenseCallback = (ILicenseCallback) pClass.newInstance();

            ((CoreImpl) getCore()).setMessage(R.string.checking_license);

            mLicenseCallback.setActivity((Activity) core.getContext());
            mLicenseCallback.check(this);
        } catch (Throwable t) {
            started();
        }
    }

    @Override
    public void stop() {
        if (mLicenseCallback != null) {
            mLicenseCallback.destroy();
        }
        stopped();
    }

    public void setPurchased() {
        mPurchased = true;
        started();
    }

    public boolean isPurchased() {
        return mPurchased;
    }

}
