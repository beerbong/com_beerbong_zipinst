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

    public abstract void addItem(String realPath, String sdcardPath, boolean delete);

    public abstract void removeItem(FileItem item);

    public abstract void removeAllItems();

    public abstract void redraw(MainActivity activity);

    public abstract void redrawItems();

    public abstract void refreshActionBar();

    public abstract void addUIListener(UIListener listener);

    public abstract void removeUIListener(UIListener listener);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public abstract boolean onPrepareOptionsMenu(Menu menu);

    public abstract boolean onCreateOptionsMenu(Menu menu);

    public abstract boolean onOptionsItemSelected(MenuItem item);

    public abstract void onNewIntent(Intent intent);

    public abstract void settingsChanged();

    public abstract void onPause();

    public abstract void onDestroy();

    public abstract void onResume();
}