package com.skrumaz.app.classes;

/**
 * Created by Paito Anderson on 2013-09-24.
 */
public class Iteration {
    private Long Oid;
    private String Name;
    private IterationStatus IterationStatus;

    public Long getOid() {
        return Oid;
    }

    public void setOid(Long oid) {
        this.Oid = oid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public IterationStatus getIterationStatus() {
        return IterationStatus;
    }

    public void setIterationStatus(IterationStatus iterationStatus) {
        IterationStatus = iterationStatus;
    }
}
