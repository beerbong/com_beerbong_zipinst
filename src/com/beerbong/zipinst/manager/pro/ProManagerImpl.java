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

package com.beerbong.zipinst.manager.pro;

import android.content.Context;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.Manager;
import com.beerbong.zipinst.manager.ProManager;

public class ProManagerImpl extends Manager implements ProManager {

    private static final String PRO_CLASS = "com.beerbong.zipinst.pro.ProManager";

    private ProManager mRealPro;

    public ProManagerImpl(Context context) {
        super(context);

        try {
            Class<?> pClass = Class.forName(PRO_CLASS);
            mRealPro = (ProManager) pClass.newInstance();
            mRealPro.setContext(context);
        } catch (Throwable t) {
            // sorry, you are not a pro
        }
        
        if (!iAmPro()) {
            Toast.makeText(context, R.string.consider_becoming_a_pro, Toast.LENGTH_LONG).show();
        }
    }

    public void setContext(Context context) {
        mContext = context;
        if (mRealPro != null) {
            mRealPro.setContext(context);
        }
    }
    public boolean iAmPro() {
        return mRealPro != null;
    }
}