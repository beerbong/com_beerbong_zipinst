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

package com.beerbong.zipinst.io;

import android.view.Menu;
import android.view.MenuItem;

import com.beerbong.zipinst.ui.IFragment;

public class Menus {

    public static void onPrepareOptionsMenu(Menu menu, IFragment fragment) {
        if (fragment == null) {
            return;
        }
        for (int i = 0; menu != null && i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            boolean visible = false;
            int[] visibleMenuItems = fragment.getVisibleMenuItems();
            if (visibleMenuItems != null) {
                for (int id : visibleMenuItems) {
                    if (id != 0 && item.getItemId() == id) {
                        visible = true;
                        break;
                    }
                }
            }
            item.setVisible(visible);
        }
    }
}
