package com.skrumaz.app.classes;

import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
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
    private List<Task> Tasks = new ArrayList<Task>();
    private Boolean Blocked;
    private Status Status;
    private Date LastUpdate;

    @Override
    public String toString(){
        return String.valueOf(this.Name);
    }

    public static class OrderByRank implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o1.getRank().compareTo(o2.getRank());
        }
    }

    public static class OrderByState implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o1.getStatus().compareTo(o2.getStatus());
        }
    }

    public static class OrderById implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o1.getFormattedID().compareTo(o2.getFormattedID());
        }
    }

    public Artifact(String name) {
        this.Name = name;
    }

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

    public void addTask(Task task) {
        Tasks.add(task);
    }

    public void addTasks(List<Task> tasks) {
        Tasks.addAll(tasks);
    }

    public boolean isBlocked() {
        return Blocked;
    }

    public void setBlocked(Boolean blocked) {
        Blocked = blocked;
    }

    public Status getStatus() {
        return Status;
    }

    public void setStatus(Status status) {
        Status = status;
    }

    public Date getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        LastUpdate = lastUpdate;
    }
}
