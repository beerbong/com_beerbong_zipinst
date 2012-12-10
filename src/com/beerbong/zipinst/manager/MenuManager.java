package com.beerbong.zipinst.manager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.activities.*;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.Constants;

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
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.recovery:
                i = new Intent(mActivity, Recovery.class);
                break;
            case R.id.sdcard:
                i = new Intent(mActivity, Sdcard.class);
                break;
            case R.id.about:
                i = new Intent(mActivity, About.class);
                break;
            case R.id.donate:
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DONATE_URL));
                break;
            case R.id.exit:
                mActivity.finish();
                return;
        }
        if (i != null) {
            mActivity.startActivity(i);
        }
    }
}