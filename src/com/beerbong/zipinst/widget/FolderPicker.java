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

package com.beerbong.zipinst.widget;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beerbong.zipinst.R;

public class FolderPicker extends Dialog implements OnItemClickListener, OnClickListener {

    private ListView mFolders;
    private TextView mCurrentFolder;
    private Folder mPath;
    private Folder mFilePath;
    private File mRoot;
    private FolderAdapter mAdapter;
    private OnClickListener mListener;
    private boolean mAcceptFiles;
    private View mOkButton;
    private String[] mFileExtensions;
    private int[] mFileDrawables;

    public FolderPicker(Context context, OnClickListener listener, String defaultFolder) {
        this(context, listener, defaultFolder, null, null, false);
    }

    public FolderPicker(Context context, OnClickListener listener, String defaultFolder,
            String[] fileExtensions, int[] fileDrawables, boolean acceptFiles) {
        super(context);
        mListener = listener;
        mAcceptFiles = acceptFiles;
        mFileExtensions = fileExtensions;
        mFileDrawables = fileDrawables;
        setTitle(acceptFiles ? R.string.pick_file : R.string.pick_folder);
        setContentView(R.layout.picker_folders);

        mRoot = new File("/");

        mOkButton = findViewById(R.id.ok_btn);
        mOkButton.setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        mCurrentFolder = (TextView) findViewById(R.id.current_folder);
        mCurrentFolder.setSelected(true);
        mFolders = (ListView) findViewById(R.id.folders);
        mFolders.setOnItemClickListener(this);

        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(150);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        mFolders.setLayoutAnimation(controller);

        mAdapter = new FolderAdapter();
        mFolders.setAdapter(mAdapter);
        if (defaultFolder != null) {
            mPath = new Folder(defaultFolder);
        }
        if (mPath == null || !mPath.exists()) {
            mPath = new Folder(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        updateAdapter();
    }

    public String getPath() {
        if (!mAcceptFiles) {
            return mPath.getAbsolutePath();
        } else if (mFilePath != null) {
            return mFilePath.getAbsolutePath();
        } else {
            return null;
        }
    }

    public void onClick(View v) {
        if (v == mOkButton && mListener != null) {
            mListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
        }
        dismiss();
    }

    private void updateAdapter() {
        mCurrentFolder.setText(mPath.getAbsolutePath());
        mAdapter.clear();
        if (!mPath.equals(mRoot)) {
            mAdapter.add(new Folder(mPath, true));
        }
        File[] dirs = mPath.listFiles(mDirFilter);
        Arrays.sort(dirs);
        for (int i = 0; i < dirs.length; i++) {
            mAdapter.add(new Folder(dirs[i]));
        }
        if (mAcceptFiles) {
            File[] files = mPath.listFiles(mFileFilter);
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                if (mFileExtensions == null) {
                    mAdapter.add(new Folder(files[i]));
                } else {
                    for (int j=0;j<mFileExtensions.length;j++) {
                        if (files[i].getName().endsWith(mFileExtensions[j])) {
                            mAdapter.add(new Folder(files[i], mFileDrawables[j]));
                        }
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
        mFolders.setSelection(0);
        mFolders.startLayoutAnimation();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAcceptFiles) {
            Folder item = (Folder) mAdapter.getItem(position);
            if (item.isDirectory()) {
                mPath = item;
                updateAdapter();
                mFilePath = null;
            } else {
                mCurrentFolder.setText(item.getAbsolutePath());
                mFilePath = item;
                mListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
                dismiss();
            }
        } else {
            mPath = (Folder) mAdapter.getItem(position);
            updateAdapter();
        }
    }

    private FileFilter mDirFilter = new FileFilter() {

        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    private FileFilter mFileFilter = new FileFilter() {

        public boolean accept(File file) {
            return file.isFile();
        }
    };

    class FolderAdapter extends BaseAdapter {

        ArrayList<Folder> mFolders = new ArrayList<Folder>();
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        private Drawable mFileDrawable;

        public FolderAdapter() {
            Resources res = getContext().getResources();
            mFileDrawable = res.getDrawable(R.drawable.file);
        }

        public int getCount() {
            return mFolders.size();
        }

        public void add(Folder folder) {
            mFolders.add(folder);
        }

        public void clear() {
            mFolders.clear();
        }

        public Object getItem(int position) {
            return mFolders.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.picker_folder, parent, false);
            Folder folder = mFolders.get(position);
            TextView name = (TextView) v.findViewById(R.id.folder_name);

            Drawable drawable = null;
            Resources res = getContext().getResources();
            if (folder.isParent) {
                name.setText("[..]");
                drawable = res.getDrawable(R.drawable.folder);
            } else {
                name.setText(folder.getName());
                if (folder.isDirectory()) {
                    drawable = res.getDrawable(R.drawable.folder);
                } else if (folder.drawable > -1) {
                    drawable = getContext().getResources().getDrawable(folder.drawable);
                } else {
                    drawable = mFileDrawable;
                }
            }
            v.findViewById(R.id.folder_icon).setBackground(drawable);
            return v;
        }
    }

    @SuppressWarnings("serial")
    class Folder extends File {

        protected int drawable = -1;
        protected boolean isParent;

        public Folder(File file) {
            super(file.getAbsolutePath());
        }

        public Folder(File file, int drawable) {
            super(file.getAbsolutePath());
            this.drawable = drawable;
        }

        public Folder(File file, boolean unused) {
            super(file.getParent() == null ? "/" : file.getParent());
            isParent = true;
        }

        public Folder(String path) {
            super(path);
        }
    }
}
