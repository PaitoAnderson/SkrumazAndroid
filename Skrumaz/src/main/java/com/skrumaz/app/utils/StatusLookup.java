package com.skrumaz.app.utils;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Status;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class StatusLookup {

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

    public static int statusToRes(Boolean blocked, Status status) {

        if (blocked) {
            switch (status)
            {
                case RD_DEFINED:
                    return R.drawable.dr;
                case RD_IN_PROGRESS:
                    return R.drawable.pr;
                case RD_COMPLETED:
                    return R.drawable.cr;
                case RD_ACCEPTED:
                    return R.drawable.ar;
                default:
                    return R.drawable.dr;
            }
        }
        else
        {
            switch (status)
            {
                case RD_IN_PROGRESS:
                    return R.drawable.pg;
                case RD_COMPLETED:
                    return R.drawable.cg;
                case RD_ACCEPTED:
                    return R.drawable.ag;
                case RD_DEFINED:
                    return R.drawable.dg;
                default:
                    return R.drawable.dg;
            }
        }
    }
}
