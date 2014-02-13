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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class Shell {

    private static final String TAG = "Shell";

    private String mShell = "su";

    protected Shell() {
    }

    @SuppressWarnings("deprecation")
    private String getStreamLines(final InputStream is) {
        String out = null;
        StringBuffer buffer = null;
        final DataInputStream dis = new DataInputStream(is);

        try {
            if (dis.available() > 0) {
                String line = dis.readLine();
                buffer = new StringBuffer(line);
                while (dis.available() > 0) {
                    line = "\n" + dis.readLine();
                    buffer.append(line);
                }
            }
            dis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (buffer != null) {
            out = buffer.toString();
        }
        return out;
    }

    private Process run(final String s) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(mShell);
            final DataOutputStream toProcess = new DataOutputStream(
                    process.getOutputStream());
            toProcess.writeBytes("exec " + s + "\n");
            toProcess.flush();
        } catch (final Exception e) {
            e.printStackTrace();
            process = null;
        }
        return process;
    }

    public CommandResult runWaitFor(final String s, final ShellCallback callback) {
        final Process process = run(s);
        Integer exit_value = null;
        String stdout = null;
        String stderr = null;
        if (process != null) {
            try {

                if (callback != null) {
                    new StreamGobbler(process.getInputStream(), callback).start();
                    new Thread() {
                        public void run() {
                            try {
                                process.waitFor();
                                Integer exit_value = process.waitFor();
                                String stdout = getStreamLines(process.getInputStream());
                                String stderr = getStreamLines(process.getErrorStream());
                                Log.d(TAG, "Execute \"" + s + "\" (" + exit_value + ") err=\"" + stderr + "\"");
                                callback.end(new CommandResult(exit_value, stdout, stderr));
                            } catch (final InterruptedException e) {
                                e.printStackTrace();
                            } catch (final NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    exit_value = process.waitFor();
                    stdout = getStreamLines(process.getInputStream());
                    stderr = getStreamLines(process.getErrorStream());
                    Log.d(TAG, "Execute \"" + s + "\" (" + exit_value + ") out=\"" + stdout + "\" err=\"" + stderr + "\"");
                }

            } catch (final InterruptedException e) {
                e.printStackTrace();
            } catch (final NullPointerException e) {
                e.printStackTrace();
            }
        }
        return new CommandResult(exit_value, stdout, stderr);
    }

    public boolean test() {
        Process p;
        try {
            p = Runtime.getRuntime().exec(mShell);

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo \"test\" > /system/zipinstaller-tmp.txt\n");
            os.writeBytes("rm -f /system/zipinstaller-tmp.txt\n");

            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
                if (p.exitValue() == 0) {
                    return true;
                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return false;
    }
}
