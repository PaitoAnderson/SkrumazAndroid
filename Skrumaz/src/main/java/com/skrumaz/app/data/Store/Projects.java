package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Database;

import java.util.ArrayList;
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
    public void storeProjects(List<Project> projects) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear Database in Preparation for new data
        db.execSQL("DELETE FROM " + Table.PROJECTS);

        // Iterate though all projects
        for (Project project : projects) {

            // Setup Values for project
            ContentValues iterationValues = new ContentValues();
            iterationValues.put(Field.TITLE, project.getName());

            // Try Update then Insert
            int rows = 0;
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

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Projects from the database and put them in a usable list
     */
    public List<Project> getProjects() {

        List<Project> projects = new ArrayList<Project>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate projects from Database
        Cursor cursor = db.query(Table.PROJECTS + " Order BY " + Field.PROJECT_ID + " DESC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();
                project.setOid(cursor.getLong(0));
                project.setName(cursor.getString(1));

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
}
