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
            return IterationStatus.RD_PLANNED;
        } else if (status.equalsIgnoreCase("Committed")) {
            return IterationStatus.RD_COMMITTED;
        } else if (status.equalsIgnoreCase("Accepted")) {
            return IterationStatus.RD_ACCEPTED;
        } else {
            return IterationStatus.RD_PLANNED;
        }
    }

    public static String iterationStatusToString(IterationStatus status) {

        if (status == null) {
            return "Planning";
        }

        switch (status)
        {
            case RD_PLANNED:
                return "Planning";
            case RD_COMMITTED:
                return "Committed";
            case RD_ACCEPTED:
                return "Accepted";
            default:
                return "Planning";
        }
    }

    public static int iterationStatusToRes(IterationStatus status) {
        switch (status)
        {
            case RD_PLANNED:
                return R.drawable.pg;
            case RD_COMMITTED:
                return R.drawable.cg;
            case RD_ACCEPTED:
                return R.drawable.ag;
            default:
                return R.drawable.pg;
        }
    }
}
