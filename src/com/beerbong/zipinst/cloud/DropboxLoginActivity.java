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

package com.beerbong.zipinst.cloud;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.preferences.Preferences;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropboxLoginActivity extends Activity {

    private static String sDropboxKey = "30sf9jomssqj6x8";
    private static String sDropboxSecret = null;
    private static AccessType sDropboxAccess = AccessType.APP_FOLDER;

    private boolean mWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DropboxStorage.mDBApi = createDropboxAPI();

        if (DropboxStorage.mDBApi.getSession().getAccessTokenPair() == null) {
            DropboxStorage.mDBApi.getSession().startAuthentication(this);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DropboxStorage.mDBApi.getSession().authenticationSuccessful()) {
            try {
                DropboxStorage.mDBApi.getSession().finishAuthentication();

                Preferences prefs = CoreFactory.getCore().getPreferences();
                AccessTokenPair tokens = DropboxStorage.mDBApi.getSession().getAccessTokenPair();
                prefs.setDropboxKey(tokens.key);
                prefs.setDropboxSecret(tokens.secret);

                finish();

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        } else {
            if (mWaiting) {
                finish();
            }
            mWaiting = true;
        }
    }

    private DropboxAPI<AndroidAuthSession> createDropboxAPI() {
        AppKeyPair appKeys = new AppKeyPair(sDropboxKey, getDropboxSecret(this));
        AndroidAuthSession session = new AndroidAuthSession(appKeys, sDropboxAccess);
        DropboxAPI<AndroidAuthSession> dBApi = new DropboxAPI<AndroidAuthSession>(session);
        Preferences prefs = CoreFactory.getCore().getPreferences();
        String key = prefs.getDropboxKey();
        String secret = prefs.getDropboxSecret();
        if (key != null && secret != null) {
            AccessTokenPair access = new AccessTokenPair(key, secret);
            dBApi.getSession().setAccessTokenPair(access);
        }
        return dBApi;
    }

    private String getDropboxSecret(Context context) {
        if (sDropboxSecret == null) {
            sDropboxSecret = Files.readAssets(context, "dropbox");
        }
        return sDropboxSecret;
    }

}
