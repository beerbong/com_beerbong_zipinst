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

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.Core.CoreListener;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;
import com.beerbong.zipinst.core.plugins.update.RomUpdater;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.ui.UIActivity;

public class IntroActivity extends UIActivity implements CoreListener {

    private static final String CURRENT_MESSAGE = "CURRENT_MESSAGE";
    private static final String ON_DISCLAIMER = "ON_DISCLAIMER";

    private TextView mTextVersion;
    private TextView mTextMessage;
    private int mCurrentMessage;
    private boolean mOnDisclaimer = false;

    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public View getMainView() {
        return findViewById(R.id.intro_layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_intro);
        mTextMessage = (TextView) findViewById(R.id.text_message);

        CoreFactory.init(this, this);

        super.onCreate(savedInstanceState);

        if (getCore().isStarted()) {
            forward();
        }
    }

    @Override
    public void create(boolean isNew) {

        mTextVersion = (TextView) findViewById(R.id.text_version);
        mCurrentMessage = R.string.loading;

        mTextVersion.setText(getResources().getString(R.string.version, getCore().getVersion().toString()));

    }

    @Override
    public void restore(Bundle savedInstanceState) {
        mTextMessage.setText(savedInstanceState.getInt(CURRENT_MESSAGE));
        mOnDisclaimer = savedInstanceState.getBoolean(ON_DISCLAIMER);
        if (mOnDisclaimer) {
            switchToDisclaimer();
        }
    }

    @Override
    public void save(Bundle outState) {
        outState.putInt(CURRENT_MESSAGE, mCurrentMessage);
        outState.putBoolean(ON_DISCLAIMER, mOnDisclaimer);
    }

    @Override
    public void pluginStarted(String name) {
        if (name.equals(Core.PLUGIN_UI)) {
            ((UIPlugin) getCore().getPlugin(Core.PLUGIN_UI)).registerUI(this);
        }
    }

    @Override
    public void coreMessage(int resId) {
        mTextMessage.setText(resId);
    }

    @Override
    public void pluginStopped(String name) {
    }

    @Override
    public void coreStopped() {
    }

    @Override
    public void coreStarted() {
        Preferences prefs = getCore().getPreferences();
        if (prefs.isFirstRun()) {
            prefs.setFirstRun(false);
            switchToDisclaimer();
        } else {
            mTextMessage.setText(R.string.loading);
            mCurrentMessage = R.string.loading;
            (new Handler()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    forward();
                }
            }, 50);
        }
    }

    private void switchToDisclaimer() {
        mOnDisclaimer = true;
        mTextMessage.setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.GONE);
        findViewById(R.id.disclaimer).setVisibility(View.VISIBLE);
        getMainView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                forward();
            }
        });
    }

    private void forward() {

        Intent previous = getIntent();

        String[] filePaths = null;
        String fileUrl = null;
        String fileName = null;
        String fileMd5 = null;

        String action = previous.getAction();
        String type = previous.getType();
        if ("application/zip".equals(type) || "*/*".equals(type)) {
            if (Intent.ACTION_SEND.equals(action)) {
                Uri zipUri = (Uri) previous.getParcelableExtra(Intent.EXTRA_STREAM);
                if (zipUri != null) {
                    filePaths = new String[] {getZipPath(zipUri)};
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ArrayList<Uri> zipUris = previous.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (zipUris != null) {
                    List<String> paths = new ArrayList<String>();
                    for (Uri zipUri : zipUris) {
                        if (zipUri != null) {
                            paths.add(getZipPath(zipUri));
                        }
                    }
                    filePaths = paths.toArray(new String[paths.size()]);
                }
            }
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri zipUri = (Uri) previous.getData();
            String scheme = zipUri.getScheme();
            if (ContentResolver.SCHEME_CONTENT.endsWith(scheme)
                    || ContentResolver.SCHEME_FILE.endsWith(scheme)) {
                filePaths = new String[] {getZipPath(zipUri)};
            } else {
                fileUrl = zipUri.toString();
            }
        }
        if (previous.getExtras() != null && previous.getExtras().containsKey(RomUpdater.EXTRA_NOTIFICATION_ID)) {
            fileUrl = previous.getExtras().getString(RomUpdater.EXTRA_URL);
            fileMd5 = previous.getStringExtra(RomUpdater.EXTRA_MD5);
            fileName = previous.getStringExtra(RomUpdater.EXTRA_NAME);
        }

        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (filePaths != null && filePaths.length > 0) {
            intent.putExtra(MainActivity.BUNDLE_FILE_PATHS, filePaths);
        }
        if (fileUrl != null) {
            intent.putExtra(MainActivity.BUNDLE_URL, fileUrl);
            intent.putExtra(MainActivity.BUNDLE_NAME, fileName);
            intent.putExtra(MainActivity.BUNDLE_MD5, fileMd5);
        }
        startActivity(intent);
        finish();
    }

    private String getZipPath(Uri zipUri) {
        String scheme = zipUri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.endsWith(scheme)) {
            return Files.getPathFromUri(getCore(), zipUri);
        } else if (ContentResolver.SCHEME_FILE.endsWith(scheme)) {
            return zipUri.getPath();
        }
        return null;
    }

}
