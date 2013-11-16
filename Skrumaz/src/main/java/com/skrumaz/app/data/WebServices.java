package com.skrumaz.app.data;

import android.content.Context;

import com.skrumaz.app.classes.Artifact;

import java.util.List;

/**
 * Created by Paito Anderson on 10/19/2013.
 */
public class WebServices {

    public static List<Artifact> GetItems(Context context) {

        return new com.skrumaz.app.data.RallyDev.GetItems().FetchItems(context);

    }
}
