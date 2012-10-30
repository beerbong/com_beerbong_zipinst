package com.beerbong.zipinst.manager;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.Constants;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class FileManager extends UIAdapter {

    private SharedPreferences settings;
    private Activity mActivity;

    protected FileManager(Activity activity) {
        mActivity = activity;
        settings = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0);

        UI.getInstance().addUIListener(this);
    }

    public void onPreferenceClicked(String id) {
        if (Constants.PREFERENCE_CHOOSE_ZIP.equals(id)) {
            PackageManager packageManager = mActivity.getPackageManager();
            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
            test.setType("file/*");
            List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
            if(list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("file/*");
                mActivity.startActivityForResult(intent, Constants.REQUEST_PICK_ZIP);
            } else {
                //No app installed to handle the intent - file explorer required
                Toast.makeText(mActivity, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_PICK_ZIP) {
            if (data == null) {
                //Nothing returned by user, probably pressed back button in file manager
                return;
            }

            String zipPath = data.getData().getEncodedPath();

            if (zipPath.startsWith("/extSdCard") || zipPath.startsWith("/storage/sdcard1") || zipPath.startsWith("/mnt/extsdcard")) {
                // external sdcard not allowed
                Toast.makeText(mActivity, R.string.install_file_manager_intsdcard, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!zipPath.endsWith(".zip")) {
                Toast.makeText(mActivity, R.string.install_file_manager_zip, Toast.LENGTH_SHORT).show();
                return;
            }

            String sdcardPath = new String(zipPath);

            String internalStorage = settings.getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);
            
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(mActivity.getAssets().open("paths.xml"));
                
                NodeList list = doc.getElementsByTagName("path");
                
                for (int i=0;i<list.getLength();i++) {
                    String name = list.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    if (zipPath.startsWith(name)) zipPath = zipPath.replace(name, "/" + internalStorage);
                }
            } catch (Exception ex) {
                Toast.makeText(mActivity, R.string.paths_error, Toast.LENGTH_LONG).show();
            }

            UI.getInstance().addPreference(zipPath, sdcardPath);

        }
    }
}