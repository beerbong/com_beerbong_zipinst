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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerbong.zipinst.R;

public class ButtonItem extends LinearLayout {

    public static interface OnButtonItemClickListener {

        public void onButtonItemClick(int id);
    };

    private TextView mTitleView;
    private int mDownColor;
    private OnButtonItemClickListener mButtonItemClickListener = null;
    private ColorStateList mDefaultColors;

    public ButtonItem(final Context context, AttributeSet attrs) {
        super(context, attrs);

        String title = null;
        Drawable icon = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonItem);

        CharSequence s = a.getString(R.styleable.ButtonItem_title);
        if (s != null) {
            title = s.toString();
        }
        Drawable d = a.getDrawable(R.styleable.ButtonItem_icon);
        if (d != null) {
            icon = d;
        }

        mDownColor = a.getColor(R.styleable.ButtonItem_downColor, android.R.color.holo_blue_light);

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.button_item, this, true);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mTitleView.setText(title);
        mDefaultColors = mTitleView.getTextColors();

        ImageView iView = (ImageView) view.findViewById(R.id.icon);
        iView.setImageDrawable(icon);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!isEnabled()) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackgroundColor(mDownColor);
                        break;
                    case MotionEvent.ACTION_UP:
                        setBackgroundColor(context.getResources().getColor(
                                android.R.color.transparent));
                        if (mButtonItemClickListener != null) {
                            mButtonItemClickListener.onButtonItemClick(ButtonItem.this.getId());
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void setOnButtonItemClickListener(OnButtonItemClickListener buttonItemClickListener) {
        mButtonItemClickListener = buttonItemClickListener;
    }

    public void setTitle(int resourceId) {
        mTitleView.setText(resourceId);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mTitleView != null) {
            if (enabled) {
                mTitleView.setTextColor(mDefaultColors);
            } else {
                mTitleView.setTextColor(R.color.gray);
            }
        }
    }
}