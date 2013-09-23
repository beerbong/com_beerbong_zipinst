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

package com.beerbong.zipinst.ui;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.util.FileItem;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView.OnItemClickListener;

public abstract class UI implements OnItemClickListener {

    private static UI instance = null;

    public static synchronized void create(MainActivity activity) {
        if (instance == null) {
            instance = new UIImpl(activity);
        } else {
            instance.redraw(activity);
        }
    }

    public static synchronized UI getInstance() {
        return instance;
    }

    public abstract void showNoSuAlertAndExit();

    public abstract void addItem(String realPath, String sdcardPath);

    public abstract void removeItem(FileItem item);

    public abstract void removeAllItems();

    public abstract void redraw(MainActivity activity);

    public abstract void addUIListener(UIListener listener);

    public abstract void removeUIListener(UIListener listener);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public abstract boolean onCreateOptionsMenu(Menu menu);

    public abstract boolean onOptionsItemSelected(MenuItem item);

    public abstract void onNewIntent(Intent intent);
}