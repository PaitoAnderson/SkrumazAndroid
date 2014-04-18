package com.skrumaz.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;

/**
 * Created by Paito Anderson on 2013-09-22.
 */
public class Settings extends PreferenceActivity {

    private Tracker mTracker;

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
        Preference appVersion = findPreference("appVersion");
        appVersion.setSummary(appVersionString);

        Preference showTeam = findPreference("showTeam");
        showTeam.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Artifacts db = new Artifacts(getBaseContext());
                db.invalidArtifacts();
                return true;
            }
        });

        Preference username = findPreference("username");
        username.setSummary(Preferences.getUsername(getBaseContext()));

        Preference resetApp = findPreference("resetApp");
        resetApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle("Reset Application");
                builder.setMessage("Are you sure you want to reset Skrumaz?").setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Call Reset Function
                                Preferences.resetApplication(getBaseContext());

                                // Dismiss Dialog
                                dialog.dismiss();

                                // Sent to Login
                                Intent welcome = new Intent(getApplicationContext(), Welcome.class);
                                startActivity(welcome);
                                finish(); // Remove Activity from Stack

                            }
                        }).show();

                return true;
            }
        });

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

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Iterations");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
