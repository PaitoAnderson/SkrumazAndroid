package com.skrumaz.app.utils;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.IterationStatus;

/**
 * Created by Paito Anderson on 12/3/2013.
 */
public class IterationStatusLookup {

    public static IterationStatus stringToIterationStatus(String status) {

        // When we move to Java SE 7 we can have our String Switch case ;)
        if (status.equalsIgnoreCase("Planning")) {
            return IterationStatus.RD_PLANNING;
        } else if (status.equalsIgnoreCase("Committed")) {
            return IterationStatus.RD_COMMITTED;
        } else if (status.equalsIgnoreCase("Accepted")) {
            return IterationStatus.RD_ACCEPTED;
        } else {
            return IterationStatus.RD_PLANNING;
        }
    }

    public static String iterationStatusToString(IterationStatus status) {

        if (status == null) {
            return "Planning";
        }

        switch (status)
        {
            case RD_PLANNING:
                return "Planning";
            case RD_COMMITTED:
                return "Committed";
            case RD_ACCEPTED:
                return "Accepted";
            default:
                return "Planning";
        }
    }

    public static int iterationStatusToColor(IterationStatus status) {

        if (status == null) {
            return R.color.iteration_planning;
        }

        switch (status)
        {
            case RD_PLANNING:
                return R.color.iteration_planning;
            case RD_COMMITTED:
                return R.color.iteration_committed;
            case RD_ACCEPTED:
                return R.color.iteration_accepted;
            default:
                return R.color.iteration_planning;
        }
    }
}
