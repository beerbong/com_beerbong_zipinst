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

package com.beerbong.zipinst;

import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.NoSuException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        UI.create(this);

        super.onCreate(savedInstanceState);

        try {
            ManagerFactory.start(this);
        } catch (NoSuException ex) {
            // device not rooted or user clicked no
            UI.getInstance().showNoSuAlertAndExit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        UI.getInstance().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return UI.getInstance().onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return UI.getInstance().onOptionsItemSelected(item);

    }

    @Override
    protected void onNewIntent(Intent intent) {

        UI.getInstance().onNewIntent(intent);

    }
}