package com.skrumaz.app.tutorial;

import com.skrumaz.app.R;

import java.util.ArrayList;

/**
 * Created by Paito Anderson on 11/16/2013.
 */
public class Images {

    private ArrayList<Integer> imageId;

    public Images() {
        imageId = new ArrayList<Integer>();
        imageId.add(R.drawable.slide1);
        imageId.add(R.drawable.slide2);
        imageId.add(R.drawable.slide3);
    }

    public ArrayList<Integer> getImageItem() {
        return imageId;
    }
}
