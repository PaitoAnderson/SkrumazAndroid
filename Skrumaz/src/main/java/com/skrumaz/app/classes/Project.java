package com.skrumaz.app.classes;

import java.util.List;

/**
 * Created by Paito Anderson on 11/28/2013.
 */
public class Project {
    private Long Oid;
    private String Name;

    @Override
    public String toString() {
        return this.Name;
    }

    /*
     * Find in position in List of Projects based on Oid
     */
    public static int findOid(List<Project> projects, Long Oid) {
        int i = 0;
        for (Project project : projects) {
            if (project.getOid().equals(Oid)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /*
     * Find in Project in List of Projects based on Oid
     */
    public static Project find(List<Project> projects, Long Oid) {
        for (Project project : projects) {
            if (project.getOid().equals(Oid)) {
                return project;
            }
        }
        return new Project();
    }

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
