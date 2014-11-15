package com.skrumaz.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;

/**
 * Created by Paito Anderson on 2013-09-22.
 */
public class Settings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("Settings");

        // Load Settings Template from XML
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);
        addPreferencesFromResource(R.layout.settings);

        String appVersionString = "1.0";
        try {
            appVersionString = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            if (getActivity().getPackageName().endsWith(".dev")) {
                appVersionString += " DEV";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference appVersion = findPreference("appVersion");
        appVersion.setSummary(appVersionString);

        Preference showTeam = findPreference("showTeam");
        showTeam.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Artifacts db = new Artifacts(getActivity());
                db.invalidArtifacts();
                return true;
            }
        });

        Preference username = findPreference("username");
        username.setSummary(Preferences.getUsername(getActivity()));

        Preference resetApp = findPreference("resetApp");
        resetApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                                Preferences.resetApplication(getActivity());

                                // Dismiss Dialog
                                dialog.dismiss();

                                // Sent to Login
                                Intent welcome = new Intent(getActivity(), Welcome.class);
                                startActivity(welcome);
                                //finish(); // Remove Activity from Stack

                            }
                        }).show();

                return true;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(getActivity()).activityStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }
}
