package com.skrumaz.app.classes;

import java.util.List;

/**
 * Created by Paito Anderson on 12/1/2013.
 */
public class Workspace {
    private Long Oid;
    private String Name;

    @Override
    public String toString() {
        return this.Name;
    }

    /*
     * Find in position in List of Workspaces based on Oid
     */
    public static int findOid(List<Workspace> workspaces, Long Oid) {
        int i = 0;
        for (Workspace workspace : workspaces) {
            if (workspace.getOid().equals(Oid)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /*
     * Find in Workspace in List of Workspaces based on Oid
     */
    public static Workspace find(List<Workspace> workspaces, Long Oid) {
        for (Workspace workspace : workspaces) {
            if (workspace.getOid().equals(Oid)) {
                return workspace;
            }
        }
        return new Workspace();
    }

    public Long getOid() {
        return Oid;
    }

    public void setOid(Long oid) {
        Oid = oid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }
}
