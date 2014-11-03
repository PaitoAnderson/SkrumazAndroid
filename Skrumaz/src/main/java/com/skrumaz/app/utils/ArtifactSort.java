package com.skrumaz.app.utils;

import com.skrumaz.app.classes.Artifact;

import java.util.Comparator;

/**
 * Created by Paito Anderson on 14-11-02.
 */
public class ArtifactSort {

    public static class OrderByRank implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o1.getRank().compareTo(o2.getRank());
        }
    }

    public static class OrderByState implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o2.getStatus().compareTo(o1.getStatus());
        }
    }

    public static class OrderById implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o2.getFormattedID().compareTo(o1.getFormattedID());
        }
    }

    public static class OrderByName implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static class OrderByModified implements Comparator<Artifact> {

        @Override
        public int compare(Artifact o1, Artifact o2) {
            return o2.getLastUpdate().compareTo(o1.getLastUpdate());
        }
    }
}
