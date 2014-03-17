package com.skrumaz.app.ui;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Projects extends Fragment {

    public Projects() {
        // Empty constructor required for fragment subclasses
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Projects");
    }

}