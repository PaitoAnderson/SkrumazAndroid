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

    // Get RallyDev Password
    public static String getPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString("password", "");
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

    // Get Workspace ID to use
    public static long getWorkspaceId(Context context, boolean force) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long workspace = prefs.getLong("useWorkspace", 0);
        Date workspaceTime = new Date(prefs.getLong("useWorkspaceTime", 0));

        if (force) {
            return workspace;
        }

        if (workspaceTime.after(new Date(System.currentTimeMillis()))) {
            return workspace;
        } else {
            return 0;
        }
    }

    // Set Workspace ID to use
    public static void setWorkspaceId(Context context, long workspaceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("useWorkspace", workspaceId);
        Long currentDate = System.currentTimeMillis() + (20*60*60*1000); //Add 20hours to current time
        prefsEditor.putLong("useWorkspaceTime", currentDate);
        prefsEditor.commit();
    }

    // Get Project ID to use
    public static long getProjectId(Context context, boolean force) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long project = prefs.getLong("useProject", 0);
        Date projectTime = new Date(prefs.getLong("useProjectTime", 0));

        if (force) {
            return project;
        }

        if (projectTime.after(new Date(System.currentTimeMillis()))) {
            return project;
        } else {
            return 0;
        }
    }

    // Set Project ID to use
    public static void setProjectId(Context context, long projectId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("useProject", projectId);
        Long currentDate = System.currentTimeMillis() + (20*60*60*1000); //Add 20hours to current time
        prefsEditor.putLong("useProjectTime", currentDate);
        prefsEditor.commit();
    }

    // Get Iteration ID to use
    public static long getIterationId(Context context, boolean force) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long iteration = prefs.getLong("useIteration", 0);
        Date iterationTime = new Date(prefs.getLong("useIterationTime", 0));

        if (force) {
            return iteration;
        }

        if (iterationTime.after(new Date(System.currentTimeMillis()))) {
            return iteration;
        } else {
            return 0;
        }
    }

    // Set Iteration ID to use
    public static void setIterationId(Context context, long iterationId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("useIteration", iterationId);
        Long currentDate = System.currentTimeMillis() + (20*60*60*1000); //Add 20hours to current time
        prefsEditor.putLong("useIterationTime", currentDate);
        prefsEditor.commit();
    }

    // Get Default Sort Order for Artifacts
    public static String getDefaultSort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString("defaultSort", "Rank");
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
        prefsEditor.putBoolean("showTeam", false);
        prefsEditor.putLong("dataExpiry", 0);
        prefsEditor.putString("username", "");
        prefsEditor.putString("password", "");

        prefsEditor.remove("useWorkspace");
        prefsEditor.remove("useWorkspaceTime");
        prefsEditor.remove("useProject");
        prefsEditor.remove("useProjectTime");
        prefsEditor.remove("useIteration");
        prefsEditor.remove("useIterationTime");

        // Empty Database
        Database db = new Database(context);
        db.emptyDatabasePref();
        db.close();

        prefsEditor.commit();
    }

}
