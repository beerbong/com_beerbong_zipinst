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

package com.beerbong.zipinst;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.license.LicensePlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;
import com.beerbong.zipinst.fragment.FragmentAbout;
import com.beerbong.zipinst.fragment.FragmentBackupRestore;
import com.beerbong.zipinst.fragment.FragmentGoo;
import com.beerbong.zipinst.fragment.FragmentInstall;
import com.beerbong.zipinst.fragment.FragmentRecovery;
import com.beerbong.zipinst.fragment.FragmentRules;
import com.beerbong.zipinst.fragment.FragmentSettings;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.http.Downloads;
import com.beerbong.zipinst.io.Menus;
import com.beerbong.zipinst.store.FileItemStore;
import com.beerbong.zipinst.ui.IFragment;
import com.beerbong.zipinst.ui.UIActivity;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.DrawerAdapter;
import com.beerbong.zipinst.ui.widget.DrawerItem;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends UIActivity {

    public static final String BUNDLE_FILE_PATHS = "bundle_file_paths";
    public static final String BUNDLE_URL = "bundle_url";
    public static final String BUNDLE_NAME = "bundle_name";
    public static final String BUNDLE_MD5 = "bundle_md5";

    private static final String SELECTED_ITEM = "SELECTED_ITEM";

    private List<DrawerItem> mItems;
    private IFragment[] mFragments;
    private IFragment mFragment;
    private int mSelectedItem;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private AdView mAdView;

    @Override
    public void create(boolean isNew) {

        Context context = getCore().getContext();

        mItems = new ArrayList<DrawerItem>();
        // TODO create selected icons (orange and blue)
        mItems.add(new DrawerItem(context, FragmentInstall.class, R.string.install_title, R.attr.flashIcon, 0));
        mItems.add(new DrawerItem(context, FragmentRules.class, R.string.rules_title, R.attr.acceptIcon, 0));
        mItems.add(new DrawerItem(context, FragmentBackupRestore.class, R.string.backuprestore_title, R.attr.backupRestoreIcon, 0));
        mItems.add(new DrawerItem(context, FragmentRecovery.class, R.string.recovery_fragment_title, R.attr.gridIcon, 0));
        mItems.add(new DrawerItem(context, FragmentGoo.class, R.string.goo_title, R.attr.webIcon, 0));
        mItems.add(new DrawerItem(context, FragmentSettings.class, R.string.settings_title, R.attr.settingsIcon, 0));
        mItems.add(new DrawerItem(context, FragmentAbout.class, R.string.about_title, R.attr.starIcon, 0));

        mFragments = new IFragment[mItems.size()];

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerList.setAdapter(new DrawerAdapter(this, mItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                ((UIPlugin) getCore().getPlugin(Core.PLUGIN_UI)).redraw(mDrawerLayout);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        boolean toInstall = isNew;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String[] filePaths = extras.getStringArray(BUNDLE_FILE_PATHS);
            if (filePaths != null && filePaths.length > 0) {
                FileItemStore.removeItems();
                StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);
                for (String path : filePaths) {
                    sPlugin.addFileItemToStore(path);
                }
                toInstall = true;
            }
            final String url = extras.getString(BUNDLE_URL);
            if (url != null && !url.isEmpty()) {
                final String fileName = extras.getString(BUNDLE_NAME);
                final String md5 = extras.getString(BUNDLE_MD5);
                if (fileName != null && md5 != null) {
                    String message = context.getResources().getString(R.string.new_rom_found_summary,
                            new Object[] { fileName });
                    Dialog.dialog(context, message, R.string.new_rom_found_title, true,
                            R.string.new_version_download, android.R.string.cancel,
                            new OnDialogClosedListener() {

                                @Override
                                public void dialogOk() {
                                    new DownloadFile(getCore(), url, fileName, md5);
                                }

                                @Override
                                public void dialogCancel() {
                                }

                            });
                } else {
                    new DownloadFile(getCore(), url, fileName, md5);
                }
            }
        }

        if (toInstall) {
            selectItem(0);
        }

        mAdView = (AdView) findViewById(R.id.adview);

        LicensePlugin lPlugin = (LicensePlugin) getCore().getPlugin(Core.PLUGIN_LICENSE);
        if (lPlugin.hasProCode()) {
            mAdView.setVisibility(View.GONE);
        } else {
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
            }
            mAdView.loadAd(new AdRequest.Builder().build());
            mAdView.setAdListener(new AdListener() {
    
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }
    
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    mAdView.loadAd(new AdRequest.Builder().build());
                }
    
                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }
    
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }
    
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
                
            });

            Toast.makeText(this, R.string.consider_becoming_a_pro, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFragment != null) {
            ((Fragment) mFragment).onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void restore(Bundle savedInstanceState) {
        int item = savedInstanceState.getInt(SELECTED_ITEM);
        selectItem(item);
    }

    @Override
    public void save(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public View getMainView() {
        return this.findViewById(R.id.drawer_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Menus.onPrepareOptionsMenu(menu, mFragment);
        mFragment.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        mFragment.onOptionsItemSelected(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Downloads.cancelAll();
        mAdView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
        getCore().destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdView.resume();
    }

    public void moveToStart() {
        selectItem(0);
        invalidateOptionsMenu();
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mSelectedItem = position;
        if (position >= 0 && position < mItems.size()) {
            if ((mFragment = mFragments[position]) == null) {
                try {
                    DrawerItem item = mItems.get(position);
                    mFragment = (IFragment) item.getFragmentClass().newInstance();
                    mFragments[position] = mFragment;
                    for (DrawerItem i : mItems) {
                        i.setChecked(i.equals(item));
                    }
                } catch (Exception ex) {
                    // should never get here
                    ex.printStackTrace();
                }
            }
        }
        if (mFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, (Fragment) mFragment).commit();
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
