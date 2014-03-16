package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-03-15.
 */
public class Workspaces extends Database {

    public Workspaces(Context context) {
        super(context);
    }

    /*
     * Store all Workspaces in list
     */
    public void storeWorkspaces(List<Workspace> workspaces) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear Database in Preparation for new data
        db.execSQL("DELETE FROM " + Table.WORKSPACES);

        // Iterate though all workspaces
        for (Workspace workspace : workspaces) {

            // Setup Values for workspace
            ContentValues iterationValues = new ContentValues();
            iterationValues.put(Field.TITLE, workspace.getName());

            // Try Update then Insert
            int rows = 0;
            try {
                rows = db.update(Table.WORKSPACES, iterationValues, Field.WORKSPACE_ID + " = " + workspace.getOid(), null);
            } finally {
                if (rows == 0)
                {
                    iterationValues.put(Field.WORKSPACE_ID, workspace.getOid());
                    iterationValues.put(Field.REFRESH_DATE, 0);
                    db.insert(Table.WORKSPACES, null, iterationValues);
                }
            }
        }

        // Close database connection
        db.releaseReference();
    }

    /*
     * Pull all Workspaces from the database and put them in a usable list
     */
    public List<Workspace> getWorkspaces() {

        List<Workspace> workspaces = new ArrayList<Workspace>();

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate workspaces from Database
        Cursor cursor = db.query(Table.WORKSPACES + " Order BY " + Field.WORKSPACE_ID + " DESC",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Workspace workspace = new Workspace();
                workspace.setOid(cursor.getLong(0));
                workspace.setName(cursor.getString(1));

                if (!cursor.isNull(1)) {
                    workspaces.add(workspace);
                }

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Close Database connection
        db.releaseReference();

        return workspaces;
    }
}
