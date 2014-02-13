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

package com.beerbong.zipinst.ui.preference;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class UIPreferenceCategory extends PreferenceCategory {

    private int mTextColor = 0;
    private int mAppearanceMedium = 0;

    public UIPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public UIPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UIPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Context context = getContext();
        Resources res = context.getResources();
        if (mTextColor == 0) {
            TypedValue typedValue = new TypedValue();
            ((Activity) context).getTheme().resolveAttribute(R.attr.fragmentTitleColor, typedValue, true);
            mTextColor = typedValue.resourceId;
            typedValue = new TypedValue();
            ((Activity) context).getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, typedValue, true);
            mAppearanceMedium = typedValue.resourceId;
        }
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextAppearance(context, mAppearanceMedium);
            titleView.setTextColor(res.getColor(mTextColor));
            float size = res.getDimension(R.dimen.fragment_title_size);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            //titleView.setBackgroundColor(mTextColor);
        }
        ((UIPlugin) CoreFactory.getCore().getPlugin(Core.PLUGIN_UI)).redraw(view);
    }
}
