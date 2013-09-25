package com.skrumaz.app;

import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.skrumaz.app.data.Preferences;

/**
 * Created by Paito Anderson on 2013-09-22.
 */
public class Settings extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Settings Template from XML
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);
        addPreferencesFromResource(R.layout.settings);

        String appVersionString = "1.0";
        try {
            appVersionString = getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference appVersion = ((Preference) findPreference("PrefAppVersion"));
        appVersion.setSummary(appVersionString);

        // Add back button icon
        ActionBar actionbar = getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle("Settings");

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
