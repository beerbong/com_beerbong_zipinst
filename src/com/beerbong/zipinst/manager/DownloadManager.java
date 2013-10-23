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
