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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // call the pro receiver if available
        try {
            Class<?> pClass = Class.forName("com.beerbong.zipinst.pro.Receiver");
            BroadcastReceiver mProReceiver = (BroadcastReceiver) pClass.newInstance();
            mProReceiver.onReceive(context, intent);
        } catch (Throwable t) {
            // sorry, you are not a pro
        }
    }

}
