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

package com.beerbong.zipinst;

import com.beerbong.zipinst.pro.updater.RomUpdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    private static final String SERVICE_CLASS = "com.beerbong.zipinst.pro.Service";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Class<?> pClass = Class.forName(SERVICE_CLASS);
            RomUpdater romUpdater = new RomUpdater(context, false);
            if (romUpdater.canUpdate()) {
                Intent service = new Intent(context, pClass);
                context.startService(service);
            }
        } catch (Exception ex) {
            // ignore
        }
    }

}