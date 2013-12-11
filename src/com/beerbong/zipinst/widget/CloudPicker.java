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

package com.beerbong.zipinst.widget;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.activities.Dropbox;
import com.beerbong.zipinst.activities.GoogleDrive;

public class CloudPicker extends Dialog implements OnItemClickListener {

    private ListView mList;
    private CloudAdapter mAdapter;
    private String mBackupName;

    public CloudPicker(Context context, String backupName) {
        super(context);
        setTitle(R.string.cloud_service);
        setContentView(R.layout.picker_cloud);

        mBackupName = backupName;

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(this);

        mAdapter = new CloudAdapter();
        mList.setAdapter(mAdapter);

        mAdapter.add(new Cloud(R.string.dropbox_title, R.drawable.ic_dropbox_icon, Dropbox.class));
        mAdapter.add(new Cloud(R.string.drive_title, R.drawable.ic_drive_icon, GoogleDrive.class));
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cloud item = (Cloud) mAdapter.getItem(position);
        Context context = getContext();
        Intent intent = new Intent(context, item.clazz);
        if (mBackupName != null) {
            intent.putExtra("backupName", mBackupName);
        }
        context.startActivity(intent);
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
            name.setText(res.getString(cloud.name));
            v.findViewById(R.id.folder_icon).setBackground(res.getDrawable(cloud.drawable));
            return v;
        }
    }

    @SuppressWarnings("serial")
    class Cloud {

        protected int drawable;
        protected int name;
        protected Class<?> clazz;

        public Cloud(int name, int drawable, Class<?> clazz) {
            this.name = name;
            this.drawable = drawable;
            this.clazz = clazz;
        }

    }
}
