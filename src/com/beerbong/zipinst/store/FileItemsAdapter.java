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

package com.beerbong.zipinst.store;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;
import com.beerbong.zipinst.io.Strings;


public class FileItemsAdapter extends ArrayAdapter<FileItem> {

    public interface FileItemsAdapterHolder {

        public boolean useDragAndDrop();

        public boolean canRemove();

        public boolean showPath();

        public boolean showSize();

        public boolean showDate();

        public int getItemLayoutId();
    }

    private UIPlugin mUiPlugin;
    private FileItemsAdapterHolder mHolder;

    public FileItemsAdapter(Core core, FileItemsAdapterHolder holder, List<FileItem> items) {
        super(core.getContext(), holder.getItemLayoutId(), items);

        mUiPlugin = (UIPlugin) core.getPlugin(Core.PLUGIN_UI);

        mHolder = holder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        FileItem item = getItem(position);

        if (convertView == null) {
            itemView = new LinearLayout(getContext());
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(mHolder.getItemLayoutId(), itemView, true);
        } else {
            itemView = (LinearLayout) convertView;
        }
        TextView title = (TextView) itemView.findViewById(R.id.title);
        TextView summary = (TextView) itemView.findViewById(R.id.summary);

        title.setText(item.getName());

        String summaryStr = "";
        if (mHolder.showPath()) {
            summaryStr += item.getShortPath();
            if (mHolder.showSize() || mHolder.showDate()) {
                summaryStr += ", ";
            }
        }
        if (mHolder.showSize()) {
            if (item.getSize() >= 0) {
                summaryStr += Strings.formatSize(item.getSize());
            } else if (item.getKey() != null) {
                File file = new File(item.getKey());
                if (file.exists()) {
                    summaryStr += Strings.formatSize(file.length());
                }
            }
            if (mHolder.showDate()) {
                summaryStr += ", ";
            }
        }
        if (mHolder.showDate()) {
            summaryStr += Strings.formatDate(new File(item.getKey()).lastModified());
        }
        if (item.isDelete()) {
            summaryStr += " " + getContext().getResources().getString(R.string.to_be_deleted);
        }
        summary.setText(summaryStr);

        if (!mHolder.useDragAndDrop()) {
            itemView.findViewById(R.id.grabber).setVisibility(View.GONE);
        }
        if (!mHolder.canRemove()) {
            ((ImageView)itemView.findViewById(R.id.trash)).setImageDrawable(null);
            itemView.findViewById(R.id.trash_separator).setVisibility(View.GONE);
        }
        if (item.getImage() != 0) {
            ((ImageView) itemView.findViewById(R.id.grabber)).setImageDrawable(getContext()
                    .getResources().getDrawable(item.getImage()));
        } else if (item.getImageAttr() != 0) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[] {item.getImageAttr()});
            ((ImageView) itemView.findViewById(R.id.grabber)).setImageDrawable(ta.getDrawable(0));
            ta.recycle();
        }

        mUiPlugin.redraw(itemView);

        return itemView;
    }
}
