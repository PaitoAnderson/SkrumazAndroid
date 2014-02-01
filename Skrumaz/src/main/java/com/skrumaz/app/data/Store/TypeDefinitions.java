package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.TypeDefinition;
import com.skrumaz.app.data.Database;
import com.skrumaz.app.data.WebService.GetTypeDefinitions;

import java.util.List;

/**
 * Created by Paito Anderson on 1/26/2014.
 */
public class TypeDefinitions extends Database {

    public TypeDefinitions(Context context) {
        super(context);
    }

    /*
    * Store all Definition IDs in list
    */
    public void storeDefinitions(List<TypeDefinition> typeDefinitions) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear all old TypeDefinition IDs
        db.execSQL("DELETE FROM " + Table.TYPE_DEFINITIONS + ";");

        // Iterate though all definitions
        for (TypeDefinition typeDefinition : typeDefinitions) {

            // Setup Values for Definition
            ContentValues artifactValues = new ContentValues();
            artifactValues.put(Field.DEFINITION_ID, typeDefinition.getOid());
            artifactValues.put(Field.ELEMENTNAME, typeDefinition.getElementName());

            // Insert Row
            db.insert(Table.TYPE_DEFINITIONS, null, artifactValues);
        }

        // Close database connection
        db.releaseReference();
    }

    /*
    * Pull Definition ID for given ElementName
    */
    public Long getDefinition(Context context, String elementName) {

        Long value = getDefinitionDB(elementName);

        // Likely not cached yet
        if (value == null) {
            new GetTypeDefinitions().FetchItems(context);
            value = getDefinitionDB(elementName);
        }

        return value;
    }

    private Long getDefinitionDB(String elementName) {

        Long value = null;

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Query Database
        Cursor cursor = db.query(Table.TYPE_DEFINITIONS + " WHERE " + Field.ELEMENTNAME + " = '" + elementName + "'",
                new String[] { "*" }, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getLong(0);
            } while (cursor.moveToNext());
        }

        // Close Database connection
        db.releaseReference();

        return value;
    }

}
