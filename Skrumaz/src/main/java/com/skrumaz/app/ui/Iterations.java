package com.skrumaz.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skrumaz.app.R;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Iterations extends Fragment {

    private Context mContext;

    public Iterations() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Title
        getActivity().setTitle("Iterations");

        // Get Context
        mContext = getActivity();

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Return View
        return inflater.inflate(R.layout.activity_iteration_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.iteration, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Initiate API Requests
                Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_swipe_down), Toast.LENGTH_LONG).show();
                //populateExpandableListView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
