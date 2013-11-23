package com.skrumaz.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.utils.StatusLookup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class Store extends SQLiteOpenHelper {

    // TAG for logging
    private static final String TAG = "DATABASE";

    // Database Setup
    private static final String DATABASE_NAME = "Skrumaz.db";
    private static final int DATABASE_VERSION = 3;

    // Database Tables
    private static final String TABLE_ITERATIONS = "Iterations";
    private static final String TABLE_ARTIFACTS = "Artifacts";
    private static final String TABLE_TASKS = "Tasks";

    // Database Fields
    private static final String KEY_ITERATION_ID = "IterationID";
    private static final String KEY_FORMATTED_ID = "FormattedID";
    private static final String KEY_PARENT_FORMATTED_ID = "ParentFormattedID";
    private static final String KEY_TITLE = "Title";
    private static final String KEY_BLOCKED = "Blocked";
    private static final String KEY_RANK = "Rank";
    private static final String KEY_STATUS = "Status";
    private static final String KEY_MODIFIED_DATE = "ModifiedDate";
    private static final String KEY_REFRESH_DATE = "RefreshDate";

    // Database Create SQL
    private static final String CREATE_TABLE_ITERATIONS = "CREATE TABLE " + TABLE_ITERATIONS +
            "(" + KEY_ITERATION_ID + " LONG PRIMARY KEY, " + KEY_TITLE + " TEXT, " + KEY_REFRESH_DATE + " LONG)";

    private static final String CREATE_TABLE_ARTIFACTS = "CREATE TABLE " + TABLE_ARTIFACTS +
            "(" + KEY_FORMATTED_ID + " VARCHAR(15) PRIMARY KEY, " + KEY_ITERATION_ID + " LONG," + KEY_TITLE + " TEXT, " +
            KEY_BLOCKED + " BOOLEAN, " + KEY_RANK + " VARCHAR(65), " + KEY_STATUS + " VARCHAR(12), " + KEY_MODIFIED_DATE + " LONG)";

    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS +
            "(" + KEY_FORMATTED_ID + " VARCHAR(15) PRIMARY KEY, " + KEY_PARENT_FORMATTED_ID + " VARCHAR(15), " + KEY_TITLE + " TEXT, " +
            KEY_BLOCKED + " BOOLEAN, " + KEY_STATUS + " VARCHAR(12), " + KEY_MODIFIED_DATE + " LONG)";

    // Default Constructor
    public Store(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Iterations Table
        Log.v(TAG, CREATE_TABLE_ITERATIONS);
        db.execSQL(CREATE_TABLE_ITERATIONS);

        // Create Artifacts Table
        Log.v(TAG, CREATE_TABLE_ARTIFACTS);
        db.execSQL(CREATE_TABLE_ARTIFACTS);

        // Create Tasks Table
        Log.v(TAG, CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITERATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTIFACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    /*
     * Store all Artifacts in list with tasks
     */
    public void storeArtifacts(List<Artifact> artifacts) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear Database in Preparation for new data
        db.execSQL("DELETE FROM " + TABLE_ARTIFACTS);
        db.execSQL("DELETE FROM " + TABLE_TASKS);

        // Update Refresh Date
        ContentValues iterationValues = new ContentValues();
        iterationValues.put(KEY_REFRESH_DATE, System.currentTimeMillis());

        int rows = 0;
        try {
            // Update Record
            rows = db.update(TABLE_ITERATIONS, iterationValues, KEY_ITERATION_ID + " = " + artifacts.get(0).getIteration(), null);
        }

        finally {
            if (rows == 0) {
                // Insert record
                iterationValues.put(KEY_ITERATION_ID, artifacts.get(0).getIteration());
                db.insert(TABLE_ITERATIONS, null, iterationValues);
            }
        }

        // Iterate though all artifacts (US/DE)
        for (Artifact artifact : artifacts) {

            // Setup Values for Artifact
            ContentValues artifactValues = new ContentValues();
            artifactValues.put(KEY_FORMATTED_ID, artifact.getFormattedID());
            artifactValues.put(KEY_ITERATION_ID, artifact.getIteration());
            artifactValues.put(KEY_TITLE, artifact.getName());
            artifactValues.put(KEY_BLOCKED, artifact.isBlocked());
            artifactValues.put(KEY_RANK, artifact.getRank());
            artifactValues.put(KEY_STATUS, StatusLookup.statusToString(artifact.getStatus()));
            artifactValues.put(KEY_MODIFIED_DATE, artifact.getLastUpdate().getTime());

            // Insert Row
            db.insert(TABLE_ARTIFACTS, null, artifactValues);

            // Iterate though all tasks for this artifact (TA)
            for (Task task : artifact.getTasks()) {

                // Setup Values for Task
                ContentValues taskValues = new ContentValues();
                taskValues.put(KEY_FORMATTED_ID, task.getFormattedID());
                taskValues.put(KEY_PARENT_FORMATTED_ID, artifact.getFormattedID());
                taskValues.put(KEY_TITLE, task.getName());
                //taskValues.put(KEY_BLOCKED, task.isBlocked());
                //taskValues.put(KEY_STATUS, StatusLookup.statusToString(task.getStatus()));
                //taskValues.put(KEY_MODIFIED_DATE, task.getLastUpdate().getTime());

                // Insert Row
                db.insert(TABLE_TASKS, null, taskValues);
            }
        }

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Artifacts / Tasks from the database and put them in a usable list
     */
    public List<Artifact> getArtifacts() {
        List<Artifact> artifacts = new ArrayList<Artifact>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate artifacts from Database
        Cursor cursor = db.query(TABLE_ARTIFACTS + " Order BY Rank ASC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Artifact artifact = new Artifact(cursor.getString(2));
                artifact.setFormattedID(cursor.getString(0));
                artifact.setIteration(cursor.getLong(1));
                artifact.setBlocked(cursor.getInt(3)>0);
                artifact.setRank(cursor.getString(4));
                artifact.setStatus(StatusLookup.stringToStatus(cursor.getString(5)));
                artifact.setLastUpdate(new Date(cursor.getLong(6)));

                Cursor cursor1 = db.query(TABLE_TASKS + " WHERE (" + KEY_PARENT_FORMATTED_ID + " = '" + artifact.getFormattedID() + "')",
                        new String[] { "*" }, null, null, null, null, null);
                if (cursor1.moveToFirst()) {
                    do {
                        Task task = new Task(cursor1.getString(2));
                        task.setFormattedID(cursor1.getString(0));
                        artifact.addTask(task);

                    } while (cursor1.moveToNext());
                }
                if (cursor1 != null && !cursor1.isClosed()) {
                    cursor1.close();
                }

                artifacts.add(artifact);

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Close Database connection
        db.releaseReference();

        return artifacts;
    }

    public boolean isValidArtifacts(long IterationId) {

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        Date refreshDate = new Date(0);
        Date currentDate = new Date(System.currentTimeMillis());

        // Get Refresh Date
        Cursor cursor = db.query(TABLE_ITERATIONS + " WHERE (" + KEY_ITERATION_ID + " = " + IterationId + ")",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                refreshDate = new Date(cursor.getLong(2) + (15*60*1000));
            } while (cursor.moveToNext());
        }

        // Close Database connection
        db.releaseReference();

        if (currentDate.after(refreshDate)) {
            return true;
        } else {
            return false;
        }
    }

    public void invalidArtifacts() {

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Update Refresh Date
        ContentValues iterationValues = new ContentValues();
        iterationValues.put(KEY_REFRESH_DATE, 0);
        db.update(TABLE_ITERATIONS, iterationValues, null, null);

        // Close Database connection
        db.releaseReference();
    }
}
