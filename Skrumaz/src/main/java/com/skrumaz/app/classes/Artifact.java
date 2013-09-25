package com.skrumaz.app.classes;

import java.util.List;

/**
 * Created by Paito Anderson on 2013-09-24.
 *
 * This data class is used for storing user stories and defects
 */
public class Artifact {

    private String FormattedID;
    private String Name;
    private String Rank;
    private List<Task> Tasks;

    public String getFormattedID() {
        return FormattedID;
    }

    public void setFormattedID(String formattedID) {
        this.FormattedID = formattedID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getRank() {
        return Rank;
    }

    public void setRank(String rank) {
        this.Rank = rank;
    }

    public Task getTask(int location) {
        return Tasks.get(location);
    }

    public List<Task> getTasks() {
        return Tasks;
    }

    public void addTask(int location, Task task) {
        Tasks.add(location, task);
    }

    public void addTasks(List<Task> tasks) {
        Tasks.addAll(tasks);
    }
}
