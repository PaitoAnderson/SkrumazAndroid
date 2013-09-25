package com.skrumaz.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

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

    // Get RallyDev Username
    public static String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString("username", "");
    }
}
