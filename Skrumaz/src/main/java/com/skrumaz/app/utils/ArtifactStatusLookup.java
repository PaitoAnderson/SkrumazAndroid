package com.skrumaz.app.utils;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Status;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class ArtifactStatusLookup {

    public static Status stringToStatus(String status) {

        // When we move to Java SE 7 we can have our String Switch case ;)
        if (status.equalsIgnoreCase("Defined")) {
            return Status.RD_DEFINED;
        } else if (status.equalsIgnoreCase("In-Progress")) {
            return Status.RD_IN_PROGRESS;
        } else if (status.equalsIgnoreCase("Completed")) {
            return Status.RD_COMPLETED;
        } else if (status.equalsIgnoreCase("Accepted")) {
            return Status.RD_ACCEPTED;
        } else {
            return Status.RD_DEFINED;
        }
    }

    public static String statusToString(Status status) {

        if (status == null) {
            return "Defined";
        }

        switch (status)
        {
            case RD_DEFINED:
                return "Defined";
            case RD_IN_PROGRESS:
                return "In-Progress";
            case RD_COMPLETED:
                return "Completed";
            case RD_ACCEPTED:
                return "Accepted";
            default:
                return "Defined";
        }
    }

    public static int statusToColor(Status status) {

        if (status == null) {
            return R.color.artifact_defined;
        }

        switch (status)
        {
            case RD_DEFINED:
                return R.color.artifact_defined;
            case RD_IN_PROGRESS:
                return R.color.artifact_in_progress;
            case RD_COMPLETED:
                return R.color.artifact_completed;
            case RD_ACCEPTED:
                return R.color.artifact_accepted;
            default:
                return R.color.artifact_defined;
        }
    }
}
