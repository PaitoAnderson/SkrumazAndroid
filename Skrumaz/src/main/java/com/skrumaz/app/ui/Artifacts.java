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
public class Artifacts extends Fragment {

    public Artifacts() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        getActivity().setTitle("Artifacts");
        return inflater.inflate(R.layout.activity_artifact_list, container, false);
    }

}
