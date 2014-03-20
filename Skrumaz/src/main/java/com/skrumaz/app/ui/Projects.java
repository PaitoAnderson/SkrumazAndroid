package com.skrumaz.app.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skrumaz.app.R;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Projects extends Fragment {

    public Projects() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        getActivity().setTitle("Projects");
        return inflater.inflate(R.layout.activity_project_list, container, false);
    }

}