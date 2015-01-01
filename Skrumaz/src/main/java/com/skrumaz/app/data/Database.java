package com.skrumaz.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.skrumaz.app.data.Store.Field;
import com.skrumaz.app.data.Store.Table;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class Database extends SQLiteOpenHelper {

    // TAG for logging
    private static final String TAG = "DATABASE";

    // Database Versions
    private static final int SKRUMAZ_106 = 11;
    private static final int SKRUMAZ_108 = 12;

    // Database Setup
    private static final String DATABASE_NAME = "Skrumaz.db";
    private static final int DATABASE_VERSION = SKRUMAZ_108;

    // Database Create SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + Table.USERS + "("
            + Field.USER_ID + " LONG PRIMARY KEY, " + Field.USER_NAME + " TEXT, "
            + Field.USER_EMAIL + " TEXT, " + Field.USER_PHOTO + " BLOB,"
            + "UNIQUE (" + Field.USER_ID + ") ON CONFLICT REPLACE)";

    private static final String CREATE_TABLE_WORKSPACES = "CREATE TABLE " + Table.WORKSPACES + "("
            + Field.WORKSPACE_ID + " LONG PRIMARY KEY, " + Field.TITLE + " TEXT, "
            + Field.REFRESH_DATE + " LONG)";

    private static final String CREATE_TABLE_PROJECTS = "CREATE TABLE " + Table.PROJECTS + "("
            + Field.PROJECT_ID + " LONG PRIMARY KEY, " + Field.WORKSPACE_ID + " LONG, "
            + Field.TITLE + " TEXT, " + Field.REFRESH_DATE + " LONG, "
            + Field.UPDATED + " CHAR(1))";

    private static final String CREATE_TABLE_ITERATIONS = "CREATE TABLE " + Table.ITERATIONS + "("
            + Field.ITERATION_ID + " LONG PRIMARY KEY, " + Field.PROJECT_ID + " LONG, "
            + Field.TITLE + " TEXT, " + Field.REFRESH_DATE + " LONG, "
            + Field.ITERATION_STATUS + " VARCHAR(12), " + Field.UPDATED + " CHAR(1))";

    private static final String CREATE_TABLE_ARTIFACTS = "CREATE TABLE " + Table.ARTIFACTS + "("
            + Field.FORMATTED_ID + " VARCHAR(15) PRIMARY KEY, " + Field.ITERATION_ID + " LONG, "
            + Field.TITLE + " TEXT, " + Field.BLOCKED + " BOOLEAN, "
            + Field.RANK + " VARCHAR(65), " + Field.STATUS + " VARCHAR(12), "
            + Field.MODIFIED_DATE + " LONG, " + Field.OWNER_NAME + " STRING, " + Field.DESCRIPTION + " TEXT)";

    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + Table.TASKS + "("
            + Field.FORMATTED_ID + " VARCHAR(15) PRIMARY KEY, " + Field.PARENT_FORMATTED_ID + " VARCHAR(15), "
            + Field.TITLE + " TEXT, " + Field.BLOCKED + " BOOLEAN, "
            + Field.STATUS + " VARCHAR(12), " + Field.MODIFIED_DATE + " LONG)";

    private static final String CREATE_TABLE_TYPE_DEFINITIONS = "CREATE TABLE " + Table.TYPE_DEFINITIONS + "("
            + Field.DEFINITION_ID + " LONG PRIMARY KEY, " + Field.ELEMENT_NAME + " VARCHAR(256))";

    // Default Constructor
    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Users Table
        Log.v(TAG, CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_USERS);

        // Create Workspaces Table
        Log.v(TAG, CREATE_TABLE_WORKSPACES);
        db.execSQL(CREATE_TABLE_WORKSPACES);

        // Create Projects Table
        Log.v(TAG, CREATE_TABLE_PROJECTS);
        db.execSQL(CREATE_TABLE_PROJECTS);

        // Create Iterations Table
        Log.v(TAG, CREATE_TABLE_ITERATIONS);
        db.execSQL(CREATE_TABLE_ITERATIONS);

        // Create Artifacts Table
        Log.v(TAG, CREATE_TABLE_ARTIFACTS);
        db.execSQL(CREATE_TABLE_ARTIFACTS);

        // Create Tasks Table
        Log.v(TAG, CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_TASKS);

        // Create Tasks Table
        Log.v(TAG, CREATE_TABLE_TYPE_DEFINITIONS);
        db.execSQL(CREATE_TABLE_TYPE_DEFINITIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Upgrade to Skrumaz
        switch (oldVersion) {
            case SKRUMAZ_106:
                emptyDatabase(db);
                break;
            case SKRUMAZ_108:
                newArtifacts(db);
                break;
            default:
                emptyDatabase(db);
                break;
        }
    }

    private void newArtifacts(SQLiteDatabase db) {

        // Drop Tasks / Artifacts Tables
        db.execSQL("DROP TABLE IF EXISTS " + Table.TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.ARTIFACTS);

        // Create Artifacts Table
        Log.v(TAG, CREATE_TABLE_ARTIFACTS);
        db.execSQL(CREATE_TABLE_ARTIFACTS);

        // Create Tasks Table
        Log.v(TAG, CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    public void emptyDatabasePref() {
        emptyDatabase(this.getWritableDatabase());
    }

    private void emptyDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Table.USERS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.WORKSPACES);
        db.execSQL("DROP TABLE IF EXISTS " + Table.PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.ITERATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.ARTIFACTS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + Table.TYPE_DEFINITIONS);
        onCreate(db);
    }
}
