package com.skrumaz.app.classes;

/**
 * Created by Paito Anderson on 11/28/2013.
 */
public class Project {
    private Long Oid;
    private String Name;

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
}
