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

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.util.StoredItems;
import com.beerbong.zipinst.util.ZipItem;
import com.beerbong.zipinst.widget.Item;
import com.beerbong.zipinst.widget.TouchInterceptor;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class UIImpl extends UI {

    private class ZipItemsAdapter extends ArrayAdapter<ZipItem> {

        public ZipItemsAdapter(List<ZipItem> items) {
            super(mActivity,
                    ManagerFactory.getPreferencesManager().isUseDragAndDrop() ? R.layout.order_item
                            : R.layout.item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            ZipItem item = getItem(position);

            boolean useDad = ManagerFactory.getPreferencesManager().isUseDragAndDrop();

            if (convertView == null) {
                itemView = new LinearLayout(getContext());
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                vi.inflate(useDad ? R.layout.order_item : R.layout.item, itemView, true);
            } else {
                itemView = (LinearLayout) convertView;
            }
            TextView title = (TextView) itemView.findViewById(R.id.title);
            TextView summary = (TextView) itemView.findViewById(R.id.summary);

            title.setText(item.getName());
            summary.setText(item.getPath());

            return itemView;
        }

    }

    private static final String TAG = "UIImpl";

    private List<UIListener> mListeners = new ArrayList<UIListener>();
    private MainActivity mActivity = null;
    private TouchInterceptor mFileList;

    private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {

        public void drop(int from, int to) {
            StoredItems.move(from, to);
            redrawItems();
        }
    };

    protected UIImpl(MainActivity activity) {

        redraw(activity);
    }

    @Override
    public void requestRestart() {
        Intent mainIntent = new Intent(mActivity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(mainIntent);
        mActivity.finish();
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
        mFileList = (TouchInterceptor) mActivity.findViewById(R.id.file_list);
        mFileList.setOnItemClickListener(this);
        mFileList.setDropListener(mDropListener);

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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ZipItem item = StoredItems.getItem(position);
        dispatchOnZipItemClicked(item);
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
    public void addItem(String realPath, String sdcardPath) {

        StoredItems.removeItem(realPath);

        ZipItem item = new ZipItem(realPath, sdcardPath.substring(sdcardPath.lastIndexOf("/") + 1),
                sdcardPath);

        StoredItems.addItem(item);

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

    public void removeItem(ZipItem item) {

        StoredItems.removeItem(item.getKey());

        redrawItems();

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

    private void dispatchOnZipItemClicked(ZipItem item) {
        int size = mListeners.size(), i = 0;
        for (; i < size; i++) {
            mListeners.get(i).onZipItemClicked(item);
        }
    }

    private void redrawItems() {

        mFileList.setAdapter(new ZipItemsAdapter(StoredItems.getItems()));
    }
}