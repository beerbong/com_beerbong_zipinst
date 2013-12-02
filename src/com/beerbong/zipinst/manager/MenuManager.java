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

package com.beerbong.zipinst.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.activities.Recovery;
import com.beerbong.zipinst.activities.Settings;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIListener;
import com.beerbong.zipinst.util.FileItem;

public class MenuManager extends Manager implements UIListener {

    private Menu mMenu;

    protected MenuManager(Context context) {
        super(context);

        UI.getInstance().addUIListener(this);
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.rules).setVisible(ManagerFactory.getPreferencesManager().hasRules());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {

        mMenu = menu;

        MenuInflater inflater = ((Activity) mContext).getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        ManagerFactory.getProManager().manage(this, ProManager.ManageMode.Menu);
    }

    @Override
    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mContext.startActivity(new Intent(mContext, Settings.class));
                break;
            case R.id.rules:
                ManagerFactory.getFileManager().applyRules();
                break;
            case R.id.backup:
                ManagerFactory.getRebootManager().showBackupDialog(mContext);
                break;
            case R.id.restore:
                ManagerFactory.getRebootManager().showRestoreDialog(mContext);
                break;
            case R.id.advanced_recovery:
                mContext.startActivity(new Intent(mContext, Recovery.class));
                break;
            case R.id.loadlist:
                ManagerFactory.getFileManager().loadList();
                break;
            case R.id.savelist:
                ManagerFactory.getFileManager().saveList();
                break;
            case R.id.downloadzip:
                ManagerFactory.getFileManager().downloadZip();
                break;
        }
    }

    @Override
    public void onButtonClicked(int id) {
    }

    @Override
    public void onFileItemClicked(FileItem item) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onPause() {
    }
}