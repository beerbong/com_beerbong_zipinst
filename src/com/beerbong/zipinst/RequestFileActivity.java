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

package com.beerbong.zipinst;

import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.ui.UIActivity;

public class RequestFileActivity extends UIActivity {

    private static final int REQUEST_PICK_FILE = 203;

    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public View getMainView() {
        return null;
    }

    @Override
    public void create(boolean isNew) {

        PackageManager packageManager = getPackageManager();
        Intent test = new Intent(Intent.ACTION_GET_CONTENT);
        test.setType("application/zip*");
        List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                PackageManager.GET_ACTIVITIES);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setType("application/zip");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        } else {
            Toast.makeText(this, R.string.error_file_manager, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FILE) {
            if (data == null) {
                finish();
                return;
            }

            Uri uri = data.getData();

            String filePath = uri.getPath();
            StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);

            filePath = Files.getPathFromUri(getCore(), uri);

            sPlugin.addFileItemToStore(filePath);

        }
        finish();
    }
}
