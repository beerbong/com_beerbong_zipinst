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

package com.beerbong.zipinst.ui.widget;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.cloud.CloudStorage;

public class CloudPicker extends Dialog implements OnItemClickListener {

    public interface CloudPickerListener {

        public void cloudStorageSelected(int id);
    }

    private CloudPickerListener mListener;
    private ListView mList;
    private CloudAdapter mAdapter;

    public CloudPicker(Context context, CloudPickerListener listener) {
        super(context);
        setTitle(R.string.cloud_service);
        setContentView(R.layout.picker_cloud);

        mListener = listener;

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(this);

        mAdapter = new CloudAdapter();
        mList.setAdapter(mAdapter);

        Resources res = context.getResources();

        mAdapter.add(new Cloud(CloudStorage.STORAGE_NONE, R.string.storage_none, res.getDrawable(R.drawable.ic_blank)));
        mAdapter.add(new Cloud(CloudStorage.STORAGE_DROPBOX, R.string.dropbox, res.getDrawable(R.drawable.ic_dropbox_icon)));
        mAdapter.add(new Cloud(CloudStorage.STORAGE_DRIVE, R.string.drive, res.getDrawable(R.drawable.ic_drive_icon)));
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cloud item = (Cloud) mAdapter.getItem(position);
        mListener.cloudStorageSelected(item.getId());
        dismiss();
    }

    class CloudAdapter extends BaseAdapter {

        ArrayList<Cloud> mClouds = new ArrayList<Cloud>();
        LayoutInflater mInflater = LayoutInflater.from(getContext());

        public CloudAdapter() {
        }

        public int getCount() {
            return mClouds.size();
        }

        public void add(Cloud cloud) {
            mClouds.add(cloud);
        }

        public void clear() {
            mClouds.clear();
        }

        public Object getItem(int position) {
            return mClouds.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.picker_folder, parent, false);
            Cloud cloud = mClouds.get(position);
            TextView name = (TextView) v.findViewById(R.id.folder_name);

            Resources res = getContext().getResources();
            name.setText(res.getString(cloud.getName()));
            if (VERSION.SDK_INT > 15) {
                v.findViewById(R.id.folder_icon).setBackgroundDrawable(cloud.getDrawable());
            } else {
                v.findViewById(R.id.folder_icon).setBackground(cloud.getDrawable());
            }
            return v;
        }
    }

    class Cloud {

        private int mId;
        private Drawable mDrawable;
        private int mName;

        public Cloud(int id, int name, Drawable drawable) {
            mId = id;
            mName = name;
            mDrawable = drawable;
        }

        protected int getId() {
            return mId;
        }

        protected int getName() {
            return mName;
        }

        protected Drawable getDrawable() {
            return mDrawable;
        }

    }
}
