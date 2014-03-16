package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Database;
import com.skrumaz.app.utils.IterationStatusLookup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Paito Anderson on 11/30/2013.
 */
public class Iterations extends Database {

    public Iterations(Context context) {
        super(context);
    }

    /*
     * Store all Iterations in list for Project
     */
    public void storeIterations(List<Iteration> iterations, Project project) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Update Project Refresh date
        ContentValues projectValues = new ContentValues();
        projectValues.put(Field.REFRESH_DATE, System.currentTimeMillis());

        // Try Update then Insert
        int rows = 0;
        try {
            rows = db.update(Table.PROJECTS, projectValues, Field.PROJECT_ID + " = " + project.getOid(), null);
        } finally {
            if (rows == 0) {
                projectValues.put(Field.PROJECT_ID, project.getOid());
                db.insert(Table.PROJECTS, null, projectValues);
            }
        }

        db.execSQL("UPDATE " + Table.ITERATIONS + " SET " + Field.UPDATED + " = 'N' WHERE " + Field.PROJECT_ID + " = " + project.getOid());

        // Iterate though all iterations
        for (Iteration iteration : iterations) {

            // Setup Values for Iteration
            ContentValues iterationValues = new ContentValues();

            iterationValues.put(Field.TITLE, iteration.getName());
            iterationValues.put(Field.PROJECT_ID, project.getOid());
            iterationValues.put(Field.ITERATION_STATUS, IterationStatusLookup.iterationStatusToString(iteration.getIterationStatus()));
            iterationValues.put(Field.UPDATED, "Y");

            // Try Update then Insert
            rows = 0;
            try {
                rows = db.update(Table.ITERATIONS, iterationValues, Field.ITERATION_ID + " = " + iteration.getOid(), null);
            } finally {
                if (rows == 0)
                {
                    iterationValues.put(Field.ITERATION_ID, iteration.getOid());
                    iterationValues.put(Field.REFRESH_DATE, 0);
                    db.insert(Table.ITERATIONS, null, iterationValues);
                }
            }
        }

        // Delete any iteration in this project that weren't updated (prevents deleted iterations from showing up)
        db.execSQL("DELETE FROM " + Table.ITERATIONS + " WHERE (" + Field.UPDATED + " = 'N') AND (" + Field.PROJECT_ID + " = " + project.getOid() + ")");

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Iterations from the database and put them in a usable list
     */
    public List<Iteration> getIterations(long projectId) {
        List<Iteration> iterations = new ArrayList<Iteration>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate iterations from Database
        Cursor cursor = db.query(Table.ITERATIONS + " WHERE " + Field.PROJECT_ID + " = " + projectId + " Order BY " + Field.ITERATION_ID + " DESC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {

                Iteration iteration = new Iteration();
                iteration.setName(cursor.getString(2));
                iteration.setOid(cursor.getLong(0));
                iteration.setIterationStatus(IterationStatusLookup.stringToIterationStatus(cursor.getString(4)));
                iterations.add(iteration);

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Close Database connection
        db.releaseReference();

        return iterations;
    }

    public boolean isValidIterations(long projectId) {

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        Date refreshDate = new Date(0);
        Date currentDate = new Date(System.currentTimeMillis());

        // Get Refresh Date
        Cursor cursor = db.query(Table.PROJECTS + " WHERE " + Field.PROJECT_ID + " = " + projectId,
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                refreshDate = new Date(cursor.getLong(3) + (20*60*60*1000)); // 20 Hours
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

    public void invalidIterations() {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Update Refresh Date
        ContentValues iterationValues = new ContentValues();
        iterationValues.put(Field.REFRESH_DATE, 0);
        db.update(Table.PROJECTS, iterationValues, null, null);

        // Close Database connection
        db.releaseReference();
    }
}
