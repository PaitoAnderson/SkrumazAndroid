package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Database;
import com.skrumaz.app.utils.IterationStatusLookup;
import com.skrumaz.app.utils.StatusLookup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Paito Anderson on 11/30/2013.
 */
public class Artifacts extends Database {

    public Artifacts(Context context) {
        super(context);
    }

    /*
     * Store all Artifacts in list with tasks
     */
    public void storeArtifacts(List<Artifact> artifacts, Iteration iteration) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear Database in Preparation for new data
        String whereIn = "SELECT " + Field.FORMATTED_ID + " FROM " + Table.ARTIFACTS + " WHERE " + Field.ITERATION_ID + " = " + iteration.getOid();

        db.execSQL("DELETE FROM " + Table.TASKS + " WHERE " + Field.PARENT_FORMATTED_ID + " IN (" + whereIn + ");");
        db.execSQL("DELETE FROM " + Table.ARTIFACTS + " WHERE " + Field.ITERATION_ID + " = " + iteration.getOid() + ";");

        // Update Iteration Refresh Date
        ContentValues iterationValues = new ContentValues();
        iterationValues.put(Field.REFRESH_DATE, System.currentTimeMillis());

        int rows = 0;
        try {
            // Update Record
            rows = db.update(Table.ITERATIONS, iterationValues, Field.ITERATION_ID + " = " + iteration.getOid(), null);
        }

        finally {
            if (rows == 0) {
                // Insert record
                iterationValues.put(Field.ITERATION_ID, iteration.getOid());
                iterationValues.put(Field.TITLE, iteration.getName());
                iterationValues.put(Field.ITERATION_STATUS, IterationStatusLookup.iterationStatusToString(iteration.getIterationStatus()));
                        db.insert(Table.ITERATIONS, null, iterationValues);
            }
        }

        // Iterate though all artifacts (US/DE)
        for (Artifact artifact : artifacts) {

            // Setup Values for Artifact
            ContentValues artifactValues = new ContentValues();
            artifactValues.put(Field.FORMATTED_ID, artifact.getFormattedID());
            artifactValues.put(Field.ITERATION_ID, iteration.getOid());
            artifactValues.put(Field.TITLE, artifact.getName());
            artifactValues.put(Field.BLOCKED, artifact.isBlocked());
            artifactValues.put(Field.RANK, artifact.getRank());
            artifactValues.put(Field.STATUS, StatusLookup.statusToString(artifact.getStatus()));
            artifactValues.put(Field.MODIFIED_DATE, artifact.getLastUpdate().getTime());

            // Insert Row
            try {
                db.insertOrThrow(Table.ARTIFACTS, null, artifactValues);
            } catch (SQLiteConstraintException e) {
                db.update(Table.ARTIFACTS, artifactValues, Field.FORMATTED_ID + " = '" + artifact.getFormattedID() + "'", null);
            }

            // Iterate though all tasks for this artifact (TA)
            for (Task task : artifact.getTasks()) {

                // Setup Values for Task
                ContentValues taskValues = new ContentValues();
                taskValues.put(Field.FORMATTED_ID, task.getFormattedID());
                taskValues.put(Field.PARENT_FORMATTED_ID, artifact.getFormattedID());
                taskValues.put(Field.TITLE, task.getName());
                //taskValues.put(Field.BLOCKED, task.isBlocked());
                //taskValues.put(Field.STATUS, StatusLookup.statusToString(task.getStatus()));
                //taskValues.put(Field.MODIFIED_DATE, task.getLastUpdate().getTime());

                // Insert Row
                try {
                    db.insertOrThrow(Table.TASKS, null, taskValues);
                } catch (SQLiteConstraintException e) {
                    db.update(Table.TASKS, taskValues, Field.FORMATTED_ID + " = '" + task.getFormattedID() + "'", null);
                }
            }
        }

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Artifacts / Tasks from the database and put them in a usable list
     */
    public List<Artifact> getArtifacts(Long iterationId) {
        List<Artifact> artifacts = new ArrayList<Artifact>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate artifacts from Database
        Cursor cursor = db.query(Table.ARTIFACTS + " WHERE (" + Field.ITERATION_ID + " = " + iterationId + ") Order BY " + Field.RANK + " ASC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Artifact artifact = new Artifact(cursor.getString(2));
                artifact.setFormattedID(cursor.getString(0));
                artifact.setBlocked(cursor.getInt(3)>0);
                artifact.setRank(cursor.getString(4));
                artifact.setStatus(StatusLookup.stringToStatus(cursor.getString(5)));
                artifact.setLastUpdate(new Date(cursor.getLong(6)));

                Cursor cursor1 = db.query(Table.TASKS + " WHERE (" + Field.PARENT_FORMATTED_ID + " = '" + artifact.getFormattedID() + "')",
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

    public boolean isValidArtifacts(long iterationId) {

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        Date refreshDate = new Date(0);
        Date currentDate = new Date(System.currentTimeMillis());

        // Get Refresh Date
        Cursor cursor = db.query(Table.ITERATIONS + " WHERE " + Field.ITERATION_ID + " = " + iterationId,
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                refreshDate = new Date(cursor.getLong(3) + (15*60*1000)); // 15 Minutes
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
        iterationValues.put(Field.REFRESH_DATE, 0);
        db.update(Table.ITERATIONS, iterationValues, null, null);

        // Close Database connection
        db.releaseReference();
    }
}
