package com.skrumaz.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.sql.Date;

/**
 * Created by Paito Anderson on 2013-09-22.
 */
public class Preferences {

    // Identify Shared Preference Store
    public final static String PREFS_NAME = "skrumaz_prefs";

    // Get RallyDev Credentials
    public static String getCredentials(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return "Basic " + Base64.encodeToString((prefs.getString("username", "") + ":" + prefs.getString("password", "")).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
    }

    // Set RallyDev Credentials
    public static void setCredentials(Context context, String username, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("username", username);
        prefsEditor.putString("password", password);
        prefsEditor.commit();
    }

    // Get RallyDev Username
    public static String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString("username", "");
    }

    // Check if app is setup
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String username = prefs.getString("username", "");

        if (username.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    // Get Iteration ID to use
    public static long getIterationID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long iteration = prefs.getLong("useIteration", 0);
        Date iterationTime = new Date(prefs.getLong("useIterationTime", 0));

        if (iterationTime.after(new Date(System.currentTimeMillis()))) {
            return iteration;
        } else {
            return 0;
        }
    }

    // Set Iteration ID to use
    public static void setIterationID(Context context, long IterationId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("useIteration", IterationId);
        Long currentDate = System.currentTimeMillis() + (20*60*60*1000); //Add 20hours to current time
        prefsEditor.putLong("useIterationTime", currentDate);
        prefsEditor.commit();
    }

    // Has the artifact data expired
    public static boolean isDataExpired(Context context, String item) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Date expiryDate = new Date(prefs.getLong("de-" + item, 0));

        if (expiryDate.after(new Date(System.currentTimeMillis()))) {
            return false;
        } else {
            return true;
        }
    }

    // Set expiry date for the data that was just downloaded
    public static void setDataExpiry(Context context, String item) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Long currentDate = System.currentTimeMillis() + (15*60*1000); //Add 15minutes to current time
        prefsEditor.putLong("de-" + item, currentDate);
        prefsEditor.commit();
    }

    // Expire previously downloaded data
    public static void invalidateData(Context context, String item) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("de-" + item, 0);
        prefsEditor.commit();
    }

    // Get Default Sort Order for Artifacts
    public static String getDefaultSort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString("defaultSort", "Rank");
    }

    // Show Formatted ID's with US/DE/TA's
    public static boolean showFormattedID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean("showIDs", false);
    }

    // Show Artifacts for all Owners in Iteration
    public static boolean showAllOwners(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean("showTeam", false);
    }

    public static void resetApplication(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("defaultSort", "Rank");
        prefsEditor.putBoolean("showIDs", false);
        prefsEditor.putBoolean("showTeam", false);
        prefsEditor.putLong("dataExpiry", 0);
        prefsEditor.putString("username", "");
        prefsEditor.putString("password", "");
        prefsEditor.commit();
    }

}
