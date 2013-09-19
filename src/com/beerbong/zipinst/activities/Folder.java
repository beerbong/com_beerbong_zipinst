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

package com.beerbong.zipinst.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.FileManager;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.widget.BackActivity;
import com.beerbong.zipinst.widget.FileItemsAdapter;
import com.mobeta.android.dslv.DragSortListView;

public class Folder extends BackActivity implements FileItemsAdapter.FileItemsAdapterHolder,
        OnItemClickListener, DragSortListView.RemoveListener {

    private DragSortListView mFileList;
    private List<FileItem> mItems = new ArrayList<FileItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.folder);

        setTitle(ManagerFactory.getPreferencesManager().getFolder());

        mFileList = (DragSortListView) findViewById(R.id.file_list);
        mFileList.setOnItemClickListener(this);
        mFileList.setRemoveListener(this);

        redrawItems();
    }

    @Override
    public void remove(int which) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_remove_file_title);

        final File file = new File(mItems.get(which).getKey());

        alert.setMessage(this.getResources().getString(R.string.alert_remove_file_summary,
                new Object[] { file.getAbsolutePath() }));

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                boolean done = file.delete();
                Toast.makeText(Folder.this, done ? R.string.alert_remove_file_done
                        : R.string.alert_remove_file_not_done, Toast.LENGTH_LONG);

                redrawItems();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                redrawItems();
            }
        });

        alert.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = mItems.get(position);
        ManagerFactory.getFileManager().addFile(item.getKey());

        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean useDragAndDrop() {
        return false;
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public boolean showPath() {
        return false;
    }

    @Override
    public boolean showSize() {
        return true;
    }

    @Override
    public boolean showDate() {
        return true;
    }

    private void redrawItems() {
        File folder = new File(ManagerFactory.getPreferencesManager().getFolder());
        File[] files = folder.listFiles();

        Arrays.sort(files, new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {
                String name1 = lhs.getName().toLowerCase();
                String name2 = rhs.getName().toLowerCase();
                return name1.compareTo(name2);
            }

        });

        FileManager fManager = ManagerFactory.getFileManager();

        mItems = new ArrayList<FileItem>();
        for (File file : files) {

            if (file.getName().toLowerCase().endsWith(".zip")
                    || file.getName().toLowerCase().endsWith(".sh") && !file.isDirectory()) {

                String sdcardPath = fManager.getPath(file.getAbsolutePath());
                mItems.add(new FileItem(file.getAbsolutePath(), file.getName(), sdcardPath));
            }
        }

        mFileList.setAdapter(new FileItemsAdapter(this, this, mItems));
    }
}