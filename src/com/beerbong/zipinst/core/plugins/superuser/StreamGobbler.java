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

package com.beerbong.zipinst.core.plugins.superuser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

    private ShellCallback mCallback;
    private InputStream mInputStream;
    
    protected StreamGobbler(InputStream is, ShellCallback callback) {
        mInputStream = is;
        mCallback = callback;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(mInputStream);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                mCallback.lineRead(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            mCallback.error(ioe.getMessage());
        }
    }
}