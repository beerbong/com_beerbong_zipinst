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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.StoredItems;
import com.beerbong.zipinst.widget.FileItemsAdapter;
import com.beerbong.zipinst.widget.Item;
import com.mobeta.android.dslv.DragSortListView;

public class UIImpl extends UI implements FileItemsAdapter.FileItemsAdapterHolder {

    private List<UIListener> mListeners = new ArrayList<UIListener>();
    private MainActivity mActivity = null;
    private DragSortListView mFileList;

    private DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {

        public void drop(int from, int to) {
            StoredItems.move(from, to);
            redrawItems();
        }
    };

    protected UIImpl(MainActivity activity) {

        redraw(activity);
    }

    @Override
    public void showNoSuAlertAndExit() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle(R.string.root_needed_title);
        alert.setMessage(R.string.root_needed_summary);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                mActivity.finish();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    public void redraw(MainActivity activity) {

        boolean darkTheme = ManagerFactory.getPreferencesManager(activity).isDarkTheme();
        activity.setTheme(darkTheme ? R.style.AppTheme_Dark : R.style.AppTheme);

        mListeners.clear();

        this.mActivity = activity;

        activity.setContentView(R.layout.activity);
        init();
    }

    private void init() {
        mFileList = (DragSortListView) mActivity.findViewById(R.id.file_list);
        mFileList.setOnItemClickListener(this);
        mFileList.setDropListener(mDropListener);
        mFileList.setRemoveListener(new DragSortListView.RemoveListener() {
            
            @Override
            public void remove(int which) {
                FileItem item = StoredItems.getItem(which);
                removeItem(item);
            }
        });

        Item.OnItemClickListener cListener = new Item.OnItemClickListener() {

            @Override
            public void onClick(int id) {
                dispatchOnButtonClicked(id);
            }
        };

        Item chooseZip = (Item) mActivity.findViewById(R.id.choose_zip);
        chooseZip.setOnItemClickListener(cListener);
        Item installNow = (Item) mActivity.findViewById(R.id.install_now);
        installNow.setOnItemClickListener(cListener);

        redrawItems();

        onNewIntent(mActivity.getIntent());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = StoredItems.getItem(position);
        dispatchOnFileItemClicked(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        dispatchOnActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        dispatchOnCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        dispatchOnOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {

        dispatchOnNewIntent(intent);

    }

    @Override
    public void addItem(String realPath, String sdcardPath) {

        StoredItems.removeItem(realPath);

        FileItem item = new FileItem(realPath, sdcardPath.substring(sdcardPath.lastIndexOf("/") + 1),
                sdcardPath);

        if ("first".equals(ManagerFactory.getPreferencesManager().getZipPosition())) {
            StoredItems.addItem(item, 0);
        } else {
            StoredItems.addItem(item);
        }

        redrawItems();
    }

    @Override
    public void addUIListener(UIListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeUIListener(UIListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void removeAllItems() {

        StoredItems.removeItems();

        redrawItems();
    }

    public void removeItem(FileItem item) {

        StoredItems.removeItem(item.getKey());

        redrawItems();

    }

    @Override
    public boolean useDragAndDrop() {
        return ManagerFactory.getPreferencesManager().isUseDragAndDrop();
    }

    @Override
    public boolean canRemove() {
        return true;
    }

    @Override
    public boolean showPath() {
        return true;
    }

    @Override
    public boolean showSize() {
        return false;
    }

    @Override
    public boolean showDate() {
        return false;
    }

    private void dispatchOnActivityResult(int requestCode, int resultCode, Intent data) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onActivityResult(requestCode, resultCode, data);
        }
    }

    private void dispatchOnCreateOptionsMenu(Menu menu) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onCreateOptionsMenu(menu);
        }
    }

    private void dispatchOnOptionsItemSelected(MenuItem menuItem) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onOptionsItemSelected(menuItem);
        }
    }

    private void dispatchOnButtonClicked(int id) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onButtonClicked(id);
        }
    }

    private void dispatchOnFileItemClicked(FileItem item) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onFileItemClicked(item);
        }
    }

    private void dispatchOnNewIntent(Intent intent) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onNewIntent(intent);
        }
    }

    private void redrawItems() {

        mFileList.setAdapter(new FileItemsAdapter(mActivity, this, StoredItems.getItems()));
    }
}