package com.skrumaz.app.classes;

/**
 * Created by Paito Anderson on 2013-09-24.
 *
 * This data class is used for storing tasks from user stories and defects
 */
public class Task {
    private String Name;
    private String FormattedID;

    public Task(String name) {
        this.Name = name;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getFormattedID() {
        return this.FormattedID;
    }

    public void setFormattedID(String formattedID) {
        this.FormattedID = formattedID;
    }
}
