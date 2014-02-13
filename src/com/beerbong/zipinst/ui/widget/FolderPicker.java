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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
    private Drawable[] mFileDrawables;

    public FolderPicker(Context context, OnClickListener listener, String defaultFolder) {
        this(context, listener, defaultFolder, null, null, false);
    }

    public FolderPicker(Context context, OnClickListener listener, String defaultFolder,
            String[] fileExtensions, Drawable[] fileDrawables, boolean acceptFiles) {
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
                            mAdapter.add(new Folder(files[i],
                                    mFileDrawables != null ? mFileDrawables[j] : null));
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
        private Drawable mFolderDrawable;

        public FolderAdapter() {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[] {R.attr.fileIcon});
            mFileDrawable = ta.getDrawable(0);
            ta.recycle();
            ta = getContext().getTheme().obtainStyledAttributes(new int[] {R.attr.folderIcon});
            mFolderDrawable = ta.getDrawable(0);
            ta.recycle();
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
            if (folder.isParent) {
                name.setText("[..]");
                drawable = mFolderDrawable;
            } else {
                name.setText(folder.getName());
                if (folder.isDirectory()) {
                    drawable = mFolderDrawable;
                } else if (folder.drawable != null) {
                    drawable = folder.drawable;
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

        protected Drawable drawable;
        protected boolean isParent;

        public Folder(File file) {
            super(file.getAbsolutePath());
        }

        public Folder(File file, Drawable drawable) {
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
