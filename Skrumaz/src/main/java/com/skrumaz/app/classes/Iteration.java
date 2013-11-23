package com.skrumaz.app.classes;

/**
 * Created by Paito Anderson on 2013-09-24.
 *
 * Note: This should be refactored as it's currently RallyDev specific
 */
public class Iteration {
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
