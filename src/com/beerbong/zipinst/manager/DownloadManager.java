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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.DownloadTask;

public class DownloadManager extends Manager {

    private List<DownloadTask> mList = new ArrayList<DownloadTask>();

    protected DownloadManager(Context context) {
        super(context);

        UI.getInstance().addUIListener(new UIAdapter() {

            @Override
            public void onPause() {
                cancelAll();
            }

        });

    }

    public void addDownloadTask(DownloadTask task) {
        mList.add(task);
    }

    public void removeDownloadTask(DownloadTask task) {
        mList.remove(task);
    }

    public void cancelAll() {
        int size = mList.size();
        for (int i = size - 1; i >= 0; i--) {
            DownloadTask task = (DownloadTask) mList.get(i);
            task.detach();
            task.cancel(true);
        }
    }
}
