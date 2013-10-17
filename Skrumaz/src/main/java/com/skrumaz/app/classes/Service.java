package com.skrumaz.app.classes;

/**
 * Created by Paito Anderson on 10/16/2013.
 */
public enum Service {
    RALLY_DEV, PIVOTAL_TRACKER;

    public static Service toService (String myEnumString) {
        try {
            return valueOf(myEnumString);
        } catch (Exception ex) {
            // For error cases
            return RALLY_DEV;
        }
    }
}

