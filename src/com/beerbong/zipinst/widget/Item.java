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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beerbong.zipinst.R;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Item extends LinearLayout {

    public static interface OnItemClickListener {

        public void onClick(int id);
    };

    private OnItemClickListener mItemClickListener = null;

    public Item(final Context context, AttributeSet attrs) {
        super(context, attrs);

        String title = null;
        String summary = null;
        Drawable icon = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Item);

        CharSequence s = a.getString(R.styleable.Item_title);
        if (s != null) {
            title = s.toString();
        }
        s = a.getString(R.styleable.Item_summary);
        if (s != null) {
            summary = s.toString();
        }
        Drawable d = a.getDrawable(R.styleable.Item_icon);
        if (d != null) {
            icon = d;
        }

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item, this, true);

        LinearLayout lLayout = (LinearLayout) this.getChildAt(0);
        RelativeLayout rLayout = (RelativeLayout) lLayout.getChildAt(1);

        TextView tView = (TextView) rLayout.getChildAt(0);
        tView.setText(title);

        ImageView iView = (ImageView) lLayout.getChildAt(0);
        iView.setImageDrawable(icon);

        TextView sView = (TextView) rLayout.getChildAt(1);
        sView.setText(summary);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackgroundColor(context.getResources().getColor(
                                android.R.color.holo_blue_light));
                        break;
                    case MotionEvent.ACTION_UP:
                        setBackgroundColor(context.getResources().getColor(
                                android.R.color.transparent));
                        if (mItemClickListener != null) {
                            mItemClickListener.onClick(Item.this.getId());
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }
}