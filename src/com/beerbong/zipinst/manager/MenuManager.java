package com.beerbong.zipinst.manager;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.activities.*;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class MenuManager extends UIAdapter {

    private Activity mActivity;

    protected MenuManager(Activity activity) {
        mActivity = activity;

        UI.getInstance().addUIListener(this);
    }

    public void onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
    }
    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(mActivity, Settings.class);
                mActivity.startActivity(i);
                break;
            case R.id.backup:
                Manager.getRebootManager().showBackupDialog();
                break;
            case R.id.restore:
                Manager.getRebootManager().showRestoreDialog();
                break;
            case R.id.loadlist:
                Manager.getFileManager().loadList();
                break;
            case R.id.savelist:
                Manager.getFileManager().saveList();
                break;
            case R.id.exit:
                mActivity.finish();
                return;
        }
    }
}