package com.skrumaz.app.data;

import android.content.Context;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Service;

import java.util.List;

/**
 * Created by Paito Anderson on 10/19/2013.
 */
public class WebServices {

    public static List<Artifact> GetItems(Service service, Context context) {
        switch (service)
        {
            case PIVOTAL_TRACKER:
                return new com.skrumaz.app.data.PivotalTracker.GetItems().FetchItems(context);
            default: //RALLY_DEV
                return new com.skrumaz.app.data.RallyDev.GetItems().FetchItems(context);
        }
    }
}
