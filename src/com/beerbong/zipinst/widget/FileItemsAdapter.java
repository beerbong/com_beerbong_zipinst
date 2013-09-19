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
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.FileItem;


public class FileItemsAdapter extends ArrayAdapter<FileItem> {

    public interface FileItemsAdapterHolder {

        public boolean useDragAndDrop();

        public boolean canRemove();

        public boolean showPath();

        public boolean showSize();

        public boolean showDate();
    }

    private FileItemsAdapterHolder mHolder;

    public FileItemsAdapter(Context context, FileItemsAdapterHolder holder, List<FileItem> items) {
        super(context, R.layout.order_item, items);

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
            vi.inflate(R.layout.order_item, itemView, true);
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
            summaryStr += Constants.formatSize(new File(item.getKey()).length());
            if (mHolder.showDate()) {
                summaryStr += ", ";
            }
        }
        if (mHolder.showDate()) {
            summaryStr += Constants.formatDate(new File(item.getKey()).lastModified());
        }
        summary.setText(summaryStr);

        if (!mHolder.useDragAndDrop()) {
            itemView.findViewById(R.id.grabber).setVisibility(View.GONE);
        }
        if (!mHolder.canRemove()) {
            ((ImageView)itemView.findViewById(R.id.trash)).setImageDrawable(null);
            itemView.findViewById(R.id.trash_separator).setVisibility(View.GONE);
        }

        return itemView;
    }
}
