package com.skrumaz.app.data.Store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skrumaz.app.classes.User;
import com.skrumaz.app.data.Database;

/**
 * Created by Paito Anderson on 2014-03-22.
 */
public class Users extends Database {

    public Users(Context context) {
        super(context);
    }

    public void storeUser(User user) {

        // Open Database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // Empty Users Table
        db.execSQL("DELETE FROM " + Table.USERS);

        // Insert User Record
        ContentValues userValues = new ContentValues();
        userValues.put(Field.USER_ID, user.getOid());
        userValues.put(Field.USER_NAME, user.getName());
        userValues.put(Field.USER_EMAIL, user.getEmail());
        userValues.put(Field.USER_PHOTO, user.getPhoto());
        db.insert(Table.USERS, null, userValues);
    }

    public User getUser() {
        User user = null;

        // Open Database connection
        SQLiteDatabase db = this.getReadableDatabase();

        // Populate artifacts from Database
        Cursor cursor = db.query(Table.USERS,
                new String[] { "*" }, null, null, null, null, null, null);
        while(cursor.moveToNext())
        {
                user = new User(cursor.getString(1));
                user.setOid(cursor.getLong(0));
                user.setEmail(cursor.getString(2));
                user.setPhoto(cursor.getBlob(3));
        }

        return user;
    }

}
