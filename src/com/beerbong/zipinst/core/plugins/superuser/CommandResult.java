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

public class CommandResult {

    private final String mStdOut;
    private final String mStdErr;
    private final Integer mExitValue;

    protected CommandResult(final Integer exit_value_in) {
        this(exit_value_in, null, null);
    }

    protected CommandResult(final Integer exit_value_in, final String stdout_in,
            final String stderr_in) {
        mExitValue = exit_value_in;
        mStdOut = stdout_in;
        mStdErr = stderr_in;
    }

    public boolean success() {
        return mExitValue != null && mExitValue == 0;
    }

    public String getOutString() {
        return mStdOut;
    }

    public String getErrString() {
        return mStdErr;
    }
}
