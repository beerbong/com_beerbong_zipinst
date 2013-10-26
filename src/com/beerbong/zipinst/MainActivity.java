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

package com.beerbong.zipinst;

import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.NoSuException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        UI.create(this);

        super.onCreate(savedInstanceState);

        try {
            ManagerFactory.start(this);
        } catch (NoSuException ex) {
            // device not rooted or user clicked no
            UI.getInstance().showNoSuAlertAndExit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        UI.getInstance().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return UI.getInstance().onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return UI.getInstance().onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return UI.getInstance().onOptionsItemSelected(item);

    }

    @Override
    protected void onNewIntent(Intent intent) {

        UI.getInstance().onNewIntent(intent);

    }

    @Override
    public void onPause() {

        UI.getInstance().onPause();
        super.onPause();

    }
}