package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.Project;
import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Paito Anderson on 12/1/2013.
 */
public class Projects extends Database {

    public Projects(Context context) {
        super(context);
    }

    /*
     * Store all Projects in list
     */
    public void storeProjects(List<Project> projects, Workspace workspace) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Update Project Refresh date
        ContentValues workspaceValues = new ContentValues();
        workspaceValues.put(Field.REFRESH_DATE, System.currentTimeMillis());

        // Try Update then Insert
        int rows = 0;
        try {
            rows = db.update(Table.WORKSPACES, workspaceValues, Field.WORKSPACE_ID + " = " + workspace.getOid(), null);
        } finally {
            if (rows == 0) {
                workspaceValues.put(Field.WORKSPACE_ID, workspace.getOid());
                db.insert(Table.WORKSPACES, null, workspaceValues);
            }
        }

        db.execSQL("UPDATE " + Table.PROJECTS + " SET " + Field.UPDATED + " = 'N' WHERE " + Field.WORKSPACE_ID + " = " + workspace.getOid());

        // Iterate though all projects
        for (Project project : projects) {

            // Setup Values for project
            ContentValues iterationValues = new ContentValues();
            iterationValues.put(Field.TITLE, project.getName());
            iterationValues.put(Field.WORKSPACE_ID, workspace.getOid());
            iterationValues.put(Field.UPDATED, "Y");

            // Try Update then Insert
            rows = 0;
            try {
                rows = db.update(Table.PROJECTS, iterationValues, Field.PROJECT_ID + " = " + project.getOid(), null);
            } finally {
                if (rows == 0)
                {
                    iterationValues.put(Field.PROJECT_ID, project.getOid());
                    iterationValues.put(Field.REFRESH_DATE, 0);
                    db.insert(Table.PROJECTS, null, iterationValues);
                }
            }
        }

        // Delete any iteration in this project that weren't updated (prevents deleted iterations from showing up)
        db.execSQL("DELETE FROM " + Table.PROJECTS + " WHERE (" + Field.UPDATED + " = 'N') AND (" + Field.WORKSPACE_ID + " = " + workspace.getOid() + ")");

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Projects from the database and put them in a usable list
     */
    public List<Project> getProjects(long workspaceId) {

        List<Project> projects = new ArrayList<Project>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate projects from Database
        Cursor cursor = db.query(Table.PROJECTS + " WHERE (" + Field.WORKSPACE_ID + " = " + workspaceId + ") ORDER BY " + Field.TITLE + " ASC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();
                project.setOid(cursor.getLong(0));
                project.setName(cursor.getString(2));

                if (!cursor.isNull(1)) {
                    projects.add(project);
                }

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Close Database connection
        db.releaseReference();

        return projects;
    }

    /*
     * Get Project from the database based on Project ID
     */
    public Project getProject(long projectId) {

        Project project = null;

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate projects from Database
        Cursor cursor = db.query(Table.PROJECTS + " WHERE (" + Field.PROJECT_ID + " = " + projectId + ")",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                project = new Project();
                project.setOid(cursor.getLong(0));
                project.setName(cursor.getString(2));

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Close Database connection
        db.releaseReference();

        return project;
    }

    public boolean isValidProjects(long workspaceId) {

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        Date refreshDate = new Date(0);
        Date currentDate = new Date(System.currentTimeMillis());

        // Get Refresh Date
        Cursor cursor = db.query(Table.WORKSPACES + " WHERE " + Field.WORKSPACE_ID + " = " + workspaceId,
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                refreshDate = new Date(cursor.getLong(2) + (20*60*60*1000)); // 20 Hours
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

    public void invalidProjects() {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Update Refresh Date
        ContentValues projectValues = new ContentValues();
        projectValues.put(Field.REFRESH_DATE, 0);
        db.update(Table.WORKSPACES, projectValues, null, null);

        // Close Database connection
        db.releaseReference();
    }
}
