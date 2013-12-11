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
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;
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

    private AdView mAdView;

    protected UIImpl(MainActivity activity) {

        redraw(activity);
    }

    @Override
    public void showNoSuAlertAndExit() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle(R.string.root_needed_title);
        alert.setMessage(R.string.root_needed_summary);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.root_needed_close, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                mActivity.finish();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton(R.string.root_needed_continue, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
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
        settingsChanged();

        onNewIntent(mActivity.getIntent());

        mAdView = (AdView) mActivity.findViewById(R.id.adview);
        if (ManagerFactory.getProManager(mActivity).iAmPro()) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.loadAd(new AdRequest());
            mAdView.setAdListener(new AdListener() {

                @Override
                public void onDismissScreen(Ad arg0) {
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
                    mAdView.loadAd(new AdRequest());
                }

                @Override
                public void onLeaveApplication(Ad arg0) {
                }

                @Override
                public void onPresentScreen(Ad arg0) {
                }

                @Override
                public void onReceiveAd(Ad arg0) {
                }
                
            });
        }
    }

    @Override
    public void refreshActionBar() {
        mActivity.invalidateOptionsMenu();
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
    public boolean onPrepareOptionsMenu(Menu menu) {

        dispatchOnPrepareOptionsMenu(menu);

        return true;
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
    public void onPause() {

        dispatchOnPause();

    }

    @Override
    public void onDestroy() {

        mAdView.destroy();

    }

    @Override
    public void addItem(String realPath, String sdcardPath, boolean delete) {

        StoredItems.removeItem(realPath);

        FileItem item = new FileItem(realPath, sdcardPath.substring(sdcardPath.lastIndexOf("/") + 1),
                sdcardPath, delete);

        if ("first".equals(ManagerFactory.getPreferencesManager().getZipPosition())) {
            StoredItems.addItem(item, 0);
        } else {
            StoredItems.addItem(item);
        }

        redrawItems();
    }

    @Override
    public void redrawItems() {

        mFileList.setAdapter(new FileItemsAdapter(mActivity, this, StoredItems.getItems()));
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

    @Override
    public void settingsChanged() {
    }

    private void dispatchOnActivityResult(int requestCode, int resultCode, Intent data) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onActivityResult(requestCode, resultCode, data);
        }
    }

    private void dispatchOnPrepareOptionsMenu(Menu menu) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onPrepareOptionsMenu(menu);
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

    private void dispatchOnPause() {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onPause();
        }
    }
}