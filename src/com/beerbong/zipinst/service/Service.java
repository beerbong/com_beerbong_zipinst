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

package com.beerbong.zipinst.service;

import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.ProManager;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class Service extends android.app.Service {

    public interface ServiceListener {

        public void onStart();

        public void onDestroy();
    }

    public class LocalBinder extends Binder {

        Service getService() {
            return Service.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    private ServiceListener mListener;

    @Override
    public void onCreate() {
        ManagerFactory.getProManager().manage(this, ProManager.ManageMode.Service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mListener != null) {
            mListener.onStart();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mListener != null) {
            mListener.onDestroy();
        }
        super.onDestroy();
    }

    public void setServiceListener(ServiceListener listener) {
        mListener = listener;
    }
}
