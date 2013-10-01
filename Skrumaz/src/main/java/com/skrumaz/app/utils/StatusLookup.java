package com.skrumaz.app.utils;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Status;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class StatusLookup {

    public static Status stringToStatus(String status) {
        // When we move to Java SE 7 we can have our String Switch case ;)
        if (status.equalsIgnoreCase("In-Progress")) {
            return Status.INPROGRESS;
        } else if (status.equalsIgnoreCase("Completed")) {
            return Status.COMPLETED;
        } else if (status.equalsIgnoreCase("Accepted")) {
            return Status.ACCEPTED;
        } else {
            return Status.DEFINED;
        }
    }

    public static String statusToString(Status status) {
        switch (status)
        {
            case INPROGRESS:
                return "In-Progress";
            case COMPLETED:
                return "Completed";
            case ACCEPTED:
                return "Accepted";
            default:
                return "Defined";
        }
    }

    public static int statusToRes(Boolean blocked, Status status) {

        if (blocked) {
            switch (status)
            {
                case INPROGRESS:
                    return R.drawable.pr;
                case COMPLETED:
                    return R.drawable.cr;
                case ACCEPTED:
                    return R.drawable.ar;
                default:
                    return R.drawable.dr;
            }
        }
        else
        {
            switch (status)
            {
                case INPROGRESS:
                    return R.drawable.pg;
                case COMPLETED:
                    return R.drawable.cg;
                case ACCEPTED:
                    return R.drawable.ag;
                default:
                    return R.drawable.dg;
            }
        }
    }
}
