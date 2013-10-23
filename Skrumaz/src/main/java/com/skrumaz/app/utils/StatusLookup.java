package com.skrumaz.app.utils;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Service;
import com.skrumaz.app.classes.Status;

/**
 * Created by Paito Anderson on 2013-09-30.
 */
public class StatusLookup {

    public static Status stringToStatus(String status, Service service) {

        if (service.equals(Service.RALLY_DEV))
        {
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
        else if (service.equals(Service.PIVOTAL_TRACKER))
        {
            // When we move to Java SE 7 we can have our String Switch case ;)
            if (status.equalsIgnoreCase("unscheduled")) {
                return Status.PT_UN_SCHEDULED;
            } else if (status.equalsIgnoreCase("unstarted")) {
                return Status.PT_UN_STARTED;
            } else if (status.equalsIgnoreCase("started")) {
                return Status.PT_STARTED;
            } else if (status.equalsIgnoreCase("finished")) {
                return Status.PT_FINISHED;
            } else if (status.equalsIgnoreCase("delivered")) {
                return Status.PT_DELIVERED;
            } else if (status.equalsIgnoreCase("accepted")) {
                return Status.PT_ACCEPTED;
            } else if (status.equalsIgnoreCase("rejected")) {
                return Status.PT_REJECTED;
            } else {
                return Status.PT_UN_SCHEDULED;
            }
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
            case PT_UN_SCHEDULED:
                return "unscheduled";
            case PT_UN_STARTED:
                return "unstarted";
            case PT_STARTED:
                return "started";
            case PT_FINISHED:
                return "finished";
            case PT_DELIVERED:
                return "delivered";
            case PT_ACCEPTED:
                return "accepted";
            case PT_REJECTED:
                return "rejected";
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
                case PT_UN_SCHEDULED:
                    return R.drawable.pt_unsheduled;
                case PT_UN_STARTED:
                    return R.drawable.pt_unstarted;
                case PT_STARTED:
                    return R.drawable.pt_started;
                case PT_FINISHED:
                    return R.drawable.pt_finished;
                case PT_DELIVERED:
                    return R.drawable.pt_delivered;
                case PT_ACCEPTED:
                    return R.drawable.pt_accepted;
                case PT_REJECTED:
                    return R.drawable.pt_rejected;
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
                case PT_UN_SCHEDULED:
                    return R.drawable.pt_unsheduled;
                case PT_UN_STARTED:
                    return R.drawable.pt_unstarted;
                case PT_STARTED:
                    return R.drawable.pt_started;
                case PT_FINISHED:
                    return R.drawable.pt_finished;
                case PT_DELIVERED:
                    return R.drawable.pt_delivered;
                case PT_ACCEPTED:
                    return R.drawable.pt_accepted;
                case PT_REJECTED:
                    return R.drawable.pt_rejected;
                default:
                    return R.drawable.dg;
            }
        }
    }
}
