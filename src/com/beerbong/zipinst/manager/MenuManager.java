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

    public void onCreateOptionsMenu(Menu menu) {

        mMenu = menu;

        MenuInflater inflater = ((Activity) mContext).getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        ManagerFactory.getProManager().manage(this, ProManager.ManageMode.Menu);
    }

    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mContext.startActivity(new Intent(mContext, Settings.class));
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
}