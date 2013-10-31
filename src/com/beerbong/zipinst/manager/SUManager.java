/*
 * Copyright 2013 ZipInstaller Project
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

package com.beerbong.zipinst.manager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class SUManager extends Manager {

    private static SH su;

    protected SUManager(Context context) {
        super(context);

        if (su == null) {
            su = new SH("su");
        }
    }
    
    public CommandResult runWaitFor(final String s) {
        return su.runWaitFor(s, null);
    }

    public void run(String s, ReadOutCallback callback) {
        su.runWaitFor(s, callback);
    }

    public interface ReadOutCallback {

        public void lineRead(String line);

        public void error(String error);

        public void end();
    }

    class CommandResult {

        public final String stdout;
        public final String stderr;
        public final Integer exit_value;

        CommandResult(final Integer exit_value_in) {
            this(exit_value_in, null, null);
        }

        CommandResult(final Integer exit_value_in, final String stdout_in,
                final String stderr_in) {
            exit_value = exit_value_in;
            stdout = stdout_in;
            stderr = stderr_in;
        }

        public boolean success() {
            return exit_value != null && exit_value == 0;
        }
    }

    class SH {

        private String SHELL = "sh";

        public SH(final String SHELL_in) {
            SHELL = SHELL_in;
        }

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

        public Process run(final String s) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(SHELL);
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

        public CommandResult runWaitFor(final String s, final ReadOutCallback callback) {
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
                                    callback.end();
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
                    }

                } catch (final InterruptedException e) {
                    e.printStackTrace();
                } catch (final NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return new CommandResult(exit_value, stdout, stderr);
        }
    }

    class StreamGobbler extends Thread {

        private ReadOutCallback mCallback;
        private InputStream mInputStream;
        
        protected StreamGobbler(InputStream is, ReadOutCallback callback) {
            mInputStream = is;
            mCallback = callback;
        }

        /**
         * creates readers to handle the text created by the external program
         */
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
}
