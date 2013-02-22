package com.beerbong.zipinst.manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import android.content.Context;

public class SUManager extends Manager {

    private SH su;

    protected SUManager(Context context) {
        super(context);
        
        su = new SH("su");
    }
    
    public CommandResult runWaitFor(final String s) {
        return su.runWaitFor(s);
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
                    buffer = new StringBuffer(dis.readLine());
                    while (dis.available() > 0) {
                        buffer.append("\n").append(dis.readLine());
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

        public CommandResult runWaitFor(final String s) {
            final Process process = run(s);
            Integer exit_value = null;
            String stdout = null;
            String stderr = null;
            if (process != null) {
                try {
                    exit_value = process.waitFor();

                    stdout = getStreamLines(process.getInputStream());
                    stderr = getStreamLines(process.getErrorStream());

                } catch (final InterruptedException e) {
                    e.printStackTrace();
                } catch (final NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return new CommandResult(exit_value, stdout, stderr);
        }
    }
}
